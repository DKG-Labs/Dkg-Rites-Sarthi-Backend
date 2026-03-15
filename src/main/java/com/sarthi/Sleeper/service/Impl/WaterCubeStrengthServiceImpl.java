package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthResponseDto;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthDetail;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeStrengthRepository;
import com.sarthi.Sleeper.service.WaterCubeStrengthService;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaterCubeStrengthServiceImpl implements WaterCubeStrengthService {

    @Autowired
    private WaterCubeStrengthRepository repository;

    @Override
    public WaterCubeStrengthResponseDto create(WaterCubeStrengthRequestDto dto) {
        WaterCubeStrengthTest entity = new WaterCubeStrengthTest();
        mapDtoToEntity(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy());
        repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Override
    public WaterCubeStrengthResponseDto update(Long id, WaterCubeStrengthRequestDto dto) {
        WaterCubeStrengthTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water Cube Strength Test not found"));
        mapDtoToEntity(dto, entity);
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());
        repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Override
    public WaterCubeStrengthResponseDto getById(Long id) {
        WaterCubeStrengthTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water Cube Strength Test not found"));
        return mapEntityToDto(entity);
    }

    @Override
    public List<WaterCubeStrengthResponseDto> getAll() {
        return repository.findAll().stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<WaterCubeStrengthResponseDto> getByUser(Long userId) {
        return repository.findByCreatedBy(userId).stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    public WaterCubeStrengthResponseDto getByDeclaration(Long declarationId) {
        return repository.findByWaterCubeSampleDeclarationId(declarationId)
                .map(this::mapEntityToDto)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(WaterCubeStrengthRequestDto dto, WaterCubeStrengthTest entity) {
        entity.setWaterCubeSampleDeclarationId(dto.getWaterCubeSampleDeclarationId());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setCastingDate(dto.getCastingDate());
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setFckTarget(dto.getFckTarget());
        entity.setAgeDays(dto.getAgeDays());
        entity.setS1Avg(dto.getS1Avg());
        entity.setS2Avg(dto.getS2Avg());
        entity.setAvgX(dto.getAvgX());
        entity.setMinY(dto.getMinY());
        entity.setS1Variation(dto.getS1Variation());
        entity.setS2Variation(dto.getS2Variation());
        entity.setCondition1(dto.getCondition1());
        entity.setCondition2(dto.getCondition2());
        entity.setCondition3(dto.getCondition3());
        entity.setMrSamplesRequired(dto.getMrSamplesRequired());
        entity.setFinalTestResult(dto.getFinalTestResult());

        if (entity.getDetails() != null) {
            entity.getDetails().clear();
        } else {
            entity.setDetails(new ArrayList<>());
        }

        if (dto.getDetails() != null) {
            for (WaterCubeStrengthDetailDto detailDto : dto.getDetails()) {
                WaterCubeStrengthDetail detail = new WaterCubeStrengthDetail();
                detail.setSampleNumber(detailDto.getSampleNumber());
                detail.setCubeIndex(detailDto.getCubeIndex());
                detail.setCubeId(detailDto.getCubeId());
                detail.setWeightKg(detailDto.getWeightKg());
                detail.setLoadKn(detailDto.getLoadKn());
                detail.setStrengthNmm2(detailDto.getStrengthNmm2());
                detail.setTestingDate(CommonUtils.convertStringToDateObject(detailDto.getTestingDate()));
                detail.setTestingTime(CommonUtils.convertStringToTimeObject(detailDto.getTestingTime()));
                detail.setStrengthTest(entity);
                entity.getDetails().add(detail);
            }
        }
    }

    private WaterCubeStrengthResponseDto mapEntityToDto(WaterCubeStrengthTest entity) {
        WaterCubeStrengthResponseDto response = new WaterCubeStrengthResponseDto();
        response.setId(entity.getId());
        response.setWaterCubeSampleDeclarationId(entity.getWaterCubeSampleDeclarationId());
        response.setBatchNumber(entity.getBatchNumber());
        response.setConcreteGrade(entity.getConcreteGrade());
        response.setCastingDate(entity.getCastingDate());
        response.setShift(entity.getShift());
        response.setLineNo(entity.getLineNo());
        response.setFckTarget(entity.getFckTarget());
        response.setAgeDays(entity.getAgeDays());
        response.setS1Avg(entity.getS1Avg());
        response.setS2Avg(entity.getS2Avg());
        response.setAvgX(entity.getAvgX());
        response.setMinY(entity.getMinY());
        response.setS1Variation(entity.getS1Variation());
        response.setS2Variation(entity.getS2Variation());
        response.setCondition1(entity.getCondition1());
        response.setCondition2(entity.getCondition2());
        response.setCondition3(entity.getCondition3());
        response.setMrSamplesRequired(entity.getMrSamplesRequired());
        response.setFinalTestResult(entity.getFinalTestResult());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());

        if (entity.getDetails() != null) {
            List<WaterCubeStrengthDetailDto> detailDtos = entity.getDetails().stream().map(detail -> {
                WaterCubeStrengthDetailDto d = new WaterCubeStrengthDetailDto();
                d.setSampleNumber(detail.getSampleNumber());
                d.setCubeIndex(detail.getCubeIndex());
                d.setCubeId(detail.getCubeId());
                d.setWeightKg(detail.getWeightKg());
                d.setLoadKn(detail.getLoadKn());
                d.setStrengthNmm2(detail.getStrengthNmm2());
                d.setTestingDate(CommonUtils.convertDateToString(detail.getTestingDate()));
                d.setTestingTime(CommonUtils.convertTimeToString(detail.getTestingTime()));
                return d;
            }).collect(Collectors.toList());
            response.setDetails(detailDtos);
        }

        return response;
    }
}
