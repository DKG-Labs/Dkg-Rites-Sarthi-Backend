package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalChemicalAnalysisRowDTO {
    private Integer sNo;
    private String heatNo;
    private String lotNo;
    private String colourCode;
    private Integer qtyNo;
    private Integer sampleSize;
    
    // Chemical composition
    private BigDecimal carbonPercent;
    private BigDecimal manganesePercent;
    private BigDecimal siliconPercent;
    private BigDecimal sulphurPercent;
    private BigDecimal phosphorusPercent;
    
    // Status & Remarks
    private String remarks;
    private String acceptedOrRejected;
    private String signOfSupervisor;
}
