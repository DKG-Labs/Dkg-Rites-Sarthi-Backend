package com.sarthi.SRailPad.service.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.PlantSetupRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.PlantSetupResponseDto;

import java.util.List;

public interface PlantSetupService {
    PlantSetupResponseDto create(PlantSetupRequestDto dto);
    PlantSetupResponseDto update(Long id, PlantSetupRequestDto dto);
    PlantSetupResponseDto getById(Long id);
    List<PlantSetupResponseDto> getAll();
    List<PlantSetupResponseDto> getAllByVendorCode(String vendorCode);
    List<PlantSetupResponseDto> getAllByPlantId(String plantId);
    void delete(Long id);
}
