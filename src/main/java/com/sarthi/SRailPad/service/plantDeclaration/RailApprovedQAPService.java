package com.sarthi.SRailPad.service.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedQAPRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedQAPResponseDto;

import java.util.List;

public interface RailApprovedQAPService {
    ApprovedQAPResponseDto create(ApprovedQAPRequestDto dto);
    ApprovedQAPResponseDto update(Long id, ApprovedQAPRequestDto dto);
    ApprovedQAPResponseDto getById(Long id);
    List<ApprovedQAPResponseDto> getAllByVendorCode(String vendorCode);
    List<ApprovedQAPResponseDto> getAllByPlantId(String plantId);
    void delete(Long id);
}
