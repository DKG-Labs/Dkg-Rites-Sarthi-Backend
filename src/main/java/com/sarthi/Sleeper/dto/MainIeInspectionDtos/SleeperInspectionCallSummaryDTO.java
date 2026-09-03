package com.sarthi.Sleeper.dto.MainIeInspectionDtos;

import lombok.Data;

@Data
public class SleeperInspectionCallSummaryDTO {

    private String poNo;
    private String srNo;

    private String sleeperType;
    private String callDate;

    private Integer quantityOnOrder;
    private Integer cumulativeQtyOffered;
    private Integer cumulativeQtyPassed;

    private Integer qtyOfferedNow;
    private Integer noOfBatches;

    private Integer totalAccepted;
    private Integer totalRejected;

    private Integer noOfEtSleepers;
    private String desiredInspectionDate;
}
