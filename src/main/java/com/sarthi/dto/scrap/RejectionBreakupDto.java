package com.sarthi.dto.scrap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionBreakupDto {

    private String stage;       // PROCESS or FINAL
    private LocalDate date;
    private String shift;
    private String lineName;

    private List<StageSection> stageSections;

    @Data
    @AllArgsConstructor
    public static class StageSection {
        private String stageName;
        private int totalRejected;
        private List<DefectEntry> defects;
    }

    @Data
    @AllArgsConstructor
    public static class DefectEntry {
        private String defectName;
        private int rejectedQty;
    }
}
