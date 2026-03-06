package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.ProductionDeclaration.ProductionDeclarationRequestDto;
import com.sarthi.Sleeper.dto.ProductionDeclaration.ProductionDeclarationResponseDto;

import java.util.List;

public interface ProductionDeclarationService {

    ProductionDeclarationResponseDto create(ProductionDeclarationRequestDto dto);

    ProductionDeclarationResponseDto update(Long id, ProductionDeclarationRequestDto dto);

    ProductionDeclarationResponseDto getById(Long id);

    List<ProductionDeclarationResponseDto> getAll();

    void delete(Long id);
}
