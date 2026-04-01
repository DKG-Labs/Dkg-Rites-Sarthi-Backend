package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
/**
 * Request DTO for Final Toe Load Test
 * 
 * Frontend sends data like:
 * {
 *   "inspectionCallNo": "EP-01090004",
 *   "lotNo": "lot2",
 *   "heatNo": "T844929",
 *   "qtyNo": 81,
 *   "remarks": "Paused after 1st sampling",
 *   "samples": [
 *     { "samplingNo": 1, "sampleNo": 1, "sampleValue": 2500.50, "isRejected": false },
 *     { "samplingNo": 1, "sampleNo": 2, "sampleValue": 2600.75, "isRejected": false }
 *   ]
 * }
 */
@Data
public class FinalToeLoadTestRequest {

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

