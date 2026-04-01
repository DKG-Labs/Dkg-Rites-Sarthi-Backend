package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalVisualInspectionDto;
import com.sarthi.entity.finalmaterial.FinalVisualInspection;
import com.sarthi.repository.finalmaterial.FinalVisualInspectionRepository;
import com.sarthi.service.finalmaterial.FinalVisualInspectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Final Visual Inspection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinalVisualInspectionServiceImpl implements FinalVisualInspectionService {

    private final FinalVisualInspectionRepository repository;

    @Override
    @Transactional
    public FinalVisualInspection saveVisualInspection(FinalVisualInspectionDto dto, String userId) {
        log.info("Saving visual inspection for call: {} lot: {} heat: {}",
                 dto.getInspectionCallNo(), dto.getLotNo(), dto.getHeatNo());

        // UPSERT Pattern: Check if record exists using unique business key (inspectionCallNo, lotNo, heatNo)
        Optional<FinalVisualInspection> existing = repository.findByInspectionCallNoAndLotNoAndHeatNo(
                dto.getInspectionCallNo(),
                dto.getLotNo(),
                dto.getHeatNo()
        );

        FinalVisualInspection entity;
        if (existing.isPresent()) {
            // EXISTING RECORD: Update only business fields and updatedBy/updatedAt
            // createdBy and createdAt remain immutable (protected by @Column(updatable = false))
            entity = existing.get();
            log.info("Updating existing visual inspection record, id={}", entity.getId());
            entity.setFirstSampleRejected(dto.getFirstSampleRejected() != null ? dto.getFirstSampleRejected() : 0);
            entity.setSecondSampleRejected(dto.getSecondSampleRejected() != null ? dto.getSecondSampleRejected() : 0);
            entity.setTotalRejected(dto.getTotalRejected() != null ? dto.getTotalRejected() : 0);
            if (dto.getStatus() != null) {
                entity.setStatus(dto.getStatus());
            }
            entity.setRemarks(dto.getRemarks());
            entity.setDateOfInspection(dto.getDateOfInspection());
            entity.setUpdatedBy(userId);
            // updatedAt is set automatically by @PreUpdate
        } else {
            // NEW RECORD: Create new entity with all fields including createdBy
            entity = new FinalVisualInspection();
            entity.setInspectionCallNo(dto.getInspectionCallNo());
            entity.setLotNo(dto.getLotNo());
            entity.setHeatNo(dto.getHeatNo());
            entity.setFirstSampleRejected(dto.getFirstSampleRejected() != null ? dto.getFirstSampleRejected() : 0);
            entity.setSecondSampleRejected(dto.getSecondSampleRejected() != null ? dto.getSecondSampleRejected() : 0);
            entity.setTotalRejected(dto.getTotalRejected() != null ? dto.getTotalRejected() : 0);
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
            entity.setRemarks(dto.getRemarks());
            entity.setDateOfInspection(dto.getDateOfInspection());
            entity.setCreatedBy(userId);
            entity.setUpdatedBy(userId);
            log.info("Creating new visual inspection record for call: {} lot: {} heat: {}",
                     dto.getInspectionCallNo(), dto.getLotNo(), dto.getHeatNo());
        }

        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalVisualInspection> getVisualInspectionById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalVisualInspection> getVisualInspectionByCallNo(String inspectionCallNo) {
        return repository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalVisualInspection> getVisualInspectionByCallNoAndLotNo(String inspectionCallNo, String lotNo) {
        return repository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalVisualInspection> getVisualInspectionByCallNoAndHeatNo(String inspectionCallNo, String heatNo) {
        return repository.findByInspectionCallNoAndHeatNo(inspectionCallNo, heatNo);
    }

    @Override
    @Transactional
    public FinalVisualInspection updateVisualInspection(FinalVisualInspectionDto dto, String userId) {
        log.info("Updating visual inspection for call: {} lot: {}", dto.getInspectionCallNo(), dto.getLotNo());

        FinalVisualInspection entity = repository.findByInspectionCallNoAndLotNo(dto.getInspectionCallNo(), dto.getLotNo())
                .orElseThrow(() -> new RuntimeException("Visual inspection not found"));

        entity.setFirstSampleRejected(dto.getFirstSampleRejected());
        entity.setSecondSampleRejected(dto.getSecondSampleRejected());
        entity.setTotalRejected(dto.getTotalRejected());
        entity.setStatus(dto.getStatus());
        entity.setRemarks(dto.getRemarks());
        entity.setDateOfInspection(dto.getDateOfInspection());
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());

        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteVisualInspection(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteVisualInspectionByCallNo(String inspectionCallNo) {
        List<FinalVisualInspection> records = repository.findByInspectionCallNo(inspectionCallNo);
        repository.deleteAll(records);
    }
}

