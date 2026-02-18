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

        // Get RM IC and Process IC IDs from IC numbers
        Optional<InspectionCall> rmIcOpt = inspectionCallRepository.findByIcNumber(finalDetails.getRmIcNumber());
        Optional<InspectionCall> processIcOpt = inspectionCallRepository
                .findByIcNumber(finalDetails.getProcessIcNumber());

        if (rmIcOpt.isPresent()) {
            finalInspectionDetails.setRmIcId(rmIcOpt.get().getId().longValue());
        }
        finalInspectionDetails.setRmIcNumber(finalDetails.getRmIcNumber());

        if (processIcOpt.isPresent()) {
            finalInspectionDetails.setProcessIcId(processIcOpt.get().getId().longValue());
        }
        finalInspectionDetails.setProcessIcNumber(finalDetails.getProcessIcNumber());

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
        // Create mapping entries for each lot (linking Final IC to Process IC)
        if (processIcOpt.isPresent() && lotDetailsList != null && !lotDetailsList.isEmpty()) {
            InspectionCall processIc = processIcOpt.get();

            for (FinalInspectionLotDetailsRequestDto lotDto : lotDetailsList) {
                FinalProcessIcMapping mapping = new FinalProcessIcMapping();

                mapping.setFinalIcId(inspectionCall.getId().longValue());
                mapping.setProcessIcId(processIc.getId().longValue());
                mapping.setProcessIcNumber(finalDetails.getProcessIcNumber());
                mapping.setLotNumber(lotDto.getLotNumber());
                mapping.setHeatNumber(lotDto.getHeatNumber());
                mapping.setManufacturer(lotDto.getManufacturer());
                mapping.setProcessQtyAccepted(lotDto.getOfferedQty()); // Assuming offered qty from process
                mapping.setProcessIcDate(processIc.getDesiredInspectionDate());

                finalProcessIcMappingRepository.save(mapping);
                logger.info("✅ Final-Process IC Mapping saved for Lot: {}", lotDto.getLotNumber());
            }
        }

        logger.info("========== FINAL INSPECTION CALL CREATED SUCCESSFULLY ==========");
        logger.info("IC Number: {}", icNumber);
        logger.info("Total Lots: {}", lotDetailsList != null ? lotDetailsList.size() : 0);

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
}
