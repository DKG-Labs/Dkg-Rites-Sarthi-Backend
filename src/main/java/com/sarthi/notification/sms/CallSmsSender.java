package com.sarthi.notification.sms;

import com.sarthi.service.Impl.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Background worker: resolves the IE / vendor / CM mobiles of a call, sends the SMS and logs every
 * recipient in call_sms_log (SENT / FAILED / SKIPPED). Runs after the workflow transaction committed,
 * on the async executor, so it never slows down or breaks the workflow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSmsSender {

    private final JdbcTemplate jdbc;
    private final SmsService smsService;
    private final Environment env;

    enum Product { ERC, SLEEPER, RAILPAD }

    static Product productOf(String callNo) {
        String c = callNo == null ? "" : callNo.trim().toUpperCase();
        if (c.startsWith("SF-") || c.startsWith("SP-")) return Product.SLEEPER;
        if (c.startsWith("RPP-") || c.startsWith("RPF-")) return Product.RAILPAD;
        if (c.startsWith("ER") || c.startsWith("EP") || c.startsWith("EF")) return Product.ERC;
        return null;
    }

    private static String productLabel(Product p) {
        return switch (p) { case ERC -> "ERC"; case SLEEPER -> "Sleeper"; case RAILPAD -> "Rail Pad"; };
    }

    private record Person(Integer userId, String name, String mobile, Integer cmUserId) {}
    private record Plant(String name, String mobile) {}

    // ───────────────────────────── entry points ─────────────────────────────

    /*
     * Own small pool (not a Spring bean on purpose: a second Executor bean would change which executor the
     * existing unnamed @Async notification methods use). Queue full → the SMS is dropped and logged, never blocks.
     */
    private final java.util.concurrent.ThreadPoolExecutor pool = new java.util.concurrent.ThreadPoolExecutor(
            2, 4, 60, java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.LinkedBlockingQueue<>(500),
            r -> { Thread t = new Thread(r, "CALL-SMS"); t.setDaemon(true); return t; },
            (r, ex) -> log.warn("Call SMS queue full — SMS dropped"));

    @jakarta.annotation.PreDestroy
    void shutdown() { pool.shutdown(); }

    public void assigned(String callNo, Integer ieUserId) {
        pool.execute(() -> doAssigned(callNo, ieUserId));
    }

    public void reassigned(String callNo, Integer oldIeUserId, Integer newIeUserId) {
        pool.execute(() -> doReassigned(callNo, oldIeUserId, newIeUserId));
    }

    private void doAssigned(String callNoRaw, Integer ieUserId) {
        try {
            String callNo = callNoRaw.trim().toUpperCase();
            Product p = productOf(callNo);
            if (p == null) return;
            if (ieUserId == null && callNo.startsWith("EP")) ieUserId = resolveErcProcessIe(callNo);
            if (ieUserId == null) { log.info("Call SMS {}: no IE assigned yet, nothing sent", callNo); return; }

            Person ie = person(ieUserId);
            Person cm = ie != null && ie.cmUserId() != null ? person(ie.cmUserId()) : null;
            Plant plant = plant(p, callNo);
            Map<String, String> v = values(callNo, p, plant, ie, null);
            v.putAll(callInfo(p, callNo, ieUserId));

            // single DLT template "QA Call Marked" for every recipient
            deliver(p, callNo, "ASSIGNED", "IE", ie, ieUserId, CallSmsTemplate.QA_CALL_MARKED, v);
            deliverVendor(p, callNo, "ASSIGNED", plant, ieUserId, CallSmsTemplate.QA_CALL_MARKED, v);
            deliver(p, callNo, "ASSIGNED", "CM", cm, ieUserId, CallSmsTemplate.QA_CALL_MARKED, v);
        } catch (Exception e) {
            log.warn("Call SMS (assigned) failed for {}: {}", callNoRaw, e.getMessage());
        }
    }

    private void doReassigned(String callNoRaw, Integer oldIeUserId, Integer newIeUserId) {
        try {
            String callNo = callNoRaw.trim().toUpperCase();
            Product p = productOf(callNo);
            if (p == null || newIeUserId == null || newIeUserId.equals(oldIeUserId)) return;

            Person newIe = person(newIeUserId);
            Person oldIe = oldIeUserId != null ? person(oldIeUserId) : null;
            Person cm = newIe != null && newIe.cmUserId() != null ? person(newIe.cmUserId()) : null;
            Plant plant = plant(p, callNo);
            Map<String, String> v = values(callNo, p, plant, newIe, oldIe);
            v.putAll(callInfo(p, callNo, newIeUserId));

            // single DLT template "QA Call Marked" (new IE's details) for the new IE, vendor and CM.
            // The previous IE gets no SMS: this template has no "reassigned away" wording.
            deliver(p, callNo, "REASSIGNED", "IE", newIe, newIeUserId, CallSmsTemplate.QA_CALL_MARKED, v);
            deliverVendor(p, callNo, "REASSIGNED", plant, newIeUserId, CallSmsTemplate.QA_CALL_MARKED, v);
            deliver(p, callNo, "REASSIGNED", "CM", cm, newIeUserId, CallSmsTemplate.QA_CALL_MARKED, v);
        } catch (Exception e) {
            log.warn("Call SMS (reassigned) failed for {}: {}", callNoRaw, e.getMessage());
        }
    }

    // ───────────────────────────── delivery + log ─────────────────────────────

    private void deliver(Product p, String callNo, String event, String recipientType, Person who,
                         Integer ieUserId, CallSmsTemplate tpl, Map<String, String> v) {
        if (who == null) {
            log(p, callNo, event, recipientType, null, ieUserId, null, tpl, "SKIPPED", "Recipient not found / not mapped");
            return;
        }
        send(p, callNo, event, recipientType, who.userId(), ieUserId, who.mobile(), tpl, v);
    }

    private void deliverVendor(Product p, String callNo, String event, Plant plant, Integer ieUserId,
                               CallSmsTemplate tpl, Map<String, String> v) {
        send(p, callNo, event, "VENDOR", null, ieUserId, plant == null ? null : plant.mobile(), tpl, v);
    }

    private void send(Product p, String callNo, String event, String recipientType, Integer recipientUserId,
                      Integer ieUserId, String rawMobile, CallSmsTemplate tpl, Map<String, String> v) {
        String mobile = normaliseMobile(rawMobile);
        if (mobile == null) {
            log(p, callNo, event, recipientType, recipientUserId, ieUserId, null, tpl, "SKIPPED", "No valid mobile number");
            return;
        }
        String templateId = env.getProperty("xpertsms.template.call." + tpl.propertyKey(), "").trim();
        if (templateId.isEmpty()) {
            log(p, callNo, event, recipientType, recipientUserId, ieUserId, mobile, tpl, "SKIPPED", "DLT template id not configured");
            return;
        }
        if (alreadySent(callNo, event, recipientType, ieUserId)) return;     // double click / retry
        try {
            smsService.send(mobile, tpl.render(v), templateId);
            log(p, callNo, event, recipientType, recipientUserId, ieUserId, mobile, tpl, "SENT", null);
        } catch (Exception e) {
            log(p, callNo, event, recipientType, recipientUserId, ieUserId, mobile, tpl, "FAILED", trim(e.getMessage(), 250));
        }
    }

    /**
     * ASSIGNED: never twice for the same call + recipient + IE (some flows re-insert CALL_REGISTERED).
     * REASSIGNED: only within 10 minutes (double click / retry) — a later A→B→A→B move is a real new event.
     */
    private boolean alreadySent(String callNo, String event, String recipientType, Integer ieUserId) {
        String window = "REASSIGNED".equals(event) ? " AND created_at >= NOW() - INTERVAL 10 MINUTE" : "";
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM call_sms_log WHERE call_no = ? AND event_type = ? "
                        + "AND recipient_type = ? AND status = 'SENT' AND ie_user_id <=> ?" + window,
                Integer.class, callNo, event, recipientType, ieUserId);
        return n != null && n > 0;
    }

    private void log(Product p, String callNo, String event, String recipientType, Integer recipientUserId,
                     Integer ieUserId, String mobile, CallSmsTemplate tpl, String status, String reason) {
        try {
            jdbc.update("INSERT INTO call_sms_log (product, call_no, event_type, recipient_type, recipient_user_id, "
                            + "ie_user_id, mobile_masked, template_key, status, reason) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    p.name(), callNo, event, recipientType, recipientUserId, ieUserId, mask(mobile), tpl.name(), status, reason);
        } catch (Exception e) {
            log.warn("call_sms_log insert failed ({} {} {}): {}", callNo, recipientType, status, e.getMessage());
        }
    }

    // ───────────────────────────── lookups ─────────────────────────────

    private Person person(Integer userId) {
        List<Person> l = jdbc.query(
                "SELECT USERID, COALESCE(NULLIF(FULL_NAME,''), USERNAME) AS nm, MOBILENUMBER, controlling_manager_id "
                        + "FROM user_master WHERE USERID = ? LIMIT 1",
                (rs, i) -> new Person(rs.getInt(1), rs.getString(2), rs.getString(3),
                        (Integer) rs.getObject(4)),
                userId);
        return l.isEmpty() ? null : l.get(0);
    }

    /** ERC process calls carry no assigned user on CALL_REGISTERED: the Process IE comes from the POI mapping. */
    private Integer resolveErcProcessIe(String callNo) {
        List<Integer> l = jdbc.queryForList(
                "SELECT um.USERID FROM inspection_calls ic "
                        + "JOIN poi_process_ie_mapping m ON m.poi_code = ic.place_of_inspection "
                        + "JOIN user_master um ON um.EMPLOYEE_CODE = m.employee_code "
                        + "WHERE ic.ic_number = ? LIMIT 1", Integer.class, callNo);
        return l.isEmpty() ? null : l.get(0);
    }

    /** Plant name + contact person number (vendor SMS goes to the plant contact). */
    private Plant plant(Product p, String callNo) {
        String sql = switch (p) {
            case ERC -> "SELECT COALESCE(NULLIF(pp.unit_name,''), pp.company_name), pp.contact_person_number "
                    + "FROM inspection_calls ic JOIN pincode_poi_mapping pp ON pp.poi_code = ic.place_of_inspection "
                    + "WHERE ic.ic_number = ? LIMIT 1";
            case SLEEPER -> "SELECT COALESCE(NULLIF(vp.plant_name,''), vp.company_name), vp.contact_person_number "
                    + "FROM sleeper_inspection_call c JOIN vendor_plant vp "
                    + "ON REPLACE(vp.plant_id, ':', '') = REPLACE(c.plant_id, ':', '') "
                    + "WHERE UPPER(c.call_no) = ? LIMIT 1";
            case RAILPAD -> "SELECT COALESCE(NULLIF(rp.plant_name,''), rp.company_name), rp.contact_person_number "
                    + "FROM rail_inspection_call c JOIN rail_vendor_plant rp "
                    + "ON REPLACE(rp.plant_id, ':', '') = REPLACE(c.plant_id, ':', '') "
                    + "WHERE UPPER(c.call_no) = ? LIMIT 1";
        };
        List<Plant> l = jdbc.query(sql, (rs, i) -> new Plant(rs.getString(1), rs.getString(2)), callNo);
        return l.isEmpty() ? null : l.get(0);
    }

    private static Map<String, String> values(String callNo, Product p, Plant plant, Person ie, Person oldIe) {
        Map<String, String> v = new HashMap<>();
        v.put("callNo", callNo);
        v.put("product", productLabel(p));
        v.put("plant", plant != null && plant.name() != null ? plant.name().trim() : "-");
        v.put("firm", cut(plant != null ? plant.name() : null));            // overwritten by company name when known
        v.put("ieName", cut(ie != null ? ie.name() : null));
        String ieMob = ie != null ? normaliseMobile(ie.mobile()) : null;
        v.put("ieMobile", ieMob != null ? ieMob : "-");
        v.put("oldIeName", oldIe != null && oldIe.name() != null ? oldIe.name().trim() : "-");
        return v;
    }

    /**
     * Values for the "QA Call Marked" template: railway, PO no + date, firm, call date and the IE's RIO.
     * Each DLT variable is limited to 30 characters, so every value is cut to 30.
     */
    private Map<String, String> callInfo(Product p, String callNo, Integer ieUserId) {
        String sql = switch (p) {
            case ERC -> "SELECT ic.po_no, ic.created_at, ic.company_name FROM inspection_calls ic WHERE ic.ic_number = ? LIMIT 1";
            case SLEEPER -> "SELECT c.po_no, c.created_at, NULL FROM sleeper_inspection_call c WHERE UPPER(c.call_no) = ? LIMIT 1";
            case RAILPAD -> "SELECT c.po_no, c.created_at, NULL FROM rail_inspection_call c WHERE UPPER(c.call_no) = ? LIMIT 1";
        };
        Map<String, String> v = new HashMap<>();
        java.time.format.DateTimeFormatter d = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            List<Object[]> rows = jdbc.query(sql, (rs, i) -> new Object[]{rs.getString(1), rs.getTimestamp(2), rs.getString(3)}, callNo);
            String poFull = rows.isEmpty() ? null : (String) rows.get(0)[0];
            String poNo = poFull == null ? null : poFull.split("/")[0].trim();
            java.sql.Timestamp callTs = rows.isEmpty() ? null : (java.sql.Timestamp) rows.get(0)[1];
            String company = rows.isEmpty() ? null : (String) rows.get(0)[2];

            String rly = null, poDate = null;
            if (poNo != null) {
                List<Object[]> po = jdbc.query("SELECT rly_short_name, po_date FROM po_header WHERE po_no = ? LIMIT 1",
                        (rs, i) -> new Object[]{rs.getString(1), rs.getTimestamp(2)}, poNo);
                if (!po.isEmpty()) {
                    rly = (String) po.get(0)[0];
                    java.sql.Timestamp pd = (java.sql.Timestamp) po.get(0)[1];
                    poDate = pd == null ? null : pd.toLocalDateTime().toLocalDate().format(d);
                }
            }
            v.put("rly", cut(rly));
            v.put("poNo", cut(poNo));
            v.put("poDate", cut(poDate));
            v.put("callDate", cut(callTs == null ? null : callTs.toLocalDateTime().toLocalDate().format(d)));
            if (company != null && !company.isBlank()) v.put("firm", cut(company));
        } catch (Exception e) {
            log.warn("Call SMS {}: call/PO details not found: {}", callNo, e.getMessage());
        }
        List<String> rio = jdbc.queryForList("SELECT Region FROM user_master WHERE USERID = ? LIMIT 1", String.class, ieUserId);
        v.put("rio", cut(rio.isEmpty() ? null : rio.get(0)));
        return v;
    }

    /** DLT variables are max 30 characters; empty -> "-". */
    private static String cut(String s) {
        if (s == null || s.isBlank()) return "-";
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() > 30 ? t.substring(0, 30) : t;
    }

    /** 10-digit Indian mobile (drops +91 / 0 / spaces); null if not valid. */
    static String normaliseMobile(String raw) {
        if (raw == null) return null;
        String d = raw.replaceAll("[^0-9]", "");
        if (d.length() == 12 && d.startsWith("91")) d = d.substring(2);
        if (d.length() == 11 && d.startsWith("0")) d = d.substring(1);
        return d.matches("[6-9][0-9]{9}") ? d : null;
    }

    private static String mask(String m) {
        return m == null || m.length() < 4 ? null : m.substring(0, 2) + "XXXXXX" + m.substring(m.length() - 2);
    }

    private static String trim(String s, int max) {
        return s == null ? null : (s.length() > max ? s.substring(0, max) : s);
    }
}
