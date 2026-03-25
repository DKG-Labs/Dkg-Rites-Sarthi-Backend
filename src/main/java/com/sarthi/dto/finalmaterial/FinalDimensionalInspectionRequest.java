package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for Final Dimensional Inspection
 * 
 * Frontend sends data like:
 * {
 *   "inspectionCallNo": "EP-01090004",
 *   "lotNo": "lot2",
 *   "heatNo": "T844929",
 *   "sampleSize": 200,
 *   "remarks": "Paused after 1st sampling",
 *   "samples": [
 *     { "samplingNo": 1, "goGaugeFailed": 1, "noGoGaugeFailed": 0, "flatnessFailed": 0 },
 *     { "samplingNo": 2, "goGaugeFailed": 2, "noGoGaugeFailed": 1, "flatnessFailed": 0 }
 *   ]
 * }
 */
@Data
public class FinalDimensionalInspectionRequest {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;
    private Integer sampleSize;
    private String status;
    private Integer rejected;
    private String remarks;
    private String createdBy;
    private String updatedBy;
    private List<SampleData> samples;

    @Data
    public static class SampleData {
        private Integer samplingNo;
        private Integer goGaugeFailed;
        private Integer noGoGaugeFailed;
        private Integer flatnessFailed;
    }
}

