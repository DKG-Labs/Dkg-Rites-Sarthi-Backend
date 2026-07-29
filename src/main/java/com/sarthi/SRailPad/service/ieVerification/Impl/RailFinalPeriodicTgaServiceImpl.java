package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicTgaRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicTgaResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalPeriodicTga;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalPeriodicTgaRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalPeriodicTgaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalPeriodicTgaServiceImpl implements RailFinalPeriodicTgaService {

    @Autowired
    private RailFinalPeriodicTgaRepository repository;

    @Override
    @Transactional
    public RailFinalPeriodicTgaResponseDto save(RailFinalPeriodicTgaRequestDto dto) {
        RailFinalPeriodicTga entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalPeriodicTga());

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
    public RailFinalPeriodicTgaResponseDto getById(Long id) {
        RailFinalPeriodicTga entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Periodic TGA record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalPeriodicTgaResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalPeriodicTga entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Periodic TGA record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalPeriodicTgaResponseDto> getByCallNo(String callNo) {
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

    private void mapDtoToEntity(RailFinalPeriodicTgaRequestDto dto, RailFinalPeriodicTga entity) {
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
        entity.setS1SampleWt(dto.getS1SampleWt());
        entity.setS1TempRange(dto.getS1TempRange());
        entity.setS1PolymerContent(dto.getS1PolymerContent());

        entity.setS2LotNo(dto.getS2LotNo());
        entity.setS2SampleNo(dto.getS2SampleNo());
        entity.setS2SampleWt(dto.getS2SampleWt());
        entity.setS2TempRange(dto.getS2TempRange());
        entity.setS2PolymerContent(dto.getS2PolymerContent());

        entity.setS3LotNo(dto.getS3LotNo());
        entity.setS3SampleNo(dto.getS3SampleNo());
        entity.setS3SampleWt(dto.getS3SampleWt());
        entity.setS3TempRange(dto.getS3TempRange());
        entity.setS3PolymerContent(dto.getS3PolymerContent());

        entity.setS4LotNo(dto.getS4LotNo());
        entity.setS4SampleNo(dto.getS4SampleNo());
        entity.setS4SampleWt(dto.getS4SampleWt());
        entity.setS4TempRange(dto.getS4TempRange());
        entity.setS4PolymerContent(dto.getS4PolymerContent());

        entity.setS5LotNo(dto.getS5LotNo());
        entity.setS5SampleNo(dto.getS5SampleNo());
        entity.setS5SampleWt(dto.getS5SampleWt());
        entity.setS5TempRange(dto.getS5TempRange());
        entity.setS5PolymerContent(dto.getS5PolymerContent());

        entity.setTgaStatus(dto.getTgaStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalPeriodicTgaResponseDto buildResponse(RailFinalPeriodicTga entity) {
        RailFinalPeriodicTgaResponseDto dto = new RailFinalPeriodicTgaResponseDto();
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
        dto.setS1SampleWt(entity.getS1SampleWt());
        dto.setS1TempRange(entity.getS1TempRange());
        dto.setS1PolymerContent(entity.getS1PolymerContent());

        dto.setS2LotNo(entity.getS2LotNo());
        dto.setS2SampleNo(entity.getS2SampleNo());
        dto.setS2SampleWt(entity.getS2SampleWt());
        dto.setS2TempRange(entity.getS2TempRange());
        dto.setS2PolymerContent(entity.getS2PolymerContent());

        dto.setS3LotNo(entity.getS3LotNo());
        dto.setS3SampleNo(entity.getS3SampleNo());
        dto.setS3SampleWt(entity.getS3SampleWt());
        dto.setS3TempRange(entity.getS3TempRange());
        dto.setS3PolymerContent(entity.getS3PolymerContent());

        dto.setS4LotNo(entity.getS4LotNo());
        dto.setS4SampleNo(entity.getS4SampleNo());
        dto.setS4SampleWt(entity.getS4SampleWt());
        dto.setS4TempRange(entity.getS4TempRange());
        dto.setS4PolymerContent(entity.getS4PolymerContent());

        dto.setS5LotNo(entity.getS5LotNo());
        dto.setS5SampleNo(entity.getS5SampleNo());
        dto.setS5SampleWt(entity.getS5SampleWt());
        dto.setS5TempRange(entity.getS5TempRange());
        dto.setS5PolymerContent(entity.getS5PolymerContent());

        dto.setTgaStatus(entity.getTgaStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedOn(entity.getCreatedDate());
        dto.setUpdatedOn(entity.getUpdatedDate());

        return dto;
    }
}
