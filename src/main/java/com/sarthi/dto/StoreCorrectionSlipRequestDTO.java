package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreCorrectionSlipRequestDTO {
    private String callNo;
    private String icNumber;
    private String moduleType;
    private String pdfBase64;
    private String fileName;
    private String uploadedBy;
    private String stage; // PRE_SIGN, SIGNED, ISSUED
}
