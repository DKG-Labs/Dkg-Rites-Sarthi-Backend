package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SleeperInspectionCallListDto {
    private Long id;
    private String callNo;
    private String poNo;
    private String srNo;
    private String callDate;
    private LocalDate desiredInspectionDate;
    private String sleeperType;
    private java.math.BigDecimal qtyOffered;
    private java.math.BigDecimal acceptedQty;
    private java.math.BigDecimal rejectedQty;
    private Integer batches;
    private String status;
    private String jobStatus;
    private String plantId;
    private String uom;
    private String icNo;
    private String icDate;
}

