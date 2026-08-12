package com.sarthi.service.certificate.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sarthi.dto.certificate.RawMaterialCertificateDto;
import com.sarthi.dto.certificate.ProcessMaterialCertificateDto;
import com.sarthi.dto.certificate.FinalCertificateDto;
import com.sarthi.dto.certificate.IcReportDataResponse;
import com.sarthi.entity.InspectionCompleteDetails;
import com.sarthi.entity.MainPoInformation;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.entity.RmHeatFinalResult;
import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import com.sarthi.repository.MainPoInformationRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoItemRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.processmaterial.ProcessRmIcMappingRepository;
import com.sarthi.entity.processmaterial.ProcessRmIcMapping;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.rawmaterial.RmIcEdit;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import com.sarthi.entity.rawmaterial.RmIcSaveChanges;
import com.sarthi.repository.rawmaterial.RmIcSaveChangesRepository;
import com.sarthi.entity.processmaterial.ProcessIcSaveChanges;
import com.sarthi.repository.processmaterial.ProcessIcSaveChangesRepository;
import com.sarthi.entity.finalmaterial.FinalIcSaveChanges;
import com.sarthi.repository.finalmaterial.FinalIcSaveChangesRepository;
import com.sarthi.entity.processmaterial.ProcessIcEdit;
import com.sarthi.repository.processmaterial.ProcessIcEditRepository;
import com.sarthi.entity.finalmaterial.FinalIcEdit;
import com.sarthi.repository.finalmaterial.FinalIcEditRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import com.sarthi.service.certificate.CertificateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sarthi.entity.finalmaterial.FinalInspectionLotResults;
import com.sarthi.repository.finalmaterial.FinalInspectionLotResultsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for generating Inspection Certificates.
 * Aggregates data from multiple tables to create certificate data.
 */
