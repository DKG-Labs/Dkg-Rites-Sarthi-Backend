package com.sarthi.Sleeper.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HtsWirePlacementResponseDTO {

    private Long id;

    private String lineShedNo;

    private String placementDate;

    private String placementTime;

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

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private BigDecimal observedWeightKgM;
    private String status;


    private String vendorCode;
    private String plantId;
    private String shift;
}
