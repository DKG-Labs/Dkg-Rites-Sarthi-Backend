package com.sarthi.dto.finalmaterial;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * DTO for Final Visual Inspection
 * Transfers visual inspection data between frontend and backend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalVisualInspectionDto {

    private Long id;
    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfInspection;

    // Visual Inspection Data
    private Integer firstSampleRejected;
    private Integer secondSampleRejected;
    private Integer totalRejected;

    // Status and Remarks
    private String status; // Values: 'OK', 'NOT_OK', 'PENDING'
    private String remarks;

    // Audit Fields
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private String updatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;
}

