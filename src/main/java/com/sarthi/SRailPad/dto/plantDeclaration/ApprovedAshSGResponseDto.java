package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ApprovedAshSGResponseDto {
    private Long id;
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
    private String status;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
