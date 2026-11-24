package com.sarthi.superadmin;

import com.sarthi.entity.UserMaster;
import com.sarthi.service.AzureBlobStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/**
 * Super Admin – "Cancel Last Transaction" of an ERC / Sleeper / Rail Pad call.
 *
 * Only the LATEST workflow transaction of a call can be deleted, one at a time.
 * Together with the row, the data that this step created is removed — the same cleanup the
 * existing "Back to Inspection" / "Back to Issuance of IC" / "Move to Completed" reverts do:
 *   • IC e-sign row              → stored IC (certificate_storage + Azure PDF), IC edit, case letter (soft delete)
 *   • ERC ISSUE_IC / INSPECTION_COMPLETE_CONFIRM, Sleeper & Rail Pad FINISH / GENERATE_IC → IC saved changes
 *   • inspection complete row    → *_inspection_complete_details, call status reset (Sleeper / Rail Pad)
 *   • send-to-IBS row            → the row only
 *   • ERC (every row)            → final_ie_mapping of that transition
 * Every delete is copied to workflow_delete_history with the Super Admin's user id, emp code, name and remarks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminTxnService {

    public static final String SOURCE = "SUPER_ADMIN_CANCEL";

    private final JdbcTemplate jdbc;
    private final AzureBlobStorageService azureBlobStorageService;

    // ───────────────────────────── product routing ─────────────────────────────

    enum Product { ERC, SLEEPER, RAILPAD }

    /** Table + column names of the workflow history table of each product. */
    private record WfTable(String table, String id, String req, String action, String status,
                           String jobStatus, String currentRole, String nextRole, String assigned,
                           String createdBy, String createdDate, String remarks, String workflowId) {}

    private static final WfTable ERC_WF = new WfTable("workflow_transition", "WORKFLOWTRANSITIONID", "REQUESTID",
            "ACTION", "STATUS", "job_status", "CURRENTROLE", "NEXTROLE", "assigned_to_user", "CREATEDBY",
            "CREATEDDATE", "REMARKS", "WORKFLOWID");
    private static final WfTable SLEEPER_WF = new WfTable("sleeper_workflow_transaction", "workflow_transition_id",
            "request_id", "action", "status", "job_status", "current_role", "next_role", "assigned_to_user",
            "created_by", "created_date", "remarks", "workflow_id");
    private static final WfTable RAIL_WF = new WfTable("rail_workflow_transaction", "workflow_transition_id",
            "request_id", "action", "status", "job_status", "current_role", "next_role", "assigned_to_user",
            "created_by", "created_date", "remarks", "workflow_id");

    static Product productOf(String callNo) {
        String c = callNo.toUpperCase();
        if (c.startsWith("SF-") || c.startsWith("SP-")) return Product.SLEEPER;
        if (c.startsWith("RPP-") || c.startsWith("RPF-")) return Product.RAILPAD;
        if (c.startsWith("ER") || c.startsWith("EP") || c.startsWith("EF")) return Product.ERC;
        throw new IllegalArgumentException("Unknown call number format: " + callNo
                + " (expected ER/EP/EF, SF-, RPP- or RPF-)");
    }

    private static WfTable wf(Product p) {
        return switch (p) { case ERC -> ERC_WF; case SLEEPER -> SLEEPER_WF; case RAILPAD -> RAIL_WF; };
    }

    private static String completeDetailsTable(Product p) {
        return switch (p) {
            case ERC -> "inspection_complete_details";
            case SLEEPER -> "sleeper_inspection_complete_details";
            case RAILPAD -> "rail_inspection_complete_details";
        };
    }

    /** [icEditTable, icSaveChangesTable] for the call type. */
    private static String[] icTables(String callNo) {
        String c = callNo.toUpperCase();
        if (c.startsWith("ER")) return new String[]{"rm_ic_edit", "rm_ic_save_changes"};
        if (c.startsWith("EP")) return new String[]{"process_ic_edit", "process_ic_save_changes"};
        if (c.startsWith("EF")) return new String[]{"final_ic_edit", "final_ic_save_changes"};
        if (c.startsWith("SF-") || c.startsWith("SP-")) return new String[]{"sleeper_final_ic_edit", "sleeper_final_ic_save_changes"};
        if (c.startsWith("RPP-")) return new String[]{"railpad_process_ic_edit", "railpad_process_ic_save_changes"};
        if (c.startsWith("RPF-")) return new String[]{"railpad_final_ic_edit", "railpad_final_ic_save_changes"};
        return new String[0];
    }

    // ───────────────────────────── step classification ─────────────────────────────

    /**
     * What to clean up together with the deleted transaction.
     *   saveChanges → IC saved changes
     *       ERC: ISSUE_IC / INSPECTION_COMPLETE_CONFIRM;  Sleeper & Rail Pad: FINISH / GENERATE_IC
     *   esign       → stored IC (certificate_storage + Azure PDF), IC edit, case letter
     *       ERC: DSC_SIGN_IC;  Sleeper: IC_GENERATION (IC e-sign);  Rail Pad: GENERATE_IC (IC e-sign)
     *   complete    → *_inspection_complete_details (+ Sleeper/Rail Pad call status back to pending)
     *       ERC: GENERATE_IC / IC_GENERATION / INSPECTION_COMPLETE*;  Sleeper & Rail Pad: FINISH
     */
    record Cleanup(boolean ibs, boolean saveChanges, boolean esign, boolean complete) {
        String label() {
            if (ibs) return "IBS";
            List<String> l = new ArrayList<>();
            if (esign) l.add("IC_ESIGN");
            if (complete) l.add("INSPECTION_COMPLETE");
            if (saveChanges) l.add("IC_SAVE_CHANGES");
            return l.isEmpty() ? "OTHER" : String.join("+", l);
        }
    }

    static Cleanup classify(Product p, String action, String status) {
        String a = action == null ? "" : action.toUpperCase().trim();
        String s = status == null ? "" : status.toUpperCase().trim();
        String both = a + "|" + s;
        if (both.contains("SEND_CALL_TO_IBS") || both.contains("SENT_TO_IBS") || both.contains("SEND_TO_IBS")) {
            return new Cleanup(true, false, false, false);
        }
        if (p == Product.ERC) {
            boolean save = both.contains("ISSUE_IC") || both.contains("INSPECTION_COMPLETE_CONFIRM");
            boolean esign = both.contains("DSC_SIGN_IC");
            boolean complete = both.contains("GENERATE_IC") || both.contains("IC_GENERATION") || both.contains("INSPECTION_COMPLETE");
            return new Cleanup(false, save, esign, complete);
        }
        boolean save = a.equals("FINISH") || a.equals("GENERATE_IC");
        boolean esign = p == Product.SLEEPER
                ? (a.equals("IC_GENERATION") || a.contains("SIGN"))
                : (a.equals("GENERATE_IC") || a.equals("IC_GENERATION") || a.contains("SIGN"));
        boolean complete = a.equals("FINISH");
        return new Cleanup(false, save, esign, complete);
    }

    // ───────────────────────────── access ─────────────────────────────

    public boolean isSuperAdmin(UserMaster user) {
        if (user == null || user.getUserId() == null) return false;
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_role_master u JOIN role_master r ON r.roleid = u.roleid "
                        + "WHERE u.userid = ? AND LOWER(TRIM(r.rolename)) = 'super admin'",
                Integer.class, user.getUserId());
        return n != null && n > 0;
    }

    // ───────────────────────────── list ─────────────────────────────

    public Map<String, Object> listTransactions(String callNoRaw) {
        String callNo = normCall(callNoRaw);
        Product p = productOf(callNo);
        WfTable t = wf(p);

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT w." + t.id() + " AS id, w." + t.action() + " AS action, w." + t.status() + " AS status, "
                        + "w." + t.jobStatus() + " AS jobStatus, w." + t.currentRole() + " AS currentRole, "
                        + "w." + t.nextRole() + " AS nextRole, w." + t.assigned() + " AS assignedToUser, "
                        + "w." + t.createdBy() + " AS createdBy, w." + t.createdDate() + " AS createdDate, "
                        + "w." + t.remarks() + " AS remarks, "
                        + "(SELECT CONCAT(COALESCE(NULLIF(um.FULL_NAME,''), um.USERNAME), "
                        + "   CASE WHEN COALESCE(um.EMPLOYEE_CODE,'') <> '' THEN CONCAT(' (', um.EMPLOYEE_CODE, ')') ELSE '' END) "
                        + " FROM user_master um WHERE um.USERID = w." + t.assigned() + " LIMIT 1) AS assignedToName "
                        + "FROM " + t.table() + " w WHERE UPPER(w." + t.req() + ") = ? ORDER BY w." + t.id(),
                callNo);

        if (rows.isEmpty()) throw new NoSuchElementException("No transactions found for call " + callNo);

        String icNo = findIcNo(p, callNo);

        // ERC Process: the workflow rows carry no assigned user — the call is worked by every Process IE mapped
        // to the place of inspection (poi_process_ie_mapping, same join as CallSmsSender.resolveErcProcessIe).
        // Shown from call-desk verification onwards, only where the row itself has no assigned user.
        String mappedProcessIes = callNo.startsWith("EP") ? mappedProcessIes(callNo) : null;
        boolean verified = false;

        Map<String, Object> latest = rows.get(rows.size() - 1);
        for (Map<String, Object> r : rows) {
            String st = String.valueOf(r.get("status")).toUpperCase();
            if (st.contains("VERIFIED") || st.contains("CALL_REGISTERED")) verified = true;
            if (mappedProcessIes != null && verified && r.get("assignedToName") == null) {
                r.put("assignedToName", mappedProcessIes);
                r.put("assignedVia", "Process IE mapping");
            }
            boolean isLatest = r == latest;
            r.put("deletable", isLatest);
            Cleanup step = classify(p, str(r.get("action")), str(r.get("status")));
            r.put("step", step.label());
            if (isLatest) r.put("willAlsoRemove", preview(p, callNo, icNo, step));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("callNo", callNo);
        out.put("product", p.name());
        out.put("icNumber", icNo);
        out.put("transactions", rows);
        return out;
    }

    /** "Name (EMP), Name (EMP)" of the Process IEs mapped to the call's place of inspection, or null. */
    private String mappedProcessIes(String callNo) {
        List<String> l = jdbc.queryForList(
                "SELECT DISTINCT CONCAT(TRIM(COALESCE(NULLIF(TRIM(um.FULL_NAME),''), um.USERNAME)), ' (', TRIM(m.employee_code), ')') "
                        + "FROM inspection_calls ic "
                        + "JOIN poi_process_ie_mapping m ON m.poi_code = ic.place_of_inspection "
                        + "JOIN user_master um ON um.EMPLOYEE_CODE = m.employee_code "
                        + "WHERE ic.ic_number = ?", String.class, callNo);
        return l.isEmpty() ? null : String.join(", ", l);
    }

    private List<String> preview(Product p, String callNo, String icNo, Cleanup step) {
        List<String> items = new ArrayList<>();
        String[] ic = icTables(callNo);
        List<String> keys = icKeys(icNo, callNo);
        if (step.ibs()) items.add("Only this transaction (call goes back to Completed)");
        if (step.esign()) {
            items.add("Stored IC in Azure: " + countIn("certificate_storage", "IC_NUMBER", keys) + " certificate record(s) + PDF file");
            if (ic.length == 2) items.add("IC edit: " + countIn(ic[0], "ic_number", keys) + " row(s)");
            items.add("Case letter(s): " + countActiveCaseLetters(callNo) + " (marked DELETED)");
        }
        if (step.saveChanges() && ic.length == 2) {
            items.add("IC saved changes: " + countIn(ic[1], "ic_number", keys) + " row(s)");
        }
        if (step.complete()) {
            items.add("Inspection complete details (IC no. " + (icNo == null ? "-" : icNo) + ")");
            if (p != Product.ERC) items.add("Call status reset to pending");
        }
        if (items.isEmpty()) items.add("Only this transaction");
        if (p == Product.ERC) items.add("Final IE mapping of this transaction");
        return items;
    }

    // ───────────────────────────── delete latest ─────────────────────────────

    @Transactional
    public Map<String, Object> deleteLatest(String callNoRaw, long transitionId, String remarks, UserMaster user) {
        String callNo = normCall(callNoRaw);
        if (remarks == null || remarks.trim().length() < 5) {
            throw new IllegalArgumentException("Remarks are required (at least 5 characters)");
        }
        Product p = productOf(callNo);
        WfTable t = wf(p);

        // 1. Lock the call's rows and make sure the requested one is still the latest
        List<Map<String, Object>> locked = jdbc.queryForList(
                "SELECT * FROM " + t.table() + " WHERE UPPER(" + t.req() + ") = ? ORDER BY " + t.id() + " DESC FOR UPDATE",
                callNo);
        if (locked.isEmpty()) throw new NoSuchElementException("No transactions found for call " + callNo);
        Map<String, Object> row = locked.get(0);
        long latestId = ((Number) ci(row, t.id())).longValue();
        if (latestId != transitionId) {
            throw new IllegalStateException("Only the latest transaction can be cancelled. Latest is " + latestId
                    + " — refresh the list and try again.");
        }

        String action = str(ci(row, t.action()));
        String status = str(ci(row, t.status()));
        Cleanup step = classify(p, action, status);
        String icNo = findIcNo(p, callNo);
        List<String> keys = icKeys(icNo, callNo);
        String[] ic = icTables(callNo);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("step", step.label());
        summary.put("icNumber", icNo);
        final List<String> blobFiles = new ArrayList<>();

        // 2. Clean up what this step created
        if (step.esign()) {
            // stored IC in Azure, IC edit, case letter
            blobFiles.addAll(selectIn("SELECT FILE_NAME FROM certificate_storage WHERE IC_NUMBER IN ", keys));
            summary.put("certificateStorage", deleteIn("certificate_storage", "IC_NUMBER", keys));
            summary.put("azureFiles", blobFiles);
            if (ic.length == 2) summary.put(ic[0], deleteIn(ic[0], "ic_number", keys));
            summary.put("caseLetters", jdbc.update(
                    "UPDATE case_letter_document SET STATUS = 'DELETED' WHERE UPPER(CALL_NO) = ? AND STATUS = 'ACTIVE'", callNo));
        }
        if (step.saveChanges() && ic.length == 2) {
            summary.put(ic[1], deleteIn(ic[1], "ic_number", keys));
        }
        if (step.complete()) {
            summary.put(completeDetailsTable(p), jdbc.update(
                    "DELETE FROM " + completeDetailsTable(p) + " WHERE UPPER(CALL_NO) = ?", callNo));
            if (p == Product.SLEEPER) {
                summary.put("callStatus", jdbc.update(
                        "UPDATE sleeper_inspection_call SET status = 'Pending for verification' WHERE UPPER(call_no) = ?", callNo));
            } else if (p == Product.RAILPAD) {
                summary.put("callStatus", jdbc.update(
                        "UPDATE rail_inspection_call SET status = 'PENDING' WHERE UPPER(call_no) = ?", callNo));
            }
        }
        if (p == Product.ERC) {
            summary.put("finalIeMapping", jdbc.update(
                    "DELETE FROM final_ie_mapping WHERE WORKFLOW_TRANSITION_ID = ?", latestId));
        }

        // 3. Audit (fails → whole delete rolls back)
        String empCode = user.getEmployeeCode();
        String name = (user.getFullName() != null && !user.getFullName().isBlank()) ? user.getFullName() : user.getUsername();
        jdbc.update("INSERT INTO workflow_delete_history (REQUEST_ID, REQUEST_TYPE, WORKFLOW_TRANSITION_ID, WORKFLOW_ID, "
                        + "CURRENT_ROLE, NEXT_ROLE, STATUS, ACTION, REMARKS, CREATED_BY, ASSIGNED_TO_USER, JOB_STATUS, "
                        + "TRANSITION_CREATED_DATE, CERTIFICATE_NO, DELETED_BY, DELETED_ON, DELETED_BY_EMP_CODE, DELETED_BY_NAME, "
                        + "DELETE_REMARKS, DELETE_SOURCE, CLEANUP_SUMMARY) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),?,?,?,?,?)",
                callNo, p.name(), (int) latestId, intOrNull(ci(row, t.workflowId())),
                str(ci(row, t.currentRole())), str(ci(row, t.nextRole())), status, action, str(ci(row, t.remarks())),
                intOrNull(ci(row, t.createdBy())), intOrNull(ci(row, t.assigned())), str(ci(row, t.jobStatus())),
                ci(row, t.createdDate()), icNo, user.getUserId(), empCode, name, remarks.trim(), SOURCE, toJson(summary));

        // 4. Delete the transaction row
        jdbc.update("DELETE FROM " + t.table() + " WHERE " + t.id() + " = ?", latestId);

        // 5. Azure files only after the DB commit succeeded
        if (!blobFiles.isEmpty() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    for (String f : blobFiles) {
                        try { azureBlobStorageService.deleteFile(f); }
                        catch (Exception e) { log.warn("Super Admin cancel: could not delete blob {}: {}", f, e.getMessage()); }
                    }
                }
            });
        }

        log.info("Super Admin {} ({}) cancelled transaction {} [{}/{}] of {} — {}", user.getUserId(), empCode,
                latestId, action, status, callNo, summary);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("callNo", callNo);
        out.put("deletedTransactionId", latestId);
        out.put("action", action);
        out.put("status", status);
        out.put("removed", summary);
        return out;
    }

    // ───────────────────────────── history ─────────────────────────────

    public Map<String, Object> history(String callNo, int page, int size) {
        int s = Math.min(Math.max(size, 1), 100);
        int pg = Math.max(page, 0);
        String where = " WHERE DELETE_SOURCE = ?" + (callNo != null && !callNo.isBlank() ? " AND UPPER(REQUEST_ID) LIKE ?" : "");
        List<Object> args = new ArrayList<>(List.of(SOURCE));
        if (callNo != null && !callNo.isBlank()) args.add("%" + callNo.trim().toUpperCase() + "%");

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM workflow_delete_history" + where, Long.class, args.toArray());
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(s);
        pageArgs.add(pg * s);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT ID AS id, REQUEST_ID AS callNo, REQUEST_TYPE AS product, WORKFLOW_TRANSITION_ID AS transactionId, "
                        + "ACTION AS action, STATUS AS status, CERTIFICATE_NO AS icNumber, DELETED_BY AS deletedBy, "
                        + "DELETED_BY_EMP_CODE AS empCode, DELETED_BY_NAME AS deletedByName, DELETE_REMARKS AS remarks, "
                        + "DELETED_ON AS deletedOn, CLEANUP_SUMMARY AS cleanupSummary "
                        + "FROM workflow_delete_history" + where + " ORDER BY ID DESC LIMIT ? OFFSET ?",
                pageArgs.toArray());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", rows);
        out.put("page", pg);
        out.put("size", s);
        out.put("totalElements", total == null ? 0 : total);
        out.put("totalPages", total == null ? 0 : (int) Math.ceil(total / (double) s));
        return out;
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private String findIcNo(Product p, String callNo) {
        List<String> l = jdbc.queryForList("SELECT CERTIFICATE_NO FROM " + completeDetailsTable(p)
                + " WHERE UPPER(CALL_NO) = ? ORDER BY ID DESC LIMIT 1", String.class, callNo);
        return l.isEmpty() || l.get(0) == null || l.get(0).isBlank() ? null : l.get(0).trim();
    }

    private static List<String> icKeys(String icNo, String callNo) {
        LinkedHashSet<String> k = new LinkedHashSet<>();
        if (icNo != null) k.add(icNo);
        k.add(callNo);
        return new ArrayList<>(k);
    }

    private static String placeholders(int n) { return "(" + String.join(",", Collections.nCopies(n, "?")) + ")"; }

    private int deleteIn(String table, String col, List<String> keys) {
        return jdbc.update("DELETE FROM " + table + " WHERE " + col + " IN " + placeholders(keys.size()), keys.toArray());
    }

    private int countIn(String table, String col, List<String> keys) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + col + " IN " + placeholders(keys.size()),
                Integer.class, keys.toArray());
        return n == null ? 0 : n;
    }

    private List<String> selectIn(String sqlPrefix, List<String> keys) {
        List<String> l = jdbc.queryForList(sqlPrefix + placeholders(keys.size()), String.class, keys.toArray());
        List<String> out = new ArrayList<>();
        for (String f : l) if (f != null && !f.isBlank()) out.add(f);
        return out;
    }

    private int countActiveCaseLetters(String callNo) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM case_letter_document WHERE UPPER(CALL_NO) = ? AND STATUS = 'ACTIVE'",
                Integer.class, callNo);
        return n == null ? 0 : n;
    }

    private static String normCall(String c) {
        if (c == null || c.trim().isEmpty()) throw new IllegalArgumentException("Call number is required");
        return c.trim().toUpperCase();
    }

    /** Case-insensitive column lookup in a queryForList row. */
    private static Object ci(Map<String, Object> row, String col) {
        if (row.containsKey(col)) return row.get(col);
        for (Map.Entry<String, Object> e : row.entrySet()) if (e.getKey().equalsIgnoreCase(col)) return e.getValue();
        return null;
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }

    private static Integer intOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.valueOf(String.valueOf(o).trim()); } catch (Exception e) { return null; }
    }

    private static String toJson(Map<String, Object> m) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else if (v instanceof Collection<?> c) {
                sb.append('[');
                boolean f2 = true;
                for (Object x : c) { if (!f2) sb.append(','); f2 = false; sb.append('"').append(esc(String.valueOf(x))).append('"'); }
                sb.append(']');
            } else sb.append('"').append(esc(String.valueOf(v))).append('"');
        }
        return sb.append('}').toString();
    }

    private static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
}
