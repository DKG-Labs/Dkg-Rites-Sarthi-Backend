package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalApplicationDeflectionResponseDTO {
    private String inspectionCallNo;
    private String manufacturer;
    private String vendor;
    private String certificateNo;
    private String productName;
    private String dateOfInspection;
    
    private List<ApplicationDeflectionPageDTO> pages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationDeflectionPageDTO {
        private String heatNo;
        private String lotNo;
        private String colourCode;
        private String quantity;
        private String sampleSize;
        private Integer samplingNo;
        private Integer noOfDefectives;
        private Integer cumulativeDefectives;
        private String testResult; // Satisfactory / Not Satisfactory
        private String status;     // Accepted / Rejected / Second sampling required
    }
}
