package com.sarthi.SRailPad.service.plantDeclaration.Impl;

import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedQAPRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedQAPResponseDto;
import com.sarthi.SRailPad.entity.plantDeclaration.ApprovedQAP;
import com.sarthi.SRailPad.entity.plantDeclaration.QAPProductDetail;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.RailApprovedQAPRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.plantDeclaration.RailApprovedQAPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailApprovedQAPServiceImpl implements RailApprovedQAPService {

    @Autowired
    private RailApprovedQAPRepository repository;

    @Autowired
    private RailWorkflowTransactionRepository workflowTransactionRepository;

    @Autowired
    private RailWorkflowService railWorkflowService;

    private static final Long MODULE_ID = 6L;
    private static final Long WORKFLOW_ID = 1L;

    @Override
    @Transactional
    public ApprovedQAPResponseDto create(ApprovedQAPRequestDto dto) {
        ApprovedQAP entity = new ApprovedQAP();
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
    public ApprovedQAPResponseDto update(Long id, ApprovedQAPRequestDto dto) {
        ApprovedQAP entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("QAP record not found"));

        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        return buildResponse(entity);
    }

    private void mapDtoToEntity(ApprovedQAPRequestDto dto, ApprovedQAP entity) {
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setQapNo(dto.getQapNo());
        entity.setApprovingAuthority(dto.getApprovingAuthority());
        entity.setApprovalDate(dto.getApprovalDate());
        entity.setEffectiveDate(dto.getEffectiveDate());
        entity.setValidityDate(dto.getValidityDate());

        // Handle Product Details
        if (entity.getProductDetails() != null) {
            entity.getProductDetails().clear();
        }

        if (dto.getProductDetails() != null) {
            List<QAPProductDetail> details = dto.getProductDetails().stream().map(pDto -> {
                QAPProductDetail detail = new QAPProductDetail();
                detail.setApprovedQAP(entity);
                detail.setPadType(pDto.getPadType());
                detail.setMinMixingTime(pDto.getMinMixingTime());
                detail.setMaxMixingTime(pDto.getMaxMixingTime());
                detail.setMinMixingTemp(pDto.getMinMixingTemp());
                detail.setMaxMixingTemp(pDto.getMaxMixingTemp());
                detail.setMixingWeight(pDto.getMixingWeight());
                detail.setMinCuringTime(pDto.getMinCuringTime());
                detail.setMaxCuringTime(pDto.getMaxCuringTime());
                detail.setMinCuringTemp(pDto.getMinCuringTemp());
                detail.setMaxCuringTemp(pDto.getMaxCuringTemp());
                detail.setMinCuringPressure(pDto.getMinCuringPressure());
                detail.setMaxCuringPressure(pDto.getMaxCuringPressure());
                return detail;
            }).collect(Collectors.toList());
            
            if (entity.getProductDetails() == null) {
                entity.setProductDetails(details);
            } else {
                entity.getProductDetails().addAll(details);
            }
        }
    }

    @Override
    public ApprovedQAPResponseDto getById(Long id) {
        ApprovedQAP entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("QAP record not found"));
        return buildResponse(entity);
    }

    @Override
    public List<ApprovedQAPResponseDto> getAllByVendorCode(String vendorCode) {
        return repository.findAllByVendorCode(vendorCode).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApprovedQAPResponseDto> getAllByPlantId(String plantId) {
        return repository.findAllByPlantId(plantId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ApprovedQAPResponseDto buildResponse(ApprovedQAP entity) {
        ApprovedQAPResponseDto dto = new ApprovedQAPResponseDto();
        dto.setId(entity.getId());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());
        dto.setQapNo(entity.getQapNo());
        dto.setApprovingAuthority(entity.getApprovingAuthority());
        dto.setApprovalDate(entity.getApprovalDate());
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setValidityDate(entity.getValidityDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getProductDetails() != null) {
            dto.setProductDetails(entity.getProductDetails().stream().map(p -> {
                ApprovedQAPResponseDto.ProductDetailResponseDto pDto = new ApprovedQAPResponseDto.ProductDetailResponseDto();
                pDto.setId(p.getId());
                pDto.setPadType(p.getPadType());
                pDto.setMinMixingTime(p.getMinMixingTime());
                pDto.setMaxMixingTime(p.getMaxMixingTime());
                pDto.setMinMixingTemp(p.getMinMixingTemp());
                pDto.setMaxMixingTemp(p.getMaxMixingTemp());
                pDto.setMixingWeight(p.getMixingWeight());
                pDto.setMinCuringTime(p.getMinCuringTime());
                pDto.setMaxCuringTime(p.getMaxCuringTime());
                pDto.setMinCuringTemp(p.getMinCuringTemp());
                pDto.setMaxCuringTemp(p.getMaxCuringTemp());
                pDto.setMinCuringPressure(p.getMinCuringPressure());
                pDto.setMaxCuringPressure(p.getMaxCuringPressure());
                return pDto;
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
