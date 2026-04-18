package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Cement.CementFinenessRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementFinenessResponseDto;
import java.util.List;

public interface CementFinenessService {
    CementFinenessResponseDto create(CementFinenessRequestDto dto);
    CementFinenessResponseDto update(Long id, CementFinenessRequestDto dto);
    CementFinenessResponseDto getById(Long id);
    List<CementFinenessResponseDto> getAll();
    List<CementFinenessResponseDto> getPeriodic();
    void delete(Long id);
    CementFinenessResponseDto getByRequestId(Long requestId);
}
