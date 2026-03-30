package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RawMaterialSourceResponseDto {

    private Long id;

    private String rawMaterialType;
    private String supplierName;
    private String approvalReference;

    private String validFrom;
    private String validTo;

    private Integer createdBy;
    private LocalDateTime createdDate;

    private Integer updatedBy;
    private LocalDateTime updatedDate;

    private String status;


    private String vendorCode;
    private String plantId;
}
