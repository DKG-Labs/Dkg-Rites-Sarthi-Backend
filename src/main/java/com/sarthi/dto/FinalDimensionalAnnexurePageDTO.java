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
public class FinalDimensionalAnnexurePageDTO {
    private String heatNo;
    private String lotNo;
    private Integer samplingNo;
    private Integer sampleSize;
    private String colourCode;
    private String quantity;
    private String status;

    private List<DimensionalRowDTO> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionalRowDTO {
        private Integer sNo;
        private String heatNo;
        private String lotNo;
        private String colourCode;
        private String qty;
        private Integer sampleSize;
        
        // Main gauge acceptance
        private String mainBoxGo;
        private String mainBoxNoGo;
        
        // Falling in Gauges
        private String fallingGo;
        private String fallingNoGo;
        
        // Flat bearing length
        private String flatBearingGo;
        private String flatBearingNoGo;
        
        private Integer defectives;
        private Integer cumulativeDefectives;
        private String status;
    }
}
