package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalDimensionalInspectionFlatDto;
import com.sarthi.entity.finalmaterial.FinalDimensionalInspectionFlat;
import com.sarthi.repository.finalmaterial.FinalDimensionalInspectionFlatRepository;
import com.sarthi.service.finalmaterial.FinalDimensionalInspectionFlatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Final Dimensional Inspection - FLAT STRUCTURE
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinalDimensionalInspectionFlatServiceImpl implements FinalDimensionalInspectionFlatService {

    private final FinalDimensionalInspectionFlatRepository repository;

    @Override
    @Transactional
    public FinalDimensionalInspectionFlat saveDimensionalInspection(FinalDimensionalInspectionFlatDto dto, String userId) {
        log.info("Saving dimensional inspection (flat) for call: {} lot: {} heat: {}",
                 dto.getInspectionCallNo(), dto.getLotNo(), dto.getHeatNo());

        // UPSERT Pattern: Check if record exists using unique business key
        Optional<FinalDimensionalInspectionFlat> existing = repository.findByInspectionCallNoAndLotNoAndHeatNo(
                dto.getInspectionCallNo(),
                dto.getLotNo(),
                dto.getHeatNo()
        );

        FinalDimensionalInspectionFlat entity;
        if (existing.isPresent()) {
            entity = existing.get();
            // Summary Fields (for backward compatibility)
            entity.setFirstSampleGoGaugeFail(dto.getFirstSampleGoGaugeFail() != null ? dto.getFirstSampleGoGaugeFail() : 0);
            entity.setSecondSampleGoGaugeFail(dto.getSecondSampleGoGaugeFail() != null ? dto.getSecondSampleGoGaugeFail() : 0);
            entity.setFirstSampleNoGoFail(dto.getFirstSampleNoGoFail() != null ? dto.getFirstSampleNoGoFail() : 0);
            entity.setSecondSampleNoGoFail(dto.getSecondSampleNoGoFail() != null ? dto.getSecondSampleNoGoFail() : 0);
            entity.setFirstSampleFlatBearingFail(dto.getFirstSampleFlatBearingFail() != null ? dto.getFirstSampleFlatBearingFail() : 0);
            entity.setSecondSampleFlatBearingFail(dto.getSecondSampleFlatBearingFail() != null ? dto.getSecondSampleFlatBearingFail() : 0);
            
            // Detailed Fields - 1st Sampling
            entity.setFirstSampleMainBoxGo(dto.getFirstSampleMainBoxGo() != null ? dto.getFirstSampleMainBoxGo() : 0);
            entity.setFirstSampleMainBoxNoGo(dto.getFirstSampleMainBoxNoGo() != null ? dto.getFirstSampleMainBoxNoGo() : 0);
            entity.setFirstSampleFallingGo(dto.getFirstSampleFallingGo() != null ? dto.getFirstSampleFallingGo() : 0);
            entity.setFirstSampleFallingNoGo(dto.getFirstSampleFallingNoGo() != null ? dto.getFirstSampleFallingNoGo() : 0);
            entity.setFirstSampleFlatBearingGo(dto.getFirstSampleFlatBearingGo() != null ? dto.getFirstSampleFlatBearingGo() : 0);
            entity.setFirstSampleFlatBearingNoGo(dto.getFirstSampleFlatBearingNoGo() != null ? dto.getFirstSampleFlatBearingNoGo() : 0);
            
            // Detailed Fields - 2nd Sampling
            entity.setSecondSampleMainBoxGo(dto.getSecondSampleMainBoxGo() != null ? dto.getSecondSampleMainBoxGo() : 0);
            entity.setSecondSampleMainBoxNoGo(dto.getSecondSampleMainBoxNoGo() != null ? dto.getSecondSampleMainBoxNoGo() : 0);
            entity.setSecondSampleFallingGo(dto.getSecondSampleFallingGo() != null ? dto.getSecondSampleFallingGo() : 0);
            entity.setSecondSampleFallingNoGo(dto.getSecondSampleFallingNoGo() != null ? dto.getSecondSampleFallingNoGo() : 0);
            entity.setSecondSampleFlatBearingGo(dto.getSecondSampleFlatBearingGo() != null ? dto.getSecondSampleFlatBearingGo() : 0);
            entity.setSecondSampleFlatBearingNoGo(dto.getSecondSampleFlatBearingNoGo() != null ? dto.getSecondSampleFlatBearingNoGo() : 0);

            entity.setTotalRejected(dto.getTotalRejected() != null ? dto.getTotalRejected() : 0);
            if (dto.getStatus() != null) {
                entity.setStatus(dto.getStatus());
            }
            entity.setRemarks(dto.getRemarks());
            entity.setDateOfInspection(dto.getDateOfInspection());
            entity.setUpdatedBy(userId);
            log.info("Updating existing dimensional inspection (flat) record");
        } else {
            entity = new FinalDimensionalInspectionFlat();
            entity.setInspectionCallNo(dto.getInspectionCallNo());
            entity.setLotNo(dto.getLotNo());
            entity.setHeatNo(dto.getHeatNo());
            
            // Summary Fields
            entity.setFirstSampleGoGaugeFail(dto.getFirstSampleGoGaugeFail() != null ? dto.getFirstSampleGoGaugeFail() : 0);
            entity.setSecondSampleGoGaugeFail(dto.getSecondSampleGoGaugeFail() != null ? dto.getSecondSampleGoGaugeFail() : 0);
            entity.setFirstSampleNoGoFail(dto.getFirstSampleNoGoFail() != null ? dto.getFirstSampleNoGoFail() : 0);
            entity.setSecondSampleNoGoFail(dto.getSecondSampleNoGoFail() != null ? dto.getSecondSampleNoGoFail() : 0);
            entity.setFirstSampleFlatBearingFail(dto.getFirstSampleFlatBearingFail() != null ? dto.getFirstSampleFlatBearingFail() : 0);
            entity.setSecondSampleFlatBearingFail(dto.getSecondSampleFlatBearingFail() != null ? dto.getSecondSampleFlatBearingFail() : 0);
            
            // Detailed Fields - 1st Sampling
            entity.setFirstSampleMainBoxGo(dto.getFirstSampleMainBoxGo() != null ? dto.getFirstSampleMainBoxGo() : 0);
            entity.setFirstSampleMainBoxNoGo(dto.getFirstSampleMainBoxNoGo() != null ? dto.getFirstSampleMainBoxNoGo() : 0);
            entity.setFirstSampleFallingGo(dto.getFirstSampleFallingGo() != null ? dto.getFirstSampleFallingGo() : 0);
            entity.setFirstSampleFallingNoGo(dto.getFirstSampleFallingNoGo() != null ? dto.getFirstSampleFallingNoGo() : 0);
            entity.setFirstSampleFlatBearingGo(dto.getFirstSampleFlatBearingGo() != null ? dto.getFirstSampleFlatBearingGo() : 0);
            entity.setFirstSampleFlatBearingNoGo(dto.getFirstSampleFlatBearingNoGo() != null ? dto.getFirstSampleFlatBearingNoGo() : 0);
            
            // Detailed Fields - 2nd Sampling
            entity.setSecondSampleMainBoxGo(dto.getSecondSampleMainBoxGo() != null ? dto.getSecondSampleMainBoxGo() : 0);
            entity.setSecondSampleMainBoxNoGo(dto.getSecondSampleMainBoxNoGo() != null ? dto.getSecondSampleMainBoxNoGo() : 0);
            entity.setSecondSampleFallingGo(dto.getSecondSampleFallingGo() != null ? dto.getSecondSampleFallingGo() : 0);
            entity.setSecondSampleFallingNoGo(dto.getSecondSampleFallingNoGo() != null ? dto.getSecondSampleFallingNoGo() : 0);
            entity.setSecondSampleFlatBearingGo(dto.getSecondSampleFlatBearingGo() != null ? dto.getSecondSampleFlatBearingGo() : 0);
            entity.setSecondSampleFlatBearingNoGo(dto.getSecondSampleFlatBearingNoGo() != null ? dto.getSecondSampleFlatBearingNoGo() : 0);
            
            entity.setTotalRejected(dto.getTotalRejected() != null ? dto.getTotalRejected() : 0);
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
            entity.setRemarks(dto.getRemarks());
            entity.setDateOfInspection(dto.getDateOfInspection());
            entity.setCreatedBy(userId);
            entity.setUpdatedBy(userId);
            log.info("Creating new dimensional inspection (flat) record");
        }

        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalDimensionalInspectionFlat> getDimensionalInspectionById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalDimensionalInspectionFlat> getDimensionalInspectionByCallNo(String inspectionCallNo) {
        return repository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalDimensionalInspectionFlat> getDimensionalInspectionByCallNoAndLotNo(
            String inspectionCallNo, String lotNo) {
        return repository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalDimensionalInspectionFlat> getDimensionalInspectionByCallNoAndHeatNo(
            String inspectionCallNo, String heatNo) {
        return repository.findByInspectionCallNoAndHeatNo(inspectionCallNo, heatNo);
    }

    @Override
    @Transactional
    public FinalDimensionalInspectionFlat updateDimensionalInspection(FinalDimensionalInspectionFlatDto dto, String userId) {
        log.info("Updating dimensional inspection (flat) for call: {} lot: {}", dto.getInspectionCallNo(), dto.getLotNo());

        FinalDimensionalInspectionFlat entity = repository.findByInspectionCallNoAndLotNo(
                dto.getInspectionCallNo(), dto.getLotNo())
                .orElseThrow(() -> new RuntimeException("Dimensional inspection (flat) not found"));

        entity.setFirstSampleGoGaugeFail(dto.getFirstSampleGoGaugeFail());
        entity.setSecondSampleGoGaugeFail(dto.getSecondSampleGoGaugeFail());
        entity.setFirstSampleNoGoFail(dto.getFirstSampleNoGoFail());
        entity.setSecondSampleNoGoFail(dto.getSecondSampleNoGoFail());
        entity.setFirstSampleFlatBearingFail(dto.getFirstSampleFlatBearingFail());
        entity.setSecondSampleFlatBearingFail(dto.getSecondSampleFlatBearingFail());
        
        // Detailed Fields
        entity.setFirstSampleMainBoxGo(dto.getFirstSampleMainBoxGo());
        entity.setFirstSampleMainBoxNoGo(dto.getFirstSampleMainBoxNoGo());
        entity.setFirstSampleFallingGo(dto.getFirstSampleFallingGo());
        entity.setFirstSampleFallingNoGo(dto.getFirstSampleFallingNoGo());
        entity.setFirstSampleFlatBearingGo(dto.getFirstSampleFlatBearingGo());
        entity.setFirstSampleFlatBearingNoGo(dto.getFirstSampleFlatBearingNoGo());

        entity.setSecondSampleMainBoxGo(dto.getSecondSampleMainBoxGo());
        entity.setSecondSampleMainBoxNoGo(dto.getSecondSampleMainBoxNoGo());
        entity.setSecondSampleFallingGo(dto.getSecondSampleFallingGo());
        entity.setSecondSampleFallingNoGo(dto.getSecondSampleFallingNoGo());
        entity.setSecondSampleFlatBearingGo(dto.getSecondSampleFlatBearingGo());
        entity.setSecondSampleFlatBearingNoGo(dto.getSecondSampleFlatBearingNoGo());

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
    public void deleteDimensionalInspection(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteDimensionalInspectionByCallNo(String inspectionCallNo) {
        List<FinalDimensionalInspectionFlat> records = repository.findByInspectionCallNo(inspectionCallNo);
        repository.deleteAll(records);
    }
}

