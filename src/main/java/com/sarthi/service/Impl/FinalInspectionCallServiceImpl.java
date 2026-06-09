package com.sarthi.service.Impl;

import com.sarthi.dto.IcDtos.FinalInspectionDetailsRequestDto;
import com.sarthi.dto.IcDtos.FinalInspectionLotDetailsRequestDto;
import com.sarthi.dto.IcDtos.InspectionCallRequestDto;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import com.sarthi.entity.finalmaterial.FinalProcessIcMapping;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.repository.InspectionCompleteDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalProcessIcMappingRepository;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.FinalInspectionCallService;
import com.sarthi.service.InspectionCallService;
import com.sarthi.util.IcNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Final Inspection Call operations
 */
@Service
public class FinalInspectionCallServiceImpl implements FinalInspectionCallService {

    private static final Logger logger = LoggerFactory.getLogger(FinalInspectionCallServiceImpl.class);

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private FinalInspectionDetailsRepository finalInspectionDetailsRepository;

    @Autowired
    private FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;

    @Autowired
    private FinalProcessIcMappingRepository finalProcessIcMappingRepository;

    @Autowired
    private InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;

    @Autowired
    private ProcessInspectionDetailsRepository processInspectionDetailsRepository;

    @Autowired
    private IcNumberGenerator icNumberGenerator;

    @Autowired
    private InspectionCallService inspectionCallService;

    @Autowired
    private com.sarthi.service.WorkflowService workflowService;

