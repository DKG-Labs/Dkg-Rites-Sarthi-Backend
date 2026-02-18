package com.sarthi.service.finalmaterial.impl;

import com.sarthi.entity.finalmaterial.*;
import com.sarthi.repository.finalmaterial.*;
import com.sarthi.service.finalmaterial.FinalInspectionSubmoduleService;
import com.sarthi.dto.finalmaterial.FinalLadleValuesDto;
import com.sarthi.dto.finalmaterial.FinalChemicalAnalysisRequest;
import com.sarthi.dto.finalmaterial.FinalChemicalAnalysisResponse;
import com.sarthi.entity.rawmaterial.RmChemicalAnalysis;
import com.sarthi.entity.rawmaterial.InspectionCall;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final FinalInspectionDetailsRepository finalInspectionDetailsRepository;
    private final FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;
    private final com.sarthi.repository.rawmaterial.RmChemicalAnalysisRepository rmChemicalAnalysisRepository;
    private final com.sarthi.repository.rawmaterial.InspectionCallRepository inspectionCallRepository;

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

    @Override
    public List<FinalLadleValuesDto> getLadleValuesByCallNo(String inspectionCallNo) {
        // 1. Fetch the Inspection Call entity
        Optional<InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(inspectionCallNo);
        if (callOpt.isEmpty()) {
            return new ArrayList<>();
        }
        InspectionCall call = callOpt.get();
        String poSerialNo = call.getPoSerialNo();

        // 2. Fetch Final Inspection Details to get the ID
        Optional<FinalInspectionDetails> detailsOpt = finalInspectionDetailsRepository.findByIcId(call.getId());
        if (detailsOpt.isEmpty()) {
            return new ArrayList<>();
        }
        Long finalDetailId = detailsOpt.get().getId();

        // 3. Fetch all Lots for this Final Inspection
        List<FinalInspectionLotDetails> finalLots = finalInspectionLotDetailsRepository.findByFinalDetailId(finalDetailId);
        if (finalLots.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. Fetch all RM Chemical Analysis sharing the same PO Serial Number
        // (RM data is stored against RM calls, which have the same PO Serial No)
        List<RmChemicalAnalysis> rmChemicalAnalyses = rmChemicalAnalysisRepository.findByInspectionCallPoSerialNo(poSerialNo);

        // 5. Group RM analyses by Heat Number and keep only the LATEST one (highest ID or latest createdAt)
        Map<String, RmChemicalAnalysis> latestRmAnalysisByHeat = rmChemicalAnalyses.stream()
                .collect(Collectors.toMap(
                        RmChemicalAnalysis::getHeatNumber,
                        ca -> ca,
                        (existing, replacement) -> {
                            // Keep the one with the later createdAt or higher ID
                            if (replacement.getCreatedAt() != null && existing.getCreatedAt() != null) {
                                return replacement.getCreatedAt().isAfter(existing.getCreatedAt()) ? replacement : existing;
                            }
                            return replacement.getId() > existing.getId() ? replacement : existing;
                        }
                ));

        // 6. Map Final Lots to DTOs, matching with the latest RM analysis
        List<FinalLadleValuesDto> dtos = new ArrayList<>();
        for (FinalInspectionLotDetails lot : finalLots) {
            String heatNumber = lot.getHeatNumber();
            RmChemicalAnalysis rmAnalysis = latestRmAnalysisByHeat.get(heatNumber);

            FinalLadleValuesDto dto = FinalLadleValuesDto.builder()
                    .lotNo(lot.getLotNumber())
                    .heatNo(heatNumber)
                    .percentC(rmAnalysis != null ? rmAnalysis.getCarbon() : BigDecimal.ZERO)
                    .percentSi(rmAnalysis != null ? rmAnalysis.getSilicon() : BigDecimal.ZERO)
                    .percentMn(rmAnalysis != null ? rmAnalysis.getManganese() : BigDecimal.ZERO)
                    .percentP(rmAnalysis != null ? rmAnalysis.getPhosphorus() : BigDecimal.ZERO)
                    .percentS(rmAnalysis != null ? rmAnalysis.getSulphur() : BigDecimal.ZERO)
                    .build();
            dtos.add(dto);
        }

        return dtos;
    }
}

