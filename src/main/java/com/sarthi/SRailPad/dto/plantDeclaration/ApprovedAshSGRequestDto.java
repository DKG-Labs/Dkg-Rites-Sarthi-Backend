package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ApprovedAshSGRequestDto {
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private String padType;
    private Double ashContentA;
    private Double specificGravityA;
    private Double ashContentB;
    private Double specificGravityB;
    private String approvalRefNo;
    private LocalDate approvalDate;
    private Long createdBy;
    private Long updatedBy;
}
