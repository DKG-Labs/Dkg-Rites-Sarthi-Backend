package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MorTestRequestDto;
import com.sarthi.Sleeper.dto.MorTestResponseDto;
import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;
import com.sarthi.Sleeper.entity.MorTestResult;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.MorSampleRepository;
import com.sarthi.Sleeper.repository.MorTestRepository;
import com.sarthi.Sleeper.service.MorTestService;
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
public class MorTestServiceImpl implements MorTestService {

    @Autowired
    private MorTestRepository repository;

    @Autowired
    private MorSampleRepository morSampleRepository;

    // ================= CREATE =================

    @Override
    public MorTestResponseDto create(MorTestRequestDto dto) {

        MorTestResult entity = new MorTestResult();

        entity.setTestingDate(LocalDate.parse(dto.getTestingDate()));
        entity.setWeight(dto.getWeight());
        entity.setLoadKn(dto.getLoadKn());
        entity.setStrength(dto.getStrength());
        entity.setRemarks(dto.getRemarks());

        entity.setSampleIdentificationNumber(dto.getSampleIdentificationNumber());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setSamplingDate(dto.getSamplingDate());

        entity.setShift(dto.getShift());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());


        MorSampleDeclaration sample =
                morSampleRepository.findById(dto.getMorSampleId()).orElseThrow();

        entity.setMorSample(sample);

        // AUTO RESULT CALCULATION
        if(dto.getStrength() >= 60){
            entity.setResult("PASS");
        }else{
            entity.setResult("FAIL");
        }

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }


    // ================= UPDATE =================

    @Override
    public MorTestResponseDto update(Long id, MorTestRequestDto dto) {

        MorTestResult entity = repository.findById(id) .orElseThrow(() -> new BusinessException(
                new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "MOR Sample not found")));

        entity.setTestingDate(LocalDate.parse(dto.getTestingDate()));
        entity.setWeight(dto.getWeight());
        entity.setLoadKn(dto.getLoadKn());
        entity.setStrength(dto.getStrength());
        entity.setRemarks(dto.getRemarks());


        entity.setSampleIdentificationNumber(dto.getSampleIdentificationNumber());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setSamplingDate(dto.getSamplingDate());

        entity.setShift(dto.getShift());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());

        if(dto.getStrength() >= 60){
            entity.setResult("PASS");
        }else{
            entity.setResult("FAIL");
        }

        entity.setUpdatedBy(dto.getCreatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }


    // ================= GET BY ID =================

    @Override
    public MorTestResponseDto getById(Long id) {

        MorTestResult entity = repository.findById(id) .orElseThrow(() -> new BusinessException(
                new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "MOR Sample not found")));

        return buildResponse(entity);
    }


    // ================= GET ALL =================

    @Override
    public List<MorTestResponseDto> getAll() {

        List<MorTestResponseDto> list = new ArrayList<>();

        for(MorTestResult entity : repository.findAll()){
            list.add(buildResponse(entity));
        }

        return list;
    }


    // ================= DELETE =================

    @Override
    public void delete(Long id) {

        MorTestResult entity = repository.findById(id).orElseThrow();

        repository.deleteById(entity.getId());
    }


    // ================= RESPONSE BUILDER =================

    private MorTestResponseDto buildResponse(MorTestResult entity){

        MorTestResponseDto dto = new MorTestResponseDto();

        dto.setId(entity.getId());
        dto.setMorSampleId(entity.getMorSample().getId());
        dto.setTestingDate(entity.getTestingDate());
        dto.setWeight(entity.getWeight());
        dto.setLoadKn(entity.getLoadKn());
        dto.setStrength(entity.getStrength());
        dto.setResult(entity.getResult());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedBy(entity.getCreatedBy());


       dto.setSampleIdentificationNumber(entity.getSampleIdentificationNumber());
       dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setSamplingDate(entity.getSamplingDate());

        dto.setShift(entity.getShift());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());

        return dto;
    }
}