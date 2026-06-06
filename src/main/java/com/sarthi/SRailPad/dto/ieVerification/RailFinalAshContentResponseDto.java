package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RailFinalAshContentResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    private String s1ACrucible;
    private String s1ASample;
    private String s1AAsh;
    private String s2ACrucible;
    private String s2ASample;
    private String s2AAsh;
    private String s3ACrucible;
    private String s3ASample;
    private String s3AAsh;
    private String m1ACrucible;
    private String m1ASample;
    private String m1AAsh;
    private String m2ACrucible;
    private String m2ASample;
    private String m2AAsh;
    private String m3ACrucible;
    private String m3ASample;
    private String m3AAsh;
    private String m4ACrucible;
    private String m4ASample;
    private String m4AAsh;
    private String m5ACrucible;
    private String m5ASample;
    private String m5AAsh;
    private String m6ACrucible;
    private String m6ASample;
    private String m6AAsh;
    private String s1BCrucible;
    private String s1BSample;
    private String s1BAsh;
    private String s2BCrucible;
    private String s2BSample;
    private String s2BAsh;
    private String s3BCrucible;
    private String s3BSample;
    private String s3BAsh;
    private String m1BCrucible;
    private String m1BSample;
    private String m1BAsh;
    private String m2BCrucible;
    private String m2BSample;
    private String m2BAsh;
    private String m3BCrucible;
    private String m3BSample;
    private String m3BAsh;
    private String m4BCrucible;
    private String m4BSample;
    private String m4BAsh;
    private String m5BCrucible;
    private String m5BSample;
    private String m5BAsh;
    private String m6BCrucible;
    private String m6BSample;
    private String m6BAsh;

    private String ashStatus;
    private Integer notOkCount;
    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
