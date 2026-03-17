package com.sarthi.Sleeper.dto.Dowel;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DowelResponseDto {

    private Long id;

    private String dateOfReceipt;

    private String gradeType;
    private String manufacturer;

    private String invoiceNumber;
    private String invoiceDate;

    private String ritesIcNumber;
    private String ritesIcDate;

    private Integer totalQtyReceived;

    private Integer createdBy;
    private LocalDateTime createdDate;

    private Integer updatedBy;
    private LocalDateTime updatedDate;

    private String status;
}