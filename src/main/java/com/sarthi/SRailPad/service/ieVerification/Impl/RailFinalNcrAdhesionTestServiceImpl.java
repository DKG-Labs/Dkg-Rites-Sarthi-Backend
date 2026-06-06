package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrAdhesionTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrAdhesionTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalNcrAdhesionTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalNcrAdhesionTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalNcrAdhesionTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalNcrAdhesionTestServiceImpl implements RailFinalNcrAdhesionTestService {

    @Autowired
    private RailFinalNcrAdhesionTestRepository repository;

    @Override
    @Transactional
    public RailFinalNcrAdhesionTestResponseDto save(RailFinalNcrAdhesionTestRequestDto dto) {
        RailFinalNcrAdhesionTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalNcrAdhesionTest());

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
    public RailFinalNcrAdhesionTestResponseDto getById(Long id) {
        RailFinalNcrAdhesionTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RailFinalNcrAdhesionTest record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalNcrAdhesionTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalNcrAdhesionTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("RailFinalNcrAdhesionTest record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalNcrAdhesionTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalNcrAdhesionTestRequestDto dto, RailFinalNcrAdhesionTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setS1Peel(dto.getS1Peel());
        entity.setS2Peel(dto.getS2Peel());
        entity.setS1Hpull(dto.getS1Hpull());
        entity.setS2Hpull(dto.getS2Hpull());
        entity.setM1Peel(dto.getM1Peel());
        entity.setM2Peel(dto.getM2Peel());
        entity.setM3Peel(dto.getM3Peel());
        entity.setM4Peel(dto.getM4Peel());
        entity.setM1Hpull(dto.getM1Hpull());
        entity.setM2Hpull(dto.getM2Hpull());
        entity.setM3Hpull(dto.getM3Hpull());
        entity.setM4Hpull(dto.getM4Hpull());

        entity.setNcrAdhesionStatus(dto.getNcrAdhesionStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalNcrAdhesionTestResponseDto buildResponse(RailFinalNcrAdhesionTest entity) {
        RailFinalNcrAdhesionTestResponseDto dto = new RailFinalNcrAdhesionTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setS1Peel(entity.getS1Peel());
        dto.setS2Peel(entity.getS2Peel());
        dto.setS1Hpull(entity.getS1Hpull());
        dto.setS2Hpull(entity.getS2Hpull());
        dto.setM1Peel(entity.getM1Peel());
        dto.setM2Peel(entity.getM2Peel());
        dto.setM3Peel(entity.getM3Peel());
        dto.setM4Peel(entity.getM4Peel());
        dto.setM1Hpull(entity.getM1Hpull());
        dto.setM2Hpull(entity.getM2Hpull());
        dto.setM3Hpull(entity.getM3Hpull());
        dto.setM4Hpull(entity.getM4Hpull());

        dto.setNcrAdhesionStatus(entity.getNcrAdhesionStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
