package com.sarthi;

import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.processmaterial.ProcessTurningDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

@SpringBootTest
class SarthiBackendApplicationTests {

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProcessTurningDataRepository processTurningDataRepository;

    @Test
    void testCompactToFile() throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter("compact_list.txt"))) {
            out.println("=== COMPACT LIST ===");
            List<Object[]> rawRows = inspectionCallRepository.findCompanyUnitIcNumbers();
            
            java.util.Map<String, java.util.Map<String, java.util.List<String>>> grouped = new java.util.LinkedHashMap<>();
            for (Object[] row : rawRows) {
                String company = row[0] != null ? row[0].toString() : "";
                String unit = row[1] != null ? row[1].toString() : "";
                String ic = row[2] != null ? row[2].toString() : "";
                if (company.isBlank() || unit.isBlank() || ic.isBlank()) continue;
                grouped
                    .computeIfAbsent(company, k -> new java.util.LinkedHashMap<>())
                    .computeIfAbsent(unit, k -> new java.util.ArrayList<>())
                    .add(ic);
            }

            int count = 1;
            for (var compEntry : grouped.entrySet()) {
                String company = compEntry.getKey();
                for (var unitEntry : compEntry.getValue().entrySet()) {
                    String unit = unitEntry.getKey();
                    List<String> ics = unitEntry.getValue();
                    
                    long nonNullRows = 0;
                    for (String ic : ics) {
                        var list = processTurningDataRepository.findByInspectionCallNo(ic);
                        for (var item : list) {
                            if (item.getDia1() != null || item.getDia2() != null || item.getDia3() != null) {
                                nonNullRows++;
                            }
                        }
                    }
                    
                    if (nonNullRows >= 2) {
                        out.println("ROW " + count++ + ": COMP: [" + company + "] | UNIT: [" + unit.replaceAll("\\r\\n|\\r|\\n", " ") + "] | COUNT: " + nonNullRows);
                    }
                }
            }
            out.println("=== END ===");
        }
    }
}
