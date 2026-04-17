package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.ModulusOfFailureRequestDto;
import com.sarthi.Sleeper.dto.ModulusOfFailureResponseDto;
import com.sarthi.Sleeper.entity.ModulusOfFailure;
import com.sarthi.Sleeper.repository.ModulusOfFailureRepository;
import com.sarthi.Sleeper.service.ModulusOfFailureService;
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
public class ModulusOfFailureServiceImpl implements ModulusOfFailureService {

    @Autowired
    private ModulusOfFailureRepository repository;

    // ================= CREATE =================

    @Override
    public ModulusOfFailureResponseDto create(ModulusOfFailureRequestDto dto) {

        ModulusOfFailure entity = new ModulusOfFailure();

        entity.setSamplingDate(LocalDate.parse(dto.getSamplingDate()));
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setPlantType(dto.getPlantType());
        entity.setShedLineNumber(dto.getShedLineNumber());
        entity.setBatchNo(dto.getBatchNo());
        entity.setCastingDate(LocalDate.parse(dto.getCastingDate()));
        entity.setBenchGangNumber(dto.getBenchGangNumber());
        entity.setMouldNo(dto.getMouldNo());

        entity.setShift(dto.getShift());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());

        // Auto generate Sample Identification
        entity.setSampleIdentification(
                dto.getShedLineNumber() + " + "
                        + dto.getBenchGangNumber() + " + "
                        + dto.getMouldNo()
        );

        entity.setMrResult(dto.getMrResult());
        entity.setSampleType(dto.getSampleType());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }


    // ================= UPDATE =================

    @Override
    public ModulusOfFailureResponseDto update(Long id, ModulusOfFailureRequestDto dto) {

        ModulusOfFailure entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Modulus Of Failure record not found")));

        entity.setSamplingDate(LocalDate.parse(dto.getSamplingDate()));
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setPlantType(dto.getPlantType());
        entity.setShedLineNumber(dto.getShedLineNumber());
        entity.setBatchNo(dto.getBatchNo());
        entity.setCastingDate(LocalDate.parse(dto.getCastingDate()));
        entity.setBenchGangNumber(dto.getBenchGangNumber());
        entity.setMouldNo(dto.getMouldNo());


        entity.setShift(dto.getShift());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());

        // regenerate identification
        entity.setSampleIdentification(
                dto.getShedLineNumber() + " + "
                        + dto.getBenchGangNumber() + " + "
                        + dto.getMouldNo()
        );

        entity.setMrResult(dto.getMrResult());
        entity.setSampleType(dto.getSampleType());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }


    // ================= GET BY ID =================

    @Override
    public ModulusOfFailureResponseDto getById(Long id) {

        ModulusOfFailure entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Modulus Of Failure record not found")));

        return buildResponse(entity);
    }


    // ================= GET ALL =================

 /*   @Override
    public List<ModulusOfFailureResponseDto> getAll() {

        List<ModulusOfFailureResponseDto> list = new ArrayList<>();

        for (ModulusOfFailure entity : repository.findAll()) {
            list.add(buildResponse(entity));
        }

        return list;
    }

  */

    @Override
    public List<ModulusOfFailureResponseDto> getAll() {

        List<ModulusOfFailureResponseDto> list = new ArrayList<>();

        for (ModulusOfFailure entity : repository.findAllNotTested()) {
            list.add(buildResponse(entity));
        }

        return list;
    }

    // ================= DELETE =================

    @Override
    public void delete(Long id) {

        ModulusOfFailure entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Modulus Of Failure record not found")));

        repository.deleteById(entity.getId());
    }


    // ================= RESPONSE BUILDER =================

    private ModulusOfFailureResponseDto buildResponse(ModulusOfFailure entity) {

        ModulusOfFailureResponseDto dto = new ModulusOfFailureResponseDto();

        dto.setId(entity.getId());
        dto.setSamplingDate(entity.getSamplingDate());
        dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setPlantType(entity.getPlantType());
        dto.setShedLineNumber(entity.getShedLineNumber());
        dto.setBatchNo(entity.getBatchNo());
        dto.setCastingDate(entity.getCastingDate());
        dto.setBenchGangNumber(entity.getBenchGangNumber());
        dto.setMouldNo(entity.getMouldNo());
        dto.setSampleIdentification(entity.getSampleIdentification());
        dto.setMrResult(entity.getMrResult());
        dto.setSampleType(entity.getSampleType());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setShift(entity.getShift());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());

        return dto;
    }
}