package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Cement.CementSpecificSurfaceRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementSpecificSurfaceResponseDto;
import java.util.List;

public interface CementSpecificSurfaceService {
    CementSpecificSurfaceResponseDto create(CementSpecificSurfaceRequestDto dto);
    CementSpecificSurfaceResponseDto update(Long id, CementSpecificSurfaceRequestDto dto);
    CementSpecificSurfaceResponseDto getById(Long id);
    List<CementSpecificSurfaceResponseDto> getAll();
    List<CementSpecificSurfaceResponseDto> getPeriodic();
    void delete(Long id);
    CementSpecificSurfaceResponseDto getByRequestId(Long requestId);
}
