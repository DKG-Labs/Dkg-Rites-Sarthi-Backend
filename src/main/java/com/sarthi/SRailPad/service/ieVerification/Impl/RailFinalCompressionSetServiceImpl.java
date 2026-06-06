package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalCompressionSetRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalCompressionSetResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalCompressionSet;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalCompressionSetRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalCompressionSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalCompressionSetServiceImpl implements RailFinalCompressionSetService {

    @Autowired
    private RailFinalCompressionSetRepository repository;

    @Override
    @Transactional
    public RailFinalCompressionSetResponseDto save(RailFinalCompressionSetRequestDto dto) {
        RailFinalCompressionSet entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalCompressionSet());

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
    public RailFinalCompressionSetResponseDto getById(Long id) {
        RailFinalCompressionSet entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Compression Set record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalCompressionSetResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalCompressionSet entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Compression Set record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalCompressionSetResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalCompressionSetRequestDto dto, RailFinalCompressionSet entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        // Initial Thickness (A) actual samples (1 to 3)
        entity.setSampleInitial1(dto.getSampleInitial1());
        entity.setSampleInitial2(dto.getSampleInitial2());
        entity.setSampleInitial3(dto.getSampleInitial3());

        // Initial Thickness (A) marginal samples (1 to 6)
        entity.setMarginalInitial1(dto.getMarginalInitial1());
        entity.setMarginalInitial2(dto.getMarginalInitial2());
        entity.setMarginalInitial3(dto.getMarginalInitial3());
        entity.setMarginalInitial4(dto.getMarginalInitial4());
        entity.setMarginalInitial5(dto.getMarginalInitial5());
        entity.setMarginalInitial6(dto.getMarginalInitial6());

        // Final Thickness (B) actual samples (1 to 3)
        entity.setSampleFinal1(dto.getSampleFinal1());
        entity.setSampleFinal2(dto.getSampleFinal2());
        entity.setSampleFinal3(dto.getSampleFinal3());

        // Final Thickness (B) marginal samples (1 to 6)
        entity.setMarginalFinal1(dto.getMarginalFinal1());
        entity.setMarginalFinal2(dto.getMarginalFinal2());
        entity.setMarginalFinal3(dto.getMarginalFinal3());
        entity.setMarginalFinal4(dto.getMarginalFinal4());
        entity.setMarginalFinal5(dto.getMarginalFinal5());
        entity.setMarginalFinal6(dto.getMarginalFinal6());

        entity.setCompressionStatus(dto.getCompressionStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalCompressionSetResponseDto buildResponse(RailFinalCompressionSet entity) {
        RailFinalCompressionSetResponseDto dto = new RailFinalCompressionSetResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        // Initial Thickness (A) actual samples (1 to 3)
        dto.setSampleInitial1(entity.getSampleInitial1());
        dto.setSampleInitial2(entity.getSampleInitial2());
        dto.setSampleInitial3(entity.getSampleInitial3());

        // Initial Thickness (A) marginal samples (1 to 6)
        dto.setMarginalInitial1(entity.getMarginalInitial1());
        dto.setMarginalInitial2(entity.getMarginalInitial2());
        dto.setMarginalInitial3(entity.getMarginalInitial3());
        dto.setMarginalInitial4(entity.getMarginalInitial4());
        dto.setMarginalInitial5(entity.getMarginalInitial5());
        dto.setMarginalInitial6(entity.getMarginalInitial6());

        // Final Thickness (B) actual samples (1 to 3)
        dto.setSampleFinal1(entity.getSampleFinal1());
        dto.setSampleFinal2(entity.getSampleFinal2());
        dto.setSampleFinal3(entity.getSampleFinal3());

        // Final Thickness (B) marginal samples (1 to 6)
        dto.setMarginalFinal1(entity.getMarginalFinal1());
        dto.setMarginalFinal2(entity.getMarginalFinal2());
        dto.setMarginalFinal3(entity.getMarginalFinal3());
        dto.setMarginalFinal4(entity.getMarginalFinal4());
        dto.setMarginalFinal5(entity.getMarginalFinal5());
        dto.setMarginalFinal6(entity.getMarginalFinal6());

        dto.setCompressionStatus(entity.getCompressionStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
