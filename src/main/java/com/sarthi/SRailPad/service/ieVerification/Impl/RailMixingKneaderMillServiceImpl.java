package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailMixingKneaderMillRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailMixingKneaderMillResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailMixingKneaderMill;
import com.sarthi.SRailPad.repository.ieVerification.RailMixingKneaderMillRepository;
import com.sarthi.SRailPad.service.ieVerification.RailMixingKneaderMillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailMixingKneaderMillServiceImpl implements RailMixingKneaderMillService {

    @Autowired
    private RailMixingKneaderMillRepository repository;

    @Override
    @Transactional
    public RailMixingKneaderMillResponseDto create(RailMixingKneaderMillRequestDto dto) {
        RailMixingKneaderMill entity = new RailMixingKneaderMill();
        mapDtoToEntity(dto, entity);
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());
        
        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    @Transactional
    public RailMixingKneaderMillResponseDto update(Long id, RailMixingKneaderMillRequestDto dto) {
        RailMixingKneaderMill entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mixing Kneader Mill record not found with id: " + id));
        
        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    public RailMixingKneaderMillResponseDto getById(Long id) {
        RailMixingKneaderMill entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mixing Kneader Mill record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public List<RailMixingKneaderMillResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate castingDate) {
        return repository.findAllByPlantIdAndShiftAndCastingDate(plantId, shift, castingDate).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RailMixingKneaderMillResponseDto> getByPlant(String plantId, LocalDate fromDate, LocalDate toDate) {
        String cleanPlantId = (plantId != null && plantId.startsWith(":")) ? plantId.substring(1) : plantId;
        String colonPlantId = (plantId != null && !plantId.startsWith(":")) ? ":" + plantId : plantId;

        List<RailMixingKneaderMill> list = repository.findAllByPlantIdOrderByCreatedDateDesc(plantId);
        if (list.isEmpty() && cleanPlantId != null && !cleanPlantId.equals(plantId)) {
            list = repository.findAllByPlantIdOrderByCreatedDateDesc(cleanPlantId);
        } else if (list.isEmpty() && colonPlantId != null && !colonPlantId.equals(plantId)) {
            list = repository.findAllByPlantIdOrderByCreatedDateDesc(colonPlantId);
        }

        return list.stream()
                .filter(e -> {
                    LocalDate d = e.getCastingDate() != null ? e.getCastingDate()
                                : (e.getCreatedDate() != null ? e.getCreatedDate().toLocalDate() : null);
                    if (d == null) return true;
                    if (fromDate != null && d.isBefore(fromDate)) return false;
                    if (toDate != null && d.isAfter(toDate)) return false;
                    return true;
                })
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(RailMixingKneaderMillRequestDto dto, RailMixingKneaderMill entity) {
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setCastingDate(dto.getCastingDate());
        entity.setRailPadType(dto.getRailPadType());
        entity.setBatchNo(dto.getBatchNo());
        entity.setMixingTime(dto.getMixingTime());
        entity.setMixingTemp(dto.getMixingTemp());
        entity.setWaterCirculation(dto.getWaterCirculation());
        entity.setDustCollector(dto.getDustCollector());
        entity.setStatus(dto.getStatus());
        entity.setTimestamp(dto.getTimestamp());
    }

    private RailMixingKneaderMillResponseDto buildResponse(RailMixingKneaderMill entity) {
        RailMixingKneaderMillResponseDto dto = new RailMixingKneaderMillResponseDto();
        dto.setId(entity.getId());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setCastingDate(entity.getCastingDate());
        dto.setRailPadType(entity.getRailPadType());
        dto.setBatchNo(entity.getBatchNo());
        dto.setMixingTime(entity.getMixingTime());
        dto.setMixingTemp(entity.getMixingTemp());
        dto.setWaterCirculation(entity.getWaterCirculation());
        dto.setDustCollector(entity.getDustCollector());
        dto.setStatus(entity.getStatus());
        dto.setTimestamp(entity.getTimestamp());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
