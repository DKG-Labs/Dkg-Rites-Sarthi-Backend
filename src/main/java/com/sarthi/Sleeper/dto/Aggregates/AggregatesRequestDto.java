package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;

@Data
public class AggregatesRequestDto {

    private String dateOfReceipt;
    private String gradeSpec;
    private String source;

    private String challanNumber;
    private String challanDate;

    private Double totalQtyReceived;

    private Integer createdBy;
    private Integer updatedBy;
}