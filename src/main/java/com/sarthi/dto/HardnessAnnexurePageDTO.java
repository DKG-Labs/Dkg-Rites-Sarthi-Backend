package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing a single page/report for one Lot + Heat + Sampling combination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HardnessAnnexurePageDTO {
    private String lotNo;
    private String heatNo;
    private Integer samplingNo;
    private Integer qtyNo;
    private Integer sampleSize;
    private String dateOfInspection;
    
    // One or more technical rows (Heat/Lot details + Readings)
    private List<HardnessAnnexureRowDTO> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HardnessAnnexureRowDTO {
        private String heatNo;
        private String lotNo;
        private String colourCode;
        private Integer qty;
        private Integer sampleSize;
        
        // List of readings (each inner list is one sample row of 10 readings)
        private List<List<BigDecimal>> readings;
        
        private Integer defectives;
        private Integer cumulativeDefectives;
        private String status; // Accepted, Not Accepted, N/A
    }
}
