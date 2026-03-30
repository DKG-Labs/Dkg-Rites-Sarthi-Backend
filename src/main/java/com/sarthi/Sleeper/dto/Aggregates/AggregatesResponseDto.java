package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AggregatesResponseDto {

    private Long id;

    private String dateOfReceipt;
    private String gradeSpec;
    private String source;

    private String challanNumber;
    private String challanDate;

    private Double totalQtyReceived;

    private Integer createdBy;
    private LocalDateTime createdDate;

    private Integer updatedBy;
    private LocalDateTime updatedDate;

    private String status;


    private String vendorCode;
    private String plantId;
}
