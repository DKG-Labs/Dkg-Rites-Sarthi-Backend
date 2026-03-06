package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.ProductionDeclaration.PProductionDeclarationRequestDto;

import java.util.List;

public interface ProductionDeclarationService {

    PProductionDeclarationRequestDto create(
            ProductionDeclarationRequestDto dto);

    PProductionDeclarationRequestDto update(
            Long id,
            ProductionDeclarationRequestDto dto);

    PProductionDeclarationRequestDto getById(Long id);

    List<PProductionDeclarationRequestDto> getAll();

    void delete(Long id);
}
