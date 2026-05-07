package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.Data;

import java.util.List;

@Data
public class ParetoAnalysisResponseDto {

    private List<ParetoAnalysisDto> defects;
}