    @Override
    @Transactional
    public InspectionCall createFinalInspectionCall(
            InspectionCallRequestDto icRequest,
            FinalInspectionDetailsRequestDto finalDetails,
            List<FinalInspectionLotDetailsRequestDto> lotDetailsList) {
        logger.info("========== CREATE FINAL INSPECTION CALL ==========");
        logger.info("IC Request: {}", icRequest);
        logger.info("Final Details: {}", finalDetails);
        logger.info("Lot Details Count: {}", lotDetailsList != null ? lotDetailsList.size() : 0);

        // ================== 1. CREATE INSPECTION CALL ==================
        InspectionCall inspectionCall = new InspectionCall();

        // Generate IC Number with daily sequence reset
        LocalDate today = LocalDate.now();
        long dailySequence = inspectionCallRepository.countByTypeOfCallAndCreatedDate("Final", today) + 1;
        String icNumber = icNumberGenerator.generateIcNumber("Final", dailySequence);
        logger.info("Generated IC Number: {} (Daily Sequence: {})", icNumber, dailySequence);

        inspectionCall.setIcNumber(icNumber);
        inspectionCall.setPoNo(icRequest.getPoNo());
        inspectionCall.setPoSerialNo(icRequest.getPoSerialNo());
        inspectionCall.setTypeOfCall(icRequest.getTypeOfCall());
        inspectionCall.setErcType(icRequest.getErcType());
        inspectionCall.setStatus(icRequest.getStatus());

        inspectionCall.setPlaceOfInspection(icRequest.getPlaceOfInspection());

        inspectionCall.setVendorId(icRequest.getVendorId());

        inspectionCall.setDesiredInspectionDate(
                LocalDate.parse(icRequest.getDesiredInspectionDate()));

        if (icRequest.getActualInspectionDate() != null) {
            inspectionCall.setActualInspectionDate(
                    LocalDate.parse(icRequest.getActualInspectionDate()));
        }

        inspectionCall.setCompanyId(icRequest.getCompanyId());
        inspectionCall.setCompanyName(icRequest.getCompanyName());
        inspectionCall.setUnitId(icRequest.getUnitId());
        inspectionCall.setUnitName(icRequest.getUnitName());
        inspectionCall.setUnitAddress(icRequest.getUnitAddress());
        inspectionCall.setRemarks(icRequest.getRemarks());

        inspectionCall.setCreatedBy(icRequest.getCreatedBy());
        inspectionCall.setUpdatedBy(icRequest.getUpdatedBy());
        inspectionCall.setCreatedAt(LocalDateTime.now());
        inspectionCall.setUpdatedAt(LocalDateTime.now());

        // Save inspection call first to get the ID
        inspectionCall = inspectionCallRepository.save(inspectionCall);
        logger.info(" Inspection Call saved with ID: {}", inspectionCall.getId());

        // ================== 2. CREATE FINAL INSPECTION DETAILS ==================
        FinalInspectionDetails finalInspectionDetails = new FinalInspectionDetails();
        finalInspectionDetails.setInspectionCall(inspectionCall);

        // ---- Resolve RM IC numbers (support both single and multi-select) ----
        // If rmIcNumbers list is provided (new multi-select flow), use it; else fall
        // back to single rmIcNumber
        List<String> rmIcList = (finalDetails.getRmIcNumbers() != null && !finalDetails.getRmIcNumbers().isEmpty())
                ? finalDetails.getRmIcNumbers()
                : (finalDetails.getRmIcNumber() != null ? List.of(finalDetails.getRmIcNumber()) : List.of());

        String rmIcNumbersCsv = rmIcList.stream().collect(Collectors.joining(","));
        finalInspectionDetails.setRmIcNumber(rmIcNumbersCsv);

        // Set primary RM IC ID using the first IC in the list
        Optional<InspectionCall> rmIcOpt = rmIcList.isEmpty()
                ? Optional.empty()
                : inspectionCallRepository.findByIcNumber(rmIcList.get(0));
        if (rmIcOpt.isPresent()) {
            finalInspectionDetails.setRmIcId(rmIcOpt.get().getId().longValue());
        }

        // ---- Resolve Process IC numbers (support both single and multi-select) ----
        List<String> processIcList = (finalDetails.getProcessIcNumbers() != null
                && !finalDetails.getProcessIcNumbers().isEmpty())
                ? finalDetails.getProcessIcNumbers()
                : (finalDetails.getProcessIcNumber() != null ? List.of(finalDetails.getProcessIcNumber())
                : List.of());

        String processIcNumbersCsv = processIcList.stream().collect(Collectors.joining(","));
        finalInspectionDetails.setProcessIcNumber(processIcNumbersCsv);

        // Set primary Process IC ID using the first IC in the list
        Optional<InspectionCall> processIcOpt = processIcList.isEmpty()
                ? Optional.empty()
                : inspectionCallRepository.findByIcNumber(processIcList.get(0));
        if (processIcOpt.isPresent()) {
            finalInspectionDetails.setProcessIcId(processIcOpt.get().getId().longValue());
        }

        finalInspectionDetails.setCompanyId(finalDetails.getCompanyId());
        finalInspectionDetails.setCompanyName(finalDetails.getCompanyName());
        finalInspectionDetails.setUnitId(finalDetails.getUnitId());
        finalInspectionDetails.setUnitName(finalDetails.getUnitName());
        finalInspectionDetails.setUnitAddress(finalDetails.getUnitAddress());

        finalInspectionDetails.setTotalLots(finalDetails.getTotalLots());
        finalInspectionDetails.setTotalOfferedQty(finalDetails.getTotalOfferedQty());
        finalInspectionDetails.setTotalAcceptedQty(null); // Will be set after inspection
        finalInspectionDetails.setTotalRejectedQty(null); // Will be set after inspection

        finalInspectionDetails = finalInspectionDetailsRepository.save(finalInspectionDetails);
        logger.info(" Final Inspection Details saved with ID: {}", finalInspectionDetails.getId());

        // ================== 3. CREATE FINAL INSPECTION LOT DETAILS ==================
        if (lotDetailsList != null && !lotDetailsList.isEmpty()) {
            for (FinalInspectionLotDetailsRequestDto lotDto : lotDetailsList) {
                FinalInspectionLotDetails lotDetails = new FinalInspectionLotDetails();

                lotDetails.setFinalDetailId(finalInspectionDetails.getId());
                lotDetails.setLotNumber(lotDto.getLotNumber());
                lotDetails.setHeatNumber(lotDto.getHeatNumber());
                lotDetails.setManufacturer(lotDto.getManufacturer());
                lotDetails.setManufacturerHeat(lotDto.getManufacturerHeat());
                lotDetails.setOfferedQty(lotDto.getOfferedQty());

                // Set No. of Bags - Use value from DTO if provided, otherwise calculate
                if (lotDto.getNoOfBags() != null && lotDto.getNoOfBags() > 0) {
                    lotDetails.setNoOfBags(lotDto.getNoOfBags());
                } else if (lotDto.getOfferedQty() != null) {
                    int bags = (int) Math.ceil((double) lotDto.getOfferedQty() / 50);
                    lotDetails.setNoOfBags(bags);
                }

                lotDetails.setQtyAccepted(null); // Will be set after inspection
                lotDetails.setQtyRejected(null); // Will be set after inspection
                lotDetails.setRejectionReason(null);

                // Set Process IC reference if available
                if (lotDto.getProcessIcNumber() != null) {
                    Optional<InspectionCall> processIcForLot = inspectionCallRepository
                            .findByIcNumber(lotDto.getProcessIcNumber());
                    if (processIcForLot.isPresent()) {
                        lotDetails.setProcessIcId(processIcForLot.get().getId().longValue());
                    }
                    lotDetails.setProcessIcNumber(lotDto.getProcessIcNumber());
                }

                finalInspectionLotDetailsRepository.save(lotDetails);
                logger.info("✅ Final Lot Details saved for Lot: {}", lotDto.getLotNumber());
            }
        }

        // ================== 4. CREATE FINAL PROCESS IC MAPPING ==================
        // Create mapping entries for each lot × each Process IC selected
        if (!processIcList.isEmpty() && lotDetailsList != null && !lotDetailsList.isEmpty()) {
            for (String processIcNumber : processIcList) {
                Optional<InspectionCall> processIcForMapping = inspectionCallRepository.findByIcNumber(processIcNumber);
                if (!processIcForMapping.isPresent()) {
                    logger.warn("⚠️ Process IC not found for number: {}. Skipping mapping.", processIcNumber);
                    continue;
                }
                InspectionCall processIc = processIcForMapping.get();

                for (FinalInspectionLotDetailsRequestDto lotDto : lotDetailsList) {
                    FinalProcessIcMapping mapping = new FinalProcessIcMapping();

                    mapping.setFinalIcId(inspectionCall.getId().longValue());
                    mapping.setProcessIcId(processIc.getId().longValue());
                    mapping.setProcessIcNumber(processIcNumber);
                    mapping.setLotNumber(lotDto.getLotNumber());
                    mapping.setHeatNumber(lotDto.getHeatNumber());
                    mapping.setManufacturer(lotDto.getManufacturer());
                    mapping.setProcessQtyAccepted(lotDto.getOfferedQty());
                    mapping.setProcessIcDate(processIc.getDesiredInspectionDate());

                    finalProcessIcMappingRepository.save(mapping);
                    logger.info("✅ Final-Process IC Mapping saved: ProcessIC={} Lot={}", processIcNumber,
                            lotDto.getLotNumber());
                }
            }
        }

        logger.info("========== FINAL INSPECTION CALL CREATED SUCCESSFULLY ==========");
        logger.info("IC Number: {}", icNumber);
        logger.info("Total Lots: {}", lotDetailsList != null ? lotDetailsList.size() : 0);

        // Trigger workflow ONLY on success of save, but inside the same transaction
        // so if workflow fails, the save is rolled back.
        String workflowName = "INSPECTION CALL";
        Integer createdByUserId = null;
        try {
            createdByUserId = Integer.valueOf(inspectionCall.getCreatedBy());
        } catch (NumberFormatException e) {
            logger.warn("⚠️ createdBy is not a valid integer: {}. Skipping workflow initiation.", inspectionCall.getCreatedBy());
        }

        if (createdByUserId != null) {
            workflowService.initiateWorkflow(
                    inspectionCall.getIcNumber(),
                    createdByUserId,
                    workflowName,
                    "560001"
            );
            logger.info("✅ Workflow initiated for IC: {}", inspectionCall.getIcNumber());
        }

        return inspectionCall;
    }

