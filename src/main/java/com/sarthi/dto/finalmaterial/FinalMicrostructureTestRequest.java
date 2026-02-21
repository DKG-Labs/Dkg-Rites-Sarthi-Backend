package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.util.List;

/**
 * Request DTO for Final Inspection - Microstructure Test
 * Accepts parent data and nested sample data from frontend
 */
@Data
public class FinalMicrostructureTestRequest {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;
    private Integer sampleSize;
    private Integer qty;
    private String remarks;
    private String status;
    private String createdBy;

    /**
     * Nested sample data
     */
    private List<SampleData> samples;

    @Data
    public static class SampleData {
        private Integer sampleNo;
        private Integer samplingNo;
        private String sampleType;
    }
}

