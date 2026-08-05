package com.sarthi.SRailPad.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RailProcessCallDto {
    private String callNo;
    private String poNo;
    private String poSr;
    private String vendorCode;
    private String plantId;
    private String railPadType;
    private Integer totalQty;
    private String status;
    private LocalDateTime createdAt;

    // Process Call Details
    private String drawingNo;
    private String uom;
    private Integer qtyOnOrder;
    private Integer qtyAcceptedTillNow;
    private Integer qtyDesiredForFinal;
    private Integer qtyDue;
    private LocalDate productionInitiationDate;
}
