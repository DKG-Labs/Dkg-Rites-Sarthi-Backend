package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for Final Inspection - Freedom from Defects Test
 * Accepts parent data and nested sample data from frontend
 */
@Data
public class FinalFreedomFromDefectsTestRequest {

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
        private Integer samplingNo;
        private Integer sampleNo;
        private String sampleType;
    }
}

