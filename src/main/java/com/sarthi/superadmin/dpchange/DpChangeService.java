package com.sarthi.superadmin.dpchange;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.entity.UserMaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Super Admin – temporary DP change of a PO serial.
 *
 * Sets po_item.delivery_date (and every amendment_po_item copy of the same PO + Sr No.) to a new date for
 * {@link #REVERT_HOURS} hours, then {@link DpChangeRevertJob} restores the previous values.
 * Every change is kept in super_admin_dp_change (who, when, old / new date, remark, how it was reverted).
 *
 * The revert only restores a row that still holds the date Super Admin set: if a PO / MA sync changed it in
 * the meantime, that newer date is kept and the change is marked SKIPPED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DpChangeService {

    static final int REVERT_HOURS = 2;
    /** Vendor screens allow raising a call up to (effective DP - 4 days). */
    private static final int CUTOFF_DAYS = 4;
    private static final String DP_COL = "delivery_date";
    private static final String EXT_DP_COL = "extended_delivery_date";

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ObjectMapper mapper = new ObjectMapper();

    // ───────────────────────────── PO dropdown ─────────────────────────────

    public Map<String, Object> listPos(String q, int page, int size) {
        int s = Math.min(Math.max(size, 10), 100);
        int pg = Math.max(page, 0);
        List<Object> args = new ArrayList<>();
        String where = "";
        if (q != null && !q.isBlank()) {
            where = " WHERE (ph.po_no LIKE ? OR ph.vendor_code LIKE ? OR ph.firm_details LIKE ? OR ph.vendor_details LIKE ?)";
            String like = "%" + q.trim() + "%";
            args.addAll(List.of(like, like, like, like));
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM po_header ph" + where, Long.class, args.toArray());
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(s);
        pageArgs.add(pg * s);
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT ph.po_no, DATE_FORMAT(ph.po_date, '%Y-%m-%d') po_date, ph.vendor_code, ph.firm_details, ph.vendor_details, ph.item_cat_descr, ph.rly_short_name "
                        + "FROM po_header ph" + where + " ORDER BY ph.po_date DESC, ph.po_no LIMIT ? OFFSET ?",
                (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("poNo", rs.getString("po_no"));
                    m.put("poDate", rs.getString("po_date"));
                    m.put("vendorCode", cleanCode(rs.getString("vendor_code")));
                    m.put("vendorName", vendorName(rs.getString("firm_details"), rs.getString("vendor_details")));
                    m.put("product", product(rs.getString("item_cat_descr")));
                    m.put("railway", rs.getString("rly_short_name"));
                    return m;
                }, pageArgs.toArray());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", rows);
        out.put("total", total == null ? 0 : total);
        out.put("page", pg);
        out.put("hasMore", (long) (pg + 1) * s < (total == null ? 0 : total));
        return out;
    }

    // ───────────────────────────── PO details + serials ─────────────────────────────

    public Map<String, Object> getPo(String poNo) {
        List<Map<String, Object>> head = jdbc.query(
                "SELECT id, po_no, DATE_FORMAT(po_date, '%Y-%m-%d') po_date, vendor_code, firm_details, vendor_details, item_cat_descr, rly_short_name, "
                        + "COALESCE(is_amended, 0) is_amended FROM po_header WHERE po_no = ? LIMIT 1",
                (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getLong("id"));
                    m.put("poNo", rs.getString("po_no"));
                    m.put("poDate", rs.getString("po_date"));
                    m.put("vendorCode", cleanCode(rs.getString("vendor_code")));
                    m.put("vendorName", vendorName(rs.getString("firm_details"), rs.getString("vendor_details")));
                    m.put("product", product(rs.getString("item_cat_descr")));
                    m.put("itemCategory", rs.getString("item_cat_descr"));
                    m.put("railway", rs.getString("rly_short_name"));
                    m.put("amended", rs.getInt("is_amended") == 1);
                    return m;
                }, poNo == null ? "" : poNo.trim());
        if (head.isEmpty()) throw new NoSuchElementException("PO " + poNo + " not found.");
        Map<String, Object> po = head.get(0);

        Map<String, Map<String, Object>> active = new HashMap<>();
        for (Map<String, Object> a : activeChanges(po.get("poNo").toString())) active.put(a.get("srNo").toString(), a);

        List<Map<String, Object>> items = jdbc.query(
                "SELECT id, item_sr_no, item_desc, qty, uom, DATE_FORMAT(delivery_date, '%Y-%m-%d') delivery_date, "
                        + "DATE_FORMAT(extended_delivery_date, '%Y-%m-%d') extended_delivery_date "
                        + "FROM po_item WHERE po_header_id = ? ORDER BY item_sr_no",
                (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    String sr = rs.getString("item_sr_no");
                    m.put("srNo", sr);
                    m.put("itemDesc", rs.getString("item_desc"));
                    m.put("qty", rs.getBigDecimal("qty"));
                    m.put("uom", rs.getString("uom"));
                    m.put("dp", rs.getString("delivery_date"));
                    m.put("extDp", rs.getString("extended_delivery_date"));
                    m.put("activeChange", active.get(sr));
                    return m;
                }, po.get("id"));
        po.put("items", items);
        po.remove("id");
        return po;
    }

    // ───────────────────────────── apply ─────────────────────────────

    @Transactional
    public Map<String, Object> apply(String poNo, String srNo, String newDateRaw, String remarks, UserMaster user) {
        String po = req(poNo, "PO number");
        String sr = req(srNo, "Sr No.");
        String note = req(remarks, "Remarks");
        if (note.length() > 500) throw new IllegalArgumentException("Remarks must be 500 characters or less.");
        LocalDate newDate;
        try {
            newDate = LocalDate.parse(req(newDateRaw, "New DP date"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("New DP date is not a valid date.");
        }
        // The vendor can raise a call up to (date - 4 days). A date whose cutoff is already past opens nothing,
        // and the vendor PO list replaces a past Ext. DP from the PO amendment — so refuse it.
        LocalDate earliest = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")).plusDays(CUTOFF_DAYS);
        if (newDate.isBefore(earliest)) {
            throw new IllegalArgumentException("New date must be on or after " + earliest.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    + " (calls can be raised up to " + CUTOFF_DAYS + " days before the DP).");
        }
        // dates are handled as 'yyyy-MM-dd HH:mm:ss' text: no JVM (IST) <-> DB (UTC) conversion
        String newDt = newDate + " 23:59:00";   // same time of day the PO sync stores

        Integer open = jdbc.queryForObject("SELECT COUNT(*) FROM super_admin_dp_change WHERE po_no = ? AND item_sr_no = ? AND status = 'ACTIVE'",
                Integer.class, po, sr);
        if (open != null && open > 0) {
            throw new IllegalStateException("Sr No. " + sr + " already has a temporary DP change. Revert it first.");
        }

        // The vendor screens (ERC / Sleeper / Rail Pad) take the call cutoff from the Ext. DP when the serial has one,
        // else from the DP — so change that same date, or the change has no effect.
        List<String> extDps = jdbc.queryForList(
                "SELECT DATE_FORMAT(i.extended_delivery_date, '%Y-%m-%d %H:%i:%s') FROM po_item i JOIN po_header h ON h.id = i.po_header_id "
                        + "WHERE h.po_no = ? AND i.item_sr_no = ?", String.class, po, sr);
        if (extDps.isEmpty()) throw new NoSuchElementException("Sr No. " + sr + " not found on PO " + po + ".");
        String col = extDps.get(0) != null ? EXT_DP_COL : DP_COL;

        // every row that carries this serial's date: po_item + amendment copies
        List<Map<String, Object>> affected = new ArrayList<>();
        affected.addAll(jdbc.query(
                "SELECT 'po_item' t, i.id, DATE_FORMAT(i." + col + ", '%Y-%m-%d %H:%i:%s') d FROM po_item i JOIN po_header h ON h.id = i.po_header_id "
                        + "WHERE h.po_no = ? AND i.item_sr_no = ?", (rs, i) -> row(rs.getString("t"), rs.getLong("id"), rs.getString("d"), col), po, sr));
        String oldDp = (String) affected.get(0).get("old");
        if (oldDp != null && oldDp.startsWith(newDate.toString())) {
            throw new IllegalArgumentException(EXT_DP_COL.equals(col) ? "New date is the same as the current Ext. DP." : "New DP is the same as the current DP.");
        }
        affected.addAll(jdbc.query(
                "SELECT 'amendment_po_item' t, ai.id, DATE_FORMAT(ai." + col + ", '%Y-%m-%d %H:%i:%s') d FROM amendment_po_item ai "
                        + "JOIN amendment_po_header ah ON ah.id = ai.amended_po_header_id WHERE ah.po_no = ? AND ai.item_sr_no = ?",
                (rs, i) -> row(rs.getString("t"), rs.getLong("id"), rs.getString("d"), col), po, sr));

        for (Map<String, Object> r : affected) {
            jdbc.update("UPDATE " + table(r) + " SET " + column(r) + " = CAST(? AS DATETIME) WHERE id = ?", newDt, r.get("id"));
        }

        String name = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
        jdbc.update("INSERT INTO super_admin_dp_change (po_no, item_sr_no, old_delivery_date, new_delivery_date, affected_rows, remarks, "
                        + "changed_by, changed_by_emp_code, changed_by_name, changed_at, revert_at, status) "
                        + "VALUES (?,?,CAST(? AS DATETIME),CAST(? AS DATETIME),?,?,?,?,?, NOW(), NOW() + INTERVAL " + REVERT_HOURS + " HOUR, 'ACTIVE')",
                po, sr, oldDp, newDt, toJson(affected), note, user.getUserId(), user.getEmployeeCode(), name);
        log.info("Super Admin {} set DP of PO {} Sr {} to {} for {} h ({} rows)", user.getUserId(), po, sr, newDate, REVERT_HOURS, affected.size());
        return getPo(po);
    }

    // ───────────────────────────── revert ─────────────────────────────

    /** Revert now (Super Admin). */
    public Map<String, Object> revertNow(long id, UserMaster user) {
        String name = user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername();
        String result = revert(id, "REVERTED_MANUAL", user.getUserId(), name);
        if (result == null) throw new IllegalStateException("This change is no longer active.");
        return Map.of("status", result);
    }

    /**
     * Restores every change whose 2 hours are over. Used by the job every minute, and before the screen reads
     * data, so a change never stays past its time even if the scheduler is busy.
     */
    public int revertOverdue() {
        int done = 0;
        for (Long id : dueIds()) {
            try {
                if (revert(id, "REVERTED", null, "System (auto, after " + REVERT_HOURS + " h)") != null) done++;
            } catch (Exception e) {
                log.error("Auto-revert of DP change {} failed", id, e);
            }
        }
        return done;
    }

    /** Changes whose 2 hours are over (used by the job). */
    List<Long> dueIds() {
        return jdbc.queryForList("SELECT id FROM super_admin_dp_change WHERE status = 'ACTIVE' AND revert_at <= NOW() ORDER BY revert_at", Long.class);
    }

    /**
     * Restores the previous DP of one change in its own transaction. The status is claimed first
     * (ACTIVE → REVERTING), so two servers or a manual revert can't restore the same change twice.
     * Returns the final status, or null when the change was not ACTIVE.
     */
    String revert(long id, String doneStatus, Integer byUser, String byName) {
        return tx.execute(st -> {
            if (jdbc.update("UPDATE super_admin_dp_change SET status = 'REVERTING' WHERE id = ? AND status = 'ACTIVE'", id) == 0) return null;
            // read as text: the driver returns DATETIME as LocalDateTime (a Timestamp cast failed here and every
            // revert rolled back), and text avoids any time-zone shift
            Map<String, Object> c = jdbc.queryForMap("SELECT DATE_FORMAT(new_delivery_date, '%Y-%m-%d %H:%i:%s') set_dt, "
                    + "DATE_FORMAT(old_delivery_date, '%Y-%m-%d %H:%i:%s') old_dt, affected_rows "
                    + "FROM super_admin_dp_change WHERE id = ?", id);
            String setDt = (String) c.get("set_dt");
            List<Map<String, Object>> rows = fromJson((String) c.get("affected_rows"));

            int restored = 0, keptNewer = 0;
            for (Map<String, Object> r : rows) {
                // new records keep each row's old value as DB text ("fmt":"text"). Records saved before this fix
                // kept a time-zone-shifted value, so they restore the change's own old_delivery_date (exact DB text).
                Object old = "text".equals(r.get("fmt")) ? r.get("old") : c.get("old_dt");
                // only if the row still holds the date we set (a PO / MA sync may have changed it since)
                String colName = column(r);   // records saved before Ext. DP support have no "col": delivery_date
                int n = jdbc.update("UPDATE " + table(r) + " SET " + colName + " = CAST(? AS DATETIME) WHERE id = ? AND " + colName + " <=> CAST(? AS DATETIME)",
                        old == null ? null : old.toString(), ((Number) r.get("id")).longValue(), setDt);
                if (n > 0) restored++; else keptNewer++;
            }
            String status = restored == 0 ? "SKIPPED" : doneStatus;
            String note = keptNewer == 0 ? "Previous DP restored." :
                    restored == 0 ? "DP was changed by another source (PO / MA sync) — not reverted." :
                            "Restored " + restored + " row(s); " + keptNewer + " row(s) changed by another source were kept.";
            jdbc.update("UPDATE super_admin_dp_change SET status = ?, reverted_at = NOW(), reverted_by = ?, reverted_by_name = ?, revert_note = ? WHERE id = ?",
                    status, byUser, byName, note, id);
            log.info("DP change {} -> {} ({})", id, status, note);
            return status;
        });
    }

    // ───────────────────────────── lists ─────────────────────────────

    public List<Map<String, Object>> activeChanges(String poNo) {
        revertOverdue();
        String where = " WHERE status = 'ACTIVE'" + (poNo != null && !poNo.isBlank() ? " AND po_no = ?" : "");
        Object[] args = poNo != null && !poNo.isBlank() ? new Object[]{poNo.trim()} : new Object[]{};
        return jdbc.query("SELECT *, TIMESTAMPDIFF(SECOND, NOW(), revert_at) secs_left, DATE_FORMAT(old_delivery_date, '%Y-%m-%d') old_dp, "
                + "DATE_FORMAT(new_delivery_date, '%Y-%m-%d') new_dp FROM super_admin_dp_change" + where
                + " ORDER BY revert_at", (rs, i) -> change(rs), args);
    }

    public Map<String, Object> history(String poNo, int page, int size) {
        revertOverdue();
        int s = Math.min(Math.max(size, 5), 100);
        int pg = Math.max(page, 0);
        boolean byPo = poNo != null && !poNo.isBlank();
        String where = byPo ? " WHERE po_no = ?" : "";
        Long total = byPo ? jdbc.queryForObject("SELECT COUNT(*) FROM super_admin_dp_change" + where, Long.class, poNo.trim())
                : jdbc.queryForObject("SELECT COUNT(*) FROM super_admin_dp_change", Long.class);
        List<Object> args = new ArrayList<>();
        if (byPo) args.add(poNo.trim());
        args.add(s);
        args.add(pg * s);
        List<Map<String, Object>> rows = jdbc.query("SELECT *, TIMESTAMPDIFF(SECOND, NOW(), revert_at) secs_left, DATE_FORMAT(old_delivery_date, '%Y-%m-%d') old_dp, "
                + "DATE_FORMAT(new_delivery_date, '%Y-%m-%d') new_dp FROM super_admin_dp_change"
                + where + " ORDER BY id DESC LIMIT ? OFFSET ?", (rs, i) -> change(rs), args.toArray());
        return Map.of("content", rows, "total", total == null ? 0 : total, "page", pg);
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private static Map<String, Object> change(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getLong("id"));
        m.put("poNo", rs.getString("po_no"));
        m.put("srNo", rs.getString("item_sr_no"));
        m.put("oldDp", rs.getString("old_dp"));
        m.put("newDp", rs.getString("new_dp"));
        // which date was changed: "EXT_DP" when the serial had an Ext. DP, else "DP"
        String rowsJson = rs.getString("affected_rows");
        m.put("field", rowsJson != null && rowsJson.contains("\"" + EXT_DP_COL + "\"") ? "EXT_DP" : "DP");
        m.put("remarks", rs.getString("remarks"));
        m.put("changedBy", rs.getString("changed_by_name"));
        m.put("changedByEmpCode", rs.getString("changed_by_emp_code"));
        m.put("changedAt", ts(rs.getTimestamp("changed_at")));
        m.put("revertAt", ts(rs.getTimestamp("revert_at")));
        m.put("secondsLeft", Math.max(0, rs.getLong("secs_left")));
        m.put("status", rs.getString("status"));
        m.put("revertedAt", ts(rs.getTimestamp("reverted_at")));
        m.put("revertedBy", rs.getString("reverted_by_name"));
        m.put("revertNote", rs.getString("revert_note"));
        return m;
    }

    private static Map<String, Object> row(String table, long id, String old, String col) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("table", table);
        m.put("id", id);
        m.put("old", old);
        m.put("col", col);
        return m;
    }

    /** Only the two date columns can be written; records without "col" (saved before Ext. DP support) are delivery_date. */
    private static String column(Map<String, Object> r) {
        return EXT_DP_COL.equals(r.get("col")) ? EXT_DP_COL : DP_COL;
    }

    /** Only the two known tables can be written (the name comes from our own JSON, never from the request). */
    private static String table(Map<String, Object> r) {
        Object t = r.get("table");
        if ("po_item".equals(t)) return "po_item";
        if ("amendment_po_item".equals(t)) return "amendment_po_item";
        throw new IllegalStateException("Unknown table " + t);
    }

    private String toJson(List<Map<String, Object>> rows) {
        List<Map<String, Object>> clean = new ArrayList<>();
        for (Map<String, Object> r : rows) clean.add(Map.of("table", r.get("table"), "id", r.get("id"),
                "old", r.get("old") == null ? "" : r.get("old"), "fmt", "text", "col", column(r)));
        try {
            return mapper.writeValueAsString(clean);
        } catch (Exception e) {
            throw new IllegalStateException("Could not record the change.");
        }
    }

    private List<Map<String, Object>> fromJson(String json) {
        try {
            List<Map<String, Object>> l = mapper.readValue(json, new TypeReference<>() { });
            for (Map<String, Object> r : l) if ("".equals(r.get("old"))) r.put("old", null);
            return l;
        } catch (Exception e) {
            throw new IllegalStateException("Change record is unreadable.");
        }
    }

    static String product(String cat) {
        String c = cat == null ? "" : cat.toLowerCase();
        if (c.contains("clip") || c.contains("erc")) return "ERC";
        if (c.contains("sleeper")) return "Sleeper";
        if (c.contains("pad")) return "Rail Pad";
        return cat == null ? "-" : cat;
    }

    private static String vendorName(String firm, String details) {
        if (firm != null && !firm.isBlank()) return firm.trim();
        if (details == null) return "-";
        return details.split("~")[0].trim();
    }

    private static String cleanCode(String code) {
        return code == null ? null : code.replace(":", "").trim();
    }

    private static String date(Timestamp t) {
        return t == null ? null : t.toLocalDateTime().toLocalDate().toString();
    }

    private static String ts(Timestamp t) {
        return t == null ? null : t.toLocalDateTime().toString();
    }

    private static String req(String v, String label) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(label + " is required.");
        return v.trim();
    }
}
