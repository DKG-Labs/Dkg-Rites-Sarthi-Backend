package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleResponseDto;

import java.util.List;

public interface WaterCubeSampleService {

    WaterCubeSampleResponseDto create(WaterCubeSampleRequestDto dto);

    WaterCubeSampleResponseDto update(Long id, WaterCubeSampleRequestDto dto);

    WaterCubeSampleResponseDto getById(Long id);

    List<WaterCubeSampleResponseDto> getAll();

    List<WaterCubeSampleResponseDto> getByUser(Long userId);

    void delete(Long id);
}
