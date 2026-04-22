package com.sarthi.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightBatchDTO {
    private String heatNo;
    private String lotNo;
    private String colourCode;
    private Integer qty;
    private Integer sampleSize;
    
    // Each row in the readings table (sub-rows of 10 samples)
    private List<List<BigDecimal>> readings;
    
    private Integer defectives;
    private Integer cumulativeDefectives;
    private String status; // Accepted / Not Accepted
}
