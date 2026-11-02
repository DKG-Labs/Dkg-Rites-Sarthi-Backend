package com.sarthi.service;

import com.sarthi.dto.NearestPlantResponseDto;

public interface NearestPlantService {
    NearestPlantResponseDto findNearestPlant(String userId);
}
