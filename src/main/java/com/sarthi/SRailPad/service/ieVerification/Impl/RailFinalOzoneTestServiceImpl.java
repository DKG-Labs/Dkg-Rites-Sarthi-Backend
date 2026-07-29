package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalOzoneTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalOzoneTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalOzoneTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalOzoneTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalOzoneTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalOzoneTestServiceImpl implements RailFinalOzoneTestService {

    @Autowired
    private RailFinalOzoneTestRepository repository;

    @Override
    @Transactional
    public RailFinalOzoneTestResponseDto save(RailFinalOzoneTestRequestDto dto) {
        RailFinalOzoneTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalOzoneTest());

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
    public RailFinalOzoneTestResponseDto getById(Long id) {
        RailFinalOzoneTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Ozone Test record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalOzoneTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalOzoneTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Ozone Test record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalOzoneTestResponseDto> getByCallNo(String callNo) {
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

    private void mapDtoToEntity(RailFinalOzoneTestRequestDto dto, RailFinalOzoneTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setInitialLength(dto.getInitialLength());
        entity.setStretchedLength(dto.getStretchedLength());
        entity.setObservation(dto.getObservation());

        entity.setOzoneStatus(dto.getOzoneStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalOzoneTestResponseDto buildResponse(RailFinalOzoneTest entity) {
        RailFinalOzoneTestResponseDto dto = new RailFinalOzoneTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setInitialLength(entity.getInitialLength());
        dto.setStretchedLength(entity.getStretchedLength());
        dto.setObservation(entity.getObservation());

        dto.setOzoneStatus(entity.getOzoneStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedOn(entity.getCreatedDate());
        dto.setUpdatedOn(entity.getUpdatedDate());

        return dto;
    }
}
