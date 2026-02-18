package com.sarthi.service.certificate.impl;

import com.sarthi.dto.certificate.RawMaterialCertificateDto;
import com.sarthi.dto.certificate.ProcessMaterialCertificateDto;
import com.sarthi.dto.certificate.FinalCertificateDto;
import com.sarthi.entity.InspectionCompleteDetails;
import com.sarthi.entity.MainPoInformation;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.entity.RmHeatFinalResult;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import com.sarthi.repository.InspectionCompleteDetailsRepository;
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
import com.sarthi.repository.finalmaterial.FinalInspectionDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.service.certificate.CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;

    @Autowired
    private ProcessInspectionDetailsRepository processInspectionDetailsRepository;

    @Autowired
    private ProcessLineFinalResultRepository processLineFinalResultRepository;

    @Autowired
    private ProcessRmIcMappingRepository processRmIcMappingRepository;

    @Autowired
    private FinalInspectionDetailsRepository finalInspectionDetailsRepository;

    @Autowired
    private FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;

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
        String poNo = inspectionCall.getPoNo();
        String offeredInst = calculateOfferedInstallment(poNo);
        String passedInst = calculatePassedInstallment(poNo);

        // 8. Build Certificate DTO
        return RawMaterialCertificateDto.builder()
                .certificateNo(generateCertificateNumber(inspectionCall))
                .certificateDate(formatDate(LocalDate.now()))
                .offeredInstNo(offeredInst)
                .passedInstNo(passedInst)
                .contractor(buildContractorInfo(poHeader))
                .manufacturer(buildManufacturerInfo(heatQuantities))
                .placeOfInspection(buildPlaceOfInspection(inspectionCall))
                .contractRef(buildContractRef(poHeader))
                .contractorPo(buildContractorPo(rmDetails))
                .billPayingOfficer(buildBillPayingOfficer(inspectionCall, poItems))
                .consigneeRailway(buildConsigneeRailway(poItems))
                .consigneeManufacturer(buildConsigneeManufacturer(poHeader))
                .purchasingAuthority(buildPurchasingAuthority(poHeader, mainPoInfo))
                .description(buildDescription(inspectionCall))
                .drgNo("") // Keep blank
                .specNo("IRS T-31-2025")
                .qapNo("Clause No.4.11.2 & 4.11.3 of Indian Railway Standard Specification for Elastic Rail Clip, IRS T-31-2025")
                .inspectionType("Visual/Physical/Chemical/Metallurgical/Dimensional")
                .chpClause("Clause No.4.11.2 & 4.11.3 of Indian Railway Standard Specification for Elastic Rail Clip, IRS T-31-2025")
                .contractChpReq("Visual, Dimensional, Mechanical & Chemical")
                .detailsOfInspection("Visual, Dimensional, Mechanical & Chemical")
                .result(buildResult(heatResults))
                .qtyCleared(buildQtyCleared(heatResults))
                .qtyRejected(buildQtyRejected(heatResults))
                .remarks(buildRemarks(inspectionCall))
                .dateOfCall(buildDateOfCall(inspectionCall))
                .noOfVisits("") // Keep blank for now
                .dateOfInspection(buildDateOfInspection(heatResults))
                .sealingPattern(buildSealingPattern())
                .sealFacsimile("") // Blank for stamp
                .inspectingEngineer("") // Keep blank for now (DSC signature)
                .heatDetails(buildHeatDetails(heatQuantities, heatResults))
                .build();
    }

    /* ==================== Helper Methods for Building Certificate Fields ==================== */

    /**
     * Build Place of Inspection
     * Format: Company Name + Unit Address
     */
    private String buildPlaceOfInspection(InspectionCall inspectionCall) {
        String companyName = inspectionCall.getCompanyName() != null ? inspectionCall.getCompanyName() : "";
        String unitAddress = inspectionCall.getUnitAddress() != null ? inspectionCall.getUnitAddress() : "";
        return companyName + (unitAddress.isBlank() ? "" : ", " + unitAddress);
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
        return String.valueOf(inspectionCallRepository.countByPoNo(poNo));
    }

    /**
     * Calculate Passed Installment Number
     * Count of accepted ICs for this PO
     */
    private String calculatePassedInstallment(String poNo) {
        return String.valueOf(inspectionCallRepository.countByPoNoAndStatusIn(poNo, List.of("ACCEPTED", "COMPLETED")));
    }

    /**
     * Build Contractor Information (Vendor Name with Address)
     */
    private String buildContractorInfo(PoHeader poHeader) {
        if (poHeader == null) return "";
		String vendorName = extractVendorName(poHeader.getVendorDetails());
		String vendorAddress = extractVendorAddress(poHeader.getVendorDetails());
		return vendorName + (vendorAddress.isBlank() ? "" : ", " + vendorAddress);
    }

    /**
     * Build Manufacturer Information
     * Name of Manufacturer of Steel Rounds / Supplier of Raw Material along with city
     */
    private String buildManufacturerInfo(List<RmHeatQuantity> heatQuantities) {
        if (heatQuantities.isEmpty()) return "";

        // Get unique manufacturers
        return heatQuantities.stream()
                .map(RmHeatQuantity::getManufacturer)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    /**
     * Build Contract Reference (PO Number & Date + Modification Advise)
     */
    private String buildContractRef(PoHeader poHeader) {
        if (poHeader == null) return "";
		return poHeader.getPoNo() + " dated " + formatDate(poHeader.getPoDate() != null ? poHeader.getPoDate().toLocalDate() : null);
    }

    /**
     * Build Contractor's PO Number & Date
     */
    private String buildContractorPo(RmInspectionDetails rmDetails) {
        if (rmDetails == null) return "";
        return (rmDetails.getSubPoNumber() != null ? rmDetails.getSubPoNumber() : "") +
               " dated " +
               (rmDetails.getSubPoDate() != null ? formatDate(rmDetails.getSubPoDate()) : "");
    }

    /**
     * Build Consignee Railway from PO Items
     */
    private String buildConsigneeRailway(List<PoItem> poItems) {
        if (poItems.isEmpty()) return "";

        // Get consignee from first item (assuming same for all items in PO)
        PoItem firstItem = poItems.get(0);
        return firstItem.getConsigneeDetail() != null ? firstItem.getConsigneeDetail() : "";
    }

    /**
     * Build Consignee Manufacturer (Vendor with complete address)
     */
    private String buildConsigneeManufacturer(PoHeader poHeader) {
        if (poHeader == null) return "";
		String vendorName = extractVendorName(poHeader.getVendorDetails());
		String vendorAddress = extractVendorAddress(poHeader.getVendorDetails());
		return vendorName + (vendorAddress.isBlank() ? "" : ", " + vendorAddress);
    }

	/**
	 * vendorDetails is persisted from CRIS field VENDOR_DETAILS.
	 * Observed format in existing code: "VENDOR NAME-CITY~address~...".
	 */
	private String extractVendorName(String vendorDetails) {
		if (vendorDetails == null || vendorDetails.isBlank()) return "";
		String[] parts = vendorDetails.split("~");
		return parts.length > 0 ? parts[0].trim() : vendorDetails.trim();
	}

	private String extractVendorAddress(String vendorDetails) {
		if (vendorDetails == null || vendorDetails.isBlank()) return "";
		String[] parts = vendorDetails.split("~");
		if (parts.length <= 1) return "";
		// Join remaining segments as a human-readable address/details string
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < parts.length; i++) {
			String p = parts[i] != null ? parts[i].trim() : "";
			if (p.isEmpty()) continue;
			if (sb.length() > 0) sb.append(", ");
			sb.append(p);
		}
		return sb.toString();
	}

    /**
     * Build Description based on ERC Type
     */
    private String buildDescription(InspectionCall inspectionCall) {
        String ercType = inspectionCall.getErcType();
        if (ercType == null) return "";

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
     * Build Result based on Heat Final Results
     */
    private String buildResult(List<RmHeatFinalResult> heatResults) {
        if (heatResults.isEmpty()) return "";

        // Check if all heats are accepted (fully confirmed)
        boolean allAccepted = heatResults.stream()
                .allMatch(hr -> "ACCEPTED".equalsIgnoreCase(hr.getStatus()));

        if (allAccepted) {
            return "CONFIRMING TO THE SPECIFICATION IRS T–31-2025, GRADE 55SI7";
        } else {
            // Check if any results were found at all (if non-pending exist, it means it's processed)
            boolean processed = heatResults.stream()
                    .anyMatch(hr -> List.of("ACCEPTED", "REJECTED", "PARTIALLY_ACCEPTED").contains(hr.getStatus()));
            
            if (processed) {
                return "NOT CONFIRMING TO THE SPECIFICATION IRS T–31-2025, GRADE 55SI7";
            }
            return "";
        }
    }

    /**
     * Build Quantity Cleared (Heat No. / Qty (MT) + Total + No. of bundles + ERC calculation)
     */
    private String buildQtyCleared(List<RmHeatFinalResult> heatResults) {
        if (heatResults.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        double totalAccepted = 0.0;

        for (RmHeatFinalResult hr : heatResults) {
            // Include both fully ACCEPTED and PARTIALLY_ACCEPTED heats
            if ("ACCEPTED".equalsIgnoreCase(hr.getStatus()) || "PARTIALLY_ACCEPTED".equalsIgnoreCase(hr.getStatus())) {
                BigDecimal acceptedQty = hr.getWeightAcceptedMt();
                if (acceptedQty != null && acceptedQty.doubleValue() > 0) {
                    sb.append(hr.getHeatNo()).append(" / ").append(acceptedQty).append(" MT\n");
                    totalAccepted += acceptedQty.doubleValue();
                }
            }
        }

        sb.append("Total: ").append(totalAccepted).append(" MT");
        return sb.toString();
    }

    /**
     * Build Quantity Rejected
     */
    private String buildQtyRejected(List<RmHeatFinalResult> heatResults) {
        if (heatResults.isEmpty()) return "Nil";

        StringBuilder sb = new StringBuilder();
        double totalRejected = 0.0;

        for (RmHeatFinalResult hr : heatResults) {
            // Include both fully REJECTED and PARTIALLY_ACCEPTED heats
            if ("REJECTED".equalsIgnoreCase(hr.getStatus()) || "PARTIALLY_ACCEPTED".equalsIgnoreCase(hr.getStatus())) {
                BigDecimal rejectedQty = hr.getWeightRejectedMt();
                if (rejectedQty != null && rejectedQty.doubleValue() > 0) {
                    sb.append(hr.getHeatNo()).append(" / ").append(rejectedQty).append(" MT\n");
                    totalRejected += rejectedQty.doubleValue();
                }
            }
        }

        return totalRejected > 0 ? sb.toString() : "Nil";
    }

    /**
     * Build Remarks
     */
    private String buildRemarks(InspectionCall inspectionCall) {
        String ercType = inspectionCall.getErcType();
        if (ercType == null) return "";
        return "LOT FOUND ACCEPTABLE AND CLEARED FOR MANUFACTURING OF ERC " + ercType.toUpperCase();
    }

    /**
     * Build Date of Call (Call Date + Desired Date)
     */
    private String buildDateOfCall(InspectionCall inspectionCall) {
        String callDate = formatDate(inspectionCall.getCreatedAt() != null ?
                inspectionCall.getCreatedAt().toLocalDate() : null);
        String desiredDate = formatDate(inspectionCall.getDesiredInspectionDate());
        return "Call Date: " + callDate + ", Desired Date: " + desiredDate;
    }

    /**
     * Build Sealing Pattern
     */
    private String buildSealingPattern() {
        return "RITES HOLOGRAM FROM SL. NO. W-XXXXXXX TO W-XXXXXXX AFFIXED WITH TAPE ON LEAD SEAL OR ON TAG OF EACH BUNDLE.";
    }

    /**
     * Build Heat Details List
     * Combines data from RmHeatFinalResult (weights, status) and RmHeatQuantity (manufacturer, TC info)
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
        if (inspectionCall == null || inspectionCall.getPoSerialNo() == null || poItems == null) {
            return "";
        }

        try {
            // Extract itemSrNo from poSerialNo (e.g., "PO/001" -> "001")
            String poSerialNo = inspectionCall.getPoSerialNo();
            String itemSrNo = poSerialNo;
            if (poSerialNo.contains("/")) {
                String[] parts = poSerialNo.split("/");
                itemSrNo = parts[parts.length - 1].trim();
            }

            final String targetSrNo = itemSrNo;
            // Use pre-fetched items to avoid extra DB call
            return poItems.stream()
                    .filter(item -> targetSrNo.equals(item.getItemSrNo()))
                    .map(item -> item.getBillPayOffDesc() != null ? item.getBillPayOffDesc() : "")
                    .findFirst()
                    .orElse("");
        } catch (Exception e) {
            logger.warn("Error processing bill paying officer for IC: {}", inspectionCall.getIcNumber(), e);
            return "";
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
     * Build Date of Inspection
     * Use created_at from rm_heat_final_result table (earliest date if multiple heats)
     */
    private String buildDateOfInspection(List<RmHeatFinalResult> heatResults) {
        if (heatResults == null || heatResults.isEmpty()) {
            return "";
        }

        // Find the earliest created_at from heat results
        LocalDate earliestDate = heatResults.stream()
                .map(RmHeatFinalResult::getCreatedAt)
                .filter(dateTime -> dateTime != null)
                .map(LocalDateTime::toLocalDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        return formatDate(earliestDate);
    }

    /**
     * Format LocalDate to dd/MM/yyyy
     */
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /* ==================== PROCESS MATERIAL CERTIFICATE METHODS ==================== */

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
                .orElseThrow(() -> new IllegalArgumentException("Process inspection call not found with ID: " + callId));

        return buildProcessCertificateDto(inspectionCall);
    }

    /**
     * Build the complete Process Material certificate DTO from inspection call data
     */
    private ProcessMaterialCertificateDto buildProcessCertificateDto(InspectionCall inspectionCall) {
        logger.info("Building Process Material certificate DTO for IC: {}", inspectionCall.getIcNumber());

        // 2. Fetch Process Inspection Details (get first lot if multiple lots exist)
        List<ProcessInspectionDetails> processDetailsList = processInspectionDetailsRepository.findByIcId(
                Long.valueOf(inspectionCall.getId()));
        ProcessInspectionDetails processDetails = processDetailsList.isEmpty() ? null : processDetailsList.get(0);

        // 3. Fetch PO Information
        PoHeader poHeader = poHeaderRepository.findByPoNo(inspectionCall.getPoNo()).orElse(null);
        List<PoItem> poItems = new ArrayList<>();
        if (poHeader != null) {
            poItems = poItemRepository.findByPoHeader_Id(poHeader.getId());
        }
        PoItem poItem = poItems.isEmpty() ? null : poItems.get(0);
        List<MainPoInformation> mainPoInfos = mainPoInformationRepository.findByPoNo(inspectionCall.getPoNo());
        MainPoInformation mainPoInfo = mainPoInfos.isEmpty() ? null : mainPoInfos.get(0);

        // 4. Fetch Inspection Complete Details (for certificate number)
        InspectionCompleteDetails completeDetails = inspectionCompleteDetailsRepository
                .findByCallNo(inspectionCall.getIcNumber())
                .orElse(null);

        // 5. Fetch Process Line Final Results (for lot details)
        List<ProcessLineFinalResult> lineFinalResults = processLineFinalResultRepository
                .findByInspectionCallNo(inspectionCall.getIcNumber());

        // 6. Build Lot Details
        List<ProcessMaterialCertificateDto.LotDetailDto> lots = buildProcessLotDetails(lineFinalResults);

        // 6. Load Sections Pre-calculated
        String poNo = inspectionCall.getPoNo();
        String offeredInst = calculateOfferedInstallment(poNo);
        String passedInst = calculatePassedInstallment(poNo);

        // 7. Build Certificate DTO
        return ProcessMaterialCertificateDto.builder()
                .certificateNo(generateCertificateNumber(inspectionCall))
                .certificateDate(formatDate(LocalDate.now()))
                .offeredInstNo(offeredInst)
                .passedInstNo(passedInst)
                .contractor(inspectionCall.getCompanyName() != null ? inspectionCall.getCompanyName() : "")
                .manufacturer(processDetails != null ? processDetails.getManufacturer() : "")
                .contractRef(buildContractRef(poHeader))
                .poDetails(buildPoDetails(poHeader))
                .billPayingOfficer(buildBillPayingOfficer(inspectionCall, poItems))
                .consigneeRailway(poItem != null ? poItem.getConsigneeDetail() : "")
                .consigneeManufacturer(inspectionCall.getCompanyName() != null ? inspectionCall.getCompanyName() : "")
                .purchasingAuthority("")
                .description(buildProcessDescription(inspectionCall))
                .drgNo(getDrgNoForErc(inspectionCall))
                .specNo("IRS T-31-2025")
                .qapNo("RAILWAY BOARD's Letter No. 2024/RS(G)/779/12(E3482675),\nDtd. 06.01.2025")
                .inspectionType("Checking Length of Cut Bars/ Turning Length/ MPI Test/Checking of Die/Quenching Temp. & Duration/Quenching Hardness/ Tempering Temperature & Duration/Dimensional Check/Hardness of Finished ERC/ Documentation")
                .ercType(inspectionCall.getErcType())
                .chpClause(buildProcessDescription(inspectionCall))
                .lots(lots)
                .reference(buildProcessReference(inspectionCall))
                .callDate(formatDate(inspectionCall.getDesiredInspectionDate()))
                .inspectionDate(buildInspectionDateRange(inspectionCall))
                .manDays("")
                .sealingPattern(buildProcessSealingPattern())
                .inspectingEngineer("")
                .build();
    }

    /**
     * Build lot details from Process Line Final Results
     */
    private List<ProcessMaterialCertificateDto.LotDetailDto> buildProcessLotDetails(
            List<ProcessLineFinalResult> lineFinalResults) {

        List<ProcessMaterialCertificateDto.LotDetailDto> lots = new ArrayList<>();

        for (ProcessLineFinalResult result : lineFinalResults) {
            String heatNum = result.getHeatNumber();
            String lotNum = result.getLotNumber();
            String heatLot;
            if (heatNum != null && !heatNum.isBlank()) {
                heatLot = heatNum;
                if (lotNum != null && !lotNum.isBlank() && !lotNum.equals(heatNum)) {
                    heatLot = heatLot + " - " + lotNum;
                }
            } else {
                heatLot = lotNum != null ? lotNum : "";
            }

            ProcessMaterialCertificateDto.LotDetailDto lot = ProcessMaterialCertificateDto.LotDetailDto.builder()
                    .heatNo(heatLot)
                    .totalProcessed(result.getTotalManufactured() != null ? result.getTotalManufactured() : 0)
                    .acceptedQty(result.getTotalAccepted() != null ? result.getTotalAccepted() : 0)
                    .rejectedQty(result.getTotalRejected() != null ? result.getTotalRejected() : 0)
                    .build();
            lots.add(lot);
        }

        return lots;
    }

    /**
     * Build process description based on ERC type
     */
    private String buildProcessDescription(InspectionCall inspectionCall) {
        String ercType = inspectionCall.getErcType();
        if (ercType != null && !ercType.isBlank()) {
            // Normalize spacing and present as: PROCESS INSPECTION OF ELASTIC RAIL CLIP + <ERC_TYPE>
            return "PROCESS INSPECTION OF ELASTIC RAIL CLIP  + " + ercType.trim();
        }
        return "PROCESS INSPECTION OF ELASTIC RAIL CLIP";
    }

    /**
     * Build process reference
     */
    private String buildProcessReference(InspectionCall inspectionCall) {
        // Try to gather RM IC mappings for this process call
        List<ProcessRmIcMapping> mappings = processRmIcMappingRepository.findByProcessIcId(Long.valueOf(inspectionCall.getId()));

        if (mappings == null || mappings.isEmpty()) {
            return "Call No: " + inspectionCall.getIcNumber();
        }

        // Use the process call's createdAt as the date to display for all RM IC entries
        LocalDate processCallDate = inspectionCall.getCreatedAt() != null ? inspectionCall.getCreatedAt().toLocalDate() : null;
        String processDateStr = formatDate(processCallDate);

        StringBuilder sb = new StringBuilder();
        sb.append("Raw Material STAGE IC No. ");

        for (int i = 0; i < mappings.size(); i++) {
            ProcessRmIcMapping m = mappings.get(i);
            String rmIc = m.getRmIcNumber() != null ? m.getRmIcNumber() : "";

            sb.append(rmIc);
            if (!processDateStr.isBlank()) sb.append(", Dt.").append(processDateStr);

            if (m.getBookSetNo() != null && !m.getBookSetNo().isBlank()) {
                sb.append(" (Book/Set No - ").append(m.getBookSetNo()).append(")");
            }

            if (i < mappings.size() - 1) sb.append("; ");
        }

        return sb.toString();
    }

    /**
     * Return drawing number based on ERC type
     */
    private String getDrgNoForErc(InspectionCall inspectionCall) {
        String ercType = inspectionCall.getErcType();
        if (ercType == null) return "";
        if (ercType.toUpperCase().contains("MK-III") || ercType.toUpperCase().contains("MK III")) {
            return "RT-3701";
        }
        return "";
    }

    /**
     * Build PO details for process certificate
     */
    private String buildPoDetails(PoHeader poHeader) {
        if (poHeader == null) return "";
        return poHeader.getPoNo() + " dated " + formatDate(poHeader.getPoDate() != null ? poHeader.getPoDate().toLocalDate() : null);
    }

    /**
     * Build inspection date range for process certificate
     */
    private String buildInspectionDateRange(InspectionCall inspectionCall) {
        // For now, return the desired inspection date
        return formatDate(inspectionCall.getDesiredInspectionDate());
    }

    /**
     * Build process sealing pattern
     */
    private String buildProcessSealingPattern() {
        return "IT IS TO CERTIFY THAT 01 (ONE) RITES IE IS ENGAGED PER SHIFT PER LINE FOR PROCESS INSPECTION OF ERCs AT FIRM PREMISES.";
    }

    /* ==================== FINAL MATERIAL CERTIFICATE METHODS ==================== */

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
    private FinalCertificateDto buildFinalCertificateDto(InspectionCall inspectionCall, FinalInspectionDetails finalDetails) {
        logger.info("Building final certificate DTO for IC: {}", inspectionCall.getIcNumber());

        // 2. Fetch Final Inspection Lot Details
        List<FinalInspectionLotDetails> lotDetails = new ArrayList<>();
        if (finalDetails != null) {
            lotDetails = finalInspectionLotDetailsRepository.findByFinalDetailId(finalDetails.getId());
        }

        // 3. Fetch PO Header and Items
        PoHeader poHeader = poHeaderRepository.findByPoNo(inspectionCall.getPoNo()).orElse(null);
        List<PoItem> poItems = new ArrayList<>();
        if (poHeader != null) {
            poItems = poItemRepository.findByPoHeader_Id(poHeader.getId());
        }

        // 4. Fetch Section A data (Main PO Information) for Bill Paying Officer / Purchasing Authority fallback
        MainPoInformation mainPoInfo = mainPoInformationRepository
                .findByInspectionCallNo(inspectionCall.getIcNumber())
                .orElse(null);

        // 5. Load Sections Pre-calculated
        String poNo = inspectionCall.getPoNo();
        String offeredInst = calculateOfferedInstallment(poNo);
        String passedInst = calculatePassedInstallment(poNo);

        // 6. Build Certificate DTO
        return FinalCertificateDto.builder()
                .certificateNo(generateCertificateNumber(inspectionCall))
                .certificateDate(formatDate(LocalDate.now()))
                .offeredInstNo(offeredInst)
                .passedInstNo(passedInst)
                .contractor(buildContractorInfo(poHeader))
                .placeOfInspection(buildFinalPlaceOfInspection(finalDetails))
                .contractRef(poHeader != null ? poHeader.getPoNo() : "")
                .contractRefDate(poHeader != null && poHeader.getPoDate() != null ? formatDate(poHeader.getPoDate().toLocalDate()) : "")
                .billPayingOfficer(buildBillPayingOfficer(inspectionCall, poItems))
                .consigneeRailway(buildConsigneeRailway(poItems))
                .purchasingAuthority(buildPurchasingAuthority(poHeader, mainPoInfo))
                .itemNo(poItems.isEmpty() ? "" : poItems.get(0).getItemSrNo())
                .description(buildDescription(inspectionCall))
                .totalLots(finalDetails != null && finalDetails.getTotalLots() != null ? finalDetails.getTotalLots() : 0)
                .totalOfferedQty(finalDetails != null && finalDetails.getTotalOfferedQty() != null ? finalDetails.getTotalOfferedQty() : 0)
                .totalAcceptedQty(finalDetails != null && finalDetails.getTotalAcceptedQty() != null ? finalDetails.getTotalAcceptedQty() : 0)
                .totalRejectedQty(finalDetails != null && finalDetails.getTotalRejectedQty() != null ? finalDetails.getTotalRejectedQty() : 0)
                .remarks(buildFinalRemarks(finalDetails))
                .trRecDate("")
                .quantityNowPassedText("")
                .lotDetails(buildFinalLotDetails(lotDetails))
                .build();
    }

    /**
     * Build place of inspection for final certificate
     */
    private String buildFinalPlaceOfInspection(FinalInspectionDetails finalDetails) {
        if (finalDetails == null) return "";
        String unitName = finalDetails.getUnitName() != null ? finalDetails.getUnitName() : "";
        String unitAddress = finalDetails.getUnitAddress() != null ? finalDetails.getUnitAddress() : "";
        return unitName + (unitAddress.isBlank() ? "" : ", " + unitAddress);
    }

    /**
     * Build remarks for final certificate
     */
    private String buildFinalRemarks(FinalInspectionDetails finalDetails) {
        if (finalDetails == null) return "";
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
}

