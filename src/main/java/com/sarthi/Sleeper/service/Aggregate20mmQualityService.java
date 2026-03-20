package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Aggregates.Aggregate20mmQualityRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.Aggregate20mmQualityResponseDto;
import java.util.List;

public interface Aggregate20mmQualityService {
    Aggregate20mmQualityResponseDto create(Aggregate20mmQualityRequestDto dto);
    Aggregate20mmQualityResponseDto update(Long id, Aggregate20mmQualityRequestDto dto);
    Aggregate20mmQualityResponseDto getById(Long id);
    List<Aggregate20mmQualityResponseDto> getAll();
    void delete(Long id);
    Aggregate20mmQualityResponseDto getByRequestId(Long requestId);
}
