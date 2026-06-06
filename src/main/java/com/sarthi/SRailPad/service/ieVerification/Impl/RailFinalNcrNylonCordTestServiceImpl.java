package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrNylonCordTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrNylonCordTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalNcrNylonCordTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalNcrNylonCordTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalNcrNylonCordTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalNcrNylonCordTestServiceImpl implements RailFinalNcrNylonCordTestService {

    @Autowired
    private RailFinalNcrNylonCordTestRepository repository;

    @Override
    @Transactional
    public RailFinalNcrNylonCordTestResponseDto save(RailFinalNcrNylonCordTestRequestDto dto) {
        RailFinalNcrNylonCordTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalNcrNylonCordTest());

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
    public RailFinalNcrNylonCordTestResponseDto getById(Long id) {
        RailFinalNcrNylonCordTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RailFinalNcrNylonCordTest record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalNcrNylonCordTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalNcrNylonCordTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("RailFinalNcrNylonCordTest record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalNcrNylonCordTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalNcrNylonCordTestRequestDto dto, RailFinalNcrNylonCordTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setS1Denier(dto.getS1Denier());
        entity.setS1Epi(dto.getS1Epi());
        entity.setS1Thickness(dto.getS1Thickness());
        entity.setS1LoadAtBreak(dto.getS1LoadAtBreak());
        entity.setS1Elongation(dto.getS1Elongation());
        entity.setS1Twists(dto.getS1Twists());
        entity.setS2Denier(dto.getS2Denier());
        entity.setS2Epi(dto.getS2Epi());
        entity.setS2Thickness(dto.getS2Thickness());
        entity.setS2LoadAtBreak(dto.getS2LoadAtBreak());
        entity.setS2Elongation(dto.getS2Elongation());
        entity.setS2Twists(dto.getS2Twists());
        entity.setS3Denier(dto.getS3Denier());
        entity.setS3Epi(dto.getS3Epi());
        entity.setS3Thickness(dto.getS3Thickness());
        entity.setS3LoadAtBreak(dto.getS3LoadAtBreak());
        entity.setS3Elongation(dto.getS3Elongation());
        entity.setS3Twists(dto.getS3Twists());
        entity.setM1Denier(dto.getM1Denier());
        entity.setM1Epi(dto.getM1Epi());
        entity.setM1Thickness(dto.getM1Thickness());
        entity.setM1LoadAtBreak(dto.getM1LoadAtBreak());
        entity.setM1Elongation(dto.getM1Elongation());
        entity.setM1Twists(dto.getM1Twists());
        entity.setM2Denier(dto.getM2Denier());
        entity.setM2Epi(dto.getM2Epi());
        entity.setM2Thickness(dto.getM2Thickness());
        entity.setM2LoadAtBreak(dto.getM2LoadAtBreak());
        entity.setM2Elongation(dto.getM2Elongation());
        entity.setM2Twists(dto.getM2Twists());
        entity.setM3Denier(dto.getM3Denier());
        entity.setM3Epi(dto.getM3Epi());
        entity.setM3Thickness(dto.getM3Thickness());
        entity.setM3LoadAtBreak(dto.getM3LoadAtBreak());
        entity.setM3Elongation(dto.getM3Elongation());
        entity.setM3Twists(dto.getM3Twists());
        entity.setM4Denier(dto.getM4Denier());
        entity.setM4Epi(dto.getM4Epi());
        entity.setM4Thickness(dto.getM4Thickness());
        entity.setM4LoadAtBreak(dto.getM4LoadAtBreak());
        entity.setM4Elongation(dto.getM4Elongation());
        entity.setM4Twists(dto.getM4Twists());
        entity.setM5Denier(dto.getM5Denier());
        entity.setM5Epi(dto.getM5Epi());
        entity.setM5Thickness(dto.getM5Thickness());
        entity.setM5LoadAtBreak(dto.getM5LoadAtBreak());
        entity.setM5Elongation(dto.getM5Elongation());
        entity.setM5Twists(dto.getM5Twists());
        entity.setM6Denier(dto.getM6Denier());
        entity.setM6Epi(dto.getM6Epi());
        entity.setM6Thickness(dto.getM6Thickness());
        entity.setM6LoadAtBreak(dto.getM6LoadAtBreak());
        entity.setM6Elongation(dto.getM6Elongation());
        entity.setM6Twists(dto.getM6Twists());

        entity.setNcrCordStatus(dto.getNcrCordStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalNcrNylonCordTestResponseDto buildResponse(RailFinalNcrNylonCordTest entity) {
        RailFinalNcrNylonCordTestResponseDto dto = new RailFinalNcrNylonCordTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setS1Denier(entity.getS1Denier());
        dto.setS1Epi(entity.getS1Epi());
        dto.setS1Thickness(entity.getS1Thickness());
        dto.setS1LoadAtBreak(entity.getS1LoadAtBreak());
        dto.setS1Elongation(entity.getS1Elongation());
        dto.setS1Twists(entity.getS1Twists());
        dto.setS2Denier(entity.getS2Denier());
        dto.setS2Epi(entity.getS2Epi());
        dto.setS2Thickness(entity.getS2Thickness());
        dto.setS2LoadAtBreak(entity.getS2LoadAtBreak());
        dto.setS2Elongation(entity.getS2Elongation());
        dto.setS2Twists(entity.getS2Twists());
        dto.setS3Denier(entity.getS3Denier());
        dto.setS3Epi(entity.getS3Epi());
        dto.setS3Thickness(entity.getS3Thickness());
        dto.setS3LoadAtBreak(entity.getS3LoadAtBreak());
        dto.setS3Elongation(entity.getS3Elongation());
        dto.setS3Twists(entity.getS3Twists());
        dto.setM1Denier(entity.getM1Denier());
        dto.setM1Epi(entity.getM1Epi());
        dto.setM1Thickness(entity.getM1Thickness());
        dto.setM1LoadAtBreak(entity.getM1LoadAtBreak());
        dto.setM1Elongation(entity.getM1Elongation());
        dto.setM1Twists(entity.getM1Twists());
        dto.setM2Denier(entity.getM2Denier());
        dto.setM2Epi(entity.getM2Epi());
        dto.setM2Thickness(entity.getM2Thickness());
        dto.setM2LoadAtBreak(entity.getM2LoadAtBreak());
        dto.setM2Elongation(entity.getM2Elongation());
        dto.setM2Twists(entity.getM2Twists());
        dto.setM3Denier(entity.getM3Denier());
        dto.setM3Epi(entity.getM3Epi());
        dto.setM3Thickness(entity.getM3Thickness());
        dto.setM3LoadAtBreak(entity.getM3LoadAtBreak());
        dto.setM3Elongation(entity.getM3Elongation());
        dto.setM3Twists(entity.getM3Twists());
        dto.setM4Denier(entity.getM4Denier());
        dto.setM4Epi(entity.getM4Epi());
        dto.setM4Thickness(entity.getM4Thickness());
        dto.setM4LoadAtBreak(entity.getM4LoadAtBreak());
        dto.setM4Elongation(entity.getM4Elongation());
        dto.setM4Twists(entity.getM4Twists());
        dto.setM5Denier(entity.getM5Denier());
        dto.setM5Epi(entity.getM5Epi());
        dto.setM5Thickness(entity.getM5Thickness());
        dto.setM5LoadAtBreak(entity.getM5LoadAtBreak());
        dto.setM5Elongation(entity.getM5Elongation());
        dto.setM5Twists(entity.getM5Twists());
        dto.setM6Denier(entity.getM6Denier());
        dto.setM6Epi(entity.getM6Epi());
        dto.setM6Thickness(entity.getM6Thickness());
        dto.setM6LoadAtBreak(entity.getM6LoadAtBreak());
        dto.setM6Elongation(entity.getM6Elongation());
        dto.setM6Twists(entity.getM6Twists());

        dto.setNcrCordStatus(entity.getNcrCordStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
