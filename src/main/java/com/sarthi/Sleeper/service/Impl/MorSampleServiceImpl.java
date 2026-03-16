package com.sarthi.Sleeper.service.Impl;



import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleResponseDto;
import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.MorSampleRepository;

import com.sarthi.Sleeper.service.MorSampleService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MorSampleServiceImpl implements MorSampleService {

    @Autowired
    private MorSampleRepository repository;

    // ================= CREATE =================
    @Override
    public MorSampleResponseDto create(MorSampleRequestDto dto) {

        MorSampleDeclaration entity = new MorSampleDeclaration();

        entity.setSamplingDate(LocalDate.parse(dto.getSamplingDate()));
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setPlantType(dto.getPlantType());
        entity.setShedLine(dto.getShedLine());
        entity.setSampleIdentificationNumber(dto.getSampleIdentificationNumber());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= UPDATE =================
    @Override
    public MorSampleResponseDto update(Long id, MorSampleRequestDto dto) {

        MorSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MOR Sample not found")));

        entity.setSamplingDate(LocalDate.parse(dto.getSamplingDate()));
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setPlantType(dto.getPlantType());
        entity.setShedLine(dto.getShedLine());
        entity.setSampleIdentificationNumber(dto.getSampleIdentificationNumber());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= GET BY ID =================
    @Override
    public MorSampleResponseDto getById(Long id) {

        MorSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MOR Sample not found")));

        return buildResponse(entity);
    }

    // ================= GET ALL =================
    @Override
    public List<MorSampleResponseDto> getAll() {

        List<MorSampleResponseDto> list = new ArrayList<>();

        for (MorSampleDeclaration entity : repository.findAll()) {
            list.add(buildResponse(entity));
        }

        return list;
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {

        MorSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MOR Sample not found")));

        repository.deleteById(entity.getId());
    }

    // ================= RESPONSE BUILDER =================
    private MorSampleResponseDto buildResponse(MorSampleDeclaration entity) {

        MorSampleResponseDto dto = new MorSampleResponseDto();

        dto.setId(entity.getId());
        dto.setSamplingDate(String.valueOf(entity.getSamplingDate()));
        dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setPlantType(entity.getPlantType());
        dto.setShedLine(entity.getShedLine());
        dto.setSampleIdentificationNumber(entity.getSampleIdentificationNumber());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    }
}