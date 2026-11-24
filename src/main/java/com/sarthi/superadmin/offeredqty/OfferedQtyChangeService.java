package com.sarthi.superadmin.offeredqty;

import com.sarthi.entity.UserMaster;
import com.sarthi.service.InventoryEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Super Admin – change the offered quantity of an ERC call (ER per heat in MT, EP / EF per lot in Nos).
 *
 * Allowed only before inspection is complete. Every table that holds the offered qty is updated in one
 * transaction, and every changed heat / lot is written to erc_offered_qty_change_history and to
 * inspection_modification_history (the call's existing history).
 *
 *   ER : rm_heat_quantities → rm_inspection_details totals → inventory_entries (by difference)
 *        → rm_heat_final_result / rm_inspection_summary (if present) → inspection_initiation
 *   EP : process_inspection_details → process_line_final_result → process_ie_qty → inspection_initiation
 *   EF : final_inspection_lot_details (+ no_of_bags) → final_inspection_details total → final_process_ic_mapping
 *        → final_cumulative_results (this call) + cumm_qty_offered_previously of LATER calls of the same PO/serial
 *        → final test tables (hardness, toe load, weight, defects, microstructure, decarb) → inspection_initiation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferedQtyChangeService {

    private final JdbcTemplate jdbc;
    private final InventoryEntryService inventoryEntryService;

    public enum CallType { ER, EP, EF }

    /** Latest workflow statuses / actions after which the offered qty is frozen. */
    private static final List<String> BLOCKING = List.of(
            "FINISH", "INSPECTION_COMPLETE_CONFIRM", "INSPECTION_COMPLETE", "GENERATE_IC", "IC_GENERATION",
            "DSC_SIGN_IC", "ISSUE_IC", "IC_ISSUED", "SEND_CALL_TO_IBS", "SENT_TO_IBS",
            "WITHDRAW", "CANCEL", "WITHHELD");

    static final String BLOCK_MSG = "Inspection for this call is already complete — offered quantity can no longer be changed.";

    // ───────────────────────────── call list (dropdown) ─────────────────────────────

    public Map<String, Object> listCalls(String typeRaw, String q, int page, int size) {
        CallType type = parseType(typeRaw);
        int s = Math.min(Math.max(size, 5), 50);
        int pg = Math.max(page, 0);
        List<Object> args = new ArrayList<>();
        args.add(type.name() + "%");
        String where = " WHERE ic.ic_number LIKE ?";
        if (q != null && !q.isBlank()) {
            where += " AND (ic.ic_number LIKE ? OR ic.po_no LIKE ? OR ic.company_name LIKE ?)";
            String like = "%" + q.trim() + "%";
            args.add(like); args.add(like); args.add(like);
        }
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(s + 1);
        pageArgs.add(pg * s);

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT ic.id, ic.ic_number AS callNo, ic.po_no AS poNo, ic.po_serial_no AS poSerialNo, "
                        + "ic.company_name AS vendor, ic.unit_name AS unit, ic.created_at AS createdAt, "
                        + "(SELECT CONCAT(COALESCE(w.STATUS,''),'|',COALESCE(w.ACTION,'')) FROM workflow_transition w "
                        + "  WHERE w.REQUESTID = ic.ic_number ORDER BY w.WORKFLOWTRANSITIONID DESC LIMIT 1) AS wf "
                        + "FROM inspection_calls ic" + where + " ORDER BY ic.created_at DESC, ic.id DESC LIMIT ? OFFSET ?",
                pageArgs.toArray());

        boolean hasMore = rows.size() > s;
        if (hasMore) rows = rows.subList(0, s);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            String[] wf = splitWf((String) r.remove("wf"));
            Map<String, Object> m = new LinkedHashMap<>(r);
            m.put("status", displayStatus(wf));
            m.put("editable", isEditable(wf));
            out.add(m);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("content", out);
        res.put("page", pg);
        res.put("hasMore", hasMore);
        return res;
    }

    // ───────────────────────────── call details ─────────────────────────────

    public Map<String, Object> getCall(String callNoRaw) {
        String callNo = norm(callNoRaw);
        CallType type = typeOf(callNo);
        Map<String, Object> call = loadCall(callNo);
        String[] wf = latestWf(callNo);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("callNo", callNo);
        out.put("type", type.name());
        out.put("unit", type == CallType.ER ? "MT" : "Nos");
        out.put("poNo", call.get("po_no"));
        out.put("poSerialNo", call.get("po_serial_no"));
        out.put("vendor", call.get("company_name"));
        out.put("plant", call.get("unit_name"));
        out.put("status", displayStatus(wf));
        boolean editable = isEditable(wf);
        out.put("editable", editable);
        out.put("blockedReason", editable ? null : BLOCK_MSG);
        out.put("rows", rows(type, callNo, ((Number) call.get("id")).longValue()));
        return out;
    }

    private List<Map<String, Object>> rows(CallType type, String callNo, long icId) {
        switch (type) {
            case ER: {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT h.id AS rowId, h.heat_number AS heatNo, h.tc_number AS tcNo, h.offered_qty AS offeredQty, "
                                + "d.sub_po_number AS subPoNo, "
                                + "(SELECT COALESCE(SUM(e.qty_left_for_inspection),0) FROM inventory_entries e "
                                + "   WHERE e.heat_number = h.heat_number AND e.tc_number = h.tc_number) AS inventoryLeft "
                                + "FROM rm_inspection_details d JOIN rm_heat_quantities h ON h.rm_detail_id = d.id "
                                + "WHERE d.ic_id = ? ORDER BY h.id", icId);
                for (Map<String, Object> r : rows) {
                    BigDecimal offered = bd(r.get("offeredQty"));
                    BigDecimal left = bd(r.get("inventoryLeft"));
                    r.put("label", "Heat " + r.get("heatNo") + " / TC " + r.get("tcNo"));
                    r.put("minQty", BigDecimal.ZERO);
                    r.put("maxQty", offered.add(left));
                    r.put("alreadyUsed", null);
                    r.put("hint", "Inventory left: " + left.setScale(3, RoundingMode.HALF_UP) + " MT");
                }
                return rows;
            }
            case EP: {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT p.id AS rowId, p.lot_number AS lotNo, p.heat_number AS heatNo, p.offered_qty AS offeredQty, "
                                + "p.rm_ic_number AS rmIcNumber, "
                                + "(SELECT COALESCE(SUM(l.total_accepted),0) FROM process_line_final_result l "
                                + "   WHERE l.inspection_call_no = ? AND l.lot_number = p.lot_number) AS alreadyUsed "
                                + "FROM process_inspection_details p WHERE p.ic_id = ? ORDER BY p.id", callNo, icId);
                for (Map<String, Object> r : rows) {
                    r.put("label", "Lot " + r.get("lotNo") + " / Heat " + r.get("heatNo"));
                    r.put("minQty", Math.max(1, toLong(r.get("alreadyUsed"))));
                    Long avail = epHeatAvailable(str(r.get("rmIcNumber")), str(r.get("heatNo")), callNo);
                    r.put("maxQty", avail == null ? null : toLong(r.get("offeredQty")) + avail);
                    r.put("hint", "Accepted so far: " + toLong(r.get("alreadyUsed"))
                            + (avail == null ? "" : " · Heat balance left: " + avail));
                }
                return rows;
            }
            default: {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT l.id AS rowId, l.lot_number AS lotNo, l.heat_number AS heatNo, l.offered_qty AS offeredQty, "
                                + "(COALESCE(l.qty_accepted,0) + COALESCE(l.qty_rejected,0)) AS alreadyUsed "
                                + "FROM final_inspection_details d JOIN final_inspection_lot_details l ON l.final_detail_id = d.id "
                                + "WHERE d.ic_id = ? ORDER BY l.id", icId);
                for (Map<String, Object> r : rows) {
                    r.put("label", "Lot " + r.get("lotNo") + " / Heat " + r.get("heatNo"));
                    r.put("minQty", Math.max(1, toLong(r.get("alreadyUsed"))));
                    Long avail = efLotAvailable(icId, str(r.get("lotNo")));
                    r.put("maxQty", avail == null ? null : toLong(r.get("offeredQty")) + avail);
                    r.put("hint", "Accepted + rejected so far: " + toLong(r.get("alreadyUsed"))
                            + (avail == null ? "" : " · Process IC balance left: " + avail));
                }
                return rows;
            }
        }
    }

    // ───────────────────────────── update ─────────────────────────────

    @Transactional
    public Map<String, Object> update(String callNoRaw, List<Map<String, Object>> items, String remarks, UserMaster user) {
        String callNo = norm(callNoRaw);
        CallType type = typeOf(callNo);
        if (remarks == null || remarks.trim().length() < 5) throw new IllegalArgumentException("Remarks are required (at least 5 characters).");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Nothing to change.");

        Map<String, Object> call = loadCall(callNo);
        long icId = ((Number) call.get("id")).longValue();

        // lock the call's workflow rows, then re-check the status
        jdbc.queryForList("SELECT WORKFLOWTRANSITIONID FROM workflow_transition WHERE REQUESTID = ? FOR UPDATE", callNo);
        String[] wf = latestWf(callNo);
        if (!isEditable(wf)) throw new IllegalStateException(BLOCK_MSG);
        String statusAtChange = displayStatus(wf);

        // current rows (fresh, for validation)
        Map<Long, Map<String, Object>> current = new LinkedHashMap<>();
        for (Map<String, Object> r : rows(type, callNo, icId)) current.put(toLong(r.get("rowId")), r);

        List<Map<String, Object>> changes = new ArrayList<>();
        for (Map<String, Object> it : items) {
            long rowId = toLong(it.get("rowId"));
            Map<String, Object> row = current.get(rowId);
            if (row == null) throw new IllegalArgumentException("Row " + rowId + " does not belong to call " + callNo + ".");
            BigDecimal oldQty = bd(row.get("offeredQty"));
            BigDecimal newQty = bd(it.get("newQty"));
            if (type != CallType.ER) newQty = newQty.setScale(0, RoundingMode.HALF_UP);
            else newQty = newQty.setScale(3, RoundingMode.HALF_UP);
            if (newQty.compareTo(oldQty) == 0) continue;
            String label = str(row.get("label"));
            if (newQty.signum() <= 0) throw new IllegalArgumentException(label + ": quantity must be more than 0.");
            BigDecimal min = row.get("minQty") == null ? BigDecimal.ZERO : bd(row.get("minQty"));
            if (type != CallType.ER && newQty.compareTo(min) < 0) {
                throw new IllegalArgumentException(label + ": cannot be less than " + min.toPlainString()
                        + " (already accepted" + (type == CallType.EF ? " + rejected" : "") + " by the IE).");
            }
            if (row.get("maxQty") != null && newQty.compareTo(bd(row.get("maxQty"))) > 0) {
                throw new IllegalArgumentException(label + ": cannot be more than " + bd(row.get("maxQty")).stripTrailingZeros().toPlainString()
                        + (type == CallType.ER ? " MT (inventory left for this heat/TC)." : " (available balance)."));
            }
            Map<String, Object> c = new LinkedHashMap<>(row);
            c.put("oldQty", oldQty);
            c.put("newQty", newQty);
            c.put("delta", newQty.subtract(oldQty));
            changes.add(c);
        }
        if (changes.isEmpty()) throw new IllegalArgumentException("No quantity was changed.");

        Map<String, Object> summary = switch (type) {
            case ER -> applyEr(callNo, icId, changes);
            case EP -> applyEp(callNo, icId, changes);
            case EF -> applyEf(callNo, icId, call, changes);
        };

        // history
        String name = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
        int version = Optional.ofNullable(jdbc.queryForObject(
                "SELECT COALESCE(MAX(modification_version),0) FROM inspection_modification_history WHERE ic_number = ?",
                Integer.class, callNo)).orElse(0) + 1;
        String tableName = switch (type) { case ER -> "rm_heat_quantities"; case EP -> "process_inspection_details"; case EF -> "final_inspection_lot_details"; };
        String unit = type == CallType.ER ? "MT" : "Nos";
        for (Map<String, Object> c : changes) {
            jdbc.update("INSERT INTO erc_offered_qty_change_history (call_no, call_type, row_ref, heat_no, lot_no, old_qty, new_qty, unit, "
                            + "status_at_change, remarks, changed_by, changed_by_emp_code, changed_by_name, changed_at, tables_updated) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),?)",
                    callNo, type.name(), str(c.get("label")), str(c.get("heatNo")), str(c.get("lotNo")),
                    c.get("oldQty"), c.get("newQty"), unit, statusAtChange, remarks.trim(),
                    user.getUserId(), user.getEmployeeCode(), name, toJson(summary));
            jdbc.update("INSERT INTO inspection_modification_history (inspection_call_id, ic_number, modification_version, table_name, "
                            + "field_name, old_value, new_value, change_type, modified_by, modified_at) VALUES (?,?,?,?,?,?,?,?,?,NOW())",
                    // field_name is VARCHAR(100), modified_by VARCHAR(100) — remarks live in erc_offered_qty_change_history
                    icId, callNo, version, tableName, truncate("offeredQty (" + c.get("label") + ")", 100),
                    plain(c.get("oldQty")), plain(c.get("newQty")), "SUPER_ADMIN_OFFERED_QTY",
                    truncate(name + " (" + user.getEmployeeCode() + ")", 100));
        }

        log.info("Super Admin {} changed offered qty of {} — {}", user.getUserId(), callNo, summary);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("callNo", callNo);
        out.put("changedRows", changes.size());
        out.put("tablesUpdated", summary);
        return out;
    }

    // ── ER ──
    private Map<String, Object> applyEr(String callNo, long icId, List<Map<String, Object>> changes) {
        Map<String, Object> s = new LinkedHashMap<>();
        int heatRows = 0, finalRows = 0;
        for (Map<String, Object> c : changes) {
            long rowId = toLong(c.get("rowId"));
            BigDecimal delta = bd(c.get("delta"));
            heatRows += jdbc.update("UPDATE rm_heat_quantities SET offered_qty = ?, updated_at = NOW() WHERE id = ?", c.get("newQty"), rowId);
            // inventory by the difference (same service the vendor "modify call" uses)
            inventoryEntryService.updateOfferedQuantity(str(c.get("heatNo")), str(c.get("tcNo")), str(c.get("subPoNo")), delta);
            finalRows += jdbc.update("UPDATE rm_heat_final_result SET weight_offered_mt = ?, updated_at = NOW() "
                    + "WHERE inspection_call_no = ? AND heat_no = ?", c.get("newQty"), callNo, c.get("heatNo"));
        }
        s.put("rm_heat_quantities", heatRows);
        s.put("inventory_entries", changes.size());
        s.put("rm_heat_final_result.weight_offered_mt", finalRows);

        BigDecimal total = Optional.ofNullable(jdbc.queryForObject(
                "SELECT COALESCE(SUM(h.offered_qty),0) FROM rm_inspection_details d JOIN rm_heat_quantities h ON h.rm_detail_id = d.id WHERE d.ic_id = ?",
                BigDecimal.class, icId)).orElse(BigDecimal.ZERO);
        // keep the existing MT -> ERC ratio of the call
        List<Map<String, Object>> det = jdbc.queryForList("SELECT id, total_offered_qty_mt, offered_qty_erc FROM rm_inspection_details WHERE ic_id = ?", icId);
        for (Map<String, Object> d : det) {
            BigDecimal oldMt = bd(d.get("total_offered_qty_mt"));
            Integer oldErc = d.get("offered_qty_erc") == null ? null : ((Number) d.get("offered_qty_erc")).intValue();
            Integer newErc = (oldErc != null && oldMt.signum() > 0)
                    ? BigDecimal.valueOf(oldErc).multiply(total).divide(oldMt, 0, RoundingMode.HALF_UP).intValue() : oldErc;
            jdbc.update("UPDATE rm_inspection_details SET total_offered_qty_mt = ?, offered_qty_erc = ?, updated_at = NOW() WHERE id = ?",
                    total, newErc, d.get("id"));
            s.put("rm_inspection_details.offered_qty_erc", oldErc + " → " + newErc);
        }
        s.put("rm_inspection_details.total_offered_qty_mt", total);
        s.put("rm_heat_final_result.total_qty_offered_mt", jdbc.update(
                "UPDATE rm_heat_final_result SET total_qty_offered_mt = ? WHERE inspection_call_no = ?", total, callNo));
        s.put("rm_inspection_summary", jdbc.update(
                "UPDATE rm_inspection_summary SET total_qty_offered_mt = ?, updated_at = NOW() WHERE inspection_call_no = ?", total, callNo));
        s.put("inspection_initiation", jdbc.update("UPDATE inspection_initiation SET offered_qty = ? WHERE call_no = ?", total, callNo));
        return s;
    }

    // ── EP ──
    private Map<String, Object> applyEp(String callNo, long icId, List<Map<String, Object>> changes) {
        Map<String, Object> s = new LinkedHashMap<>();
        int a = 0, b = 0, c2 = 0;
        for (Map<String, Object> c : changes) {
            int q = bd(c.get("newQty")).intValue();
            a += jdbc.update("UPDATE process_inspection_details SET offered_qty = ?, updated_at = NOW() WHERE id = ?", q, toLong(c.get("rowId")));
            b += jdbc.update("UPDATE process_line_final_result SET offered_qty = ?, updated_at = NOW() WHERE inspection_call_no = ? AND lot_number = ?",
                    q, callNo, c.get("lotNo"));
            c2 += jdbc.update("UPDATE process_ie_qty SET offered_qty = ? WHERE REQUEST_ID = ? AND lot_number = ?", q, callNo, c.get("lotNo"));
        }
        s.put("process_inspection_details", a);
        s.put("process_line_final_result", b);
        s.put("process_ie_qty", c2);
        Long total = jdbc.queryForObject("SELECT COALESCE(SUM(offered_qty),0) FROM process_inspection_details WHERE ic_id = ?", Long.class, icId);
        s.put("inspection_initiation", jdbc.update("UPDATE inspection_initiation SET offered_qty = ? WHERE call_no = ?", total, callNo));
        return s;
    }

    // ── EF ──
    private Map<String, Object> applyEf(String callNo, long icId, Map<String, Object> call, List<Map<String, Object>> changes) {
        Map<String, Object> s = new LinkedHashMap<>();
        int lots = 0, maps = 0, tests = 0;
        long totalDelta = 0;
        String[] testTables = {"final_hardness_test:qty_no", "final_toe_load_test:qty_no", "final_weight_test:qty_no",
                "final_freedom_from_defects_test:qty", "final_microstructure_test:qty", "final_depth_of_decarburization:qty"};
        for (Map<String, Object> c : changes) {
            int oldQ = bd(c.get("oldQty")).intValue();
            int q = bd(c.get("newQty")).intValue();
            totalDelta += (q - oldQ);
            // no_of_bags follows the qty only when it was the auto value ceil(qty/50)
            lots += jdbc.update("UPDATE final_inspection_lot_details SET offered_qty = ?, "
                            + "no_of_bags = CASE WHEN no_of_bags IS NULL OR no_of_bags = CEIL(? / 50) THEN CEIL(? / 50) ELSE no_of_bags END, "
                            + "updated_at = NOW() WHERE id = ?", q, oldQ, q, toLong(c.get("rowId")));
            maps += jdbc.update("UPDATE final_process_ic_mapping SET process_qty_accepted = ?, updated_at = NOW() WHERE final_ic_id = ? AND lot_number = ?",
                    q, icId, c.get("lotNo"));
            for (String t : testTables) {
                String[] tc = t.split(":");
                tests += jdbc.update("UPDATE " + tc[0] + " SET " + tc[1] + " = ? WHERE inspection_call_no = ? AND lot_no = ? AND (heat_no = ? OR ? IS NULL)",
                        q, callNo, c.get("lotNo"), c.get("heatNo"), c.get("heatNo"));
            }
        }
        s.put("final_inspection_lot_details", lots);
        s.put("final_process_ic_mapping", maps);
        s.put("final_test_tables", tests);

        Long total = jdbc.queryForObject("SELECT COALESCE(SUM(l.offered_qty),0) FROM final_inspection_details d "
                + "JOIN final_inspection_lot_details l ON l.final_detail_id = d.id WHERE d.ic_id = ?", Long.class, icId);
        Long bags = jdbc.queryForObject("SELECT COALESCE(SUM(l.no_of_bags),0) FROM final_inspection_details d "
                + "JOIN final_inspection_lot_details l ON l.final_detail_id = d.id WHERE d.ic_id = ?", Long.class, icId);
        s.put("final_inspection_details", jdbc.update("UPDATE final_inspection_details SET total_offered_qty = ?, updated_at = NOW() WHERE ic_id = ?", total, icId));
        s.put("final_cumulative_results", jdbc.update("UPDATE final_cumulative_results SET qty_now_offered = ?, bags_offered = ?, updated_at = NOW() "
                + "WHERE inspection_call_no = ?", total, bags, callNo));
        // later calls of the same PO + serial carry this call in their "cumm qty offered previously"
        if (totalDelta != 0) {
            s.put("final_cumulative_results.later_calls", jdbc.update(
                    "UPDATE final_cumulative_results f JOIN inspection_calls ic ON ic.ic_number = f.inspection_call_no "
                            + "SET f.cumm_qty_offered_previously = COALESCE(f.cumm_qty_offered_previously,0) + ?, f.updated_at = NOW() "
                            + "WHERE ic.ic_number LIKE 'EF%' AND ic.po_no = ? AND ic.po_serial_no <=> ? AND ic.ic_number <> ? "
                            + "AND (ic.created_at > ? OR (ic.created_at = ? AND ic.id > ?))",
                    totalDelta, call.get("po_no"), call.get("po_serial_no"), callNo, call.get("created_at"), call.get("created_at"), icId));
        }
        s.put("inspection_initiation", jdbc.update("UPDATE inspection_initiation SET offered_qty = ? WHERE call_no = ?", total, callNo));
        return s;
    }

    // ───────────────────────────── balances ─────────────────────────────

    /** EP: Nos still available on the RM heat for other process calls (null = could not be determined). */
    private Long epHeatAvailable(String rmIcNumber, String heatNo, String excludeCall) {
        if (rmIcNumber == null || rmIcNumber.isBlank() || heatNo == null) return null;
        List<String> keys = new ArrayList<>();
        for (String k : rmIcNumber.split(",")) if (!k.isBlank()) keys.add(k.trim());
        if (keys.isEmpty()) return null;
        String in = "(" + String.join(",", Collections.nCopies(keys.size(), "?")) + ")";
        // RM certificate no(s) → RM call no(s)
        List<Object> a = new ArrayList<>(keys);
        a.addAll(keys);
        List<String> rmCalls = jdbc.queryForList("SELECT CALL_NO FROM inspection_complete_details WHERE CERTIFICATE_NO IN " + in
                + " UNION SELECT ic_number FROM inspection_calls WHERE ic_number IN " + in, String.class, a.toArray());
        if (rmCalls.isEmpty()) return null;
        String in2 = "(" + String.join(",", Collections.nCopies(rmCalls.size(), "?")) + ")";
        List<Object> a2 = new ArrayList<>(rmCalls);
        a2.add(heatNo);
        List<Map<String, Object>> res = jdbc.queryForList("SELECT accepted_qty_mt, weight_accepted_mt FROM rm_heat_final_result "
                + "WHERE inspection_call_no IN " + in2 + " AND heat_no = ? LIMIT 1", a2.toArray());
        if (res.isEmpty()) return null;
        BigDecimal accNos = bd(res.get(0).get("accepted_qty_mt"));
        if (accNos.signum() == 0) {
            accNos = bd(res.get(0).get("weight_accepted_mt")).multiply(new BigDecimal("1000")).divide(new BigDecimal("1.14"), 0, RoundingMode.HALF_UP);
        }
        List<Object> a3 = new ArrayList<>(keys);
        a3.add(heatNo);
        a3.add(excludeCall);
        StringBuilder like = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) like.append(i == 0 ? "" : " OR ").append("FIND_IN_SET(?, REPLACE(p.rm_ic_number,' ','')) > 0");
        Long used = jdbc.queryForObject("SELECT COALESCE(SUM(p.offered_qty),0) FROM process_inspection_details p "
                + "JOIN inspection_calls ic ON ic.id = p.ic_id WHERE (" + like + ") AND p.heat_number = ? AND ic.ic_number <> ?",
                Long.class, a3.toArray());
        return Math.max(0, accNos.longValue() - (used == null ? 0 : used));
    }

    /** EF: Nos still available on the Process IC lot (null = could not be determined). */
    private Long efLotAvailable(long finalIcId, String lotNo) {
        List<Map<String, Object>> m = jdbc.queryForList(
                "SELECT process_ic_number FROM final_process_ic_mapping WHERE final_ic_id = ? AND lot_number = ? LIMIT 1", finalIcId, lotNo);
        if (m.isEmpty() || m.get(0).get("process_ic_number") == null) return null;
        String pic = str(m.get(0).get("process_ic_number")).trim();
        List<String> pCalls = jdbc.queryForList("SELECT CALL_NO FROM inspection_complete_details WHERE CERTIFICATE_NO = ? "
                + "UNION SELECT ic_number FROM inspection_calls WHERE ic_number = ?", String.class, pic, pic);
        if (pCalls.isEmpty()) return null;
        String in = "(" + String.join(",", Collections.nCopies(pCalls.size(), "?")) + ")";
        List<Object> a = new ArrayList<>(pCalls);
        a.add(lotNo);
        Long accepted = jdbc.queryForObject("SELECT COALESCE(SUM(total_accepted),0) FROM process_line_final_result "
                + "WHERE inspection_call_no IN " + in + " AND lot_number = ?", Long.class, a.toArray());
        if (accepted == null || accepted == 0) return null;
        Long used = jdbc.queryForObject("SELECT COALESCE(SUM(process_qty_accepted),0) FROM final_process_ic_mapping "
                + "WHERE process_ic_number = ? AND lot_number = ? AND final_ic_id <> ?", Long.class, pic, lotNo, finalIcId);
        return Math.max(0, accepted - (used == null ? 0 : used));
    }

    // ───────────────────────────── history ─────────────────────────────

    public Map<String, Object> history(String callNo, int page, int size) {
        int s = Math.min(Math.max(size, 1), 100);
        int pg = Math.max(page, 0);
        String where = (callNo != null && !callNo.isBlank()) ? " WHERE call_no LIKE ?" : "";
        List<Object> args = new ArrayList<>();
        if (!where.isEmpty()) args.add("%" + callNo.trim().toUpperCase() + "%");
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM erc_offered_qty_change_history" + where, Long.class, args.toArray());
        List<Object> pa = new ArrayList<>(args);
        pa.add(s);
        pa.add(pg * s);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, call_no AS callNo, call_type AS type, row_ref AS item, old_qty AS oldQty, new_qty AS newQty, unit, "
                        + "status_at_change AS status, remarks, changed_by_name AS changedBy, changed_by_emp_code AS empCode, changed_at AS changedAt "
                        + "FROM erc_offered_qty_change_history" + where + " ORDER BY id DESC LIMIT ? OFFSET ?", pa.toArray());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", rows);
        out.put("page", pg);
        out.put("totalElements", total == null ? 0 : total);
        out.put("totalPages", total == null ? 0 : (int) Math.ceil(total / (double) s));
        return out;
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private Map<String, Object> loadCall(String callNo) {
        List<Map<String, Object>> l = jdbc.queryForList(
                "SELECT id, ic_number, po_no, po_serial_no, company_name, unit_name, created_at FROM inspection_calls WHERE ic_number = ?", callNo);
        if (l.isEmpty()) throw new NoSuchElementException("Call " + callNo + " not found.");
        return l.get(0);
    }

    private String[] latestWf(String callNo) {
        List<Map<String, Object>> l = jdbc.queryForList("SELECT STATUS, ACTION FROM workflow_transition WHERE REQUESTID = ? "
                + "ORDER BY WORKFLOWTRANSITIONID DESC LIMIT 1", callNo);
        if (l.isEmpty()) return new String[]{"", ""};
        return new String[]{str(l.get(0).get("STATUS")), str(l.get(0).get("ACTION"))};
    }

    private static String[] splitWf(String wf) {
        if (wf == null) return new String[]{"", ""};
        String[] p = wf.split("\\|", -1);
        return new String[]{p.length > 0 ? p[0] : "", p.length > 1 ? p[1] : ""};
    }

    static boolean isEditable(String[] wf) {
        String both = ((wf[0] == null ? "" : wf[0]) + "|" + (wf[1] == null ? "" : wf[1])).toUpperCase();
        for (String b : BLOCKING) if (both.contains(b)) return false;
        return true;
    }

    private static String displayStatus(String[] wf) {
        String s = wf[0] == null || wf[0].isBlank() ? wf[1] : wf[0];
        return s == null || s.isBlank() ? "Pending" : s;
    }

    static CallType typeOf(String callNo) {
        String c = callNo.toUpperCase();
        if (c.startsWith("ER")) return CallType.ER;
        if (c.startsWith("EP")) return CallType.EP;
        if (c.startsWith("EF")) return CallType.EF;
        throw new IllegalArgumentException("Only ERC calls (ER / EP / EF) are supported.");
    }

    private static CallType parseType(String t) {
        try { return CallType.valueOf(t == null ? "" : t.trim().toUpperCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Type must be ER, EP or EF."); }
    }

    private static String norm(String c) {
        if (c == null || c.isBlank()) throw new IllegalArgumentException("Call number is required.");
        return c.trim().toUpperCase();
    }

    private static BigDecimal bd(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        try { return new BigDecimal(String.valueOf(o).trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static long toLong(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.longValue();
        try { return new BigDecimal(String.valueOf(o).trim()).longValue(); } catch (Exception e) { return 0; }
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }

    private static String plain(Object o) { return o == null ? null : bd(o).stripTrailingZeros().toPlainString(); }

    private static String truncate(String s, int n) { return s.length() <= n ? s : s.substring(0, n - 1) + "…"; }

    private static String toJson(Map<String, Object> m) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey()).append("\":\"").append(String.valueOf(e.getValue()).replace("\"", "'")).append('"');
        }
        return sb.append('}').toString();
    }
}
