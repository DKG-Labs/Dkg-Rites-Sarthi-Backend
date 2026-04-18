package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Cement.CementNormalConsistencyRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementNormalConsistencyResponseDto;
import java.util.List;

public interface CementNormalConsistencyService {
    CementNormalConsistencyResponseDto create(CementNormalConsistencyRequestDto dto);
    CementNormalConsistencyResponseDto update(Long id, CementNormalConsistencyRequestDto dto);
    CementNormalConsistencyResponseDto getById(Long id);
    List<CementNormalConsistencyResponseDto> getAll();
    List<CementNormalConsistencyResponseDto> getPeriodic();
    void delete(Long id);
    CementNormalConsistencyResponseDto getByRequestId(Long requestId);
}
