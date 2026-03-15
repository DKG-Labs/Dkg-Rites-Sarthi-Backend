package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthResponseDto;

import java.util.List;

public interface WaterCubeStrengthService {
    WaterCubeStrengthResponseDto create(WaterCubeStrengthRequestDto dto);
    WaterCubeStrengthResponseDto update(Long id, WaterCubeStrengthRequestDto dto);
    WaterCubeStrengthResponseDto getById(Long id);
    List<WaterCubeStrengthResponseDto> getAll();
    List<WaterCubeStrengthResponseDto> getByUser(Long userId);
    WaterCubeStrengthResponseDto getByDeclaration(Long declarationId);
    void delete(Long id);
}
