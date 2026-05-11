package com.sarthi.SRailPad.service.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ProductRecipeRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ProductRecipeResponseDto;

import java.util.List;

public interface RailProductRecipeService {
    ProductRecipeResponseDto create(ProductRecipeRequestDto dto);
    ProductRecipeResponseDto update(Long id, ProductRecipeRequestDto dto);
    ProductRecipeResponseDto getById(Long id);
    List<ProductRecipeResponseDto> getAllByVendorCode(String vendorCode);
    List<ProductRecipeResponseDto> getAllByPlantId(String plantId);
    void delete(Long id);
}
