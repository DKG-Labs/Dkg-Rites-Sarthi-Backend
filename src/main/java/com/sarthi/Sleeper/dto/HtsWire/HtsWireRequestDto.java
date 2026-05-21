package com.sarthi.Sleeper.dto.HtsWire;

import lombok.Data;

import java.util.List;

@Data
public class HtsWireRequestDto {

    private String dateOfReceipt;
    private String gradeSpec;
    private String manufacturer;

    private String invoiceNumber;
    private String invoiceDate;

    private String ritesIcNumber;
    private String ritesIcDate;

    private String relaxationTest;       // 100 Hours Test / 1000 Hours Test
    private String relaxationTestDate;
    private String relaxationTestTc;
    private String relaxationTestValidity;

    private Integer createdBy;
    private Integer updatedBy;


    private String vendorCode;
    private String plantId;

    private List<HtsCoilDetailsRequestDto> coilDetails;
}
