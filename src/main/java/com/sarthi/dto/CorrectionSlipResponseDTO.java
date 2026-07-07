package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a single correction slip row.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionSlipResponseDTO {
    private Long id;
    private String callNo;
    private String columnName;
    private String readAs;
    private String insteadOf;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