@Service
@Transactional(readOnly = true)
public class CertificateServiceImpl implements CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private RmInspectionDetailsRepository rmInspectionDetailsRepository;

    @Autowired
    private RmHeatQuantityRepository rmHeatQuantityRepository;

    @Autowired
    private RmHeatFinalResultRepository rmHeatFinalResultRepository;

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private PoItemRepository poItemRepository;

    @Autowired
    private MainPoInformationRepository mainPoInformationRepository;

    @Autowired
    private com.sarthi.repository.InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;

    @Autowired
    private ProcessInspectionDetailsRepository processInspectionDetailsRepository;

    @Autowired
    private RmIcEditRepository rmIcEditRepository;

    @Autowired
    private RmIcSaveChangesRepository rmIcSaveChangesRepository;

    @Autowired
    private ProcessIcEditRepository processIcEditRepository;

    @Autowired
    private ProcessIcSaveChangesRepository processIcSaveChangesRepository;

    @Autowired
    private FinalIcEditRepository finalIcEditRepository;

    @Autowired
    private FinalIcSaveChangesRepository finalIcSaveChangesRepository;

    @Autowired
    private ProcessLineFinalResultRepository processLineFinalResultRepository;

    @Autowired
    private ProcessRmIcMappingRepository processRmIcMappingRepository;

    @Autowired
    private FinalInspectionDetailsRepository finalInspectionDetailsRepository;

    @Autowired
    private FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;

    @Autowired
    private FinalCumulativeResultsRepository finalCumulativeResultsRepository;

    @Autowired
    private FinalInspectionLotResultsRepository finalInspectionLotResultsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.sarthi.repository.WorkflowTransitionRepository workflowTransitionRepository;

    @Autowired
    private com.sarthi.repository.UserMasterRepository userMasterRepository;

    @Override
    public RawMaterialCertificateDto generateRawMaterialCertificate(String icNumber) {
        logger.info("Generating Raw Material Certificate for IC Number: {}", icNumber);

        // 1. Fetch Inspection Call
        InspectionCall inspectionCall = inspectionCallRepository.findByIcNumber(icNumber)
                .orElseThrow(() -> new IllegalArgumentException("Inspection call not found: " + icNumber));

        return buildCertificateDto(inspectionCall);
    }

    @Override
    public RawMaterialCertificateDto generateRawMaterialCertificateById(Long callId) {
        logger.info("Generating Raw Material Certificate for Call ID: {}", callId);

        // 1. Fetch Inspection Call
        InspectionCall inspectionCall = inspectionCallRepository.findById(Math.toIntExact(callId))
                .orElseThrow(() -> new IllegalArgumentException("Inspection call not found with ID: " + callId));

        return buildCertificateDto(inspectionCall);
    }

    /**
     * Build the complete certificate DTO from inspection call data
     */
    private RawMaterialCertificateDto buildCertificateDto(InspectionCall inspectionCall) {
        logger.info("Building certificate DTO for IC: {}", inspectionCall.getIcNumber());

        // 2. Fetch RM Inspection Details
        RmInspectionDetails rmDetails = rmInspectionDetailsRepository.findByIcId(inspectionCall.getId())
                .orElse(null);

        // 3. Fetch Heat Quantities
        List<RmHeatQuantity> heatQuantities = new ArrayList<>();
        if (rmDetails != null) {
            heatQuantities = rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(rmDetails.getId()));
        }

        // 4. Fetch Heat Final Results
        List<RmHeatFinalResult> heatResults = rmHeatFinalResultRepository
                .findByInspectionCallNo(inspectionCall.getIcNumber());

        // 5. Fetch PO Header and Items
        PoHeader poHeader = poHeaderRepository.findByPoNo(inspectionCall.getPoNo()).orElse(null);
        List<PoItem> poItems = new ArrayList<>();
        if (poHeader != null) {
            poItems = poItemRepository.findByPoHeader_Id(poHeader.getId());
        }

        // 6. Fetch Section A data (Main PO Information) for Bill Paying Officer
        MainPoInformation mainPoInfo = mainPoInformationRepository
                .findByInspectionCallNo(inspectionCall.getIcNumber())
                .orElse(null);

        if (mainPoInfo != null) {
            logger.info("✅ Section A data found for IC: {}", inspectionCall.getIcNumber());
            logger.info("   Bill Paying Officer: {}", mainPoInfo.getBillPayingOfficer());
            logger.info("   Purchasing Authority: {}", mainPoInfo.getPurchasingAuthority());
        } else {
            logger.warn("⚠️ Section A data NOT found for IC: {}", inspectionCall.getIcNumber());
        }

        // 7. Load Sections Pre-calculated
        calculateOfferedInstallment(inspectionCall.getPoNo());
        calculatePassedInstallment(inspectionCall.getPoNo());

        List<LocalDate> visitDates = getVisitDates(inspectionCall.getIcNumber());

        // 10. Build Certificate DTO
        RawMaterialCertificateDto dto = RawMaterialCertificateDto.builder()
                .certificateNo(generateCertificateNumber(inspectionCall))
                .certificateDate(formatDate(LocalDate.now()))
                .offeredInstNo(calculateOfferedInstallment(inspectionCall.getPoNo()))
                .passedInstNo(calculatePassedInstallment(inspectionCall.getPoNo()))
                .contractor(buildContractorInfo(poHeader))
                .manufacturer(buildManufacturerInfo(heatQuantities))
                .placeOfInspection(buildPlaceOfInspection(inspectionCall))
                .contractRef(buildContractRef(poHeader, inspectionCall))
                .contractorPo(inspectionCall.getPoNo())
                .billPayingOfficer(buildBillPayingOfficer(inspectionCall, poItems))
                .consigneeRailway(buildConsigneeRailway(inspectionCall, poItems))
                .consigneeManufacturer(buildConsigneeManufacturer(poHeader))
                .purchasingAuthority(buildPurchasingAuthority(poHeader, mainPoInfo))
                .description(buildItemDescription(inspectionCall, poItems))
                .ercType(inspectionCall.getErcType())
                .drgNo("") // Keep blank
                .specNo("IRS T-31-2025")
                .qapNo("Clause No.4.11.2 & 4.11.3 of Indian Railway Standard Specification for Elastic Rail Clip, IRS T-31-2025")
                .inspectionType("Visual/Physical/Chemical/Metallurgical/Dimensional")
                .chpClause(
                        "Clause No.4.11.2 & 4.11.3 of Indian Railway Standard Specification for Elastic Rail Clip, IRS T-31-2025")
                .contractChpReq("Visual, Dimensional, Mechanical & Chemical")
                .detailsOfInspection("Visual, Dimensional, Mechanical & Chemical")
                .result(buildResult(heatResults))
                .qtyCleared(buildQtyCleared(heatResults))
                .qtyRejected(buildQtyRejected(heatResults))
                .remarks(buildRemarks(inspectionCall, heatResults))
                .dateOfCall(buildDateOfCall(inspectionCall))
                .noOfVisits(visitDates.isEmpty() ? "" : String.valueOf(visitDates.size()))
                .dateOfInspection(formatDateRange(visitDates))
                .sealingPattern(buildSealingPattern(heatResults))
                .sealFacsimile("") // Blank for stamp
                .inspectingEngineer("") // Keep blank for now (DSC signature)
                .heatDetails(buildHeatDetails(heatQuantities, heatResults))
                .build();

        // Merge saved draft edits if available, else fallback to final edits
        Optional<RmIcSaveChanges> rmIcSaveChangesOpt = rmIcSaveChangesRepository
                .findByIcNumber(inspectionCall.getIcNumber());
        if (rmIcSaveChangesOpt.isPresent()) {
            RmIcSaveChanges saveChanges = rmIcSaveChangesOpt.get();
            if (saveChanges.getBookNo() != null && !saveChanges.getBookNo().isBlank()) {
                dto.setBookNo(saveChanges.getBookNo());
            }
            if (saveChanges.getSetNo() != null && !saveChanges.getSetNo().isBlank()) {
                dto.setSetNo(saveChanges.getSetNo());
            }
            if (saveChanges.getOfferedInstallmentNo() != null && !saveChanges.getOfferedInstallmentNo().isBlank()) {
                dto.setOfferedInstNo(saveChanges.getOfferedInstallmentNo());
            }
            if (saveChanges.getPassedInstallmentNo() != null && !saveChanges.getPassedInstallmentNo().isBlank()) {
                dto.setPassedInstNo(saveChanges.getPassedInstallmentNo());
            }
            if (saveChanges.getDrawingNo() != null && !saveChanges.getDrawingNo().isBlank()) {
                dto.setDrgNo(saveChanges.getDrawingNo());
            }
            if (saveChanges.getManufacturer() != null && !saveChanges.getManufacturer().isBlank()) {
                dto.setManufacturer(saveChanges.getManufacturer());
            }
            if (saveChanges.getContractorPo() != null && !saveChanges.getContractorPo().isBlank()) {
                dto.setContractorPo(saveChanges.getContractorPo());
            }
            if (saveChanges.getConsigneeRailway() != null && !saveChanges.getConsigneeRailway().isBlank()) {
                dto.setConsigneeRailway(saveChanges.getConsigneeRailway());
            }
            if (saveChanges.getConsigneeManufacturer() != null && !saveChanges.getConsigneeManufacturer().isBlank()) {
                dto.setConsigneeManufacturer(saveChanges.getConsigneeManufacturer());
            }
            if (saveChanges.getPurchasingAuthority() != null && !saveChanges.getPurchasingAuthority().isBlank()) {
                dto.setPurchasingAuthority(saveChanges.getPurchasingAuthority());
            }
            if (saveChanges.getDescription() != null && !saveChanges.getDescription().isBlank()) {
                dto.setDescription(saveChanges.getDescription());
            }
            if (saveChanges.getSpecNo() != null && !saveChanges.getSpecNo().isBlank()) {
                dto.setSpecNo(saveChanges.getSpecNo());
            }
            if (saveChanges.getQapNo() != null && !saveChanges.getQapNo().isBlank()) {
                dto.setQapNo(saveChanges.getQapNo());
            }
            if (saveChanges.getChpClause() != null && !saveChanges.getChpClause().isBlank()) {
                dto.setChpClause(saveChanges.getChpClause());
            }
        } else {
            Optional<RmIcEdit> rmIcEditOpt = rmIcEditRepository.findByIcNumber(inspectionCall.getIcNumber());
            if (rmIcEditOpt.isPresent()) {
                RmIcEdit rmIcEdit = rmIcEditOpt.get();
                if (rmIcEdit.getBookNo() != null && !rmIcEdit.getBookNo().isBlank()) {
                    dto.setBookNo(rmIcEdit.getBookNo());
                }
                if (rmIcEdit.getSetNo() != null && !rmIcEdit.getSetNo().isBlank()) {
                    dto.setSetNo(rmIcEdit.getSetNo());
                }
                if (rmIcEdit.getOfferedInstallmentNo() != null && !rmIcEdit.getOfferedInstallmentNo().isBlank()) {
                    dto.setOfferedInstNo(rmIcEdit.getOfferedInstallmentNo());
                }
                if (rmIcEdit.getPassedInstallmentNo() != null && !rmIcEdit.getPassedInstallmentNo().isBlank()) {
                    dto.setPassedInstNo(rmIcEdit.getPassedInstallmentNo());
                }
                if (rmIcEdit.getDrawingNo() != null && !rmIcEdit.getDrawingNo().isBlank()) {
                    dto.setDrgNo(rmIcEdit.getDrawingNo());
                }
                if (rmIcEdit.getManufacturer() != null && !rmIcEdit.getManufacturer().isBlank()) {
                    dto.setManufacturer(rmIcEdit.getManufacturer());
                }
                if (rmIcEdit.getContractorPo() != null && !rmIcEdit.getContractorPo().isBlank()) {
                    dto.setContractorPo(rmIcEdit.getContractorPo());
                }
                if (rmIcEdit.getConsigneeRailway() != null && !rmIcEdit.getConsigneeRailway().isBlank()) {
                    dto.setConsigneeRailway(rmIcEdit.getConsigneeRailway());
                }
                if (rmIcEdit.getConsigneeManufacturer() != null && !rmIcEdit.getConsigneeManufacturer().isBlank()) {
                    dto.setConsigneeManufacturer(rmIcEdit.getConsigneeManufacturer());
                }
                if (rmIcEdit.getPurchasingAuthority() != null && !rmIcEdit.getPurchasingAuthority().isBlank()) {
                    dto.setPurchasingAuthority(rmIcEdit.getPurchasingAuthority());
                }
                if (rmIcEdit.getDescription() != null && !rmIcEdit.getDescription().isBlank()) {
                    dto.setDescription(rmIcEdit.getDescription());
                }
                if (rmIcEdit.getSpecNo() != null && !rmIcEdit.getSpecNo().isBlank()) {
                    dto.setSpecNo(rmIcEdit.getSpecNo());
                }
                if (rmIcEdit.getQapNo() != null && !rmIcEdit.getQapNo().isBlank()) {
                    dto.setQapNo(rmIcEdit.getQapNo());
                }
                if (rmIcEdit.getChpClause() != null && !rmIcEdit.getChpClause().isBlank()) {
                    dto.setChpClause(rmIcEdit.getChpClause());
                }
            }
        }

        return dto;
    }

    /*
     * ==================== Helper Methods for Building Certificate Fields
     * ====================
     */

    /**
     * Build Place of Inspection
     * Format: Company Name + Unit Address
     */
    private String buildPlaceOfInspection(InspectionCall inspectionCall) {
        if (inspectionCall == null)
            return "";

        // Construct the fallback address from company and unit details
        String companyName = inspectionCall.getCompanyName() != null ? inspectionCall.getCompanyName() : "";
        String unitAddress = inspectionCall.getUnitAddress() != null ? inspectionCall.getUnitAddress() : "";
        String constructedAddress = companyName + (unitAddress.isBlank() ? "" : ", " + unitAddress);

        String directPlace = inspectionCall.getPlaceOfInspection();

        // If direct place is present AND is not a POI code, use it.
        // Otherwise, use the constructed address.
        if (directPlace != null && !directPlace.isBlank() && !directPlace.toUpperCase().startsWith("POI")) {
            return directPlace;
        }

        return constructedAddress;
    }

    /**
     * Generate Certificate Number
     * Fetches from inspection_complete_details table if available
     * Format: {RIO_First_Letter}/{IC_Number}/{IE_Short_Name}
     * Example: N/RM-IC-1767618858167/RAJK
     *
     * Falls back to IC number if not found in inspection_complete_details
     */
    private String generateCertificateNumber(InspectionCall inspectionCall) {
        String certNo = inspectionCompleteDetailsRepository.findCertificateNoByCallNo(inspectionCall.getIcNumber());
        return certNo != null ? certNo : (inspectionCall.getIcNumber() != null ? inspectionCall.getIcNumber() : "");
    }

    /**
     * Calculate Offered Installment Number
     * Count of all inspection calls for this PO
     */
    private String calculateOfferedInstallment(String poNo) {
        long start = System.currentTimeMillis();
        long count = inspectionCallRepository.countByPoNo(poNo);
        long end = System.currentTimeMillis();
        logger.info("countByPoNo for {} took {} ms", poNo, (end - start));
        return String.valueOf(count);
    }

    /**
     * Calculate Passed Installment Number
     * Count of accepted ICs for this PO
     */
    private String calculatePassedInstallment(String poNo) {
        long start = System.currentTimeMillis();
        long count = inspectionCallRepository.countByPoNoAndStatusIn(poNo, List.of("ACCEPTED", "COMPLETED"));
        long end = System.currentTimeMillis();
        logger.info("countByPoNoAndStatusIn for {} took {} ms", poNo, (end - start));
        return String.valueOf(count);
    }

    /**
     * Build Contractor Information (Vendor Name with Address)
     */
    private String buildContractorInfo(PoHeader poHeader) {
        if (poHeader == null)
            return "";
        String vendorName = extractVendorName(poHeader.getVendorDetails());
        String vendorAddress = extractVendorAddress(poHeader.getVendorDetails());
        return vendorName + (vendorAddress.isBlank() ? "" : ", " + vendorAddress);
    }

    /**
     * Build Manufacturer Information
     * Name of Manufacturer of Steel Rounds / Supplier of Raw Material along with
     * city
     */
    private String buildManufacturerInfo(List<RmHeatQuantity> heatQuantities) {
        if (heatQuantities.isEmpty())
            return "";

        // Get unique manufacturers
        return heatQuantities.stream()
                .map(RmHeatQuantity::getManufacturer)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    /**
     * Build Contract Reference (PO Number & Date + Modification Advise)
     */
    private String buildContractRef(PoHeader poHeader, InspectionCall inspectionCall) {
        if (poHeader == null)
            return "";

        String rly = poHeader.getRlyShortName() != null ? poHeader.getRlyShortName() : "";
        String poNo = poHeader.getPoNo() != null ? poHeader.getPoNo() : "";
        String serial = (inspectionCall != null && inspectionCall.getPoSerialNo() != null)
                ? inspectionCall.getPoSerialNo()
                : "";

        // If serial already contains poNo (e.g., "60256836107122/020"), we just use
        // serial
        String basePoDetails;
        if (!serial.isEmpty() && serial.contains(poNo) && !poNo.isEmpty()) {
            basePoDetails = (rly.isEmpty() ? "" : rly + "/") + serial;
        } else {
            basePoDetails = (rly.isEmpty() ? "" : rly + "/") +
                    poNo +
                    (serial.isEmpty() ? "" : "/" + serial);
        }

        String dateStr = formatDate(poHeader.getPoDate() != null ? poHeader.getPoDate().toLocalDate() : null);

        return basePoDetails + " dated " + dateStr;
    }

    /**
     * Build Consignee Railway from PO Items
     */
    private String buildConsigneeRailway(InspectionCall inspectionCall, List<PoItem> poItems) {
        if (inspectionCall == null || poItems == null || poItems.isEmpty()) {
            return "";
        }

        try {
            String poSerialNo = inspectionCall.getPoSerialNo();
            String itemSrNo = poSerialNo;
            if (poSerialNo != null && poSerialNo.contains("/")) {
                String[] parts = poSerialNo.split("/");
                itemSrNo = parts[parts.length - 1].trim();
            }

            final String targetSrNo = itemSrNo;

            String result = poItems.stream()
                    .filter(item -> targetSrNo != null && targetSrNo.equals(item.getItemSrNo()))
                    .map(item -> item.getConsigneeDetail() != null ? item.getConsigneeDetail() : "")
                    .findFirst()
                    .orElse("");

            if (result.isBlank()) {
                result = poItems.get(0).getConsigneeDetail() != null ? poItems.get(0).getConsigneeDetail() : "";
            }

            return result;
        } catch (Exception e) {
            logger.warn("Error processing consignee railway for IC: {}", inspectionCall.getIcNumber(), e);
            return poItems.get(0).getConsigneeDetail() != null ? poItems.get(0).getConsigneeDetail() : "";
        }
    }

    /**
     * Build Consignee Manufacturer (Vendor with complete address)
     */
    private String buildConsigneeManufacturer(PoHeader poHeader) {
        if (poHeader == null)
            return "";
        String vendorName = extractVendorName(poHeader.getVendorDetails());
        String vendorAddress = extractVendorAddress(poHeader.getVendorDetails());
        return vendorName + (vendorAddress.isBlank() ? "" : ", " + vendorAddress);
    }

    /**
     * vendorDetails is persisted from CRIS field VENDOR_DETAILS.
     * Observed format in existing code: "VENDOR NAME-CITY~address~...".
     */
    private String extractVendorName(String vendorDetails) {
        if (vendorDetails == null || vendorDetails.isBlank())
            return "";
        String[] parts = vendorDetails.split("~");
        return parts.length > 0 ? parts[0].trim() : vendorDetails.trim();
    }

    private String extractVendorAddress(String vendorDetails) {
        if (vendorDetails == null || vendorDetails.isBlank())
            return "";
        String[] parts = vendorDetails.split("~");
        if (parts.length <= 1)
            return "";
        // Join remaining segments as a human-readable address/details string
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            String p = parts[i] != null ? parts[i].trim() : "";
            if (p.isEmpty())
                continue;
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(p);
        }
        return sb.toString();
    }

    /**
     * Build Description based on ERC Type
     */
    private String buildDescription(InspectionCall inspectionCall) {
        String ercType = inspectionCall.getErcType();
        if (ercType == null)
            return "";

        switch (ercType.toUpperCase()) {
            case "MK-III":
                return "55Si7 SPRING STEEL ROUND 20.64MM";
            case "MK-V":
                return "55Si7 SPRING STEEL ROUND 23MM";
            case "J TYPE CLIP":
                return "J TYPE CLIP";
            default:
                return ercType;
        }
    }

    /**
     * Build Item Description from PO Item list matching PO Sr No
     */
    private String buildItemDescription(InspectionCall inspectionCall, List<PoItem> poItems) {
        if (inspectionCall == null || poItems == null || poItems.isEmpty()) {
            return "";
        }
        try {
            String poSerialNo = inspectionCall.getPoSerialNo();
            if (poSerialNo == null || poSerialNo.isBlank()) {
                return poItems.get(0).getItemDesc() != null ? poItems.get(0).getItemDesc() : "";
            }

            String itemSrNo = poSerialNo.trim();
            if (itemSrNo.contains("/")) {
                String[] parts = itemSrNo.split("/");
                itemSrNo = parts[parts.length - 1].trim();
            }

            final String targetSrNo = itemSrNo;

            // 1. Exact match search
            for (PoItem item : poItems) {
                if (item.getItemSrNo() != null && item.getItemSrNo().trim().equals(targetSrNo)) {
                    return item.getItemDesc() != null ? item.getItemDesc() : "";
                }
            }

            // 2. Numeric match fallback (e.g. "001" vs "1")
            try {
                int targetInt = Integer.parseInt(targetSrNo);
                for (PoItem item : poItems) {
                    if (item.getItemSrNo() != null) {
                        try {
                            int itemInt = Integer.parseInt(item.getItemSrNo().trim());
                            if (targetInt == itemInt) {
                                return item.getItemDesc() != null ? item.getItemDesc() : "";
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }

            // 3. Fallback: If no match found, use the first item's description
            return poItems.get(0).getItemDesc() != null ? poItems.get(0).getItemDesc() : "";

        } catch (Exception e) {
            logger.warn("Error resolving item description for IC: {}", inspectionCall.getIcNumber(), e);
            return poItems.get(0).getItemDesc() != null ? poItems.get(0).getItemDesc() : "";
        }
    }

    /**
     * Build Result based on Heat Final Results
     */
    private String buildResult(List<RmHeatFinalResult> heatResults) {
        if (heatResults.isEmpty())
            return "";

        // Check if all heats are accepted
        boolean allAccepted = heatResults.stream()
                .allMatch(hr -> "ACCEPTED".equalsIgnoreCase(hr.getStatus()));

        if (allAccepted) {
            return "CONFIRMING TO THE SPECIFICATION IRS T–31-2025, GRADE 55SI7";
        } else {
            return "NOT CONFIRMING TO THE SPECIFICATION IRS T–31-2025, GRADE 55SI7";
        }
    }

    /**
     * Build Quantity Cleared (Heat No. / Qty (MT) + Total + No. of bundles + ERC
     * calculation)
     */
    /**
     * Build Quantity Cleared (Heat No. / Qty (MT) + Total)
     */
    private String buildQtyCleared(List<RmHeatFinalResult> heatResults) {
        if (heatResults.isEmpty())
            return "Total Qty - Nill";

        StringBuilder sb = new StringBuilder();
        double totalAccepted = 0.0;

        for (RmHeatFinalResult hr : heatResults) {
            if ("ACCEPTED".equalsIgnoreCase(hr.getStatus())) {
                BigDecimal acceptedQty = hr.getWeightAcceptedMt();
                double val = acceptedQty != null ? acceptedQty.doubleValue() : 0.0;
                sb.append(hr.getHeatNo()).append(" - ").append(String.format(java.util.Locale.US, "%.3f", val))
                        .append(" MT\n");
                totalAccepted += val;
            } else {
                sb.append(hr.getHeatNo()).append(" - Nill\n");
            }
        }

        if (totalAccepted > 0) {
            sb.append("Total Qty - ").append(String.format(java.util.Locale.US, "%.3f", totalAccepted)).append(" MT");
        } else {
            sb.append("Total Qty - Nill");
        }

        return sb.toString();
    }

    /**
     * Build Quantity Rejected
     */
    private String buildQtyRejected(List<RmHeatFinalResult> heatResults) {
        if (heatResults.isEmpty())
            return "Nil";

        StringBuilder sb = new StringBuilder();
        double totalRejected = 0.0;

        for (RmHeatFinalResult hr : heatResults) {
            if ("REJECTED".equalsIgnoreCase(hr.getStatus())) {
                BigDecimal rejectedQty = hr.getWeightRejectedMt();
                double val = rejectedQty != null ? rejectedQty.doubleValue() : 0.0;
                sb.append(hr.getHeatNo()).append(" - ").append(String.format(java.util.Locale.US, "%.3f", val))
                        .append(" MT\n");
                totalRejected += val;
            }
        }

        if (totalRejected > 0) {
            sb.append("Total Qty - ").append(String.format(java.util.Locale.US, "%.3f", totalRejected)).append(" MT");
            return sb.toString();
        }

        return "Nil";
    }

    /**
     * Build Remarks
     */
    private String buildRemarks(InspectionCall inspectionCall, List<RmHeatFinalResult> heatResults) {
        String ercType = inspectionCall.getErcType();
        if (ercType == null || ercType.isBlank()) {
            ercType = "MK-V";
        }
        ercType = ercType.toUpperCase().trim();

        long acceptedCount = heatResults.stream().filter(hr -> "ACCEPTED".equalsIgnoreCase(hr.getStatus())).count();
        long rejectedCount = heatResults.stream().filter(hr -> "REJECTED".equalsIgnoreCase(hr.getStatus())).count();

        if (acceptedCount > 0 && rejectedCount == 0) {
            return "LOT FOUND ACCEPTABLE AND CLEARED FOR MANUFACTURING OF ERC " + ercType + ".";
        } else if (acceptedCount == 0 && rejectedCount > 0) {
            return "LOT FOUND NOT ACCEPTABLE AND NOT CLEARED FOR MANUFACTURING OF ERC " + ercType;
        } else {
            return "LOT FOUND PARTIALLY ACCEPTABLE. ACCEPTED QUANTITY CLEARED FOR MANUFACTURING OF ERC " + ercType
                    + "; BALANCE QUANTITY REJECTED.";
        }
    }

    /**
     * Build Date of Call (Call Date + Desired Date)
     */
    private String buildDateOfCall(InspectionCall inspectionCall) {
        String callDate = formatDate(
                inspectionCall.getCreatedAt() != null ? inspectionCall.getCreatedAt().toLocalDate() : null);
        String desiredDate = formatDate(inspectionCall.getDesiredInspectionDate());
        return callDate + ", Desired Date: " + desiredDate;
    }

    /**
     * Get visit dates from workflow_transition starting from INSPECTION_INITIATION
     * to INSPECTION_COMPLETE_CONFIRM
     */
    private List<LocalDate> getVisitDates(String icNumber) {
        if (icNumber == null || icNumber.isEmpty())
            return new ArrayList<>();
        try {
            List<com.sarthi.entity.WorkflowTransition> transitions = workflowTransitionRepository
                    .findByRequestId(icNumber);
            if (transitions == null || transitions.isEmpty())
                return new ArrayList<>();

            transitions.sort(
                    java.util.Comparator.comparing(com.sarthi.entity.WorkflowTransition::getWorkflowTransitionId));

            java.util.Set<LocalDate> visitDates = new java.util.HashSet<>();
            boolean inspectionStarted = false;

            for (com.sarthi.entity.WorkflowTransition wt : transitions) {
                String status = wt.getStatus() != null ? wt.getStatus() : "";
                String action = wt.getAction() != null ? wt.getAction() : "";
                if ("INSPECTION_INITIATION".equalsIgnoreCase(status) || 
                    "INITIATE_INSPECTION".equalsIgnoreCase(action) || 
                    "INSPECTION_IN_PROGRESS".equalsIgnoreCase(status) ||
                    "ENTER_SHIFT_DETAILS_AND_START_INSPECTION".equalsIgnoreCase(status) ||
                    "ENTER_SHIFT_DETAILS_AND_START_INSPECTION".equalsIgnoreCase(action)) {
                    inspectionStarted = true;
                }

                if (inspectionStarted && wt.getCreatedDate() != null) {
                    LocalDate date = wt.getCreatedDate().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                    visitDates.add(date);
                }

                if ("INSPECTION_COMPLETE_CONFIRM".equalsIgnoreCase(status)
                        || "INSPECTION_COMPLETE_CONFIRM".equalsIgnoreCase(action)) {
                    break;
                }
            }

            return visitDates.stream().sorted().collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error calculating visit dates for IC: {}", icNumber, e);
            return new ArrayList<>();
        }
    }

    /**
     * Build Sealing Pattern
     */
    private String buildSealingPattern(List<RmHeatFinalResult> heatResults) {
        if (heatResults == null || heatResults.isEmpty()) {
            return "RITES HOLOGRAM FROM SL NO. C0000599 TO C0001604 HAS BEEN AFFIXED ON THE LEAD SEAL ,TIED WITH SEALING WIRE TO THE PACKING STRIP OF EACH CORRUGATED BOX";
        }

        java.util.Set<String> uniqueHolograms = new java.util.LinkedHashSet<>();
        java.util.Set<String> uniqueStamps = new java.util.LinkedHashSet<>();
        boolean hasHologram = false;
        boolean hasSteelPunch = false;

        for (RmHeatFinalResult hr : heatResults) {
            String sType = hr.getSealingType();
            if (sType != null) {
                if (sType.toUpperCase().contains("HOLOGRAM")) {
                    hasHologram = true;
                }
                if (sType.toUpperCase().contains("STEEL") || sType.toUpperCase().contains("PUNCH")) {
                    hasSteelPunch = true;
                }
            }

            String details = hr.getHologramDetails();
            if (details != null && !details.isEmpty()) {
                hasHologram = true;
                String[] entries = details.split(", ");
                for (String entry : entries) {
                    String cleaned = entry.replace("Range: ", "").replace("Single: ", "").trim();
                    if (!cleaned.isEmpty()) {
                        uniqueHolograms.add(cleaned.toUpperCase());
                    }
                }
            }

            String stamp = hr.getSteelStampNumber();
            if (stamp != null && !stamp.trim().isEmpty()) {
                hasSteelPunch = true;
                uniqueStamps.add(stamp.trim());
            }
        }

        if (hasHologram && hasSteelPunch) {
            String holoPart;
            if (!uniqueHolograms.isEmpty()) {
                holoPart = "RITES HOLOGRAM FROM SL. NO. " + String.join(", ", uniqueHolograms)
                        + " AFFIXED WITH TAPE ON LEAD SEAL OR ON TAG OF EACH BUNDLE";
            } else {
                holoPart = "RITES HOLOGRAM FROM SL NO. C0000599 TO C0001604 HAS BEEN AFFIXED ON THE LEAD SEAL ,TIED WITH SEALING WIRE TO THE PACKING STRIP OF EACH CORRUGATED BOX";
            }

            String stampPart;
            if (!uniqueStamps.isEmpty()) {
                stampPart = "RITES STEEL PUNCH WITH STAMP NUMBER(S) " + String.join(", ", uniqueStamps);
            } else {
                stampPart = "RITES STEEL PUNCH";
            }
            return holoPart + " AS WELL AS " + stampPart + " FOR SEALING OF MATERIAL.";
        } else if (hasSteelPunch) {
            if (!uniqueStamps.isEmpty()) {
                return "RITES STEEL PUNCH WITH STAMP NUMBER(S) " + String.join(", ", uniqueStamps)
                        + " FOR SEALING OF MATERIAL.";
            }
            return "RITES STEEL PUNCH FOR SEALING OF MATERIAL.";
        } else {
            if (!uniqueHolograms.isEmpty()) {
                String aggregatedDetails = String.join(", ", uniqueHolograms);
                return "RITES HOLOGRAM FROM SL. NO. " + aggregatedDetails
                        + " AFFIXED WITH TAPE ON LEAD SEAL OR ON TAG OF EACH BUNDLE.";
            }
            return "RITES HOLOGRAM FROM SL NO. C0000599 TO C0001604 HAS BEEN AFFIXED ON THE LEAD SEAL ,TIED WITH SEALING WIRE TO THE PACKING STRIP OF EACH CORRUGATED BOX";
        }
    }

    /**
     * Build Heat Details List
     * Combines data from RmHeatFinalResult (weights, status) and RmHeatQuantity
     * (manufacturer, TC info)
     */
    private List<RawMaterialCertificateDto.HeatDetailDto> buildHeatDetails(
            List<RmHeatQuantity> heatQuantities,
            List<RmHeatFinalResult> heatResults) {

        List<RawMaterialCertificateDto.HeatDetailDto> heatDetails = new ArrayList<>();

        // Create a map of heat number to heat quantity for quick lookup
        Map<String, RmHeatQuantity> heatQuantityMap = heatQuantities.stream()
                .collect(Collectors.toMap(
                        RmHeatQuantity::getHeatNumber,
                        hq -> hq,
                        (existing, replacement) -> existing // Keep first if duplicates
                ));

        for (RmHeatFinalResult hr : heatResults) {
            // Find matching heat quantity by heat number
            RmHeatQuantity hq = heatQuantityMap.get(hr.getHeatNo());

            RawMaterialCertificateDto.HeatDetailDto detail = RawMaterialCertificateDto.HeatDetailDto.builder()
                    .heatNo(hr.getHeatNo())
                    .manufacturer(hq != null ? hq.getManufacturer() : "")
                    .weightOfferedMt(hr.getWeightOfferedMt() != null ? hr.getWeightOfferedMt().toString() : "0")
                    .weightAcceptedMt(hr.getWeightAcceptedMt() != null ? hr.getWeightAcceptedMt().toString() : "0")
                    .weightRejectedMt(hr.getWeightRejectedMt() != null ? hr.getWeightRejectedMt().toString() : "0")
                    .status(hr.getStatus())
                    .tcNo(hq != null ? hq.getTcNumber() : "")
                    .tcDate(hq != null ? formatDate(hq.getTcDate()) : "")
                    .build();
            heatDetails.add(detail);
        }

        return heatDetails;
    }

    /**
     * Build Bill Paying Officer
     * Fetches from PoItem based on poNo and itemSrNo extracted from poSerialNo
     */
    private String buildBillPayingOfficer(InspectionCall inspectionCall, List<PoItem> poItems) {
        if (inspectionCall == null || poItems == null || poItems.isEmpty()) {
            return "";
        }

        try {
            // Extract itemSrNo from poSerialNo (e.g., "PO/001" -> "001")
            String poSerialNo = inspectionCall.getPoSerialNo();
            String itemSrNo = poSerialNo;
            if (poSerialNo != null && poSerialNo.contains("/")) {
                String[] parts = poSerialNo.split("/");
                itemSrNo = parts[parts.length - 1].trim();
            }

            final String targetSrNo = itemSrNo;

            // 1. Try to find a match by itemSrNo
            String result = poItems.stream()
                    .filter(item -> targetSrNo != null && targetSrNo.equals(item.getItemSrNo()))
                    .map(item -> item.getBillPayOffDesc() != null ? item.getBillPayOffDesc() : "")
                    .findFirst()
                    .orElse("");

            // 2. Fallback: If no match found, use the first item's description as a
            // reasonable default
            if (result.isBlank()) {
                result = poItems.get(0).getBillPayOffDesc() != null ? poItems.get(0).getBillPayOffDesc() : "";
            }

            return result;
        } catch (Exception e) {
            logger.warn("Error processing bill paying officer for IC: {}", inspectionCall.getIcNumber(), e);
            return poItems.get(0).getBillPayOffDesc() != null ? poItems.get(0).getBillPayOffDesc() : "";
        }
    }

    /**
     * Build Purchasing Authority
     * Priority: PO Header (purchaser_detail + purchaser_code) > Section A > Empty
     * Format: "PURCHASER_DETAIL (PURCHASER_CODE)"
     */
    private String buildPurchasingAuthority(PoHeader poHeader, MainPoInformation mainPoInfo) {
        // Try PO Header first
        if (poHeader != null) {
            String purchaserDetail = poHeader.getPurchaserDetail();
            String purchaserCode = poHeader.getPurchaserCode();

            if (purchaserDetail != null && !purchaserDetail.isEmpty()) {
                if (purchaserCode != null && !purchaserCode.isEmpty()) {
                    return purchaserDetail + " (" + purchaserCode + ")";
                }
                return purchaserDetail;
            }
        }

        // Fallback to Section A
        if (mainPoInfo != null && mainPoInfo.getPurchasingAuthority() != null) {
            return mainPoInfo.getPurchasingAuthority();
        }

        return "";
    }

    /**
     * Format LocalDate to dd/MM/yyyy
     */
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    private String formatDateRange(List<LocalDate> visitDates) {
        if (visitDates == null || visitDates.isEmpty()) {
            return "";
        }
        if (visitDates.size() == 1) {
            return formatDate(visitDates.get(0));
        }
        List<LocalDate> sorted = visitDates.stream().sorted().collect(Collectors.toList());
        return formatDate(sorted.get(0)) + " - " + formatDate(sorted.get(sorted.size() - 1));
    }

    /**
     * Helper to get IC Date (Creation Date or Edit Date)
     */
    private String getIcDate(String icNumber) {
        if (icNumber == null || icNumber.isBlank())
            return "";
        Optional<RmIcEdit> rmIcEditOpt = rmIcEditRepository.findByIcNumber(icNumber);
        if (rmIcEditOpt.isPresent() && rmIcEditOpt.get().getCreatedAt() != null) {
            return formatDate(rmIcEditOpt.get().getCreatedAt().toLocalDate());
        }
        Optional<InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(icNumber);
        if (callOpt.isPresent() && callOpt.get().getCreatedAt() != null) {
            return formatDate(callOpt.get().getCreatedAt().toLocalDate());
        }
        return "";
    }

    /*
     * ==================== PROCESS MATERIAL CERTIFICATE METHODS
     * ====================
     */

    @Override
    public ProcessMaterialCertificateDto generateProcessMaterialCertificate(String icNumber) {
        logger.info("Generating Process Material Certificate for IC Number: {}", icNumber);

        // 1. Fetch Inspection Call
        InspectionCall inspectionCall = inspectionCallRepository.findByIcNumber(icNumber)
                .orElseThrow(() -> new IllegalArgumentException("Process inspection call not found: " + icNumber));

        return buildProcessCertificateDto(inspectionCall);
    }

    @Override
    public ProcessMaterialCertificateDto generateProcessMaterialCertificateById(Long callId) {
        logger.info("Generating Process Material Certificate for Call ID: {}", callId);

        // 1. Fetch Inspection Call
        InspectionCall inspectionCall = inspectionCallRepository.findById(Math.toIntExact(callId))
                .orElseThrow(
                        () -> new IllegalArgumentException("Process inspection call not found with ID: " + callId));

        return buildProcessCertificateDto(inspectionCall);
    }

    /**
     * Build the complete Process Material certificate DTO from inspection call data
     */
    private ProcessMaterialCertificateDto buildProcessCertificateDto(InspectionCall inspectionCall) {
        logger.info("Building Process Material certificate DTO for IC: {}", inspectionCall.getIcNumber());

        // 2. Fetch Process Inspection Details (get first lot if multiple lots exist)
        processInspectionDetailsRepository.findByIcId(
                Long.valueOf(inspectionCall.getId()));

        // 3. Fetch PO Information
        PoHeader poHeader = poHeaderRepository.findByPoNo(inspectionCall.getPoNo()).orElse(null);
        List<PoItem> poItems = new ArrayList<>();
        if (poHeader != null) {
            poItems = poItemRepository.findByPoHeader_Id(poHeader.getId());
        }
        List<MainPoInformation> mainPoInfos = mainPoInformationRepository.findByPoNo(inspectionCall.getPoNo());
        MainPoInformation mainPoInfo = mainPoInfos.isEmpty() ? null : mainPoInfos.get(0);

        // 4. Fetch Inspection Complete Details (for certificate number)
        inspectionCompleteDetailsRepository
                .findByCallNo(inspectionCall.getIcNumber());

        // 5. Fetch Process Line Final Results (for lot details)
        List<ProcessLineFinalResult> lineFinalResults = processLineFinalResultRepository
                .findByInspectionCallNo(inspectionCall.getIcNumber());

        // 6. Build Lot Details
        List<ProcessMaterialCertificateDto.LotDetailDto> lots = buildProcessLotDetails(lineFinalResults);

        // 6. Load Sections Pre-calculated
        calculateOfferedInstallment(inspectionCall.getPoNo());
        calculatePassedInstallment(inspectionCall.getPoNo());

        List<LocalDate> visitDates = lineFinalResults.stream()
                .map(ProcessLineFinalResult::getDateOfInspection)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        // 7. Build Certificate DTO
        ProcessMaterialCertificateDto dto = ProcessMaterialCertificateDto.builder()
                .certificateNo(generateCertificateNumber(inspectionCall))
                .certificateDate(formatDate(LocalDate.now()))
                .offeredInstNo(calculateOfferedInstallment(inspectionCall.getPoNo()))
                .passedInstNo(calculatePassedInstallment(inspectionCall.getPoNo()))
                .contractor(buildContractorInfo(poHeader))
                .manufacturer(buildContractorInfo(poHeader))
                .placeOfInspection(buildPlaceOfInspection(inspectionCall))
                .contractRef(buildContractRef(poHeader, inspectionCall))
                .poDetails(inspectionCall.getPoNo() + " dated "
                        + (poHeader != null && poHeader.getPoDate() != null
                                ? formatDate(poHeader.getPoDate().toLocalDate())
                                : ""))
                .billPayingOfficer(buildBillPayingOfficer(inspectionCall, poItems))
                .consigneeRailway(buildConsigneeRailway(inspectionCall, poItems))
                .consigneeManufacturer(buildConsigneeManufacturer(poHeader))
                .purchasingAuthority(buildPurchasingAuthority(poHeader, mainPoInfo))
                .description(buildItemDescription(inspectionCall, poItems))
                .ercType(inspectionCall.getErcType())
                .drgNo(getDrgNoForErc(inspectionCall))
                .specNo("IRS T-31-2025")
                .qapNo("Clause No. of QAP")
                .chpClause("Clause No. of QAP")
                .inspectionType(
                        "Checking Length of cut bars/ Turning length/ MPI Test/  Checking of Die/ Quenching temperature & duration/ Quenching hardness/ Tempering temperature & duration/ Dimensional check/ Hardness of finished ERC/ Documentaion")
                .lots(lots)
                .reference(buildProcessReference(inspectionCall, lots))
                .dateOfCall(buildDateOfCall(inspectionCall))
                .inspectionDate(formatDateRange(visitDates))
                .manDays(visitDates.isEmpty() ? "" : String.valueOf(visitDates.size()))
                .noOfVisits(visitDates.isEmpty() ? "" : String.valueOf(visitDates.size()))
                .sealingPattern(buildProcessSealingPattern())
                .inspectingEngineer("") // Keep blank for now (DSC signature)
                .build();

        // Merge saved draft edits if available, else fallback to final edits
        Optional<ProcessIcSaveChanges> processIcSaveChangesOpt = processIcSaveChangesRepository
                .findByIcNumber(inspectionCall.getIcNumber());
        if (processIcSaveChangesOpt.isPresent()) {
            ProcessIcSaveChanges saveChanges = processIcSaveChangesOpt.get();
            if (saveChanges.getBookNo() != null && !saveChanges.getBookNo().isBlank()) {
                dto.setBookNo(saveChanges.getBookNo());
            }
            if (saveChanges.getSetNo() != null && !saveChanges.getSetNo().isBlank()) {
                dto.setSetNo(saveChanges.getSetNo());
            }
            if (saveChanges.getOfferedInstallmentNo() != null && !saveChanges.getOfferedInstallmentNo().isBlank()) {
                dto.setOfferedInstNo(saveChanges.getOfferedInstallmentNo());
            }
            if (saveChanges.getPassedInstallmentNo() != null && !saveChanges.getPassedInstallmentNo().isBlank()) {
                dto.setPassedInstNo(saveChanges.getPassedInstallmentNo());
            }
            if (saveChanges.getConsignee() != null && !saveChanges.getConsignee().isBlank()) {
                dto.setConsigneeRailway(saveChanges.getConsignee());
            }
            if (saveChanges.getContractRef() != null && !saveChanges.getContractRef().isBlank()) {
                dto.setContractRef(saveChanges.getContractRef());
            }
            if (saveChanges.getMaNumberAndDate() != null && !saveChanges.getMaNumberAndDate().isBlank()) {
                dto.setMaNumberAndDate(saveChanges.getMaNumberAndDate());
            }
            if (saveChanges.getBillPayingOfficer() != null && !saveChanges.getBillPayingOfficer().isBlank()) {
                dto.setBillPayingOfficer(saveChanges.getBillPayingOfficer());
            }
            if (saveChanges.getPurchasingAuthority() != null && !saveChanges.getPurchasingAuthority().isBlank()) {
                dto.setPurchasingAuthority(saveChanges.getPurchasingAuthority());
            }
            if (saveChanges.getDescription() != null && !saveChanges.getDescription().isBlank()) {
                dto.setDescription(saveChanges.getDescription());
            }
            if (saveChanges.getManufacturer() != null && !saveChanges.getManufacturer().isBlank()) {
                dto.setManufacturer(saveChanges.getManufacturer());
            }
            if (saveChanges.getQapNo() != null && !saveChanges.getQapNo().isBlank()) {
                dto.setQapNo(saveChanges.getQapNo());
                dto.setChpClause(saveChanges.getQapNo());
            }
        } else {
            Optional<ProcessIcEdit> processIcEditOpt = processIcEditRepository
                    .findByIcNumber(inspectionCall.getIcNumber());
            if (processIcEditOpt.isPresent()) {
                ProcessIcEdit processIcEdit = processIcEditOpt.get();
                if (processIcEdit.getBookNo() != null && !processIcEdit.getBookNo().isBlank()) {
                    dto.setBookNo(processIcEdit.getBookNo());
                }
                if (processIcEdit.getSetNo() != null && !processIcEdit.getSetNo().isBlank()) {
                    dto.setSetNo(processIcEdit.getSetNo());
                }
                if (processIcEdit.getOfferedInstallmentNo() != null
                        && !processIcEdit.getOfferedInstallmentNo().isBlank()) {
                    dto.setOfferedInstNo(processIcEdit.getOfferedInstallmentNo());
                }
                if (processIcEdit.getPassedInstallmentNo() != null
                        && !processIcEdit.getPassedInstallmentNo().isBlank()) {
                    dto.setPassedInstNo(processIcEdit.getPassedInstallmentNo());
                }
                if (processIcEdit.getConsignee() != null && !processIcEdit.getConsignee().isBlank()) {
                    dto.setConsigneeRailway(processIcEdit.getConsignee());
                }
                if (processIcEdit.getContractRef() != null && !processIcEdit.getContractRef().isBlank()) {
                    dto.setContractRef(processIcEdit.getContractRef());
                }
                if (processIcEdit.getMaNumberAndDate() != null && !processIcEdit.getMaNumberAndDate().isBlank()) {
                    dto.setMaNumberAndDate(processIcEdit.getMaNumberAndDate());
                }
                if (processIcEdit.getBillPayingOfficer() != null && !processIcEdit.getBillPayingOfficer().isBlank()) {
                    dto.setBillPayingOfficer(processIcEdit.getBillPayingOfficer());
                }
                if (processIcEdit.getPurchasingAuthority() != null
                        && !processIcEdit.getPurchasingAuthority().isBlank()) {
                    dto.setPurchasingAuthority(processIcEdit.getPurchasingAuthority());
                }
                if (processIcEdit.getDescription() != null && !processIcEdit.getDescription().isBlank()) {
                    dto.setDescription(processIcEdit.getDescription());
                }
                if (processIcEdit.getQapNo() != null && !processIcEdit.getQapNo().isBlank()) {
                    dto.setQapNo(processIcEdit.getQapNo());
                    dto.setChpClause(processIcEdit.getQapNo());
                }
            }
        }
        return dto;
    }

    /**
     * Build lot details from Process Line Final Results
     * Aggregates quantities by heatNo-lotNo combinations across all shifts and
     * lines.
     */
    private List<ProcessMaterialCertificateDto.LotDetailDto> buildProcessLotDetails(
            List<ProcessLineFinalResult> lineFinalResults) {

        // Use LinkedHashMap to preserve the order of lots as they appear in the results
        java.util.Map<String, ProcessMaterialCertificateDto.LotDetailDto> aggregatedLots = new java.util.LinkedHashMap<>();

        for (ProcessLineFinalResult result : lineFinalResults) {
            String heatNum = result.getHeatNumber();
            String lotNum = result.getLotNumber();
            String heatLot;

            // Construct unique heatLot key
            if (heatNum != null && !heatNum.isBlank()) {
                heatLot = heatNum;
                if (lotNum != null && !lotNum.isBlank() && !lotNum.equals(heatNum)) {
                    heatLot = heatLot + " - " + lotNum;
                }
            } else {
                heatLot = lotNum != null ? lotNum : "";
            }

            if (aggregatedLots.containsKey(heatLot)) {
                // Aggregate quantities for existing lot
                ProcessMaterialCertificateDto.LotDetailDto existingLot = aggregatedLots.get(heatLot);
                existingLot.setTotalProcessed(existingLot.getTotalProcessed()
                        + (result.getTotalManufactured() != null ? result.getTotalManufactured() : 0));
                existingLot.setAcceptedQty(existingLot.getAcceptedQty()
                        + (result.getTotalAccepted() != null ? result.getTotalAccepted() : 0));
                existingLot.setRejectedQty(existingLot.getRejectedQty()
                        + (result.getTotalRejected() != null ? result.getTotalRejected() : 0));
            } else {
                // Add new lot entry
                ProcessMaterialCertificateDto.LotDetailDto newLot = ProcessMaterialCertificateDto.LotDetailDto.builder()
                        .heatNo(heatLot)
                        .totalProcessed(result.getTotalManufactured() != null ? result.getTotalManufactured() : 0)
                        .acceptedQty(result.getTotalAccepted() != null ? result.getTotalAccepted() : 0)
                        .rejectedQty(result.getTotalRejected() != null ? result.getTotalRejected() : 0)
                        .build();
                aggregatedLots.put(heatLot, newLot);
            }
        }

        return new ArrayList<>(aggregatedLots.values());
    }

    /**
     * Build process description based on ERC type
     */
    private String buildProcessDescription(InspectionCall inspectionCall) {
        String ercType = inspectionCall.getErcType();
        if (ercType != null && !ercType.isBlank()) {
            // Normalize spacing and present as: PROCESS INSPECTION OF ELASTIC RAIL CLIP +
            // <ERC_TYPE>
            return "PROCESS INSPECTION OF ELASTIC RAIL CLIP  + " + ercType.trim();
        }
        return "PROCESS INSPECTION OF ELASTIC RAIL CLIP";
    }

    private static final String[] QTY_UNITS = {
            "", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN",
            "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"
    };

    private static final String[] QTY_TENS = {
            "", "", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY"
    };

    private String convertQuantityToWords(long n) {
        if (n <= 0)
            return "Zero";
        String rawWords = convertNumberToWordsInternal(n).trim();
        String[] parts = rawWords.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String buildFinalReasonsForRejection(int totalRejectedQty) {
        if (totalRejectedQty <= 0) {
            return "Not Applicable";
        }
        String words = convertQuantityToWords(totalRejectedQty);
        return String.format(
                "%s (%d) Nos. of ERC rejected due to dimensional non-conformity and/or visual surface defects such as deep dents, bends, cracks, or other specified defects and Dimension Inspection /Hardness Test/Decarburisation/ Freedom from defect /Micro-Structure/Application and Diflection Test/Toe Load Test .",
                words, totalRejectedQty);
    }

    private String convertNumberToWordsInternal(long n) {
        if (n < 20)
            return QTY_UNITS[(int) n];
        if (n < 100)
            return QTY_TENS[(int) (n / 10)] + ((n % 10 != 0) ? " " : "") + QTY_UNITS[(int) (n % 10)];
        if (n < 1000)
            return QTY_UNITS[(int) (n / 100)] + " HUNDRED" + ((n % 100 != 0) ? " " : "")
                    + convertNumberToWordsInternal(n % 100);
        if (n < 100000)
            return convertNumberToWordsInternal(n / 1000) + " THOUSAND" + ((n % 1000 != 0) ? " " : "")
                    + convertNumberToWordsInternal(n % 1000);
        if (n < 10000000)
            return convertNumberToWordsInternal(n / 100000) + " LAKH" + ((n % 100000 != 0) ? " " : "")
                    + convertNumberToWordsInternal(n % 100000);
        return convertNumberToWordsInternal(n / 10000000) + " CRORE" + ((n % 10000000 != 0) ? " " : "")
                + convertNumberToWordsInternal(n % 10000000);
    }

    private String fetchInvolvedIeNames(String icNumber) {
        if (icNumber == null || icNumber.isBlank())
            return "";
        try {
            List<com.sarthi.entity.WorkflowTransition> transitions = workflowTransitionRepository
                    .findByRequestIdOrderByWorkflowTransitionIdAsc(icNumber);
            if (transitions == null || transitions.isEmpty()) {
                return "";
            }

            java.util.Set<Integer> userIds = new java.util.LinkedHashSet<>();
            boolean inInspectionWindow = false;

            for (com.sarthi.entity.WorkflowTransition wt : transitions) {
                String status = wt.getStatus() != null ? wt.getStatus().toUpperCase() : "";

                // Start collecting from INSPECTION SCHEDULE status
                if (status.contains("SCHEDULE") || status.contains("INITIATE_INSPECTION")) {
                    inInspectionWindow = true;
                }

                // Collect modifiedBy user IDs while inside inspection window
                if (inInspectionWindow) {
                    if (wt.getModifiedBy() != null && wt.getModifiedBy() > 0) {
                        userIds.add(wt.getModifiedBy());
                    }
                }

                // End collecting after INSPECTION COMPLETE CONFIRM status
                if (status.contains("COMPLETE_CONFIRM") || status.contains("CONFIRM_INSPECTION")
                        || status.contains("INSPECTION_COMPLETE")) {
                    inInspectionWindow = false;
                }
            }

            // Fallback: If no userIds collected in window, fallback to all modifiedBy
            // entries
            if (userIds.isEmpty()) {
                for (com.sarthi.entity.WorkflowTransition wt : transitions) {
                    if (wt.getModifiedBy() != null && wt.getModifiedBy() > 0) {
                        userIds.add(wt.getModifiedBy());
                    }
                }
            }

            List<String> ieNames = new ArrayList<>();
            for (Integer uId : userIds) {
                Optional<com.sarthi.entity.UserMaster> uOpt = userMasterRepository.findById(uId);
                if (uOpt.isPresent()) {
                    com.sarthi.entity.UserMaster u = uOpt.get();
                    String role = u.getRoleName() != null ? u.getRoleName().toUpperCase() : "";
                    if (role.contains("IE") || role.contains("INSPECT")
                            || (!role.contains("VENDOR") && !role.contains("FIRM"))) {
                        String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName()
                                : u.getShortName();
                        if (name == null || name.isBlank()) {
                            name = u.getUsername();
                        }
                        if (name != null && !name.isBlank()) {
                            String formattedName = formatPersonName(name);
                            if (!ieNames.contains(formattedName)) {
                                ieNames.add(formattedName);
                            }
                        }
                    }
                }
            }

            return String.join(", ", ieNames);
        } catch (Exception e) {
            logger.error("Error fetching involved IE names for IC: {}", icNumber, e);
            return "";
        }
    }

    private String formatPersonName(String name) {
        String trimmed = name.trim();
        if (trimmed.toUpperCase().startsWith("MR.") || trimmed.toUpperCase().startsWith("MR ") ||
                trimmed.toUpperCase().startsWith("MS.") || trimmed.toUpperCase().startsWith("MS ") ||
                trimmed.toUpperCase().startsWith("DR.") || trimmed.toUpperCase().startsWith("DR ")) {
            return trimmed;
        }
        return "Mr " + trimmed;
    }

    /**
     * Build process reference
     */
    private String buildProcessReference(InspectionCall inspectionCall,
            List<ProcessMaterialCertificateDto.LotDetailDto> lots) {
        long totalAcceptedQty = 0;
        if (lots != null && !lots.isEmpty()) {
            for (ProcessMaterialCertificateDto.LotDetailDto lot : lots) {
                if (lot.getAcceptedQty() != null) {
                    totalAcceptedQty += lot.getAcceptedQty();
                }
            }
        }

        String qtyWords = convertQuantityToWords(totalAcceptedQty);

        String ieNames = fetchInvolvedIeNames(inspectionCall.getIcNumber());
        if (ieNames.isEmpty()) {
            ieNames = "RITES Process IE";
        }

        // Fetch Process Inspection Details to get RM IC numbers
        List<ProcessInspectionDetails> detailsList = processInspectionDetailsRepository.findByIcId(
                Long.valueOf(inspectionCall.getId()));

        List<String> rmIcNumbers = new ArrayList<>();
        if (detailsList != null) {
            rmIcNumbers = detailsList.stream()
                    .map(ProcessInspectionDetails::getRmIcNumber)
                    .filter(ic -> ic != null && !ic.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Quantity ").append(qtyWords)
                .append(" Nos. cleared for next/final stage after completion of process inspection as per PIO detailed under Annexure-A of Rly. Bd. Letter No. 2024/RS (G)/779/12 (E3482675) Dtd.06.01.2025 conducted by RITES Process IEs team (")
                .append(ieNames).append(")");

        if (!rmIcNumbers.isEmpty()) {
            sb.append(", Raw Material STAGE IC No. ");
            for (int i = 0; i < rmIcNumbers.size(); i++) {
                String rmIc = rmIcNumbers.get(i).trim();
                sb.append(rmIc);
                if (i < rmIcNumbers.size() - 1) {
                    sb.append(", ");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Return drawing number based on ERC type
     */
    private String getDrgNoForErc(InspectionCall inspectionCall) {
        String ercType = inspectionCall.getErcType();
        if (ercType == null)
            return "";
        if (ercType.toUpperCase().contains("MK-III") || ercType.toUpperCase().contains("MK III")) {
            return "RT-3701";
        }
        return "";
    }

    /**
     * Build process sealing pattern
     */
    private String buildProcessSealingPattern() {
        return "NA";
    }

    /*
     * ==================== FINAL MATERIAL CERTIFICATE METHODS ====================
     */

    @Override
    public FinalCertificateDto generateFinalCertificate(String icNumber) {
        logger.info("Generating Final Material Certificate for IC Number: {}", icNumber);

        // 1. Fetch Final Inspection Details with Call (Joined) - ELIMINATES N+1
        FinalInspectionDetails finalDetails = finalInspectionDetailsRepository.findByIcNumberWithCall(icNumber)
                .orElse(null);

        InspectionCall inspectionCall;
        if (finalDetails != null) {
            inspectionCall = finalDetails.getInspectionCall();
        } else {
            // Fallback if not found in final_inspection_details
            inspectionCall = inspectionCallRepository.findByIcNumber(icNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Final inspection call not found: " + icNumber));
        }

        return buildFinalCertificateDto(inspectionCall, finalDetails);
    }

    @Override
    public FinalCertificateDto generateFinalCertificateById(Long callId) {
        logger.info("Generating Final Material Certificate for Call ID: {}", callId);

        // 1. Fetch Inspection Call
        InspectionCall inspectionCall = inspectionCallRepository.findById(Math.toIntExact(callId))
                .orElseThrow(() -> new IllegalArgumentException("Inspection call not found with ID: " + callId));

        // 2. Fetch Final Details
        FinalInspectionDetails finalDetails = finalInspectionDetailsRepository.findByIcId(callId)
                .orElse(null);

        return buildFinalCertificateDto(inspectionCall, finalDetails);
    }

    /**
     * Build the complete final certificate DTO from inspection call data
     */
    private FinalCertificateDto buildFinalCertificateDto(InspectionCall inspectionCall,
            FinalInspectionDetails finalDetails) {
        logger.info("Building final certificate DTO for IC: {}", inspectionCall.getIcNumber());

        // 2. Fetch Final Inspection Lot Details
        List<FinalInspectionLotDetails> lotDetails = new ArrayList<>();
        if (finalDetails != null) {
            lotDetails = finalInspectionLotDetailsRepository.findByFinalDetailId(finalDetails.getId());
        }

        // 3. Fetch PO Header and Items
        PoHeader poHeader = poHeaderRepository.findByPoNoWithItems(inspectionCall.getPoNo()).orElse(null);
        List<PoItem> poItems = poHeader != null ? poHeader.getItems() : new ArrayList<>();

        // 4. Fetch Section A data (Main PO Information) for Bill Paying Officer /
        // Purchasing Authority fallback
        MainPoInformation mainPoInfo = mainPoInformationRepository
                .findByInspectionCallNo(inspectionCall.getIcNumber())
                .orElse(null);

        // 5. Load Sections Pre-calculated
        String poNo = inspectionCall.getPoNo();
        long startInit = System.currentTimeMillis();
        String offeredInst = calculateOfferedInstallment(poNo);
        String passedInst = calculatePassedInstallment(poNo);
        long endInit = System.currentTimeMillis();
        logger.info("Calculated installments for {} in {} ms", poNo, (endInit - startInit));

        // 6. Calculate Quantities
        final String cleanSerial;
        if (inspectionCall.getPoSerialNo() != null) {
            String tempSerial = inspectionCall.getPoSerialNo();
            if (tempSerial.contains("/")) {
                String[] parts = tempSerial.split("/");
                tempSerial = parts[parts.length - 1].trim();
            }
            cleanSerial = tempSerial;
        } else {
            cleanSerial = null;
        }

        Integer qtyOnOrder = 0;
        if (cleanSerial != null && !poItems.isEmpty()) {
            qtyOnOrder = poItems.stream()
                    .filter(item -> cleanSerial.equals(item.getItemSrNo()))
                    .map(item -> item.getQty() != null ? item.getQty().intValue() : 0)
                    .findFirst()
                    .orElse(0);
        }

        Long currentId = finalDetails != null ? finalDetails.getId() : 0L;
        String poSerialNo = inspectionCall.getPoSerialNo();

        long offeredPrev = 0L;
        long passedPrev = 0L;

        // Fetch from final_cumulative_results if available
        long cumStart = System.currentTimeMillis();
        FinalCumulativeResults cumulativeResults = finalCumulativeResultsRepository
                .findByInspectionCallNo(inspectionCall.getIcNumber()).orElse(null);
        long cumEnd = System.currentTimeMillis();
        logger.info("Fetched cumulative results for {} in {} ms", inspectionCall.getIcNumber(), (cumEnd - cumStart));

        int qtyNowOffered = finalDetails != null && finalDetails.getTotalOfferedQty() != null
                ? finalDetails.getTotalOfferedQty()
                : 0;
        int qtyNowPassed = finalDetails != null && finalDetails.getTotalAcceptedQty() != null
                ? finalDetails.getTotalAcceptedQty()
                : 0;
        int qtyNowRejected = finalDetails != null && finalDetails.getTotalRejectedQty() != null
                ? finalDetails.getTotalRejectedQty()
                : 0;

        if (cumulativeResults != null) {
            offeredPrev = cumulativeResults.getCummQtyOfferedPreviously() != null
                    ? cumulativeResults.getCummQtyOfferedPreviously()
                    : 0L;
            passedPrev = cumulativeResults.getCummQtyPassedPreviously() != null
                    ? cumulativeResults.getCummQtyPassedPreviously()
                    : 0L;
            qtyNowOffered = cumulativeResults.getQtyNowOffered() != null ? cumulativeResults.getQtyNowOffered()
                    : qtyNowOffered;
            qtyNowPassed = cumulativeResults.getQtyNowPassed() != null ? cumulativeResults.getQtyNowPassed()
                    : qtyNowPassed;
            qtyNowRejected = cumulativeResults.getQtyNowRejected() != null ? cumulativeResults.getQtyNowRejected()
                    : qtyNowRejected;
        } else if (poSerialNo != null && currentId > 0) {
            long qStart = System.currentTimeMillis();
            Long offeredPrevLong = finalInspectionDetailsRepository.sumOfferedQtyByPoSerialNoAndIdLessThan(poSerialNo,
                    currentId);
            Long passedPrevLong = finalInspectionDetailsRepository.sumAcceptedQtyByPoSerialNoAndIdLessThan(poSerialNo,
                    currentId);
            offeredPrev = offeredPrevLong != null ? offeredPrevLong : 0L;
            passedPrev = passedPrevLong != null ? passedPrevLong : 0L;
            long qEnd = System.currentTimeMillis();
            logger.info("Summed previous quantities for {} in {} ms", poSerialNo, (qEnd - qStart));
        }

        Integer totalErcUsed = finalInspectionLotResultsRepository.sumErcUsedForTestingByInspectionCallNo(inspectionCall.getIcNumber());
        int ercUsed = totalErcUsed != null ? totalErcUsed : 0;
        int qtyStillDue = qtyOnOrder - (int) passedPrev - (qtyNowPassed - ercUsed);
        qtyStillDue = Math.max(0, qtyStillDue);

        long start = System.currentTimeMillis();
        List<LocalDate> visitDates = getVisitDates(inspectionCall.getIcNumber());
        long end = System.currentTimeMillis();
        logger.info("Fetched visit dates for {} in {} ms", inspectionCall.getIcNumber(), (end - start));

        // Fetch RM and PM IC Details for remarks
        String rmIcNoStr = finalDetails != null && finalDetails.getRmIcNumber() != null ? finalDetails.getRmIcNumber()
                : "";
        String processIcNoStr = finalDetails != null && finalDetails.getProcessIcNumber() != null
                ? finalDetails.getProcessIcNumber()
                : "";
        String rmIcDateStr = "";
        String processIcDateStr = "";

        if (!rmIcNoStr.isBlank()) {
            String[] rmIcs = rmIcNoStr.split(",");
            if (rmIcs.length > 0) {
                rmIcDateStr = getIcDate(rmIcs[0].trim());
            }
        }
        if (!processIcNoStr.isBlank()) {
            String[] pmIcs = processIcNoStr.split(",");
            if (pmIcs.length > 0) {
                processIcDateStr = getIcDate(pmIcs[0].trim());
            }
        }

        // 9. Build Certificate DTO
        start = System.currentTimeMillis();

        String certNo = generateCertificateNumber(inspectionCall);
        String contractor = buildContractorInfo(poHeader);
        String placeOfInspection = buildFinalPlaceOfInspection(finalDetails);
        String sealingPattern = buildFinalSealingPattern(inspectionCall.getIcNumber());
        String remarks = buildFinalRemarks(finalDetails);
        Integer lotResultsRejectedSum = finalInspectionLotResultsRepository.sumTotalRejectedQtyByInspectionCallNo(inspectionCall.getIcNumber());
        int totalRejCount = (lotResultsRejectedSum != null && lotResultsRejectedSum > 0) ? lotResultsRejectedSum : qtyNowRejected;
        String reasonsForRejection = buildFinalReasonsForRejection(totalRejCount);

        FinalCertificateDto dto = FinalCertificateDto.builder()
                .certificateNo(certNo)
                .certificateDate(formatDate(LocalDate.now()))
                .offeredInstNo(offeredInst)
                .passedInstNo(passedInst)
                .contractor(contractor)
                .placeOfInspection(placeOfInspection)
                .contractRef(buildContractRef(poHeader, inspectionCall))
                .contractRefDate(poHeader != null && poHeader.getPoDate() != null
                        ? formatDate(poHeader.getPoDate().toLocalDate())
                        : "")
                .billPayingOfficer(buildBillPayingOfficer(inspectionCall, poItems))
                .consigneeRailway(buildConsigneeRailway(inspectionCall, poItems))
                .purchasingAuthority(buildPurchasingAuthority(poHeader, mainPoInfo))
                .itemNo(poItems.isEmpty() ? "" : poItems.get(0).getItemSrNo())
                .description(buildItemDescription(inspectionCall, poItems))
                .totalLots(
                        finalDetails != null && finalDetails.getTotalLots() != null ? finalDetails.getTotalLots() : 0)
                .qtyOnOrder(qtyOnOrder)
                .qtyOfferedPreviously((int) offeredPrev)
                .qtyPassedPreviously((int) passedPrev)
                .qtyNowOffered(qtyNowOffered)
                .qtyNowPassed(qtyNowPassed)
                .qtyNowRejected(qtyNowRejected)
                .qtyStillDue(qtyStillDue)
                .ercUsedForTesting(totalErcUsed != null ? totalErcUsed : 0)
                .remarks(remarks)
                .trRecDate("")
                .noOfItemsChecked("1")
                .dateOfCall(buildDateOfCall(inspectionCall))
                .noOfVisits(visitDates.isEmpty() ? "" : String.valueOf(visitDates.size()))
                .inspectionDates(formatDateRange(visitDates))
                .sealingPattern(sealingPattern)
                .quantityNowPassedText("")
                .reasonsForRejection(reasonsForRejection)
                .rmIcNo(rmIcNoStr)
                .rmIcDate(rmIcDateStr)
                .processIcNo(processIcNoStr)
                .processIcDate(processIcDateStr)
                .lotDetails(buildFinalLotDetails(lotDetails))
                .build();
        end = System.currentTimeMillis();
        logger.info("Built FinalCertificateDto for {} in {} ms", inspectionCall.getIcNumber(), (end - start));

        // Merge saved draft edits if available, else fallback to final edits
        Optional<FinalIcSaveChanges> finalIcSaveChangesOpt = finalIcSaveChangesRepository
                .findByIcNumber(inspectionCall.getIcNumber());
        if (finalIcSaveChangesOpt.isPresent()) {
            FinalIcSaveChanges saveChanges = finalIcSaveChangesOpt.get();
            if (saveChanges.getBookNo() != null && !saveChanges.getBookNo().isBlank()) {
                dto.setBookNo(saveChanges.getBookNo());
            }
            if (saveChanges.getSetNo() != null && !saveChanges.getSetNo().isBlank()) {
                dto.setSetNo(saveChanges.getSetNo());
            }
            if (saveChanges.getOfferedInstallmentNo() != null && !saveChanges.getOfferedInstallmentNo().isBlank()) {
                dto.setOfferedInstNo(saveChanges.getOfferedInstallmentNo());
            }
            if (saveChanges.getPassedInstallmentNo() != null && !saveChanges.getPassedInstallmentNo().isBlank()) {
                dto.setPassedInstNo(saveChanges.getPassedInstallmentNo());
            }
            if (saveChanges.getConsignee() != null && !saveChanges.getConsignee().isBlank()) {
                dto.setConsignee(saveChanges.getConsignee());
                dto.setConsigneeRailway(saveChanges.getConsignee());
            }
            if (saveChanges.getCummQtyOfferedPrev() != null) {
                try {
                    dto.setQtyOfferedPreviously(Integer.parseInt(saveChanges.getCummQtyOfferedPrev()));
                } catch (NumberFormatException ignored) {
                }
            }
            if (saveChanges.getQtyPrevPassed() != null) {
                try {
                    dto.setQtyPassedPreviously(Integer.parseInt(saveChanges.getQtyPrevPassed()));
                } catch (NumberFormatException ignored) {
                }
            }
            if (saveChanges.getQtyStillDue() != null) {
                try {
                    dto.setQtyStillDue(Integer.parseInt(saveChanges.getQtyStillDue()));
                } catch (NumberFormatException ignored) {
                }
            }
            if (saveChanges.getMaNumberAndDate() != null && !saveChanges.getMaNumberAndDate().isBlank()) {
                dto.setMaNumberAndDate(saveChanges.getMaNumberAndDate());
            }
            if (saveChanges.getPurchasingAuthority() != null && !saveChanges.getPurchasingAuthority().isBlank()) {
                dto.setPurchasingAuthority(saveChanges.getPurchasingAuthority());
            }
            if (saveChanges.getDescription() != null && !saveChanges.getDescription().isBlank()) {
                dto.setDescription(saveChanges.getDescription());
            }
            if (saveChanges.getTrRecDate() != null && !saveChanges.getTrRecDate().isBlank()) {
                dto.setTrRecDate(saveChanges.getTrRecDate());
            }
        } else {
            Optional<FinalIcEdit> finalIcEditOpt = finalIcEditRepository.findByIcNumber(inspectionCall.getIcNumber());
            if (finalIcEditOpt.isPresent()) {
                FinalIcEdit finalIcEdit = finalIcEditOpt.get();
                if (finalIcEdit.getBookNo() != null && !finalIcEdit.getBookNo().isBlank()) {
                    dto.setBookNo(finalIcEdit.getBookNo());
                }
                if (finalIcEdit.getSetNo() != null && !finalIcEdit.getSetNo().isBlank()) {
                    dto.setSetNo(finalIcEdit.getSetNo());
                }
                if (finalIcEdit.getOfferedInstallmentNo() != null && !finalIcEdit.getOfferedInstallmentNo().isBlank()) {
                    dto.setOfferedInstNo(finalIcEdit.getOfferedInstallmentNo());
                }
                if (finalIcEdit.getPassedInstallmentNo() != null && !finalIcEdit.getPassedInstallmentNo().isBlank()) {
                    dto.setPassedInstNo(finalIcEdit.getPassedInstallmentNo());
                }
                if (finalIcEdit.getConsignee() != null && !finalIcEdit.getConsignee().isBlank()) {
                    dto.setConsignee(finalIcEdit.getConsignee());
                    dto.setConsigneeRailway(finalIcEdit.getConsignee());
                }
                if (finalIcEdit.getCummQtyOfferedPrev() != null) {
                    try {
                        dto.setQtyOfferedPreviously(Integer.parseInt(finalIcEdit.getCummQtyOfferedPrev()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (finalIcEdit.getQtyPrevPassed() != null) {
                    try {
                        dto.setQtyPassedPreviously(Integer.parseInt(finalIcEdit.getQtyPrevPassed()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (finalIcEdit.getQtyStillDue() != null) {
                    try {
                        dto.setQtyStillDue(Integer.parseInt(finalIcEdit.getQtyStillDue()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (finalIcEdit.getMaNumberAndDate() != null && !finalIcEdit.getMaNumberAndDate().isBlank()) {
                    dto.setMaNumberAndDate(finalIcEdit.getMaNumberAndDate());
                }
                if (finalIcEdit.getPurchasingAuthority() != null && !finalIcEdit.getPurchasingAuthority().isBlank()) {
                    dto.setPurchasingAuthority(finalIcEdit.getPurchasingAuthority());
                }
                if (finalIcEdit.getDescription() != null && !finalIcEdit.getDescription().isBlank()) {
                    dto.setDescription(finalIcEdit.getDescription());
                }
                if (finalIcEdit.getTrRecDate() != null && !finalIcEdit.getTrRecDate().isBlank()) {
                    dto.setTrRecDate(finalIcEdit.getTrRecDate());
                }
            }
        }

        return dto;
    }

    /**
     * Build place of inspection for final certificate
     */
    private String buildFinalPlaceOfInspection(FinalInspectionDetails finalDetails) {
        if (finalDetails == null)
            return "";
        String unitName = finalDetails.getUnitName() != null ? finalDetails.getUnitName() : "";
        String unitAddress = finalDetails.getUnitAddress() != null ? finalDetails.getUnitAddress() : "";
        return unitName + (unitAddress.isBlank() ? "" : ", " + unitAddress);
    }

    /**
     * Build remarks for final certificate
     */
    private String buildFinalRemarks(FinalInspectionDetails finalDetails) {
        if (finalDetails == null)
            return "";
        // For now, return a generic remark based on acceptance status
        return "LOT FOUND ACCEPTABLE AND CLEARED FOR DELIVERY";
    }

    /**
     * Build lot details for final certificate
     */
    private List<FinalCertificateDto.LotDetailDto> buildFinalLotDetails(List<FinalInspectionLotDetails> lotDetails) {
        return lotDetails.stream()
                .map(lot -> FinalCertificateDto.LotDetailDto.builder()
                        .lotNo(lot.getLotNumber())
                        .heatNo(lot.getHeatNumber())
                        .manufacturer(lot.getManufacturer())
                        .offeredQty(lot.getOfferedQty() != null ? lot.getOfferedQty() : 0)
                        .acceptedQty(lot.getQtyAccepted() != null ? lot.getQtyAccepted() : 0)
                        .rejectedQty(lot.getQtyRejected() != null ? lot.getQtyRejected() : 0)
                        .status(lot.getQtyRejected() != null && lot.getQtyRejected() > 0 ? "PARTIAL" : "ACCEPTED")
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Build Sealing Pattern using Hologram Details from FinalInspectionLotResults
     */
    private String buildFinalSealingPattern(String icNumber) {
        try {
            List<FinalInspectionLotResults> lotResults = finalInspectionLotResultsRepository
                    .findByInspectionCallNo(icNumber);
            if (lotResults == null || lotResults.isEmpty()) {
                return "";
            }

            List<String> hologramStrings = new ArrayList<>();
            for (FinalInspectionLotResults lot : lotResults) {
                if (lot.getHologramDetails() != null && !lot.getHologramDetails().isEmpty()) {
                    List<Map<String, String>> details = objectMapper.readValue(
                            lot.getHologramDetails(),
                            new TypeReference<List<Map<String, String>>>() {
                            });

                    for (Map<String, String> entry : details) {
                        String type = entry.get("type");
                        if ("range".equalsIgnoreCase(type)) {
                            String from = entry.get("from");
                            String to = entry.get("to");
                            if (from != null && to != null) {
                                hologramStrings.add(from + " TO " + to);
                            }
                        } else if ("single".equalsIgnoreCase(type)) {
                            String value = entry.get("value");
                            if (value != null) {
                                hologramStrings.add(value);
                            }
                        }
                    }
                }
            }

            if (hologramStrings.isEmpty()) {
                return "";
            }

            String joinedHolograms = String.join(" & ", hologramStrings);
            return "RITES HOLOGRAM AFFIXED SL. NO. " + joinedHolograms +
                    " ON LEAD SEAL AT THE CENTRE OF KNOTLESS STITCH ON EACH BAG.";

        } catch (Exception e) {
            logger.error("Error building final sealing pattern for IC: {}", icNumber, e);
            return "";
        }
    }

    @Override
    public IcReportDataResponse generateReportData(Map<String, String> params) {
        String rawCaseNo = params.get("CaseNO");
        boolean isDigitallySign = Boolean.parseBoolean(params.get("isDigitallySign"));
        String type = params.get("type");

        logger.info("Generating REAL IC PDF for CaseNo: {}, Type: {}, SignMode: {}", rawCaseNo, type, isDigitallySign);

        try {
            byte[] pdfBytes;
            String pdfBase64Input = params.get("pdfBase64");

            if (pdfBase64Input != null && !pdfBase64Input.isEmpty()) {
                logger.info("Using frontend-provided PDF for E-Sign. Input size: {} chars", pdfBase64Input.length());
                pdfBytes = Base64.getDecoder().decode(pdfBase64Input);
            } else {
                // 1. Generate Unified IC PDF (Fallback)
                pdfBytes = generateICReport(params);
                logger.info("PDF generated successfully by backend. Size: {} bytes", pdfBytes.length);
            }

            // 2. Base64 Encode
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes).trim();

            if (isDigitallySign) {
                // --- MANDATORY VALIDATIONS ---
                if (base64Pdf.isEmpty()) {
                    throw new Exception("Generated PDF Base64 is empty");
                }
                // 1. Validate PDF Base64 (MANDATORY: must start with JVBER)
                if (base64Pdf == null || !base64Pdf.startsWith("JVBER")) {
                    throw new Exception("Invalid PDF data: Missing JVBER header.");
                }

                // 2. Mandatory Logging
                logger.info("PKI Payload - Length: {}", base64Pdf.length());
                logger.info("PKI Payload - First 20 chars: {}",
                        base64Pdf.substring(0, Math.min(20, base64Pdf.length())));

                // 3. Generate unique Transaction ID (Format: SARTHI{timestamp})
                String txnId = "SARTHI" + System.currentTimeMillis();

                // 4. Construct signing XML (MANDATORY: PDF_SIGN | data | Single Line | No XML
                // Declaration)
                String responseText = "<request><command>PDF_SIGN</command><txn>" + txnId + "</txn><data>" + base64Pdf
                        + "</data><sigX>375</sigX><sigY>190</sigY><sigPage>1</sigPage></request>";

                logger.info(
                        "PKI XML Request (Masked): <request><command>PDF_SIGN</command><txn>{}</txn><data>[{} bytes]</data><sigX>375</sigX><sigY>190</sigY><sigPage>1</sigPage></request>",
                        txnId, base64Pdf.length());

                return IcReportDataResponse.builder()
                        .status("1")
                        .isDigitalSignatureConfig(true)
                        .responseText(responseText)
                        .build();
            } else {
                // Return raw Base64 for viewing
                return IcReportDataResponse.builder()
                        .status("1")
                        .isDigitalSignatureConfig(false)
                        .responseText(base64Pdf)
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error generating unified IC PDF", e);
            return IcReportDataResponse.builder()
                    .status("0")
                    .responseText("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Unified entry point for generating the real Inspection Certificate PDF.
     * Routes to specific DTO generation and then to the PDF builder.
     */
    private byte[] generateICReport(Map<String, String> params) throws Exception {
        String rawCaseNo = params.get("CaseNO");
        String type = params.get("type"); // RM, PM, FM

        // Sanitize CaseNO to extract internal IC Number (e.g. EP-02260001)
        String icNumber = extractIcNumber(rawCaseNo);
        logger.info("Extracted IC Number: '{}' from raw CaseNO: '{}'", icNumber, rawCaseNo);

        Object dto;
        if ("RM".equalsIgnoreCase(type)) {
            dto = generateRawMaterialCertificate(icNumber);
        } else if ("PM".equalsIgnoreCase(type)) {
            dto = generateProcessMaterialCertificate(icNumber);
        } else if ("FM".equalsIgnoreCase(type)) {
            dto = generateFinalCertificate(icNumber);
        } else {
            throw new IllegalArgumentException("Unknown certificate type: " + type);
        }

        return buildPdfFromDto(dto, type);
    }

    /**
     * Universal Pixel-Perfect PDF Engine.
     * Replicates the exact grid structures for RM, PM, and FM certificate types.
     */
    private byte[] buildPdfFromDto(Object dto, String type) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Standard RITES Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font tinyBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font tinyNormal = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 9);

        // --- SECTION 1: COMMON HEADER ---
        addCommonHeader(document, dto, titleFont, boldFont, tinyBold, smallFont);

        // --- SECTION 2: COMMON CERTIFICATE INFO ---
        addCertificateInfoRow(document, dto, boldFont, tinyBold);

        // --- SECTION 3: TYPE-SPECIFIC BODY ---
        if ("RM".equalsIgnoreCase(type)) {
            buildRmLayout(document, dto, normalFont, boldFont, tinyBold, smallFont, italicFont);
        } else if ("PM".equalsIgnoreCase(type)) {
            buildPmLayout(document, dto, normalFont, boldFont, tinyBold, smallFont, italicFont);
        } else if ("FM".equalsIgnoreCase(type)) {
            buildFmLayout(document, dto, normalFont, boldFont, tinyBold, smallFont, italicFont);
        }

        document.close();
        return baos.toByteArray();
    }

    private void addCommonHeader(Document document, Object dto, Font titleFont, Font boldFont, Font tinyBold,
            Font smallFont) throws Exception {
        PdfPTable outerHeader = new PdfPTable(1);
        outerHeader.setWidthPercentage(100);

        PdfPTable bookSetBox = new PdfPTable(2);
        bookSetBox.setTotalWidth(150f);
        bookSetBox.setLockedWidth(true);
        bookSetBox.setHorizontalAlignment(Element.ALIGN_CENTER);

        bookSetBox.addCell(createLabelValueCell("बुक सं. Book No.", getDtoValue(dto, "bookNo"), boldFont, tinyBold));
        bookSetBox.addCell(createLabelValueCell("सेट सं. Set No.", getDtoValue(dto, "setNo"), boldFont, tinyBold));

        PdfPCell boxWrapper = new PdfPCell(bookSetBox);
        boxWrapper.setBorder(Rectangle.NO_BORDER);
        boxWrapper.setPaddingTop(32f);
        boxWrapper.setPaddingBottom(12f);
        outerHeader.addCell(boxWrapper);
        document.add(outerHeader);

        PdfPTable branding = new PdfPTable(3);
        branding.setWidthPercentage(100);
        branding.setWidths(new float[] { 1, 3, 1.5f });

        branding.addCell(createEmptyCell());
        PdfPCell rTitle = new PdfPCell(new Phrase("RITES LTD, NORTHERN REGION, DELHI", titleFont));
        rTitle.setBorder(Rectangle.NO_BORDER);
        rTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        branding.addCell(rTitle);

        PdfPCell contCell = new PdfPCell();
        contCell.addElement(new Phrase("निरंतरता पत्रक शामिल", tinyBold));
        contCell.addElement(new Phrase("Contains 0 Continuation Sheets", tinyBold));
        contCell.setBorder(Rectangle.NO_BORDER);
        contCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        branding.addCell(contCell);
        document.add(branding);
        document.add(new Paragraph("\n"));
    }

    private void addCertificateInfoRow(Document document, Object dto, Font boldFont, Font tinyBold) throws Exception {
        PdfPTable certInfoRow = new PdfPTable(1);
        certInfoRow.setWidthPercentage(100);

        PdfPTable certInfoBox = new PdfPTable(3);
        certInfoBox.setWidthPercentage(75);
        certInfoBox.setHorizontalAlignment(Element.ALIGN_RIGHT);
        certInfoBox.setWidths(new float[] { 1.8f, 1f, 2.7f });

        certInfoBox.addCell(createLabelValueCell("प्रमाणपत्र पत्र सं. Certificate No.",
                getDtoValue(dto, "certificateNo").toUpperCase(), boldFont, tinyBold));
        certInfoBox
                .addCell(createLabelValueCell("दिनांक Date", getDtoValue(dto, "certificateDate"), boldFont, tinyBold));

        PdfPCell instCell = new PdfPCell();
        instCell.setPadding(3);
        instCell.addElement(
                new Phrase("प्रस्तावित किस्त सं. Offered Instt. No. " + getDtoValue(dto, "offeredInstNo"), tinyBold));
        instCell.addElement(
                new Phrase("किस्त स. पारित Passed Instt. No. " + getDtoValue(dto, "passedInstNo"), tinyBold));
        certInfoBox.addCell(instCell);

        PdfPCell certWrapper = new PdfPCell(certInfoBox);
        certWrapper.setBorder(Rectangle.NO_BORDER);
        certWrapper.setPaddingBottom(5);
        certInfoRow.addCell(certWrapper);
        document.add(certInfoRow);
    }

    private void buildPmLayout(Document document, Object dto, Font normalFont, Font boldFont, Font tinyBold,
            Font smallFont, Font italicFont) throws Exception {
        PdfPTable mainGrid = new PdfPTable(1);
        mainGrid.setWidthPercentage(100);

        mainGrid.addCell(createTwoColRow("ठेकेदार / Contractor", getDtoValue(dto, "contractor"),
                "उत्पादक / Manufacturer", getDtoValue(dto, "manufacturer"), normalFont, tinyBold));

        // Contract Ref Row
        PdfPTable rowCB = new PdfPTable(2);
        PdfPCell cRefCell = new PdfPCell();
        cRefCell.addElement(new Phrase("संविदा संदर्भ एवं दिनांक (रेलवे) / Contract Ref. & Date (Rly.)", tinyBold));
        cRefCell.addElement(new Phrase(getDtoValue(dto, "contractRef"), normalFont));
        cRefCell.addElement(new Phrase("खरीद आदेश सं. एवं दिनांक (ठेकेदार) / PO No. & Date (Contractor)", tinyBold));
        cRefCell.addElement(new Phrase(getDtoValue(dto, "poDetails"), normalFont));
        rowCB.addCell(cRefCell);
        rowCB.addCell(createLabelValueCell("बिल अदायगी अधिकारी / Bill Paying Officer",
                getDtoValue(dto, "billPayingOfficer"), normalFont, tinyBold));
        mainGrid.addCell(rowCB);

        mainGrid.addCell(
                createThreeColRow("विवरण / Description", getDtoValue(dto, "description"), "ड्रॉइंग सं. / Drg. No.",
                        getDtoValue(dto, "drgNo"), "Spec No.", getDtoValue(dto, "specNo"), normalFont, tinyBold));
        mainGrid.addCell(createLabelValueCell("किए गए निरीक्षण/परीक्षण विवरण / Type of inspection/tests conducted",
                getDtoValue(dto, "inspectionType"), normalFont, tinyBold));
        document.add(mainGrid);

        // Body Table
        PdfPTable lotTable = new PdfPTable(5);
        lotTable.setWidthPercentage(100);
        lotTable.setWidths(new float[] { 2, 1, 1, 1, 1 });
        addTableHeader(lotTable,
                new String[] { "CHP CL. NO.", "HEAT No. / Lot No.", "Total Nos.", "Accepted Nos.", "Rejected Nos." },
                tinyBold);

        java.util.List<?> lots = (java.util.List<?>) getDtoObject(dto, "lots");
        if (lots != null) {
            for (Object lot : lots) {
                lotTable.addCell(new Phrase(getDtoValue(dto, "chpClause"), smallFont));
                lotTable.addCell(new Phrase(getDtoValue(lot, "heatNo"), smallFont));
                lotTable.addCell(new Phrase(getDtoValue(lot, "totalProcessed"), smallFont));
                lotTable.addCell(new Phrase(getDtoValue(lot, "acceptedQty"), smallFont));
                lotTable.addCell(new Phrase(getDtoValue(lot, "rejectedQty"), smallFont));
            }
        }
        document.add(lotTable);

        // Footer
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        footer.addCell(createLabelValueCell("संदर्भ / Reference", getDtoValue(dto, "reference"), normalFont, tinyBold));

        PdfPTable sigRow = new PdfPTable(2);
        sigRow.addCell(createLabelValueCell("सील/स्टैंपिंग Seal Pattern", getDtoValue(dto, "sealingPattern"),
                normalFont, tinyBold));
        sigRow.addCell(createSignatureCell("Inspecting Engineer", boldFont));
        footer.addCell(sigRow);

        addFinalCertification(footer, "It is certified that Process Inspection of ERCs carried out satisfactorily.",
                italicFont, smallFont);
        document.add(footer);
    }

    private void buildRmLayout(Document document, Object dto, Font normalFont, Font boldFont, Font tinyBold,
            Font smallFont, Font italicFont) throws Exception {
        PdfPTable mainGrid = new PdfPTable(1);
        mainGrid.setWidthPercentage(100);

        mainGrid.addCell(createTwoColRow("ठेकेदार / Contractor", getDtoValue(dto, "contractor"),
                "उत्पादक / Manufacturer & Place",
                getDtoValue(dto, "manufacturer") + "\n" + getDtoValue(dto, "placeOfInspection"), normalFont, tinyBold));
        mainGrid.addCell(createLabelValueCell("निरीक्षण का प्रकार / Type of inspection",
                getDtoValue(dto, "inspectionType"), normalFont, tinyBold));
        document.add(mainGrid);

        // CHP Table (6 Columns)
        PdfPTable chpTable = new PdfPTable(6);
        chpTable.setWidthPercentage(100);
        chpTable.setWidths(new float[] { 1.2f, 1f, 1.2f, 0.8f, 1f, 0.8f });
        addTableHeader(chpTable,
                new String[] { "CHP CL. NO.", "Requirement", "Details", "Result", "Cleared Qty", "Rejected Qty" },
                tinyBold);

        chpTable.addCell(createNestedCell(getDtoValue(dto, "chpClause"), smallFont));
        chpTable.addCell(createNestedCell(getDtoValue(dto, "contractChpReq"), smallFont));
        chpTable.addCell(createNestedCell(getDtoValue(dto, "inspectionDetails"), smallFont));
        chpTable.addCell(createNestedCell(getDtoValue(dto, "result"), smallFont));
        chpTable.addCell(createNestedCell(getDtoValue(dto, "clearedQty"), smallFont));
        chpTable.addCell(createNestedCell(getDtoValue(dto, "qtyRejected"), smallFont));
        document.add(chpTable);

        // Footer with 3-column signature
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);

        PdfPTable sigRow = new PdfPTable(3);
        sigRow.addCell(createLabelValueCell("Seal Pattern", getDtoValue(dto, "sealingPattern"), normalFont, tinyBold));
        sigRow.addCell(
                createLabelValueCell("Facsimile of seal", getDtoValue(dto, "sealFacsimile"), normalFont, tinyBold));
        sigRow.addCell(createSignatureCell("Inspecting Engineer", boldFont));
        footer.addCell(sigRow);

        addFinalCertification(footer, "It is certified that material is cleared for the next stage.", italicFont,
                smallFont);
        document.add(footer);
    }

    private void buildFmLayout(Document document, Object dto, Font normalFont, Font boldFont, Font tinyBold,
            Font smallFont, Font italicFont) throws Exception {
        PdfPTable mainGrid = new PdfPTable(1);
        mainGrid.setWidthPercentage(100);

        mainGrid.addCell(createTwoColRow("Contractor", getDtoValue(dto, "contractor"), "Place of Inspection",
                getDtoValue(dto, "placeOfInspection"), normalFont, tinyBold));
        mainGrid.addCell(createTwoColRow("Consignee", getDtoValue(dto, "consigneeRailway"), "Purchasing Authority",
                getDtoValue(dto, "purchasingAuthority"), normalFont, tinyBold));
        document.add(mainGrid);

        // Store Details (9 Columns)
        PdfPTable storeTable = new PdfPTable(9);
        storeTable.setWidthPercentage(100);
        storeTable.setWidths(new float[] { 0.5f, 2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f });

        addTableHeader(storeTable, new String[] { "Item No.", "Description", "Order Qty", "Prev Offd", "Prev Pass",
                "Now Offd", "Now Pass", "Now Rej", "Still Due" }, tinyBold);

        storeTable.addCell(createNestedCell(getDtoValue(dto, "itemNo"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "description"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "qtyOnOrder"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "qtyOfferedPreviously"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "qtyPassedPreviously"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "qtyNowOffered"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "qtyNowPassed"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "qtyNowRejected"), smallFont));
        storeTable.addCell(createNestedCell(getDtoValue(dto, "qtyStillDue"), smallFont));
        document.add(storeTable);

        // Quantity in Words
        PdfPTable wordsTable = new PdfPTable(1);
        wordsTable.setWidthPercentage(100);
        wordsTable.addCell(createLabelValueCell("QUANTITY NOW PASSED IN WORDS:",
                getDtoValue(dto, "quantityNowPassedText"), italicFont, tinyBold));
        document.add(wordsTable);

        // Inspection Grid (5 Columns)
        PdfPTable grid = new PdfPTable(5);
        grid.setWidthPercentage(100);
        grid.addCell(createLabelValueCell("No. Checked", getDtoValue(dto, "noOfItemsChecked"), normalFont, tinyBold));
        grid.addCell(createLabelValueCell("Date of Call", getDtoValue(dto, "dateOfCall"), normalFont, tinyBold));
        grid.addCell(createLabelValueCell("No. of Visits", getDtoValue(dto, "noOfVisits"), normalFont, tinyBold));
        grid.addCell(
                createLabelValueCell("Dates of Insp", getDtoValue(dto, "datesOfInspection"), normalFont, tinyBold));
        grid.addCell(createLabelValueCell("TR Rec. Dt", getDtoValue(dto, "trRecDate"), normalFont, tinyBold));
        document.add(grid);

        // Signature
        PdfPTable sigRow = new PdfPTable(3);
        sigRow.addCell(createLabelValueCell("Seal Pattern", getDtoValue(dto, "sealingPattern"), normalFont, tinyBold));
        sigRow.addCell(
                createLabelValueCell("Facsimile of seal", getDtoValue(dto, "facsimileText"), normalFont, tinyBold));
        sigRow.addCell(createSignatureCell("Inspecting Engineer", boldFont));
        document.add(sigRow);

        document.add(createLabelValueCell("Reasons for Rejection", getDtoValue(dto, "reasonsForRejection"), normalFont,
                tinyBold));
    }

    // --- HELPER METHODS ---

    private PdfPCell createLabelValueCell(String label, String value, Font font, Font labelFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(3);
        cell.addElement(new Phrase(label, labelFont));
        cell.addElement(new Phrase(value != null ? value : "", font));
        return cell;
    }

    private PdfPCell createTwoColRow(String l1, String v1, String l2, String v2, Font font, Font lFont) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(createLabelValueCell(l1, v1, font, lFont));
        table.addCell(createLabelValueCell(l2, v2, font, lFont));
        PdfPCell wrap = new PdfPCell(table);
        wrap.setPadding(0);
        return wrap;
    }

    private PdfPCell createThreeColRow(String l1, String v1, String l2, String v2, String l3, String v3, Font font,
            Font lFont) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.addCell(createLabelValueCell(l1, v1, font, lFont));
        table.addCell(createLabelValueCell(l2, v2, font, lFont));
        table.addCell(createLabelValueCell(l3, v3, font, lFont));
        PdfPCell wrap = new PdfPCell(table);
        wrap.setPadding(0);
        return wrap;
    }

    private void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, font));
            c.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c);
        }
    }

    private PdfPCell createSignatureCell(String label, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(80f);
        cell.addElement(new Phrase(label, font));
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    private void addFinalCertification(PdfPTable table, String text, Font itFont, Font smFont) {
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.addElement(new Phrase(text, itFont));
        cell.addElement(new Phrase(
                "Distribution: Manufacturer Office copy, RITES Bill Copy, Contractor, Purchaser (Railway)", smFont));
        table.addCell(cell);
    }

    private PdfPCell createNestedCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setPadding(3);
        return c;
    }

    private PdfPCell createEmptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private Object getDtoObject(Object dto, String fieldName) {
        try {
            java.lang.reflect.Method method = dto.getClass()
                    .getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            return method.invoke(dto);
        } catch (Exception e) {
            return null;
        }
    }

    private String getDtoValue(Object dto, String fieldName) {
        try {
            java.lang.reflect.Method method = dto.getClass()
                    .getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            Object result = method.invoke(dto);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extracts the internal IC Number (e.g., EP-01060001) from a decorated
     * certificate number (e.g., W/EP-01060001/nitish).
     */
    private String extractIcNumber(String raw) {
        if (raw == null || raw.isEmpty())
            return "";

        // Regex to find the pattern: [ER/EP/EF]-[8 digits]
        Pattern pattern = Pattern.compile("(E[RPF]-\\d{8})");
        Matcher matcher = pattern.matcher(raw);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Fallback to original string if no pattern found (standard IC format)
        return raw;
    }
}
