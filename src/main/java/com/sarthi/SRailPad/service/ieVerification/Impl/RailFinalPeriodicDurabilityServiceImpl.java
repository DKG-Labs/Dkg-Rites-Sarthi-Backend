package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicDurabilityRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicDurabilityResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalPeriodicDurability;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalPeriodicDurabilityRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalPeriodicDurabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalPeriodicDurabilityServiceImpl implements RailFinalPeriodicDurabilityService {

    @Autowired
    private RailFinalPeriodicDurabilityRepository repository;

    @Override
    @Transactional
    public RailFinalPeriodicDurabilityResponseDto save(RailFinalPeriodicDurabilityRequestDto dto) {
        RailFinalPeriodicDurability entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalPeriodicDurability());

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
    public RailFinalPeriodicDurabilityResponseDto getById(Long id) {
        RailFinalPeriodicDurability entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Periodic Durability record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalPeriodicDurabilityResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalPeriodicDurability entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Periodic Durability record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalPeriodicDurabilityResponseDto> getByCallNo(String callNo) {
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

    private void mapDtoToEntity(RailFinalPeriodicDurabilityRequestDto dto, RailFinalPeriodicDurability entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setDateOfLastTest(dto.getDateOfLastTest());
        entity.setQtyProducedSinceLastTest(dto.getQtyProducedSinceLastTest());
        entity.setTestingThreshold(dto.getTestingThreshold());
        entity.setIsMandatory(dto.getIsMandatory());

        entity.setS1LotNo(dto.getS1LotNo());
        entity.setS1InitialThickness(dto.getS1InitialThickness());
        entity.setS1FinalThickness(dto.getS1FinalThickness());
        entity.setS1ReductionThickness(dto.getS1ReductionThickness());
        entity.setS1InitialLoadComp(dto.getS1InitialLoadComp());
        entity.setS1FinalLoadComp(dto.getS1FinalLoadComp());
        entity.setS1ChangeLd(dto.getS1ChangeLd());

        entity.setS2LotNo(dto.getS2LotNo());
        entity.setS2InitialThickness(dto.getS2InitialThickness());
        entity.setS2FinalThickness(dto.getS2FinalThickness());
        entity.setS2ReductionThickness(dto.getS2ReductionThickness());
        entity.setS2InitialLoadComp(dto.getS2InitialLoadComp());
        entity.setS2FinalLoadComp(dto.getS2FinalLoadComp());
        entity.setS2ChangeLd(dto.getS2ChangeLd());

        entity.setS3LotNo(dto.getS3LotNo());
        entity.setS3InitialThickness(dto.getS3InitialThickness());
        entity.setS3FinalThickness(dto.getS3FinalThickness());
        entity.setS3ReductionThickness(dto.getS3ReductionThickness());
        entity.setS3InitialLoadComp(dto.getS3InitialLoadComp());
        entity.setS3FinalLoadComp(dto.getS3FinalLoadComp());
        entity.setS3ChangeLd(dto.getS3ChangeLd());

        entity.setS4LotNo(dto.getS4LotNo());
        entity.setS4InitialThickness(dto.getS4InitialThickness());
        entity.setS4FinalThickness(dto.getS4FinalThickness());
        entity.setS4ReductionThickness(dto.getS4ReductionThickness());
        entity.setS4InitialLoadComp(dto.getS4InitialLoadComp());
        entity.setS4FinalLoadComp(dto.getS4FinalLoadComp());
        entity.setS4ChangeLd(dto.getS4ChangeLd());

        entity.setS5LotNo(dto.getS5LotNo());
        entity.setS5InitialThickness(dto.getS5InitialThickness());
        entity.setS5FinalThickness(dto.getS5FinalThickness());
        entity.setS5ReductionThickness(dto.getS5ReductionThickness());
        entity.setS5InitialLoadComp(dto.getS5InitialLoadComp());
        entity.setS5FinalLoadComp(dto.getS5FinalLoadComp());
        entity.setS5ChangeLd(dto.getS5ChangeLd());

        entity.setDurabilityStatus(dto.getDurabilityStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalPeriodicDurabilityResponseDto buildResponse(RailFinalPeriodicDurability entity) {
        RailFinalPeriodicDurabilityResponseDto dto = new RailFinalPeriodicDurabilityResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setDateOfLastTest(entity.getDateOfLastTest());
        dto.setQtyProducedSinceLastTest(entity.getQtyProducedSinceLastTest());
        dto.setTestingThreshold(entity.getTestingThreshold());
        dto.setIsMandatory(entity.getIsMandatory());

        dto.setS1LotNo(entity.getS1LotNo());
        dto.setS1InitialThickness(entity.getS1InitialThickness());
        dto.setS1FinalThickness(entity.getS1FinalThickness());
        dto.setS1ReductionThickness(entity.getS1ReductionThickness());
        dto.setS1InitialLoadComp(entity.getS1InitialLoadComp());
        dto.setS1FinalLoadComp(entity.getS1FinalLoadComp());
        dto.setS1ChangeLd(entity.getS1ChangeLd());

        dto.setS2LotNo(entity.getS2LotNo());
        dto.setS2InitialThickness(entity.getS2InitialThickness());
        dto.setS2FinalThickness(entity.getS2FinalThickness());
        dto.setS2ReductionThickness(entity.getS2ReductionThickness());
        dto.setS2InitialLoadComp(entity.getS2InitialLoadComp());
        dto.setS2FinalLoadComp(entity.getS2FinalLoadComp());
        dto.setS2ChangeLd(entity.getS2ChangeLd());

        dto.setS3LotNo(entity.getS3LotNo());
        dto.setS3InitialThickness(entity.getS3InitialThickness());
        dto.setS3FinalThickness(entity.getS3FinalThickness());
        dto.setS3ReductionThickness(entity.getS3ReductionThickness());
        dto.setS3InitialLoadComp(entity.getS3InitialLoadComp());
        dto.setS3FinalLoadComp(entity.getS3FinalLoadComp());
        dto.setS3ChangeLd(entity.getS3ChangeLd());

        dto.setS4LotNo(entity.getS4LotNo());
        dto.setS4InitialThickness(entity.getS4InitialThickness());
        dto.setS4FinalThickness(entity.getS4FinalThickness());
        dto.setS4ReductionThickness(entity.getS4ReductionThickness());
        dto.setS4InitialLoadComp(entity.getS4InitialLoadComp());
        dto.setS4FinalLoadComp(entity.getS4FinalLoadComp());
        dto.setS4ChangeLd(entity.getS4ChangeLd());

        dto.setS5LotNo(entity.getS5LotNo());
        dto.setS5InitialThickness(entity.getS5InitialThickness());
        dto.setS5FinalThickness(entity.getS5FinalThickness());
        dto.setS5ReductionThickness(entity.getS5ReductionThickness());
        dto.setS5InitialLoadComp(entity.getS5InitialLoadComp());
        dto.setS5FinalLoadComp(entity.getS5FinalLoadComp());
        dto.setS5ChangeLd(entity.getS5ChangeLd());

        dto.setDurabilityStatus(entity.getDurabilityStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedOn(entity.getCreatedDate());
        dto.setUpdatedOn(entity.getUpdatedDate());

        return dto;
    }
}
