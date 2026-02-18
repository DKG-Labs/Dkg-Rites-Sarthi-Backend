package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.util.List;

/**
 * Request DTO for Final Inspection - Inclusion Rating (New Structure)
 * Accepts parent data and nested sample data from frontend
 */
@Data
public class FinalInclusionRatingNewRequest {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;
    private Integer sampleSize;
    private String samplingType;
    private String remarks;
    private String status;
    private String createdBy;

    /**
     * Nested sample data
     */
    private List<SampleData> samples;

    @Data
    public static class SampleData {
        private Integer samplingNo;
        private Integer sampleNo;
        private String sampleValueA;
        private String sampleTypeA;
        private String sampleValueB;
        private String sampleTypeB;
        private String sampleValueC;
        private String sampleTypeC;
        private String sampleValueD;
        private String sampleTypeD;
    }
}

