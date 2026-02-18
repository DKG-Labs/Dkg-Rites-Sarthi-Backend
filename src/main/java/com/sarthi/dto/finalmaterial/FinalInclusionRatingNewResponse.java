package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Final Inspection - Inclusion Rating (New Structure)
 * Returns parent data with nested sample data
 */
@Data
public class FinalInclusionRatingNewResponse {

    private Long id;
    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;
    private Integer sampleSize;
    private String samplingType;
    private String remarks;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    /**
     * Nested sample data
     */
    private List<SampleData> samples;

    @Data
    public static class SampleData {
        private Long id;
        private Integer samplingNo;
        private Integer sampleNo;
        private String sampleValueA;
        private String sampleValueB;
        private String sampleValueC;
        private String sampleValueD;
        private String createdBy;
        private LocalDateTime createdAt;
    }
}

