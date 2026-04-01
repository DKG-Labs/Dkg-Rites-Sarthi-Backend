package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for Final Hardness Test
 * 
 * Frontend sends data like:
 * {
 *   "inspectionCallNo": "EP-01090004",
 *   "lotNo": "lot2",
 *   "heatNo": "T844929",
 *   "qtyNo": 81,
 *   "remarks": "Paused after 1st sampling",
 *   "samples": [
 *     { "samplingNo": 1, "sampleNo": 1, "sampleValue": 0.40, "isRejected": true },
 *     { "samplingNo": 1, "sampleNo": 2, "sampleValue": 0.50, "isRejected": true },
 *     { "samplingNo": 1, "sampleNo": 3, "sampleValue": 0.47, "isRejected": true }
 *   ]
 * }
 */
@Data
public class FinalHardnessTestRequest {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;
    private Integer qtyNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;
    private String remarks;
    private String status;
    private Integer rejected;
    private String createdBy;
    private String updatedBy;
    private List<SampleData> samples;

    @Data
    public static class SampleData {
        private Integer samplingNo;
        private Integer sampleNo;
        private BigDecimal sampleValue;
        private Boolean isRejected;
    }
}

