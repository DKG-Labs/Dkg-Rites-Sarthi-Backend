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

    private BigDecimal rmVmDefect = BigDecimal.ZERO;
    private BigDecimal rmDimentionalDefect = BigDecimal.ZERO;
    private BigDecimal rmInclusionDefect = BigDecimal.ZERO;
    private BigDecimal rmGrainSizeDefect = BigDecimal.ZERO;
    private BigDecimal rmDecarbDefect = BigDecimal.ZERO;

    private BigDecimal finalVisualDimDefect = BigDecimal.ZERO;
    private BigDecimal finalHardnessDefect = BigDecimal.ZERO;
    private BigDecimal finalInclusionDefect = BigDecimal.ZERO;
    private BigDecimal finalDeflectionDefect = BigDecimal.ZERO;
    private BigDecimal finalToeLoadDefect = BigDecimal.ZERO;

    private BigDecimal agePercentage;


}
