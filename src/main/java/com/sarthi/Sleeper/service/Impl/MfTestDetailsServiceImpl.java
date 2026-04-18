package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MfTestDetailsRequestDto;
import com.sarthi.Sleeper.dto.MfTestDetailsResponseDto;
import com.sarthi.Sleeper.entity.MfTestDetails;
import com.sarthi.Sleeper.entity.ModulusOfFailure;
import com.sarthi.Sleeper.repository.MfTestDetailsRepository;
import com.sarthi.Sleeper.repository.ModulusOfFailureRepository;
import com.sarthi.Sleeper.service.MfTestDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MfTestDetailsServiceImpl implements MfTestDetailsService {

    @Autowired
    private MfTestDetailsRepository repository;

    @Autowired
    private ModulusOfFailureRepository mofRepository;

    // ================= CREATE =================

    @Override
    public MfTestDetailsResponseDto create(MfTestDetailsRequestDto dto) {

        MfTestDetails entity = new MfTestDetails();

        entity.setTestingDate(LocalDate.parse(dto.getTestingDate()));
        entity.setStrength(dto.getStrength());

        entity.setFinalStrength(dto.getStrength());

        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setSampleIdentification(dto.getSampleIdentification());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setBatchNo(dto.getBatchNo());
        entity.setCastingDate(dto.getCastingDate());


        // Result Auto Calculation
        if(dto.getStrength() >= 4500){
            entity.setResult("PASS");
        } else {
            entity.setResult("FAIL");
        }

        entity.setRemarks(dto.getRemarks());

        ModulusOfFailure mof =
                mofRepository.findById(dto.getModulusOfFailureId())
                        .orElseThrow();

        entity.setModulusOfFailure(mof);

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }


    // ================= UPDATE =================

    @Override
    public MfTestDetailsResponseDto update(Long id, MfTestDetailsRequestDto dto) {

        MfTestDetails entity = repository.findById(id).orElseThrow();

        entity.setTestingDate(LocalDate.parse(dto.getTestingDate()));
        entity.setStrength(dto.getStrength());

        entity.setFinalStrength(dto.getStrength());


        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());

        entity.setSampleIdentification(dto.getSampleIdentification());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setBatchNo(dto.getBatchNo());
        entity.setCastingDate(dto.getCastingDate());

        if(dto.getStrength() >= 4500){
            entity.setResult("PASS");
        } else {
            entity.setResult("FAIL");
        }

        entity.setRemarks(dto.getRemarks());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }


    // ================= GET BY ID =================

    @Override
    public MfTestDetailsResponseDto getById(Long id) {

        MfTestDetails entity = repository.findById(id).orElseThrow();

        return buildResponse(entity);
    }


    // ================= GET ALL =================

    @Override
    public List<MfTestDetailsResponseDto> getAll() {

        List<MfTestDetailsResponseDto> list = new ArrayList<>();

        for(MfTestDetails entity : repository.findAll()){
            list.add(buildResponse(entity));
        }

        return list;
    }


    // ================= DELETE =================

    @Override
    public void delete(Long id) {

        MfTestDetails entity = repository.findById(id).orElseThrow();

        repository.deleteById(entity.getId());
    }


    // ================= RESPONSE BUILDER =================

    private MfTestDetailsResponseDto buildResponse(MfTestDetails entity){

        MfTestDetailsResponseDto dto = new MfTestDetailsResponseDto();

        dto.setId(entity.getId());
        dto.setModulusOfFailureId(entity.getModulusOfFailure().getId());
        dto.setTestingDate(entity.getTestingDate());
        dto.setStrength(entity.getStrength());
        dto.setFinalStrength(entity.getFinalStrength());
        dto.setResult(entity.getResult());
        dto.setRemarks(entity.getRemarks());

        dto.setSampleIdentification(entity.getSampleIdentification());
        dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setBatchNo(entity.getBatchNo());
        dto.setCastingDate(entity.getCastingDate());

        dto.setCreatedBy(entity.getCreatedBy());

       dto.setPlantId(entity.getPlantId());
       dto.setVendorCode(entity.getVendorCode());
       dto.setShift(entity.getShift());

        return dto;
    }
}