package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class RawMaterialSourceRequestDto {

    private String rawMaterialType;
    private String supplierName;
    private String approvalReference;

    private String validFrom;   // dd/MM/yyyy
    private String validTo;

    private Integer createdBy;
    private Integer updatedBy;


    private String vendorCode;
    private String plantId;
}
