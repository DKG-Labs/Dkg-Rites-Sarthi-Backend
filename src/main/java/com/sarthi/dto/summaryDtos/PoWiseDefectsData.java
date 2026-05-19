package com.sarthi.dto.summaryDtos;

import com.sarthi.dto.reports.ProcessQtyDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PoWiseDefectsData {

    private String zonalRailway;
    private String vendor;
    private String typeOfErc;
    private String poDate;
    private String poNo;
    private BigDecimal qtyAccpeted;
    private BigDecimal qtyInspected;
    private BigDecimal totalRejected;

    private ProcessQtyDto processQty;

}
