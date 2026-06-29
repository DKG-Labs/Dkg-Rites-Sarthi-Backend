package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.RawMaterialConsumptionDto;
import java.util.List;

import org.springframework.data.domain.Page;

public interface RawMaterialConsumptionService {
    RawMaterialConsumptionDto saveConsumption(RawMaterialConsumptionDto dto);
    RawMaterialConsumptionDto getConsumptionById(Long id);
    Page<RawMaterialConsumptionDto> getAllConsumptionByPlantAndMaterial(String plantId, String rawMaterial, List<String> statuses, int page, int size);
    List<RawMaterialConsumptionDto> getAllVerifiedConsumptionByPlantAndMaterial(String plantId, String rawMaterial);
    Page<RawMaterialConsumptionDto> getAllConsumptionByPlant(String plantId, int page, int size);
    void deleteConsumption(Long id);
}
