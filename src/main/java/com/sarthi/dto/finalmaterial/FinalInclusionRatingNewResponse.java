package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;
    private String samplingType;
    private String remarks;
    private String status;
    private Integer rejected;
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
        private String sampleTypeA;
        private String sampleValueB;
        private String sampleTypeB;
        private String sampleValueC;
        private String sampleTypeC;
        private String sampleValueD;
        private String sampleTypeD;
        private String createdBy;
        private LocalDateTime createdAt;
    }
}

