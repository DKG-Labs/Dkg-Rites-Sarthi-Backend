package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalAshContentRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalAshContentResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalAshContent;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalAshContentRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalAshContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalAshContentServiceImpl implements RailFinalAshContentService {

    @Autowired
    private RailFinalAshContentRepository repository;

    @Override
    @Transactional
    public RailFinalAshContentResponseDto save(RailFinalAshContentRequestDto dto) {
        RailFinalAshContent entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalAshContent());

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
    public RailFinalAshContentResponseDto getById(Long id) {
        RailFinalAshContent entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RailFinalAshContent record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalAshContentResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalAshContent entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("RailFinalAshContent record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalAshContentResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalAshContentRequestDto dto, RailFinalAshContent entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        entity.setS1ACrucible(dto.getS1ACrucible());
        entity.setS2ACrucible(dto.getS2ACrucible());
        entity.setS3ACrucible(dto.getS3ACrucible());
        entity.setS1BCrucible(dto.getS1BCrucible());
        entity.setS2BCrucible(dto.getS2BCrucible());
        entity.setS3BCrucible(dto.getS3BCrucible());
        entity.setS1ASample(dto.getS1ASample());
        entity.setS2ASample(dto.getS2ASample());
        entity.setS3ASample(dto.getS3ASample());
        entity.setS1BSample(dto.getS1BSample());
        entity.setS2BSample(dto.getS2BSample());
        entity.setS3BSample(dto.getS3BSample());
        entity.setS1AAsh(dto.getS1AAsh());
        entity.setS2AAsh(dto.getS2AAsh());
        entity.setS3AAsh(dto.getS3AAsh());
        entity.setS1BAsh(dto.getS1BAsh());
        entity.setS2BAsh(dto.getS2BAsh());
        entity.setS3BAsh(dto.getS3BAsh());
        entity.setM1ACrucible(dto.getM1ACrucible());
        entity.setM2ACrucible(dto.getM2ACrucible());
        entity.setM3ACrucible(dto.getM3ACrucible());
        entity.setM4ACrucible(dto.getM4ACrucible());
        entity.setM5ACrucible(dto.getM5ACrucible());
        entity.setM6ACrucible(dto.getM6ACrucible());
        entity.setM1BCrucible(dto.getM1BCrucible());
        entity.setM2BCrucible(dto.getM2BCrucible());
        entity.setM3BCrucible(dto.getM3BCrucible());
        entity.setM4BCrucible(dto.getM4BCrucible());
        entity.setM5BCrucible(dto.getM5BCrucible());
        entity.setM6BCrucible(dto.getM6BCrucible());
        entity.setM1ASample(dto.getM1ASample());
        entity.setM2ASample(dto.getM2ASample());
        entity.setM3ASample(dto.getM3ASample());
        entity.setM4ASample(dto.getM4ASample());
        entity.setM5ASample(dto.getM5ASample());
        entity.setM6ASample(dto.getM6ASample());
        entity.setM1BSample(dto.getM1BSample());
        entity.setM2BSample(dto.getM2BSample());
        entity.setM3BSample(dto.getM3BSample());
        entity.setM4BSample(dto.getM4BSample());
        entity.setM5BSample(dto.getM5BSample());
        entity.setM6BSample(dto.getM6BSample());
        entity.setM1AAsh(dto.getM1AAsh());
        entity.setM2AAsh(dto.getM2AAsh());
        entity.setM3AAsh(dto.getM3AAsh());
        entity.setM4AAsh(dto.getM4AAsh());
        entity.setM5AAsh(dto.getM5AAsh());
        entity.setM6AAsh(dto.getM6AAsh());
        entity.setM1BAsh(dto.getM1BAsh());
        entity.setM2BAsh(dto.getM2BAsh());
        entity.setM3BAsh(dto.getM3BAsh());
        entity.setM4BAsh(dto.getM4BAsh());
        entity.setM5BAsh(dto.getM5BAsh());
        entity.setM6BAsh(dto.getM6BAsh());

        entity.setAshStatus(dto.getAshStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalAshContentResponseDto buildResponse(RailFinalAshContent entity) {
        RailFinalAshContentResponseDto dto = new RailFinalAshContentResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        dto.setS1ACrucible(entity.getS1ACrucible());
        dto.setS2ACrucible(entity.getS2ACrucible());
        dto.setS3ACrucible(entity.getS3ACrucible());
        dto.setS1BCrucible(entity.getS1BCrucible());
        dto.setS2BCrucible(entity.getS2BCrucible());
        dto.setS3BCrucible(entity.getS3BCrucible());
        dto.setS1ASample(entity.getS1ASample());
        dto.setS2ASample(entity.getS2ASample());
        dto.setS3ASample(entity.getS3ASample());
        dto.setS1BSample(entity.getS1BSample());
        dto.setS2BSample(entity.getS2BSample());
        dto.setS3BSample(entity.getS3BSample());
        dto.setS1AAsh(entity.getS1AAsh());
        dto.setS2AAsh(entity.getS2AAsh());
        dto.setS3AAsh(entity.getS3AAsh());
        dto.setS1BAsh(entity.getS1BAsh());
        dto.setS2BAsh(entity.getS2BAsh());
        dto.setS3BAsh(entity.getS3BAsh());
        dto.setM1ACrucible(entity.getM1ACrucible());
        dto.setM2ACrucible(entity.getM2ACrucible());
        dto.setM3ACrucible(entity.getM3ACrucible());
        dto.setM4ACrucible(entity.getM4ACrucible());
        dto.setM5ACrucible(entity.getM5ACrucible());
        dto.setM6ACrucible(entity.getM6ACrucible());
        dto.setM1BCrucible(entity.getM1BCrucible());
        dto.setM2BCrucible(entity.getM2BCrucible());
        dto.setM3BCrucible(entity.getM3BCrucible());
        dto.setM4BCrucible(entity.getM4BCrucible());
        dto.setM5BCrucible(entity.getM5BCrucible());
        dto.setM6BCrucible(entity.getM6BCrucible());
        dto.setM1ASample(entity.getM1ASample());
        dto.setM2ASample(entity.getM2ASample());
        dto.setM3ASample(entity.getM3ASample());
        dto.setM4ASample(entity.getM4ASample());
        dto.setM5ASample(entity.getM5ASample());
        dto.setM6ASample(entity.getM6ASample());
        dto.setM1BSample(entity.getM1BSample());
        dto.setM2BSample(entity.getM2BSample());
        dto.setM3BSample(entity.getM3BSample());
        dto.setM4BSample(entity.getM4BSample());
        dto.setM5BSample(entity.getM5BSample());
        dto.setM6BSample(entity.getM6BSample());
        dto.setM1AAsh(entity.getM1AAsh());
        dto.setM2AAsh(entity.getM2AAsh());
        dto.setM3AAsh(entity.getM3AAsh());
        dto.setM4AAsh(entity.getM4AAsh());
        dto.setM5AAsh(entity.getM5AAsh());
        dto.setM6AAsh(entity.getM6AAsh());
        dto.setM1BAsh(entity.getM1BAsh());
        dto.setM2BAsh(entity.getM2BAsh());
        dto.setM3BAsh(entity.getM3BAsh());
        dto.setM4BAsh(entity.getM4BAsh());
        dto.setM5BAsh(entity.getM5BAsh());
        dto.setM6BAsh(entity.getM6BAsh());

        dto.setAshStatus(entity.getAshStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
