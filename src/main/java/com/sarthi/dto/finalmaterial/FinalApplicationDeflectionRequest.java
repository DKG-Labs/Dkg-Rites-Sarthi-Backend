package com.sarthi.dto.finalmaterial;

import lombok.Data;
import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Request DTO for Final Application & Deflection Test
 * 
 * Frontend sends data like:
 * {
 *   "inspectionCallNo": "EP-01090004",
 *   "lotNo": "lot2",
 *   "heatNo": "T844929",
 *   "sampleSize": 200,
 *   "remarks": "Paused after 1st sampling",
 *   "samples": [
 *     { "samplingNo": 1, "noOfSamplesFailed": 1 },
 *     { "samplingNo": 2, "noOfSamplesFailed": 2 }
 *   ]
 * }
 */
@Data
public class FinalApplicationDeflectionRequest {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;
    private Integer sampleSize;
    private String remarks;
    private String status;
    private Integer rejected;
    private String createdBy;
    private String updatedBy;
    private List<SampleData> samples;

    @Data
    public static class SampleData {
        private Integer samplingNo;
        private Integer noOfSamplesFailed;
    }
}

