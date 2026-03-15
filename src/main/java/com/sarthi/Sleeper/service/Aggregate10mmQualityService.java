package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Aggregates.Aggregate10mmQualityRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.Aggregate10mmQualityResponseDto;
import java.util.List;

public interface Aggregate10mmQualityService {
    Aggregate10mmQualityResponseDto create(Aggregate10mmQualityRequestDto dto);
    Aggregate10mmQualityResponseDto update(Long id, Aggregate10mmQualityRequestDto dto);
    Aggregate10mmQualityResponseDto getById(Long id);
    List<Aggregate10mmQualityResponseDto> getAll();
    void delete(Long id);
}
