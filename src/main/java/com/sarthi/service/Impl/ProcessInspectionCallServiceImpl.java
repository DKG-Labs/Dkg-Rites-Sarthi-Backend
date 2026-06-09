package com.sarthi.service.Impl;

import com.sarthi.dto.IcDtos.InspectionCallRequestDto;
import com.sarthi.dto.IcDtos.ProcessInspectionDetailsRequestDto;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessRmIcMapping;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.processmaterial.ProcessRmIcMappingRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.ProcessInspectionCallService;
import com.sarthi.service.InspectionCallService;
import com.sarthi.util.IcNumberGenerator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProcessInspectionCallServiceImpl implements ProcessInspectionCallService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInspectionCallServiceImpl.class);

    private final InspectionCallRepository inspectionCallRepository;
    private final ProcessInspectionDetailsRepository processDetailsRepository;
    private final ProcessRmIcMappingRepository processMappingRepository;
    private final IcNumberGenerator icNumberGenerator;
    private final InspectionCallService inspectionCallService;
    private final com.sarthi.service.WorkflowService workflowService;

    @Autowired
    public ProcessInspectionCallServiceImpl(
            InspectionCallRepository inspectionCallRepository,
            ProcessInspectionDetailsRepository processDetailsRepository,
            ProcessRmIcMappingRepository processMappingRepository,
            IcNumberGenerator icNumberGenerator,
            InspectionCallService inspectionCallService,
            com.sarthi.service.WorkflowService workflowService) {
        this.inspectionCallRepository = inspectionCallRepository;
        this.processDetailsRepository = processDetailsRepository;
        this.processMappingRepository = processMappingRepository;
        this.icNumberGenerator = icNumberGenerator;
        this.inspectionCallService = inspectionCallService;
        this.workflowService = workflowService;
    }

    @Override
    public InspectionCall createProcessInspectionCall(
            InspectionCallRequestDto icRequest,
            List<ProcessInspectionDetailsRequestDto> processDetailsList) {
        logger.info("========== CREATE PROCESS INSPECTION CALL ==========");
        logger.info("IC Request: {}", icRequest);
        logger.info("ERC Type from Request: {}", icRequest.getErcType());
        logger.info("Process Details Count: {}", processDetailsList != null ? processDetailsList.size() : 0);

        // ================== 1. CREATE INSPECTION CALL ==================
        InspectionCall inspectionCall = new InspectionCall();

        // Generate IC Number with daily sequence reset
        LocalDate today = LocalDate.now();
        long dailySequence = inspectionCallRepository.countByTypeOfCallAndCreatedDate("Process", today) + 1;
        String icNumber = icNumberGenerator.generateIcNumber("Process", dailySequence);
        logger.info("Generated IC Number: {} (Daily Sequence: {})", icNumber, dailySequence);

        logger.info("🔍 DEBUG: icRequest.getErcType() = {}", icRequest.getErcType());

        inspectionCall.setIcNumber(icNumber);
        inspectionCall.setPoNo(icRequest.getPoNo());
        inspectionCall.setPoSerialNo(icRequest.getPoSerialNo());
        inspectionCall.setTypeOfCall(icRequest.getTypeOfCall());
        inspectionCall.setErcType(icRequest.getErcType());
        inspectionCall.setStatus(icRequest.getStatus());
        inspectionCall.setPlaceOfInspection(icRequest.getPlaceOfInspection());

        logger.info("🔍 DEBUG: After setting - inspectionCall.getErcType() = {}", inspectionCall.getErcType());
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
        logger.info("✅ Inspection Call saved with ID: {}", inspectionCall.getId());

        // ================== 2. CREATE PROCESS INSPECTION DETAILS (MULTIPLE ROWS FOR
        // MULTIPLE LOTS) ==================
        if (processDetailsList != null && !processDetailsList.isEmpty()) {
            logger.info("📦 Creating {} lot records for Process IC: {}", processDetailsList.size(),
                    inspectionCall.getIcNumber());

            // Get RM IC reference (same for all lots)
            ProcessInspectionDetailsRequestDto firstDetail = processDetailsList.get(0);
            String rmIcNumberFromRequest = firstDetail.getRmIcNumber();

            // Extract call number from certificate number if needed
            String callNumber = rmIcNumberFromRequest;
            if (rmIcNumberFromRequest != null && rmIcNumberFromRequest.startsWith("N/")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("N/([^/]+)/");
                java.util.regex.Matcher matcher = pattern.matcher(rmIcNumberFromRequest);
                if (matcher.find()) {
                    callNumber = matcher.group(1);
                    logger.info("📋 Extracted call number '{}' from certificate number '{}'", callNumber,
                            rmIcNumberFromRequest);
                }
            }

            // Fetch RM IC to get rmIcId
            InspectionCall rmIc = null;
            if (callNumber != null && !callNumber.isEmpty()) {
                rmIc = inspectionCallRepository.findByIcNumber(callNumber).orElse(null);
                if (rmIc != null) {
                    logger.info("✅ Found RM IC with call number '{}' and ID: {}", callNumber, rmIc.getId());
                } else {
                    logger.warn("⚠️ RM IC not found for call number: {}. Proceeding without RM IC reference.",
                            callNumber);
                }
            }

            // ================== CREATE ONE ROW PER LOT ==================
            int lotCounter = 0;
            for (ProcessInspectionDetailsRequestDto detail : processDetailsList) {
                lotCounter++;

                ProcessInspectionDetails processDetails = new ProcessInspectionDetails();

                // Set the SAME ic_id for all lots
                processDetails.setInspectionCall(inspectionCall);

                // Set RM IC reference
                processDetails.setRmIcNumber(rmIcNumberFromRequest);
                processDetails.setRmIcId(rmIc != null ? rmIc.getId() : null);

                // Set INDIVIDUAL lot information
                processDetails.setLotNumber(detail.getLotNumber());
                processDetails.setHeatNumber(detail.getHeatNumber());
                processDetails.setManufacturer(detail.getManufacturer());
                processDetails.setManufacturerHeat(detail.getManufacturerHeat());

                // Set INDIVIDUAL quantity information
                processDetails.setOfferedQty(detail.getOfferedQty());
                processDetails.setTotalAcceptedQtyRm(detail.getTotalAcceptedQtyRm());
                processDetails.setDeclaredLotSize(detail.getDeclaredLotSize());
                processDetails.setTentativeStartDate(detail.getTentativeStartDate());

                // Set place of inspection (from request or from RM IC if available)
                processDetails.setCompanyId(detail.getCompanyId() != null ? detail.getCompanyId()
                        : (rmIc != null ? rmIc.getCompanyId() : null));
                processDetails.setCompanyName(detail.getCompanyName() != null ? detail.getCompanyName()
                        : (rmIc != null ? rmIc.getCompanyName() : null));
                processDetails.setUnitId(
                        detail.getUnitId() != null ? detail.getUnitId() : (rmIc != null ? rmIc.getUnitId() : null));
                processDetails.setUnitName(detail.getUnitName() != null ? detail.getUnitName()
                        : (rmIc != null ? rmIc.getUnitName() : null));
                processDetails.setUnitAddress(detail.getUnitAddress() != null ? detail.getUnitAddress()
                        : (rmIc != null ? rmIc.getUnitAddress() : null));

                // Save each lot as a separate row
                processDetails = processDetailsRepository.save(processDetails);
                logger.info("✅ Lot {}/{} saved - ID: {} | Lot: {} | Qty: {}",
                        lotCounter, processDetailsList.size(), processDetails.getId(),
                        detail.getLotNumber(), detail.getOfferedQty());
            }

            logger.info("🎉 Successfully saved {} lots for Process IC: {}", lotCounter, inspectionCall.getIcNumber());

            // ================== 3. CREATE PROCESS RM IC MAPPING ==================
            // The rmIcNumberFromRequest may be comma-separated (multiple RM ICs selected).
            // We split it and create ONE mapping row per RM IC (correct relational design).
            if (rmIcNumberFromRequest != null && !rmIcNumberFromRequest.isEmpty()) {
                String[] allRmIcNumbers = rmIcNumberFromRequest.split(",");

                for (String rawRmIcEntry : allRmIcNumbers) {
                    String singleCertNo = rawRmIcEntry.trim();

                    // Extract call number from certificate format "N/ER-xxxxx/RAJK"
                    String singleCallNo = singleCertNo;
                    if (singleCertNo.startsWith("N/")) {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("N/([^/]+)/");
                        java.util.regex.Matcher m = p.matcher(singleCertNo);
                        if (m.find()) {
                            singleCallNo = m.group(1);
                        }
                    }

                    InspectionCall singleRmIc = inspectionCallRepository.findByIcNumber(singleCallNo).orElse(null);
                    if (singleRmIc == null) {
                        logger.warn("⚠️ RM IC not found for '{}' (extracted: '{}'). Skipping mapping row.",
                                singleCertNo, singleCallNo);
                        continue;
                    }

                    for (ProcessInspectionDetailsRequestDto detail : processDetailsList) {
                        ProcessRmIcMapping mapping = new ProcessRmIcMapping();

                        mapping.setProcessIcId(inspectionCall.getId());
                        mapping.setRmIcId(singleRmIc.getId());
                        mapping.setRmIcNumber(singleCertNo); // ← single IC number, no truncation
                        mapping.setHeatNumber(detail.getHeatNumber());
                        mapping.setManufacturer(detail.getManufacturer());
                        mapping.setRmQtyAccepted(
                                detail.getTotalAcceptedQtyRm() != null ? detail.getTotalAcceptedQtyRm() : 0);
                        mapping.setRmIcDate(singleRmIc.getDesiredInspectionDate());

                        processMappingRepository.save(mapping);
                        logger.info("✅ Mapping saved: RmIC={} Heat={}", singleCertNo, detail.getHeatNumber());
                    }
                }
            } else {
                logger.info("⚠️ Skipping Process RM IC Mapping creation - no RM IC numbers provided");
            }
        }
        logger.info("========== PROCESS INSPECTION CALL CREATED SUCCESSFULLY ==========");
        
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
    @Transactional
    public InspectionCall modifyProcessInspectionCall(
            String icNumber,
            InspectionCallRequestDto icDto,
            List<ProcessInspectionDetailsRequestDto> processDetailsList) {

        logger.info("========== MODIFY PROCESS INSPECTION CALL ==========");
        logger.info("IC Number: {}", icNumber);
        logger.info("IC Dto: {}", icDto);

        InspectionCall inspection = inspectionCallRepository.findByIcNumber(icNumber)
                .orElseThrow(() -> new RuntimeException("Inspection Call Not Found"));

        // Update main InspectionCall fields using the reflection helper
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

        // Handle lots
        if (processDetailsList != null) {
            List<ProcessInspectionDetails> existingLots = processDetailsRepository.findByIcId(inspection.getId());

            // 1. Delete lots that are no longer present in the request list (matched by lotNumber)
            for (ProcessInspectionDetails existing : existingLots) {
                boolean stillExists = processDetailsList.stream()
                        .anyMatch(dto -> dto.getLotNumber() != null && dto.getLotNumber().equals(existing.getLotNumber()));
                if (!stillExists) {
                    processDetailsRepository.delete(existing);
                }
            }

            // 2. Update existing lots and insert new ones
            for (ProcessInspectionDetailsRequestDto dto : processDetailsList) {
                if (dto.getLotNumber() == null || dto.getLotNumber().trim().isEmpty()) {
                    continue;
                }

                // Find matching existing lot
                ProcessInspectionDetails lot = existingLots.stream()
                        .filter(existing -> dto.getLotNumber().equals(existing.getLotNumber()))
                        .findFirst()
                        .orElse(null);

                if (lot != null) {
                    // Update existing lot using processDtoFields reflection
                    inspectionCallService.processDtoFields(
                            dto,
                            lot,
                            inspection,
                            "process_inspection_details",
                            1,
                            icDto != null && icDto.getUpdatedBy() != null ? icDto.getUpdatedBy() : "SYSTEM_USER"
                    );
                    
                    // Always make sure relations and update date are set
                    lot.setUpdatedAt(LocalDateTime.now());
                    processDetailsRepository.save(lot);
                } else {
                    // Insert new lot
                    ProcessInspectionDetails newLot = new ProcessInspectionDetails();
                    newLot.setInspectionCall(inspection);
                    newLot.setLotNumber(dto.getLotNumber());
                    newLot.setHeatNumber(dto.getHeatNumber());
                    newLot.setManufacturer(dto.getManufacturer());
                    newLot.setManufacturerHeat(dto.getManufacturerHeat());
                    newLot.setOfferedQty(dto.getOfferedQty());
                    newLot.setTotalAcceptedQtyRm(dto.getTotalAcceptedQtyRm());
                    newLot.setDeclaredLotSize(dto.getDeclaredLotSize());
                    newLot.setTentativeStartDate(dto.getTentativeStartDate());
                    newLot.setRmIcNumber(dto.getRmIcNumber());
                    newLot.setCreatedAt(LocalDateTime.now());
                    newLot.setUpdatedAt(LocalDateTime.now());
                    
                    // Extract RM IC reference if exists
                    if (dto.getRmIcNumber() != null) {
                        String callNo = dto.getRmIcNumber();
                        if (callNo.startsWith("N/")) {
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("N/([^/]+)/");
                            java.util.regex.Matcher matcher = pattern.matcher(callNo);
                            if (matcher.find()) {
                                callNo = matcher.group(1);
                            }
                        }
                        Optional<InspectionCall> rmIc = inspectionCallRepository.findByIcNumber(callNo);
                        if (rmIc.isPresent()) {
                            newLot.setRmIcId(rmIc.get().getId());
                            newLot.setCompanyId(rmIc.get().getCompanyId());
                            newLot.setCompanyName(rmIc.get().getCompanyName());
                            newLot.setUnitId(rmIc.get().getUnitId());
                            newLot.setUnitName(rmIc.get().getUnitName());
                            newLot.setUnitAddress(rmIc.get().getUnitAddress());
                        }
                    }
                    
                    processDetailsRepository.save(newLot);
                }
            }

            // 3. Recreate ProcessRmIcMapping
            processMappingRepository.deleteByProcessIcId(inspection.getId());

            // Create new mapping rows
            ProcessInspectionDetailsRequestDto firstDetail = processDetailsList.isEmpty() ? null : processDetailsList.get(0);
            String rmIcNumberFromRequest = firstDetail != null ? firstDetail.getRmIcNumber() : null;

            if (rmIcNumberFromRequest != null && !rmIcNumberFromRequest.isEmpty()) {
                String[] allRmIcNumbers = rmIcNumberFromRequest.split(",");
                for (String rawRmIcEntry : allRmIcNumbers) {
                    String singleCertNo = rawRmIcEntry.trim();
                    String singleCallNo = singleCertNo;
                    if (singleCertNo.startsWith("N/")) {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("N/([^/]+)/");
                        java.util.regex.Matcher m = p.matcher(singleCertNo);
                        if (m.find()) {
                            singleCallNo = m.group(1);
                        }
                    }

                    InspectionCall singleRmIc = inspectionCallRepository.findByIcNumber(singleCallNo).orElse(null);
                    if (singleRmIc == null) {
                        continue;
                    }

                    for (ProcessInspectionDetailsRequestDto detail : processDetailsList) {
                        ProcessRmIcMapping mapping = new ProcessRmIcMapping();
                        mapping.setProcessIcId(inspection.getId());
                        mapping.setRmIcId(singleRmIc.getId());
                        mapping.setRmIcNumber(singleCertNo);
                        mapping.setHeatNumber(detail.getHeatNumber());
                        mapping.setManufacturer(detail.getManufacturer());
                        mapping.setRmQtyAccepted(detail.getTotalAcceptedQtyRm() != null ? detail.getTotalAcceptedQtyRm() : 0);
                        mapping.setRmIcDate(singleRmIc.getDesiredInspectionDate());

                        processMappingRepository.save(mapping);
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
