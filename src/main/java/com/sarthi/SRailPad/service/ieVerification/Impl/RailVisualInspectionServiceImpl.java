package com.sarthi.SRailPad.service.ieVerification.Impl;

import com.sarthi.SRailPad.dto.ieVerification.RailVisualInspectionDto;
import com.sarthi.SRailPad.entity.ieVerification.RailVisualInspection;
import com.sarthi.SRailPad.repository.ieVerification.RailVisualInspectionRepository;
import com.sarthi.SRailPad.service.ieVerification.RailVisualInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailVisualInspectionServiceImpl implements RailVisualInspectionService {

    @Autowired
    private RailVisualInspectionRepository repository;

    @Override
    public RailVisualInspectionDto create(RailVisualInspectionDto dto) {
        try {
            RailVisualInspection entity = new RailVisualInspection();
            mapDtoToEntity(dto, entity);
            RailVisualInspection saved = repository.save(entity);
            return mapEntityToDto(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save visual inspection: " + e.getMessage());
        }
    }

    @Override
    public RailVisualInspectionDto update(Long id, RailVisualInspectionDto dto) {
        try {
            RailVisualInspection entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Visual Inspection not found with id: " + id));
            mapDtoToEntity(dto, entity);
            RailVisualInspection updated = repository.save(entity);
            return mapEntityToDto(updated);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update visual inspection: " + e.getMessage());
        }
    }

    @Override
    public List<RailVisualInspectionDto> getList(String plantId, String vendorCode) {
        try {
            return repository.findByPlantIdAndVendorCodeOrderByTimestampDesc(plantId, vendorCode)
                    .stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch visual inspections: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try {
            if (!repository.existsById(id)) {
                throw new RuntimeException("Visual Inspection not found with id: " + id);
            }
            repository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete visual inspection: " + e.getMessage());
        }
    }

    private void mapDtoToEntity(RailVisualInspectionDto dto, RailVisualInspection entity) {
        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());
        entity.setShift(dto.getShift());
        entity.setCastingDate(dto.getCastingDate());
        entity.setTimeOfCheck(dto.getTimeOfCheck());
        entity.setSampleQuantity(dto.getSampleQuantity());
        entity.setClearCutSides(dto.getClearCutSides());
        entity.setSmoothSurface(dto.getSmoothSurface());
        entity.setDefectRemarks(dto.getDefectRemarks());
        entity.setStatus(dto.getStatus());
        entity.setTimestamp(dto.getTimestamp());
    }

    private RailVisualInspectionDto mapEntityToDto(RailVisualInspection entity) {
        RailVisualInspectionDto dto = new RailVisualInspectionDto();
        dto.setId(entity.getId());
        dto.setPlantId(entity.getPlantId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setShift(entity.getShift());
        dto.setCastingDate(entity.getCastingDate());
        dto.setTimeOfCheck(entity.getTimeOfCheck());
        dto.setSampleQuantity(entity.getSampleQuantity());
        dto.setClearCutSides(entity.getClearCutSides());
        dto.setSmoothSurface(entity.getSmoothSurface());
        dto.setDefectRemarks(entity.getDefectRemarks());
        dto.setStatus(entity.getStatus());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }
}
