package com.sarthi.SRailPad.service.plantDeclaration.Impl;

import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGResponseDto;
import com.sarthi.SRailPad.entity.plantDeclaration.ApprovedAshSG;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.RailApprovedAshSGRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.plantDeclaration.RailApprovedAshSGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailApprovedAshSGServiceImpl implements RailApprovedAshSGService {

    @Autowired
    private RailApprovedAshSGRepository repository;

    @Autowired
    private RailWorkflowTransactionRepository workflowTransactionRepository;

    @Autowired
    private RailWorkflowService railWorkflowService;

    private static final Long MODULE_ID = 5L;
    private static final Long WORKFLOW_ID = 1L;

    @Override
    @Transactional
    public ApprovedAshSGResponseDto create(ApprovedAshSGRequestDto dto) {
        ApprovedAshSG entity = new ApprovedAshSG();
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
    public ApprovedAshSGResponseDto update(Long id, ApprovedAshSGRequestDto dto) {
        ApprovedAshSG entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approved Ash & SG record not found"));

        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        return buildResponse(entity);
    }

    private void mapDtoToEntity(ApprovedAshSGRequestDto dto, ApprovedAshSG entity) {
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setPadType(dto.getPadType());
        entity.setAshContentA(dto.getAshContentA());
        entity.setSpecificGravityA(dto.getSpecificGravityA());
        entity.setAshContentB(dto.getAshContentB());
        entity.setSpecificGravityB(dto.getSpecificGravityB());
        entity.setApprovalRefNo(dto.getApprovalRefNo());
        entity.setApprovalDate(dto.getApprovalDate());
    }

    @Override
    public ApprovedAshSGResponseDto getById(Long id) {
        ApprovedAshSG entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approved Ash & SG record not found"));
        return buildResponse(entity);
    }

    @Override
    public List<ApprovedAshSGResponseDto> getAllByVendorCode(String vendorCode) {
        return repository.findAllByVendorCode(vendorCode).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApprovedAshSGResponseDto> getAllByPlantId(String plantId) {
        return repository.findAllByPlantId(plantId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ApprovedAshSGResponseDto buildResponse(ApprovedAshSG entity) {
        ApprovedAshSGResponseDto dto = new ApprovedAshSGResponseDto();
        dto.setId(entity.getId());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());
        dto.setPadType(entity.getPadType());
        dto.setAshContentA(entity.getAshContentA());
        dto.setSpecificGravityA(entity.getSpecificGravityA());
        dto.setAshContentB(entity.getAshContentB());
        dto.setSpecificGravityB(entity.getSpecificGravityB());
        dto.setApprovalRefNo(entity.getApprovalRefNo());
        dto.setApprovalDate(entity.getApprovalDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        // Get Status from Workflow
        String status = workflowTransactionRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), MODULE_ID)
                .orElse("NOT_STARTED");
        dto.setStatus(status);

        return dto;
    }
}
