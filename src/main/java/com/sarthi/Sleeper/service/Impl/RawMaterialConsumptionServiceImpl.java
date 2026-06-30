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

    @Autowired
    private com.sarthi.Sleeper.repository.SleeperWorkflowRepository workflowRepository;

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
    public Page<RawMaterialConsumptionDto> getAllConsumptionByPlantAndMaterial(String plantId, String rawMaterial, List<String> statuses, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        if (statuses != null && !statuses.isEmpty()) {
            return repository.findByPlantIdAndRawMaterialAndStatusIn(plantId, rawMaterial, statuses, pageable)
                    .map(this::mapToDto);
        }
        return repository.findByPlantIdAndRawMaterial(plantId, rawMaterial, pageable)
                .map(this::mapToDto);
    }

    @Override
    public List<RawMaterialConsumptionDto> getAllVerifiedConsumptionByPlantAndMaterial(String plantId, String rawMaterial) {
        List<String> verifiedStatuses = java.util.Arrays.asList("Completed", "Verified", "Locked");
        return repository.findByPlantIdAndRawMaterialAndStatusIn(plantId, rawMaterial, verifiedStatuses)
                .stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList());
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

    private Long getModuleIdForMaterial(String materialName) {
        if (materialName == null) return 13L; // Default fallback
        switch (materialName.toLowerCase()) {
            case "hts wire":
            case "hts-wire":
                return 13L; // HTS RM Consumption Records
            case "cement":
                return 14L; // CEMENT RM Consumption Records
            case "admixture":
                return 15L; // ADMIXTURE RM Consumption Records
            case "aggregates":
            case "aggregate":
                return 16L; // AGGREGATE RM Consumption Records
            case "sgci insert":
            case "sgci-insert":
                return 17L; // SGCI Insert RM Consumption Records
            case "dowel":
                return 18L; // DOWEL RM Consumption Records
            default:
                return 13L; // Default to HTS if unknown for now
        }
    }

    private RawMaterialConsumptionDto mapToDto(RawMaterialConsumption entity) {
        String frontendId = "USED-" + (entity.getRawMaterial() != null ? entity.getRawMaterial().toUpperCase().substring(0, Math.min(entity.getRawMaterial().length(), 3)) : "GEN") + "-ID-" + entity.getId();
        
        String wfStatus = null;
        String wfRemarks = null;
        if (entity.getId() != null) {
            try {
                Long expectedModuleId = getModuleIdForMaterial(entity.getRawMaterial());
                List<com.sarthi.Sleeper.entity.SleeperWorkflowTransaction> transactions = workflowRepository.findByRequestIdOrderByCreatedDateAsc(String.valueOf(entity.getId()));
                if (transactions != null && !transactions.isEmpty()) {
                    com.sarthi.Sleeper.entity.SleeperWorkflowTransaction latest = transactions.stream()
                        .filter(t -> t.getModuleId() != null && t.getModuleId().equals(expectedModuleId))
                        .reduce((first, second) -> second)
                        .orElse(null);
                    if (latest != null) {
                        wfStatus = latest.getStatus();
                        wfRemarks = latest.getRemarks();
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

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
                .workflowStatus(wfStatus)
                .workflowRemarks(wfRemarks)
                .build();
    }
}
