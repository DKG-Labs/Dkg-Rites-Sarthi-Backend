package com.sarthi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = SarthiBackendApplication.class)
public class TestIcCounts {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Test
    public void testEp0925260001() {
        System.out.println("=== Testing connection to UAT DB sarthibackenddb ===");
        try {
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://sarthibackenddb.mysql.database.azure.com:3306/sarthidb?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "DkgSarthi",
                "Test@123"
            );
            System.out.println("Connected to UAT DB successfully!");
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(
                "SELECT WORKFLOWTRANSITIONID, REQUESTID, STATUS, ACTION, REMARKS FROM workflow_transition WHERE REQUESTID = 'EP-0925260001'"
            );
            while (rs.next()) {
                System.out.println("UAT wt: ID=" + rs.getLong("WORKFLOWTRANSITIONID") + ", req=" + rs.getString("REQUESTID") + ", status=" + rs.getString("STATUS") + ", action=" + rs.getString("ACTION"));
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("UAT DB error: " + e.getMessage());
        }

        System.out.println("=== Search information_schema for column WORKFLOWTRANSITIONID ===");
        List<Map<String, Object>> cols = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME LIKE '%WORKFLOWTRANSITIONID%' AND TABLE_SCHEMA = 'sarthidb'"
        );
        for (Map<String, Object> c : cols) {
            System.out.println("Col: " + c);
        }

        System.out.println("=== Searching like EP-0925% in workflow_transition ===");
        List<Map<String, Object>> wtSearch = jdbcTemplate.queryForList(
            "SELECT workflowtransitionid, requestid, status FROM workflow_transition WHERE requestid LIKE '%EP-%' ORDER BY workflowtransitionid DESC LIMIT 10"
        );
        for (Map<String, Object> r : wtSearch) {
            System.out.println("wtEP: " + r);
        }

        System.out.println("=== 3. Checking call_cancellation_details for EP-0925260001 ===");
        List<Map<String, Object>> cdRows = jdbcTemplate.queryForList(
            "SELECT * FROM call_cancellation_details WHERE call_number = 'EP-0925260001'"
        );
        System.out.println("cdRows count: " + cdRows.size());
        for (Map<String, Object> r : cdRows) {
            System.out.println("cdRow: " + r);
        }

        System.out.println("=== 4. Checking ibs_call_registration for EP-0925260001 ===");
        List<Map<String, Object>> ibsRows = jdbcTemplate.queryForList(
            "SELECT * FROM ibs_call_registration WHERE call_number = 'EP-0925260001'"
        );
        System.out.println("ibsRows count: " + ibsRows.size());
        for (Map<String, Object> r : ibsRows) {
            System.out.println("ibsRow: " + r);
        }

        System.out.println("=== 5. Running getErcCancelledInspectionCalls ===");
        List<Object[]> ercCancelled = inspectionCallRepository.getErcCancelledInspectionCalls();
        System.out.println("ercCancelled count: " + ercCancelled.size());
        boolean foundEp = false;
        for (Object[] row : ercCancelled) {
            String callNo = String.valueOf(row[0]);
            if (callNo.contains("EP-0925260001")) {
                foundEp = true;
                System.out.println("FOUND EP-0925260001 in getErcCancelledInspectionCalls!");
            }
        }
        System.out.println("Found EP-0925260001? " + foundEp);
    }
}


