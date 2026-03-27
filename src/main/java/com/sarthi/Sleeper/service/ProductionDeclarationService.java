package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.BenchDetailsResponseDto;
import com.sarthi.Sleeper.dto.ProductionDeclaration.ProductionDeclarationRequestDto;
import com.sarthi.Sleeper.dto.ProductionDeclaration.ProductionDeclarationResponseDto;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionBenchGroup;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionSleeper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductionDeclarationService {

    ProductionDeclarationResponseDto create(ProductionDeclarationRequestDto dto);

    ProductionDeclarationResponseDto update(Long id, ProductionDeclarationRequestDto dto);

    ProductionDeclarationResponseDto getById(Long id);

    List<ProductionDeclarationResponseDto> getAll();

    List<ProductionDeclarationResponseDto> getByUser(Long userId);

    void delete(Long id);

    List<String> getVerifiedProductionDeclarations();

    public List<String> getBatchNumbers(Long vendorId, LocalDate castingDate);

    public List<String> getBenchNumbers(String batchNo);

    public List<String> getSleeperTypes(String batchNo, Integer benchNo);
    public List<String> getSleepers(String batchNo, Integer benchNo, String sleeperType);
}
