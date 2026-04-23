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
public class ChemicalAnalysisRowDTO {
    private Integer sNo;
    private String date;
    private String heatNo;
    private Integer sampleNo;
    private BigDecimal quantity;
    
    // Chemical composition
    private BigDecimal carbon;
    private BigDecimal manganese;
    private BigDecimal silicon;
    private BigDecimal sulphur;
    private BigDecimal phosphorus;
    
    // Metallurgical properties
    private BigDecimal grainSize;
    private String inclusion;
    private BigDecimal hardness;
    private BigDecimal decarb;
    
    // Statuses
    private String freedomFromDefects; // OK / NOT OK
    private String acceptedOrNot;      // Accepted / Rejected / Partially Accepted
}
