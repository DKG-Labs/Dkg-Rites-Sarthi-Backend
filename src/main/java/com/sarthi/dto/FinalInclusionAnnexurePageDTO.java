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
public class FinalInclusionAnnexurePageDTO {
    private String heatNo;
    private String lotNo;
    private Integer samplingNo;
    private Integer sampleSize;
    private String colourCode;
    private String quantity;
    private String overallStatus;

    private List<InclusionRowDTO> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InclusionRowDTO {
        private Integer sNo;
        private String heatNo;
        private String lotNo;
        private String colourCode;
        private String quantity;
        private String sampleSize;
        private String sampleNo;

        private String inclusionAThin;
        private String inclusionAThick;
        private String inclusionBThin;
        private String inclusionBThick;
        private String inclusionCThin;
        private String inclusionCThick;
        private String inclusionDThin;
        private String inclusionDThick;

        private String microstructureResult;
        private String freedomResult;
        private String decarbResult;
    }
}
