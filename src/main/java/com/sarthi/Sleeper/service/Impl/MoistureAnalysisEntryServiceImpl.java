package com.sarthi.Sleeper.service.Impl;


import com.sarthi.Sleeper.dto.MoistureAnalysisRequestDTO;
import com.sarthi.Sleeper.dto.MoistureAnalysisResponseDTO;
import com.sarthi.Sleeper.entity.MoistureAnalysisEntry;
import com.sarthi.Sleeper.repository.MoistureAnalysisEntryRepository;
import com.sarthi.Sleeper.service.MoistureAnalysisEntryService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoistureAnalysisEntryServiceImpl implements MoistureAnalysisEntryService {

    @Autowired
    private MoistureAnalysisEntryRepository moistureAnalysisEntryRepository;


    @Override
    public MoistureAnalysisResponseDTO create(MoistureAnalysisRequestDTO dto) {

        MoistureAnalysisEntry e =
                new MoistureAnalysisEntry();

        LocalDate eDate = CommonUtils.convertStringToDateObject(dto.getEntryDate());
        if (eDate != null) {
            e.setEntryDate(eDate);
        }

        e.setShift(dto.getShift());
        e.setEntryTime(dto.getEntryTime());
        e.setBatchNo(dto.getBatchNo());
        e.setBatchWtDryCa1(dto.getBatchWtDryCa1());
        e.setBatchWtDryCa2(dto.getBatchWtDryCa2());
        e.setBatchWtDryFa(dto.getBatchWtDryFa());
        e.setBatchWtDryWater(dto.getBatchWtDryWater());
        e.setBatchWtDryAdmix(dto.getBatchWtDryAdmix());
        e.setBatchWtDryCement(dto.getBatchWtDryCement());

        e.setWtAdoptedCa1(dto.getWtAdoptedCa1());
        e.setWtAdoptedCa2(dto.getWtAdoptedCa2());
        e.setWtAdoptedFa(dto.getWtAdoptedFa());

        e.setTotalFreeMoisture(dto.getTotalFreeMoisture());
        e.setAdjustedWaterWt(dto.getAdjustedWaterWt());
        e.setWcRatio(dto.getWcRatio());
        e.setAcRatio(dto.getAcRatio());

        e.setSectionType(dto.getSectionType());

        e.setWtWetSample(dto.getWtWetSample());
        e.setWtDriedSample(dto.getWtDriedSample());
        e.setWtMoistureSample(dto.getWtMoistureSample());
        e.setMoisturePercent(dto.getMoisturePercent());
        e.setAbsorptionPercent(dto.getAbsorptionPercent());
        e.setFreeMoisturePercent(dto.getFreeMoisturePercent());
        e.setBatchWtDry(dto.getBatchWtDry());
        e.setFreeMoistureKg(dto.getFreeMoistureKg());
        e.setAdjustedWeight(dto.getAdjustedWeight());
        e.setAdoptedWeight(dto.getAdoptedWeight());


        e.setCreatedBy(dto.getCreatedBy());
        e.setCreatedDate(LocalDateTime.now());
        e.setStatus("A");

        MoistureAnalysisEntry saved =
                moistureAnalysisEntryRepository.save(e);

        return mapToResponse(saved);
    }


    @Override
    public List<MoistureAnalysisResponseDTO> getAll() {

        return moistureAnalysisEntryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }



    @Override
    public MoistureAnalysisResponseDTO update(
            Long id,
            MoistureAnalysisRequestDTO dto) {

        MoistureAnalysisEntry e =
                moistureAnalysisEntryRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        " Moisture Analysis not found for the provided Id.")
                        ));
        LocalDate eDate = CommonUtils.convertStringToDateObject(dto.getEntryDate());
        if (eDate != null) {
            e.setEntryDate(eDate);
        }

        e.setShift(dto.getShift());
        e.setEntryTime(dto.getEntryTime());
        e.setBatchNo(dto.getBatchNo());

        e.setBatchWtDryCa1(dto.getBatchWtDryCa1());
        e.setBatchWtDryCa2(dto.getBatchWtDryCa2());
        e.setBatchWtDryFa(dto.getBatchWtDryFa());
        e.setBatchWtDryWater(dto.getBatchWtDryWater());
        e.setBatchWtDryAdmix(dto.getBatchWtDryAdmix());
        e.setBatchWtDryCement(dto.getBatchWtDryCement());

        e.setWtAdoptedCa1(dto.getWtAdoptedCa1());
        e.setWtAdoptedCa2(dto.getWtAdoptedCa2());
        e.setWtAdoptedFa(dto.getWtAdoptedFa());

        e.setTotalFreeMoisture(dto.getTotalFreeMoisture());
        e.setAdjustedWaterWt(dto.getAdjustedWaterWt());
        e.setWcRatio(dto.getWcRatio());
        e.setAcRatio(dto.getAcRatio());


        e.setSectionType(dto.getSectionType());

        e.setWtWetSample(dto.getWtWetSample());
        e.setWtDriedSample(dto.getWtDriedSample());
        e.setWtMoistureSample(dto.getWtMoistureSample());
        e.setMoisturePercent(dto.getMoisturePercent());
        e.setAbsorptionPercent(dto.getAbsorptionPercent());
        e.setFreeMoisturePercent(dto.getFreeMoisturePercent());
        e.setBatchWtDry(dto.getBatchWtDry());
        e.setFreeMoistureKg(dto.getFreeMoistureKg());
        e.setAdjustedWeight(dto.getAdjustedWeight());
        e.setAdoptedWeight(dto.getAdoptedWeight());
        e.setUpdatedBy(dto.getUpdatedBy());

        e.setUpdatedDate(LocalDateTime.now());

        MoistureAnalysisEntry updated =
                moistureAnalysisEntryRepository.save(e);

        return mapToResponse(updated);
    }



    private MoistureAnalysisResponseDTO mapToResponse(
            MoistureAnalysisEntry e) {

        MoistureAnalysisResponseDTO dto =
                new MoistureAnalysisResponseDTO();

        dto.setId(e.getId());

        String eDate = CommonUtils.convertDateToString(e.getEntryDate());
        if (eDate != null) {
            dto.setEntryDate(eDate);
        }

        dto.setShift(e.getShift());
        dto.setEntryTime(e.getEntryTime());
        dto.setBatchNo(e.getBatchNo());

        dto.setBatchWtDryCa1(e.getBatchWtDryCa1());
        dto.setBatchWtDryCa2(e.getBatchWtDryCa2());
        dto.setBatchWtDryFa(e.getBatchWtDryFa());
        dto.setBatchWtDryWater(e.getBatchWtDryWater());
        dto.setBatchWtDryAdmix(e.getBatchWtDryAdmix());
        dto.setBatchWtDryCement(e.getBatchWtDryCement());

        dto.setWtAdoptedCa1(e.getWtAdoptedCa1());
        dto.setWtAdoptedCa2(e.getWtAdoptedCa2());
        dto.setWtAdoptedFa(e.getWtAdoptedFa());

        dto.setTotalFreeMoisture(e.getTotalFreeMoisture());
        dto.setAdjustedWaterWt(e.getAdjustedWaterWt());
        dto.setWcRatio(e.getWcRatio());
        dto.setAcRatio(e.getAcRatio());

        dto.setSectionType(e.getSectionType());

        dto.setWtWetSample(e.getWtWetSample());
        dto.setWtDriedSample(e.getWtDriedSample());
        dto.setWtMoistureSample(e.getWtMoistureSample());
        dto.setMoisturePercent(e.getMoisturePercent());
        dto.setAbsorptionPercent(e.getAbsorptionPercent());
        dto.setFreeMoisturePercent(e.getFreeMoisturePercent());
        dto.setBatchWtDry(e.getBatchWtDry());
        dto.setFreeMoistureKg(e.getFreeMoistureKg());
        dto.setAdjustedWeight(e.getAdjustedWeight());
        dto.setAdoptedWeight(e.getAdoptedWeight());

        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());

        dto.setCreatedDate(e.getCreatedDate());
        dto.setUpdatedDate(e.getUpdatedDate());
        dto.setStatus(e.getStatus());

        return dto;
    }
}
