package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Page DTO for Toe Load Test, representing a single sampling round.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToeLoadAnnexurePageDTO {
    private Integer samplingNo;
    private String heatNo; // Principal heat/lot for header display if needed
    private String lotNo;
    
    private List<ToeLoadBatchDTO> rows;
}
