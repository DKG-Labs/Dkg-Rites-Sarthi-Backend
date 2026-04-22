package com.sarthi.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightAnnexurePageDTO {
    private Integer samplingNo;
    private String heatNo;
    private String lotNo;
    
    // Rows (batches) for this page/sampling round
    private List<WeightBatchDTO> rows;
}
