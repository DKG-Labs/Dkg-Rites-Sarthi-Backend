package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailHydraulicPressRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailHydraulicPressResponseDto;
import com.sarthi.SRailPad.entity.ieVerification.RailHydraulicPress;
import com.sarthi.SRailPad.repository.ieVerification.RailHydraulicPressRepository;
import com.sarthi.SRailPad.service.ieVerification.RailHydraulicPressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailHydraulicPressServiceImpl implements RailHydraulicPressService {

    @Autowired
    private RailHydraulicPressRepository repository;

    @Override
    @Transactional
    public RailHydraulicPressResponseDto create(RailHydraulicPressRequestDto dto) {
        RailHydraulicPress entity = new RailHydraulicPress();
        mapDtoToEntity(dto, entity);
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());
        
        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    @Transactional
    public RailHydraulicPressResponseDto update(Long id, RailHydraulicPressRequestDto dto) {
        RailHydraulicPress entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hydraulic Press record not found with id: " + id));
        
        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        return buildResponse(entity);
    }

    @Override
    public RailHydraulicPressResponseDto getById(Long id) {
        RailHydraulicPress entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hydraulic Press record not found with id: " + id));
        return buildResponse(entity);
    }

    @Override
    public List<RailHydraulicPressResponseDto> getByShiftAndDate(String plantId, String shift, LocalDate castingDate) {
        return repository.findAllByPlantIdAndShiftAndCastingDate(plantId, shift, castingDate).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RailHydraulicPressResponseDto> getByPlant(String plantId, LocalDate fromDate, LocalDate toDate) {
        String cleanPlantId = (plantId != null && plantId.startsWith(":")) ? plantId.substring(1) : plantId;
        String colonPlantId = (plantId != null && !plantId.startsWith(":")) ? ":" + plantId : plantId;

        List<RailHydraulicPress> list = repository.findAllByPlantIdOrderByCreatedDateDesc(plantId);
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

    private void mapDtoToEntity(RailHydraulicPressRequestDto dto, RailHydraulicPress entity) {
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setCastingDate(dto.getCastingDate());
        entity.setRailPadType(dto.getRailPadType());
        entity.setBatchNo(dto.getBatchNo());
        entity.setTimeOfCheck(dto.getTimeOfCheck());
        entity.setCuringTime(dto.getCuringTime());
        entity.setCuringTemp(dto.getCuringTemp());
        entity.setCuringPressure(dto.getCuringPressure());
        entity.setStatus(dto.getStatus());
        entity.setTimestamp(dto.getTimestamp());
    }

    private RailHydraulicPressResponseDto buildResponse(RailHydraulicPress entity) {
        RailHydraulicPressResponseDto dto = new RailHydraulicPressResponseDto();
        dto.setId(entity.getId());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setCastingDate(entity.getCastingDate());
        dto.setRailPadType(entity.getRailPadType());
        dto.setBatchNo(entity.getBatchNo());
        dto.setTimeOfCheck(entity.getTimeOfCheck());
        dto.setCuringTime(entity.getCuringTime());
        dto.setCuringTemp(entity.getCuringTemp());
        dto.setCuringPressure(entity.getCuringPressure());
        dto.setStatus(entity.getStatus());
        dto.setTimestamp(entity.getTimestamp());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }
}
