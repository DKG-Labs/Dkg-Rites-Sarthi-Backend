package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Final Inspection - Microstructure Test
 * Returns parent data with nested sample data
 */
@Data
public class FinalMicrostructureTestResponse {

    private Long id;
    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;
    private Integer sampleSize;
    private Integer qty;
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
        private Integer sampleNo;
        private String sampleType;
        private String createdBy;
        private LocalDateTime createdAt;
    }
}

