package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalInspectionLotResultsRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalInspectionLotResultsResponseDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalInspectionSectionResultDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionSectionResult;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalInspectionLotResultsRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalInspectionLotResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalInspectionLotResultsServiceImpl implements RailFinalInspectionLotResultsService {

    @Autowired
    private RailFinalInspectionLotResultsRepository repository;

    @Override
    @Transactional
    public RailFinalInspectionLotResultsResponseDto save(RailFinalInspectionLotResultsRequestDto dto) {
        RailFinalInspectionLotResults entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalInspectionLotResults());

        boolean isNew = entity.getId() == null;
        mapDtoToEntity(dto, entity);

        if (isNew) {
            entity.setCreatedBy(dto.getUserId());
            entity.setCreatedDate(LocalDateTime.now());
        } else {
            entity.setUpdatedBy(dto.getUserId());
            entity.setUpdatedDate(LocalDateTime.now());
        }

        // Phase 1: clear children and flush to DB so orphan DELETEs happen before INSERTs
        if (entity.getSectionResults() != null) {
            entity.getSectionResults().clear();
        } else {
            entity.setSectionResults(new ArrayList<>());
        }
        entity = repository.saveAndFlush(entity);

        // Phase 2: add new section results now that old ones are deleted
        if (dto.getSectionResults() != null) {
            for (RailFinalInspectionSectionResultDto childDto : dto.getSectionResults()) {
                RailFinalInspectionSectionResult childEntity = new RailFinalInspectionSectionResult();
                childEntity.setSectionKey(childDto.getSectionKey());
                childEntity.setSectionName(childDto.getSectionName());
                childEntity.setSampleSize(childDto.getSampleSize());
                childEntity.setStatus(childDto.getStatus());
                childEntity.setLotResult(entity);
                entity.getSectionResults().add(childEntity);
            }
        }

        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    public RailFinalInspectionLotResultsResponseDto getById(Long id) {
        RailFinalInspectionLotResults entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Inspection Lot Results record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalInspectionLotResultsResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RailFinalInspectionLotResultsResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate dateOfInspection) {
        return repository.findAllByPlantIdAndShiftAndDateOfInspection(plantId, shift, dateOfInspection).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalInspectionLotResultsRequestDto dto, RailFinalInspectionLotResults entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setShift(dto.getShift());
        entity.setDateOfInspection(dto.getDateOfInspection());
        entity.setPlantId(dto.getPlantId());
        entity.setRlyPoSrNo(dto.getRlyPoSrNo());
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setRailpadType(dto.getRailpadType());
        entity.setLotNo(dto.getLotNo());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setAcceptedQty(dto.getAcceptedQty());
        entity.setRejectedQty(dto.getRejectedQty());
        entity.setVisualDimensionalStatus(dto.getVisualDimensionalStatus());
        entity.setPhysicalAgeingPropertiesStatus(dto.getPhysicalAgeingPropertiesStatus());
        entity.setElectricalChemicalStatus(dto.getElectricalChemicalStatus());
        entity.setDynamicDurabilityTestStatus(dto.getDynamicDurabilityTestStatus());
        entity.setNcrgrspStatus(dto.getNcrgrspStatus());
        entity.setOverallStatus(dto.getOverallStatus());
        entity.setHologram(dto.getHologram());
        entity.setRemarks(dto.getRemarks());
        // Note: sectionResults are managed separately in save() using two-phase flush
    }

    private RailFinalInspectionLotResultsResponseDto buildResponse(RailFinalInspectionLotResults entity) {
        RailFinalInspectionLotResultsResponseDto dto = new RailFinalInspectionLotResultsResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setShift(entity.getShift());
        dto.setDateOfInspection(entity.getDateOfInspection());
        dto.setPlantId(entity.getPlantId());
        dto.setRlyPoSrNo(entity.getRlyPoSrNo());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorCode(entity.getVendorCode());
        dto.setRailpadType(entity.getRailpadType());
        dto.setLotNo(entity.getLotNo());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setAcceptedQty(entity.getAcceptedQty());
        dto.setRejectedQty(entity.getRejectedQty());
        dto.setVisualDimensionalStatus(entity.getVisualDimensionalStatus());
        dto.setPhysicalAgeingPropertiesStatus(entity.getPhysicalAgeingPropertiesStatus());
        dto.setElectricalChemicalStatus(entity.getElectricalChemicalStatus());
        dto.setDynamicDurabilityTestStatus(entity.getDynamicDurabilityTestStatus());
        dto.setNcrgrspStatus(entity.getNcrgrspStatus());
        dto.setOverallStatus(entity.getOverallStatus());
        dto.setHologram(entity.getHologram());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getSectionResults() != null) {
            List<RailFinalInspectionSectionResultDto> childDtos = entity.getSectionResults().stream()
                    .map(childEntity -> {
                        RailFinalInspectionSectionResultDto childDto = new RailFinalInspectionSectionResultDto();
                        childDto.setId(childEntity.getId());
                        childDto.setSectionKey(childEntity.getSectionKey());
                        childDto.setSectionName(childEntity.getSectionName());
                        childDto.setSampleSize(childEntity.getSampleSize());
                        childDto.setStatus(childEntity.getStatus());
                        return childDto;
                    })
                    .collect(Collectors.toList());
            dto.setSectionResults(childDtos);
        }
        return dto;
    }
}
