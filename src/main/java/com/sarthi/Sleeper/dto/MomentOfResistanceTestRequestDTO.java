package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

@Data
public class MomentOfResistanceTestRequestDTO {

    private Long MonmentOfResistanceId;
    private String batchNumber;
    private String sleeperType;
    private String castingDate;
    private String benchNumber;
    private String sleeperNo;

    private String vendorCode;
    private String plantId;
    private String shift;
    private String testResult;

    private Long createdBy;
    private Long updatedBy;

    private List<MomentOfResistanceDetailRequestDTO> details;
}
