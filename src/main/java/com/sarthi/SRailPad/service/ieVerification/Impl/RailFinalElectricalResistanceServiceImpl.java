package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalElectricalResistanceRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalElectricalResistanceResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalElectricalResistance;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalElectricalResistanceRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalElectricalResistanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalElectricalResistanceServiceImpl implements RailFinalElectricalResistanceService {

    @Autowired
    private RailFinalElectricalResistanceRepository repository;

    @Override
    @Transactional
    public RailFinalElectricalResistanceResponseDto save(RailFinalElectricalResistanceRequestDto dto) {
        RailFinalElectricalResistance entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalElectricalResistance());

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
    public RailFinalElectricalResistanceResponseDto getById(Long id) {
        RailFinalElectricalResistance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Electrical Resistance record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalElectricalResistanceResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalElectricalResistance entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Electrical Resistance record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalElectricalResistanceResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalElectricalResistanceRequestDto dto, RailFinalElectricalResistance entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        // Before Immersion Forward
        entity.setS1BeforeForward(dto.getS1BeforeForward());
        entity.setS2BeforeForward(dto.getS2BeforeForward());
        entity.setS3BeforeForward(dto.getS3BeforeForward());
        entity.setM1BeforeForward(dto.getM1BeforeForward());
        entity.setM2BeforeForward(dto.getM2BeforeForward());
        entity.setM3BeforeForward(dto.getM3BeforeForward());
        entity.setM4BeforeForward(dto.getM4BeforeForward());
        entity.setM5BeforeForward(dto.getM5BeforeForward());
        entity.setM6BeforeForward(dto.getM6BeforeForward());

        // Before Immersion Reverse
        entity.setS1BeforeReverse(dto.getS1BeforeReverse());
        entity.setS2BeforeReverse(dto.getS2BeforeReverse());
        entity.setS3BeforeReverse(dto.getS3BeforeReverse());
        entity.setM1BeforeReverse(dto.getM1BeforeReverse());
        entity.setM2BeforeReverse(dto.getM2BeforeReverse());
        entity.setM3BeforeReverse(dto.getM3BeforeReverse());
        entity.setM4BeforeReverse(dto.getM4BeforeReverse());
        entity.setM5BeforeReverse(dto.getM5BeforeReverse());
        entity.setM6BeforeReverse(dto.getM6BeforeReverse());

        // After Immersion Forward
        entity.setS1AfterForward(dto.getS1AfterForward());
        entity.setS2AfterForward(dto.getS2AfterForward());
        entity.setS3AfterForward(dto.getS3AfterForward());
        entity.setM1AfterForward(dto.getM1AfterForward());
        entity.setM2AfterForward(dto.getM2AfterForward());
        entity.setM3AfterForward(dto.getM3AfterForward());
        entity.setM4AfterForward(dto.getM4AfterForward());
        entity.setM5AfterForward(dto.getM5AfterForward());
        entity.setM6AfterForward(dto.getM6AfterForward());

        // After Immersion Reverse
        entity.setS1AfterReverse(dto.getS1AfterReverse());
        entity.setS2AfterReverse(dto.getS2AfterReverse());
        entity.setS3AfterReverse(dto.getS3AfterReverse());
        entity.setM1AfterReverse(dto.getM1AfterReverse());
        entity.setM2AfterReverse(dto.getM2AfterReverse());
        entity.setM3AfterReverse(dto.getM3AfterReverse());
        entity.setM4AfterReverse(dto.getM4AfterReverse());
        entity.setM5AfterReverse(dto.getM5AfterReverse());
        entity.setM6AfterReverse(dto.getM6AfterReverse());

        entity.setElectricalStatus(dto.getElectricalStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalElectricalResistanceResponseDto buildResponse(RailFinalElectricalResistance entity) {
        RailFinalElectricalResistanceResponseDto dto = new RailFinalElectricalResistanceResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        // Before Immersion Forward
        dto.setS1BeforeForward(entity.getS1BeforeForward());
        dto.setS2BeforeForward(entity.getS2BeforeForward());
        dto.setS3BeforeForward(entity.getS3BeforeForward());
        dto.setM1BeforeForward(entity.getM1BeforeForward());
        dto.setM2BeforeForward(entity.getM2BeforeForward());
        dto.setM3BeforeForward(entity.getM3BeforeForward());
        dto.setM4BeforeForward(entity.getM4BeforeForward());
        dto.setM5BeforeForward(entity.getM5BeforeForward());
        dto.setM6BeforeForward(entity.getM6BeforeForward());

        // Before Immersion Reverse
        dto.setS1BeforeReverse(entity.getS1BeforeReverse());
        dto.setS2BeforeReverse(entity.getS2BeforeReverse());
        dto.setS3BeforeReverse(entity.getS3BeforeReverse());
        dto.setM1BeforeReverse(entity.getM1BeforeReverse());
        dto.setM2BeforeReverse(entity.getM2BeforeReverse());
        dto.setM3BeforeReverse(entity.getM3BeforeReverse());
        dto.setM4BeforeReverse(entity.getM4BeforeReverse());
        dto.setM5BeforeReverse(entity.getM5BeforeReverse());
        dto.setM6BeforeReverse(entity.getM6BeforeReverse());

        // After Immersion Forward
        dto.setS1AfterForward(entity.getS1AfterForward());
        dto.setS2AfterForward(entity.getS2AfterForward());
        dto.setS3AfterForward(entity.getS3AfterForward());
        dto.setM1AfterForward(entity.getM1AfterForward());
        dto.setM2AfterForward(entity.getM2AfterForward());
        dto.setM3AfterForward(entity.getM3AfterForward());
        dto.setM4AfterForward(entity.getM4AfterForward());
        dto.setM5AfterForward(entity.getM5AfterForward());
        dto.setM6AfterForward(entity.getM6AfterForward());

        // After Immersion Reverse
        dto.setS1AfterReverse(entity.getS1AfterReverse());
        dto.setS2AfterReverse(entity.getS2AfterReverse());
        dto.setS3AfterReverse(entity.getS3AfterReverse());
        dto.setM1AfterReverse(entity.getM1AfterReverse());
        dto.setM2AfterReverse(entity.getM2AfterReverse());
        dto.setM3AfterReverse(entity.getM3AfterReverse());
        dto.setM4AfterReverse(entity.getM4AfterReverse());
        dto.setM5AfterReverse(entity.getM5AfterReverse());
        dto.setM6AfterReverse(entity.getM6AfterReverse());

        dto.setElectricalStatus(entity.getElectricalStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
