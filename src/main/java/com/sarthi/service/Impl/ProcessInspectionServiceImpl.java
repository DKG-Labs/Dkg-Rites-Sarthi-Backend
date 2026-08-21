package com.sarthi.service.Impl;

import com.sarthi.dto.processmaterial.*;
import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.InspectionImageRepository;
import com.sarthi.entity.InspectionImage;
import com.sarthi.service.*;
import java.util.UUID;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of ProcessInspectionService.
 * Orchestrates existing submodule services to save/retrieve Process Material inspection data.
 * Supports both finish and pause operations with proper audit trail (createdBy/updatedBy).
 */
@Service
public class ProcessInspectionServiceImpl implements ProcessInspectionService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInspectionServiceImpl.class);

    @Autowired
    private ProcessLineFinalResultRepository lineFinalResultRepository;

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProcessCalibrationDocumentsService calibrationService;

    @Autowired
    private ProcessStaticPeriodicCheckService staticCheckService;

    @Autowired
    private ProcessShearingDataService shearingService;

    @Autowired
    private ProcessTurningDataService turningService;

    @Autowired
    private ProcessMpiDataService mpiService;

    @Autowired
    private ProcessForgingDataService forgingService;

    @Autowired
    private ProcessQuenchingDataService quenchingService;

    @Autowired
    private ProcessTemperingDataService temperingService;

    @Autowired
    private ProcessFinalCheckDataService finalCheckService;

    @Autowired
    private ProcessTestingFinishingDataService testingFinishingService;

    @Autowired
    private ProcessOilTankCounterService oilTankService;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessShearingDataRepository shearingRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessTurningDataRepository turningRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessMpiDataRepository mpiRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessForgingDataRepository forgingRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessQuenchingDataRepository quenchingRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessTemperingDataRepository temperingRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessFinalCheckDataRepository finalCheckRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessTestingFinishingDataRepository testingFinishingRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessStaticPeriodicCheckRepository staticCheckRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessCalibrationDocumentsRepository calibrationRepository;

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessOilTankCounterRepository oilTankRepository;

    @Autowired
    private InspectionImageRepository inspectionImageRepository;

    @Value("${azure.storage.images-container-name}")
    private String imagesContainerName;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Override
    @Transactional
    public String finishInspection(ProcessFinishInspectionDto dto, String userId) {
        String callNo = dto.getInspectionCallNo();
        logger.info("Finishing Process Material inspection for call: {} by user: {}", callNo, userId);

        // Save all inspection data
        saveInspectionData(dto, userId);

        // Update Inspection Call Status to COMPLETED
        updateInspectionCallStatus(callNo, "COMPLETED");

        logger.info("Process Material inspection finished successfully for call: {}", callNo);
        return "Process Material Inspection completed successfully";
    }

    @Override
    @Transactional
    public String pauseInspection(ProcessFinishInspectionDto dto, String userId) {
        String callNo = dto.getInspectionCallNo();
        logger.info("Pausing Process Material inspection for call: {} by user: {}", callNo, userId);

        // Save all inspection data without changing status
        saveInspectionData(dto, userId);

        logger.info("Process Material inspection data saved (paused) for call: {}", callNo);
        return "Process Material Inspection data saved successfully";
    }

    @Override
    @Transactional
    public String revertPauseInspection(ProcessFinishInspectionDto dto, String userId) {
        String callNo = dto.getInspectionCallNo();
        logger.warn("Reverting / Rolling back Process Material inspection pause data for call: {} by user: {}", callNo, userId);

        if (dto.getLinesData() != null) {
            for (ProcessLineDataDto lineData : dto.getLinesData()) {
                String lineNo = lineData.getLineNo();
                String poNo = lineData.getPoNo();
                String lineCallNo = lineData.getInspectionCallNo() != null ? lineData.getInspectionCallNo() : callNo;
                String shift = dto.getShift() != null ? dto.getShift() : (lineData.getLineFinalResult() != null ? lineData.getLineFinalResult().getShift() : null);
                String lotNo = lineData.getLotNo() != null ? lineData.getLotNo() : (lineData.getLineFinalResult() != null ? lineData.getLineFinalResult().getLotNumber() : null);

                // 1. Delete matching ProcessLineFinalResult
                List<ProcessLineFinalResult> finalResults = lineFinalResultRepository.findByInspectionCallNo(lineCallNo);
                if (finalResults != null && !finalResults.isEmpty()) {
                    List<ProcessLineFinalResult> toDelete = finalResults.stream().filter(r ->
                        (lineNo == null || lineNo.equalsIgnoreCase(r.getLineNo())) &&
                        (shift == null || shift.equalsIgnoreCase(r.getShift())) &&
                        (lotNo == null || r.getLotNumber() == null || lotNo.trim().equalsIgnoreCase(r.getLotNumber().trim())) &&
                        (userId == null || userId.equals(r.getCreatedBy()))
                    ).collect(Collectors.toList());
                    if (!toDelete.isEmpty()) {
                        lineFinalResultRepository.deleteAll(toDelete);
                        logger.info("Deleted {} ProcessLineFinalResult rows for call: {}, line: {}, lot: {}", toDelete.size(), lineCallNo, lineNo, lotNo);
                    }
                }

                // 2. Delete Shearing
                shearingRepository.deleteAll(shearingRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 3. Delete Turning
                turningRepository.deleteAll(turningRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 4. Delete MPI
                mpiRepository.deleteAll(mpiRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 5. Delete Forging
                forgingRepository.deleteAll(forgingRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 6. Delete Quenching
                quenchingRepository.deleteAll(quenchingRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 7. Delete Tempering
                temperingRepository.deleteAll(temperingRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 8. Delete Final Check
                finalCheckRepository.deleteAll(finalCheckRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 9. Delete Testing & Finishing
                testingFinishingRepository.deleteAll(testingFinishingRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 10. Delete Static Periodic Checks
                staticCheckRepository.deleteAll(staticCheckRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (shift == null || shift.equalsIgnoreCase(e.getShift())) && (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 11. Delete Calibration Documents
                calibrationRepository.deleteAll(calibrationRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).stream()
                        .filter(e -> (userId == null || userId.equals(e.getCreatedBy())))
                        .collect(Collectors.toList()));

                // 12. Delete Oil Tank Counter
                oilTankRepository.findByInspectionCallNoAndPoNoAndLineNo(lineCallNo, poNo, lineNo).ifPresent(oilTank -> {
                    if (userId == null || userId.equals(oilTank.getCreatedBy())) {
                        oilTankRepository.delete(oilTank);
                    }
                });
            }
        }

        logger.info("Process Material inspection pause data reverted successfully for call: {}", callNo);
        return "Process Material Inspection data rollback successful";
    }

    /**
     * Core method to save all inspection data using existing submodule services.
     * Sets createdBy and updatedBy fields with userId for audit trail.
     */
    private void saveInspectionData(ProcessFinishInspectionDto dto, String userId) {
        String callNo = dto.getInspectionCallNo();

        // Save Captured Images
        if (dto.getCapturedImages() != null && !dto.getCapturedImages().isEmpty()) {
            saveCapturedImages(callNo, "PROCESS", dto.getCapturedImages(), dto.getShiftCode(), LocalDate.now().toString(), userId);
        }

        if (dto.getLinesData() == null || dto.getLinesData().isEmpty()) {
            logger.warn("No line data provided for call: {}", callNo);
            return;
        }

        for (ProcessLineDataDto lineData : dto.getLinesData()) {
            String lineNo = lineData.getLineNo();
            String poNo = lineData.getPoNo();
            String lineCallNo = lineData.getInspectionCallNo(); // Use line-specific call number

            logger.info("Processing line: {} for call: {}", lineNo, lineCallNo);

            // Set audit fields (createdBy/updatedBy) for all DTOs before saving
            // 1. Save Calibration Documents
            if (lineData.getCalibrationDocuments() != null && !lineData.getCalibrationDocuments().isEmpty()) {
                lineData.getCalibrationDocuments().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                calibrationService.saveAll(lineData.getCalibrationDocuments());
            }

            // 2. Save Static Periodic Checks (no saveAll, use individual save)
            if (lineData.getStaticPeriodicChecks() != null && !lineData.getStaticPeriodicChecks().isEmpty()) {
                for (ProcessStaticPeriodicCheckDTO d : lineData.getStaticPeriodicChecks()) {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                    staticCheckService.save(d);
                }
            }

            // 3. Save Shearing Data
            if (lineData.getShearingData() != null && !lineData.getShearingData().isEmpty()) {
                lineData.getShearingData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                shearingService.saveAll(lineData.getShearingData());
            }

            // 4. Save Turning Data
            if (lineData.getTurningData() != null && !lineData.getTurningData().isEmpty()) {
                lineData.getTurningData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                turningService.saveAll(lineData.getTurningData());
            }

            // 5. Save MPI Data
            if (lineData.getMpiData() != null && !lineData.getMpiData().isEmpty()) {
                lineData.getMpiData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                mpiService.saveAll(lineData.getMpiData());
            }

            // 6. Save Forging Data
            if (lineData.getForgingData() != null && !lineData.getForgingData().isEmpty()) {
                lineData.getForgingData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                forgingService.saveAll(lineData.getForgingData());
            }

            // 7. Save Quenching Data
            if (lineData.getQuenchingData() != null && !lineData.getQuenchingData().isEmpty()) {
                lineData.getQuenchingData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                quenchingService.saveAll(lineData.getQuenchingData());
            }

            // 8. Save Tempering Data
            if (lineData.getTemperingData() != null && !lineData.getTemperingData().isEmpty()) {
                lineData.getTemperingData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                temperingService.saveAll(lineData.getTemperingData());
            }

            // 9. Save Final Check Data
            if (lineData.getFinalCheckData() != null && !lineData.getFinalCheckData().isEmpty()) {
                lineData.getFinalCheckData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                finalCheckService.saveAll(lineData.getFinalCheckData());
            }

            // 10. Save Testing & Finishing Data
            if (lineData.getTestingFinishingData() != null && !lineData.getTestingFinishingData().isEmpty()) {
                lineData.getTestingFinishingData().forEach(d -> {
                    d.setInspectionCallNo(lineCallNo);
                    d.setPoNo(poNo);
                    d.setLineNo(lineNo);
                    if (d.getId() == null) d.setCreatedBy(userId);
                    d.setUpdatedBy(userId);
                });
                testingFinishingService.saveAll(lineData.getTestingFinishingData());
            }

            // 11. Save Oil Tank Counter (no saveAll, use individual save)
            if (lineData.getOilTankCounter() != null) {
                ProcessOilTankCounterDTO d = lineData.getOilTankCounter();
                d.setInspectionCallNo(lineCallNo);
                d.setPoNo(poNo);
                d.setLineNo(lineNo);
                if (d.getId() == null) d.setCreatedBy(userId);
                d.setUpdatedBy(userId);
                oilTankService.save(d);
            }

            // 11. Save Line Final Result (Summary with IE remarks, status, quantities)
            // Auto-calculate from submodule data if not provided
            ProcessLineFinalResultDto finalResultDto = lineData.getLineFinalResult();
            if (finalResultDto == null) {
                finalResultDto = calculateLineFinalResult(lineData);
            }

            if (finalResultDto != null) {
                finalResultDto.setInspectionCallNo(lineCallNo);
                finalResultDto.setPoNo(poNo);
                finalResultDto.setLineNo(lineNo);
                if (finalResultDto.getCreatedBy() == null) finalResultDto.setCreatedBy(userId);
                finalResultDto.setUpdatedBy(userId);

                // Convert DTO to Entity
                ProcessLineFinalResult entity = toFinalResultEntity(finalResultDto);

                lineFinalResultRepository.save(entity);
                logger.info("✅ Saved NEW Line Final Result row for line: {}", lineNo);
            }
        }
    }

    private void saveCapturedImages(String callNo, String typeOfCall, List<com.sarthi.dto.ImageCaptureDto> images, String shift, String dateOfInspection, String userId) {
        logger.info("Saving captured images for call: {} type: {}", callNo, typeOfCall);

        // Separate existing images (proxy URLs already on Azure) from new base64 images.
        // Proxy URLs look like: /api/images/ER_xxxxx.jpg
        // New images are base64 data URLs: data:image/jpeg;base64,...
        Set<String> existingImageNames = new HashSet<>();
        List<com.sarthi.dto.ImageCaptureDto> newImages = new ArrayList<>();

        for (com.sarthi.dto.ImageCaptureDto imageDto : images) {
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
        for (com.sarthi.dto.ImageCaptureDto imageDto : newImages) {
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
            imageEntity.setCreatedBy(userId);
            imageEntity.setUpdatedBy(userId);

            inspectionImageRepository.save(imageEntity);
        }

        logger.info("Images saved for call {}: {} new uploaded, {} existing retained",
                    callNo, newImages.size(), existingImageNames.size());
    }

    /**
     * Update inspection call status.
     */
    private void updateInspectionCallStatus(String callNo, String status) {
        Optional<InspectionCall> inspectionOpt = inspectionCallRepository.findByIcNumber(callNo);
        if (inspectionOpt.isPresent()) {
            InspectionCall inspection = inspectionOpt.get();
            inspection.setStatus(status);
            inspectionCallRepository.save(inspection);
            logger.info("Updated inspection call status to {} for call: {}", status, callNo);
        } else {
            logger.warn("Inspection call not found: {}", callNo);
        }
    }

    @Override
    public ProcessFinishInspectionDto getByCallNo(String callNo) {
        logger.info("Fetching Process Material inspection data for call: {}", callNo);

        ProcessFinishInspectionDto dto = new ProcessFinishInspectionDto();
        dto.setInspectionCallNo(callNo);

        // Get all lines for this inspection call
        // We need to determine which lines exist by querying one of the submodule tables
        List<ProcessCalibrationDocumentsDTO> allCalibrations = calibrationService.getByInspectionCallNo(callNo);

        // Extract unique line numbers
        List<String> lineNumbers = allCalibrations.stream()
                .map(ProcessCalibrationDocumentsDTO::getLineNo)
                .distinct()
                .collect(Collectors.toList());

        List<ProcessLineDataDto> linesData = new ArrayList<>();

        for (String lineNo : lineNumbers) {
            // Get PO number from calibration data
            String poNo = allCalibrations.stream()
                    .filter(c -> c.getLineNo().equals(lineNo))
                    .findFirst()
                    .map(ProcessCalibrationDocumentsDTO::getPoNo)
                    .orElse(null);

            ProcessLineDataDto lineData = new ProcessLineDataDto();
            lineData.setLineNo(lineNo);
            lineData.setPoNo(poNo);
            lineData.setInspectionCallNo(callNo);

            // Fetch all submodule data for this line
            lineData.setCalibrationDocuments(calibrationService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));
            lineData.setShearingData(shearingService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));
            lineData.setTurningData(turningService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));
            lineData.setMpiData(mpiService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));
            lineData.setForgingData(forgingService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));
            lineData.setQuenchingData(quenchingService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));
            lineData.setTemperingData(temperingService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));
            lineData.setFinalCheckData(finalCheckService.getByCallNoPoNoLineNo(callNo, poNo, lineNo));

            // Oil Tank Counter and Static Checks return Optional
            oilTankService.getByCallNoPoNoLineNo(callNo, poNo, lineNo).ifPresent(lineData::setOilTankCounter);
            staticCheckService.getByCallNoPoNoLineNo(callNo, poNo, lineNo).ifPresent(check -> {
                List<ProcessStaticPeriodicCheckDTO> checks = new ArrayList<>();
                checks.add(check);
                lineData.setStaticPeriodicChecks(checks);
            });

            // Fetch Line Final Result (Summary with IE remarks, status, quantities)
            lineFinalResultRepository.findFirstByInspectionCallNoAndLineNoOrderByCreatedAtDesc(callNo, lineNo)
                    .ifPresent(entity -> lineData.setLineFinalResult(toFinalResultDto(entity)));

            linesData.add(lineData);
        }

        dto.setLinesData(linesData);

        // Fetch Captured Images
        // Return proxy URL (/api/images/{imageName}) instead of raw Azure blob URL
        // because Azure Blob Storage has public access disabled (returns 409 to browser).
        List<InspectionImage> images = inspectionImageRepository.findByInspectionCallNoAndTypeOfCall(callNo, "PROCESS");
        List<com.sarthi.dto.ImageCaptureDto> imageDtos = new ArrayList<>();
        if (images != null) {
            for (InspectionImage img : images) {
                com.sarthi.dto.ImageCaptureDto imgDto = new com.sarthi.dto.ImageCaptureDto();
                // Use backend proxy URL so the browser fetches through our authenticated endpoint
                String proxyUrl = "/api/images/" + img.getImageName();
                imgDto.setBase64Data(proxyUrl);
                imgDto.setLatitude(img.getLatitude());
                imgDto.setLongitude(img.getLongitude());
                imageDtos.add(imgDto);
            }
        }
        dto.setCapturedImages(imageDtos);

        logger.info("Fetched data for {} lines for call: {}", linesData.size(), callNo);
        return dto;
    }

    @Override
    public List<ProcessLineFinalResultDto> getFinalResultsByCallNo(String callNo) {
        logger.info("Fetching final results for call: {}", callNo);

        List<ProcessLineFinalResult> entities = lineFinalResultRepository.findByInspectionCallNo(callNo);

        return entities.stream()
                .map(this::toFinalResultDto)
                .collect(Collectors.toList());
    }

    @Override
    public com.sarthi.dto.processmaterial.ProcessStageAcceptedQtyDto getAcceptedQuantitySum(String callNo, String lotNo) {
        return lineFinalResultRepository.getSumOfAcceptedQuantitiesByCallAndLot(callNo, lotNo);
    }

    private ProcessLineFinalResultDto toFinalResultDto(ProcessLineFinalResult entity) {
        ProcessLineFinalResultDto dto = new ProcessLineFinalResultDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private ProcessLineFinalResult toFinalResultEntity(ProcessLineFinalResultDto dto) {
        ProcessLineFinalResult entity = new ProcessLineFinalResult();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * Auto-calculate line final result from submodule data.
     * Aggregates quantities and statuses from all process stages.
     */
    private ProcessLineFinalResultDto calculateLineFinalResult(ProcessLineDataDto lineData) {
        ProcessLineFinalResultDto result = new ProcessLineFinalResultDto();

        // Initialize counters
        int totalAccepted = 0;
        int totalRejected = 0;

        // Aggregate from Shearing Data (only has rejected quantities)
        if (lineData.getShearingData() != null && !lineData.getShearingData().isEmpty()) {
            for (ProcessShearingDataDTO data : lineData.getShearingData()) {
                if (data.getLengthCutBarRejected() != null) totalRejected += data.getLengthCutBarRejected();
                if (data.getImproperDiaRejected() != null) totalRejected += data.getImproperDiaRejected();
                if (data.getSharpEdgesRejected() != null) totalRejected += data.getSharpEdgesRejected();
                if (data.getCrackedEdgesRejected() != null) totalRejected += data.getCrackedEdgesRejected();
            }
            result.setShearingStatus("COMPLETED");
        }

        // Aggregate from Turning Data
        if (lineData.getTurningData() != null && !lineData.getTurningData().isEmpty()) {
            for (ProcessTurningDataDTO data : lineData.getTurningData()) {
                if (data.getAcceptedQty() != null) totalAccepted += data.getAcceptedQty();
                if (data.getParallelLengthRejected() != null) totalRejected += data.getParallelLengthRejected();
                if (data.getFullTurningLengthRejected() != null) totalRejected += data.getFullTurningLengthRejected();
                if (data.getTurningDiaRejected() != null) totalRejected += data.getTurningDiaRejected();
            }
            result.setTurningStatus("COMPLETED");
        }

        // Aggregate from MPI Data
        if (lineData.getMpiData() != null && !lineData.getMpiData().isEmpty()) {
            for (ProcessMpiDataDTO data : lineData.getMpiData()) {
                if (data.getMpiRejected() != null) totalRejected += data.getMpiRejected();
            }
            result.setMpiStatus("COMPLETED");
        }

        // Aggregate from Forging Data
        if (lineData.getForgingData() != null && !lineData.getForgingData().isEmpty()) {
            for (ProcessForgingDataDTO data : lineData.getForgingData()) {
                // Sum individual rejection fields for Forging
                if (data.getForgingTempRejected() != null) totalRejected += data.getForgingTempRejected();
                if (data.getForgingStabilisationRejectionRejected() != null) totalRejected += data.getForgingStabilisationRejectionRejected();
                if (data.getImproperForgingRejected() != null) totalRejected += data.getImproperForgingRejected();
                if (data.getForgingDefectRejected() != null) totalRejected += data.getForgingDefectRejected();
                if (data.getEmbossingDefectRejected() != null) totalRejected += data.getEmbossingDefectRejected();
            }
            result.setForgingStatus("COMPLETED");
        }

        // Aggregate from Quenching Data
        if (lineData.getQuenchingData() != null && !lineData.getQuenchingData().isEmpty()) {
            for (ProcessQuenchingDataDTO data : lineData.getQuenchingData()) {
                // Aggregate all granular rejection fields for Quenching
                if (data.getQuenchingTemperatureRejected() != null) totalRejected += data.getQuenchingTemperatureRejected();
                if (data.getQuenchingDurationRejected() != null) totalRejected += data.getQuenchingDurationRejected();
                if (data.getQuenchingHardnessRejected() != null) totalRejected += data.getQuenchingHardnessRejected();
                if (data.getBoxGaugeRejected() != null) totalRejected += data.getBoxGaugeRejected();
                if (data.getFlatBearingAreaRejected() != null) totalRejected += data.getFlatBearingAreaRejected();
                if (data.getFallingGaugeRejected() != null) totalRejected += data.getFallingGaugeRejected();
            }
            result.setQuenchingStatus("COMPLETED");
        }

        // Aggregate from Tempering Data
        if (lineData.getTemperingData() != null && !lineData.getTemperingData().isEmpty()) {
            for (ProcessTemperingDataDTO data : lineData.getTemperingData()) {
                if (data.getAcceptedQty() != null) totalAccepted += data.getAcceptedQty();
                // Aggregate all granular rejection fields for Tempering
                if (data.getTemperingTemperatureRejected() != null) totalRejected += data.getTemperingTemperatureRejected();
                if (data.getTemperingDurationRejected() != null) totalRejected += data.getTemperingDurationRejected();
            }
            result.setTemperingStatus("COMPLETED");
        }

        // Aggregate from Final Check Data
        if (lineData.getFinalCheckData() != null && !lineData.getFinalCheckData().isEmpty()) {
            for (ProcessFinalCheckDataDTO data : lineData.getFinalCheckData()) {
                if (data.getBoxGaugeRejected() != null) totalRejected += data.getBoxGaugeRejected();
                if (data.getFlatBearingAreaRejected() != null) totalRejected += data.getFlatBearingAreaRejected();
                if (data.getFallingGaugeRejected() != null) totalRejected += data.getFallingGaugeRejected();
                if (data.getSurfaceDefectRejected() != null) totalRejected += data.getSurfaceDefectRejected();
                if (data.getEmbossingDefectRejected() != null) totalRejected += data.getEmbossingDefectRejected();
                if (data.getMarkingRejected() != null) totalRejected += data.getMarkingRejected();
                if (data.getTemperingHardnessRejected() != null) totalRejected += data.getTemperingHardnessRejected();
            }
            result.setFinalCheckStatus("COMPLETED");
        }

        // Aggregate from Testing & Finishing Data
        if (lineData.getTestingFinishingData() != null && !lineData.getTestingFinishingData().isEmpty()) {
            int tfAccepted = 0;
            int tfRejected = 0;
            
            for (ProcessTestingFinishingDataDTO data : lineData.getTestingFinishingData()) {
                if (data.getAcceptedQty() != null) {
                    int acc = data.getAcceptedQty();
                    tfAccepted += acc;
                    totalAccepted += acc;
                }
                
                // Aggregate all granular rejection fields for Testing & Finishing
                int currentRejected = 0;
                if (data.getToeLoadRejected() != null) currentRejected += data.getToeLoadRejected();
                if (data.getWeightRejected() != null) currentRejected += data.getWeightRejected();
                if (data.getPaintIdentificationRejected() != null) currentRejected += data.getPaintIdentificationRejected();
                if (data.getErcCoatingRejected() != null) currentRejected += data.getErcCoatingRejected();
                
                tfRejected += currentRejected;
                totalRejected += currentRejected;
            }
            
            result.setTestingFinishingAccepted(tfAccepted);
            result.setTestingFinishingRejected(tfRejected);
            result.setTestingFinishingManufactured(tfAccepted + tfRejected);
            result.setTestingFinishingStatus("COMPLETED");
        }

        // Set calibration and static check status
        if (lineData.getCalibrationDocuments() != null && !lineData.getCalibrationDocuments().isEmpty()) {
            result.setCalibrationStatus("COMPLETED");
        }
        if (lineData.getStaticPeriodicChecks() != null && !lineData.getStaticPeriodicChecks().isEmpty()) {
            result.setStaticCheckStatus("COMPLETED");
        }

        // Set totals
        result.setTotalAccepted(totalAccepted);
        result.setTotalRejected(totalRejected);

        // Determine overall status
        if (totalRejected == 0 && totalAccepted > 0) {
            result.setStatus("ACCEPTED");
            result.setOverallStatus("ACCEPTED");
        } else if (totalRejected > 0 && totalAccepted > 0) {
            result.setStatus("PARTIALLY_ACCEPTED");
            result.setOverallStatus("PARTIALLY_ACCEPTED");
        } else if (totalRejected > 0 && totalAccepted == 0) {
            result.setStatus("REJECTED");
            result.setOverallStatus("REJECTED");
        } else {
            result.setStatus("PENDING");
            result.setOverallStatus("PENDING");
        }

        result.setRemarks("Auto-calculated from submodule data");

        logger.info("Auto-calculated line final result: Accepted={}, Rejected={}, Status={}",
                    totalAccepted, totalRejected, result.getStatus());

        return result;
    }
}
