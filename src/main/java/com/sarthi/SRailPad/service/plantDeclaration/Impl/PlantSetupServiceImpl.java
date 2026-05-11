package com.sarthi.SRailPad.service.plantDeclaration.Impl;

import com.sarthi.SRailPad.dto.plantDeclaration.*;
import com.sarthi.SRailPad.entity.plantDeclaration.PlantSetup;
import com.sarthi.SRailPad.entity.plantDeclaration.PlantUnit;
import com.sarthi.SRailPad.entity.plantDeclaration.UnitProduct;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.PlantSetupRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.plantDeclaration.PlantSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlantSetupServiceImpl implements PlantSetupService {

    @Autowired
    private PlantSetupRepository repository;

    @Autowired
    private RailWorkflowTransactionRepository workflowTransactionRepository;

    @Autowired
    private RailWorkflowService railWorkflowService;

    private static final Long MODULE_ID = 1L;
    private static final Long WORKFLOW_ID = 1L;

    @Override
    @Transactional
    public PlantSetupResponseDto create(PlantSetupRequestDto dto) {
        PlantSetup entity = new PlantSetup();
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
    public PlantSetupResponseDto update(Long id, PlantSetupRequestDto dto) {
        PlantSetup entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant Setup record not found"));

        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        
        return buildResponse(entity);
    }

    private void mapDtoToEntity(PlantSetupRequestDto dto, PlantSetup entity) {
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setNumberOfUnits(dto.getNumberOfUnits());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());

        // Handle Units
        if (entity.getUnits() != null) {
            entity.getUnits().clear();
        } else {
            entity.setUnits(new ArrayList<>());
        }

        if (dto.getUnits() != null) {
            for (PlantUnitRequestDto unitDto : dto.getUnits()) {
                PlantUnit unit = new PlantUnit();
                unit.setUnitName(unitDto.getUnitName());
                unit.setAddress(unitDto.getAddress());
                unit.setNumLines(unitDto.getNumLines());
                unit.setPlantSetup(entity);

                // Handle Products
                List<UnitProduct> products = new ArrayList<>();
                if (unitDto.getProducts() != null) {
                    for (UnitProductRequestDto prodDto : unitDto.getProducts()) {
                        UnitProduct product = new UnitProduct();
                        product.setProductName(prodDto.getProductName());
                        product.setApprovalNo(prodDto.getApprovalNo());
                        product.setApprovalDate(prodDto.getApprovalDate());
                        product.setCapacity(prodDto.getCapacity());
                        product.setPlantUnit(unit);
                        products.add(product);
                    }
                }
                unit.setProducts(products);
                entity.getUnits().add(unit);
            }
        }
    }

    @Override
    public PlantSetupResponseDto getById(Long id) {
        PlantSetup entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant Setup record not found"));
        return buildResponse(entity);
    }

    @Override
    public List<PlantSetupResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlantSetupResponseDto> getAllByVendorCode(String vendorCode) {
        return repository.findAllByVendorCode(vendorCode).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlantSetupResponseDto> getAllByPlantId(String plantId) {
        return repository.findAllByPlantId(plantId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private PlantSetupResponseDto buildResponse(PlantSetup entity) {
        PlantSetupResponseDto dto = new PlantSetupResponseDto();
        dto.setId(entity.getId());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorCode(entity.getVendorCode());
        dto.setNumberOfUnits(entity.getNumberOfUnits());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        // Map Units
        if (entity.getUnits() != null) {
            dto.setUnits(entity.getUnits().stream().map(unit -> {
                PlantUnitResponseDto uDto = new PlantUnitResponseDto();
                uDto.setId(unit.getId());
                uDto.setUnitName(unit.getUnitName());
                uDto.setAddress(unit.getAddress());
                uDto.setNumLines(unit.getNumLines());

                // Map Products
                if (unit.getProducts() != null) {
                    uDto.setProducts(unit.getProducts().stream().map(prod -> {
                        UnitProductResponseDto pDto = new UnitProductResponseDto();
                        pDto.setId(prod.getId());
                        pDto.setProductName(prod.getProductName());
                        pDto.setApprovalNo(prod.getApprovalNo());
                        pDto.setApprovalDate(prod.getApprovalDate());
                        pDto.setCapacity(prod.getCapacity());
                        return pDto;
                    }).collect(Collectors.toList()));
                }
                return uDto;
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
