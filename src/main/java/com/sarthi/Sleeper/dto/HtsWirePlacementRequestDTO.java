package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class HtsWirePlacementRequestDTO {

    private String lineShedNo;

    private String placementDate;     // yyyy-MM-dd

    private String placementTime;     // HH:mm

    private String batchNo;

    private String benchNo;

    private String sleeperType;

    private Integer noOfWiresUsed;

    private Double htsWireDiaMm;

    private Double layLengthMm;

    private Boolean arrangementOk;

    private String overallStatus;

    private String remarks;

    private int createdBy;

    private int updatedBy;
}

