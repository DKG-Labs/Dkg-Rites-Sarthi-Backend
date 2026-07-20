package com.sarthi.service.Impl;

import com.sarthi.dto.*;
import com.sarthi.entity.*;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmChemicalAnalysis;
import com.sarthi.repository.*;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmChemicalAnalysisRepository;
import com.sarthi.service.RmInspectionService;
import com.sarthi.service.AzureBlobStorageService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of RmInspectionService.
 * Handles saving and retrieving Raw Material inspection data.
 */
@Service
public class RmInspectionServiceImpl implements RmInspectionService {

    private static final Logger logger = LoggerFactory.getLogger(RmInspectionServiceImpl.class);

    // Use ThreadLocal to store current user ID for thread-safe access
    private static final ThreadLocal<String> currentUserIdThreadLocal = new ThreadLocal<>();

    @Autowired
    private RmInspectionSummaryRepository summaryRepository;

    @Autowired
    private RmHeatFinalResultRepository heatResultRepository;

    @Autowired
    private RmVisualInspectionRepository visualRepository;

    @Autowired
    private RmDimensionalCheckRepository dimensionalRepository;

    @Value("${azure.storage.images-container-name}")
    private String imagesContainerName;

    @Autowired
    private RmMaterialTestingRepository materialTestingRepository;

    @Autowired
    private RmPackingStorageRepository packingRepository;

    @Autowired
    private RmCalibrationDocumentsRepository calibrationRepository;

    @Autowired
    private RmChemicalAnalysisRepository chemicalAnalysisRepository;

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private InspectionImageRepository inspectionImageRepository;

