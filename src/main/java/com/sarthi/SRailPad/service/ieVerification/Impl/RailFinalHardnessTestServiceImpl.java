package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalHardnessTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalHardnessTestResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalHardnessTest;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalHardnessTestRepository;
import com.sarthi.SRailPad.service.ieVerification.RailFinalHardnessTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailFinalHardnessTestServiceImpl implements RailFinalHardnessTestService {

    @Autowired
    private RailFinalHardnessTestRepository repository;

    @Override
    @Transactional
    public RailFinalHardnessTestResponseDto save(RailFinalHardnessTestRequestDto dto) {
        RailFinalHardnessTest entity = repository.findByCallNoAndLotNo(dto.getCallNo(), dto.getLotNo())
                .orElse(new RailFinalHardnessTest());

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
    public RailFinalHardnessTestResponseDto getById(Long id) {
        RailFinalHardnessTest entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final Hardness Test record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public RailFinalHardnessTestResponseDto getByCallNoAndLotNo(String callNo, String lotNo) {
        RailFinalHardnessTest entity = repository.findByCallNoAndLotNo(callNo, lotNo)
                .orElseThrow(() -> new RuntimeException("Final Hardness Test record not found for call: " + callNo + ", lot: " + lotNo));
        return buildResponse(entity);
    }

    @Override
    public List<RailFinalHardnessTestResponseDto> getByCallNo(String callNo) {
        return repository.findAllByCallNo(callNo).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailFinalHardnessTestRequestDto dto, RailFinalHardnessTest entity) {
        entity.setCallNo(dto.getCallNo());
        entity.setLotNo(dto.getLotNo());
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setRailpadType(dto.getRailpadType());
        entity.setOfferedQty(dto.getOfferedQty());
        entity.setDateOfShift(dto.getDateOfShift());

        // Compound A samples
        entity.setSampleA1(dto.getSampleA1());
        entity.setSampleA2(dto.getSampleA2());
        entity.setSampleA3(dto.getSampleA3());
        entity.setSampleA4(dto.getSampleA4());
        entity.setSampleA5(dto.getSampleA5());

        // Compound A marginal samples
        entity.setMarginalA1(dto.getMarginalA1());
        entity.setMarginalA2(dto.getMarginalA2());
        entity.setMarginalA3(dto.getMarginalA3());
        entity.setMarginalA4(dto.getMarginalA4());
        entity.setMarginalA5(dto.getMarginalA5());
        entity.setMarginalA6(dto.getMarginalA6());
        entity.setMarginalA7(dto.getMarginalA7());
        entity.setMarginalA8(dto.getMarginalA8());
        entity.setMarginalA9(dto.getMarginalA9());
        entity.setMarginalA10(dto.getMarginalA10());

        // Compound B samples
        entity.setSampleB1(dto.getSampleB1());
        entity.setSampleB2(dto.getSampleB2());
        entity.setSampleB3(dto.getSampleB3());
        entity.setSampleB4(dto.getSampleB4());
        entity.setSampleB5(dto.getSampleB5());

        // Compound B marginal samples
        entity.setMarginalB1(dto.getMarginalB1());
        entity.setMarginalB2(dto.getMarginalB2());
        entity.setMarginalB3(dto.getMarginalB3());
        entity.setMarginalB4(dto.getMarginalB4());
        entity.setMarginalB5(dto.getMarginalB5());
        entity.setMarginalB6(dto.getMarginalB6());
        entity.setMarginalB7(dto.getMarginalB7());
        entity.setMarginalB8(dto.getMarginalB8());
        entity.setMarginalB9(dto.getMarginalB9());
        entity.setMarginalB10(dto.getMarginalB10());

        entity.setHardnessStatus(dto.getHardnessStatus());
        entity.setNotOkCount(dto.getNotOkCount());
        entity.setRemarks(dto.getRemarks());
    }

    private RailFinalHardnessTestResponseDto buildResponse(RailFinalHardnessTest entity) {
        RailFinalHardnessTestResponseDto dto = new RailFinalHardnessTestResponseDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setLotNo(entity.getLotNo());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setRailpadType(entity.getRailpadType());
        dto.setOfferedQty(entity.getOfferedQty());
        dto.setDateOfShift(entity.getDateOfShift());

        // Compound A samples
        dto.setSampleA1(entity.getSampleA1());
        dto.setSampleA2(entity.getSampleA2());
        dto.setSampleA3(entity.getSampleA3());
        dto.setSampleA4(entity.getSampleA4());
        dto.setSampleA5(entity.getSampleA5());

        // Compound A marginal samples
        dto.setMarginalA1(entity.getMarginalA1());
        dto.setMarginalA2(entity.getMarginalA2());
        dto.setMarginalA3(entity.getMarginalA3());
        dto.setMarginalA4(entity.getMarginalA4());
        dto.setMarginalA5(entity.getMarginalA5());
        dto.setMarginalA6(entity.getMarginalA6());
        dto.setMarginalA7(entity.getMarginalA7());
        dto.setMarginalA8(entity.getMarginalA8());
        dto.setMarginalA9(entity.getMarginalA9());
        dto.setMarginalA10(entity.getMarginalA10());

        // Compound B samples
        dto.setSampleB1(entity.getSampleB1());
        dto.setSampleB2(entity.getSampleB2());
        dto.setSampleB3(entity.getSampleB3());
        dto.setSampleB4(entity.getSampleB4());
        dto.setSampleB5(entity.getSampleB5());

        // Compound B marginal samples
        dto.setMarginalB1(entity.getMarginalB1());
        dto.setMarginalB2(entity.getMarginalB2());
        dto.setMarginalB3(entity.getMarginalB3());
        dto.setMarginalB4(entity.getMarginalB4());
        dto.setMarginalB5(entity.getMarginalB5());
        dto.setMarginalB6(entity.getMarginalB6());
        dto.setMarginalB7(entity.getMarginalB7());
        dto.setMarginalB8(entity.getMarginalB8());
        dto.setMarginalB9(entity.getMarginalB9());
        dto.setMarginalB10(entity.getMarginalB10());

        dto.setHardnessStatus(entity.getHardnessStatus());
        dto.setNotOkCount(entity.getNotOkCount());
        dto.setRemarks(entity.getRemarks());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
