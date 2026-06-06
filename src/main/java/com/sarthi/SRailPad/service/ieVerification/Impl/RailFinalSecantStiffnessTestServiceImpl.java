package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalSecantStiffnessTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalSecantStiffnessTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalSecantStiffnessTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalSecantStiffnessTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalSecantStiffnessTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalSecantStiffnessTestServiceImpl implements RailFinalSecantStiffnessTestService {

    @Autowired
    private RailFinalSecantStiffnessTestRepository repository;

    @Override
    @Transactional
    public RailFinalSecantStiffnessTestResponseDto save(RailFinalSecantStiffnessTestRequestDto dto) {
        RailFinalSecantStiffnessTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalSecantStiffnessTest());

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
    public RailFinalSecantStiffnessTestResponseDto getById(Long id) {
        RailFinalSecantStiffnessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RailFinalSecantStiffnessTest record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalSecantStiffnessTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalSecantStiffnessTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("RailFinalSecantStiffnessTest record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalSecantStiffnessTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalSecantStiffnessTestRequestDto dto, RailFinalSecantStiffnessTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setS1S20A(dto.getS1S20A());
        entity.setS1S20B(dto.getS1S20B());
        entity.setS1S20C(dto.getS1S20C());
        entity.setS1S20D(dto.getS1S20D());
        entity.setS1S90A(dto.getS1S90A());
        entity.setS1S90B(dto.getS1S90B());
        entity.setS1S90C(dto.getS1S90C());
        entity.setS1S90D(dto.getS1S90D());
        entity.setS2S20A(dto.getS2S20A());
        entity.setS2S20B(dto.getS2S20B());
        entity.setS2S20C(dto.getS2S20C());
        entity.setS2S20D(dto.getS2S20D());
        entity.setS2S90A(dto.getS2S90A());
        entity.setS2S90B(dto.getS2S90B());
        entity.setS2S90C(dto.getS2S90C());
        entity.setS2S90D(dto.getS2S90D());
        entity.setM1S20A(dto.getM1S20A());
        entity.setM1S20B(dto.getM1S20B());
        entity.setM1S20C(dto.getM1S20C());
        entity.setM1S20D(dto.getM1S20D());
        entity.setM1S90A(dto.getM1S90A());
        entity.setM1S90B(dto.getM1S90B());
        entity.setM1S90C(dto.getM1S90C());
        entity.setM1S90D(dto.getM1S90D());
        entity.setM2S20A(dto.getM2S20A());
        entity.setM2S20B(dto.getM2S20B());
        entity.setM2S20C(dto.getM2S20C());
        entity.setM2S20D(dto.getM2S20D());
        entity.setM2S90A(dto.getM2S90A());
        entity.setM2S90B(dto.getM2S90B());
        entity.setM2S90C(dto.getM2S90C());
        entity.setM2S90D(dto.getM2S90D());
        entity.setM3S20A(dto.getM3S20A());
        entity.setM3S20B(dto.getM3S20B());
        entity.setM3S20C(dto.getM3S20C());
        entity.setM3S20D(dto.getM3S20D());
        entity.setM3S90A(dto.getM3S90A());
        entity.setM3S90B(dto.getM3S90B());
        entity.setM3S90C(dto.getM3S90C());
        entity.setM3S90D(dto.getM3S90D());
        entity.setM4S20A(dto.getM4S20A());
        entity.setM4S20B(dto.getM4S20B());
        entity.setM4S20C(dto.getM4S20C());
        entity.setM4S20D(dto.getM4S20D());
        entity.setM4S90A(dto.getM4S90A());
        entity.setM4S90B(dto.getM4S90B());
        entity.setM4S90C(dto.getM4S90C());
        entity.setM4S90D(dto.getM4S90D());

        entity.setSecantStatus(dto.getSecantStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalSecantStiffnessTestResponseDto buildResponse(RailFinalSecantStiffnessTest entity) {
        RailFinalSecantStiffnessTestResponseDto dto = new RailFinalSecantStiffnessTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setS1S20A(entity.getS1S20A());
        dto.setS1S20B(entity.getS1S20B());
        dto.setS1S20C(entity.getS1S20C());
        dto.setS1S20D(entity.getS1S20D());
        dto.setS1S90A(entity.getS1S90A());
        dto.setS1S90B(entity.getS1S90B());
        dto.setS1S90C(entity.getS1S90C());
        dto.setS1S90D(entity.getS1S90D());
        dto.setS2S20A(entity.getS2S20A());
        dto.setS2S20B(entity.getS2S20B());
        dto.setS2S20C(entity.getS2S20C());
        dto.setS2S20D(entity.getS2S20D());
        dto.setS2S90A(entity.getS2S90A());
        dto.setS2S90B(entity.getS2S90B());
        dto.setS2S90C(entity.getS2S90C());
        dto.setS2S90D(entity.getS2S90D());
        dto.setM1S20A(entity.getM1S20A());
        dto.setM1S20B(entity.getM1S20B());
        dto.setM1S20C(entity.getM1S20C());
        dto.setM1S20D(entity.getM1S20D());
        dto.setM1S90A(entity.getM1S90A());
        dto.setM1S90B(entity.getM1S90B());
        dto.setM1S90C(entity.getM1S90C());
        dto.setM1S90D(entity.getM1S90D());
        dto.setM2S20A(entity.getM2S20A());
        dto.setM2S20B(entity.getM2S20B());
        dto.setM2S20C(entity.getM2S20C());
        dto.setM2S20D(entity.getM2S20D());
        dto.setM2S90A(entity.getM2S90A());
        dto.setM2S90B(entity.getM2S90B());
        dto.setM2S90C(entity.getM2S90C());
        dto.setM2S90D(entity.getM2S90D());
        dto.setM3S20A(entity.getM3S20A());
        dto.setM3S20B(entity.getM3S20B());
        dto.setM3S20C(entity.getM3S20C());
        dto.setM3S20D(entity.getM3S20D());
        dto.setM3S90A(entity.getM3S90A());
        dto.setM3S90B(entity.getM3S90B());
        dto.setM3S90C(entity.getM3S90C());
        dto.setM3S90D(entity.getM3S90D());
        dto.setM4S20A(entity.getM4S20A());
        dto.setM4S20B(entity.getM4S20B());
        dto.setM4S20C(entity.getM4S20C());
        dto.setM4S20D(entity.getM4S20D());
        dto.setM4S90A(entity.getM4S90A());
        dto.setM4S90B(entity.getM4S90B());
        dto.setM4S90C(entity.getM4S90C());
        dto.setM4S90D(entity.getM4S90D());

        dto.setSecantStatus(entity.getSecantStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
