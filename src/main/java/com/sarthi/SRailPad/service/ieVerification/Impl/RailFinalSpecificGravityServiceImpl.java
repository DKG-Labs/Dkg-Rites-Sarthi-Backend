package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalSpecificGravityRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalSpecificGravityResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalSpecificGravity;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalSpecificGravityRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalSpecificGravityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalSpecificGravityServiceImpl implements RailFinalSpecificGravityService {

    @Autowired
    private RailFinalSpecificGravityRepository repository;

    @Override
    @Transactional
    public RailFinalSpecificGravityResponseDto save(RailFinalSpecificGravityRequestDto dto) {
        RailFinalSpecificGravity entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalSpecificGravity());

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
    public RailFinalSpecificGravityResponseDto getById(Long id) {
        RailFinalSpecificGravity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Specific Gravity record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalSpecificGravityResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalSpecificGravity entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Specific Gravity record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalSpecificGravityResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalSpecificGravityRequestDto dto, RailFinalSpecificGravity entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        // Compound A Air (Actual s1..3, Marginal m1..6)
        entity.setS1AAir(dto.getS1AAir());
        entity.setS2AAir(dto.getS2AAir());
        entity.setS3AAir(dto.getS3AAir());
        entity.setM1AAir(dto.getM1AAir());
        entity.setM2AAir(dto.getM2AAir());
        entity.setM3AAir(dto.getM3AAir());
        entity.setM4AAir(dto.getM4AAir());
        entity.setM5AAir(dto.getM5AAir());
        entity.setM6AAir(dto.getM6AAir());

        // Compound A Water (Actual s1..3, Marginal m1..6)
        entity.setS1AWater(dto.getS1AWater());
        entity.setS2AWater(dto.getS2AWater());
        entity.setS3AWater(dto.getS3AWater());
        entity.setM1AWater(dto.getM1AWater());
        entity.setM2AWater(dto.getM2AWater());
        entity.setM3AWater(dto.getM3AWater());
        entity.setM4AWater(dto.getM4AWater());
        entity.setM5AWater(dto.getM5AWater());
        entity.setM6AWater(dto.getM6AWater());

        // Compound B Air (Actual s1..3, Marginal m1..6)
        entity.setS1BAir(dto.getS1BAir());
        entity.setS2BAir(dto.getS2BAir());
        entity.setS3BAir(dto.getS3BAir());
        entity.setM1BAir(dto.getM1BAir());
        entity.setM2BAir(dto.getM2BAir());
        entity.setM3BAir(dto.getM3BAir());
        entity.setM4BAir(dto.getM4BAir());
        entity.setM5BAir(dto.getM5BAir());
        entity.setM6BAir(dto.getM6BAir());

        // Compound B Water (Actual s1..3, Marginal m1..6)
        entity.setS1BWater(dto.getS1BWater());
        entity.setS2BWater(dto.getS2BWater());
        entity.setS3BWater(dto.getS3BWater());
        entity.setM1BWater(dto.getM1BWater());
        entity.setM2BWater(dto.getM2BWater());
        entity.setM3BWater(dto.getM3BWater());
        entity.setM4BWater(dto.getM4BWater());
        entity.setM5BWater(dto.getM5BWater());
        entity.setM6BWater(dto.getM6BWater());

        entity.setSgStatus(dto.getSgStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalSpecificGravityResponseDto buildResponse(RailFinalSpecificGravity entity) {
        RailFinalSpecificGravityResponseDto dto = new RailFinalSpecificGravityResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        // Compound A Air (Actual s1..3, Marginal m1..6)
        dto.setS1AAir(entity.getS1AAir());
        dto.setS2AAir(entity.getS2AAir());
        dto.setS3AAir(entity.getS3AAir());
        dto.setM1AAir(entity.getM1AAir());
        dto.setM2AAir(entity.getM2AAir());
        dto.setM3AAir(entity.getM3AAir());
        dto.setM4AAir(entity.getM4AAir());
        dto.setM5AAir(entity.getM5AAir());
        dto.setM6AAir(entity.getM6AAir());

        // Compound A Water (Actual s1..3, Marginal m1..6)
        dto.setS1AWater(entity.getS1AWater());
        dto.setS2AWater(entity.getS2AWater());
        dto.setS3AWater(entity.getS3AWater());
        dto.setM1AWater(entity.getM1AWater());
        dto.setM2AWater(entity.getM2AWater());
        dto.setM3AWater(entity.getM3AWater());
        dto.setM4AWater(entity.getM4AWater());
        dto.setM5AWater(entity.getM5AWater());
        dto.setM6AWater(entity.getM6AWater());

        // Compound B Air (Actual s1..3, Marginal m1..6)
        dto.setS1BAir(entity.getS1BAir());
        dto.setS2BAir(entity.getS2BAir());
        dto.setS3BAir(entity.getS3BAir());
        dto.setM1BAir(entity.getM1BAir());
        dto.setM2BAir(entity.getM2BAir());
        dto.setM3BAir(entity.getM3BAir());
        dto.setM4BAir(entity.getM4BAir());
        dto.setM5BAir(entity.getM5BAir());
        dto.setM6BAir(entity.getM6BAir());

        // Compound B Water (Actual s1..3, Marginal m1..6)
        dto.setS1BWater(entity.getS1BWater());
        dto.setS2BWater(entity.getS2BWater());
        dto.setS3BWater(entity.getS3BWater());
        dto.setM1BWater(entity.getM1BWater());
        dto.setM2BWater(entity.getM2BWater());
        dto.setM3BWater(entity.getM3BWater());
        dto.setM4BWater(entity.getM4BWater());
        dto.setM5BWater(entity.getM5BWater());
        dto.setM6BWater(entity.getM6BWater());

        dto.setSgStatus(entity.getSgStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
