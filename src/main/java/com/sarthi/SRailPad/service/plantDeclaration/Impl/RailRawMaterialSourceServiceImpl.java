package com.sarthi.SRailPad.service.plantDeclaration.Impl;

import com.sarthi.SRailPad.dto.plantDeclaration.RawMaterialSourceRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.RawMaterialSourceResponseDto;
import com.sarthi.SRailPad.entity.plantDeclaration.RawMaterialSource;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.RailRawMaterialSourceRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.plantDeclaration.RailRawMaterialSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailRawMaterialSourceServiceImpl implements RailRawMaterialSourceService {

    @Autowired
    private RailRawMaterialSourceRepository repository;

    @Autowired
    private RailWorkflowTransactionRepository workflowTransactionRepository;

    @Autowired
    private RailWorkflowService railWorkflowService;

    private static final Long MODULE_ID = 2L;
    private static final Long WORKFLOW_ID = 1L;

    @Override
    @Transactional
    public RawMaterialSourceResponseDto create(RawMaterialSourceRequestDto dto) {
        RawMaterialSource entity = new RawMaterialSource();
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
    public RawMaterialSourceResponseDto update(Long id, RawMaterialSourceRequestDto dto) {
        RawMaterialSource entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raw Material Source record not found"));

        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        
        return buildResponse(entity);
    }

    private void mapDtoToEntity(RawMaterialSourceRequestDto dto, RawMaterialSource entity) {
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setMaterialName(dto.getMaterialName());
        entity.setMaterialType(dto.getMaterialType());
        entity.setSupplierName(dto.getSupplierName());
        entity.setDocRefNo(dto.getDocRefNo());
        entity.setDocDate(dto.getDocDate());
    }

    @Override
    public RawMaterialSourceResponseDto getById(Long id) {
        RawMaterialSource entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raw Material Source record not found"));
        return buildResponse(entity);
    }

    @Override
    public List<RawMaterialSourceResponseDto> getAllByVendorCode(String vendorCode) {
        return repository.findAllByVendorCode(vendorCode).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RawMaterialSourceResponseDto> getAllByPlantId(String plantId) {
        return repository.findAllByPlantId(plantId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private RawMaterialSourceResponseDto buildResponse(RawMaterialSource entity) {
        RawMaterialSourceResponseDto dto = new RawMaterialSourceResponseDto();
        dto.setId(entity.getId());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());
        dto.setMaterialName(entity.getMaterialName());
        dto.setMaterialType(entity.getMaterialType());
        dto.setSupplierName(entity.getSupplierName());
        dto.setDocRefNo(entity.getDocRefNo());
        dto.setDocDate(entity.getDocDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        // Get Status from Workflow
        String status = workflowTransactionRepository
                .findLatestStatusByRequestIdAndModuleIdAndPlantId(String.valueOf(entity.getId()), MODULE_ID, entity.getPlantId())
                .orElse("NOT_STARTED");
        dto.setStatus(status);

        return dto;
    }
}
