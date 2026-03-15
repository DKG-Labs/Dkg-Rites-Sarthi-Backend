package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Aggregates.AggregateGranulometricRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateGranulometricResponseDto;
import java.util.List;

public interface AggregateGranulometricService {
    AggregateGranulometricResponseDto create(AggregateGranulometricRequestDto dto);
    AggregateGranulometricResponseDto update(Long id, AggregateGranulometricRequestDto dto);
    AggregateGranulometricResponseDto getById(Long id);
    List<AggregateGranulometricResponseDto> getAll();
    void delete(Long id);
}
