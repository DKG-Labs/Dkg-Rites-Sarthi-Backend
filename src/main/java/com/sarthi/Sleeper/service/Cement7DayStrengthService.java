package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Cement.Cement7DayStrengthRequestDto;
import com.sarthi.Sleeper.dto.Cement.Cement7DayStrengthResponseDto;

import java.util.List;

public interface Cement7DayStrengthService {
    Cement7DayStrengthResponseDto create(Cement7DayStrengthRequestDto dto);
    Cement7DayStrengthResponseDto update(Long id, Cement7DayStrengthRequestDto dto);
    Cement7DayStrengthResponseDto getById(Long id);
    List<Cement7DayStrengthResponseDto> getAll();
    void delete(Long id);
    Cement7DayStrengthResponseDto getByRequestId(Long requestId);
}
