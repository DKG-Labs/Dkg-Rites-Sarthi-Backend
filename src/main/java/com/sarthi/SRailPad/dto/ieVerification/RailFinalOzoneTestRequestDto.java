package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailFinalOzoneTestRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    private String initialLength;
    private String stretchedLength;
    private String observation;

    private String ozoneStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
