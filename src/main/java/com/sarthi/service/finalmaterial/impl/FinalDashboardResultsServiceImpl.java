package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalCumulativeResultsDto;
import com.sarthi.dto.finalmaterial.FinalInspectionSummaryDto;
import com.sarthi.dto.finalmaterial.FinalInspectionLotResultsDto;
import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import com.sarthi.entity.finalmaterial.FinalInspectionSummary;
import com.sarthi.entity.finalmaterial.FinalInspectionLotResults;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionSummaryRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotResultsRepository;
import com.sarthi.service.finalmaterial.FinalDashboardResultsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of FinalDashboardResultsService
 */
@Service
@Slf4j
@Transactional
public class FinalDashboardResultsServiceImpl implements FinalDashboardResultsService {

    @Autowired
    private FinalCumulativeResultsRepository cumulativeResultsRepository;

    @Autowired
    private FinalInspectionSummaryRepository inspectionSummaryRepository;

    @Autowired
    private FinalInspectionLotResultsRepository lotResultsRepository;

    // ===== CUMULATIVE RESULTS =====
    @Override
    public FinalCumulativeResults saveCumulativeResults(FinalCumulativeResultsDto dto, String userId) {
        log.info("Saving cumulative results for call: {}", dto.getInspectionCallNo());

        // Check if record already exists (upsert pattern)
        Optional<FinalCumulativeResults> existing = cumulativeResultsRepository.findByInspectionCallNo(dto.getInspectionCallNo());

        FinalCumulativeResults entity;
        if (existing.isPresent()) {
            // Update existing record
            entity = existing.get();
            log.info("Updating existing cumulative results for call: {}", dto.getInspectionCallNo());
            entity.setPoNo(dto.getPoNo());
            entity.setPoQty(dto.getPoQty());
            entity.setCummQtyOfferedPreviously(dto.getCummQtyOfferedPreviously());
            entity.setCummQtyPassedPreviously(dto.getCummQtyPassedPreviously());
            entity.setQtyNowOffered(dto.getQtyNowOffered());
            entity.setQtyNowPassed(dto.getQtyNowPassed());
            entity.setQtyNowRejected(dto.getQtyNowRejected());
            entity.setQtyStillDue(dto.getQtyStillDue());
            entity.setTotalSampleSize(dto.getTotalSampleSize());
            entity.setBagsForSampling(dto.getBagsForSampling());
            entity.setBagsOffered(dto.getBagsOffered());
            entity.setDateOfInspection(dto.getDateOfInspection());
            entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : userId);
            entity.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        } else {
            // Create new record
            entity = new FinalCumulativeResults();
            entity.setInspectionCallNo(dto.getInspectionCallNo());
            entity.setPoNo(dto.getPoNo());
            entity.setPoQty(dto.getPoQty());
            entity.setCummQtyOfferedPreviously(dto.getCummQtyOfferedPreviously());
            entity.setCummQtyPassedPreviously(dto.getCummQtyPassedPreviously());
            entity.setQtyNowOffered(dto.getQtyNowOffered());
            entity.setQtyNowPassed(dto.getQtyNowPassed());
            entity.setQtyNowRejected(dto.getQtyNowRejected());
            entity.setQtyStillDue(dto.getQtyStillDue());
            entity.setTotalSampleSize(dto.getTotalSampleSize());
            entity.setBagsForSampling(dto.getBagsForSampling());
            entity.setBagsOffered(dto.getBagsOffered());
            entity.setDateOfInspection(dto.getDateOfInspection());
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : userId);
            entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
            entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : userId);
            entity.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        }

        return cumulativeResultsRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalCumulativeResults> getCumulativeResultsByCallNo(String inspectionCallNo) {
        return cumulativeResultsRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalCumulativeResults> getCumulativeResultsByPoNo(String poNo) {
        return cumulativeResultsRepository.findByPoNo(poNo);
    }

    @Override
    public FinalCumulativeResults updateCumulativeResults(FinalCumulativeResultsDto dto, String userId) {
        log.info("Updating cumulative results for call: {}", dto.getInspectionCallNo());
        
        FinalCumulativeResults entity = cumulativeResultsRepository
            .findByInspectionCallNo(dto.getInspectionCallNo())
            .orElseThrow(() -> new RuntimeException("Cumulative results not found"));
        
        entity.setQtyNowOffered(dto.getQtyNowOffered());
        entity.setQtyNowPassed(dto.getQtyNowPassed());
        entity.setQtyNowRejected(dto.getQtyNowRejected());
        entity.setQtyStillDue(dto.getQtyStillDue());
        entity.setUpdatedBy(userId);
        
        return cumulativeResultsRepository.save(entity);
    }

    @Override
    public void deleteCumulativeResults(String inspectionCallNo) {
        cumulativeResultsRepository.findByInspectionCallNo(inspectionCallNo)
            .ifPresent(entity -> cumulativeResultsRepository.delete(entity));
    }

    // ===== INSPECTION SUMMARY =====
    @Override
    public FinalInspectionSummary saveInspectionSummary(FinalInspectionSummaryDto dto, String userId) {
        log.info("Saving inspection summary for call: {}", dto.getInspectionCallNo());

        // Check if record already exists (upsert pattern)
        Optional<FinalInspectionSummary> existing = inspectionSummaryRepository.findByInspectionCallNo(dto.getInspectionCallNo());

        FinalInspectionSummary entity;
        if (existing.isPresent()) {
            // Update existing record
            entity = existing.get();
            log.info("Updating existing inspection summary for call: {}", dto.getInspectionCallNo());
            entity.setPackedInHdpe(dto.getPackedInHdpe());
            entity.setCleanedWithCoating(dto.getCleanedWithCoating());
            entity.setInspectionStatus(dto.getInspectionStatus());
            entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : userId);
            entity.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        } else {
            // Create new record
            entity = new FinalInspectionSummary();
            entity.setInspectionCallNo(dto.getInspectionCallNo());
            entity.setPackedInHdpe(dto.getPackedInHdpe());
            entity.setCleanedWithCoating(dto.getCleanedWithCoating());
            entity.setInspectionStatus(dto.getInspectionStatus());
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : userId);
            entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
            entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : userId);
            entity.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        }

        return inspectionSummaryRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalInspectionSummary> getInspectionSummaryByCallNo(String inspectionCallNo) {
        return inspectionSummaryRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    public FinalInspectionSummary updateInspectionSummary(FinalInspectionSummaryDto dto, String userId) {
        log.info("Updating inspection summary for call: {}", dto.getInspectionCallNo());
        
        FinalInspectionSummary entity = inspectionSummaryRepository
            .findByInspectionCallNo(dto.getInspectionCallNo())
            .orElseThrow(() -> new RuntimeException("Inspection summary not found"));
        
        entity.setPackedInHdpe(dto.getPackedInHdpe());
        entity.setCleanedWithCoating(dto.getCleanedWithCoating());
        entity.setInspectionStatus(dto.getInspectionStatus());
        entity.setUpdatedBy(userId);
        
        return inspectionSummaryRepository.save(entity);
    }

    @Override
    public void deleteInspectionSummary(String inspectionCallNo) {
        inspectionSummaryRepository.findByInspectionCallNo(inspectionCallNo)
            .ifPresent(entity -> inspectionSummaryRepository.delete(entity));
    }

    // ===== LOT RESULTS =====
    @Override
    public FinalInspectionLotResults saveLotResults(FinalInspectionLotResultsDto dto, String userId) {
        log.info("Saving lot results for call: {} lot: {}", dto.getInspectionCallNo(), dto.getLotNo());

        // Check if record already exists (upsert pattern)
        Optional<FinalInspectionLotResults> existing = lotResultsRepository.findByInspectionCallNoAndLotNo(dto.getInspectionCallNo(), dto.getLotNo());

        FinalInspectionLotResults entity;
        if (existing.isPresent()) {
            // Update existing record
            entity = existing.get();
            log.info("Updating existing lot results for call: {} lot: {}", dto.getInspectionCallNo(), dto.getLotNo());
            entity.setHeatNo(dto.getHeatNo());
            entity.setCalibrationStatus(dto.getCalibrationStatus());
            entity.setVisualDimStatus(dto.getVisualDimStatus());
            entity.setHardnessStatus(dto.getHardnessStatus());
            entity.setInclusionStatus(dto.getInclusionStatus());
            entity.setDeflectionStatus(dto.getDeflectionStatus());
            entity.setToeLoadStatus(dto.getToeLoadStatus());
            entity.setWeightStatus(dto.getWeightStatus());
            entity.setChemicalStatus(dto.getChemicalStatus());
            entity.setErcUsedForTesting(dto.getErcUsedForTesting());
            entity.setStdPackingNo(dto.getStdPackingNo());
            entity.setBagsWithStdPacking(dto.getBagsWithStdPacking());
            entity.setNonStdBagsCount(dto.getNonStdBagsCount());
            entity.setNonStdBagsQty(dto.getNonStdBagsQty());
            entity.setHologramDetails(dto.getHologramDetails());
            entity.setRemarks(dto.getRemarks());
            entity.setLotStatus(dto.getLotStatus());
            entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : userId);
            entity.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        } else {
            // Create new record
            entity = new FinalInspectionLotResults();
            entity.setInspectionCallNo(dto.getInspectionCallNo());
            entity.setLotNo(dto.getLotNo());
            entity.setHeatNo(dto.getHeatNo());
            entity.setCalibrationStatus(dto.getCalibrationStatus());
            entity.setVisualDimStatus(dto.getVisualDimStatus());
            entity.setHardnessStatus(dto.getHardnessStatus());
            entity.setInclusionStatus(dto.getInclusionStatus());
            entity.setDeflectionStatus(dto.getDeflectionStatus());
            entity.setToeLoadStatus(dto.getToeLoadStatus());
            entity.setWeightStatus(dto.getWeightStatus());
            entity.setChemicalStatus(dto.getChemicalStatus());
            entity.setErcUsedForTesting(dto.getErcUsedForTesting());
            entity.setStdPackingNo(dto.getStdPackingNo());
            entity.setBagsWithStdPacking(dto.getBagsWithStdPacking());
            entity.setNonStdBagsCount(dto.getNonStdBagsCount());
            entity.setNonStdBagsQty(dto.getNonStdBagsQty());
            entity.setHologramDetails(dto.getHologramDetails());
            entity.setRemarks(dto.getRemarks());
            entity.setLotStatus(dto.getLotStatus());
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : userId);
            entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
            entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : userId);
            entity.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        }

        return lotResultsRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalInspectionLotResults> getLotResultsByCallNoAndLotNo(String inspectionCallNo, String lotNo) {
        return lotResultsRepository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalInspectionLotResults> getLotResultsByCallNo(String inspectionCallNo) {
        return lotResultsRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalInspectionLotResults> getLotResultsByLotNo(String lotNo) {
        return lotResultsRepository.findByLotNo(lotNo);
    }

    @Override
    public FinalInspectionLotResults updateLotResults(FinalInspectionLotResultsDto dto, String userId) {
        log.info("Updating lot results for call: {} lot: {}", dto.getInspectionCallNo(), dto.getLotNo());
        
        FinalInspectionLotResults entity = lotResultsRepository
            .findByInspectionCallNoAndLotNo(dto.getInspectionCallNo(), dto.getLotNo())
            .orElseThrow(() -> new RuntimeException("Lot results not found"));
        
        entity.setVisualDimStatus(dto.getVisualDimStatus());
        entity.setHardnessStatus(dto.getHardnessStatus());
        entity.setInclusionStatus(dto.getInclusionStatus());
        entity.setDeflectionStatus(dto.getDeflectionStatus());
        entity.setToeLoadStatus(dto.getToeLoadStatus());
        entity.setWeightStatus(dto.getWeightStatus());
        entity.setChemicalStatus(dto.getChemicalStatus());
        entity.setErcUsedForTesting(dto.getErcUsedForTesting());
        entity.setStdPackingNo(dto.getStdPackingNo());
        entity.setBagsWithStdPacking(dto.getBagsWithStdPacking());
        entity.setNonStdBagsCount(dto.getNonStdBagsCount());
        entity.setNonStdBagsQty(dto.getNonStdBagsQty());
        entity.setHologramDetails(dto.getHologramDetails());
        entity.setRemarks(dto.getRemarks());
        entity.setLotStatus(dto.getLotStatus());
        entity.setUpdatedBy(userId);
        
        return lotResultsRepository.save(entity);
    }

    @Override
    public void deleteLotResults(String inspectionCallNo, String lotNo) {
        lotResultsRepository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo)
            .ifPresent(entity -> lotResultsRepository.delete(entity));
    }
}

