package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Cement.CementSettingTimeRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementSettingTimeResponseDto;
import java.util.List;

public interface CementSettingTimeService {
    CementSettingTimeResponseDto create(CementSettingTimeRequestDto dto);
    CementSettingTimeResponseDto update(Long id, CementSettingTimeRequestDto dto);
    CementSettingTimeResponseDto getById(Long id);
    List<CementSettingTimeResponseDto> getAll();
    List<CementSettingTimeResponseDto> getPeriodic();
    void delete(Long id);
    CementSettingTimeResponseDto getByRequestId(Long requestId);
}
