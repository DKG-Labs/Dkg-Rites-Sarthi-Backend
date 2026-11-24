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

    private java.math.BigDecimal qtyOfferedNow;
    private Integer noOfBatches;

    private java.math.BigDecimal totalAccepted;
    private java.math.BigDecimal totalRejected;

    private Integer noOfEtSleepers;
    private String desiredInspectionDate;
}
