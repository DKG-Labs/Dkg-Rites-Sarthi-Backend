package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreCorrectionSlipResponseDTO {
    private boolean success;
    private String message;
    private String callNo;
    private String icNumber;
    private String fileName;
    private String blobFileName;
    private String blobUrl;
    private String compressedBase64;
    private Long originalSize;
    private Long compressedSize;
    private String stage;
}
