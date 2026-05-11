package com.sarthi.SRailPad.service.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ProductionDeclarationRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ProductionDeclarationResponseDto;
import java.util.List;

public interface RailProductionDeclarationService {
    ProductionDeclarationResponseDto create(ProductionDeclarationRequestDto requestDto);
    ProductionDeclarationResponseDto update(Long id, ProductionDeclarationRequestDto requestDto);
    List<ProductionDeclarationResponseDto> getAllByPlant(String plantId);
    ProductionDeclarationResponseDto getById(Long id);
    void delete(Long id);
}