    @Override
    public List<String> getProcessIcCertificateNumbers(String vendorId) {
        logger.info("Fetching Process IC certificate numbers for vendor: {}", vendorId);
        List<String> certificateNumbers = inspectionCompleteDetailsRepository
                .findProcessIcCertificateNumbersByVendor(vendorId);
        logger.info("Found {} certificate numbers", certificateNumbers.size());
        return certificateNumbers;
    }

    @Override
    public List<String> getRmIcNumbersByCertificateNo(String certificateNo) {
        logger.info("Fetching RM IC numbers for certificate: {}", certificateNo);
        List<String> rmIcNumbers = processInspectionDetailsRepository.findRmIcNumbersByCertificateNo(certificateNo);
        logger.info("Found {} RM IC numbers", rmIcNumbers.size());
        return rmIcNumbers;
    }

    @Override
    public List<String> getLotNumbersByRmIcNumber(String rmIcNumber) {
        logger.info("Fetching lot numbers for RM IC: {}", rmIcNumber);
        List<String> lotNumbers = processInspectionDetailsRepository.findLotNumbersByRmIcNumber(rmIcNumber);
        logger.info("Found {} lot numbers", lotNumbers.size());
        return lotNumbers;
    }

    // ==================== NEW METHODS FOR REVERSED DROPDOWN FLOW
    // ====================

