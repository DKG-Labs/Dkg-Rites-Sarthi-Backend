package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.Data;

import java.util.List;

@Data
public class ProcessDefectDistributionResponseDto {

    private List<DefectDistributionDto> defects;
}