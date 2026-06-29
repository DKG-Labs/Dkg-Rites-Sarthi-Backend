package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.RawMaterialConsumptionDto;
import com.sarthi.Sleeper.entity.RawMaterialConsumption;
import com.sarthi.Sleeper.repository.RawMaterialConsumptionRepository;
import com.sarthi.Sleeper.service.RawMaterialConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RawMaterialConsumptionServiceImpl implements RawMaterialConsumptionService {

    @Autowired
    private RawMaterialConsumptionRepository repository;

    @Override
    public RawMaterialConsumptionDto saveConsumption(RawMaterialConsumptionDto dto) {
        RawMaterialConsumption entity = new RawMaterialConsumption();
        if (dto.getNumericId() != null) {
            entity = repository.findById(dto.getNumericId()).orElse(new RawMaterialConsumption());
        } else if (dto.getId() != null && dto.getId().startsWith("USED-") && dto.getId().contains("-ID-")) {
            // Frontend might send ID in format USED-HTS-ID-1
            try {
                String[] parts = dto.getId().split("-ID-");
                if (parts.length > 1) {
                    Long parsedId = Long.parseLong(parts[1]);
                    entity = repository.findById(parsedId).orElse(new RawMaterialConsumption());
                }
            } catch (Exception ignored) {}
        }
        
        entity.setDateOfUse(dto.getDate());
        entity.setRawMaterial(dto.getRawMaterial());
        entity.setSubType(dto.getSubType());
        entity.setUsedFor(dto.getUsedFor());
        entity.setSleepersMade(dto.getSleepersMade());
        entity.setEstimatedQty(dto.getEstimatedQty());
        entity.setActualQty(dto.getQty());
        entity.setStatus(dto.getStatus());
        entity.setPlantId(dto.getPlantId());
        
        if (dto.getVendorCode() != null) {
            entity.setVendorCode(dto.getVendorCode());
        }
        if (dto.getCreatedBy() != null) {
            entity.setCreatedBy(Long.valueOf(dto.getCreatedBy()));
        }
        if (dto.getUpdatedBy() != null) {
            entity.setUpdatedBy(Long.valueOf(dto.getUpdatedBy()));
        }
        
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(LocalDateTime.now());
        }
        entity.setUpdatedDate(LocalDateTime.now());

        RawMaterialConsumption saved = repository.save(entity);
        return mapToDto(saved);
    }

    @Override
    public RawMaterialConsumptionDto getConsumptionById(Long id) {
        return repository.findById(id).map(this::mapToDto).orElse(null);
    }

    @Override
    public Page<RawMaterialConsumptionDto> getAllConsumptionByPlantAndMaterial(String plantId, String rawMaterial, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return repository.findByPlantIdAndRawMaterial(plantId, rawMaterial, pageable)
                .map(this::mapToDto);
    }

    @Override
    public Page<RawMaterialConsumptionDto> getAllConsumptionByPlant(String plantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return repository.findByPlantId(plantId, pageable)
                .map(this::mapToDto);
    }

    @Override
    public void deleteConsumption(Long id) {
        repository.deleteById(id);
    }

    private RawMaterialConsumptionDto mapToDto(RawMaterialConsumption entity) {
        String frontendId = "USED-" + (entity.getRawMaterial() != null ? entity.getRawMaterial().toUpperCase().substring(0, Math.min(entity.getRawMaterial().length(), 3)) : "GEN") + "-ID-" + entity.getId();
        return RawMaterialConsumptionDto.builder()
                .id(frontendId)
                .numericId(entity.getId())
                .date(entity.getDateOfUse())
                .rawMaterial(entity.getRawMaterial())
                .subType(entity.getSubType())
                .usedFor(entity.getUsedFor())
                .sleepersMade(entity.getSleepersMade())
                .estimatedQty(entity.getEstimatedQty())
                .qty(entity.getActualQty())
                .status(entity.getStatus())
                .plantId(entity.getPlantId())
                .vendorCode(entity.getVendorCode())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().intValue() : null)
                .updatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy().intValue() : null)
                .build();
    }
}
