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
import com.sarthi.repository.InspectionImageRepository;
import com.sarthi.entity.InspectionImage;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.finalmaterial.FinalDashboardResultsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.ArrayList;
import com.sarthi.dto.ImageCaptureDto;

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

    @Autowired
    private InspectionImageRepository inspectionImageRepository;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Value("${azure.storage.images-container-name}")
    private String imagesContainerName;

    @Autowired
    private com.sarthi.repository.rawmaterial.InspectionCallRepository inspectionCallRepository;

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

        // Auto-compute cumulative values if 0 or null were sent
        if (entity.getCummQtyOfferedPreviously() == null || entity.getCummQtyOfferedPreviously() == 0 ||
            entity.getCummQtyPassedPreviously() == null || entity.getCummQtyPassedPreviously() == 0) {
            try {
                Optional<com.sarthi.entity.rawmaterial.InspectionCall> icOpt = inspectionCallRepository.findByIcNumber(dto.getInspectionCallNo());
                if (icOpt.isPresent()) {
                    com.sarthi.entity.rawmaterial.InspectionCall ic = icOpt.get();
                    String rawSerial = ic.getPoSerialNo();
                    String serialSuffix = (rawSerial != null && rawSerial.contains("/"))
                            ? rawSerial.substring(rawSerial.lastIndexOf("/") + 1).trim()
                            : (rawSerial != null ? rawSerial.trim() : "");

                    List<String> prevCallNos = inspectionCallRepository.findPreviousCallNumbersByPoAndSerial(
                            ic.getPoNo(),
                            serialSuffix,
                            ic.getId() != null ? ic.getId().longValue() : 0L,
                            ic.getCreatedAt()
                    );
                    if (prevCallNos != null && !prevCallNos.isEmpty()) {
                        List<Object[]> sumsList = cumulativeResultsRepository.sumCumulativeByCallNumbers(
                                prevCallNos,
                                ic.getIcNumber()
                        );
                        if (sumsList != null && !sumsList.isEmpty()) {
                            Object[] row = sumsList.get(0);
                            if (row != null && row.length >= 3) {
                                int passed = row[0] != null ? ((Number) row[0]).intValue() : 0;
                                int offered = row[2] != null ? ((Number) row[2]).intValue() : 0;
                                if (passed > 0) entity.setCummQtyPassedPreviously(passed);
                                if (offered > 0) entity.setCummQtyOfferedPreviously(offered);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to auto-compute cumulative values during save: {}", e.getMessage());
            }
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

        // Save Captured Images
        if (dto.getCapturedImages() != null && !dto.getCapturedImages().isEmpty()) {
            saveCapturedImages(dto.getInspectionCallNo(), "FINAL", dto.getCapturedImages(), LocalDate.now().toString(), userId);
        }

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

    private void saveCapturedImages(String callNo, String typeOfCall, List<com.sarthi.dto.ImageCaptureDto> images, String dateOfInspection, String userId) {
        log.info("Saving captured images for call: {} type: {}", callNo, typeOfCall);

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
        List<com.sarthi.entity.InspectionImage> currentDbImages = inspectionImageRepository.findByInspectionCallNoAndTypeOfCall(callNo, typeOfCall);
        for (com.sarthi.entity.InspectionImage dbImage : currentDbImages) {
            if (!existingImageNames.contains(dbImage.getImageName())) {
                inspectionImageRepository.delete(dbImage);
                log.info("Deleted removed image: {}", dbImage.getImageName());
            }
        }

        // Upload and save only truly new images
        for (com.sarthi.dto.ImageCaptureDto imageDto : newImages) {
            String fileName = callNo.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString() + ".jpg";

            String imageUrl = azureBlobStorageService.uploadBase64File(imageDto.getBase64Data(), fileName, imagesContainerName);

            com.sarthi.entity.InspectionImage imageEntity = new com.sarthi.entity.InspectionImage();
            imageEntity.setInspectionCallNo(callNo);
            imageEntity.setTypeOfCall(typeOfCall);
            imageEntity.setImageName(fileName);
            imageEntity.setImageUrl(imageUrl);
            imageEntity.setLatitude(imageDto.getLatitude());
            imageEntity.setLongitude(imageDto.getLongitude());
            imageEntity.setShift(null);
            imageEntity.setDateOfInspection(dateOfInspection);
            imageEntity.setCreatedBy(userId);
            imageEntity.setUpdatedBy(userId);

            inspectionImageRepository.save(imageEntity);
        }

        log.info("Images saved for call {}: {} new uploaded, {} existing retained",
                    callNo, newImages.size(), existingImageNames.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinalInspectionSummaryDto> getInspectionSummaryByCallNo(String inspectionCallNo) {
        return inspectionSummaryRepository.findByInspectionCallNo(inspectionCallNo)
            .map(entity -> {
                FinalInspectionSummaryDto dto = FinalInspectionSummaryDto.builder()
                    .inspectionCallNo(entity.getInspectionCallNo())
                    .packedInHdpe(entity.getPackedInHdpe())
                    .cleanedWithCoating(entity.getCleanedWithCoating())
                    .inspectionStatus(entity.getInspectionStatus())
                    .createdBy(entity.getCreatedBy())
                    .createdAt(entity.getCreatedAt())
                    .updatedBy(entity.getUpdatedBy())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
                
                // Fetch Captured Images
                // Return proxy URL (/api/images/{imageName}) instead of raw Azure blob URL
                // because Azure Blob Storage has public access disabled (returns 409 to browser).
                List<InspectionImage> images = inspectionImageRepository.findByInspectionCallNoAndTypeOfCall(inspectionCallNo, "FINAL");
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
                return dto;
            });
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
            entity.setTotalRejectedQty(dto.getTotalRejectedQty());
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
            entity.setTotalRejectedQty(dto.getTotalRejectedQty());
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
        entity.setTotalRejectedQty(dto.getTotalRejectedQty());
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

