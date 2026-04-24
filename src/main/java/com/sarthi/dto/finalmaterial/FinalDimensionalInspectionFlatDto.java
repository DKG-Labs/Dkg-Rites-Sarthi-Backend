package com.sarthi.dto.finalmaterial;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * DTO for Final Dimensional Inspection - FLAT STRUCTURE
 *
 * Used for storing dimensional inspection data with flat fields.
 * This is the OLD structure used by Final Visual Inspection page.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalDimensionalInspectionFlatDto {

    private Long id;
    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;

    // 1st Sampling
    private Integer firstSampleGoGaugeFail;
    private Integer firstSampleNoGoFail;
    private Integer firstSampleFlatBearingFail;
    
    // Detailed 1st Sampling
    private Integer firstSampleMainBoxGo;
    private Integer firstSampleMainBoxNoGo;
    private Integer firstSampleFallingGo;
    private Integer firstSampleFallingNoGo;
    private Integer firstSampleFlatBearingGo;
    private Integer firstSampleFlatBearingNoGo;

    // 2nd Sampling
    private Integer secondSampleGoGaugeFail;
    private Integer secondSampleNoGoFail;
    private Integer secondSampleFlatBearingFail;

    // Detailed 2nd Sampling
    private Integer secondSampleMainBoxGo;
    private Integer secondSampleMainBoxNoGo;
    private Integer secondSampleFallingGo;
    private Integer secondSampleFallingNoGo;
    private Integer secondSampleFlatBearingGo;
    private Integer secondSampleFlatBearingNoGo;

    // Total and Status
    private Integer totalRejected;
    private String status;
    private String remarks;

    // Audit Fields
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private String updatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;
}

