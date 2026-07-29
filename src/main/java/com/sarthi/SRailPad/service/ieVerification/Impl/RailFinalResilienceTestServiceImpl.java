package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalResilienceTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalResilienceTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalResilienceTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalResilienceTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalResilienceTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalResilienceTestServiceImpl implements RailFinalResilienceTestService {

    @Autowired
    private RailFinalResilienceTestRepository repository;

    @Override
    @Transactional
    public RailFinalResilienceTestResponseDto save(RailFinalResilienceTestRequestDto dto) {
        RailFinalResilienceTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalResilienceTest());

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
    public RailFinalResilienceTestResponseDto getById(Long id) {
        RailFinalResilienceTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Resilience Test record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalResilienceTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalResilienceTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Resilience Test record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalResilienceTestResponseDto> getByCallNo(String callNo) {
        return repository.findAll().stream()
                .filter(e -> callNo.equals(e.getCallNo()))
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalResilienceTestRequestDto dto, RailFinalResilienceTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setS1Impact1(dto.getS1Impact1());
        entity.setS1Impact2(dto.getS1Impact2());
        entity.setS1Impact3(dto.getS1Impact3());
        entity.setS1Impact4(dto.getS1Impact4());
        entity.setS1Impact5(dto.getS1Impact5());
        entity.setS1Impact6(dto.getS1Impact6());

        entity.setS2Impact1(dto.getS2Impact1());
        entity.setS2Impact2(dto.getS2Impact2());
        entity.setS2Impact3(dto.getS2Impact3());
        entity.setS2Impact4(dto.getS2Impact4());
        entity.setS2Impact5(dto.getS2Impact5());
        entity.setS2Impact6(dto.getS2Impact6());

        entity.setS3Impact1(dto.getS3Impact1());
        entity.setS3Impact2(dto.getS3Impact2());
        entity.setS3Impact3(dto.getS3Impact3());
        entity.setS3Impact4(dto.getS3Impact4());
        entity.setS3Impact5(dto.getS3Impact5());
        entity.setS3Impact6(dto.getS3Impact6());

        entity.setResilienceStatus(dto.getResilienceStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalResilienceTestResponseDto buildResponse(RailFinalResilienceTest entity) {
        RailFinalResilienceTestResponseDto dto = new RailFinalResilienceTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setS1Impact1(entity.getS1Impact1());
        dto.setS1Impact2(entity.getS1Impact2());
        dto.setS1Impact3(entity.getS1Impact3());
        dto.setS1Impact4(entity.getS1Impact4());
        dto.setS1Impact5(entity.getS1Impact5());
        dto.setS1Impact6(entity.getS1Impact6());

        dto.setS2Impact1(entity.getS2Impact1());
        dto.setS2Impact2(entity.getS2Impact2());
        dto.setS2Impact3(entity.getS2Impact3());
        dto.setS2Impact4(entity.getS2Impact4());
        dto.setS2Impact5(entity.getS2Impact5());
        dto.setS2Impact6(entity.getS2Impact6());

        dto.setS3Impact1(entity.getS3Impact1());
        dto.setS3Impact2(entity.getS3Impact2());
        dto.setS3Impact3(entity.getS3Impact3());
        dto.setS3Impact4(entity.getS3Impact4());
        dto.setS3Impact5(entity.getS3Impact5());
        dto.setS3Impact6(entity.getS3Impact6());

        dto.setResilienceStatus(entity.getResilienceStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedOn(entity.getCreatedDate());
        dto.setUpdatedOn(entity.getUpdatedDate());

        return dto;
    }
}
