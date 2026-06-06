package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalLoadTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalLoadTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalLoadTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalLoadTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalLoadTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalLoadTestServiceImpl implements RailFinalLoadTestService {

    @Autowired
    private RailFinalLoadTestRepository repository;

    @Override
    @Transactional
    public RailFinalLoadTestResponseDto save(RailFinalLoadTestRequestDto dto) {
        RailFinalLoadTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalLoadTest());

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
    public RailFinalLoadTestResponseDto getById(Long id) {
        RailFinalLoadTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Load Test record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalLoadTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalLoadTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Load Test record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalLoadTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalLoadTestRequestDto dto, RailFinalLoadTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        // Pad 1 L & R
        entity.setPad1L1(dto.getPad1L1()); entity.setPad1L2(dto.getPad1L2()); entity.setPad1L3(dto.getPad1L3()); entity.setPad1L4(dto.getPad1L4());
        entity.setPad1L5(dto.getPad1L5()); entity.setPad1L6(dto.getPad1L6()); entity.setPad1L7(dto.getPad1L7()); entity.setPad1L8(dto.getPad1L8());
        entity.setPad1R1(dto.getPad1R1()); entity.setPad1R2(dto.getPad1R2()); entity.setPad1R3(dto.getPad1R3()); entity.setPad1R4(dto.getPad1R4());
        entity.setPad1R5(dto.getPad1R5()); entity.setPad1R6(dto.getPad1R6()); entity.setPad1R7(dto.getPad1R7()); entity.setPad1R8(dto.getPad1R8());

        // Pad 2 L & R
        entity.setPad2L1(dto.getPad2L1()); entity.setPad2L2(dto.getPad2L2()); entity.setPad2L3(dto.getPad2L3()); entity.setPad2L4(dto.getPad2L4());
        entity.setPad2L5(dto.getPad2L5()); entity.setPad2L6(dto.getPad2L6()); entity.setPad2L7(dto.getPad2L7()); entity.setPad2L8(dto.getPad2L8());
        entity.setPad2R1(dto.getPad2R1()); entity.setPad2R2(dto.getPad2R2()); entity.setPad2R3(dto.getPad2R3()); entity.setPad2R4(dto.getPad2R4());
        entity.setPad2R5(dto.getPad2R5()); entity.setPad2R6(dto.getPad2R6()); entity.setPad2R7(dto.getPad2R7()); entity.setPad2R8(dto.getPad2R8());

        // Marginal Pad 1 L & R
        entity.setMPad1L1(dto.getMPad1L1()); entity.setMPad1L2(dto.getMPad1L2()); entity.setMPad1L3(dto.getMPad1L3()); entity.setMPad1L4(dto.getMPad1L4());
        entity.setMPad1L5(dto.getMPad1L5()); entity.setMPad1L6(dto.getMPad1L6()); entity.setMPad1L7(dto.getMPad1L7()); entity.setMPad1L8(dto.getMPad1L8());
        entity.setMPad1R1(dto.getMPad1R1()); entity.setMPad1R2(dto.getMPad1R2()); entity.setMPad1R3(dto.getMPad1R3()); entity.setMPad1R4(dto.getMPad1R4());
        entity.setMPad1R5(dto.getMPad1R5()); entity.setMPad1R6(dto.getMPad1R6()); entity.setMPad1R7(dto.getMPad1R7()); entity.setMPad1R8(dto.getMPad1R8());

        // Marginal Pad 2 L & R
        entity.setMPad2L1(dto.getMPad2L1()); entity.setMPad2L2(dto.getMPad2L2()); entity.setMPad2L3(dto.getMPad2L3()); entity.setMPad2L4(dto.getMPad2L4());
        entity.setMPad2L5(dto.getMPad2L5()); entity.setMPad2L6(dto.getMPad2L6()); entity.setMPad2L7(dto.getMPad2L7()); entity.setMPad2L8(dto.getMPad2L8());
        entity.setMPad2R1(dto.getMPad2R1()); entity.setMPad2R2(dto.getMPad2R2()); entity.setMPad2R3(dto.getMPad2R3()); entity.setMPad2R4(dto.getMPad2R4());
        entity.setMPad2R5(dto.getMPad2R5()); entity.setMPad2R6(dto.getMPad2R6()); entity.setMPad2R7(dto.getMPad2R7()); entity.setMPad2R8(dto.getMPad2R8());

        // Marginal Pad 3 L & R
        entity.setMPad3L1(dto.getMPad3L1()); entity.setMPad3L2(dto.getMPad3L2()); entity.setMPad3L3(dto.getMPad3L3()); entity.setMPad3L4(dto.getMPad3L4());
        entity.setMPad3L5(dto.getMPad3L5()); entity.setMPad3L6(dto.getMPad3L6()); entity.setMPad3L7(dto.getMPad3L7()); entity.setMPad3L8(dto.getMPad3L8());
        entity.setMPad3R1(dto.getMPad3R1()); entity.setMPad3R2(dto.getMPad3R2()); entity.setMPad3R3(dto.getMPad3R3()); entity.setMPad3R4(dto.getMPad3R4());
        entity.setMPad3R5(dto.getMPad3R5()); entity.setMPad3R6(dto.getMPad3R6()); entity.setMPad3R7(dto.getMPad3R7()); entity.setMPad3R8(dto.getMPad3R8());

        // Marginal Pad 4 L & R
        entity.setMPad4L1(dto.getMPad4L1()); entity.setMPad4L2(dto.getMPad4L2()); entity.setMPad4L3(dto.getMPad4L3()); entity.setMPad4L4(dto.getMPad4L4());
        entity.setMPad4L5(dto.getMPad4L5()); entity.setMPad4L6(dto.getMPad4L6()); entity.setMPad4L7(dto.getMPad4L7()); entity.setMPad4L8(dto.getMPad4L8());
        entity.setMPad4R1(dto.getMPad4R1()); entity.setMPad4R2(dto.getMPad4R2()); entity.setMPad4R3(dto.getMPad4R3()); entity.setMPad4R4(dto.getMPad4R4());
        entity.setMPad4R5(dto.getMPad4R5()); entity.setMPad4R6(dto.getMPad4R6()); entity.setMPad4R7(dto.getMPad4R7()); entity.setMPad4R8(dto.getMPad4R8());

        entity.setLoadStatus(dto.getLoadStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalLoadTestResponseDto buildResponse(RailFinalLoadTest entity) {
        RailFinalLoadTestResponseDto dto = new RailFinalLoadTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        // Pad 1 L & R
        dto.setPad1L1(entity.getPad1L1()); dto.setPad1L2(entity.getPad1L2()); dto.setPad1L3(entity.getPad1L3()); dto.setPad1L4(entity.getPad1L4());
        dto.setPad1L5(entity.getPad1L5()); dto.setPad1L6(entity.getPad1L6()); dto.setPad1L7(entity.getPad1L7()); dto.setPad1L8(entity.getPad1L8());
        dto.setPad1R1(entity.getPad1R1()); dto.setPad1R2(entity.getPad1R2()); dto.setPad1R3(entity.getPad1R3()); dto.setPad1R4(entity.getPad1R4());
        dto.setPad1R5(entity.getPad1R5()); dto.setPad1R6(entity.getPad1R6()); dto.setPad1R7(entity.getPad1R7()); dto.setPad1R8(entity.getPad1R8());

        // Pad 2 L & R
        dto.setPad2L1(entity.getPad2L1()); dto.setPad2L2(entity.getPad2L2()); dto.setPad2L3(entity.getPad2L3()); dto.setPad2L4(entity.getPad2L4());
        dto.setPad2L5(entity.getPad2L5()); dto.setPad2L6(entity.getPad2L6()); dto.setPad2L7(entity.getPad2L7()); dto.setPad2L8(entity.getPad2L8());
        dto.setPad2R1(entity.getPad2R1()); dto.setPad2R2(entity.getPad2R2()); dto.setPad2R3(entity.getPad2R3()); dto.setPad2R4(entity.getPad2R4());
        dto.setPad2R5(entity.getPad2R5()); dto.setPad2R6(entity.getPad2R6()); dto.setPad2R7(entity.getPad2R7()); dto.setPad2R8(entity.getPad2R8());

        // Marginal Pad 1 L & R
        dto.setMPad1L1(entity.getMPad1L1()); dto.setMPad1L2(entity.getMPad1L2()); dto.setMPad1L3(entity.getMPad1L3()); dto.setMPad1L4(entity.getMPad1L4());
        dto.setMPad1L5(entity.getMPad1L5()); dto.setMPad1L6(entity.getMPad1L6()); dto.setMPad1L7(entity.getMPad1L7()); dto.setMPad1L8(entity.getMPad1L8());
        dto.setMPad1R1(entity.getMPad1R1()); dto.setMPad1R2(entity.getMPad1R2()); dto.setMPad1R3(entity.getMPad1R3()); dto.setMPad1R4(entity.getMPad1R4());
        dto.setMPad1R5(entity.getMPad1R5()); dto.setMPad1R6(entity.getMPad1R6()); dto.setMPad1R7(entity.getMPad1R7()); dto.setMPad1R8(entity.getMPad1R8());

        // Marginal Pad 2 L & R
        dto.setMPad2L1(entity.getMPad2L1()); dto.setMPad2L2(entity.getMPad2L2()); dto.setMPad2L3(entity.getMPad2L3()); dto.setMPad2L4(entity.getMPad2L4());
        dto.setMPad2L5(entity.getMPad2L5()); dto.setMPad2L6(entity.getMPad2L6()); dto.setMPad2L7(entity.getMPad2L7()); dto.setMPad2L8(entity.getMPad2L8());
        dto.setMPad2R1(entity.getMPad2R1()); dto.setMPad2R2(entity.getMPad2R2()); dto.setMPad2R3(entity.getMPad2R3()); dto.setMPad2R4(entity.getMPad2R4());
        dto.setMPad2R5(entity.getMPad2R5()); dto.setMPad2R6(entity.getMPad2R6()); dto.setMPad2R7(entity.getMPad2R7()); dto.setMPad2R8(entity.getMPad2R8());

        // Marginal Pad 3 L & R
        dto.setMPad3L1(entity.getMPad3L1()); dto.setMPad3L2(entity.getMPad3L2()); dto.setMPad3L3(entity.getMPad3L3()); dto.setMPad3L4(entity.getMPad3L4());
        dto.setMPad3L5(entity.getMPad3L5()); dto.setMPad3L6(entity.getMPad3L6()); dto.setMPad3L7(entity.getMPad3L7()); dto.setMPad3L8(entity.getMPad3L8());
        dto.setMPad3R1(entity.getMPad3R1()); dto.setMPad3R2(entity.getMPad3R2()); dto.setMPad3R3(entity.getMPad3R3()); dto.setMPad3R4(entity.getMPad3R4());
        dto.setMPad3R5(entity.getMPad3R5()); dto.setMPad3R6(entity.getMPad3R6()); dto.setMPad3R7(entity.getMPad3R7()); dto.setMPad3R8(entity.getMPad3R8());

        // Marginal Pad 4 L & R
        dto.setMPad4L1(entity.getMPad4L1()); dto.setMPad4L2(entity.getMPad4L2()); dto.setMPad4L3(entity.getMPad4L3()); dto.setMPad4L4(entity.getMPad4L4());
        dto.setMPad4L5(entity.getMPad4L5()); dto.setMPad4L6(entity.getMPad4L6()); dto.setMPad4L7(entity.getMPad4L7()); dto.setMPad4L8(entity.getMPad4L8());
        dto.setMPad4R1(entity.getMPad4R1()); dto.setMPad4R2(entity.getMPad4R2()); dto.setMPad4R3(entity.getMPad4R3()); dto.setMPad4R4(entity.getMPad4R4());
        dto.setMPad4R5(entity.getMPad4R5()); dto.setMPad4R6(entity.getMPad4R6()); dto.setMPad4R7(entity.getMPad4R7()); dto.setMPad4R8(entity.getMPad4R8());

        dto.setLoadStatus(entity.getLoadStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
