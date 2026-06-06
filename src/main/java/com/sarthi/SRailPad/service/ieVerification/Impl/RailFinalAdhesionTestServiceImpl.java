package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalAdhesionTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalAdhesionTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalAdhesionTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalAdhesionTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalAdhesionTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalAdhesionTestServiceImpl implements RailFinalAdhesionTestService {

    @Autowired
    private RailFinalAdhesionTestRepository repository;

    @Override
    @Transactional
    public RailFinalAdhesionTestResponseDto save(RailFinalAdhesionTestRequestDto dto) {
        RailFinalAdhesionTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalAdhesionTest());

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
    public RailFinalAdhesionTestResponseDto getById(Long id) {
        RailFinalAdhesionTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RailFinalAdhesionTest record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalAdhesionTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalAdhesionTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("RailFinalAdhesionTest record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalAdhesionTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalAdhesionTestRequestDto dto, RailFinalAdhesionTest entity) {
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
        entity.setMarginal1(dto.getMarginal1());
        entity.setMarginal2(dto.getMarginal2());
        entity.setMarginal3(dto.getMarginal3());
        entity.setMarginal4(dto.getMarginal4());

        entity.setAdhesionStatus(dto.getAdhesionStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalAdhesionTestResponseDto buildResponse(RailFinalAdhesionTest entity) {
        RailFinalAdhesionTestResponseDto dto = new RailFinalAdhesionTestResponseDto();
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
        dto.setMarginal1(entity.getMarginal1());
        dto.setMarginal2(entity.getMarginal2());
        dto.setMarginal3(entity.getMarginal3());
        dto.setMarginal4(entity.getMarginal4());

        dto.setAdhesionStatus(entity.getAdhesionStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
