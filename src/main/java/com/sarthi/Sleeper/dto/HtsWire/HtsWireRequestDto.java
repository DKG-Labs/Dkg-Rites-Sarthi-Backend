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

    private String relaxationTest;       // YES / NO
    private String relaxationTestDate;

    private Integer createdBy;
    private Integer updatedBy;

    private List<HtsCoilDetailsRequestDto> coilDetails;
}
