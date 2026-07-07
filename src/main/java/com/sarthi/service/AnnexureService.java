package com.sarthi.service;

import com.sarthi.dto.*;
import com.sarthi.entity.*;
import com.sarthi.entity.finalmaterial.FinalChemicalAnalysis;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import com.sarthi.repository.RmMaterialTestingRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.InspectionCompleteDetailsRepository;
import com.sarthi.repository.InspectionCallDetailsRepository;
import com.sarthi.repository.RmDimensionalCheckRepository;
import com.sarthi.repository.finalmaterial.FinalChemicalAnalysisRepository;
import com.sarthi.repository.finalmaterial.FinalHardnessTestRepository;
import com.sarthi.repository.finalmaterial.FinalHardnessTestSampleRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotResultsRepository;
import com.sarthi.repository.finalmaterial.FinalToeLoadTestRepository;
import com.sarthi.repository.finalmaterial.FinalToeLoadTestSampleRepository;
import com.sarthi.repository.finalmaterial.FinalWeightTestRepository;
import com.sarthi.repository.finalmaterial.FinalWeightTestSampleRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.entity.finalmaterial.FinalHardnessTest;
import com.sarthi.entity.finalmaterial.FinalHardnessTestSample;
import com.sarthi.entity.finalmaterial.FinalInspectionLotResults;
import com.sarthi.entity.finalmaterial.FinalToeLoadTest;
import com.sarthi.entity.finalmaterial.FinalToeLoadTestSample;
import com.sarthi.entity.finalmaterial.FinalWeightTest;
import com.sarthi.entity.finalmaterial.FinalWeightTestSample;
import com.sarthi.entity.finalmaterial.FinalApplicationDeflection;
import com.sarthi.entity.finalmaterial.FinalApplicationDeflectionSample;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.sarthi.repository.finalmaterial.FinalFreedomFromDefectsSampleRepository;
import com.sarthi.repository.finalmaterial.FinalFreedomFromDefectsTestRepository;
import com.sarthi.repository.finalmaterial.FinalMicrostructureSampleRepository;
import com.sarthi.repository.finalmaterial.FinalMicrostructureTestRepository;
import com.sarthi.repository.finalmaterial.FinalDepthOfDecarburizationRepository;
import com.sarthi.repository.finalmaterial.FinalDepthOfDecarburizationSampleRepository;
import com.sarthi.repository.finalmaterial.FinalInclusionRatingNewRepository;
import com.sarthi.repository.finalmaterial.FinalInclusionRatingSampleRepository;
import com.sarthi.repository.finalmaterial.FinalApplicationDeflectionRepository;
import com.sarthi.repository.finalmaterial.FinalApplicationDeflectionSampleRepository;
import com.sarthi.repository.finalmaterial.FinalDimensionalInspectionFlatRepository;
import com.sarthi.repository.finalmaterial.FinalDimensionalInspectionRepository;
import com.sarthi.repository.RmVisualInspectionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sarthi.entity.finalmaterial.FinalInclusionRatingNew;
import com.sarthi.entity.finalmaterial.FinalInclusionRatingSample;
import com.sarthi.entity.finalmaterial.FinalMicrostructureTest;
import com.sarthi.entity.finalmaterial.FinalMicrostructureSample;
import com.sarthi.entity.finalmaterial.FinalFreedomFromDefectsTest;
import com.sarthi.entity.finalmaterial.FinalFreedomFromDefectsSample;
import com.sarthi.entity.finalmaterial.FinalDepthOfDecarburization;
import com.sarthi.entity.finalmaterial.FinalDepthOfDecarburizationSample;
import com.sarthi.entity.finalmaterial.FinalDimensionalInspection;
import com.sarthi.entity.finalmaterial.FinalDimensionalInspectionFlat;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnexureService {

    private final RmMaterialTestingRepository rmMaterialTestingRepository;
    private final RmHeatFinalResultRepository rmHeatFinalResultRepository;
    private final RmHeatQuantityRepository rmHeatQuantityRepository;
    private final InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;
    private final InspectionCallDetailsRepository inspectionCallDetailsRepository;
    private final RmDimensionalCheckRepository rmDimensionalCheckRepository;
    private final FinalChemicalAnalysisRepository finalChemicalAnalysisRepository;
    private final FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;
    private final FinalInspectionDetailsRepository finalInspectionDetailsRepository;
    private final InspectionCallRepository inspectionCallRepository;
    private final FinalHardnessTestRepository finalHardnessTestRepository;
    private final FinalHardnessTestSampleRepository finalHardnessTestSampleRepository;
    private final FinalInspectionLotResultsRepository finalInspectionLotResultsRepository;
    private final FinalToeLoadTestRepository finalToeLoadTestRepository;
    private final FinalToeLoadTestSampleRepository finalToeLoadTestSampleRepository;
    private final FinalWeightTestRepository finalWeightTestRepository;
    private final FinalWeightTestSampleRepository finalWeightTestSampleRepository;
    private final FinalInclusionRatingNewRepository finalInclusionRatingNewRepository;
    private final FinalInclusionRatingSampleRepository finalInclusionRatingSampleRepository;
    private final FinalMicrostructureTestRepository finalMicrostructureTestRepository;
    private final FinalMicrostructureSampleRepository finalMicrostructureSampleRepository;
    private final FinalFreedomFromDefectsTestRepository finalFreedomFromDefectsTestRepository;
    private final FinalFreedomFromDefectsSampleRepository finalFreedomFromDefectsSampleRepository;
    private final FinalDepthOfDecarburizationRepository finalDepthOfDecarburizationRepository;
    private final FinalDepthOfDecarburizationSampleRepository finalDepthOfDecarburizationSampleRepository;
    private final FinalApplicationDeflectionRepository finalApplicationDeflectionRepository;
    private final FinalApplicationDeflectionSampleRepository finalApplicationDeflectionSampleRepository;
    private final FinalDimensionalInspectionFlatRepository finalDimensionalInspectionFlatRepository;
    private final FinalDimensionalInspectionRepository finalDimensionalInspectionRepository;
    private final RmVisualInspectionRepository rmVisualInspectionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Fetches and aggregates data for the Chemical Analysis Annexure (Annexure-I).
     *
     * @param callNo The inspection call number
     * @return Aggregated report data
     */
    public ChemicalAnalysisResponseDTO getChemicalAnalysisData(String callNo) {
        log.info("Fetching Chemical Analysis data for call no: {}", callNo);

        // 1. Fetch Basic Info / Header Metadata
        Optional<RmHeatFinalResult> finalResultOpt = rmHeatFinalResultRepository.findByInspectionCallNo(callNo)
                .stream().findFirst();
        
        Optional<InspectionCallDetails> callDetailsOpt = inspectionCallDetailsRepository.findByInspectionCallNo(callNo);
        Optional<InspectionCompleteDetails> completeDetailsOpt = inspectionCompleteDetailsRepository.findByCallNo(callNo);
        
        // Fetch first heat quantity for manufacturer and color code (assuming consistency across call)
        List<RmHeatQuantity> heatQuantities = rmHeatFinalResultRepository.findByInspectionCallNo(callNo)
                .stream()
                .flatMap(f -> rmHeatQuantityRepository.findByHeatNumber(f.getHeatNo()).stream())
                .collect(Collectors.toList());
        
        // Fallback for manufacturer/source
        String manufacturer = heatQuantities.isEmpty() ? "RITES LTD" : heatQuantities.get(0).getManufacturer();
        String colorCode = heatQuantities.isEmpty() ? "N/A" : heatQuantities.get(0).getColorCode();
        String certificateNo = completeDetailsOpt.map(InspectionCompleteDetails::getCertificateNo).orElse("N/A");
        
        // Summing weightOfferedMt from associated heats
        List<RmHeatFinalResult> heatResultsForChem = rmHeatFinalResultRepository.findByInspectionCallNo(callNo);
        BigDecimal totalWeightChem = heatResultsForChem.stream()
                .map(RmHeatFinalResult::getWeightOfferedMt)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String quantity = totalWeightChem.compareTo(BigDecimal.ZERO) > 0 ? totalWeightChem.toString() : "N/A";
        
        // Date mapping: Priority to date_of_inspection, then created_at
        String inspectionDate = "N/A";
        if (finalResultOpt.isPresent()) {
            RmHeatFinalResult fr = finalResultOpt.get();
            if (fr.getDateOfInspection() != null) {
                inspectionDate = fr.getDateOfInspection().format(DATE_FORMATTER);
            } else if (fr.getCreatedAt() != null) {
                inspectionDate = fr.getCreatedAt().format(DATE_FORMATTER);
            }
        }

        // 2. Fetch Sample Table Data (2 samples per heat)
        List<RmMaterialTesting> samples = rmMaterialTestingRepository.findByInspectionCallNo(callNo);
        List<ChemicalAnalysisRowDTO> rows = new ArrayList<>();
        
        // Build heat to tcNumber map and colorCode map
        Map<String, String> heatToTcMap = new java.util.HashMap<>();
        Map<String, String> heatToColorCodeMap = new java.util.HashMap<>();
        Optional<InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(callNo);
        if (callOpt.isPresent() && callOpt.get().getRmInspectionDetails() != null) {
            List<RmHeatQuantity> hqList = rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(callOpt.get().getRmInspectionDetails().getId()));
            for (RmHeatQuantity hq : hqList) {
                if (hq.getHeatNumber() != null) {
                    if (hq.getTcNumber() != null) {
                        heatToTcMap.put(hq.getHeatNumber(), hq.getTcNumber());
                    }
                    if (hq.getColorCode() != null) {
                        heatToColorCodeMap.put(hq.getHeatNumber(), hq.getColorCode());
                    }
                }
            }
        }
        if (heatToTcMap.isEmpty() && heatToColorCodeMap.isEmpty()) {
            List<RmHeatQuantity> fallbackHq = rmHeatFinalResultRepository.findByInspectionCallNo(callNo)
                    .stream()
                    .flatMap(f -> rmHeatQuantityRepository.findByHeatNumber(f.getHeatNo()).stream())
                    .collect(Collectors.toList());
            for (RmHeatQuantity hq : fallbackHq) {
                if (hq.getHeatNumber() != null) {
                    if (hq.getTcNumber() != null) {
                        heatToTcMap.putIfAbsent(hq.getHeatNumber(), hq.getTcNumber());
                    }
                    if (hq.getColorCode() != null) {
                        heatToColorCodeMap.putIfAbsent(hq.getHeatNumber(), hq.getColorCode());
                    }
                }
            }
        }

        int sNo = 1;
        for (RmMaterialTesting sample : samples) {
            // Find statuses for this heat to populate row-level statuses
            Optional<RmHeatFinalResult> heatResultOpt = rmHeatFinalResultRepository.findByInspectionCallNoAndHeatNo(callNo, sample.getHeatNo())
                    .stream().findFirst();

            String visualStatus = heatResultOpt.map(RmHeatFinalResult::getVisualStatus).orElse("N/A");
            String overallStatus = heatResultOpt.map(RmHeatFinalResult::getOverallStatus).orElse("PENDING");

            String freedomFromDefects = "N/A";
            if ("OK".equalsIgnoreCase(visualStatus) || "PASS".equalsIgnoreCase(visualStatus) || "ACCEPTED".equalsIgnoreCase(visualStatus)) {
                freedomFromDefects = "OK";
            } else if ("PARTIAL".equalsIgnoreCase(visualStatus) || "PARTIALLY_ACCEPTED".equalsIgnoreCase(visualStatus) || "PARTIALLY ACCEPTED".equalsIgnoreCase(visualStatus)) {
                Optional<RmVisualInspection> visualOpt = rmVisualInspectionRepository
                        .findByInspectionCallNoAndHeatNo(callNo, sample.getHeatNo())
                        .stream().findFirst();
                if (visualOpt.isPresent() && heatResultOpt.isPresent()) {
                    BigDecimal rejected = visualOpt.get().getWeightRejected();
                    BigDecimal offered = heatResultOpt.get().getWeightOfferedMt();
                    if (rejected != null && offered != null) {
                        if (rejected.compareTo(offered) >= 0) {
                            freedomFromDefects = "NOT OK";
                        } else if (rejected.compareTo(BigDecimal.ZERO) > 0) {
                            freedomFromDefects = "Partially OK";
                        } else {
                            freedomFromDefects = "OK";
                        }
                    } else {
                        freedomFromDefects = "Partially OK";
                    }
                } else {
                    freedomFromDefects = "Partially OK";
                }
            } else if ("NOT OK".equalsIgnoreCase(visualStatus) || "FAIL".equalsIgnoreCase(visualStatus) || "REJECTED".equalsIgnoreCase(visualStatus)) {
                freedomFromDefects = "NOT OK";
            } else {
                freedomFromDefects = visualStatus;
            }

            // Format Inclusion Rating (A: 1.5, B: 1.2, C: 0.8, D: 1.0)
            String inclusionStr = String.format("A:%s, B:%s, C:%s, D:%s", 
                    formatValue(sample.getInclusionA()), 
                    formatValue(sample.getInclusionB()), 
                    formatValue(sample.getInclusionC()), 
                    formatValue(sample.getInclusionD()));

            rows.add(ChemicalAnalysisRowDTO.builder()
                    .sNo(sNo++)
                    .date(inspectionDate)
                    .heatNo(sample.getHeatNo())
                    .sampleNo(sample.getSampleNumber())
                    .tcNumber(heatToTcMap.getOrDefault(sample.getHeatNo(), "N/A"))
                    .coilCode(heatToColorCodeMap.getOrDefault(sample.getHeatNo(), "N/A"))
                    .quantity(heatResultOpt.map(RmHeatFinalResult::getWeightOfferedMt).orElse(null))
                    .carbon(sample.getCarbonPercent())
                    .manganese(sample.getManganesePercent())
                    .silicon(sample.getSiliconPercent())
                    .sulphur(sample.getSulphurPercent())
                    .phosphorus(sample.getPhosphorusPercent())
                    .grainSize(sample.getGrainSize())
                    .inclusion(inclusionStr)
                    .hardness(sample.getHardness())
                    .decarb(sample.getDecarb())
                    .freedomFromDefects(freedomFromDefects)
                    .acceptedOrNot(capitalize(overallStatus))
                    .build());
        }

        return ChemicalAnalysisResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer(manufacturer)
                .certificateNo(certificateNo)
                .sourceOfRawMaterial(manufacturer) // Assuming source is the manufacturer for RM
                .colorCode(colorCode)
                .quantity(quantity)
                .dateOfInspection(inspectionDate)
                .rows(rows)
                .build();
    }

    /**
     * Fetches and aggregates data for the Dimensional Check Annexure (Annexure-II).
     *
     * @param callNo The inspection call number
     * @return Aggregated dimensional check data
     */
    public DimensionalCheckResponseDTO getDimensionalCheckData(String callNo) {
        log.info("Fetching Dimensional Check data for call no: {}", callNo);

        // 1. Fetch Basic Info / Header Metadata (Manufacturer, Cert No, etc.)
        Optional<RmHeatFinalResult> finalResultOpt = rmHeatFinalResultRepository.findByInspectionCallNo(callNo)
                .stream().findFirst();
        
        Optional<InspectionCallDetails> callDetailsOpt = inspectionCallDetailsRepository.findByInspectionCallNo(callNo);
        Optional<InspectionCompleteDetails> completeDetailsOpt = inspectionCompleteDetailsRepository.findByCallNo(callNo);
        
        List<RmHeatQuantity> heatQuantities = rmHeatFinalResultRepository.findByInspectionCallNo(callNo)
                .stream()
                .flatMap(f -> rmHeatQuantityRepository.findByHeatNumber(f.getHeatNo()).stream())
                .collect(Collectors.toList());
        
        String manufacturer = heatQuantities.isEmpty() ? "RITES LTD" : heatQuantities.get(0).getManufacturer();
        String colorCode = heatQuantities.isEmpty() ? "N/A" : heatQuantities.get(0).getColorCode();
        String certificateNo = completeDetailsOpt.map(InspectionCompleteDetails::getCertificateNo).orElse("N/A");
        
        // Summing weightOfferedMt from associated heats
        List<RmHeatFinalResult> heatResultsForDim = rmHeatFinalResultRepository.findByInspectionCallNo(callNo);
        BigDecimal totalWeightDim = heatResultsForDim.stream()
                .map(RmHeatFinalResult::getWeightOfferedMt)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String quantity = totalWeightDim.compareTo(BigDecimal.ZERO) > 0 ? totalWeightDim.toString() : "N/A";
        
        String inspectionDate = "N/A";
        if (finalResultOpt.isPresent()) {
            RmHeatFinalResult fr = finalResultOpt.get();
            if (fr.getDateOfInspection() != null) {
                inspectionDate = fr.getDateOfInspection().format(DATE_FORMATTER);
            } else if (fr.getCreatedAt() != null) {
                inspectionDate = fr.getCreatedAt().format(DATE_FORMATTER);
            }
        }

        // 2. Fetch Dimensional Check Table Data (1 row per heat, 20 samples)
        List<com.sarthi.entity.RmDimensionalCheck> dimChecks = rmDimensionalCheckRepository.findByInspectionCallNo(callNo);
        List<RmDimensionalCheckDto> rows = new ArrayList<>();
        
        // Build heat to tcNumber map and colorCode map
        Map<String, String> heatToTcMap = new java.util.HashMap<>();
        Map<String, String> heatToColorCodeMap = new java.util.HashMap<>();
        Optional<InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(callNo);
        if (callOpt.isPresent() && callOpt.get().getRmInspectionDetails() != null) {
            List<RmHeatQuantity> hqList = rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(callOpt.get().getRmInspectionDetails().getId()));
            for (RmHeatQuantity hq : hqList) {
                if (hq.getHeatNumber() != null) {
                    if (hq.getTcNumber() != null) {
                        heatToTcMap.put(hq.getHeatNumber(), hq.getTcNumber());
                    }
                    if (hq.getColorCode() != null) {
                        heatToColorCodeMap.put(hq.getHeatNumber(), hq.getColorCode());
                    }
                }
            }
        }
        if (heatToTcMap.isEmpty() && heatToColorCodeMap.isEmpty()) {
            List<RmHeatQuantity> fallbackHq = rmHeatFinalResultRepository.findByInspectionCallNo(callNo)
                    .stream()
                    .flatMap(f -> rmHeatQuantityRepository.findByHeatNumber(f.getHeatNo()).stream())
                    .collect(Collectors.toList());
            for (RmHeatQuantity hq : fallbackHq) {
                if (hq.getHeatNumber() != null) {
                    if (hq.getTcNumber() != null) {
                        heatToTcMap.putIfAbsent(hq.getHeatNumber(), hq.getTcNumber());
                    }
                    if (hq.getColorCode() != null) {
                        heatToColorCodeMap.putIfAbsent(hq.getHeatNumber(), hq.getColorCode());
                    }
                }
            }
        }

        for (com.sarthi.entity.RmDimensionalCheck check : dimChecks) {
            RmDimensionalCheckDto dto = new RmDimensionalCheckDto();
            dto.setInspectionCallNo(check.getInspectionCallNo());
            dto.setHeatNo(check.getHeatNo());
            dto.setHeatIndex(check.getHeatIndex());
            dto.setDefectCount(check.getDefectCount());
            dto.setTcNumber(heatToTcMap.getOrDefault(check.getHeatNo(), "N/A"));
            dto.setCoilCode(heatToColorCodeMap.getOrDefault(check.getHeatNo(), "N/A"));
            
            // Map dimensional status from heat result
            Optional<RmHeatFinalResult> heatResultOpt = rmHeatFinalResultRepository.findByInspectionCallNoAndHeatNo(callNo, check.getHeatNo())
                    .stream().findFirst();
            dto.setStatus(heatResultOpt.map(RmHeatFinalResult::getDimensionalStatus).orElse("PENDING"));
            
            // Map 20 individual columns to List for easier frontend handling
            List<java.math.BigDecimal> diameters = new ArrayList<>();
            diameters.add(check.getSample1Diameter());
            diameters.add(check.getSample2Diameter());
            diameters.add(check.getSample3Diameter());
            diameters.add(check.getSample4Diameter());
            diameters.add(check.getSample5Diameter());
            diameters.add(check.getSample6Diameter());
            diameters.add(check.getSample7Diameter());
            diameters.add(check.getSample8Diameter());
            diameters.add(check.getSample9Diameter());
            diameters.add(check.getSample10Diameter());
            diameters.add(check.getSample11Diameter());
            diameters.add(check.getSample12Diameter());
            diameters.add(check.getSample13Diameter());
            diameters.add(check.getSample14Diameter());
            diameters.add(check.getSample15Diameter());
            diameters.add(check.getSample16Diameter());
            diameters.add(check.getSample17Diameter());
            diameters.add(check.getSample18Diameter());
            diameters.add(check.getSample19Diameter());
            diameters.add(check.getSample20Diameter());
            
            dto.setSampleDiameters(diameters);
            rows.add(dto);
        }

        return DimensionalCheckResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer(manufacturer)
                .certificateNo(certificateNo)
                .sourceOfRawMaterial(manufacturer)
                .colorCode(colorCode)
                .quantity(quantity)
                .dateOfInspection(inspectionDate)
                .rows(rows)
                .build();
    }

    /**
     * Fetches and aggregates data for the Final Chemical Analysis Annexure (Annexure-VI).
     *
     * @param callNo The inspection call number
     * @return Aggregated report data
     */
    public FinalChemicalAnalysisResponseDTO getFinalChemicalAnalysisData(String callNo) {
        log.info("Fetching Final Chemical Analysis data for call no: {}", callNo);

        // 1. Fetch Basic Info / Header Metadata
        Optional<com.sarthi.entity.rawmaterial.InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(callNo);
        Optional<InspectionCompleteDetails> completeDetailsOpt = inspectionCompleteDetailsRepository.findByCallNo(callNo);
        Optional<FinalInspectionDetails> finalDetailsOpt = callOpt.flatMap(c -> finalInspectionDetailsRepository.findByIcId(c.getId()));

        String manufacturer = callOpt.map(com.sarthi.entity.rawmaterial.InspectionCall::getCompanyName).orElse("RITES LTD");
        String vendor = callOpt.map(com.sarthi.entity.rawmaterial.InspectionCall::getCompanyName).orElse("N/A");
        String certificateNo = completeDetailsOpt.map(InspectionCompleteDetails::getCertificateNo).orElse("N/A");
        String productName = "ELASTIC RAIL CLIP (ERC)"; // Default or fetch from call if available

        String inspectionDate = "N/A";
        if (callOpt.isPresent() && callOpt.get().getActualInspectionDate() != null) {
            inspectionDate = callOpt.get().getActualInspectionDate().format(DATE_FORMATTER);
        }

        // 2. Fetch Chemical Analysis technical data rows
        List<FinalChemicalAnalysis> testResults = finalChemicalAnalysisRepository.findByInspectionCallNo(callNo);
        List<FinalInspectionLotResults> lotResults = finalInspectionLotResultsRepository.findByInspectionCallNo(callNo);
        List<FinalChemicalAnalysisRowDTO> rows = new ArrayList<>();

        Long finalDetailId = finalDetailsOpt.map(FinalInspectionDetails::getId).orElse(null);

        int sNo = 1;
        for (FinalChemicalAnalysis result : testResults) {
            // Find Lot Details to get Quantity
            Optional<FinalInspectionLotDetails> lotOpt = Optional.empty();
            if (finalDetailId != null) {
                lotOpt = finalInspectionLotDetailsRepository
                        .findByFinalDetailIdAndLotNumber(finalDetailId, result.getLotNo());
            }

            // Determine status from lot results
            String status = "Accepted";
            Optional<FinalInspectionLotResults> matchStatus = lotResults.stream()
                .filter(lr -> lr.getLotNo().equals(result.getLotNo()))
                .findFirst();
            
            if (matchStatus.isPresent()) {
                String subStatus = matchStatus.get().getChemicalStatus();
                if ("NOT OK".equalsIgnoreCase(subStatus)) {
                    status = "Rejected";
                }
            }

            rows.add(FinalChemicalAnalysisRowDTO.builder()
                    .sNo(sNo++)
                    .heatNo(result.getHeatNo())
                    .lotNo(result.getLotNo())
                    .colourCode("-")
                    .qtyNo(lotOpt.map(FinalInspectionLotDetails::getOfferedQty).orElse(0))
                    .sampleSize(0) // Default or fetch if stored
                    .carbonPercent(result.getCarbonPercent())
                    .manganesePercent(result.getManganesePercent())
                    .siliconPercent(result.getSiliconPercent())
                    .sulphurPercent(result.getSulphurPercent())
                    .phosphorusPercent(result.getPhosphorusPercent())
                    .remarks(result.getRemarks())
                    .acceptedOrRejected(status)
                    .signOfSupervisor("")
                    .build());
        }

        return FinalChemicalAnalysisResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer(manufacturer)
                .vendor(vendor)
                .certificateNo(certificateNo)
                .productName(productName)
                .dateOfInspection(inspectionDate)
                .rows(rows)
                .build();
    }

    /**
     * Fetches and aggregates data for the Final Hardness Test Annexure (Annexure-VIII).
     * Splits data into multiple pages based on Heat/Lot and Sampling combination.
     *
     * @param callNo The inspection call number
     * @return Aggregated report data with multi-page support
     */
    public HardnessAnnexureResponseDTO getFinalHardnessTestData(String callNo) {
        log.info("Fetching Final Hardness Test data for call no: {}", callNo);

        // 1. Fetch Basic Info / Header Metadata
        Optional<com.sarthi.entity.rawmaterial.InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(callNo);
        Optional<InspectionCompleteDetails> completeDetailsOpt = inspectionCompleteDetailsRepository.findByCallNo(callNo);

        String manufacturer = callOpt.map(com.sarthi.entity.rawmaterial.InspectionCall::getCompanyName).orElse("RITES LTD");
        String vendor = callOpt.map(com.sarthi.entity.rawmaterial.InspectionCall::getCompanyName).orElse("N/A");
        String certificateNo = completeDetailsOpt.map(InspectionCompleteDetails::getCertificateNo).orElse("N/A");
        // Product name from inspection call ercType (standard across annexures)
        String productName = callOpt.map(InspectionCall::getErcType).orElse("ELASTIC RAIL CLIP");

        String inspectionDate = "N/A";
        if (callOpt.isPresent() && callOpt.get().getActualInspectionDate() != null) {
            inspectionDate = callOpt.get().getActualInspectionDate().format(DATE_FORMATTER);
        }

        // 2. Fetch technical data across tables
        List<FinalHardnessTest> hardnessTests = finalHardnessTestRepository.findByInspectionCallNo(callNo);
        List<FinalInspectionLotResults> lotResults = finalInspectionLotResultsRepository.findByInspectionCallNo(callNo);

        // Grouping logic: One page per (Heat/Lot + Sampling)
        List<HardnessAnnexurePageDTO> pages = new ArrayList<>();

        for (FinalHardnessTest test : hardnessTests) {
            // Find status for this Specific Lot+Heat
            Optional<FinalInspectionLotResults> matchStatus = lotResults.stream()
                .filter(lr -> lr.getLotNo().equals(test.getLotNo()) && (lr.getHeatNo() == null || lr.getHeatNo().equals(test.getHeatNo())))
                .findFirst();

            String mappedStatus = "N/A";
            if (matchStatus.isPresent()) {
                String subStatus = matchStatus.get().getHardnessStatus();
                if ("OK".equalsIgnoreCase(subStatus)) mappedStatus = "Accepted";
                else if ("NOT OK".equalsIgnoreCase(subStatus)) mappedStatus = "Not Accepted";
            }

            // Get samples grouped by sampling round
            List<FinalHardnessTestSample> allSamples = finalHardnessTestSampleRepository.findByFinalHardnessTestId(test.getId());
            Map<Integer, List<FinalHardnessTestSample>> samplingGroups = allSamples.stream()
                .collect(Collectors.groupingBy(FinalHardnessTestSample::getSamplingNo, java.util.TreeMap::new, Collectors.toList()));

            int runningCumulative = 0;
            boolean hasSecondSampling = samplingGroups.containsKey(2);

            for (Map.Entry<Integer, List<FinalHardnessTestSample>> entry : samplingGroups.entrySet()) {
                Integer samplingNo = entry.getKey();
                List<FinalHardnessTestSample> samplingSamples = entry.getValue();

                // Calculate defectives for this sampling based on 40-44 HRC range
                int currentDefectives = (int) samplingSamples.stream()
                    .filter(s -> isHardnessRejected(s.getSampleValue()))
                    .count();
                
                runningCumulative += currentDefectives;
                
                // Rule: 1st sampling cumulative is always 0. 2nd is 1st + 2nd.
                int displayCumulative = (samplingNo == 1) ? 0 : runningCumulative;

                // Determine status for this sampling page
                String statusText;
                if (samplingNo == 1 && hasSecondSampling) {
                    statusText = "Second sampling required";
                } else {
                    // Logic from user base: if cumulative <= 6 Accepted, else Rejected
                    statusText = (runningCumulative <= 6) ? "Accepted" : "Rejected";
                    
                    if ("Not Accepted".equals(mappedStatus)) statusText = "Rejected";
                    else if ("Accepted".equals(mappedStatus)) statusText = "Accepted";
                }

                // Build technical rows for this page
                List<HardnessAnnexurePageDTO.HardnessAnnexureRowDTO> rows = new ArrayList<>();
                List<List<BigDecimal>> readingsTable = new ArrayList<>();
                
                Map<Integer, List<BigDecimal>> rowBuckets = new java.util.TreeMap<>();
                for (FinalHardnessTestSample sample : samplingSamples) {
                    int rowIdx = (sample.getSampleNo() - 1) / 10;
                    rowBuckets.computeIfAbsent(rowIdx, k -> new ArrayList<>()).add(sample.getSampleValue());
                }
                
                for (List<BigDecimal> readings : rowBuckets.values()) {
                    readingsTable.add(readings);
                }

                rows.add(HardnessAnnexurePageDTO.HardnessAnnexureRowDTO.builder()
                        .heatNo(test.getHeatNo())
                        .lotNo(test.getLotNo())
                        .colourCode("-")
                        .qty(test.getQtyNo())
                        .sampleSize(samplingSamples.size())
                        .readings(readingsTable)
                        .defectives(currentDefectives)
                        .cumulativeDefectives(displayCumulative)
                        .status(statusText)
                        .build());

                pages.add(HardnessAnnexurePageDTO.builder()
                        .lotNo(test.getLotNo())
                        .heatNo(test.getHeatNo())
                        .samplingNo(samplingNo)
                        .qtyNo(test.getQtyNo())
                        .sampleSize(samplingSamples.size())
                        .dateOfInspection(test.getDateOfInspection() != null ? test.getDateOfInspection().format(DATE_FORMATTER) : inspectionDate)
                        .rows(rows)
                        .build());
            }
        }

        return HardnessAnnexureResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer(manufacturer)
                .vendor(vendor)
                .certificateNo(certificateNo)
                .productName(productName)
                .dateOfInspection(inspectionDate)
                .pages(pages)
                .build();
    }

    /**
     * Fetches and aggregates data for the Final Toe Load Test Annexure (Annexure-XI).
     */
    public ToeLoadAnnexureResponseDTO getFinalToeLoadTestData(String callNo) {
        // Fetch metadata
        InspectionCall call = inspectionCallRepository.findByIcNumber(callNo).orElse(null);
        String manufacturer = "RITES LTD (QA DIVISION)"; 
        String vendor = call != null ? call.getCompanyName() : "";
        String productName = call != null ? call.getErcType() : "ELASTIC RAIL CLIP MK-III";
        String inspectionDate = call != null && call.getActualInspectionDate() != null ? call.getActualInspectionDate().format(DATE_FORMATTER) : "";
        String certificateNo = "N/A";

        // Fetch Toe Load Data
        List<FinalToeLoadTest> toeLoadTests = finalToeLoadTestRepository.findByInspectionCallNo(callNo);
        List<FinalInspectionLotResults> lotResults = finalInspectionLotResultsRepository.findByInspectionCallNo(callNo);

        List<ToeLoadAnnexurePageDTO> pages = new ArrayList<>();

        for (FinalToeLoadTest test : toeLoadTests) {
            String mappedStatus = "N/A";
            Optional<FinalInspectionLotResults> matchStatus = lotResults.stream()
                .filter(lr -> lr.getLotNo().equals(test.getLotNo()) && (lr.getHeatNo() == null || lr.getHeatNo().equals(test.getHeatNo())))
                .findFirst();

            if (matchStatus.isPresent()) {
                String subStatus = matchStatus.get().getToeLoadStatus();
                if ("OK".equalsIgnoreCase(subStatus)) mappedStatus = "Accepted";
                else if ("NOT OK".equalsIgnoreCase(subStatus)) mappedStatus = "Not Accepted";
            }

            // Get samples grouped by sampling round
            List<FinalToeLoadTestSample> allSamples = finalToeLoadTestSampleRepository.findByFinalToeLoadTestId(test.getId());
            Map<Integer, List<FinalToeLoadTestSample>> samplingGroups = allSamples.stream()
                .collect(Collectors.groupingBy(FinalToeLoadTestSample::getSamplingNo));

            // Sort sampling rounds to calculate cumulative values correctly
            List<Integer> sortedSamplingNos = new ArrayList<>(samplingGroups.keySet());
            java.util.Collections.sort(sortedSamplingNos);

            int runningTotal = 0;
            int round1Defectives = 0;

            for (Integer samplingNo : sortedSamplingNos) {
                List<FinalToeLoadTestSample> samplingSamples = samplingGroups.get(samplingNo);

                // Calculate defectives for this specific round based on ERC Type range
                int currentDefectives = (int) samplingSamples.stream().filter(s -> isToeLoadRejected(productName, s.getSampleValue())).count();
                
                runningTotal += currentDefectives;
                if (samplingNo == 1) round1Defectives = currentDefectives;
                
                // Rule: 1st sampling cumulative is always 0. 2nd is 1st + 2nd.
                int displayCumulative = (samplingNo == 1) ? 0 : runningTotal;

                List<ToeLoadBatchDTO> rows = new ArrayList<>();
                List<List<BigDecimal>> readingsTable = new ArrayList<>();
                
                // Group by "logical" sample row (10 readings per row)
                Map<Integer, List<BigDecimal>> rowBuckets = new java.util.TreeMap<>();
                for (FinalToeLoadTestSample sample : samplingSamples) {
                    int rowIdx = (sample.getSampleNo() - 1) / 10;
                    rowBuckets.computeIfAbsent(rowIdx, k -> new ArrayList<>()).add(sample.getSampleValue());
                }
                
                for (List<BigDecimal> readings : rowBuckets.values()) {
                    readingsTable.add(readings);
                }

                rows.add(ToeLoadBatchDTO.builder()
                        .heatNo(test.getHeatNo())
                        .lotNo(test.getLotNo())
                        .colourCode("-") 
                        .qty(test.getQtyNo())
                        .sampleSize(samplingSamples.size())
                        .readings(readingsTable)
                        .defectives(currentDefectives)
                        .cumulativeDefectives(displayCumulative)
                        .status(mappedStatus)
                        .build());

                pages.add(ToeLoadAnnexurePageDTO.builder()
                        .samplingNo(samplingNo)
                        .heatNo(test.getHeatNo())
                        .lotNo(test.getLotNo())
                        .rows(rows)
                        .build());
            }
        }

        return ToeLoadAnnexureResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer(manufacturer)
                .vendor(vendor)
                .certificateNo(certificateNo)
                .productName(productName)
                .dateOfInspection(inspectionDate)
                .pages(pages)
                .build();
    }

    /**
     * Fetches and aggregates data for the Final Weight Test Annexure (Annexure-XV).
     */
    public WeightAnnexureResponseDTO getFinalWeightTestData(String callNo) {
        log.info("Fetching Final Weight Test data for call no: {}", callNo);

        // Fetch metadata
        InspectionCall call = inspectionCallRepository.findByIcNumber(callNo).orElse(null);
        String manufacturer = "RITES LTD (QA DIVISION)";
        String vendor = call != null ? call.getCompanyName() : "";
        String productName = call != null ? call.getErcType() : "ELASTIC RAIL CLIP MK-III";
        String inspectionDate = call != null && call.getActualInspectionDate() != null ? call.getActualInspectionDate().format(DATE_FORMATTER) : "";
        String certificateNo = "N/A";

        // Fetch Weight Data
        List<FinalWeightTest> weightTests = finalWeightTestRepository.findByInspectionCallNo(callNo);
        List<FinalInspectionLotResults> lotResults = finalInspectionLotResultsRepository.findByInspectionCallNo(callNo);

        List<WeightAnnexurePageDTO> pages = new ArrayList<>();

        for (FinalWeightTest test : weightTests) {
            String mappedStatus = "N/A";
            Optional<FinalInspectionLotResults> matchStatus = lotResults.stream()
                .filter(lr -> lr.getLotNo().equals(test.getLotNo()) && (lr.getHeatNo() == null || lr.getHeatNo().equals(test.getHeatNo())))
                .findFirst();

            if (matchStatus.isPresent()) {
                String subStatus = matchStatus.get().getWeightStatus();
                if ("OK".equalsIgnoreCase(subStatus)) mappedStatus = "Accepted";
                else if ("NOT OK".equalsIgnoreCase(subStatus)) mappedStatus = "Not Accepted";
            }

            // Get samples grouped by sampling round
            List<FinalWeightTestSample> allSamples = finalWeightTestSampleRepository.findByFinalWeightTestId(test.getId());
            Map<Integer, List<FinalWeightTestSample>> samplingGroups = allSamples.stream()
                .collect(Collectors.groupingBy(FinalWeightTestSample::getSamplingNo));

            // Sort sampling rounds to calculate cumulative values correctly
            List<Integer> sortedSamplingNos = new ArrayList<>(samplingGroups.keySet());
            java.util.Collections.sort(sortedSamplingNos);

            int runningTotal = 0;
            int round1Defectives = 0;

            for (Integer samplingNo : sortedSamplingNos) {
                List<FinalWeightTestSample> samplingSamples = samplingGroups.get(samplingNo);

                // Calculate defectives for this specific round based on ERC Type range
                int currentDefectives = (int) samplingSamples.stream().filter(s -> isWeightRejected(productName, s.getSampleValue())).count();
                
                runningTotal += currentDefectives;
                if (samplingNo == 1) round1Defectives = currentDefectives;

                // Rule: 1st sampling cumulative is always 0. 2nd is 1st + 2nd.
                int displayCumulative = (samplingNo == 1) ? 0 : runningTotal;

                List<WeightBatchDTO> rows = new ArrayList<>();
                List<List<BigDecimal>> readingsTable = new ArrayList<>();
                
                // Group by "logical" sample row (10 readings per row)
                Map<Integer, List<BigDecimal>> rowBuckets = new java.util.TreeMap<>();
                for (FinalWeightTestSample sample : samplingSamples) {
                    int rowIdx = (sample.getSampleNo() - 1) / 10;
                    rowBuckets.computeIfAbsent(rowIdx, k -> new ArrayList<>()).add(sample.getSampleValue());
                }
                
                for (List<BigDecimal> readings : rowBuckets.values()) {
                    readingsTable.add(readings);
                }

                rows.add(WeightBatchDTO.builder()
                        .heatNo(test.getHeatNo())
                        .lotNo(test.getLotNo())
                        .colourCode("-") 
                        .qty(test.getQtyNo())
                        .sampleSize(samplingSamples.size())
                        .readings(readingsTable)
                        .defectives(currentDefectives)
                        .cumulativeDefectives(displayCumulative)
                        .status(mappedStatus)
                        .build());

                pages.add(WeightAnnexurePageDTO.builder()
                        .samplingNo(samplingNo)
                        .heatNo(test.getHeatNo())
                        .lotNo(test.getLotNo())
                        .rows(rows)
                        .build());
            }
        }

        return WeightAnnexureResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer(manufacturer)
                .vendor(vendor)
                .certificateNo(certificateNo)
                .productName(productName)
                .dateOfInspection(inspectionDate)
                .pages(pages)
                .build();
    }

    /**
     * Fetches and aggregates data for the Final Dimensional Inspection Annexure (Annexure-IX).
     *
     * @param callNo The inspection call number
     * @return Aggregated dimensional inspection report data
     */
    public FinalDimensionalAnnexureResponseDTO getFinalDimensionalAnnexureData(String callNo) {
        log.info("Fetching Final Dimensional Inspection data for call no: {}", callNo);

        // 1. Fetch Metadata
        Optional<com.sarthi.entity.rawmaterial.InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(callNo);
        Optional<InspectionCompleteDetails> completeDetailsOpt = inspectionCompleteDetailsRepository.findByCallNo(callNo);
        Optional<FinalInspectionDetails> finalDetailsOpt = callOpt.flatMap(c -> finalInspectionDetailsRepository.findByIcId(c.getId()));

        String manufacturer = callOpt.map(com.sarthi.entity.rawmaterial.InspectionCall::getCompanyName).orElse("RITES LTD");
        String vendor = manufacturer;
        String certificateNo = completeDetailsOpt.map(InspectionCompleteDetails::getCertificateNo).orElse("N/A");
        String productName = callOpt.map(InspectionCall::getErcType).orElse("ELASTIC RAIL CLIP");
        String inspectionDate = callOpt.map(c -> c.getActualInspectionDate() != null ? c.getActualInspectionDate().format(DATE_FORMATTER) : "N/A").orElse("N/A");

        Long finalDetailId = finalDetailsOpt.map(FinalInspectionDetails::getId).orElse(null);

        // 2. Fetch technical data from FLAT table
        List<FinalDimensionalInspectionFlat> dimTests = finalDimensionalInspectionFlatRepository.findByInspectionCallNo(callNo);
        List<FinalDimensionalAnnexurePageDTO> pages = new ArrayList<>();

        for (FinalDimensionalInspectionFlat test : dimTests) {
            // Fetch parent to get sample size
            Optional<FinalDimensionalInspection> parentOpt = finalDimensionalInspectionRepository
                    .findByInspectionCallNoAndLotNoAndHeatNo(callNo, test.getLotNo(), test.getHeatNo());
            Integer sampleSize = parentOpt.map(FinalDimensionalInspection::getSampleSize).orElse(0);

            // 1st Sampling Page
            if (test.getFirstSampleGoGaugeFail() != null || test.getFirstSampleNoGoFail() != null) {
                pages.add(createDimensionalPage(test, 1, finalDetailId, sampleSize, 0));
            }
            int d1 = (test.getFirstSampleGoGaugeFail() != null ? test.getFirstSampleGoGaugeFail() : 0) +
                     (test.getFirstSampleNoGoFail() != null ? test.getFirstSampleNoGoFail() : 0) +
                     (test.getFirstSampleFlatBearingFail() != null ? test.getFirstSampleFlatBearingFail() : 0);

            // 2nd Sampling Page (if exists)
            // We consider the second sample to exist if there were defectives in the first sample (d1 > 0)
            // OR if the second sample explicitly recorded defectives itself (d2 > 0).
            // If d1 == 0, a second sample is never required, so we prevent duplicating the page.
            int d2 = (test.getSecondSampleGoGaugeFail() != null ? test.getSecondSampleGoGaugeFail() : 0) +
                     (test.getSecondSampleNoGoFail() != null ? test.getSecondSampleNoGoFail() : 0) +
                     (test.getSecondSampleFlatBearingFail() != null ? test.getSecondSampleFlatBearingFail() : 0);
                     
            boolean hasSecond = (d1 > 0 || d2 > 0);
            
            if (hasSecond) {
                int cumulative2 = d1 + d2;
                pages.add(createDimensionalPage(test, 2, finalDetailId, sampleSize, cumulative2));
            }
        }

        return FinalDimensionalAnnexureResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer(manufacturer)
                .vendor(vendor)
                .certificateNo(certificateNo)
                .productName(productName)
                .dateOfInspection(inspectionDate)
                .pages(pages)
                .build();
    }

    private FinalDimensionalAnnexurePageDTO createDimensionalPage(FinalDimensionalInspectionFlat test, int samplingNo, Long finalDetailId, Integer sampleSize, int cumulativeDefectives) {
        // Fetch lot details for quantity
        String qty = "0";
        if (finalDetailId != null) {
            Optional<FinalInspectionLotDetails> lotOpt = finalInspectionLotDetailsRepository.findByFinalDetailIdAndLotNumber(finalDetailId, test.getLotNo());
            if (lotOpt.isPresent()) {
                qty = lotOpt.get().getOfferedQty() != null ? lotOpt.get().getOfferedQty().toString() : "0";
            }
        }

        // Map values based on sampling round
        String mainBoxGo = null;
        String mainBoxNoGo = null;
        String fallingGo = null;
        String fallingNoGo = null;
        String flatBearingGo = null;
        String flatBearingNoGo = null;
        Integer defectives = 0;

        if (samplingNo == 1) {
            mainBoxGo = test.getFirstSampleMainBoxGo() != null ? String.valueOf(test.getFirstSampleMainBoxGo()) : null;
            mainBoxNoGo = test.getFirstSampleMainBoxNoGo() != null ? String.valueOf(test.getFirstSampleMainBoxNoGo()) : null;
            fallingGo = test.getFirstSampleFallingGo() != null ? String.valueOf(test.getFirstSampleFallingGo()) : null;
            fallingNoGo = test.getFirstSampleFallingNoGo() != null ? String.valueOf(test.getFirstSampleFallingNoGo()) : null;
            flatBearingGo = test.getFirstSampleFlatBearingGo() != null ? String.valueOf(test.getFirstSampleFlatBearingGo()) : null;
            flatBearingNoGo = test.getFirstSampleFlatBearingNoGo() != null ? String.valueOf(test.getFirstSampleFlatBearingNoGo()) : null;
            
            defectives = (test.getFirstSampleGoGaugeFail() != null ? test.getFirstSampleGoGaugeFail() : 0) +
                         (test.getFirstSampleNoGoFail() != null ? test.getFirstSampleNoGoFail() : 0) +
                         (test.getFirstSampleFlatBearingFail() != null ? test.getFirstSampleFlatBearingFail() : 0);
        } else {
            mainBoxGo = test.getSecondSampleMainBoxGo() != null ? String.valueOf(test.getSecondSampleMainBoxGo()) : null;
            mainBoxNoGo = test.getSecondSampleMainBoxNoGo() != null ? String.valueOf(test.getSecondSampleMainBoxNoGo()) : null;
            fallingGo = test.getSecondSampleFallingGo() != null ? String.valueOf(test.getSecondSampleFallingGo()) : null;
            fallingNoGo = test.getSecondSampleFallingNoGo() != null ? String.valueOf(test.getSecondSampleFallingNoGo()) : null;
            flatBearingGo = test.getSecondSampleFlatBearingGo() != null ? String.valueOf(test.getSecondSampleFlatBearingGo()) : null;
            flatBearingNoGo = test.getSecondSampleFlatBearingNoGo() != null ? String.valueOf(test.getSecondSampleFlatBearingNoGo()) : null;
            
            defectives = (test.getSecondSampleGoGaugeFail() != null ? test.getSecondSampleGoGaugeFail() : 0) +
                         (test.getSecondSampleNoGoFail() != null ? test.getSecondSampleNoGoFail() : 0) +
                         (test.getSecondSampleFlatBearingFail() != null ? test.getSecondSampleFlatBearingFail() : 0);
        }

        String pageStatus = "Accepted";
        if ("NOT OK".equalsIgnoreCase(test.getStatus())) {
            pageStatus = "Rejected";
        }

        List<FinalDimensionalAnnexurePageDTO.DimensionalRowDTO> rows = new ArrayList<>();
        rows.add(FinalDimensionalAnnexurePageDTO.DimensionalRowDTO.builder()
                .sNo(1)
                .heatNo(test.getHeatNo())
                .lotNo(test.getLotNo())
                .colourCode("-")
                .qty(qty)
                .sampleSize(sampleSize)
                .mainBoxGo(mainBoxGo)
                .mainBoxNoGo(mainBoxNoGo)
                .fallingGo(fallingGo)
                .fallingNoGo(fallingNoGo)
                .flatBearingGo(flatBearingGo)
                .flatBearingNoGo(flatBearingNoGo)
                .defectives(defectives)
                .cumulativeDefectives(cumulativeDefectives)
                .status(pageStatus)
                .build());

        return FinalDimensionalAnnexurePageDTO.builder()
                .heatNo(test.getHeatNo())
                .lotNo(test.getLotNo())
                .samplingNo(samplingNo)
                .sampleSize(sampleSize)
                .colourCode("-")
                .quantity(qty)
                .status(pageStatus)
                .rows(rows)
                .build();
    }

    public FinalInclusionAnnexureResponseDTO getFinalInclusionAnnexureData(String callNo) {
        log.info("Fetching Process Inspection Annexure data for call no: {}", callNo);

        Optional<InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(callNo);
        Optional<InspectionCompleteDetails> completeDetailsOpt = inspectionCompleteDetailsRepository.findByCallNo(callNo);
        
        String manufacturer = callOpt.map(InspectionCall::getCompanyName).orElse("RITES LTD");
        String vendor = manufacturer; 
        String certificateNo = completeDetailsOpt.map(InspectionCompleteDetails::getCertificateNo).orElse("N/A");
        String productName = callOpt.map(InspectionCall::getErcType).orElse("ELASTIC RAIL CLIP");
        String inspectionDate = callOpt.map(c -> c.getActualInspectionDate() != null ? c.getActualInspectionDate().format(DATE_FORMATTER) : "N/A").orElse("N/A");
        
        Optional<FinalInspectionDetails> finalDetailsOpt = callOpt.flatMap(c -> finalInspectionDetailsRepository.findByIcId(c.getId()));
        Long finalDetailId = finalDetailsOpt.map(FinalInspectionDetails::getId).orElse(null);
        
        String quantity = "N/A";

        List<FinalInclusionRatingNew> inclTests = finalInclusionRatingNewRepository.findByInspectionCallNo(callNo);
        List<FinalMicrostructureTest> microTests = finalMicrostructureTestRepository.findByInspectionCallNo(callNo);
        List<FinalFreedomFromDefectsTest> freedomTests = finalFreedomFromDefectsTestRepository.findByInspectionCallNo(callNo);
        List<FinalDepthOfDecarburization> decarbTests = finalDepthOfDecarburizationRepository.findByInspectionCallNo(callNo);

        // BATCH FETCH SAMPLES to avoid N+1 performance issues (8-9 sec delay fix)
        List<Long> inclIds = inclTests.stream().map(FinalInclusionRatingNew::getId).collect(Collectors.toList());
        List<FinalInclusionRatingSample> allInclSamples = inclIds.isEmpty() ? new ArrayList<>() : 
            finalInclusionRatingSampleRepository.findByFinalInclusionRatingIdIn(inclIds);
        Map<Long, List<FinalInclusionRatingSample>> inclSamplesByTestId = allInclSamples.stream()
            .collect(Collectors.groupingBy(s -> s.getFinalInclusionRating().getId()));

        List<Long> microIds = microTests.stream().map(FinalMicrostructureTest::getId).collect(Collectors.toList());
        List<FinalMicrostructureSample> allMicroSamples = microIds.isEmpty() ? new ArrayList<>() :
            finalMicrostructureSampleRepository.findByFinalMicrostructureTestIdIn(microIds);
        Map<Long, List<FinalMicrostructureSample>> microSamplesByTestId = allMicroSamples.stream()
            .collect(Collectors.groupingBy(s -> s.getFinalMicrostructureTest().getId()));

        List<Long> freedomIds = freedomTests.stream().map(FinalFreedomFromDefectsTest::getId).collect(Collectors.toList());
        List<FinalFreedomFromDefectsSample> allFreedomSamples = freedomIds.isEmpty() ? new ArrayList<>() :
            finalFreedomFromDefectsSampleRepository.findByFinalFreedomFromDefectsTestIdIn(freedomIds);
        Map<Long, List<FinalFreedomFromDefectsSample>> freedomSamplesByTestId = allFreedomSamples.stream()
            .collect(Collectors.groupingBy(s -> s.getFinalFreedomFromDefectsTest().getId()));

        List<Long> decarbIds = decarbTests.stream().map(FinalDepthOfDecarburization::getId).collect(Collectors.toList());
        List<FinalDepthOfDecarburizationSample> allDecarbSamples = decarbIds.isEmpty() ? new ArrayList<>() :
            finalDepthOfDecarburizationSampleRepository.findByFinalDepthOfDecarburizationIdIn(decarbIds);
        Map<Long, List<FinalDepthOfDecarburizationSample>> decarbSamplesByTestId = allDecarbSamples.stream()
            .collect(Collectors.groupingBy(s -> s.getFinalDepthOfDecarburization().getId()));

        java.util.Set<String> uniqueHeatLots = new java.util.HashSet<>();
        inclTests.forEach(t -> uniqueHeatLots.add(t.getHeatNo() + "|||" + t.getLotNo()));
        microTests.forEach(t -> uniqueHeatLots.add(t.getHeatNo() + "|||" + t.getLotNo()));
        freedomTests.forEach(t -> uniqueHeatLots.add(t.getHeatNo() + "|||" + t.getLotNo()));
        decarbTests.forEach(t -> uniqueHeatLots.add(t.getHeatNo() + "|||" + t.getLotNo()));

        List<FinalInclusionAnnexurePageDTO> pages = new ArrayList<>();

        for (String key : uniqueHeatLots) {
            String[] parts = key.split("\\|\\|\\|");
            String matchHeat = parts[0];
            String matchLot = parts.length > 1 ? parts[1] : "";

            Optional<FinalInclusionRatingNew> inclTestOpt = inclTests.stream()
                .filter(t -> t.getHeatNo().equals(matchHeat) && t.getLotNo().equals(matchLot)).findFirst();
            List<FinalInclusionRatingSample> inclSamples = inclTestOpt.isPresent() ? 
                inclSamplesByTestId.getOrDefault(inclTestOpt.get().getId(), new ArrayList<>()) : new ArrayList<>();
            Map<Integer, List<FinalInclusionRatingSample>> inclSamplesBySamplings = inclSamples.stream()
                .collect(Collectors.groupingBy(FinalInclusionRatingSample::getSamplingNo));

            Optional<FinalMicrostructureTest> microTestOpt = microTests.stream()
                .filter(t -> t.getHeatNo().equals(matchHeat) && t.getLotNo().equals(matchLot)).findFirst();
            List<FinalMicrostructureSample> microSamples = microTestOpt.isPresent() ? 
                microSamplesByTestId.getOrDefault(microTestOpt.get().getId(), new ArrayList<>()) : new ArrayList<>();
            Map<Integer, List<FinalMicrostructureSample>> microSamplesBySamplings = microSamples.stream()
                .collect(Collectors.groupingBy(FinalMicrostructureSample::getSamplingNo));

            Optional<FinalFreedomFromDefectsTest> freedomTestOpt = freedomTests.stream()
                .filter(t -> t.getHeatNo().equals(matchHeat) && t.getLotNo().equals(matchLot)).findFirst();
            List<FinalFreedomFromDefectsSample> freedomSamples = freedomTestOpt.isPresent() ? 
                freedomSamplesByTestId.getOrDefault(freedomTestOpt.get().getId(), new ArrayList<>()) : new ArrayList<>();
            Map<Integer, List<FinalFreedomFromDefectsSample>> freedomSamplesBySamplings = freedomSamples.stream()
                .collect(Collectors.groupingBy(FinalFreedomFromDefectsSample::getSamplingNo));

            Optional<FinalDepthOfDecarburization> decarbTestOpt = decarbTests.stream()
                .filter(t -> t.getHeatNo().equals(matchHeat) && t.getLotNo().equals(matchLot)).findFirst();
            List<FinalDepthOfDecarburizationSample> decarbSamples = decarbTestOpt.isPresent() ? 
                decarbSamplesByTestId.getOrDefault(decarbTestOpt.get().getId(), new ArrayList<>()) : new ArrayList<>();
            Map<Integer, List<FinalDepthOfDecarburizationSample>> decarbSamplesBySamplings = decarbSamples.stream()
                .collect(Collectors.groupingBy(FinalDepthOfDecarburizationSample::getSamplingNo));

            // Fetch lot details outside sampling loop for performance
            String pageColourCode = "-";
            String pageQty = "0";
            if (finalDetailId != null) {
                Optional<FinalInspectionLotDetails> lotOpt = finalInspectionLotDetailsRepository.findByFinalDetailIdAndLotNumber(finalDetailId, matchLot);
                if (lotOpt.isPresent()) {
                    // pageColourCode = lotOpt.get().getManufacturerHeat(); // Not required for Final Stage
                    pageQty = lotOpt.get().getOfferedQty() != null ? lotOpt.get().getOfferedQty().toString() : "0";
                }
            }

            java.util.Set<Integer> allSamplings = new java.util.TreeSet<>();
            allSamplings.addAll(inclSamplesBySamplings.keySet());
            allSamplings.addAll(microSamplesBySamplings.keySet());
            allSamplings.addAll(freedomSamplesBySamplings.keySet());
            allSamplings.addAll(decarbSamplesBySamplings.keySet());
            if (allSamplings.isEmpty()) allSamplings.add(1);

            Integer maxSampleSize = 0;
            if(inclTestOpt.isPresent() && inclTestOpt.get().getSampleSize() != null) maxSampleSize = Math.max(maxSampleSize, inclTestOpt.get().getSampleSize());
            if(microTestOpt.isPresent() && microTestOpt.get().getSampleSize() != null) maxSampleSize = Math.max(maxSampleSize, microTestOpt.get().getSampleSize());
            if(freedomTestOpt.isPresent() && freedomTestOpt.get().getSampleSize() != null) maxSampleSize = Math.max(maxSampleSize, freedomTestOpt.get().getSampleSize());
            if(decarbTestOpt.isPresent() && decarbTestOpt.get().getSampleSize() != null) maxSampleSize = Math.max(maxSampleSize, decarbTestOpt.get().getSampleSize());

            for (Integer samplingNo : allSamplings) {
                FinalInclusionAnnexurePageDTO page = FinalInclusionAnnexurePageDTO.builder()
                    .heatNo(matchHeat)
                    .lotNo(matchLot)
                    .samplingNo(samplingNo)
                    .sampleSize(maxSampleSize)
                    .colourCode(pageColourCode)
                    .quantity(pageQty)
                    .rows(new ArrayList<>())
                    .build();

                // Collect all unique sample numbers for this sampling round
                java.util.Set<Integer> sampleNos = new java.util.TreeSet<>();
                inclSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>()).forEach(s -> sampleNos.add(s.getSampleNo()));
                microSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>()).forEach(s -> sampleNos.add(s.getSampleNo()));
                freedomSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>()).forEach(s -> sampleNos.add(s.getSampleNo()));
                decarbSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>()).forEach(s -> sampleNos.add(s.getSampleNo()));

                // Pre-calculate summarized results for the first row
                String concatenatedDecarb = "-";
                String overallMicro = "-";
                String overallFreedom = "-";
                String lotStatus = "-";

                // Check for overall lot failures across all samples
                boolean inclusionFailed = false;
                List<FinalInclusionRatingSample> allIncl = inclSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>());
                for (FinalInclusionRatingSample s : allIncl) {
                    if (isGtTwo(s.getSampleValueA()) || isGtTwo(s.getSampleValueB()) || 
                        isGtTwo(s.getSampleValueC()) || isGtTwo(s.getSampleValueD())) {
                        inclusionFailed = true;
                        break;
                    }
                }

                List<FinalDepthOfDecarburizationSample> dSamples = decarbSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>());
                boolean decarbFailed = dSamples.stream().anyMatch(d -> d.getSampleValue() != null && d.getSampleValue().compareTo(new BigDecimal("0.23")) > 0);
                boolean hasDecarbSecond = decarbSamplesBySamplings.containsKey(2);

                if (!dSamples.isEmpty()) {
                    String values = dSamples.stream()
                        .map(d -> d.getSampleValue() != null ? d.getSampleValue().toString() : "-")
                        .collect(Collectors.joining(", "));
                    String decarbStatusStr = (samplingNo == 1 && hasDecarbSecond) ? "Second sampling is required" : 
                        (decarbFailed ? "Not satisfactory" : "Satisfactory");
                    concatenatedDecarb = values + "\n(" + decarbStatusStr + ")";
                }

                List<FinalMicrostructureSample> mSamples = microSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>());
                boolean microFailed = false;
                boolean hasMicroSecond = microSamplesBySamplings.containsKey(2);
                if (!mSamples.isEmpty()) {
                    if (samplingNo == 1 && hasMicroSecond) overallMicro = "Retest required";
                    else {
                        overallMicro = mSamples.get(0).getSampleType() != null ? mSamples.get(0).getSampleType() : "-";
                        microFailed = !"Tempered Martensite".equalsIgnoreCase(overallMicro);
                    }
                }

                List<FinalFreedomFromDefectsSample> fSamples = freedomSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>());
                boolean freedomFailed = false;
                boolean hasFreedomSecond = freedomSamplesBySamplings.containsKey(2);
                if (!fSamples.isEmpty()) {
                    if (samplingNo == 1 && hasFreedomSecond) overallFreedom = "Retest required";
                    else {
                        String fType = fSamples.get(0).getSampleType();
                        overallFreedom = fType != null ? ("OK".equalsIgnoreCase(fType) ? "Satisfactory" : "Not satisfactory") : "-";
                        freedomFailed = "Not satisfactory".equalsIgnoreCase(overallFreedom);
                    }
                }

                // Final Status Logic:
                // 1. If Sampling 1 and any retest is triggered -> "Second sampling is required"
                // 2. If no retest triggered:
                //    - If any failure (Inclusion, Decarb, Micro, Freedom) -> "Rejected"
                //    - Else -> "Accepted"
                
                boolean anyRetestTriggered = (samplingNo == 1) && (hasDecarbSecond || hasMicroSecond || hasFreedomSecond);

                if (anyRetestTriggered) {
                    lotStatus = "Second sampling is required";
                } else {
                    if (inclusionFailed || decarbFailed || microFailed || freedomFailed) {
                        lotStatus = "Rejected";
                    } else {
                        // Only set Accepted if there is actually some data to accept
                        boolean hasData = !allIncl.isEmpty() || !dSamples.isEmpty() || !mSamples.isEmpty() || !fSamples.isEmpty();
                        lotStatus = hasData ? "Accepted" : "-";
                    }
                }
                page.setOverallStatus(lotStatus);

                int rowSNo = 1;
                for (Integer sNum : sampleNos) {
                    boolean isFirst = (rowSNo == 1);
                    FinalInclusionAnnexurePageDTO.InclusionRowDTO row = FinalInclusionAnnexurePageDTO.InclusionRowDTO.builder()
                        .sNo(isFirst ? 1 : null)
                        .heatNo(isFirst ? matchHeat : "")
                        .lotNo(isFirst ? matchLot : "")
                        .colourCode(isFirst ? pageColourCode : "")
                        .quantity(isFirst ? pageQty : "")
                        .sampleSize(isFirst ? maxSampleSize.toString() : "")
                        .sampleNo(sNum.toString())
                        .inclusionAThin("-").inclusionAThick("-")
                        .inclusionBThin("-").inclusionBThick("-")
                        .inclusionCThin("-").inclusionCThick("-")
                        .inclusionDThin("-").inclusionDThick("-")
                        .microstructureResult(isFirst ? overallMicro : "")
                        .freedomResult(isFirst ? overallFreedom : "")
                        .decarbResult(isFirst ? concatenatedDecarb : "")
                        .build();

                    // Inclusion - always populate per row
                    inclSamplesBySamplings.getOrDefault(samplingNo, new ArrayList<>()).stream()
                        .filter(s -> s.getSampleNo().equals(sNum)).findFirst().ifPresent(s -> {
                            String tA = s.getSampleTypeA() != null ? s.getSampleTypeA().trim() : "";
                            String valA = s.getSampleValueA() != null ? s.getSampleValueA().trim() : "";
                            if ("Thick".equalsIgnoreCase(tA)) row.setInclusionAThick(valA);
                            else if (!valA.isEmpty() && !"-".equals(valA)) row.setInclusionAThin(valA);

                            String tB = s.getSampleTypeB() != null ? s.getSampleTypeB().trim() : "";
                            String valB = s.getSampleValueB() != null ? s.getSampleValueB().trim() : "";
                            if ("Thick".equalsIgnoreCase(tB)) row.setInclusionBThick(valB);
                            else if (!valB.isEmpty() && !"-".equals(valB)) row.setInclusionBThin(valB);

                            String tC = s.getSampleTypeC() != null ? s.getSampleTypeC().trim() : "";
                            String valC = s.getSampleValueC() != null ? s.getSampleValueC().trim() : "";
                            if ("Thick".equalsIgnoreCase(tC)) row.setInclusionCThick(valC);
                            else if (!valC.isEmpty() && !"-".equals(valC)) row.setInclusionCThin(valC);

                            String tD = s.getSampleTypeD() != null ? s.getSampleTypeD().trim() : "";
                            String valD = s.getSampleValueD() != null ? s.getSampleValueD().trim() : "";
                            if ("Thick".equalsIgnoreCase(tD)) row.setInclusionDThick(valD);
                            else if (!valD.isEmpty() && !"-".equals(valD)) row.setInclusionDThin(valD);
                        });

                    page.getRows().add(row);
                    rowSNo++;
                }

                // Filtering: Only keep page if it has ANY meaningful data or indicates a retest
                boolean hasMeaningfulData = page.getRows().stream().anyMatch(r -> 
                    isNotBlank(r.getInclusionAThin()) || isNotBlank(r.getInclusionAThick()) ||
                    isNotBlank(r.getInclusionBThin()) || isNotBlank(r.getInclusionBThick()) ||
                    isNotBlank(r.getInclusionCThin()) || isNotBlank(r.getInclusionCThick()) ||
                    isNotBlank(r.getInclusionDThin()) || isNotBlank(r.getInclusionDThick()) ||
                    (isNotBlank(r.getDecarbResult()) && !"-".equals(r.getDecarbResult())) ||
                    (isNotBlank(r.getMicrostructureResult()) && !"-".equals(r.getMicrostructureResult())) ||
                    (isNotBlank(r.getFreedomResult()) && !"-".equals(r.getFreedomResult()))
                );

                if (hasMeaningfulData || anyRetestTriggered) {
                    pages.add(page);
                }
            }
        }

        return FinalInclusionAnnexureResponseDTO.builder()
            .inspectionCallNo(callNo)
            .manufacturer(manufacturer)
            .vendor(vendor)
            .certificateNo(certificateNo)
            .productName(productName)
            .dateOfInspection(inspectionDate)
            .quantity(quantity)
            .pages(pages)
            .build();
    }

    private boolean isGtTwo(String val) {
        if (val == null || val.trim().isEmpty() || "-".equals(val.trim())) return false;
        try {
            return new BigDecimal(val.trim()).compareTo(new BigDecimal("2.0")) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty() && !"-".equals(value.trim());
    }

    private String formatValue(Object val) {
        return val != null ? val.toString() : "0.0";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Endpoint to fetch data for Application & Deflection Annexure.
     */
    public FinalApplicationDeflectionResponseDTO getFinalApplicationDeflectionData(String callNo) {
        log.info("Fetching Application & Deflection data for call: {}", callNo);

        InspectionCall call = inspectionCallRepository.findByIcNumber(callNo)
                .orElseThrow(() -> new RuntimeException("Inspection call not found"));

        List<FinalApplicationDeflection> tests = finalApplicationDeflectionRepository.findByInspectionCallNo(callNo);
        List<FinalApplicationDeflectionResponseDTO.ApplicationDeflectionPageDTO> pages = new ArrayList<>();

        // Get total offered qty from FinalInspectionDetails
        Optional<FinalInspectionDetails> finalDetailsOpt = finalInspectionDetailsRepository.findByIcId(call.getId());
        String totalOfferedQty = finalDetailsOpt.map(fd -> fd.getTotalOfferedQty() != null ? fd.getTotalOfferedQty().toString() : "0").orElse("0");

        for (FinalApplicationDeflection test : tests) {
            List<FinalApplicationDeflectionSample> samples = test.getSamples();
            if (samples == null) samples = new ArrayList<>();
            
            // Sort samples by samplingNo to ensure cumulative calculation is correct
            samples.sort(java.util.Comparator.comparing(s -> s.getSamplingNo() == null ? 1 : s.getSamplingNo()));

            // Check if second sampling exists for this lot
            boolean hasSecondSampling = samples.stream().anyMatch(s -> s.getSamplingNo() != null && s.getSamplingNo() > 1);

            int cumulativeDefectives = 0;
            for (FinalApplicationDeflectionSample sample : samples) {
                int defectives = sample.getNoOfSamplesFailed() != null ? sample.getNoOfSamplesFailed() : 0;
                cumulativeDefectives += defectives;
                String testResult = (defectives == 0) ? "Satisfactory" : "Not Satisfactory";
                
                String statusText;
                if (hasSecondSampling && (sample.getSamplingNo() == null || sample.getSamplingNo() == 1)) {
                    statusText = "Second sampling required";
                } else {
                    statusText = (defectives == 0) ? "Accepted" : "Rejected";
                }

                pages.add(FinalApplicationDeflectionResponseDTO.ApplicationDeflectionPageDTO.builder()
                        .heatNo(test.getHeatNo())
                        .lotNo(test.getLotNo())
                        .colourCode("-") // Standard requirement for final stage
                        .quantity(totalOfferedQty)
                        .sampleSize(test.getSampleSize() != null ? test.getSampleSize().toString() : "0")
                        .samplingNo(sample.getSamplingNo())
                        .noOfDefectives(defectives)
                        .cumulativeDefectives(cumulativeDefectives)
                        .testResult(testResult)
                        .status(statusText)
                        .build());
            }
        }

        return FinalApplicationDeflectionResponseDTO.builder()
                .inspectionCallNo(callNo)
                .manufacturer("RITES LTD (QA DIVISION)")
                .vendor(call.getCompanyName())
                .certificateNo("") 
                .productName(call.getErcType() != null ? call.getErcType() : "ELASTIC RAIL CLIP")
                .dateOfInspection(call.getActualInspectionDate() != null ? call.getActualInspectionDate().format(DATE_FORMATTER) : "")
                .pages(pages)
                .build();
    }
    /**
     * Helper method to determine if a weight reading is rejected based on ERC Type.
     * MK-V: 1068g - 1108g
     * MK-III: 904g - 944g
     * ERC-J: 904g - 944g
     */
    private boolean isWeightRejected(String ercType, BigDecimal weight) {
        if (weight == null) return true;
        
        double val = weight.doubleValue();
        if (ercType != null && ercType.contains("MK-V")) {
            return val < 1068.0 || val > 1108.0;
        } else if (ercType != null && ercType.contains("MK-III")) {
            return val < 904.0 || val > 944.0;
        } else if (ercType != null && ercType.contains("ERC-J")) {
            return val < 904.0 || val > 944.0;
        }
        
        // Default tolerance check (from user's code base of 904/1068)
        double min = (ercType != null && ercType.contains("MK-V")) ? 1068.0 : 904.0;
        return val < min;
    }

    /**
     * Helper method to determine if a toe load reading is rejected based on ERC Type.
     * MK-III: 850 - 1100
     * MK-V: 1200 - 1500
     * ERC-J: > 650
     */
    private boolean isToeLoadRejected(String ercType, BigDecimal value) {
        if (value == null) return true;
        
        double val = value.doubleValue();
        if (ercType != null && ercType.contains("MK-III")) {
            return val < 850.0 || val > 1100.0;
        } else if (ercType != null && ercType.contains("MK-V")) {
            return val < 1200.0 || val > 1500.0;
        } else if (ercType != null && ercType.contains("ERC-J")) {
            return val < 650.0;
        }
        
        // Default check (MK-III)
        return val < 850.0 || val > 1100.0;
    }

    /**
     * Helper method to determine if a hardness reading is rejected.
     * Range: 40 - 44 HRC
     */
    private boolean isHardnessRejected(BigDecimal value) {
        if (value == null) return true;
        double val = value.doubleValue();
        return val < 40.0 || val > 44.0;
    }
}
