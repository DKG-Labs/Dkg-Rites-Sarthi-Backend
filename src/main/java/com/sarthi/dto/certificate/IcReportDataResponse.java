package com.sarthi.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for IC Report Data response used in E-Sign workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcReportDataResponse implements Serializable {
    private String status;
    private boolean isDigitalSignatureConfig;
    private String responseText;
}
