package com.sarthi.SRailPad.service.plantDeclaration.Impl;

import com.sarthi.SRailPad.dto.plantDeclaration.ProductionDeclarationRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ProductionDeclarationResponseDto;
import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionBatch;
import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionProduct;
import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionDeclaration;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.RailProductionDeclarationRepository;
import com.sarthi.SRailPad.service.plantDeclaration.RailProductionDeclarationService;
import com.sarthi.SRailPad.service.RailWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RailProductionDeclarationServiceImpl implements RailProductionDeclarationService {

    private final RailProductionDeclarationRepository repository;
    private final RailWorkflowService railWorkflowService;
    private final RailWorkflowTransactionRepository workflowTransactionRepository;

    private static final Long MODULE_ID = 3L;
    private static final Long WORKFLOW_ID = 1L;

    @Override
    @Transactional
    public ProductionDeclarationResponseDto create(ProductionDeclarationRequestDto requestDto) {
        RailProductionDeclaration entity = new RailProductionDeclaration();
        BeanUtils.copyProperties(requestDto, entity, "products");
        
        entity.setStatus("PENDING");
        entity.setCreatedBy(requestDto.getCreatedBy());
        entity.setUpdatedBy(requestDto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());

        if (requestDto.getProducts() != null) {
            List<RailProductionProduct> productList = new ArrayList<>();
            for (ProductionDeclarationRequestDto.ProductDto pDto : requestDto.getProducts()) {
                RailProductionProduct product = new RailProductionProduct();
                product.setProductType(pDto.getProductType());
                product.setMeasurementMode(pDto.getMeasurementMode());
                product.setDeclaration(entity);

                if (pDto.getBatches() != null) {
                    List<RailProductionBatch> batchList = pDto.getBatches().stream().map(bDto -> {
                        RailProductionBatch batch = new RailProductionBatch();
                        BeanUtils.copyProperties(bDto, batch);
                        batch.setProduct(product);
                        return batch;
                    }).collect(Collectors.toList());
                    product.setBatches(batchList);
                }
                productList.add(product);
            }
            entity.setProducts(productList);
        }

        RailProductionDeclaration saved = repository.save(entity);
        
        // Trigger Workflow
        railWorkflowService.initiateWorkflow(
                String.valueOf(saved.getId()),
                MODULE_ID,
                WORKFLOW_ID,
                requestDto.getCreatedBy(),
                requestDto.getVendorCode(),
                requestDto.getPlantId(),
                requestDto.getShift()
        );

        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public ProductionDeclarationResponseDto update(Long id, ProductionDeclarationRequestDto requestDto) {
        RailProductionDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production Declaration not found"));

        BeanUtils.copyProperties(requestDto, entity, "id", "products", "createdDate", "createdBy");
        entity.setUpdatedBy(requestDto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        // Update Products (Parent-Child)
        entity.getProducts().clear();
        if (requestDto.getProducts() != null) {
            for (ProductionDeclarationRequestDto.ProductDto pDto : requestDto.getProducts()) {
                RailProductionProduct product = new RailProductionProduct();
                product.setProductType(pDto.getProductType());
                product.setMeasurementMode(pDto.getMeasurementMode());
                product.setDeclaration(entity);

                if (pDto.getBatches() != null) {
                    List<RailProductionBatch> batchList = pDto.getBatches().stream().map(bDto -> {
                        RailProductionBatch batch = new RailProductionBatch();
                        BeanUtils.copyProperties(bDto, batch);
                        batch.setProduct(product);
                        return batch;
                    }).collect(Collectors.toList());
                    product.setBatches(batchList);
                }
                entity.getProducts().add(product);
            }
        }

        RailProductionDeclaration saved = repository.save(entity);
        return mapToResponseDto(saved);
    }

    @Override
    public List<ProductionDeclarationResponseDto> getAllByPlant(String plantId) {
        return repository.findByPlantIdOrderByProductionDateDesc(plantId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductionDeclarationResponseDto getById(Long id) {
        RailProductionDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production Declaration not found"));
        return mapToResponseDto(entity);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ProductionDeclarationResponseDto mapToResponseDto(RailProductionDeclaration entity) {
        ProductionDeclarationResponseDto dto = new ProductionDeclarationResponseDto();
        BeanUtils.copyProperties(entity, dto, "products");

        if (entity.getProducts() != null) {
            List<ProductionDeclarationResponseDto.ProductResponseDto> pDtos = entity.getProducts().stream().map(p -> {
                ProductionDeclarationResponseDto.ProductResponseDto pd = new ProductionDeclarationResponseDto.ProductResponseDto();
                pd.setId(p.getId());
                pd.setProductType(p.getProductType());
                pd.setMeasurementMode(p.getMeasurementMode());

                if (p.getBatches() != null) {
                    List<ProductionDeclarationResponseDto.BatchResponseDto> bDtos = p.getBatches().stream().map(b -> {
                        ProductionDeclarationResponseDto.BatchResponseDto bd = new ProductionDeclarationResponseDto.BatchResponseDto();
                        BeanUtils.copyProperties(b, bd);
                        return bd;
                    }).collect(Collectors.toList());
                    pd.setBatches(bDtos);
                }
                return pd;
            }).collect(Collectors.toList());
            dto.setProducts(pDtos);
        }

        // Get status from workflow
        String status = workflowTransactionRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), MODULE_ID)
                .orElse(entity.getStatus());
        dto.setStatus(status);
        
        return dto;
    }
}
