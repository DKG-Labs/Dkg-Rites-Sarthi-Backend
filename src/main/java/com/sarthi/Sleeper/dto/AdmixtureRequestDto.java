package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class AdmixtureRequestDto {

    private String dateOfReceipt;
    private String manufacturer;
    private String gradeSpec;

    private String invoiceNumber;
    private String invoiceDate;

    private String lotNo;
    private String mtcNo;

    private Double totalQuantity;

    private Integer createdBy;
    private Integer updatedBy;


    private String vendorCode;
    private String plantId;
}