    @Override
    public List<String> getRmIcCertificateNumbers(String poSerialNo) {
        logger.info("Fetching RM IC certificate numbers for PO Serial No: {}", poSerialNo);
        List<String> certificateNumbers = inspectionCompleteDetailsRepository
                .findCompletedRmIcCertificateNumbersByPoSerialNo(poSerialNo);
        logger.info("Found {} RM IC certificate numbers", certificateNumbers.size());
        return certificateNumbers;
    }

    @Override
    public List<String> getProcessIcCertificateNumbersByRmCertificate(String rmCertificateNo) {
        logger.info("Fetching Process IC certificate numbers for RM certificate: {}", rmCertificateNo);
        List<String> certificateNumbers = inspectionCompleteDetailsRepository
                .findProcessIcNumbersByRmIcNumber(rmCertificateNo);
        logger.info("Found {} Process IC certificate numbers", certificateNumbers.size());
        return certificateNumbers;
    }

    @Override
    public List<String> getLotNumbersByRmAndProcessCertificates(String rmCertificateNo, String processCertificateNo) {
        logger.info("Fetching lot numbers for RM certificate: {} and Process certificate: {}", rmCertificateNo,
                processCertificateNo);
        List<String> lotNumbers = processInspectionDetailsRepository
                .findLotNumbersByRmAndProcessIcNumbers(rmCertificateNo, processCertificateNo);
        logger.info("Found {} lot numbers", lotNumbers.size());
        return lotNumbers;
    }

    @Override
    public List<String> getHeatNumbersByLotNumber(String lotNumber, String rmCertificateNo) {
        logger.info("Fetching heat numbers for lot number: {} and RM certificate: {}", lotNumber, rmCertificateNo);
        List<String> heatNumbers = processInspectionDetailsRepository.findHeatNumbersByLotNumber(lotNumber,
                rmCertificateNo);
        logger.info("Found {} heat numbers", heatNumbers.size());
        return heatNumbers;
    }

    @Override
    public List<String> getProcessIcCertificateNumbersByMultipleRmCertificates(List<String> rmCertificateNos) {
        logger.info("Fetching Process IC certificate numbers for multiple RM certificates: {}", rmCertificateNos);
        List<String> certificateNumbers = inspectionCompleteDetailsRepository
                .findProcessIcNumbersByMultipleRmIcNumbers(rmCertificateNos);
        logger.info("Found {} Process IC certificate numbers", certificateNumbers.size());
        return certificateNumbers;
    }

    @Override
    public List<String> getLotNumbersByMultipleRmAndProcessCertificates(List<String> rmCertificateNos,
                                                                        List<String> processCertificateNos) {
        logger.info("Fetching lot numbers for multiple RM certificates: {} and Process certificates: {}",
                rmCertificateNos, processCertificateNos);
        List<String> lotNumbers = processInspectionDetailsRepository
                .findLotNumbersByMultipleRmAndProcessIcNumbers(rmCertificateNos, processCertificateNos);
        logger.info("Found {} lot numbers", lotNumbers.size());
        return lotNumbers;
    }

    @Override
    public Integer getOfferedEarlierQuantity(String heatNo, String lotNumber) {
        logger.info("Fetching offered earlier quantity for heat: {} and lot: {}", heatNo, lotNumber);
        Integer quantity = finalInspectionLotDetailsRepository.sumOfferedQtyByHeatNumberAndLotNumber(heatNo, lotNumber);
        logger.info("Offered earlier quantity: {}", quantity);
        return quantity != null ? quantity : 0;
    }

