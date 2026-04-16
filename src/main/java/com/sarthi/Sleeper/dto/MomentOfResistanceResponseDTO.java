package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MomentOfResistanceResponseDTO {

    private Long id;

    private String batchNumber;
    private String sleeperType;
    private String benchNumber;
    private String sleeperNo;

    private String testResult;
    private String remarks;

    private String vendorCode;
    private String plantId;
    private String shift;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;
}
