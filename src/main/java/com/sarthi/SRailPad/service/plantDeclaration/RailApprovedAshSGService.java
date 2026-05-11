package com.sarthi.SRailPad.service.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGResponseDto;

import java.util.List;

public interface RailApprovedAshSGService {
    ApprovedAshSGResponseDto create(ApprovedAshSGRequestDto dto);
    ApprovedAshSGResponseDto update(Long id, ApprovedAshSGRequestDto dto);
    ApprovedAshSGResponseDto getById(Long id);
    List<ApprovedAshSGResponseDto> getAllByVendorCode(String vendorCode);
    List<ApprovedAshSGResponseDto> getAllByPlantId(String plantId);
    void delete(Long id);
}
