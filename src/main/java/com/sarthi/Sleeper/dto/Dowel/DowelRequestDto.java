package com.sarthi.Sleeper.dto.Dowel;

import lombok.Data;

@Data
public class DowelRequestDto {

    private String dateOfReceipt;

    private String gradeType;
    private String manufacturer;

    private String invoiceNumber;
    private String invoiceDate;

    private String ritesIcNumber;
    private String ritesIcDate;

    private Integer totalQtyReceived;

    private Integer createdBy;
    private Integer updatedBy;
}
