package com.sarthi.SRailPad.service.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.RawMaterialSourceRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.RawMaterialSourceResponseDto;

import java.util.List;

public interface RailRawMaterialSourceService {
    RawMaterialSourceResponseDto create(RawMaterialSourceRequestDto dto);
    RawMaterialSourceResponseDto update(Long id, RawMaterialSourceRequestDto dto);
    RawMaterialSourceResponseDto getById(Long id);
    List<RawMaterialSourceResponseDto> getAllByVendorCode(String vendorCode);
    List<RawMaterialSourceResponseDto> getAllByPlantId(String plantId);
    void delete(Long id);
}
