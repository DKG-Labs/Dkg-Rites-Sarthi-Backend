package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalTensileStrengthRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalTensileStrengthResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalTensileStrength;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalTensileStrengthRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalTensileStrengthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalTensileStrengthServiceImpl implements RailFinalTensileStrengthService {

    @Autowired
    private RailFinalTensileStrengthRepository repository;

    @Override
    @Transactional
    public RailFinalTensileStrengthResponseDto save(RailFinalTensileStrengthRequestDto dto) {
        RailFinalTensileStrength entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalTensileStrength());

        boolean isNew = entity.getId() == null;
        mapDtoToEntity(dto, entity);

        if (isNew) {
            entity.setCreatedBy(dto.getUserId());
            entity.setCreatedDate(LocalDateTime.now());
        } else {
            entity.setUpdatedBy(dto.getUserId());
            entity.setUpdatedDate(LocalDateTime.now());
        }

        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    public RailFinalTensileStrengthResponseDto getById(Long id) {
        RailFinalTensileStrength entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Tensile Strength record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalTensileStrengthResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalTensileStrength entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Tensile Strength record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalTensileStrengthResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalTensileStrengthRequestDto dto, RailFinalTensileStrength entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        // Before Ageing actual samples
        entity.setSampleBefore1(dto.getSampleBefore1());
        entity.setSampleBefore2(dto.getSampleBefore2());
        entity.setSampleBefore3(dto.getSampleBefore3());
        entity.setSampleBefore4(dto.getSampleBefore4());
        entity.setSampleBefore5(dto.getSampleBefore5());

        // Before Ageing marginal samples
        entity.setMarginalBefore1(dto.getMarginalBefore1());
        entity.setMarginalBefore2(dto.getMarginalBefore2());
        entity.setMarginalBefore3(dto.getMarginalBefore3());
        entity.setMarginalBefore4(dto.getMarginalBefore4());
        entity.setMarginalBefore5(dto.getMarginalBefore5());
        entity.setMarginalBefore6(dto.getMarginalBefore6());
        entity.setMarginalBefore7(dto.getMarginalBefore7());
        entity.setMarginalBefore8(dto.getMarginalBefore8());
        entity.setMarginalBefore9(dto.getMarginalBefore9());
        entity.setMarginalBefore10(dto.getMarginalBefore10());

        // After Ageing actual samples
        entity.setSampleAfter1(dto.getSampleAfter1());
        entity.setSampleAfter2(dto.getSampleAfter2());
        entity.setSampleAfter3(dto.getSampleAfter3());
        entity.setSampleAfter4(dto.getSampleAfter4());
        entity.setSampleAfter5(dto.getSampleAfter5());

        // After Ageing marginal samples
        entity.setMarginalAfter1(dto.getMarginalAfter1());
        entity.setMarginalAfter2(dto.getMarginalAfter2());
        entity.setMarginalAfter3(dto.getMarginalAfter3());
        entity.setMarginalAfter4(dto.getMarginalAfter4());
        entity.setMarginalAfter5(dto.getMarginalAfter5());
        entity.setMarginalAfter6(dto.getMarginalAfter6());
        entity.setMarginalAfter7(dto.getMarginalAfter7());
        entity.setMarginalAfter8(dto.getMarginalAfter8());
        entity.setMarginalAfter9(dto.getMarginalAfter9());
        entity.setMarginalAfter10(dto.getMarginalAfter10());

        entity.setTensileStatus(dto.getTensileStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalTensileStrengthResponseDto buildResponse(RailFinalTensileStrength entity) {
        RailFinalTensileStrengthResponseDto dto = new RailFinalTensileStrengthResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        // Before Ageing actual samples
        dto.setSampleBefore1(entity.getSampleBefore1());
        dto.setSampleBefore2(entity.getSampleBefore2());
        dto.setSampleBefore3(entity.getSampleBefore3());
        dto.setSampleBefore4(entity.getSampleBefore4());
        dto.setSampleBefore5(entity.getSampleBefore5());

        // Before Ageing marginal samples
        dto.setMarginalBefore1(entity.getMarginalBefore1());
        dto.setMarginalBefore2(entity.getMarginalBefore2());
        dto.setMarginalBefore3(entity.getMarginalBefore3());
        dto.setMarginalBefore4(entity.getMarginalBefore4());
        dto.setMarginalBefore5(entity.getMarginalBefore5());
        dto.setMarginalBefore6(entity.getMarginalBefore6());
        dto.setMarginalBefore7(entity.getMarginalBefore7());
        dto.setMarginalBefore8(entity.getMarginalBefore8());
        dto.setMarginalBefore9(entity.getMarginalBefore9());
        dto.setMarginalBefore10(entity.getMarginalBefore10());

        // After Ageing actual samples
        dto.setSampleAfter1(entity.getSampleAfter1());
        dto.setSampleAfter2(entity.getSampleAfter2());
        dto.setSampleAfter3(entity.getSampleAfter3());
        dto.setSampleAfter4(entity.getSampleAfter4());
        dto.setSampleAfter5(entity.getSampleAfter5());

        // After Ageing marginal samples
        dto.setMarginalAfter1(entity.getMarginalAfter1());
        dto.setMarginalAfter2(entity.getMarginalAfter2());
        dto.setMarginalAfter3(entity.getMarginalAfter3());
        dto.setMarginalAfter4(entity.getMarginalAfter4());
        dto.setMarginalAfter5(entity.getMarginalAfter5());
        dto.setMarginalAfter6(entity.getMarginalAfter6());
        dto.setMarginalAfter7(entity.getMarginalAfter7());
        dto.setMarginalAfter8(entity.getMarginalAfter8());
        dto.setMarginalAfter9(entity.getMarginalAfter9());
        dto.setMarginalAfter10(entity.getMarginalAfter10());

        dto.setTensileStatus(entity.getTensileStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
