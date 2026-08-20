package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicAbrasionRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicAbrasionResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalPeriodicAbrasion;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalPeriodicAbrasionRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalPeriodicAbrasionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalPeriodicAbrasionServiceImpl implements RailFinalPeriodicAbrasionService {

    @Autowired
    private RailFinalPeriodicAbrasionRepository repository;

    @Override
    @Transactional
    public RailFinalPeriodicAbrasionResponseDto save(RailFinalPeriodicAbrasionRequestDto dto) {
        RailFinalPeriodicAbrasion entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalPeriodicAbrasion());

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
    public RailFinalPeriodicAbrasionResponseDto getById(Long id) {
        RailFinalPeriodicAbrasion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Periodic Abrasion record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalPeriodicAbrasionResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalPeriodicAbrasion entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Periodic Abrasion record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalPeriodicAbrasionResponseDto> getByCallNo(String callNo) {
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

    private void mapDtoToEntity(RailFinalPeriodicAbrasionRequestDto dto, RailFinalPeriodicAbrasion entity) {
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
        entity.setS1SampleNo(dto.getS1SampleNo());
        entity.setS1InitialMass(dto.getS1InitialMass());
        entity.setS1FinalMass(dto.getS1FinalMass());
        entity.setS1LossOfMass(dto.getS1LossOfMass());
        entity.setS1RelativeLoss(dto.getS1RelativeLoss());

        entity.setS2LotNo(dto.getS2LotNo());
        entity.setS2SampleNo(dto.getS2SampleNo());
        entity.setS2InitialMass(dto.getS2InitialMass());
        entity.setS2FinalMass(dto.getS2FinalMass());
        entity.setS2LossOfMass(dto.getS2LossOfMass());
        entity.setS2RelativeLoss(dto.getS2RelativeLoss());

        entity.setS3LotNo(dto.getS3LotNo());
        entity.setS3SampleNo(dto.getS3SampleNo());
        entity.setS3InitialMass(dto.getS3InitialMass());
        entity.setS3FinalMass(dto.getS3FinalMass());
        entity.setS3LossOfMass(dto.getS3LossOfMass());
        entity.setS3RelativeLoss(dto.getS3RelativeLoss());

        entity.setS4LotNo(dto.getS4LotNo());
        entity.setS4SampleNo(dto.getS4SampleNo());
        entity.setS4InitialMass(dto.getS4InitialMass());
        entity.setS4FinalMass(dto.getS4FinalMass());
        entity.setS4LossOfMass(dto.getS4LossOfMass());
        entity.setS4RelativeLoss(dto.getS4RelativeLoss());

        entity.setS5LotNo(dto.getS5LotNo());
        entity.setS5SampleNo(dto.getS5SampleNo());
        entity.setS5InitialMass(dto.getS5InitialMass());
        entity.setS5FinalMass(dto.getS5FinalMass());
        entity.setS5LossOfMass(dto.getS5LossOfMass());
        entity.setS5RelativeLoss(dto.getS5RelativeLoss());

        entity.setAbrasionStatus(dto.getAbrasionStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalPeriodicAbrasionResponseDto buildResponse(RailFinalPeriodicAbrasion entity) {
        RailFinalPeriodicAbrasionResponseDto dto = new RailFinalPeriodicAbrasionResponseDto();
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
        dto.setS1SampleNo(entity.getS1SampleNo());
        dto.setS1InitialMass(entity.getS1InitialMass());
        dto.setS1FinalMass(entity.getS1FinalMass());
        dto.setS1LossOfMass(entity.getS1LossOfMass());
        dto.setS1RelativeLoss(entity.getS1RelativeLoss());

        dto.setS2LotNo(entity.getS2LotNo());
        dto.setS2SampleNo(entity.getS2SampleNo());
        dto.setS2InitialMass(entity.getS2InitialMass());
        dto.setS2FinalMass(entity.getS2FinalMass());
        dto.setS2LossOfMass(entity.getS2LossOfMass());
        dto.setS2RelativeLoss(entity.getS2RelativeLoss());

        dto.setS3LotNo(entity.getS3LotNo());
        dto.setS3SampleNo(entity.getS3SampleNo());
        dto.setS3InitialMass(entity.getS3InitialMass());
        dto.setS3FinalMass(entity.getS3FinalMass());
        dto.setS3LossOfMass(entity.getS3LossOfMass());
        dto.setS3RelativeLoss(entity.getS3RelativeLoss());

        dto.setS4LotNo(entity.getS4LotNo());
        dto.setS4SampleNo(entity.getS4SampleNo());
        dto.setS4InitialMass(entity.getS4InitialMass());
        dto.setS4FinalMass(entity.getS4FinalMass());
        dto.setS4LossOfMass(entity.getS4LossOfMass());
        dto.setS4RelativeLoss(entity.getS4RelativeLoss());

        dto.setS5LotNo(entity.getS5LotNo());
        dto.setS5SampleNo(entity.getS5SampleNo());
        dto.setS5InitialMass(entity.getS5InitialMass());
        dto.setS5FinalMass(entity.getS5FinalMass());
        dto.setS5LossOfMass(entity.getS5LossOfMass());
        dto.setS5RelativeLoss(entity.getS5RelativeLoss());

        dto.setAbrasionStatus(entity.getAbrasionStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedOn(entity.getCreatedDate());
        dto.setUpdatedOn(entity.getUpdatedDate());

        return dto;
    }
}
