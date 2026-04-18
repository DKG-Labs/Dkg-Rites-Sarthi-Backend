package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Aggregates.AggregateSoundnessRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateSoundnessResponseDto;
import java.util.List;

public interface AggregateSoundnessService {
    AggregateSoundnessResponseDto create(AggregateSoundnessRequestDto dto);
    AggregateSoundnessResponseDto update(Long id, AggregateSoundnessRequestDto dto);
    AggregateSoundnessResponseDto getById(Long id);
    List<AggregateSoundnessResponseDto> getAll();
    List<AggregateSoundnessResponseDto> getPeriodic();
    void delete(Long id);
    AggregateSoundnessResponseDto getByRequestId(Long requestId);
}
