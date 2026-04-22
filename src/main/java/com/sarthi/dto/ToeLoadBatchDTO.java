package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Batch DTO representing a specific Heat/Lot combination within a sampling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToeLoadBatchDTO {
    private String heatNo;
    private String lotNo;
    private String colourCode;
    private Integer qty;
    private Integer sampleSize;
    
    // List of rows, where each row contains up to 10 readings
    private List<List<BigDecimal>> readings;
    
    private Integer defectives;
    private Integer cumulativeDefectives;
    private String status; // Accepted / Not Accepted
}
