package com.sarthi.service.finalmaterial.impl;

import com.sarthi.entity.finalmaterial.*;
import com.sarthi.repository.finalmaterial.*;
import com.sarthi.service.finalmaterial.FinalInspectionSubmoduleService;
import com.sarthi.dto.finalmaterial.FinalLadleValuesDto;
import com.sarthi.dto.finalmaterial.FinalChemicalAnalysisRequest;
import com.sarthi.dto.finalmaterial.FinalChemicalAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of Final Inspection Submodule Service
 * Handles CRUD operations for all final inspection submodule data with audit tracking
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FinalInspectionSubmoduleServiceImpl implements FinalInspectionSubmoduleService {

    private final FinalCalibrationDocumentsRepository calibrationDocumentsRepository;
    private final FinalChemicalAnalysisRepository chemicalAnalysisRepository;
    private final FinalApplicationDeflectionRepository applicationDeflectionRepository;
    private final FinalWeightTestRepository weightTestRepository;
    private final FinalToeLoadTestRepository toeLoadTestRepository;

    // ===== CALIBRATION & DOCUMENTS =====
    @Override
    public FinalCalibrationDocuments saveCalibrationDocuments(FinalCalibrationDocuments data, String userId) {
        data.setCreatedBy(userId);
        data.setUpdatedBy(userId);
        data.setCreatedAt(LocalDateTime.now());
        data.setUpdatedAt(LocalDateTime.now());
        return calibrationDocumentsRepository.save(data);
    }

    @Override
    public List<FinalCalibrationDocuments> getCalibrationDocumentsByCallNo(String inspectionCallNo) {
        return calibrationDocumentsRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    public List<FinalCalibrationDocuments> getCalibrationDocumentsByLot(String inspectionCallNo, String lotNo) {
        return calibrationDocumentsRepository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    public Optional<FinalCalibrationDocuments> getCalibrationDocumentsById(Long id) {
        return calibrationDocumentsRepository.findById(id);
    }

    @Override
    public FinalCalibrationDocuments updateCalibrationDocuments(FinalCalibrationDocuments data, String userId) {
        data.setUpdatedBy(userId);
        data.setUpdatedAt(LocalDateTime.now());
        return calibrationDocumentsRepository.save(data);
    }

    @Override
    public void deleteCalibrationDocuments(Long id) {
        calibrationDocumentsRepository.deleteById(id);
    }

    // ===== VISUAL INSPECTION =====
    // Note: Visual inspection is now handled by FinalVisualInspectionService
    // Use FinalVisualInspectionService for visual inspection operations

    // ===== DIMENSIONAL INSPECTION =====
    // Note: Dimensional inspection is now handled by FinalDimensionalInspectionService
    // Use FinalDimensionalInspectionService for dimensional inspection operations

    // ===== CHEMICAL ANALYSIS =====
    @Override
    public FinalChemicalAnalysisResponse saveChemicalAnalysis(FinalChemicalAnalysisRequest request, String userId) {
        // UPSERT Pattern: Check if data already exists for this call + lot combination
        List<FinalChemicalAnalysis> existing = chemicalAnalysisRepository
                .findByInspectionCallNoAndLotNo(request.getInspectionCallNo(), request.getLotNo());

        FinalChemicalAnalysis entity;
        if (existing != null && !existing.isEmpty()) {
            // UPDATE existing record - preserve id, createdBy, createdAt
            entity = existing.get(0);
            entity.setUpdatedBy(userId);
            entity.setUpdatedAt(LocalDateTime.now());
        } else {
            // INSERT new record
            entity = new FinalChemicalAnalysis();
            entity.setInspectionCallNo(request.getInspectionCallNo());
            entity.setLotNo(request.getLotNo());
            entity.setCreatedBy(userId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedBy(userId);
            entity.setUpdatedAt(LocalDateTime.now());
        }

        // Set/update all data fields (common for both insert and update)
        entity.setHeatNo(request.getHeatNo());
        entity.setSampleNo(request.getSampleNo());
        entity.setCarbonPercent(request.getCarbonPercent());
        entity.setSiliconPercent(request.getSiliconPercent());
        entity.setManganesePercent(request.getManganesePercent());
        entity.setSulphurPercent(request.getSulphurPercent());
        entity.setPhosphorusPercent(request.getPhosphorusPercent());
        entity.setRemarks(request.getRemarks());

        FinalChemicalAnalysis saved = chemicalAnalysisRepository.save(entity);

        // Map entity to response DTO
        return mapToResponse(saved);
    }

    /**
     * Map FinalChemicalAnalysis entity to response DTO
     */
    private FinalChemicalAnalysisResponse mapToResponse(FinalChemicalAnalysis entity) {
        FinalChemicalAnalysisResponse response = new FinalChemicalAnalysisResponse();
        response.setId(entity.getId());
        response.setInspectionCallNo(entity.getInspectionCallNo());
        response.setLotNo(entity.getLotNo());
        response.setHeatNo(entity.getHeatNo());
        response.setSampleNo(entity.getSampleNo());
        response.setCarbonPercent(entity.getCarbonPercent());
        response.setSiliconPercent(entity.getSiliconPercent());
        response.setManganesePercent(entity.getManganesePercent());
        response.setSulphurPercent(entity.getSulphurPercent());
        response.setPhosphorusPercent(entity.getPhosphorusPercent());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    public List<FinalChemicalAnalysis> getChemicalAnalysisByCallNo(String inspectionCallNo) {
        return chemicalAnalysisRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    public List<FinalChemicalAnalysis> getChemicalAnalysisByLot(String inspectionCallNo, String lotNo) {
        return chemicalAnalysisRepository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    public Optional<FinalChemicalAnalysis> getChemicalAnalysisById(Long id) {
        return chemicalAnalysisRepository.findById(id);
    }

    @Override
    public FinalChemicalAnalysis updateChemicalAnalysis(FinalChemicalAnalysis data, String userId) {
        data.setUpdatedBy(userId);
        data.setUpdatedAt(LocalDateTime.now());
        return chemicalAnalysisRepository.save(data);
    }

    @Override
    public void deleteChemicalAnalysis(Long id) {
        chemicalAnalysisRepository.deleteById(id);
    }

    // ===== INCLUSION & DECARB - DEPRECATED =====
    // Old flat structure methods removed - migrate to new parent-child structure
    // Use FinalInclusionRatingNewService, FinalDepthOfDecarburizationService,
    // FinalMicrostructureTestService, and FinalFreedomFromDefectsTestService instead

    // ===== DEFLECTION TEST =====
    @Override
    public FinalApplicationDeflection saveApplicationDeflection(FinalApplicationDeflection data, String userId) {
        data.setCreatedBy(userId);
        data.setUpdatedBy(userId);
        data.setCreatedAt(LocalDateTime.now());
        data.setUpdatedAt(LocalDateTime.now());
        return applicationDeflectionRepository.save(data);
    }

    @Override
    public List<FinalApplicationDeflection> getApplicationDeflectionByCallNo(String inspectionCallNo) {
        return applicationDeflectionRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    public List<FinalApplicationDeflection> getApplicationDeflectionByLot(String inspectionCallNo, String lotNo) {
        return applicationDeflectionRepository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    public Optional<FinalApplicationDeflection> getApplicationDeflectionById(Long id) {
        return applicationDeflectionRepository.findById(id);
    }

    @Override
    public FinalApplicationDeflection updateApplicationDeflection(FinalApplicationDeflection data, String userId) {
        data.setUpdatedBy(userId);
        data.setUpdatedAt(LocalDateTime.now());
        return applicationDeflectionRepository.save(data);
    }

    @Override
    public void deleteApplicationDeflection(Long id) {
        applicationDeflectionRepository.deleteById(id);
    }

    // ===== WEIGHT TEST =====
    @Override
    public FinalWeightTest saveWeightTest(FinalWeightTest data, String userId) {
        data.setCreatedBy(userId);
        data.setUpdatedBy(userId);
        data.setCreatedAt(LocalDateTime.now());
        data.setUpdatedAt(LocalDateTime.now());
        return weightTestRepository.save(data);
    }

    @Override
    public List<FinalWeightTest> getWeightTestByCallNo(String inspectionCallNo) {
        return weightTestRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    public List<FinalWeightTest> getWeightTestByLot(String inspectionCallNo, String lotNo) {
        return weightTestRepository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    public Optional<FinalWeightTest> getWeightTestById(Long id) {
        return weightTestRepository.findById(id);
    }

    @Override
    public FinalWeightTest updateWeightTest(FinalWeightTest data, String userId) {
        data.setUpdatedBy(userId);
        data.setUpdatedAt(LocalDateTime.now());
        return weightTestRepository.save(data);
    }

    @Override
    public void deleteWeightTest(Long id) {
        weightTestRepository.deleteById(id);
    }

    // ===== TOE LOAD TEST =====
    @Override
    public FinalToeLoadTest saveToeLoadTest(FinalToeLoadTest data, String userId) {
        data.setCreatedBy(userId);
        data.setUpdatedBy(userId);
        data.setCreatedAt(LocalDateTime.now());
        data.setUpdatedAt(LocalDateTime.now());
        return toeLoadTestRepository.save(data);
    }

    @Override
    public List<FinalToeLoadTest> getToeLoadTestByCallNo(String inspectionCallNo) {
        return toeLoadTestRepository.findByInspectionCallNo(inspectionCallNo);
    }

    @Override
    public List<FinalToeLoadTest> getToeLoadTestByLot(String inspectionCallNo, String lotNo) {
        return toeLoadTestRepository.findByInspectionCallNoAndLotNo(inspectionCallNo, lotNo);
    }

    @Override
    public Optional<FinalToeLoadTest> getToeLoadTestById(Long id) {
        return toeLoadTestRepository.findById(id);
    }

    @Override
    public FinalToeLoadTest updateToeLoadTest(FinalToeLoadTest data, String userId) {
        data.setUpdatedBy(userId);
        data.setUpdatedAt(LocalDateTime.now());
        return toeLoadTestRepository.save(data);
    }

    @Override
    public void deleteToeLoadTest(Long id) {
        toeLoadTestRepository.deleteById(id);
    }

    // ===== LADLE VALUES =====
    @Override
    public List<FinalLadleValuesDto> getLadleValuesByCallNo(String inspectionCallNo) {
        List<FinalChemicalAnalysis> entities = chemicalAnalysisRepository.findByInspectionCallNo(inspectionCallNo);
        List<FinalLadleValuesDto> dtos = new ArrayList<>();

        for (FinalChemicalAnalysis entity : entities) {
            FinalLadleValuesDto dto = FinalLadleValuesDto.builder()
                    .lotNo(entity.getLotNo())
                    .heatNo(entity.getHeatNo())
                    .percentC(entity.getCarbonPercent())
                    .percentSi(entity.getSiliconPercent())
                    .percentMn(entity.getManganesePercent())
                    .percentP(entity.getPhosphorusPercent())
                    .percentS(entity.getSulphurPercent())
                    .build();
            dtos.add(dto);
        }

        return dtos;
    }
}

