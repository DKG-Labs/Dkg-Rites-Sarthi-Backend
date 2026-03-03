package com.sarthi.Sleeper.dto.HtsWire;

import lombok.Data;

import java.util.List;

@Data
public class HtsWireResponseDto {

    private Long id;

    private String dateOfReceipt;
    private String gradeSpec;
    private String manufacturer;

    private String invoiceNumber;
    private String invoiceDate;

    private String ritesIcNumber;
    private String ritesIcDate;

    private String relaxationTest;
    private String relaxationTestDate;

    private Double totalQtyReceived;

    private List<HtsCoilDetailsResponseDto> coilDetails;
}
