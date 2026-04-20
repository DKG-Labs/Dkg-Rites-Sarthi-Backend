package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.WaterQualityTestDto;
import java.util.List;

public interface WaterQualityTestService {
    WaterQualityTestDto create(WaterQualityTestDto dto);
    WaterQualityTestDto update(Long id, WaterQualityTestDto dto);
    WaterQualityTestDto getById(Long id);
    List<WaterQualityTestDto> getAll();
    List<WaterQualityTestDto> getByUserId(Integer userId);
    void delete(Long id);
}
