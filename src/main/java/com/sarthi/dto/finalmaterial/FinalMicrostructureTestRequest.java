package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for Final Inspection - Microstructure Test
 * Accepts parent data and nested sample data from frontend
 */
@Data
public class FinalMicrostructureTestRequest {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;
    private Integer sampleSize;
    private Integer qty;
    private String remarks;
    private String status;
    private Integer rejected;
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

