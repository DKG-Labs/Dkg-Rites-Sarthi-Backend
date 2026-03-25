package com.sarthi.Sleeper.service.Impl;


import com.sarthi.Sleeper.dto.MoistureAnalysisRequestDTO;
import com.sarthi.Sleeper.dto.MoistureAnalysisResponseDTO;
import com.sarthi.Sleeper.dto.MoistureSectionDTO;
import com.sarthi.Sleeper.dto.MouldPreparationResponseDTO;
import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;
import com.sarthi.Sleeper.entity.MoistureAnalysisEntry;
import com.sarthi.Sleeper.entity.MoistureSection;
import com.sarthi.Sleeper.entity.MouldPreparation;
import com.sarthi.Sleeper.repository.MoistureAnalysisEntryRepository;
import com.sarthi.Sleeper.repository.MoistureSectionRepository;
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
    @Autowired
    private MoistureSectionRepository moistureSectionRepository;


    @Override
    public MoistureAnalysisResponseDTO create(MoistureAnalysisRequestDTO dto) {

        MoistureAnalysisEntry e = new MoistureAnalysisEntry();

        e.setEntryDate(CommonUtils.convertStringToDateObject(dto.getEntryDate()));
        e.setShift(dto.getShift());
        e.setEntryTime(dto.getEntryTime());
        e.setBatchNo(dto.getBatchNo());

        // NEW
        e.setApprovedMixDesign(dto.getApprovedMixDesign());

        e.setDesignAC(dto.getDesignAC());
        e.setDesignWC(dto.getDesignWC());
        e.setDesignCement(dto.getDesignCement());
        e.setDesignCA1(dto.getDesignCA1());
        e.setDesignCA2(dto.getDesignCA2());
        e.setDesignFA(dto.getDesignFA());
        e.setDesignWater(dto.getDesignWater());
        e.setDesignAdmix(dto.getDesignAdmix());

        e.setActualCement(dto.getActualCement());
        e.setActualCA1(dto.getActualCA1());
        e.setActualCA2(dto.getActualCA2());
        e.setActualFA(dto.getActualFA());
        e.setActualWater(dto.getActualWater());
        e.setActualAdmix(dto.getActualAdmix());

        e.setWtAdoptedCa1(dto.getWtAdoptedCa1());
        e.setWtAdoptedCa2(dto.getWtAdoptedCa2());
        e.setWtAdoptedFa(dto.getWtAdoptedFa());

        e.setTotalFreeMoisture(dto.getTotalFreeMoisture());
        e.setAdjustedWaterWt(dto.getAdjustedWaterWt());
        e.setWcRatio(dto.getWcRatio());
        e.setAcRatio(dto.getAcRatio());

        e.setCreatedBy(dto.getCreatedBy());
        e.setCreatedDate(LocalDateTime.now());
        e.setStatus("A");

        // SECTIONS SAVE
        List<MoistureSection> sections = dto.getSections().stream().map(s -> {
            MoistureSection sec = new MoistureSection();
            sec.setSectionType(s.getSectionType());
            sec.setWtWetSample(s.getWtWetSample());
            sec.setWtDriedSample(s.getWtDriedSample());
            sec.setWtMoistureSample(s.getWtMoistureSample());
            sec.setMoisturePercent(s.getMoisturePercent());
            sec.setAbsorptionPercent(s.getAbsorptionPercent());
            sec.setFreeMoisturePercent(s.getFreeMoisturePercent());
            sec.setBatchWtDry(s.getBatchWtDry());
            sec.setFreeMoistureKg(s.getFreeMoistureKg());
            sec.setAdjustedWeight(s.getAdjustedWeight());
            sec.setAdoptedWeight(s.getAdoptedWeight());
            sec.setEntry(e);
            return sec;
        }).toList();

        e.setSections(sections);

        MoistureAnalysisEntry saved = moistureAnalysisEntryRepository.save(e);

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
        e.setEntryDate(CommonUtils.convertStringToDateObject(dto.getEntryDate()));
            e.setShift(dto.getShift());
            e.setEntryTime(dto.getEntryTime());
            e.setBatchNo(dto.getBatchNo());

            e.setApprovedMixDesign(dto.getApprovedMixDesign());

            e.setDesignAC(dto.getDesignAC());
            e.setDesignWC(dto.getDesignWC());
            e.setDesignCement(dto.getDesignCement());
            e.setDesignCA1(dto.getDesignCA1());
            e.setDesignCA2(dto.getDesignCA2());
            e.setDesignFA(dto.getDesignFA());
            e.setDesignWater(dto.getDesignWater());
            e.setDesignAdmix(dto.getDesignAdmix());

            e.setActualCement(dto.getActualCement());
            e.setActualCA1(dto.getActualCA1());
            e.setActualCA2(dto.getActualCA2());
            e.setActualFA(dto.getActualFA());
            e.setActualWater(dto.getActualWater());
            e.setActualAdmix(dto.getActualAdmix());

            e.setWtAdoptedCa1(dto.getWtAdoptedCa1());
            e.setWtAdoptedCa2(dto.getWtAdoptedCa2());
            e.setWtAdoptedFa(dto.getWtAdoptedFa());

            e.setTotalFreeMoisture(dto.getTotalFreeMoisture());
            e.setAdjustedWaterWt(dto.getAdjustedWaterWt());
            e.setWcRatio(dto.getWcRatio());
            e.setAcRatio(dto.getAcRatio());

            //  CLEAR & RESET SECTIONS
            e.getSections().clear();

            List<MoistureSection> sections = dto.getSections().stream().map(s -> {
                MoistureSection sec = new MoistureSection();
                sec.setSectionType(s.getSectionType());
                sec.setWtWetSample(s.getWtWetSample());
                sec.setWtDriedSample(s.getWtDriedSample());
                sec.setWtMoistureSample(s.getWtMoistureSample());
                sec.setMoisturePercent(s.getMoisturePercent());
                sec.setAbsorptionPercent(s.getAbsorptionPercent());
                sec.setFreeMoisturePercent(s.getFreeMoisturePercent());
                sec.setBatchWtDry(s.getBatchWtDry());
                sec.setFreeMoistureKg(s.getFreeMoistureKg());
                sec.setAdjustedWeight(s.getAdjustedWeight());
                sec.setAdoptedWeight(s.getAdoptedWeight());
                sec.setEntry(e);
                return sec;
            }).toList();

            e.getSections().addAll(sections);

            e.setUpdatedBy(dto.getUpdatedBy());
            e.setUpdatedDate(LocalDateTime.now());

            return mapToResponse(moistureAnalysisEntryRepository.save(e));
        }

    @Override
    public MoistureAnalysisResponseDTO getById(Long id) {

        MoistureAnalysisEntry e =
                moistureAnalysisEntryRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        " Moisture Analysis not found for the provided Id.")
                        ));
        return mapToResponse(e);
    }


    private MoistureAnalysisResponseDTO mapToResponse(MoistureAnalysisEntry e) {

        MoistureAnalysisResponseDTO dto = new MoistureAnalysisResponseDTO();

        dto.setId(e.getId());

        String eDate = CommonUtils.convertDateToString(e.getEntryDate());
        if (eDate != null) {
            dto.setEntryDate(eDate);
        }

        dto.setShift(e.getShift());
        dto.setEntryTime(e.getEntryTime());
        dto.setBatchNo(e.getBatchNo());

        //NEW FIELDS
        dto.setApprovedMixDesign(e.getApprovedMixDesign());

        dto.setDesignAC(e.getDesignAC());
        dto.setDesignWC(e.getDesignWC());
        dto.setDesignCement(e.getDesignCement());
        dto.setDesignCA1(e.getDesignCA1());
        dto.setDesignCA2(e.getDesignCA2());
        dto.setDesignFA(e.getDesignFA());
        dto.setDesignWater(e.getDesignWater());
        dto.setDesignAdmix(e.getDesignAdmix());

        dto.setActualCement(e.getActualCement());
        dto.setActualCA1(e.getActualCA1());
        dto.setActualCA2(e.getActualCA2());
        dto.setActualFA(e.getActualFA());
        dto.setActualWater(e.getActualWater());
        dto.setActualAdmix(e.getActualAdmix());

        // COMMON
        dto.setWtAdoptedCa1(e.getWtAdoptedCa1());
        dto.setWtAdoptedCa2(e.getWtAdoptedCa2());
        dto.setWtAdoptedFa(e.getWtAdoptedFa());

        dto.setTotalFreeMoisture(e.getTotalFreeMoisture());
        dto.setAdjustedWaterWt(e.getAdjustedWaterWt());
        dto.setWcRatio(e.getWcRatio());
        dto.setAcRatio(e.getAcRatio());

        // ================= SECTIONS MAPPING =================
        if (e.getSections() != null) {

            List<MoistureSectionDTO> sectionList = e.getSections()
                    .stream()
                    .map(s -> {
                        MoistureSectionDTO sd = new MoistureSectionDTO();

                        sd.setSectionType(s.getSectionType());
                        sd.setWtWetSample(s.getWtWetSample());
                        sd.setWtDriedSample(s.getWtDriedSample());
                        sd.setWtMoistureSample(s.getWtMoistureSample());
                        sd.setMoisturePercent(s.getMoisturePercent());
                        sd.setAbsorptionPercent(s.getAbsorptionPercent());
                        sd.setFreeMoisturePercent(s.getFreeMoisturePercent());
                        sd.setBatchWtDry(s.getBatchWtDry());
                        sd.setFreeMoistureKg(s.getFreeMoistureKg());
                        sd.setAdjustedWeight(s.getAdjustedWeight());
                        sd.setAdoptedWeight(s.getAdoptedWeight());

                        return sd;
                    })
                    .toList();

            dto.setSections(sectionList);
        }

        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());


        dto.setStatus(e.getStatus());

        return dto;
    }

    @Override
    public void delete(Long id) {

        MoistureAnalysisEntry e =
                moistureAnalysisEntryRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        " Moisture Analysis not found for the provided Id.")
                        ));

       moistureAnalysisEntryRepository.deleteById(e.getId());
    }
}