    @Autowired
    private com.sarthi.repository.rawmaterial.RmHeatQuantityRepository heatQuantityRepository;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Override
    @Transactional
    public String finishInspection(RmFinishInspectionDto dto, String userId) {
        String callNo = dto.getInspectionCallNo();
        logger.info("Finishing RM inspection for call: {} by user: {}", callNo, userId);

        // Store userId in thread-local for use in save methods
        logger.debug("Setting currentUserId in ThreadLocal: {}", userId);
        currentUserIdThreadLocal.set(userId);

        try {
            // Validate all data before saving
            List<String> validationErrors = validateInspectionData(dto);
            if (!validationErrors.isEmpty()) {
                String errorMsg = String.join("; ", validationErrors);
                logger.error("Validation failed for call {}: {}", callNo, errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            // 1. Save Summary
            saveSummary(dto);

            // 2. Save Heat Final Results
            if (dto.getHeatFinalResults() != null) {
                saveHeatFinalResults(callNo, dto.getHeatFinalResults());
            }

            // 3. Save Visual Inspection Data
            if (dto.getVisualInspectionData() != null) {
                saveVisualInspection(callNo, dto.getVisualInspectionData());
            }

            // 4. Save Dimensional Check Data
            if (dto.getDimensionalCheckData() != null) {
                saveDimensionalCheck(callNo, dto.getDimensionalCheckData());
            }

            // 5. Save Material Testing Data
            if (dto.getMaterialTestingData() != null) {
                saveMaterialTesting(callNo, dto.getMaterialTestingData());
            }

            // 6. Save Packing & Storage Data
            if (dto.getPackingStorageData() != null) {
                savePackingStorage(callNo, dto.getPackingStorageData());
            }

            // 7. Save Calibration Documents Data
            if (dto.getCalibrationDocumentsData() != null) {
                saveCalibrationDocuments(callNo, dto.getCalibrationDocumentsData());
            }

            // 7.5. Save Captured Images
            if (dto.getCapturedImages() != null && !dto.getCapturedImages().isEmpty()) {
                saveCapturedImages(dto.getInspectionCallNo(), "RM", dto.getCapturedImages(), dto.getInspectorDetails() != null ? dto.getInspectorDetails().getShiftOfInspection() : null, dto.getInspectorDetails() != null ? dto.getInspectorDetails().getInspectionDate() : null);
            }

            // 8. Update Inspection Call Status to COMPLETED
            InspectionCall inspectionCall = inspectionCallRepository.findByIcNumber(callNo)
                    .orElseThrow(() -> new IllegalArgumentException("Inspection call not found: " + callNo));
            inspectionCall.setStatus("COMPLETED");
            inspectionCallRepository.save(inspectionCall);
            logger.info("Updated inspection call status to COMPLETED for call: {}", callNo);

            logger.info("RM inspection saved successfully for call: {}", callNo);
            return "Raw Material Inspection saved successfully";
        } finally {
            // Clean up ThreadLocal to prevent memory leaks
            logger.debug("Clearing currentUserId from ThreadLocal");
            currentUserIdThreadLocal.remove();
        }
    }

    @Override
    @Transactional
    public String pauseInspection(RmFinishInspectionDto dto, String userId) {
        String callNo = dto.getInspectionCallNo();
        logger.info("Pausing RM inspection for call: {} by user: {}", callNo, userId);

        // Store userId in thread-local for use in save methods
        logger.debug("Setting currentUserId in ThreadLocal: {}", userId);
        currentUserIdThreadLocal.set(userId);

        try {
            // Save all inspection data WITHOUT changing status
            // 1. Save Summary
            saveSummary(dto);

            // 2. Save Heat Final Results
            if (dto.getHeatFinalResults() != null) {
                saveHeatFinalResults(callNo, dto.getHeatFinalResults());
            }

            // 3. Save Visual Inspection Data
            if (dto.getVisualInspectionData() != null) {
                saveVisualInspection(callNo, dto.getVisualInspectionData());
            }

            // 4. Save Dimensional Check Data
            if (dto.getDimensionalCheckData() != null) {
                saveDimensionalCheck(callNo, dto.getDimensionalCheckData());
            }

            // 5. Save Material Testing Data
            if (dto.getMaterialTestingData() != null) {
                saveMaterialTesting(callNo, dto.getMaterialTestingData());
            }

            // 6. Save Packing & Storage Data
            if (dto.getPackingStorageData() != null) {
                savePackingStorage(callNo, dto.getPackingStorageData());
            }

            // 7. Save Calibration Documents Data
            if (dto.getCalibrationDocumentsData() != null) {
                saveCalibrationDocuments(callNo, dto.getCalibrationDocumentsData());
            }

            // 7.5. Save Captured Images
            if (dto.getCapturedImages() != null && !dto.getCapturedImages().isEmpty()) {
                saveCapturedImages(dto.getInspectionCallNo(), "RM", dto.getCapturedImages(), dto.getInspectorDetails() != null ? dto.getInspectorDetails().getShiftOfInspection() : null, dto.getInspectorDetails() != null ? dto.getInspectorDetails().getInspectionDate() : null);
            }

            // NOTE: Do NOT update inspection call status - it remains as is
            logger.info("RM inspection data saved (paused) for call: {}", callNo);
            return "Raw Material Inspection data saved successfully";
        } finally {
            // Clean up ThreadLocal to prevent memory leaks
            logger.debug("Clearing currentUserId from ThreadLocal");
            currentUserIdThreadLocal.remove();
        }
    }

    private void saveSummary(RmFinishInspectionDto dto) {
        RmPreInspectionDataDto pre = dto.getPreInspectionData();
        RmInspectorDetailsDto inspector = dto.getInspectorDetails();
        String callNo = dto.getInspectionCallNo();

        RmInspectionSummary summary = summaryRepository.findByInspectionCallNo(callNo)
                .orElse(new RmInspectionSummary());

        summary.setInspectionCallNo(callNo);

        if (pre != null) {
            summary.setTotalHeatsOffered(pre.getTotalHeatsOffered());
            summary.setTotalQtyOfferedMt(pre.getTotalQtyOfferedMt());
            summary.setNumberOfBundles(pre.getNumberOfBundles());
            summary.setNumberOfErc(pre.getNumberOfErc());
            summary.setProductModel(pre.getProductModel());
            summary.setPoNo(pre.getPoNo());
            summary.setPoDate(parseDate(pre.getPoDate()));
            summary.setVendorName(pre.getVendorName());
            summary.setPlaceOfInspection(pre.getPlaceOfInspection());
            summary.setSourceOfRawMaterial(pre.getSourceOfRawMaterial());
        }

        if (inspector != null) {
            summary.setFinishedBy(inspector.getFinishedBy());
            summary.setFinishedAt(parseDateTime(inspector.getFinishedAt()));
            summary.setInspectionDate(parseDate(inspector.getInspectionDate()));
            summary.setShiftOfInspection(inspector.getShiftOfInspection());
        }

        summaryRepository.save(summary);
    }

    private void saveCapturedImages(String callNo, String typeOfCall, List<ImageCaptureDto> images, String shift, String dateOfInspection) {
        logger.info("Saving captured images for call: {} type: {}", callNo, typeOfCall);

        // Separate existing images (proxy URLs already on Azure) from new base64 images.
        // Proxy URLs look like: /api/images/ER_xxxxx.jpg
        // New images are base64 data URLs: data:image/jpeg;base64,...
        Set<String> existingImageNames = new HashSet<>();
        List<ImageCaptureDto> newImages = new ArrayList<>();

        for (ImageCaptureDto imageDto : images) {
            if (imageDto.getBase64Data() != null && !imageDto.getBase64Data().isEmpty()) {
                if (imageDto.getBase64Data().startsWith("/api/images/")) {
                    // Already saved to Azure - just retain in DB
                    String existingName = imageDto.getBase64Data().substring("/api/images/".length());
                    existingImageNames.add(existingName);
                } else {
                    // New base64 image - needs to be uploaded
                    newImages.add(imageDto);
                }
            }
        }

        // Delete only images that are no longer in the payload (removed by user)
        List<InspectionImage> currentDbImages = inspectionImageRepository.findByInspectionCallNoAndTypeOfCall(callNo, typeOfCall);
        for (InspectionImage dbImage : currentDbImages) {
            if (!existingImageNames.contains(dbImage.getImageName())) {
                inspectionImageRepository.delete(dbImage);
                logger.info("Deleted removed image: {}", dbImage.getImageName());
            }
        }

        // Upload and save only truly new images
        for (ImageCaptureDto imageDto : newImages) {
            String fileName = callNo.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString() + ".jpg";

            String imageUrl = azureBlobStorageService.uploadBase64File(imageDto.getBase64Data(), fileName, imagesContainerName);

            InspectionImage imageEntity = new InspectionImage();
            imageEntity.setInspectionCallNo(callNo);
            imageEntity.setTypeOfCall(typeOfCall);
            imageEntity.setImageName(fileName);
            imageEntity.setImageUrl(imageUrl);
            imageEntity.setLatitude(imageDto.getLatitude());
            imageEntity.setLongitude(imageDto.getLongitude());
            imageEntity.setShift(shift);
            imageEntity.setDateOfInspection(dateOfInspection);
            imageEntity.setCreatedBy(getCurrentUser());
            imageEntity.setUpdatedBy(getCurrentUser());

            inspectionImageRepository.save(imageEntity);
        }

        logger.info("Images saved for call {}: {} new uploaded, {} existing retained",
                    callNo, newImages.size(), existingImageNames.size());
    }

    private void saveHeatFinalResults(String callNo, List<RmHeatFinalResultDto> results) {
        for (RmHeatFinalResultDto dto : results) {
            // Try to find existing record
            List<RmHeatFinalResult> existing = heatResultRepository.findByInspectionCallNoAndHeatNo(callNo,
                    dto.getHeatNo());

            RmHeatFinalResult entity;
            if (existing != null && !existing.isEmpty()) {
                // Update existing record
                entity = existing.get(0);
                logger.info("Updating existing heat final result for call: {} heat: {}", callNo, dto.getHeatNo());
            } else {
                // Create new record
                entity = new RmHeatFinalResult();
                entity.setInspectionCallNo(callNo);
                entity.setHeatNo(dto.getHeatNo());
                logger.info("Creating new heat final result for call: {} heat: {}", callNo, dto.getHeatNo());
            }

            // Set all fields
            entity.setWeightOfferedMt(dto.getWeightOfferedMt());
            entity.setWeightAcceptedMt(dto.getWeightAcceptedMt());
            entity.setWeightRejectedMt(dto.getWeightRejectedMt());
            entity.setAcceptedQtyMt(dto.getAcceptedQtyMt());
            entity.setCalibrationStatus(dto.getCalibrationStatus());
            entity.setVisualStatus(dto.getVisualStatus());
            entity.setDimensionalStatus(dto.getDimensionalStatus());
            entity.setMaterialTestStatus(dto.getMaterialTestStatus());
            entity.setPackingStatus(dto.getPackingStatus());
            entity.setStatus(dto.getStatus());
            entity.setOverallStatus(dto.getOverallStatus());
            entity.setTotalHeatsOffered(dto.getTotalHeatsOffered());
            entity.setTotalQtyOfferedMt(dto.getTotalQtyOfferedMt());
            entity.setNoOfBundles(dto.getNoOfBundles());
            entity.setNoOfErcFinished(dto.getNoOfErcFinished());
            entity.setRemarks(dto.getRemarks());
            entity.setShiftOfInspection(dto.getShift());
            entity.setSealingType(dto.getSealingType());
            entity.setSteelStampNumber(dto.getSteelStampNumber());
            entity.setHologramDetails(dto.getHologramDetails());
            entity.setCreatedBy(dto.getCreatedBy());
            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setDateOfInspection(dto.getDateOfInspection() != null ? dto.getDateOfInspection().atStartOfDay() : null);

            heatResultRepository.save(entity);
        }
    }

    private void saveVisualInspection(String callNo, List<RmVisualInspectionDto> data) {
        for (RmVisualInspectionDto dto : data) {
            // Find existing record by call number and heat number (one record per heat)
            RmVisualInspection entity = visualRepository.findByInspectionCallNoAndHeatNo(callNo, dto.getHeatNo())
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (entity == null) {
                // Create new record
                entity = new RmVisualInspection();
                entity.setInspectionCallNo(callNo);
                entity.setHeatNo(dto.getHeatNo());
                entity.setHeatIndex(dto.getHeatIndex());
                logger.info("Creating new visual inspection record for call: {} heat: {}", callNo, dto.getHeatNo());
            } else {
                logger.info("Updating existing visual inspection record for call: {} heat: {}", callNo,
                        dto.getHeatNo());
            }

            final RmVisualInspection finalEntity = entity;

            // Set defect selections from map
            if (dto.getDefects() != null) {
                dto.getDefects().forEach((defectName, isSelected) -> {
                    setDefectSelection(finalEntity, defectName, isSelected);
                });
            }

            // Set defect lengths from map
            if (dto.getDefectLengths() != null) {
                dto.getDefectLengths().forEach((defectName, length) -> {
                    setDefectLength(finalEntity, defectName, length);
                });
            }

            // Set explicitly calculated values
            entity.setDefectCount(dto.getDefectCount());
            entity.setWeightRejected(dto.getWeightRejected());

            // Set audit fields
            entity.setCreatedBy(getCurrentUser());
            entity.setUpdatedBy(getCurrentUser());
            entity.setUpdatedAt(LocalDateTime.now());

            visualRepository.save(entity);
        }
    }

    private void setDefectSelection(RmVisualInspection entity, String defectName, Boolean isSelected) {
        boolean selected = isSelected != null && isSelected;
        switch (defectName.toLowerCase()) {
            case "no defect":
                entity.setNoDefect(selected);
                break;
            case "distortion":
                entity.setDistortion(selected);
                break;
            case "twist":
                entity.setTwist(selected);
                break;
            case "kink":
                entity.setKink(selected);
                break;
            case "not straight":
                entity.setNotStraight(selected);
                break;
            case "fold":
                entity.setFold(selected);
                break;
            case "lap":
                entity.setLap(selected);
                break;
            case "crack":
                entity.setCrack(selected);
                break;
            case "pit":
                entity.setPit(selected);
                break;
            case "groove":
                entity.setGroove(selected);
                break;
            case "excessive scaling":
                entity.setExcessiveScaling(selected);
                break;
            case "internal defect (piping, segregation)":
            case "internal defect":
                entity.setInternalDefect(selected);
                break;
        }
    }

    private void setDefectLength(RmVisualInspection entity, String defectName, BigDecimal length) {
        switch (defectName.toLowerCase()) {
            case "distortion":
                entity.setDistortionLength(length);
                break;
            case "twist":
                entity.setTwistLength(length);
                break;
            case "kink":
                entity.setKinkLength(length);
                break;
            case "not straight":
                entity.setNotStraightLength(length);
                break;
            case "fold":
                entity.setFoldLength(length);
                break;
            case "lap":
                entity.setLapLength(length);
                break;
            case "crack":
                entity.setCrackLength(length);
                break;
            case "pit":
                entity.setPitLength(length);
                break;
            case "groove":
                entity.setGrooveLength(length);
                break;
            case "excessive scaling":
                entity.setExcessiveScalingLength(length);
                break;
            case "internal defect (piping, segregation)":
            case "internal defect":
                entity.setInternalDefectLength(length);
                break;
        }
    }

    private void saveDimensionalCheck(String callNo, List<RmDimensionalCheckDto> data) {
        for (RmDimensionalCheckDto dto : data) {
            // Find existing record by call number and heat number (one record per heat)
            RmDimensionalCheck entity = dimensionalRepository.findByInspectionCallNoAndHeatNo(callNo, dto.getHeatNo())
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (entity == null) {
                // Create new record
                entity = new RmDimensionalCheck();
                entity.setInspectionCallNo(callNo);
                entity.setHeatNo(dto.getHeatNo());
                entity.setHeatIndex(dto.getHeatIndex());
                logger.info("Creating new dimensional check record for call: {} heat: {}", callNo, dto.getHeatNo());
            } else {
                logger.info("Updating existing dimensional check record for call: {} heat: {}", callNo,
                        dto.getHeatNo());
            }

            // Set all 20 sample diameters from list
            if (dto.getSampleDiameters() != null) {
                List<BigDecimal> samples = dto.getSampleDiameters();
                if (samples.size() > 0)
                    entity.setSample1Diameter(samples.get(0));
                if (samples.size() > 1)
                    entity.setSample2Diameter(samples.get(1));
                if (samples.size() > 2)
                    entity.setSample3Diameter(samples.get(2));
                if (samples.size() > 3)
                    entity.setSample4Diameter(samples.get(3));
                if (samples.size() > 4)
                    entity.setSample5Diameter(samples.get(4));
                if (samples.size() > 5)
                    entity.setSample6Diameter(samples.get(5));
                if (samples.size() > 6)
                    entity.setSample7Diameter(samples.get(6));
                if (samples.size() > 7)
                    entity.setSample8Diameter(samples.get(7));
                if (samples.size() > 8)
                    entity.setSample9Diameter(samples.get(8));
                if (samples.size() > 9)
                    entity.setSample10Diameter(samples.get(9));
                if (samples.size() > 10)
                    entity.setSample11Diameter(samples.get(10));
                if (samples.size() > 11)
                    entity.setSample12Diameter(samples.get(11));
                if (samples.size() > 12)
                    entity.setSample13Diameter(samples.get(12));
                if (samples.size() > 13)
                    entity.setSample14Diameter(samples.get(13));
                if (samples.size() > 14)
                    entity.setSample15Diameter(samples.get(14));
                if (samples.size() > 15)
                    entity.setSample16Diameter(samples.get(15));
                if (samples.size() > 16)
                    entity.setSample17Diameter(samples.get(16));
                if (samples.size() > 17)
                    entity.setSample18Diameter(samples.get(17));
                if (samples.size() > 18)
                    entity.setSample19Diameter(samples.get(18));
                if (samples.size() > 19)
                    entity.setSample20Diameter(samples.get(19));
            }

            // Set explicitly calculated values
            entity.setDefectCount(dto.getDefectCount());

            // Set audit fields
            entity.setCreatedBy(getCurrentUser());
            entity.setUpdatedBy(getCurrentUser());
            entity.setUpdatedAt(LocalDateTime.now());

            dimensionalRepository.save(entity);
        }
    }

    private void saveMaterialTesting(String callNo, List<RmMaterialTestingDto> data) {
        for (RmMaterialTestingDto dto : data) {
            // Try to find existing record by call number, heat number, and sample number
            List<RmMaterialTesting> existing = materialTestingRepository.findByInspectionCallNoAndHeatNo(callNo,
                    dto.getHeatNo());

            RmMaterialTesting entity;
            if (existing != null && !existing.isEmpty()) {
                // Update existing record - find the one matching sample number
                var matchingRecord = existing.stream()
                        .filter(e -> e.getSampleNumber() != null && e.getSampleNumber().equals(dto.getSampleNumber()))
                        .findFirst();

                if (matchingRecord.isPresent()) {
                    entity = matchingRecord.get();
                    logger.info("Updating existing material testing for call: {} heat: {} sample: {}", callNo,
                            dto.getHeatNo(), dto.getSampleNumber());
                } else {
                    // No matching sample found, create new record
                    entity = new RmMaterialTesting();
                    entity.setInspectionCallNo(callNo);
                    entity.setHeatNo(dto.getHeatNo());
                    logger.info("Creating new material testing for call: {} heat: {} sample: {}", callNo,
                            dto.getHeatNo(), dto.getSampleNumber());
                }
            } else {
                // Create new record
                entity = new RmMaterialTesting();
                entity.setInspectionCallNo(callNo);
                entity.setHeatNo(dto.getHeatNo());
                logger.info("Creating new material testing for call: {} heat: {} sample: {}", callNo, dto.getHeatNo(),
                        dto.getSampleNumber());
            }

            // Set all fields
            entity.setHeatIndex(dto.getHeatIndex());
            entity.setSampleNumber(dto.getSampleNumber());
            entity.setCarbonPercent(dto.getCarbonPercent());
            entity.setSiliconPercent(dto.getSiliconPercent());
            entity.setManganesePercent(dto.getManganesePercent());
            entity.setPhosphorusPercent(dto.getPhosphorusPercent());
            entity.setSulphurPercent(dto.getSulphurPercent());
            entity.setGrainSize(dto.getGrainSize());
            // Map frontend field names to entity field names
            entity.setHardness(dto.getHardnessHrc());
            entity.setDecarb(dto.getDecarbDepthMm());

            // Map inclusion values
            entity.setInclusionA(dto.getInclusionA());
            entity.setInclusionB(dto.getInclusionB());
            entity.setInclusionC(dto.getInclusionC());
            entity.setInclusionD(dto.getInclusionD());

            // Map inclusion types (Thick/Thin)
            entity.setInclusionTypeA(dto.getInclusionTypeA());
            entity.setInclusionTypeB(dto.getInclusionTypeB());
            entity.setInclusionTypeC(dto.getInclusionTypeC());
            entity.setInclusionTypeD(dto.getInclusionTypeD());

            entity.setRemarks(dto.getRemarks());

            // Set audit fields
            entity.setCreatedBy(getCurrentUser());
            entity.setUpdatedBy(getCurrentUser());
            entity.setUpdatedAt(LocalDateTime.now());

            materialTestingRepository.save(entity);
        }
    }

    private void savePackingStorage(String callNo, List<RmPackingStorageDto> dtoList) {
        for (RmPackingStorageDto dto : dtoList) {
            // Try to find existing record by call number and heat number
            List<RmPackingStorage> existing = packingRepository.findByInspectionCallNoAndHeatNo(callNo,
                    dto.getHeatNo());

            RmPackingStorage entity;
            if (existing != null && !existing.isEmpty()) {
                // Update existing record
                entity = existing.get(0);
                logger.info("Updating existing packing storage for call: {} heat: {}", callNo, dto.getHeatNo());
            } else {
                // Create new record
                entity = new RmPackingStorage();
                entity.setInspectionCallNo(callNo);
                entity.setHeatNo(dto.getHeatNo());
                logger.info("Creating new packing storage for call: {} heat: {}", callNo, dto.getHeatNo());
            }

            // Set all fields
            entity.setHeatIndex(dto.getHeatIndex());
            entity.setStoredHeatWise(dto.getStoredHeatWise());
            entity.setSuppliedInBundles(dto.getSuppliedInBundles());
            entity.setHeatNumberEnds(dto.getHeatNumberEnds());
            entity.setPackingStripWidth(dto.getPackingStripWidth());
            entity.setBundleTiedLocations(dto.getBundleTiedLocations());
            entity.setIdentificationTagBundle(dto.getIdentificationTagBundle());
            entity.setMetalTagInformation(dto.getMetalTagInformation());
            entity.setRemarks(dto.getRemarks());
            entity.setShift(dto.getShift());

            // Set audit fields
            entity.setCreatedBy(getCurrentUser());
            entity.setUpdatedBy(getCurrentUser());
            entity.setUpdatedAt(LocalDateTime.now());

            packingRepository.save(entity);
        }
    }

    private void saveCalibrationDocuments(String callNo, List<RmCalibrationDocumentsDto> data) {
        for (RmCalibrationDocumentsDto dto : data) {
            // Try to find existing record by call number and heat number
            List<RmCalibrationDocuments> existing = calibrationRepository.findByInspectionCallNoAndHeatNo(callNo,
                    dto.getHeatNo());

            RmCalibrationDocuments entity;
            if (existing != null && !existing.isEmpty()) {
                // Update existing record
                entity = existing.get(0);
                logger.info("Updating existing calibration documents for call: {} heat: {}", callNo, dto.getHeatNo());
            } else {
                // Create new record
                entity = new RmCalibrationDocuments();
                entity.setInspectionCallNo(callNo);
                entity.setHeatNo(dto.getHeatNo());
                logger.info("Creating new calibration documents for call: {} heat: {}", callNo, dto.getHeatNo());
            }

            // Set all fields
            entity.setHeatIndex(dto.getHeatIndex());
            entity.setRdsoApprovalId(dto.getRdsoApprovalId());
            entity.setRdsoValidFrom(parseDate(dto.getRdsoValidFrom()));
            entity.setRdsoValidTo(parseDate(dto.getRdsoValidTo()));
            entity.setGaugesAvailable(dto.getGaugesAvailable());
            entity.setLadleCarbonPercent(dto.getLadleCarbonPercent());
            entity.setLadleSiliconPercent(dto.getLadleSiliconPercent());
            entity.setLadleManganesePercent(dto.getLadleManganesePercent());
            entity.setLadlePhosphorusPercent(dto.getLadlePhosphorusPercent());
            entity.setLadleSulphurPercent(dto.getLadleSulphurPercent());
            entity.setVendorVerified(dto.getVendorVerified());
            entity.setVerifiedBy(dto.getVerifiedBy());
            entity.setVerifiedAt(parseDateTime(dto.getVerifiedAt()));

            calibrationRepository.save(entity);
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return null;
        try {
            return LocalDate.parse(dateStr.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty())
            return null;
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public RmFinishInspectionDto getByCallNo(String callNo) {
        logger.info("Fetching RM inspection data for call: {}", callNo);

        // Check if inspection data exists
        if (!summaryRepository.existsByInspectionCallNo(callNo)) {
            logger.warn("No inspection data found for call: {}", callNo);
            return null;
        }

        RmFinishInspectionDto dto = new RmFinishInspectionDto();
        dto.setInspectionCallNo(callNo);

        // 1. Fetch Summary (Pre-Inspection Data + Inspector Details)
        summaryRepository.findByInspectionCallNo(callNo).ifPresent(summary -> {
            // Pre-Inspection Data
            RmPreInspectionDataDto preData = new RmPreInspectionDataDto();
            preData.setInspectionCallNo(callNo);
            preData.setTotalHeatsOffered(summary.getTotalHeatsOffered());
            preData.setTotalQtyOfferedMt(summary.getTotalQtyOfferedMt());
            preData.setNumberOfBundles(summary.getNumberOfBundles());
            preData.setNumberOfErc(summary.getNumberOfErc());
            preData.setProductModel(summary.getProductModel());
            preData.setPoNo(summary.getPoNo());
            preData.setPoDate(formatDate(summary.getPoDate()));
            preData.setVendorName(summary.getVendorName());
            preData.setPlaceOfInspection(summary.getPlaceOfInspection());
            preData.setSourceOfRawMaterial(summary.getSourceOfRawMaterial());
            dto.setPreInspectionData(preData);

            // Inspector Details
            RmInspectorDetailsDto inspectorData = new RmInspectorDetailsDto();
            inspectorData.setFinishedBy(summary.getFinishedBy());
            inspectorData.setFinishedAt(formatDateTime(summary.getFinishedAt()));
            inspectorData.setInspectionDate(formatDate(summary.getInspectionDate()));
            inspectorData.setShiftOfInspection(summary.getShiftOfInspection());
            dto.setInspectorDetails(inspectorData);
        });

        // 2. Fetch Heat Final Results
        List<RmHeatFinalResult> heatResults = heatResultRepository.findByInspectionCallNo(callNo);
        List<RmHeatFinalResultDto> heatResultDtos = new ArrayList<>();
        for (RmHeatFinalResult entity : heatResults) {
            RmHeatFinalResultDto heatDto = new RmHeatFinalResultDto();
            heatDto.setInspectionCallNo(entity.getInspectionCallNo());
            heatDto.setHeatNo(entity.getHeatNo());
            heatDto.setWeightOfferedMt(entity.getWeightOfferedMt());
            heatDto.setWeightAcceptedMt(entity.getWeightAcceptedMt());
            heatDto.setWeightRejectedMt(entity.getWeightRejectedMt());
            heatDto.setAcceptedQtyMt(entity.getAcceptedQtyMt());
            heatDto.setCalibrationStatus(entity.getCalibrationStatus());
            heatDto.setVisualStatus(entity.getVisualStatus());
            heatDto.setDimensionalStatus(entity.getDimensionalStatus());
            heatDto.setMaterialTestStatus(entity.getMaterialTestStatus());
            heatDto.setPackingStatus(entity.getPackingStatus());
            heatDto.setStatus(entity.getStatus());
            heatDto.setOverallStatus(entity.getOverallStatus());
            heatDto.setTotalHeatsOffered(entity.getTotalHeatsOffered());
            heatDto.setTotalQtyOfferedMt(entity.getTotalQtyOfferedMt());
            heatDto.setNoOfBundles(entity.getNoOfBundles());
            heatDto.setNoOfErcFinished(entity.getNoOfErcFinished());
            heatDto.setRemarks(entity.getRemarks());
            heatDto.setSealingType(entity.getSealingType());
            heatDto.setSteelStampNumber(entity.getSteelStampNumber());
            heatDto.setHologramDetails(entity.getHologramDetails());
            heatResultDtos.add(heatDto);
        }
        dto.setHeatFinalResults(heatResultDtos);

        // 3. Fetch Visual Inspection Data
        List<RmVisualInspection> visualEntities = visualRepository.findByInspectionCallNo(callNo);
        List<RmVisualInspectionDto> visualDtos = visualEntities.stream()
                .map(this::mapVisualInspectionEntityToDto)
                .collect(Collectors.toList());
        dto.setVisualInspectionData(visualDtos);

        // 4. Fetch Dimensional Check Data
        List<RmDimensionalCheck> dimensionalEntities = dimensionalRepository.findByInspectionCallNo(callNo);
        List<RmDimensionalCheckDto> dimensionalDtos = new ArrayList<>();
        for (RmDimensionalCheck entity : dimensionalEntities) {
            RmDimensionalCheckDto dimDto = new RmDimensionalCheckDto();
            dimDto.setInspectionCallNo(entity.getInspectionCallNo());
            dimDto.setHeatNo(entity.getHeatNo());
            dimDto.setHeatIndex(entity.getHeatIndex());

            // Convert entity sample diameters to list
            List<BigDecimal> sampleDiameters = new ArrayList<>();
            sampleDiameters.add(entity.getSample1Diameter());
            sampleDiameters.add(entity.getSample2Diameter());
            sampleDiameters.add(entity.getSample3Diameter());
            sampleDiameters.add(entity.getSample4Diameter());
            sampleDiameters.add(entity.getSample5Diameter());
            sampleDiameters.add(entity.getSample6Diameter());
            sampleDiameters.add(entity.getSample7Diameter());
            sampleDiameters.add(entity.getSample8Diameter());
            sampleDiameters.add(entity.getSample9Diameter());
            sampleDiameters.add(entity.getSample10Diameter());
            sampleDiameters.add(entity.getSample11Diameter());
            sampleDiameters.add(entity.getSample12Diameter());
            sampleDiameters.add(entity.getSample13Diameter());
            sampleDiameters.add(entity.getSample14Diameter());
            sampleDiameters.add(entity.getSample15Diameter());
            sampleDiameters.add(entity.getSample16Diameter());
            sampleDiameters.add(entity.getSample17Diameter());
            sampleDiameters.add(entity.getSample18Diameter());
            sampleDiameters.add(entity.getSample19Diameter());
            sampleDiameters.add(entity.getSample20Diameter());
            dimDto.setSampleDiameters(sampleDiameters);

            dimDto.setDefectCount(entity.getDefectCount());

            dimensionalDtos.add(dimDto);
        }
        dto.setDimensionalCheckData(dimensionalDtos);

        // 5. Fetch Material Testing Data
        List<RmMaterialTesting> materialEntities = materialTestingRepository.findByInspectionCallNo(callNo);
        List<RmMaterialTestingDto> materialDtos = new ArrayList<>();
        for (RmMaterialTesting entity : materialEntities) {
            RmMaterialTestingDto matDto = new RmMaterialTestingDto();
            matDto.setInspectionCallNo(entity.getInspectionCallNo());
            matDto.setHeatNo(entity.getHeatNo());
            matDto.setHeatIndex(entity.getHeatIndex());
            matDto.setSampleNumber(entity.getSampleNumber());
            matDto.setCarbonPercent(entity.getCarbonPercent());
            matDto.setSiliconPercent(entity.getSiliconPercent());
            matDto.setManganesePercent(entity.getManganesePercent());
            matDto.setPhosphorusPercent(entity.getPhosphorusPercent());
            matDto.setSulphurPercent(entity.getSulphurPercent());
            matDto.setGrainSize(entity.getGrainSize());
            // Map entity field names to DTO field names
            matDto.setHardnessHrc(entity.getHardness());
            matDto.setDecarbDepthMm(entity.getDecarb());
            // Map inclusion values
            matDto.setInclusionA(entity.getInclusionA());
            matDto.setInclusionB(entity.getInclusionB());
            matDto.setInclusionC(entity.getInclusionC());
            matDto.setInclusionD(entity.getInclusionD());
            // Map inclusion types (Thick/Thin)
            matDto.setInclusionTypeA(entity.getInclusionTypeA());
            matDto.setInclusionTypeB(entity.getInclusionTypeB());
            matDto.setInclusionTypeC(entity.getInclusionTypeC());
            matDto.setInclusionTypeD(entity.getInclusionTypeD());
            matDto.setRemarks(entity.getRemarks());
            materialDtos.add(matDto);
        }
        dto.setMaterialTestingData(materialDtos);

        // 6. Fetch Packing & Storage Data - per heat
        List<RmPackingStorage> packingEntities = packingRepository.findByInspectionCallNo(callNo);
        List<RmPackingStorageDto> packingDtos = new ArrayList<>();
        for (RmPackingStorage entity : packingEntities) {
            RmPackingStorageDto packingDto = new RmPackingStorageDto();
            packingDto.setInspectionCallNo(entity.getInspectionCallNo());
            packingDto.setHeatNo(entity.getHeatNo());
            packingDto.setHeatIndex(entity.getHeatIndex());
            packingDto.setStoredHeatWise(entity.getStoredHeatWise());
            packingDto.setSuppliedInBundles(entity.getSuppliedInBundles());
            packingDto.setHeatNumberEnds(entity.getHeatNumberEnds());
            packingDto.setPackingStripWidth(entity.getPackingStripWidth());
            packingDto.setBundleTiedLocations(entity.getBundleTiedLocations());
            packingDto.setIdentificationTagBundle(entity.getIdentificationTagBundle());
            packingDto.setMetalTagInformation(entity.getMetalTagInformation());
            packingDto.setRemarks(entity.getRemarks());
            packingDto.setShift(entity.getShift());
            packingDtos.add(packingDto);
        }
        dto.setPackingStorageData(packingDtos);

        // 7. Fetch Calibration Documents Data
        List<RmCalibrationDocuments> calibrationEntities = calibrationRepository.findByInspectionCallNo(callNo);
        List<RmCalibrationDocumentsDto> calibrationDtos = new ArrayList<>();
        for (RmCalibrationDocuments entity : calibrationEntities) {
            RmCalibrationDocumentsDto calDto = new RmCalibrationDocumentsDto();
            calDto.setInspectionCallNo(entity.getInspectionCallNo());
            calDto.setHeatNo(entity.getHeatNo());
            calDto.setHeatIndex(entity.getHeatIndex());
            calDto.setRdsoApprovalId(entity.getRdsoApprovalId());
            calDto.setRdsoValidFrom(formatDate(entity.getRdsoValidFrom()));
            calDto.setRdsoValidTo(formatDate(entity.getRdsoValidTo()));
            calDto.setGaugesAvailable(entity.getGaugesAvailable());
            calDto.setLadleCarbonPercent(entity.getLadleCarbonPercent());
            calDto.setLadleSiliconPercent(entity.getLadleSiliconPercent());
            calDto.setLadleManganesePercent(entity.getLadleManganesePercent());
            calDto.setLadlePhosphorusPercent(entity.getLadlePhosphorusPercent());
            calDto.setLadleSulphurPercent(entity.getLadleSulphurPercent());
            calDto.setVendorVerified(entity.getVendorVerified());
            calDto.setVerifiedBy(entity.getVerifiedBy());
            calDto.setVerifiedAt(formatDateTime(entity.getVerifiedAt()));
            calibrationDtos.add(calDto);
        }
        dto.setCalibrationDocumentsData(calibrationDtos);

        // 8. Fetch Captured Images
        // Return proxy URL (/api/images/{imageName}) instead of raw Azure blob URL
        // because Azure Blob Storage has public access disabled (returns 409 to browser).
        List<InspectionImage> images = inspectionImageRepository.findByInspectionCallNoAndTypeOfCall(callNo, "RM");
        List<ImageCaptureDto> imageDtos = new ArrayList<>();
        if (images != null) {
            for (InspectionImage img : images) {
                ImageCaptureDto imgDto = new ImageCaptureDto();
                // Use backend proxy URL so the browser fetches through our authenticated endpoint
                String proxyUrl = "/api/images/" + img.getImageName();
                imgDto.setBase64Data(proxyUrl);
                imgDto.setLatitude(img.getLatitude());
                imgDto.setLongitude(img.getLongitude());
                imageDtos.add(imgDto);
            }
        }
        dto.setCapturedImages(imageDtos);

        logger.info("Successfully fetched RM inspection data for call: {}", callNo);
        return dto;
    }

    /**
     * Convert RmVisualInspection entity to RmVisualInspectionDto
     * Maps boolean columns to defects map and length columns to defectLengths map
     */
    private RmVisualInspectionDto mapVisualInspectionEntityToDto(RmVisualInspection entity) {
        RmVisualInspectionDto visualDto = new RmVisualInspectionDto();
        visualDto.setInspectionCallNo(entity.getInspectionCallNo());
        visualDto.setHeatNo(entity.getHeatNo());
        visualDto.setHeatIndex(entity.getHeatIndex());

        // Convert entity defect booleans to map
        Map<String, Boolean> defects = new HashMap<>();
        defects.put("No Defect", entity.getNoDefect());
        defects.put("Distortion", entity.getDistortion());
        defects.put("Twist", entity.getTwist());
        defects.put("Kink", entity.getKink());
        defects.put("Not Straight", entity.getNotStraight());
        defects.put("Fold", entity.getFold());
        defects.put("Lap", entity.getLap());
        defects.put("Crack", entity.getCrack());
        defects.put("Pit", entity.getPit());
        defects.put("Groove", entity.getGroove());
        defects.put("Excessive Scaling", entity.getExcessiveScaling());
        defects.put("Internal Defect (Piping, Segregation)", entity.getInternalDefect());
        visualDto.setDefects(defects);

        // Convert entity defect lengths to map
        Map<String, BigDecimal> defectLengths = new HashMap<>();
        if (entity.getDistortionLength() != null)
            defectLengths.put("Distortion", entity.getDistortionLength());
        if (entity.getTwistLength() != null)
            defectLengths.put("Twist", entity.getTwistLength());
        if (entity.getKinkLength() != null)
            defectLengths.put("Kink", entity.getKinkLength());
        if (entity.getNotStraightLength() != null)
            defectLengths.put("Not Straight", entity.getNotStraightLength());
        if (entity.getFoldLength() != null)
            defectLengths.put("Fold", entity.getFoldLength());
        if (entity.getLapLength() != null)
            defectLengths.put("Lap", entity.getLapLength());
        if (entity.getCrackLength() != null)
            defectLengths.put("Crack", entity.getCrackLength());
        if (entity.getPitLength() != null)
            defectLengths.put("Pit", entity.getPitLength());
        if (entity.getGrooveLength() != null)
            defectLengths.put("Groove", entity.getGrooveLength());
        if (entity.getExcessiveScalingLength() != null)
            defectLengths.put("Excessive Scaling", entity.getExcessiveScalingLength());
        if (entity.getInternalDefectLength() != null)
            defectLengths.put("Internal Defect (Piping, Segregation)", entity.getInternalDefectLength());
        visualDto.setDefectLengths(defectLengths);

        // Explicitly calculated values
        visualDto.setDefectCount(entity.getDefectCount());
        visualDto.setWeightRejected(entity.getWeightRejected());

        return visualDto;
    }

    /**
     * Format LocalDate to String (dd/MM/yyyy)
     */
    private String formatDate(LocalDate date) {
        if (date == null)
            return null;
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Format LocalDateTime to ISO String
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;
        return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public RmPreInspectionDataDto getSummaryByCallNo(String callNo) {
        logger.info("Fetching cumulative summary data for call: {}", callNo);

        return summaryRepository.findByInspectionCallNo(callNo)
                .map(summary -> {
                    RmPreInspectionDataDto dto = new RmPreInspectionDataDto();
                    dto.setInspectionCallNo(callNo);
                    dto.setTotalHeatsOffered(summary.getTotalHeatsOffered());
                    dto.setTotalQtyOfferedMt(summary.getTotalQtyOfferedMt());
                    dto.setNumberOfBundles(summary.getNumberOfBundles());
                    dto.setNumberOfErc(summary.getNumberOfErc());
                    dto.setProductModel(summary.getProductModel());
                    dto.setPoNo(summary.getPoNo());
                    dto.setPoDate(formatDate(summary.getPoDate()));
                    dto.setVendorName(summary.getVendorName());
                    dto.setPlaceOfInspection(summary.getPlaceOfInspection());
                    dto.setSourceOfRawMaterial(summary.getSourceOfRawMaterial());
                    logger.info("Successfully fetched cumulative summary for call: {}", callNo);
                    return dto;
                })
                .orElse(null);
    }

    @Override
    public List<RmHeatFinalResultDto> getFinalResultsByCallNo(String callNo) {
        logger.info("Fetching final inspection results for call: {}", callNo);

        List<RmHeatFinalResult> entities = heatResultRepository.findByInspectionCallNo(callNo);
        List<RmHeatFinalResultDto> dtos = new ArrayList<>();

        for (RmHeatFinalResult entity : entities) {
            RmHeatFinalResultDto dto = new RmHeatFinalResultDto();
            dto.setInspectionCallNo(entity.getInspectionCallNo());
            dto.setHeatNo(entity.getHeatNo());
            dto.setWeightOfferedMt(entity.getWeightOfferedMt());
            dto.setWeightAcceptedMt(entity.getWeightAcceptedMt());
            dto.setWeightRejectedMt(entity.getWeightRejectedMt());
            dto.setAcceptedQtyMt(entity.getAcceptedQtyMt());
            dto.setCalibrationStatus(entity.getCalibrationStatus());
            dto.setVisualStatus(entity.getVisualStatus());
            dto.setDimensionalStatus(entity.getDimensionalStatus());
            dto.setMaterialTestStatus(entity.getMaterialTestStatus());
            dto.setPackingStatus(entity.getPackingStatus());
            dto.setStatus(entity.getStatus());
            dto.setOverallStatus(entity.getOverallStatus());
            dto.setTotalHeatsOffered(entity.getTotalHeatsOffered());
            dto.setTotalQtyOfferedMt(entity.getTotalQtyOfferedMt());
            dto.setNoOfBundles(entity.getNoOfBundles());
            dto.setNoOfErcFinished(entity.getNoOfErcFinished());
            dto.setRemarks(entity.getRemarks());
            dtos.add(dto);
        }

        logger.info("Successfully fetched {} heat final results for call: {}", dtos.size(), callNo);
        return dtos;
    }

    @Override
    public List<RmLadleValuesDto> getLadleValuesByCallNo(String callNo) {
        logger.info("Fetching ladle values (chemical analysis) for call: {}", callNo);

        List<RmChemicalAnalysis> entities = chemicalAnalysisRepository.findByInspectionCallNo(callNo);
        List<RmLadleValuesDto> dtos = new ArrayList<>();

        for (RmChemicalAnalysis entity : entities) {
            RmLadleValuesDto dto = RmLadleValuesDto.builder()
                    .heatNo(entity.getHeatNumber())
                    .percentC(entity.getCarbon())
                    .percentSi(entity.getSilicon())
                    .percentMn(entity.getManganese())
                    .percentP(entity.getPhosphorus())
                    .percentS(entity.getSulphur())
                    .percentCr(entity.getChromium())
                    .build();
            dtos.add(dto);
        }

        logger.info("Successfully fetched {} ladle values for call: {}", dtos.size(), callNo);
        return dtos;
    }

    /**
     * Validates inspection data and returns list of user-friendly error messages.
     */
    private List<String> validateInspectionData(RmFinishInspectionDto dto) {
        List<String> errors = new ArrayList<>();

        // Validate Material Testing Data
        if (dto.getMaterialTestingData() != null) {
            for (RmMaterialTestingDto mt : dto.getMaterialTestingData()) {
                String heatNo = mt.getHeatNo();
                int sample = mt.getSampleNumber() != null ? mt.getSampleNumber() : 0;
                String prefix = "Heat " + heatNo + " Sample " + sample + ": ";

                // Chemical composition validations (max value 99.9999 for DECIMAL(6,4))
                if (mt.getCarbonPercent() != null && mt.getCarbonPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(
                            prefix + "Carbon % value (" + mt.getCarbonPercent() + ") is too large. Max allowed: 99%");
                }
                if (mt.getSiliconPercent() != null && mt.getSiliconPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(
                            prefix + "Silicon % value (" + mt.getSiliconPercent() + ") is too large. Max allowed: 99%");
                }
                if (mt.getManganesePercent() != null && mt.getManganesePercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Manganese % value (" + mt.getManganesePercent()
                            + ") is too large. Max allowed: 99%");
                }
                if (mt.getPhosphorusPercent() != null
                        && mt.getPhosphorusPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Phosphorus % value (" + mt.getPhosphorusPercent()
                            + ") is too large. Max allowed: 99%");
                }
                if (mt.getSulphurPercent() != null && mt.getSulphurPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(
                            prefix + "Sulphur % value (" + mt.getSulphurPercent() + ") is too large. Max allowed: 99%");
                }

                // Inclusion ratings validation (max value 99.99 for DECIMAL(4,2))
                if (mt.getInclusionA() != null && mt.getInclusionA().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Inclusion A value (" + mt.getInclusionA() + ") is too large. Max allowed: 99");
                }
                if (mt.getInclusionB() != null && mt.getInclusionB().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Inclusion B value (" + mt.getInclusionB() + ") is too large. Max allowed: 99");
                }
                if (mt.getInclusionC() != null && mt.getInclusionC().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Inclusion C value (" + mt.getInclusionC() + ") is too large. Max allowed: 99");
                }
                if (mt.getInclusionD() != null && mt.getInclusionD().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Inclusion D value (" + mt.getInclusionD() + ") is too large. Max allowed: 99");
                }
            }
        }

        // Validate Calibration Documents Data
        if (dto.getCalibrationDocumentsData() != null) {
            for (RmCalibrationDocumentsDto cal : dto.getCalibrationDocumentsData()) {
                String heatNo = cal.getHeatNo();
                String prefix = "Heat " + heatNo + " Calibration: ";

                if (cal.getLadleCarbonPercent() != null
                        && cal.getLadleCarbonPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Ladle Carbon % value is too large. Max allowed: 99%");
                }
                if (cal.getLadleSiliconPercent() != null
                        && cal.getLadleSiliconPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Ladle Silicon % value is too large. Max allowed: 99%");
                }
                if (cal.getLadleManganesePercent() != null
                        && cal.getLadleManganesePercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Ladle Manganese % value is too large. Max allowed: 99%");
                }
                if (cal.getLadlePhosphorusPercent() != null
                        && cal.getLadlePhosphorusPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Ladle Phosphorus % value is too large. Max allowed: 99%");
                }
                if (cal.getLadleSulphurPercent() != null
                        && cal.getLadleSulphurPercent().compareTo(new BigDecimal("99")) > 0) {
                    errors.add(prefix + "Ladle Sulphur % value is too large. Max allowed: 99%");
                }
            }
        }

        return errors;
    }

    /**
     * Get the current user ID from ThreadLocal
     * 
     * @return User ID from ThreadLocal or "system" as fallback
     */
    private String getCurrentUser() {
        String userId = currentUserIdThreadLocal.get();
        logger.debug("Getting currentUserId from ThreadLocal: {}", userId);
        if (userId != null && !userId.isEmpty() && !"anonymousUser".equals(userId)) {
            return userId;
        }
        logger.warn("No user ID found in ThreadLocal, using 'system' as fallback");
        return "system";
    }

    @Override
    public List<com.sarthi.dto.HeatDetailsDto> getHeatDetailsByPoSrNo(String poSrNo) {
        List<com.sarthi.entity.rawmaterial.InspectionCall> calls = inspectionCallRepository.findByPoSerialNo(poSrNo);
        List<com.sarthi.dto.HeatDetailsDto> allDetails = new java.util.ArrayList<>();
        if (calls != null) {
            for (com.sarthi.entity.rawmaterial.InspectionCall call : calls) {
                if (call.getIcNumber() != null) {
                    List<com.sarthi.dto.HeatDetailsDto> callDetails = getHeatDetailsByCallNo(call.getIcNumber());
                    for(com.sarthi.dto.HeatDetailsDto dto : callDetails) {
                        dto.setCallNo(call.getIcNumber());
                    }
                    allDetails.addAll(callDetails);
                }
            }
        }
        return allDetails;
    }

    @Override
    public List<com.sarthi.dto.HeatDetailsDto> getHeatDetailsByCallNo(String callNo) {
        List<com.sarthi.entity.rawmaterial.RmHeatQuantity> heatQuantities = heatQuantityRepository.findByInspectionCallNo(callNo);
        List<com.sarthi.entity.RmHeatFinalResult> finalResults = heatResultRepository.findByInspectionCallNo(callNo);

        return heatQuantities.stream().map(hq -> {
            com.sarthi.dto.HeatDetailsDto dto = new com.sarthi.dto.HeatDetailsDto();
            dto.setHeatNo(hq.getHeatNumber());
            dto.setTcNo(hq.getTcNumber());
            dto.setOfferedQty(hq.getOfferedQty());

            com.sarthi.entity.RmHeatFinalResult result = finalResults.stream()
                .filter(fr -> fr.getHeatNo() != null && fr.getHeatNo().equals(hq.getHeatNumber()))
                .findFirst().orElse(null);
            
            if (result != null) {
                dto.setAcceptedQty(result.getAcceptedQtyMt());
                dto.setRejectedQty(java.math.BigDecimal.ZERO); // We don't have rejected qty in result, only weight?
                dto.setWeightAcceptedMt(result.getWeightAcceptedMt());
                dto.setWeightRejectedMt(result.getWeightRejectedMt());
            } else {
                dto.setAcceptedQty(java.math.BigDecimal.ZERO);
                dto.setRejectedQty(java.math.BigDecimal.ZERO);
                dto.setWeightAcceptedMt(java.math.BigDecimal.ZERO);
                dto.setWeightRejectedMt(java.math.BigDecimal.ZERO);
            }
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
}
