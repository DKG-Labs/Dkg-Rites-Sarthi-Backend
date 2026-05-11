package com.sarthi.SRailPad.service.plantDeclaration.Impl;

import com.sarthi.SRailPad.dto.plantDeclaration.ProductRecipeRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ProductRecipeResponseDto;
import com.sarthi.SRailPad.entity.plantDeclaration.ProductRecipe;
import com.sarthi.SRailPad.entity.plantDeclaration.RecipeIngredient;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.RailProductRecipeRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.plantDeclaration.RailProductRecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailProductRecipeServiceImpl implements RailProductRecipeService {

    @Autowired
    private RailProductRecipeRepository repository;

    @Autowired
    private RailWorkflowTransactionRepository workflowTransactionRepository;

    @Autowired
    private RailWorkflowService railWorkflowService;

    private static final Long MODULE_ID = 4L;
    private static final Long WORKFLOW_ID = 1L;

    @Override
    @Transactional
    public ProductRecipeResponseDto create(ProductRecipeRequestDto dto) {
        ProductRecipe entity = new ProductRecipe();
        mapDtoToEntity(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy());

        repository.save(entity);

        // Trigger Workflow
        railWorkflowService.initiateWorkflow(
                String.valueOf(entity.getId()),
                MODULE_ID,
                WORKFLOW_ID,
                dto.getCreatedBy(),
                dto.getVendorCode(),
                dto.getPlantId(),
                dto.getShift()
        );

        return buildResponse(entity);
    }

    @Override
    @Transactional
    public ProductRecipeResponseDto update(Long id, ProductRecipeRequestDto dto) {
        ProductRecipe entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Recipe record not found"));

        // Clear existing ingredients
        entity.getIngredients().clear();
        
        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        
        return buildResponse(entity);
    }

    private void mapDtoToEntity(ProductRecipeRequestDto dto, ProductRecipe entity) {
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setRecipeIdentification(dto.getRecipeIdentification());
        entity.setPadType(dto.getPadType());
        entity.setTotalPercentage(dto.getTotalPercentage());
        entity.setVirginTotalPercentage(dto.getVirginTotalPercentage());

        if (dto.getIngredients() != null) {
            for (ProductRecipeRequestDto.IngredientDto ingDto : dto.getIngredients()) {
                RecipeIngredient ingredient = new RecipeIngredient();
                ingredient.setRawMaterial(ingDto.getRawMaterial());
                ingredient.setPercentage(ingDto.getPercentage());
                entity.addIngredient(ingredient);
            }
        }
    }

    @Override
    public ProductRecipeResponseDto getById(Long id) {
        ProductRecipe entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Recipe record not found"));
        return buildResponse(entity);
    }

    @Override
    public List<ProductRecipeResponseDto> getAllByVendorCode(String vendorCode) {
        return repository.findAllByVendorCode(vendorCode).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductRecipeResponseDto> getAllByPlantId(String plantId) {
        return repository.findAllByPlantId(plantId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ProductRecipeResponseDto buildResponse(ProductRecipe entity) {
        ProductRecipeResponseDto dto = new ProductRecipeResponseDto();
        dto.setId(entity.getId());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());
        dto.setRecipeIdentification(entity.getRecipeIdentification());
        dto.setPadType(entity.getPadType());
        dto.setTotalPercentage(entity.getTotalPercentage());
        dto.setVirginTotalPercentage(entity.getVirginTotalPercentage());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getIngredients() != null) {
            dto.setIngredients(entity.getIngredients().stream().map(ing -> {
                ProductRecipeResponseDto.IngredientDto ingDto = new ProductRecipeResponseDto.IngredientDto();
                ingDto.setId(ing.getId());
                ingDto.setRawMaterial(ing.getRawMaterial());
                ingDto.setPercentage(ing.getPercentage());
                return ingDto;
            }).collect(Collectors.toList()));
        }

        // Get Status from Workflow
        String status = workflowTransactionRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), MODULE_ID)
                .orElse("NOT_STARTED");
        dto.setStatus(status);

        return dto;
    }
}
