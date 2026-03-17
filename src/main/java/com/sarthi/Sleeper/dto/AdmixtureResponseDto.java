package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdmixtureResponseDto {

    private Long id;

    private String dateOfReceipt;
    private String manufacturer;
    private String gradeSpec;

    private String invoiceNumber;
    private String invoiceDate;

    private String lotNo;
    private String mtcNo;

    private Double totalQuantity;

    private Integer createdBy;
    private LocalDateTime createdDate;

    private Integer updatedBy;
    private LocalDateTime updatedDate;
    private String status;
}