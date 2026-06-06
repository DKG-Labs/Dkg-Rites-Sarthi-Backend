package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RailFinalCompressionSetResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Initial Thickness (A) actual samples (1 to 3)
    private String sampleInitial1;
    private String sampleInitial2;
    private String sampleInitial3;

    // Initial Thickness (A) marginal samples (1 to 6)
    private String marginalInitial1;
    private String marginalInitial2;
    private String marginalInitial3;
    private String marginalInitial4;
    private String marginalInitial5;
    private String marginalInitial6;

    // Final Thickness (B) actual samples (1 to 3)
    private String sampleFinal1;
    private String sampleFinal2;
    private String sampleFinal3;

    // Final Thickness (B) marginal samples (1 to 6)
    private String marginalFinal1;
    private String marginalFinal2;
    private String marginalFinal3;
    private String marginalFinal4;
    private String marginalFinal5;
    private String marginalFinal6;

    private String compressionStatus;
    private Integer notOkCount;
    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
