package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailRawMaterialWeighmentRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailRawMaterialWeighmentResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailRawMaterialWeighment;
import com.sarthi.SRailPad.entity.ieVerification.RailRawMaterialWeighmentItem;
import com.sarthi.SRailPad.repository.ieVerification.RailRawMaterialWeighmentRepository;
import com.sarthi.SRailPad.service.ieVerification.RailRawMaterialWeighmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailRawMaterialWeighmentServiceImpl implements RailRawMaterialWeighmentService {

    @Autowired
    private RailRawMaterialWeighmentRepository repository;

    @Override
    @Transactional
    public RailRawMaterialWeighmentResponseDto create(RailRawMaterialWeighmentRequestDto dto) {
        RailRawMaterialWeighment entity = new RailRawMaterialWeighment();
        mapDtoToEntity(dto, entity);
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());
        
        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    @Transactional
    public RailRawMaterialWeighmentResponseDto update(Long id, RailRawMaterialWeighmentRequestDto dto) {
        RailRawMaterialWeighment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raw Material Weighment record not found with id: " + id));
        
        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    public RailRawMaterialWeighmentResponseDto getById(Long id) {
        RailRawMaterialWeighment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raw Material Weighment record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public List<RailRawMaterialWeighmentResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate castingDate) {
        return repository.findAllByPlantIdAndShiftAndCastingDate(plantId, shift, castingDate).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailRawMaterialWeighmentRequestDto dto, RailRawMaterialWeighment entity) {
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setCastingDate(dto.getCastingDate());
        entity.setRailPadType(dto.getRailPadType());
        entity.setBatchNo(dto.getBatchNo());
        entity.setTotalWeight(dto.getTotalWeight());
        entity.setAcceptedMaterials(dto.getAcceptedMaterials());
        entity.setContractSpecification(dto.getContractSpecification());
        entity.setRubberPercentage(dto.getRubberPercentage());
        entity.setStatus(dto.getStatus());
        entity.setTimestamp(dto.getTimestamp());

        // Handle Materials
        if (entity.getMaterials() != null) {
            entity.getMaterials().clear();
        } else {
            entity.setMaterials(new ArrayList<>());
        }

        if (dto.getMaterials() != null) {
            for (RailRawMaterialWeighmentRequestDto.MaterialItemDto itemDto : dto.getMaterials()) {
                RailRawMaterialWeighmentItem item = new RailRawMaterialWeighmentItem();
                item.setName(itemDto.getName());
                item.setWeight(itemDto.getWeight());
                item.setWeighment(entity);
                entity.getMaterials().add(item);
            }
        }
    }

    private RailRawMaterialWeighmentResponseDto buildResponse(RailRawMaterialWeighment entity) {
        RailRawMaterialWeighmentResponseDto dto = new RailRawMaterialWeighmentResponseDto();
        dto.setId(entity.getId());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setCastingDate(entity.getCastingDate());
        dto.setRailPadType(entity.getRailPadType());
        dto.setBatchNo(entity.getBatchNo());
        dto.setTotalWeight(entity.getTotalWeight());
        dto.setAcceptedMaterials(entity.getAcceptedMaterials());
        dto.setContractSpecification(entity.getContractSpecification());
        dto.setRubberPercentage(entity.getRubberPercentage());
        dto.setStatus(entity.getStatus());
        dto.setTimestamp(entity.getTimestamp());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getMaterials() != null) {
            dto.setMaterials(entity.getMaterials().stream().map(item -> {
                RailRawMaterialWeighmentResponseDto.MaterialItemDto itemDto = new RailRawMaterialWeighmentResponseDto.MaterialItemDto();
                itemDto.setId(item.getId());
                itemDto.setName(item.getName());
                itemDto.setWeight(item.getWeight());
                return itemDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
