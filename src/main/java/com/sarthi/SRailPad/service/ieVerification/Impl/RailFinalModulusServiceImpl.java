package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalModulusRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalModulusResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalModulus;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalModulusRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalModulusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalModulusServiceImpl implements RailFinalModulusService {

    @Autowired
    private RailFinalModulusRepository repository;

    @Override
    @Transactional
    public RailFinalModulusResponseDto save(RailFinalModulusRequestDto dto) {
        RailFinalModulus entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalModulus());

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
    public RailFinalModulusResponseDto getById(Long id) {
        RailFinalModulus entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Modulus record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalModulusResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalModulus entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Modulus record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalModulusResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalModulusRequestDto dto, RailFinalModulus entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        // Before Ageing actual samples (1 to 3)
        entity.setSampleBefore1(dto.getSampleBefore1());
        entity.setSampleBefore2(dto.getSampleBefore2());
        entity.setSampleBefore3(dto.getSampleBefore3());

        // Before Ageing marginal samples (1 to 6)
        entity.setMarginalBefore1(dto.getMarginalBefore1());
        entity.setMarginalBefore2(dto.getMarginalBefore2());
        entity.setMarginalBefore3(dto.getMarginalBefore3());
        entity.setMarginalBefore4(dto.getMarginalBefore4());
        entity.setMarginalBefore5(dto.getMarginalBefore5());
        entity.setMarginalBefore6(dto.getMarginalBefore6());

        // After Ageing actual samples (1 to 3)
        entity.setSampleAfter1(dto.getSampleAfter1());
        entity.setSampleAfter2(dto.getSampleAfter2());
        entity.setSampleAfter3(dto.getSampleAfter3());

        // After Ageing marginal samples (1 to 6)
        entity.setMarginalAfter1(dto.getMarginalAfter1());
        entity.setMarginalAfter2(dto.getMarginalAfter2());
        entity.setMarginalAfter3(dto.getMarginalAfter3());
        entity.setMarginalAfter4(dto.getMarginalAfter4());
        entity.setMarginalAfter5(dto.getMarginalAfter5());
        entity.setMarginalAfter6(dto.getMarginalAfter6());

        entity.setModulusStatus(dto.getModulusStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalModulusResponseDto buildResponse(RailFinalModulus entity) {
        RailFinalModulusResponseDto dto = new RailFinalModulusResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        // Before Ageing actual samples (1 to 3)
        dto.setSampleBefore1(entity.getSampleBefore1());
        dto.setSampleBefore2(entity.getSampleBefore2());
        dto.setSampleBefore3(entity.getSampleBefore3());

        // Before Ageing marginal samples (1 to 6)
        dto.setMarginalBefore1(entity.getMarginalBefore1());
        dto.setMarginalBefore2(entity.getMarginalBefore2());
        dto.setMarginalBefore3(entity.getMarginalBefore3());
        dto.setMarginalBefore4(entity.getMarginalBefore4());
        dto.setMarginalBefore5(entity.getMarginalBefore5());
        dto.setMarginalBefore6(entity.getMarginalBefore6());

        // After Ageing actual samples (1 to 3)
        dto.setSampleAfter1(entity.getSampleAfter1());
        dto.setSampleAfter2(entity.getSampleAfter2());
        dto.setSampleAfter3(entity.getSampleAfter3());

        // After Ageing marginal samples (1 to 6)
        dto.setMarginalAfter1(entity.getMarginalAfter1());
        dto.setMarginalAfter2(entity.getMarginalAfter2());
        dto.setMarginalAfter3(entity.getMarginalAfter3());
        dto.setMarginalAfter4(entity.getMarginalAfter4());
        dto.setMarginalAfter5(entity.getMarginalAfter5());
        dto.setMarginalAfter6(entity.getMarginalAfter6());

        dto.setModulusStatus(entity.getModulusStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
