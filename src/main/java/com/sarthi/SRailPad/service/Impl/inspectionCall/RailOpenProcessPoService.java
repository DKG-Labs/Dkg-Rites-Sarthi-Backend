package com.sarthi.SRailPad.service.Impl.inspectionCall;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rail Pad vendor – Declare New Production: POs that have a process (RPP) call still in progress at the plant.
 * In progress = from creation until the call is Sent to IBS, Withdrawn or Cancelled (checked on the call status
 * and on the latest rail_workflow_transaction). Final (RPF) calls do not count.
 */
@Service
@RequiredArgsConstructor
public class RailOpenProcessPoService {

    private final JdbcTemplate jdbc;

    public List<Map<String, Object>> openProcessPos(String vendorCode, String plantId) {
        if (vendorCode == null || vendorCode.isBlank()) return List.of();
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT SUBSTRING_INDEX(c.po_no,'/',1) AS poNo, DATE(MAX(ph.po_date)) AS poDate, "
                        + "GROUP_CONCAT(DISTINCT c.call_no ORDER BY c.call_no) AS callNos "
                        + "FROM rail_inspection_call c "
                        + "LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(c.po_no,'/',1) COLLATE utf8mb4_unicode_ci "
                        + "LEFT JOIN rail_workflow_transaction w ON w.workflow_transition_id = ("
                        + "   SELECT MAX(w2.workflow_transition_id) FROM rail_workflow_transaction w2 "
                        + "   WHERE w2.request_id COLLATE utf8mb4_unicode_ci = c.call_no COLLATE utf8mb4_unicode_ci) "
                        + "WHERE c.call_no LIKE 'RPP-%' "
                        + "AND REPLACE(TRIM(c.vendor_code),':','') = REPLACE(TRIM(?),':','') ");
        args.add(vendorCode);
        if (plantId != null && !plantId.isBlank()) {
            // plant_id looks like "1011527/KosiKalan" — compare the whole value (the part before '/' is the vendor code)
            sql.append("AND LOWER(REPLACE(TRIM(c.plant_id),':','')) = LOWER(REPLACE(TRIM(?),':','')) ");
            args.add(plantId);
        }
        sql.append("AND UPPER(COALESCE(c.status,'')) NOT IN ('WITHDRAW','WITHDRAWN','CANCELLED','CANCEL','SEND_CALL_TO_IBS') ")
           .append("AND UPPER(CONCAT(COALESCE(w.status,''),'|',COALESCE(w.action,''))) NOT REGEXP 'SEND_CALL_TO_IBS|SENT_TO_IBS|SEND_TO_IBS|WITHDRAW|CANCEL' ")
           .append("GROUP BY SUBSTRING_INDEX(c.po_no,'/',1) ORDER BY poNo");
        return jdbc.queryForList(sql.toString(), args.toArray());
    }
}
