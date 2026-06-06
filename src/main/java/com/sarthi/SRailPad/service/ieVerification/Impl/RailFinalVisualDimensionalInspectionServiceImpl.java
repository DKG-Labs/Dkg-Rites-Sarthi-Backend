package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalVisualDimensionalInspectionRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalVisualDimensionalInspectionResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalVisualDimensionalInspection;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalVisualDimensionalInspectionRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalVisualDimensionalInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalVisualDimensionalInspectionServiceImpl implements RailFinalVisualDimensionalInspectionService {

    @Autowired
    private RailFinalVisualDimensionalInspectionRepository repository;

    @Override
    @Transactional
    public RailFinalVisualDimensionalInspectionResponseDto save(RailFinalVisualDimensionalInspectionRequestDto dto) {
        RailFinalVisualDimensionalInspection entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalVisualDimensionalInspection());

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
    public RailFinalVisualDimensionalInspectionResponseDto getById(Long id) {
        RailFinalVisualDimensionalInspection entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Visual & Dimensional Inspection record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalVisualDimensionalInspectionResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalVisualDimensionalInspection entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Visual & Dimensional Inspection record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalVisualDimensionalInspectionResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalVisualDimensionalInspectionRequestDto dto, RailFinalVisualDimensionalInspection entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setVisualSamples(dto.getVisualSamples());
        entity.setVisualNotOk(dto.getVisualNotOk());
        entity.setVisualReason(dto.getVisualReason());
        entity.setVisualResult(dto.getVisualResult());

        entity.setDimensionalSamples(dto.getDimensionalSamples());
        entity.setDimensionalNotOk(dto.getDimensionalNotOk());
        entity.setDimensionalReason(dto.getDimensionalReason());
        entity.setDimensionalResult(dto.getDimensionalResult());

        entity.setTotalRejected(dto.getTotalRejected());
    }

    private RailFinalVisualDimensionalInspectionResponseDto buildResponse(RailFinalVisualDimensionalInspection entity) {
        RailFinalVisualDimensionalInspectionResponseDto dto = new RailFinalVisualDimensionalInspectionResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setVisualSamples(entity.getVisualSamples());
        dto.setVisualNotOk(entity.getVisualNotOk());
        dto.setVisualReason(entity.getVisualReason());
        dto.setVisualResult(entity.getVisualResult());

        dto.setDimensionalSamples(entity.getDimensionalSamples());
        dto.setDimensionalNotOk(entity.getDimensionalNotOk());
        dto.setDimensionalReason(entity.getDimensionalReason());
        dto.setDimensionalResult(entity.getDimensionalResult());

        dto.setTotalRejected(entity.getTotalRejected());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
