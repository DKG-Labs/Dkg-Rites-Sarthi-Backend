package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPerformanceResponseDto {
    private List<StageRejectionDto> topPerforming;
    private List<StageRejectionDto> worstPerforming;
}
