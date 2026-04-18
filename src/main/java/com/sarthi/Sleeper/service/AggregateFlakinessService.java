package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Aggregates.AggregateFlakinessRequestDto;
import com.sarthi.Sleeper.dto.Aggregates.AggregateFlakinessResponseDto;
import java.util.List;

public interface AggregateFlakinessService {
    AggregateFlakinessResponseDto create(AggregateFlakinessRequestDto dto);
    AggregateFlakinessResponseDto update(Long id, AggregateFlakinessRequestDto dto);
    AggregateFlakinessResponseDto getById(Long id);
    List<AggregateFlakinessResponseDto> getAll();
    List<AggregateFlakinessResponseDto> getPeriodic();
    void delete(Long id);
    AggregateFlakinessResponseDto getByRequestId(Long requestId);
}
