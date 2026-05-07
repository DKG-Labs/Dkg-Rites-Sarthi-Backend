package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.Data;

import java.util.List;

@Data
public class DefectDistributionResponseDto {

    private List<DefectReasonDistributionDto> defects;
}
