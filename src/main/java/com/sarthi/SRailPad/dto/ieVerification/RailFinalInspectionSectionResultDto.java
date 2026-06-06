package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;

@Data
public class RailFinalInspectionSectionResultDto {
    private Long id;
    private String sectionKey;
    private String sectionName;
    private String sampleSize;
    private String status;
}
