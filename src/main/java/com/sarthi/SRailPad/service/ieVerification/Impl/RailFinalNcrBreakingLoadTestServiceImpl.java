package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrBreakingLoadTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrBreakingLoadTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalNcrBreakingLoadTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalNcrBreakingLoadTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalNcrBreakingLoadTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalNcrBreakingLoadTestServiceImpl implements RailFinalNcrBreakingLoadTestService {

    @Autowired
    private RailFinalNcrBreakingLoadTestRepository repository;

    @Override
    @Transactional
    public RailFinalNcrBreakingLoadTestResponseDto save(RailFinalNcrBreakingLoadTestRequestDto dto) {
        RailFinalNcrBreakingLoadTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalNcrBreakingLoadTest());

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
    public RailFinalNcrBreakingLoadTestResponseDto getById(Long id) {
        RailFinalNcrBreakingLoadTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RailFinalNcrBreakingLoadTest record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalNcrBreakingLoadTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalNcrBreakingLoadTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("RailFinalNcrBreakingLoadTest record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalNcrBreakingLoadTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalNcrBreakingLoadTestRequestDto dto, RailFinalNcrBreakingLoadTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setSample1(dto.getSample1());
        entity.setSample2(dto.getSample2());
        entity.setSample3(dto.getSample3());
        entity.setSample4(dto.getSample4());
        entity.setSample5(dto.getSample5());
        entity.setMarginal1(dto.getMarginal1());
        entity.setMarginal2(dto.getMarginal2());
        entity.setMarginal3(dto.getMarginal3());
        entity.setMarginal4(dto.getMarginal4());
        entity.setMarginal5(dto.getMarginal5());
        entity.setMarginal6(dto.getMarginal6());
        entity.setMarginal7(dto.getMarginal7());
        entity.setMarginal8(dto.getMarginal8());
        entity.setMarginal9(dto.getMarginal9());
        entity.setMarginal10(dto.getMarginal10());

        entity.setNcrBreakingStatus(dto.getNcrBreakingStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalNcrBreakingLoadTestResponseDto buildResponse(RailFinalNcrBreakingLoadTest entity) {
        RailFinalNcrBreakingLoadTestResponseDto dto = new RailFinalNcrBreakingLoadTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setSample1(entity.getSample1());
        dto.setSample2(entity.getSample2());
        dto.setSample3(entity.getSample3());
        dto.setSample4(entity.getSample4());
        dto.setSample5(entity.getSample5());
        dto.setMarginal1(entity.getMarginal1());
        dto.setMarginal2(entity.getMarginal2());
        dto.setMarginal3(entity.getMarginal3());
        dto.setMarginal4(entity.getMarginal4());
        dto.setMarginal5(entity.getMarginal5());
        dto.setMarginal6(entity.getMarginal6());
        dto.setMarginal7(entity.getMarginal7());
        dto.setMarginal8(entity.getMarginal8());
        dto.setMarginal9(entity.getMarginal9());
        dto.setMarginal10(entity.getMarginal10());

        dto.setNcrBreakingStatus(entity.getNcrBreakingStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
