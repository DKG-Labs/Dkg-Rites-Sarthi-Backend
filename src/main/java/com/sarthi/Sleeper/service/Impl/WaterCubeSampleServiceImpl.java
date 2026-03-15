package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleResponseDto;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeSampleDeclaration;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeSampleDetail;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeSampleRepository;
import com.sarthi.Sleeper.service.WaterCubeSampleService;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaterCubeSampleServiceImpl implements WaterCubeSampleService {

    @Autowired
    private WaterCubeSampleRepository repository;

    @Override
    public WaterCubeSampleResponseDto create(WaterCubeSampleRequestDto dto) {
        WaterCubeSampleDeclaration entity = new WaterCubeSampleDeclaration();
        mapDtoToEntity(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy());
        repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Override
    public WaterCubeSampleResponseDto update(Long id, WaterCubeSampleRequestDto dto) {
        WaterCubeSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water Cube Sample Declaration not found"));
        mapDtoToEntity(dto, entity);
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());
        repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Override
    public WaterCubeSampleResponseDto getById(Long id) {
        WaterCubeSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water Cube Sample Declaration not found"));
        return mapEntityToDto(entity);
    }

    @Override
    public List<WaterCubeSampleResponseDto> getAll() {
        return repository.findAll().stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<WaterCubeSampleResponseDto> getByUser(Long userId) {
        return repository.findByCreatedBy(userId).stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(WaterCubeSampleRequestDto dto, WaterCubeSampleDeclaration entity) {
        entity.setProductionDeclarationId(dto.getProductionDeclarationId());
        entity.setCastingDate(CommonUtils.convertStringToDateObject(dto.getCastingDate()));
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setConcreteGrade(dto.getConcreteGrade());

        if (entity.getDetails() != null) {
            entity.getDetails().clear();
        } else {
            entity.setDetails(new ArrayList<>());
        }

        if (dto.getDetails() != null) {
            for (WaterCubeSampleDetailDto detailDto : dto.getDetails()) {
                WaterCubeSampleDetail detail = new WaterCubeSampleDetail();
                detail.setSampleNumber(detailDto.getSampleNumber());
                detail.setCubeNumber(detailDto.getCubeNumber());
                detail.setBenchNumber(detailDto.getBenchNumber());
                detail.setSequence(detailDto.getSequence());
                detail.setDeclaration(entity);
                entity.getDetails().add(detail);
            }
        }
    }

    private WaterCubeSampleResponseDto mapEntityToDto(WaterCubeSampleDeclaration entity) {
        WaterCubeSampleResponseDto response = new WaterCubeSampleResponseDto();
        response.setId(entity.getId());
        response.setProductionDeclarationId(entity.getProductionDeclarationId());
        response.setCastingDate(CommonUtils.convertDateToString(entity.getCastingDate()));
        response.setBatchNumber(entity.getBatchNumber());
        response.setShift(entity.getShift());
        response.setLineNo(entity.getLineNo());
        response.setConcreteGrade(entity.getConcreteGrade());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getDetails() != null) {
            List<WaterCubeSampleDetailDto> detailDtos = entity.getDetails().stream().map(detail -> {
                WaterCubeSampleDetailDto d = new WaterCubeSampleDetailDto();
                d.setSampleNumber(detail.getSampleNumber());
                d.setCubeNumber(detail.getCubeNumber());
                d.setBenchNumber(detail.getBenchNumber());
                d.setSequence(detail.getSequence());
                return d;
            }).collect(Collectors.toList());
            response.setDetails(detailDtos);
        }

        return response;
    }
}