    @Override
    @Transactional
    public InspectionCall modifyFinalInspectionCall(
            String icNumber,
            InspectionCallRequestDto icDto,
            FinalInspectionDetailsRequestDto finalDto,
            List<FinalInspectionLotDetailsRequestDto> lotDtoList) {

        logger.info("========== MODIFY FINAL INSPECTION CALL ==========");
        logger.info("IC Number: {}", icNumber);
        logger.info("IC Dto: {}", icDto);

        InspectionCall inspection = inspectionCallRepository.findByIcNumber(icNumber)
                .orElseThrow(() -> new RuntimeException("Inspection Call Not Found"));

        // 1. Update main InspectionCall using reflection helper
        if (icDto != null) {
            inspectionCallService.processDtoFields(
                    icDto,
                    inspection,
                    inspection,
                    "inspection_call",
                    1,
                    icDto.getUpdatedBy() != null ? icDto.getUpdatedBy() : "SYSTEM_USER"
            );
        }

        // Find existing final details
        FinalInspectionDetails finalDetails = finalInspectionDetailsRepository.findByIcId(inspection.getId().longValue())
                .orElseThrow(() -> new RuntimeException("Final Inspection Details Not Found"));

        // 2. Update final details using reflection helper
        if (finalDto != null) {
            // Setup RM IC numbers multi-select if updated
            List<String> rmIcList = (finalDto.getRmIcNumbers() != null && !finalDto.getRmIcNumbers().isEmpty())
                    ? finalDto.getRmIcNumbers()
                    : (finalDto.getRmIcNumber() != null ? List.of(finalDto.getRmIcNumber()) : List.of());
            if (!rmIcList.isEmpty()) {
                String rmIcNumbersCsv = rmIcList.stream().collect(Collectors.joining(","));
                finalDetails.setRmIcNumber(rmIcNumbersCsv);
                Optional<InspectionCall> rmIcOpt = inspectionCallRepository.findByIcNumber(rmIcList.get(0));
                if (rmIcOpt.isPresent()) {
                    finalDetails.setRmIcId(rmIcOpt.get().getId().longValue());
                }
            }

            // Setup Process IC numbers multi-select if updated
            List<String> processIcList = (finalDto.getProcessIcNumbers() != null && !finalDto.getProcessIcNumbers().isEmpty())
                    ? finalDto.getProcessIcNumbers()
                    : (finalDto.getProcessIcNumber() != null ? List.of(finalDto.getProcessIcNumber()) : List.of());
            if (!processIcList.isEmpty()) {
                String processIcNumbersCsv = processIcList.stream().collect(Collectors.joining(","));
                finalDetails.setProcessIcNumber(processIcNumbersCsv);
                Optional<InspectionCall> processIcOpt = inspectionCallRepository.findByIcNumber(processIcList.get(0));
                if (processIcOpt.isPresent()) {
                    finalDetails.setProcessIcId(processIcOpt.get().getId().longValue());
                }
            }

            inspectionCallService.processDtoFields(
                    finalDto,
                    finalDetails,
                    inspection,
                    "final_inspection_details",
                    1,
                    icDto != null && icDto.getUpdatedBy() != null ? icDto.getUpdatedBy() : "SYSTEM_USER"
            );
            finalDetails.setUpdatedAt(LocalDateTime.now());
            finalInspectionDetailsRepository.save(finalDetails);
        }

        // 3. Handle lot details
        if (lotDtoList != null) {
            List<FinalInspectionLotDetails> existingLots = finalInspectionLotDetailsRepository.findByFinalDetailId(finalDetails.getId());

            // Delete lots no longer present
            for (FinalInspectionLotDetails existing : existingLots) {
                boolean stillExists = lotDtoList.stream()
                        .anyMatch(dto -> dto.getLotNumber() != null && dto.getLotNumber().equals(existing.getLotNumber()));
                if (!stillExists) {
                    finalInspectionLotDetailsRepository.delete(existing);
                }
            }

            // Update existing and insert new
            for (FinalInspectionLotDetailsRequestDto lotDto : lotDtoList) {
                if (lotDto.getLotNumber() == null || lotDto.getLotNumber().trim().isEmpty()) {
                    continue;
                }

                FinalInspectionLotDetails lot = existingLots.stream()
                        .filter(existing -> lotDto.getLotNumber().equals(existing.getLotNumber()))
                        .findFirst()
                        .orElse(null);

                if (lot != null) {
                    inspectionCallService.processDtoFields(
                            lotDto,
                            lot,
                            inspection,
                            "final_inspection_lot_details",
                            1,
                            icDto != null && icDto.getUpdatedBy() != null ? icDto.getUpdatedBy() : "SYSTEM_USER"
                    );
                    lot.setUpdatedAt(LocalDateTime.now());
                    finalInspectionLotDetailsRepository.save(lot);
                } else {
                    FinalInspectionLotDetails newLot = new FinalInspectionLotDetails();
                    newLot.setFinalDetailId(finalDetails.getId());
                    newLot.setLotNumber(lotDto.getLotNumber());
                    newLot.setHeatNumber(lotDto.getHeatNumber());
                    newLot.setManufacturer(lotDto.getManufacturer());
                    newLot.setManufacturerHeat(lotDto.getManufacturerHeat());
                    newLot.setOfferedQty(lotDto.getOfferedQty());
                    
                    if (lotDto.getNoOfBags() != null && lotDto.getNoOfBags() > 0) {
                        newLot.setNoOfBags(lotDto.getNoOfBags());
                    } else if (lotDto.getOfferedQty() != null) {
                        newLot.setNoOfBags((int) Math.ceil((double) lotDto.getOfferedQty() / 50));
                    }
                    
                    if (lotDto.getProcessIcNumber() != null) {
                        Optional<InspectionCall> processIcForLot = inspectionCallRepository.findByIcNumber(lotDto.getProcessIcNumber());
                        if (processIcForLot.isPresent()) {
                            newLot.setProcessIcId(processIcForLot.get().getId().longValue());
                        }
                        newLot.setProcessIcNumber(lotDto.getProcessIcNumber());
                    }
                    newLot.setCreatedAt(LocalDateTime.now());
                    newLot.setUpdatedAt(LocalDateTime.now());
                    finalInspectionLotDetailsRepository.save(newLot);
                }
            }

            // 4. Recreate Final Process IC Mapping
            finalProcessIcMappingRepository.deleteByFinalIcId(inspection.getId().longValue());

            List<String> processIcList = (finalDto != null && finalDto.getProcessIcNumbers() != null && !finalDto.getProcessIcNumbers().isEmpty())
                    ? finalDto.getProcessIcNumbers()
                    : (finalDetails.getProcessIcNumber() != null ? List.of(finalDetails.getProcessIcNumber().split(",")) : List.of());

            if (!processIcList.isEmpty()) {
                for (String processIcNumber : processIcList) {
                    String cleanProcessIcNo = processIcNumber.trim();
                    Optional<InspectionCall> processIcForMapping = inspectionCallRepository.findByIcNumber(cleanProcessIcNo);
                    if (!processIcForMapping.isPresent()) {
                        continue;
                    }
                    InspectionCall processIc = processIcForMapping.get();

                    for (FinalInspectionLotDetailsRequestDto lotDto : lotDtoList) {
                        FinalProcessIcMapping mapping = new FinalProcessIcMapping();
                        mapping.setFinalIcId(inspection.getId().longValue());
                        mapping.setProcessIcId(processIc.getId().longValue());
                        mapping.setProcessIcNumber(cleanProcessIcNo);
                        mapping.setLotNumber(lotDto.getLotNumber());
                        mapping.setHeatNumber(lotDto.getHeatNumber());
                        mapping.setManufacturer(lotDto.getManufacturer());
                        mapping.setProcessQtyAccepted(lotDto.getOfferedQty());
                        mapping.setProcessIcDate(processIc.getDesiredInspectionDate());

                        finalProcessIcMappingRepository.save(mapping);
                    }
                }
            }
        }

        inspection.setIsModified(true);
        inspection.setUpdatedAt(LocalDateTime.now());
        if (icDto != null && icDto.getUpdatedBy() != null) {
            inspection.setUpdatedBy(icDto.getUpdatedBy());
        }

        return inspectionCallRepository.save(inspection);
    }
}