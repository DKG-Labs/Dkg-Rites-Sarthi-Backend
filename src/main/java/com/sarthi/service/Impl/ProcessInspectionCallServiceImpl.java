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
import com.sarthi.util.IcNumberGenerator;
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

    @Autowired
    public ProcessInspectionCallServiceImpl(
            InspectionCallRepository inspectionCallRepository,
            ProcessInspectionDetailsRepository processDetailsRepository,
            ProcessRmIcMappingRepository processMappingRepository,
            IcNumberGenerator icNumberGenerator) {
        this.inspectionCallRepository = inspectionCallRepository;
        this.processDetailsRepository = processDetailsRepository;
        this.processMappingRepository = processMappingRepository;
        this.icNumberGenerator = icNumberGenerator;
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
        return inspectionCall;
    }
}
