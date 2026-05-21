package com.sarthi.dto.summaryDtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CallCalculationDto {

    private String poNo;

    private ProcessSummaryDto processQty;

    private BigDecimal rmVmDefect = BigDecimal.ZERO;
    private BigDecimal rmDimensionalDefect = BigDecimal.ZERO;
    private BigDecimal rmInclusionDefect = BigDecimal.ZERO;
    private BigDecimal rmGrainSizeDefect = BigDecimal.ZERO;
    private BigDecimal rmDecarbDefect = BigDecimal.ZERO;

    private BigDecimal finalVisualDimDefect = BigDecimal.ZERO;
    private BigDecimal finalHardnessDefect = BigDecimal.ZERO;
    private BigDecimal finalInclusionDefect = BigDecimal.ZERO;
    private BigDecimal finalDeflectionDefect = BigDecimal.ZERO;
    private BigDecimal finalToeLoadDefect = BigDecimal.ZERO;

}
