package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for saving a Correction Slip.
 * One request can contain multiple correction rows for a single callNo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionSlipRequestDTO {

    /** The inspection call number (e.g. EP-07030001) */
    private String callNo;

    /** Username or user ID who is creating / updating the slip */
    private String createdBy;

    /** List of individual correction rows */
    private List<RowDTO> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowDTO {
        private String columnName;
        private String readAs;
        private String insteadOf;
    }
}
