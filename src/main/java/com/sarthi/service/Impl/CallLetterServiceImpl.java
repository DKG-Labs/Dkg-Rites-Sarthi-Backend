package com.sarthi.service.Impl;

import com.sarthi.dto.CallLetterDetailsDto;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.RmHeatFinalResult;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoItemRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.service.CallLetterService;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of CallLetterService.
 * Joins inspection_calls + po_header + po_item + type-specific detail tables
 * to produce the enriched DTO consumed by the PDF generator.
 */
@Service
public class CallLetterServiceImpl implements CallLetterService {

    private static final Logger logger = LoggerFactory.getLogger(CallLetterServiceImpl.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static class DefectDetail {
        final String reason;
        final Long moduleId;
        DefectDetail(String reason, Long moduleId) {
            this.reason = reason;
            this.moduleId = moduleId;
        }
    }

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private PoItemRepository poItemRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private RmHeatFinalResultRepository rmHeatFinalResultRepository;

    @Autowired
    private FinalCumulativeResultsRepository finalCumulativeResultsRepository;

    @Autowired
    private RmHeatQuantityRepository rmHeatQuantityRepository;

    @Autowired
    private com.sarthi.repository.WorkflowTransitionRepository workflowTransitionRepository;

    @Autowired
    private com.sarthi.repository.PincodePoIMappingRepository pincodePoIMappingRepository;

    @Autowired
    private com.sarthi.repository.PoiProcessIeMappingRepository poiProcessIeMappingRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository sleeperInspectionCallRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.VendorPlantRepository vendorPlantRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository sleeperPoiIeMappingRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.SleeperWorkflowRepository sleeperWorkflowRepository;

    @Autowired
    private com.sarthi.repository.VendorMasterRepository vendorMasterRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository productionDeclarationRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionSleeperRepository productionSleeperRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.EtSleeperDetailsRepository etSleeperDetailsRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.FinalInspectionRepository.InspectionTestResultRepository inspectionTestResultRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.ModulusOfFailureRepository modulusOfFailureRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.MfTestDetailsRepository mfTestDetailsRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public CallLetterDetailsDto getCallLetterDetails(String requestId) {
        logger.info("Fetching call letter details for requestId: {}", requestId);

        CallLetterDetailsDto dto = new CallLetterDetailsDto();
        dto.setRequestId(requestId);

        // -------------------------------------------------------
        // 1. Fetch the core inspection call row
        // -------------------------------------------------------
        Optional<InspectionCall> icOpt = inspectionCallRepository.findFirstByIcNumber(requestId);
        if (icOpt.isEmpty()) {
            // Check if it is a Sleeper Inspection Call
            Optional<com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall> sleeperOpt = sleeperInspectionCallRepository.findByCallNoWithBatches(requestId);
            if (sleeperOpt.isEmpty()) {
                sleeperOpt = sleeperInspectionCallRepository.findByCallNo(requestId);
            }
            if (sleeperOpt.isPresent()) {
                return enrichFromSleeperCall(sleeperOpt.get(), dto);
            }

            logger.warn("No InspectionCall or SleeperInspectionCall found for requestId: {}", requestId);
            return dto;
        }
        InspectionCall ic = icOpt.get();

        dto.setTypeOfCall(ic.getTypeOfCall());
        dto.setProductType(ic.getTypeOfCall());
        dto.setRemarks(ic.getRemarks());

        String poiCode = ic.getPlaceOfInspection();

        List<String> poiParts = new java.util.ArrayList<>();
        String cName = ic.getCompanyName() != null ? ic.getCompanyName().trim() : "";
        if (!cName.isEmpty()) {
            poiParts.add(cName);
        }
        
        String uName = ic.getUnitName() != null ? ic.getUnitName().trim() : "";
        String uAddress = ic.getUnitAddress() != null ? ic.getUnitAddress().trim() : "";
        
        if (!uName.isEmpty()) {
            String normUName = normalizeForComparison(uName);
            String normUAddress = normalizeForComparison(uAddress);
            String normCName = normalizeForComparison(cName);

            boolean inCName = !cName.isEmpty() && (cName.toLowerCase().contains(uName.toLowerCase()) || normCName.contains(normUName));
            boolean inAddress = !uAddress.isEmpty() && (uAddress.toLowerCase().contains(uName.toLowerCase()) || normUAddress.contains(normUName));
            if (!inCName && !inAddress) {
                poiParts.add(uName);
            }
        }
        if (!uAddress.isEmpty()) {
            poiParts.add(uAddress);
        }

        if (!poiParts.isEmpty()) {
            String joined = String.join(", ", poiParts);
            String[] split = joined.split("[,\\r\\n]+");
            java.util.List<String> unique = new java.util.ArrayList<>();
            java.util.Set<String> seenLower = new java.util.HashSet<>();
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    String normalized = normalizeForComparison(trimmed);
                    if (!seenLower.contains(normalized)) {
                        unique.add(trimmed);
                        seenLower.add(normalized);
                    }
                }
            }
            dto.setPlaceOfInspection(String.join(", ", unique));
        } else {
            if (poiCode != null && !poiCode.isBlank()) {
                try {
                    java.util.Optional<com.sarthi.entity.PincodePoIMapping> poiOpt = pincodePoIMappingRepository
                            .findFirstByPoiCode(poiCode);
                    if (poiOpt.isPresent() && poiOpt.get().getAddress() != null) {
                        dto.setPlaceOfInspection(poiOpt.get().getAddress());
                    } else {
                        dto.setPlaceOfInspection(poiCode);
                    }
                } catch (Exception e) {
                    logger.error("Error looking up POI for code: {}", poiCode, e);
                    dto.setPlaceOfInspection(poiCode);
                }
            }
        }

        dto.setDesiredInspectionDate(
                ic.getDesiredInspectionDate() != null ? ic.getDesiredInspectionDate().toString() : null);
        dto.setOfferedInstallmentNo(ic.getIcNumber());

        // Fetch contact details from UserMaster based on vendorId
        if (ic.getVendorId() != null) {
            try {
                Optional<UserMaster> vendorUserOpt = userMasterRepository.findFirstByUserName(ic.getVendorId());
                if (vendorUserOpt.isPresent()) {
                    UserMaster vendorUser = vendorUserOpt.get();
                    dto.setContactPersonName(vendorUser.getFullName());
                    dto.setContactMobile(vendorUser.getMobileNumber());
                    dto.setContactEmail(vendorUser.getEmail());
                }
            } catch (Exception e) {
                logger.error("Error looking up vendor user for vendorId: {}", ic.getVendorId(), e);
            }
        }

        // Fetch RIO from the latest workflow transition for this call that has a non-null RIO
        try {
            com.sarthi.entity.WorkflowTransition wt = workflowTransitionRepository
                    .findFirstByRequestIdAndRioIsNotNullOrderByWorkflowTransitionIdDesc(requestId);
            if (wt != null && wt.getRio() != null) {
                dto.setRio(wt.getRio());
            }
        } catch (Exception e) {
            logger.error("Error fetching RIO for requestId: {}", requestId, e);
        }

        // Fetch Assigned IE details (for Process calls, fetch multiple mapped Process IEs from poi_process_ie_mapping)
        try {
            boolean ieSet = false;
            String callTypeStr = ic.getTypeOfCall() != null ? ic.getTypeOfCall().toLowerCase() : "";
            boolean isProcessCall = requestId.startsWith("EP") || callTypeStr.contains("process");
            boolean isFinalCall = requestId.startsWith("EF") || callTypeStr.contains("final");

            if (isProcessCall && poiCode != null && !poiCode.isBlank()) {
                List<Long> userIds = poiProcessIeMappingRepository.findUserIdsByPoiCode(poiCode);
                if (userIds != null && !userIds.isEmpty()) {
                    List<String> iePairs = new java.util.ArrayList<>();
                    for (Long uId : userIds) {
                        Optional<UserMaster> uOpt = userMasterRepository.findById(uId.intValue());
                        if (uOpt.isPresent()) {
                            UserMaster u = uOpt.get();
                            String name = u.getFullName() != null ? u.getFullName() : u.getUsername();
                            String mobile = u.getMobileNumber();
                            if (name != null && !name.isBlank()) {
                                String pair = (mobile != null && !mobile.isBlank())
                                        ? name + " - " + mobile
                                        : name;
                                if (!iePairs.contains(pair)) {
                                    iePairs.add(pair);
                                }
                            }
                        }
                    }
                    if (!iePairs.isEmpty()) {
                        dto.setIeName(String.join(", ", iePairs));
                        dto.setIeMobile(null);
                        ieSet = true;
                    }
                }
            }

            if (!ieSet) {
                java.util.List<com.sarthi.entity.WorkflowTransition> transitions = workflowTransitionRepository
                        .findByRequestIdOrderByWorkflowTransitionIdDesc(requestId);
                if (transitions != null) {
                    for (com.sarthi.entity.WorkflowTransition transition : transitions) {
                        Integer ieUserId = null;
                        if (transition.getAssignedToUser() != null) {
                            ieUserId = transition.getAssignedToUser();
                        } else if (!isFinalCall && transition.getProcessIeUserId() != null) {
                            ieUserId = transition.getProcessIeUserId();
                        }

                        if (ieUserId != null) {
                            java.util.Optional<UserMaster> ieUserOpt = userMasterRepository.findById(ieUserId);
                            if (ieUserOpt.isPresent()) {
                                UserMaster ieUser = ieUserOpt.get();
                                dto.setIeName(ieUser.getFullName() != null ? ieUser.getFullName() : ieUser.getUsername());
                                dto.setIeMobile(ieUser.getMobileNumber());
                            }
                            break; // found the most recently assigned IE
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching IE details for requestId: {}", requestId, e);
        }

        // Raw PO number stored on the IC (e.g. "26255265205057" or "60265359103833/001")
        String rawPoNo = ic.getPoNo();
        String rawPoSerialNo = ic.getPoSerialNo();
        String poSerialNo = null;

        if (rawPoSerialNo != null && !rawPoSerialNo.isBlank()) {
            String[] parts = rawPoSerialNo.split("/");
            poSerialNo = parts[parts.length - 1].trim(); // take last segment, e.g. "012"
        } else if (rawPoNo != null && rawPoNo.contains("/")) {
            String[] parts = rawPoNo.split("/");
            poSerialNo = parts[parts.length - 1].trim();
            rawPoNo = parts[0].trim();
        }
        logger.info("Resolved poNo: {}, poSerialNo: {} (raw: {})", rawPoNo, poSerialNo, rawPoSerialNo);

        // -------------------------------------------------------
        // 2. Fetch PO Header
        // -------------------------------------------------------
        if (rawPoNo != null) {
            try {
                Optional<PoHeader> phOpt = poHeaderRepository.findFirstByPoNo(rawPoNo);
                if (phOpt.isPresent()) {
                    PoHeader ph = phOpt.get();
                    dto.setRlyShortName(ph.getRlyShortName());
                    dto.setPoNo(ph.getPoNo());
                    dto.setPurchaserDetail(ph.getPurchaserDetail());
                    dto.setVendorName(ph.getFirmDetails());
                    dto.setCaseNo(ph.getCaseNo());

                    // Format PO date
                    if (ph.getPoDate() != null) {
                        dto.setPoDate(ph.getPoDate().format(DATE_FMT));
                    }

                    // Build composite "WR / 26255265205057 / 012"
                    String rlyPoSr = buildRlyPoSr(ph.getRlyShortName(), ph.getPoNo(), poSerialNo);
                    dto.setRlyPoSr(rlyPoSr);

                    // Calculate and set total PO Quantity and Value
                    if (ph.getItems() != null && !ph.getItems().isEmpty()) {
                        int totalQty = ph.getItems().stream()
                                .mapToInt(item -> item.getQty() != null ? item.getQty() : 0)
                                .sum();
                        dto.setPoQuantity(totalQty);

                        BigDecimal totalVal = ph.getItems().stream()
                                .map(item -> item.getValue() != null ? item.getValue() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        dto.setPoValue(totalVal.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
                    }
                } else {
                    logger.warn("No PoHeader found for poNo: {}", rawPoNo);
                    dto.setPoNo(rawPoNo);
                }
            } catch (Exception e) {
                logger.error("Error fetching PoHeader for poNo: {}", rawPoNo, e);
                dto.setPoNo(rawPoNo);
            }
        }

        // -------------------------------------------------------
        // 3. Fetch matching PO Item
        // -------------------------------------------------------
        if (rawPoNo != null && poSerialNo != null) {
            try {
                Optional<PoItem> piOpt = poItemRepository.findFirstByPoHeader_PoNoAndItemSrNo(rawPoNo, poSerialNo);
                logger.info("PoItem lookup for poNo={}, itemSrNo={} -> found={}", rawPoNo, poSerialNo, piOpt.isPresent());
                if (piOpt.isPresent()) {
                    PoItem pi = piOpt.get();
                    dto.setItemSrNo(pi.getItemSrNo());
                    dto.setItemDesc(pi.getItemDesc());
                    dto.setPoQty(pi.getQty());
                    dto.setUom(pi.getUom());
                    String consignee = (pi.getImmsConsigneeName() != null && !pi.getImmsConsigneeName().isBlank())
                            ? pi.getImmsConsigneeName().trim()
                            : pi.getConsigneeDetail();
                    dto.setConsigneeDetail(consignee);
                    dto.setBillPayOffDesc(pi.getBillPayOffDesc());

                    // Format delivery dates
                    if (pi.getDeliveryDate() != null) {
                        dto.setDeliveryDate(pi.getDeliveryDate().format(DATE_FMT));
                    }
                    if (pi.getExtendedDeliveryDate() != null) {
                        dto.setExtendedDeliveryDate(pi.getExtendedDeliveryDate().format(DATE_FMT));
                    }
                } else {
                    logger.warn("No PoItem found for poNo: {}, itemSrNo: {}", rawPoNo, poSerialNo);
                }
            } catch (Exception e) {
                logger.error("Error fetching PoItem for poNo: {}, itemSrNo: {}", rawPoNo, poSerialNo, e);
            }
        }

        // -------------------------------------------------------
        // 4. Calculate cumulative quantities passed (Raw Material & Final)
        // -------------------------------------------------------
        if (rawPoNo != null && poSerialNo != null) {
            try {
                // Fetch only required columns (icNumber, poSerialNo) to avoid N+1 query loading
                // child entities
                List<Object[]> results = inspectionCallRepository.findIcNumbersAndSerialNumbersByPoNo(rawPoNo);
                final String targetSerialNo = poSerialNo;
                List<String> callNos = results.stream()
                        .filter(row -> {
                            String cRawSerial = (String) row[1];
                            if (cRawSerial == null)
                                return false;
                            String[] parts = cRawSerial.split("/");
                            return parts[parts.length - 1].trim().equals(targetSerialNo);
                        })
                        .map(row -> (String) row[0])
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // Calculate Raw Material Qty Passed (sum from RmHeatFinalResult)
                BigDecimal totalRmAccepted = BigDecimal.ZERO;
                if (!callNos.isEmpty()) {
                    List<RmHeatFinalResult> heatResults = rmHeatFinalResultRepository.findByInspectionCallNoIn(callNos);
                    for (RmHeatFinalResult hr : heatResults) {
                        if (hr.getWeightAcceptedMt() != null) {
                            totalRmAccepted = totalRmAccepted.add(hr.getWeightAcceptedMt());
                        }
                    }
                }
                dto.setRawMaterialQtyPassed(
                        totalRmAccepted.setScale(3, java.math.RoundingMode.HALF_UP).toPlainString() + " MT");

                // Calculate Final Accepted Qty (sum of qtyNowPassed from
                // FinalCumulativeResults)
                int totalFinalPassed = 0;
                if (!callNos.isEmpty()) {
                    List<Object[]> finalQtyList = finalCumulativeResultsRepository.findFinalInspectionQty(callNos);
                    if (finalQtyList != null && !finalQtyList.isEmpty() && finalQtyList.get(0) != null) {
                        Object[] row = finalQtyList.get(0);
                        if (row[0] != null) {
                            totalFinalPassed = ((Number) row[0]).intValue();
                        }
                    }
                }
                String uom = dto.getUom() != null ? dto.getUom() : "Nos.";
                dto.setFinalAcceptedQty(totalFinalPassed + " " + uom);

            } catch (Exception e) {
                logger.error("Error calculating cumulative passed quantities for PO: {}, Serial: {}", rawPoNo,
                        poSerialNo, e);
                dto.setRawMaterialQtyPassed("-");
                dto.setFinalAcceptedQty("-");
            }
        } else {
            dto.setRawMaterialQtyPassed("-");
            dto.setFinalAcceptedQty("-");
        }

        // -------------------------------------------------------
        // 5. Type-specific enrichment
        // -------------------------------------------------------
        String callType = ic.getTypeOfCall();
        if (callType == null)
            callType = "";

        if (callType.toLowerCase().contains("raw")) {
            enrichFromRm(ic, dto);
        } else if (callType.toLowerCase().contains("process")) {
            enrichFromProcess(ic, dto);
        } else if (callType.toLowerCase().contains("final")) {
            enrichFromFinal(ic, dto);
        }

        logger.info("Call letter details built successfully for requestId: {}", requestId);
        return dto;
    }

    // -------------------------------------------------------
    // Raw Material enrichment
    // -------------------------------------------------------
    private void enrichFromRm(InspectionCall ic, CallLetterDetailsDto dto) {
        RmInspectionDetails rm = ic.getRmInspectionDetails();
        if (rm == null)
            return;

        // Item description from RM details takes priority if PO item not available
        if (dto.getItemDesc() == null && rm.getItemDescription() != null) {
            dto.setItemDesc(rm.getItemDescription());
        }

        // Call quantity
        if (rm.getOfferedQtyErc() != null && rm.getOfferedQtyErc() > 0) {
            dto.setCallQty(rm.getOfferedQtyErc().toString());
            dto.setCallUnit(rm.getUnitOfMeasurement() != null ? rm.getUnitOfMeasurement() : "Nos.");
        } else {
            java.math.BigDecimal sumOfHeats = java.math.BigDecimal.ZERO;
            if (rm.getHeatQuantities() != null && !rm.getHeatQuantities().isEmpty()) {
                sumOfHeats = rm.getHeatQuantities().stream()
                        .map(hq -> hq.getOfferedQty() != null ? hq.getOfferedQty() : java.math.BigDecimal.ZERO)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            }

            if (sumOfHeats.compareTo(java.math.BigDecimal.ZERO) > 0) {
                dto.setCallQty(sumOfHeats.toPlainString());
                dto.setCallUnit("MT");
            } else if (rm.getTotalOfferedQtyMt() != null) {
                dto.setCallQty(rm.getTotalOfferedQtyMt().toPlainString());
                dto.setCallUnit("MT");
            }
        }

        // Manufacturer
        if (rm.getManufacturer() != null) {
            dto.setManufacturerName(rm.getManufacturer());
        } else {
            dto.setManufacturerName(ic.getCompanyName());
        }

        // Place of inspection fallback
        if (dto.getPlaceOfInspection() == null || dto.getPlaceOfInspection().isBlank()) {
            dto.setPlaceOfInspection(rm.getSupplierAddress());
        }

        // Fetch and map heat details
        if (rm.getId() != null) {
            try {
                List<RmHeatQuantity> hqList = rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(rm.getId()));
                if (hqList != null && !hqList.isEmpty()) {
                    List<CallLetterDetailsDto.HeatDetail> heatDetailsList = new java.util.ArrayList<>();
                    for (RmHeatQuantity hq : hqList) {
                        CallLetterDetailsDto.HeatDetail hd = new CallLetterDetailsDto.HeatDetail();
                        hd.setHeatNo(hq.getHeatNumber());
                        hd.setTcNo(hq.getTcNumber());
                        hd.setQtyOffered(hq.getOfferedQty() != null ? hq.getOfferedQty().toPlainString() : "-");
                        heatDetailsList.add(hd);
                    }
                    dto.setHeatDetails(heatDetailsList);
                }
            } catch (Exception e) {
                logger.error("Error fetching heat details for RM details ID: {}", rm.getId(), e);
            }
        }
    }

    // -------------------------------------------------------
    // Process enrichment
    // -------------------------------------------------------
    private void enrichFromProcess(InspectionCall ic, CallLetterDetailsDto dto) {
        List<ProcessInspectionDetails> processList = ic.getProcessInspectionDetails();
        if (processList == null || processList.isEmpty()) {
            dto.setManufacturerName(ic.getCompanyName());
            return;
        }

        // Aggregate offered qty across all lots
        int totalOffered = processList.stream()
                .mapToInt(p -> p.getOfferedQty() != null ? p.getOfferedQty() : 0)
                .sum();
        if (totalOffered > 0) {
            dto.setCallQty(String.valueOf(totalOffered));
            dto.setCallUnit("Nos.");
        }

        // Manufacturer from first process detail
        ProcessInspectionDetails first = processList.get(0);
        String mfr = first.getManufacturer() != null ? first.getManufacturer() : first.getCompanyName();
        dto.setManufacturerName(mfr != null ? mfr : ic.getCompanyName());

        // Place of inspection fallback
        if (dto.getPlaceOfInspection() == null || dto.getPlaceOfInspection().isBlank()) {
            String addr = first.getUnitAddress();
            dto.setPlaceOfInspection(addr);
        }
    }

    // -------------------------------------------------------
    // Final enrichment
    // -------------------------------------------------------
    private void enrichFromFinal(InspectionCall ic, CallLetterDetailsDto dto) {
        FinalInspectionDetails fin = ic.getFinalInspectionDetails();
        if (fin == null) {
            dto.setManufacturerName(ic.getCompanyName());
            return;
        }

        if (fin.getTotalOfferedQty() != null && fin.getTotalOfferedQty() > 0) {
            dto.setCallQty(fin.getTotalOfferedQty().toString());
            dto.setCallUnit("Nos.");
        }

        // Manufacturer
        String mfr = fin.getCompanyName();
        dto.setManufacturerName(mfr != null ? mfr : ic.getCompanyName());

        // Place of inspection fallback
        if (dto.getPlaceOfInspection() == null || dto.getPlaceOfInspection().isBlank()) {
            dto.setPlaceOfInspection(fin.getUnitAddress());
        }
    }

    // -------------------------------------------------------
    // Helper: build composite PO string
    // -------------------------------------------------------
    private String buildRlyPoSr(String rly, String poNo, String srNo) {
        StringBuilder sb = new StringBuilder();
        if (rly != null && !rly.isBlank())
            sb.append(rly).append(" / ");
        if (poNo != null && !poNo.isBlank())
            sb.append(poNo);
        if (srNo != null && !srNo.isBlank())
            sb.append(" / ").append(srNo);
        return sb.toString();
    }

    private String normalizeForComparison(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("unit[-\\s]*viii\\b", "unit-8")
                .replaceAll("unit[-\\s]*vii\\b", "unit-7")
                .replaceAll("unit[-\\s]*vi\\b", "unit-6")
                .replaceAll("unit[-\\s]*iv\\b", "unit-4")
                .replaceAll("unit[-\\s]*v\\b", "unit-5")
                .replaceAll("unit[-\\s]*iii\\b", "unit-3")
                .replaceAll("unit[-\\s]*ii\\b", "unit-2")
                .replaceAll("unit[-\\s]*i\\b", "unit-1")
                .replaceAll("[\\s-]", "");
    }

    private String normalizeSrNo(String srNo) {
        if (srNo == null) return "";
        return srNo.trim().replaceFirst("^0+(?!$)", "");
    }

    private CallLetterDetailsDto enrichFromSleeperCall(com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall sleeperCall, CallLetterDetailsDto dto) {
        dto.setRequestId(sleeperCall.getCallNo());
        dto.setTypeOfCall("Final Inspection");

        String sleeperType = sleeperCall.getSleeperType() != null ? sleeperCall.getSleeperType().toUpperCase() : "";
        boolean isTurnout = sleeperType.contains("SET") || sleeperType.contains("PNC") || sleeperType.contains("TURNOUT")
                || sleeperType.contains("4218") || sleeperType.contains("4865") || sleeperType.contains("9790")
                || sleeperType.contains("4732") || sleeperType.contains("DERAIL");

        if (isTurnout) {
            int sets = sleeperCall.getTotalOffered() != null && sleeperCall.getTotalOffered() > 0 ? sleeperCall.getTotalOffered() : 1;
            dto.setCallQty(String.valueOf(sets));
            dto.setCallUnit("Set");
        } else {
            int batchTotalOffered = 0;
            if (sleeperCall.getBatchesSelected() != null) {
                for (com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch b : sleeperCall.getBatchesSelected()) {
                    int g = b.getGoodSleepers() != null ? b.getGoodSleepers().size() : 0;
                    int bad = b.getBadSleepers() != null ? b.getBadSleepers().size() : 0;
                    batchTotalOffered += (g + bad);
                }
            }

            int callQtyVal = batchTotalOffered > 0
                    ? batchTotalOffered
                    : (sleeperCall.getTotalOffered() != null ? sleeperCall.getTotalOffered() : 0);

            dto.setCallQty(String.valueOf(callQtyVal));
            dto.setCallUnit("Nos.");
        }

        // Calculate offered installment number as integer based on PO Number + Sr Number
        int installmentNo = 1;
        if (sleeperCall.getPoNo() != null && !sleeperCall.getPoNo().isBlank()) {
            try {
                String targetSrNo = sleeperCall.getSrNo() != null ? sleeperCall.getSrNo().trim() : "";
                List<com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall> poCalls =
                        sleeperInspectionCallRepository.findByPoNoOrderByIdAsc(sleeperCall.getPoNo());
                if (poCalls != null && !poCalls.isEmpty()) {
                    int counter = 0;
                    for (com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall c : poCalls) {
                        String cSrNo = c.getSrNo() != null ? c.getSrNo().trim() : "";
                        boolean isSameSrNo = targetSrNo.isEmpty() || cSrNo.isEmpty()
                                || cSrNo.equalsIgnoreCase(targetSrNo)
                                || normalizeSrNo(cSrNo).equalsIgnoreCase(normalizeSrNo(targetSrNo));

                        if (isSameSrNo) {
                            counter++;
                            if ((c.getId() != null && c.getId().equals(sleeperCall.getId()))
                                    || (c.getCallNo() != null && c.getCallNo().equalsIgnoreCase(sleeperCall.getCallNo()))) {
                                installmentNo = counter;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error calculating offered installment number for sleeper call: {}", sleeperCall.getCallNo(), e);
            }
        }
        dto.setOfferedInstallmentNo(String.valueOf(installmentNo));

        if (sleeperCall.getDesiredInspectionDate() != null) {
            dto.setDesiredInspectionDate(sleeperCall.getDesiredInspectionDate().format(DATE_FMT));
        } else if (sleeperCall.getCreatedAt() != null) {
            dto.setDesiredInspectionDate(sleeperCall.getCreatedAt().format(DATE_FMT));
        }

        // Contact & Vendor info from creator / VendorMaster / VendorPlant
        String vendorCode = null;
        if (sleeperCall.getCreatedBy() != null) {
            try {
                Optional<UserMaster> userOpt = userMasterRepository.findById(sleeperCall.getCreatedBy().intValue());
                if (userOpt.isPresent()) {
                    UserMaster u = userOpt.get();
                    dto.setContactPersonName(u.getFullName() != null ? u.getFullName() : u.getUsername());
                    dto.setContactMobile(u.getMobileNumber());
                    dto.setContactEmail(u.getEmail());
                    vendorCode = u.getUsername();
                }

                // Lookup VendorMaster by ID or VendorCode (initial fallback)
                Optional<com.sarthi.entity.VendorMaster> vmOpt = vendorMasterRepository.findById(sleeperCall.getCreatedBy());
                if (vmOpt.isEmpty() && vendorCode != null && !vendorCode.isBlank()) {
                    vmOpt = vendorMasterRepository.findByVendorCode(vendorCode.trim());
                    if (vmOpt.isEmpty() && vendorCode.contains(":")) {
                        vmOpt = vendorMasterRepository.findByVendorCode(vendorCode.replace(":", "").trim());
                    }
                    if (vmOpt.isEmpty() && !vendorCode.startsWith(":")) {
                        vmOpt = vendorMasterRepository.findByVendorCode(":" + vendorCode.trim());
                    }
                }

                if (vmOpt.isPresent()) {
                    com.sarthi.entity.VendorMaster vm = vmOpt.get();
                    if (vm.getVendorName() != null && !vm.getVendorName().isBlank()) {
                        dto.setVendorName(vm.getVendorName().trim());
                        dto.setManufacturerName(vm.getVendorName().trim());
                        if (dto.getContactPersonName() == null || dto.getContactPersonName().isBlank()
                                || dto.getContactPersonName().startsWith(":") || dto.getContactPersonName().equalsIgnoreCase(vendorCode)) {
                            dto.setContactPersonName(vm.getVendorName().trim());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error looking up vendor/user for sleeper call createdBy: {}", sleeperCall.getCreatedBy(), e);
            }
        }

        // Look up VendorPlant: vendor_plant.plant_name takes precedence for Vendor Name, Manufacturer Name, and Place of Inspection
        com.sarthi.Sleeper.entity.VendorPlant resolvedVp = null;
        if (sleeperCall.getPlantId() != null && !sleeperCall.getPlantId().isBlank()) {
            String pId = sleeperCall.getPlantId().trim();
            try {
                List<com.sarthi.Sleeper.entity.VendorPlant> vpList = vendorPlantRepository.findMatchingPlants(pId);
                if (vpList == null || vpList.isEmpty()) {
                    String cleanPlant = pId.replace(":", "");
                    vpList = vendorPlantRepository.findMatchingPlants(cleanPlant);
                }
                if ((vpList == null || vpList.isEmpty()) && pId.contains("/")) {
                    String[] parts = pId.split("/");
                    for (int i = parts.length - 1; i >= 0; i--) {
                        String part = parts[i].trim();
                        if (!part.isEmpty()) {
                            vpList = vendorPlantRepository.findMatchingPlants(part);
                            if (vpList != null && !vpList.isEmpty()) break;
                        }
                    }
                }
                if (vpList != null && !vpList.isEmpty()) {
                    resolvedVp = vpList.get(0);
                } else {
                    Optional<com.sarthi.Sleeper.entity.VendorPlant> vpOpt = vendorPlantRepository.findByPlantId(pId);
                    if (vpOpt.isEmpty() && pId.contains("/")) {
                        for (String part : pId.split("/")) {
                            vpOpt = vendorPlantRepository.findByPlantId(part.trim());
                            if (vpOpt.isPresent()) break;
                        }
                    }
                    if (vpOpt.isPresent()) {
                        resolvedVp = vpOpt.get();
                    }
                }
            } catch (Exception e) {
                logger.error("Error looking up vendor_plant for plantId: {}", pId, e);
            }
        }

        if (resolvedVp == null && sleeperCall.getCreatedBy() != null) {
            try {
                List<com.sarthi.Sleeper.entity.VendorPlant> vpByVendor = vendorPlantRepository.findByVendorId(sleeperCall.getCreatedBy());
                if (vpByVendor != null && !vpByVendor.isEmpty()) {
                    resolvedVp = vpByVendor.get(0);
                }
            } catch (Exception ignore) {}
        }

        if (resolvedVp != null) {
            String plantOrCompanyName = (resolvedVp.getPlantName() != null && !resolvedVp.getPlantName().isBlank())
                    ? resolvedVp.getPlantName().trim()
                    : (resolvedVp.getCompanyName() != null ? resolvedVp.getCompanyName().trim() : null);

            if (plantOrCompanyName != null) {
                dto.setPlaceOfInspection(plantOrCompanyName);
                dto.setVendorName(plantOrCompanyName);
                dto.setManufacturerName(plantOrCompanyName);
                dto.setContactPersonName(plantOrCompanyName);
            }

            if (resolvedVp.getContactPerson() != null && !resolvedVp.getContactPerson().isBlank()
                    && !resolvedVp.getContactPerson().startsWith(":") && !resolvedVp.getContactPerson().equalsIgnoreCase(vendorCode)) {
                dto.setContactPersonName(resolvedVp.getContactPerson().trim());
            }

            if (resolvedVp.getContactPersonNumber() != null && !resolvedVp.getContactPersonNumber().isBlank()) {
                dto.setContactMobile(resolvedVp.getContactPersonNumber().trim());
            }
        } else if (dto.getPlaceOfInspection() == null || dto.getPlaceOfInspection().isBlank()) {
            if (sleeperCall.getPlantId() != null) {
                String pId = sleeperCall.getPlantId().trim();
                if (pId.contains("/")) {
                    String[] parts = pId.split("/");
                    dto.setPlaceOfInspection(parts[parts.length - 1].trim());
                } else {
                    dto.setPlaceOfInspection(pId.replace(":", "").trim());
                }
            }
        }

        // RIO info from workflow or VendorPlant
        try {
            com.sarthi.entity.WorkflowTransition wt = workflowTransitionRepository
                    .findFirstByRequestIdAndRioIsNotNullOrderByWorkflowTransitionIdDesc(sleeperCall.getCallNo());
            if (wt != null && wt.getRio() != null && !wt.getRio().isBlank()) {
                dto.setRio(wt.getRio());
            } else if (sleeperCall.getPlantId() != null && !sleeperCall.getPlantId().isBlank()) {
                String pId = sleeperCall.getPlantId().trim();
                List<com.sarthi.Sleeper.entity.VendorPlant> vpList = vendorPlantRepository.findMatchingPlants(pId);
                if (vpList == null || vpList.isEmpty()) {
                    String cleanPlant = pId.replace(":", "");
                    vpList = vendorPlantRepository.findMatchingPlants(cleanPlant);
                }
                if ((vpList == null || vpList.isEmpty()) && pId.contains("/")) {
                    for (String part : pId.split("/")) {
                        if (!part.trim().isEmpty()) {
                            vpList = vendorPlantRepository.findMatchingPlants(part.trim());
                            if (vpList != null && !vpList.isEmpty()) break;
                        }
                    }
                }
                if (vpList != null && !vpList.isEmpty()) {
                    for (com.sarthi.Sleeper.entity.VendorPlant vp : vpList) {
                        if (vp.getRio() != null && !vp.getRio().trim().isEmpty()) {
                            dto.setRio(vp.getRio().trim());
                            break;
                        }
                    }
                }
                if (dto.getRio() == null || dto.getRio().isBlank()) {
                    Optional<com.sarthi.Sleeper.entity.VendorPlant> vpOpt = vendorPlantRepository.findByPlantId(pId);
                    if (vpOpt.isEmpty() && pId.contains("/")) {
                        for (String part : pId.split("/")) {
                            vpOpt = vendorPlantRepository.findByPlantId(part.trim());
                            if (vpOpt.isPresent()) break;
                        }
                    }
                    if (vpOpt.isPresent() && vpOpt.get().getRio() != null && !vpOpt.get().getRio().trim().isEmpty()) {
                        dto.setRio(vpOpt.get().getRio().trim());
                    }
                }
            }

            // Fallback by createdBy (vendorId) if still not found
            if ((dto.getRio() == null || dto.getRio().isBlank()) && sleeperCall.getCreatedBy() != null) {
                List<com.sarthi.Sleeper.entity.VendorPlant> vpByVendor = vendorPlantRepository.findByVendorId(sleeperCall.getCreatedBy());
                if (vpByVendor != null && !vpByVendor.isEmpty()) {
                    for (com.sarthi.Sleeper.entity.VendorPlant vp : vpByVendor) {
                        if (vp.getRio() != null && !vp.getRio().trim().isEmpty()) {
                            dto.setRio(vp.getRio().trim());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching RIO for sleeper call: {}", sleeperCall.getCallNo(), e);
        }

        // IE Details from workflow (only populated if an IE is actually assigned)
        try {
            Integer assignedIeId = null;

            // 1. Check sleeper_workflow_transaction
            List<com.sarthi.Sleeper.entity.SleeperWorkflowTransaction> sleeperTxs = sleeperWorkflowRepository
                    .findByRequestIdOrderByCreatedDateAsc(sleeperCall.getCallNo());
            if (sleeperTxs != null && !sleeperTxs.isEmpty()) {
                for (int i = sleeperTxs.size() - 1; i >= 0; i--) {
                    if (sleeperTxs.get(i).getAssignedToUser() != null) {
                        assignedIeId = sleeperTxs.get(i).getAssignedToUser().intValue();
                        break;
                    }
                }
            }

            // 2. Check general WORKFLOW_TRANSITION if not found
            if (assignedIeId == null) {
                List<com.sarthi.entity.WorkflowTransition> transitions = workflowTransitionRepository
                        .findByRequestIdOrderByWorkflowTransitionIdDesc(sleeperCall.getCallNo());
                if (transitions != null) {
                    for (com.sarthi.entity.WorkflowTransition transition : transitions) {
                        Integer ieUserId = transition.getAssignedToUser() != null ? transition.getAssignedToUser() : transition.getProcessIeUserId();
                        if (ieUserId != null) {
                            assignedIeId = ieUserId;
                            break;
                        }
                    }
                }
            }

            // 3. Populate IE info only if assigned
            if (assignedIeId != null) {
                Optional<UserMaster> ieUserOpt = userMasterRepository.findById(assignedIeId);
                if (ieUserOpt.isPresent()) {
                    UserMaster ieUser = ieUserOpt.get();
                    dto.setIeName(ieUser.getFullName() != null ? ieUser.getFullName() : ieUser.getUsername());
                    dto.setIeMobile(ieUser.getMobileNumber());
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching IE details for sleeper call: {}", sleeperCall.getCallNo(), e);
        }

        // PO Header & Item lookup
        String rawPoNo = sleeperCall.getPoNo();
        String rawSrNo = sleeperCall.getSrNo();

        if (rawPoNo != null) {
            try {
                Optional<PoHeader> phOpt = poHeaderRepository.findFirstByPoNo(rawPoNo);
                if (phOpt.isPresent()) {
                    PoHeader ph = phOpt.get();
                    dto.setRlyShortName(ph.getRlyShortName());
                    dto.setPoNo(ph.getPoNo());
                    dto.setPurchaserDetail(ph.getPurchaserDetail());
                    String resolvedCaseNo = resolveSleeperCaseNo(ph.getCaseNo(), dto.getRio());
                    dto.setCaseNo(resolvedCaseNo);
                    if (ph.getPoDate() != null) {
                        dto.setPoDate(ph.getPoDate().format(DATE_FMT));
                    }
                    dto.setRlyPoSr(buildRlyPoSr(ph.getRlyShortName(), ph.getPoNo(), rawSrNo));

                    if (ph.getItems() != null && !ph.getItems().isEmpty()) {
                        int totalQty = ph.getItems().stream()
                                .mapToInt(item -> item.getQty() != null ? item.getQty() : 0)
                                .sum();
                        dto.setPoQuantity(totalQty);

                        BigDecimal totalVal = ph.getItems().stream()
                                .map(item -> item.getValue() != null ? item.getValue() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        dto.setPoValue(totalVal.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
                    }
                } else {
                    dto.setPoNo(rawPoNo);
                }
            } catch (Exception e) {
                logger.error("Error fetching PoHeader for sleeper call: {}", rawPoNo, e);
                dto.setPoNo(rawPoNo);
            }
        }

        if (rawPoNo != null && rawSrNo != null) {
            try {
                Optional<PoItem> piOpt = poItemRepository.findFirstByPoHeader_PoNoAndItemSrNo(rawPoNo, rawSrNo);
                if (piOpt.isEmpty() && rawSrNo.length() < 3) {
                    try {
                        String padded = String.format("%03d", Integer.parseInt(rawSrNo));
                        piOpt = poItemRepository.findFirstByPoHeader_PoNoAndItemSrNo(rawPoNo, padded);
                    } catch (Exception ignore) {}
                }
                if (piOpt.isPresent()) {
                    PoItem pi = piOpt.get();
                    dto.setItemSrNo(pi.getItemSrNo());
                    dto.setItemDesc(pi.getItemDesc());
                    dto.setPoQty(pi.getQty());
                    if (pi.getUom() != null && !pi.getUom().isBlank()) {
                        String itemUom = pi.getUom().trim();
                        dto.setUom(itemUom);
                        if (itemUom.toUpperCase().contains("SET")) {
                            dto.setCallUnit("Set");
                            int setQty = sleeperCall.getTotalOffered() != null && sleeperCall.getTotalOffered() > 0 && sleeperCall.getTotalOffered() <= 50
                                    ? sleeperCall.getTotalOffered()
                                    : 1;
                            dto.setCallQty(String.valueOf(setQty));
                        } else {
                            dto.setCallUnit(itemUom);
                        }
                    } else if (dto.getUom() == null) {
                        dto.setUom("Nos.");
                    }
                    String consignee = (pi.getImmsConsigneeName() != null && !pi.getImmsConsigneeName().isBlank())
                            ? pi.getImmsConsigneeName().trim()
                            : pi.getConsigneeDetail();
                    dto.setConsigneeDetail(consignee);
                    dto.setBillPayOffDesc(pi.getBillPayOffDesc());
                    if (pi.getDeliveryDate() != null) {
                        dto.setDeliveryDate(pi.getDeliveryDate().format(DATE_FMT));
                    }
                    if (pi.getExtendedDeliveryDate() != null) {
                        dto.setExtendedDeliveryDate(pi.getExtendedDeliveryDate().format(DATE_FMT));
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching PoItem for sleeper call: poNo={}, srNo={}", rawPoNo, rawSrNo, e);
            }
        }

        // Batches to heatDetails and batchesSelected
        try {
            if (sleeperCall.getBatchesSelected() != null && !sleeperCall.getBatchesSelected().isEmpty()) {
                List<CallLetterDetailsDto.HeatDetail> heatDetailsList = new java.util.ArrayList<>();
                List<CallLetterDetailsDto.SleeperBatchDetail> batchesList = new java.util.ArrayList<>();

                java.util.Map<Long, Long> batchCountCache = new java.util.HashMap<>();
                java.util.Map<String, ProductionDeclaration> pdBatchCache = new java.util.HashMap<>();

                // Preload all batch names
                java.util.Set<String> allBatchNames = new java.util.HashSet<>();
                for (com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch batch : sleeperCall.getBatchesSelected()) {
                    if (batch != null && batch.getBatchNo() != null) {
                        String raw = batch.getBatchNo().trim();
                        String clean = raw.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                        allBatchNames.add(raw);
                        allBatchNames.add(clean);
                        allBatchNames.add("Batch " + clean);
                    }
                }

                // 1. Preload all ET sleepers in 1 bulk query
                java.util.Map<String, List<String>> etSleepersByBatch = new java.util.HashMap<>();
                if (!allBatchNames.isEmpty() && etSleeperDetailsRepository != null) {
                    try {
                        List<com.sarthi.Sleeper.entity.EtSleeperDetails> etList = etSleeperDetailsRepository.findByEt_BatchNumberIn(allBatchNames);
                        if (etList != null) {
                            for (com.sarthi.Sleeper.entity.EtSleeperDetails et : etList) {
                                if (et != null && et.getEt() != null && et.getEt().getBatchNumber() != null && et.getSleeperNo() != null) {
                                    String bNo = et.getEt().getBatchNumber().trim();
                                    etSleepersByBatch.computeIfAbsent(bNo, k -> new java.util.ArrayList<>()).add(et.getSleeperNo().trim());
                                    String cleanBNo = bNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                                    etSleepersByBatch.computeIfAbsent(cleanBNo, k -> new java.util.ArrayList<>()).add(et.getSleeperNo().trim());
                                }
                            }
                        }
                    } catch (Exception ignore) {}
                }

                // 2. Preload ProductionDeclaration records using indexed batch IN query
                if (!allBatchNames.isEmpty() && productionDeclarationRepository != null) {
                    try {
                        List<ProductionDeclaration> pds = productionDeclarationRepository.findAllByBatchNumbers(allBatchNames);
                        if (pds != null) {
                            for (ProductionDeclaration item : pds) {
                                if (item != null && item.getBatchNumber() != null) {
                                    String b = item.getBatchNumber().trim();
                                    pdBatchCache.putIfAbsent(b, item);
                                    String cb = b.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                                    pdBatchCache.putIfAbsent(cb, item);
                                    pdBatchCache.putIfAbsent("Batch " + cb, item);
                                }
                            }
                        }
                    } catch (Exception ignore) {}
                }

                // 3. Preload all batch sleeper counts in 1 single bulk query
                java.util.Set<Long> allPdIds = new java.util.HashSet<>();
                for (var pdItem : pdBatchCache.values()) {
                    if (pdItem != null && pdItem.getId() != null) {
                        allPdIds.add(pdItem.getId());
                    }
                }
                if (!allPdIds.isEmpty() && productionSleeperRepository != null) {
                    try {
                        List<Object[]> counts = productionSleeperRepository.countSleepersByBatchIds(new java.util.ArrayList<>(allPdIds));
                        if (counts != null) {
                            for (Object[] row : counts) {
                                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                                    Long batchId = ((Number) row[0]).longValue();
                                    Long cnt = ((Number) row[1]).longValue();
                                    batchCountCache.put(batchId, cnt);
                                }
                            }
                        }
                    } catch (Exception ignore) {}
                }

                // 4. Preload all rejected inspection test results directly by batch numbers
                java.util.Map<String, DefectDetail> defectByBatchAndSleeper = new java.util.HashMap<>();
                if (!allBatchNames.isEmpty() && inspectionTestResultRepository != null) {
                    try {
                        List<Object[]> rejRows = inspectionTestResultRepository.findRejectionDetailsByBatchNumbers(allBatchNames);
                        if (rejRows != null) {
                            for (Object[] row : rejRows) {
                                if (row != null && row.length >= 4) {
                                    String bNo = row[0] != null ? String.valueOf(row[0]).trim() : "";
                                    String sNo = row[1] != null ? String.valueOf(row[1]).trim() : "";
                                    String reason = row[3] != null ? String.valueOf(row[3]).trim() : "";
                                    Long modId = null;
                                    if (row.length >= 5 && row[4] != null) {
                                        try {
                                            modId = ((Number) row[4]).longValue();
                                        } catch (Exception ignore) {}
                                    }
                                    if (!sNo.isEmpty()) {
                                        String cleanBNo = bNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                                        String cleanSNo = sNo.replaceAll("\\s+", "").toLowerCase();
                                        DefectDetail dd = new DefectDetail(reason, modId);
                                        defectByBatchAndSleeper.put(cleanBNo.toLowerCase() + ":" + cleanSNo, dd);
                                        defectByBatchAndSleeper.put(bNo.toLowerCase() + ":" + cleanSNo, dd);
                                        defectByBatchAndSleeper.putIfAbsent(cleanSNo, dd);
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.warn("Error preloading rejection details by batch numbers: {}", ex.getMessage());
                    }
                }

                java.util.Map<Long, List<com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult>> resultsByBatchId = new java.util.HashMap<>();
                if (!allPdIds.isEmpty() && inspectionTestResultRepository != null) {
                    try {
                        List<com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult> allResults = inspectionTestResultRepository.findAllResultsByBatchIds(allPdIds);
                        if (allResults != null) {
                            for (com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult r : allResults) {
                                if (r != null && r.getTestHeader() != null && r.getTestHeader().getBatchId() != null) {
                                    resultsByBatchId.computeIfAbsent(r.getTestHeader().getBatchId(), k -> new java.util.ArrayList<>()).add(r);
                                }
                            }
                        }
                    } catch (Exception ignore) {}
                }

                // 5. Preload all Moment of Failure (MF / MFT) tested sleepers in 1 bulk query
                java.util.Map<String, List<String>> mfSleepersByBatch = new java.util.HashMap<>();
                if (!allBatchNames.isEmpty()) {
                    if (mfTestDetailsRepository != null) {
                        try {
                            List<com.sarthi.Sleeper.entity.MfTestDetails> mfTests = mfTestDetailsRepository.findByBatchNumbersIn(allBatchNames);
                            if (mfTests != null) {
                                for (com.sarthi.Sleeper.entity.MfTestDetails t : mfTests) {
                                    if (t != null && t.getBatchNo() != null && t.getSampleIdentification() != null && !t.getSampleIdentification().isBlank()) {
                                        String bNo = t.getBatchNo().trim();
                                        String sNo = cleanMfSleeperNo(t.getSampleIdentification());
                                        if (!sNo.isBlank()) {
                                            mfSleepersByBatch.computeIfAbsent(bNo, k -> new java.util.ArrayList<>()).add(sNo);
                                            String cleanBNo = bNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                                            mfSleepersByBatch.computeIfAbsent(cleanBNo, k -> new java.util.ArrayList<>()).add(sNo);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignore) {}
                    }
                    if (modulusOfFailureRepository != null) {
                        try {
                            List<com.sarthi.Sleeper.entity.ModulusOfFailure> mfs = modulusOfFailureRepository.findByBatchNumbersIn(allBatchNames);
                            if (mfs != null) {
                                for (com.sarthi.Sleeper.entity.ModulusOfFailure m : mfs) {
                                    if (m != null && m.getBatchNo() != null && m.getSampleIdentification() != null && !m.getSampleIdentification().isBlank()) {
                                        String bNo = m.getBatchNo().trim();
                                        String sNo = cleanMfSleeperNo(m.getSampleIdentification());
                                        if (!sNo.isBlank()) {
                                            mfSleepersByBatch.computeIfAbsent(bNo, k -> new java.util.ArrayList<>()).add(sNo);
                                            String cleanBNo = bNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                                            mfSleepersByBatch.computeIfAbsent(cleanBNo, k -> new java.util.ArrayList<>()).add(sNo);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignore) {}
                    }
                }

                for (com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch batch : sleeperCall.getBatchesSelected()) {
                    if (batch == null) continue;
                    String rawBatchNo = batch.getBatchNo() != null ? batch.getBatchNo().trim() : "-";
                    String displayBatchNo = rawBatchNo.startsWith("Batch ") ? rawBatchNo : "Batch " + rawBatchNo;
                    String cleanBatch = rawBatchNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();

                    List<String> goodSleepersList = new java.util.ArrayList<>();
                    List<String> rawBadSleepersList = new java.util.ArrayList<>();

                    try {
                        if (batch.getGoodSleepers() != null) {
                            for (com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail s : batch.getGoodSleepers()) {
                                if (s != null && s.getSleeperNo() != null && !s.getSleeperNo().isBlank()) {
                                    goodSleepersList.add(s.getSleeperNo().trim());
                                }
                            }
                        }
                    } catch (Exception ignore) {}

                    try {
                        if (batch.getBadSleepers() != null) {
                            for (com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail s : batch.getBadSleepers()) {
                                if (s != null && s.getSleeperNo() != null && !s.getSleeperNo().isBlank()) {
                                    rawBadSleepersList.add(s.getSleeperNo().trim());
                                }
                            }
                        }
                    } catch (Exception ignore) {}

                    int goodCount = goodSleepersList.size();
                    int badCount = rawBadSleepersList.size();

                    // Look up ProductionDeclaration directly from preloaded pdBatchCache
                    String castDateStr = batch.getCastDate();
                    Integer totalCasted = batch.getTotalCasted();
                    int prevOffered = batch.getPreviouslyOffered() != null ? batch.getPreviouslyOffered() : 0;

                    ProductionDeclaration pd = pdBatchCache.get(rawBatchNo);
                    if (pd == null) {
                        pd = pdBatchCache.get(cleanBatch);
                    }
                    if (pd == null) {
                        pd = pdBatchCache.get(displayBatchNo);
                    }

                    // Extract castDate and totalCasted from pd if needed
                    if (pd != null) {
                        if (castDateStr == null || castDateStr.isBlank() || "-".equals(castDateStr) || "N/A".equalsIgnoreCase(castDateStr)) {
                            if (pd.getCastingDate() != null) {
                                castDateStr = pd.getCastingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            }
                        }
                        if (totalCasted == null || totalCasted <= 0) {
                            try {
                                Long count = batchCountCache.get(pd.getId());
                                if (count != null && count > 0) {
                                    totalCasted = count.intValue();
                                } else if (pd.getTotalCastedSleepers() != null && pd.getTotalCastedSleepers() > 0) {
                                    totalCasted = pd.getTotalCastedSleepers();
                                }
                            } catch (Exception ignore) {
                                if (pd.getTotalCastedSleepers() != null && pd.getTotalCastedSleepers() > 0) {
                                    totalCasted = pd.getTotalCastedSleepers();
                                }
                            }
                        }
                    }

                    // Fallbacks
                    if (totalCasted == null || totalCasted <= 0) {
                        totalCasted = goodCount + badCount;
                    }
                    if (castDateStr == null || castDateStr.isBlank() || "-".equals(castDateStr) || "N/A".equalsIgnoreCase(castDateStr)) {
                        castDateStr = sleeperCall.getCreatedAt() != null ? sleeperCall.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
                    }

                    // Look up ET sleepers from preloaded map (with deduplication)
                    java.util.Set<String> etSleepersDistinct = new java.util.LinkedHashSet<>();
                    if (etSleepersByBatch.containsKey(rawBatchNo)) {
                        etSleepersDistinct.addAll(etSleepersByBatch.get(rawBatchNo));
                    } else if (etSleepersByBatch.containsKey(cleanBatch)) {
                        etSleepersDistinct.addAll(etSleepersByBatch.get(cleanBatch));
                    }
                    List<String> etSleepersList = new java.util.ArrayList<>(etSleepersDistinct);
                    String etNoStr = String.join(", ", etSleepersList);
                    int etCount = etSleepersList.size();

                    int notOffered = Math.max(0, totalCasted - goodCount - badCount);

                    List<com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult> batchResults = java.util.Collections.emptyList();
                    if (pd != null && pd.getId() != null) {
                        batchResults = resultsByBatchId.getOrDefault(pd.getId(), java.util.Collections.emptyList());
                    }

                    // Format bad sleepers with bracket abbreviations e.g. 1A (SD), 4A (RSD), 10D (OGL)
                    List<String> formattedBadSleepersList = new java.util.ArrayList<>();
                    if (batch.getBadSleepers() != null) {
                        for (com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail s : batch.getBadSleepers()) {
                            if (s == null || s.getSleeperNo() == null || s.getSleeperNo().isBlank()) continue;
                            String sNo = s.getSleeperNo().trim();
                            if (sNo.contains("(") && sNo.contains(")")) {
                                formattedBadSleepersList.add(sNo);
                                continue;
                            }
                            String cleanSNo = sNo.replaceAll("\\s+", "").toLowerCase();
                            DefectDetail foundDd = defectByBatchAndSleeper.get(cleanBatch.toLowerCase() + ":" + cleanSNo);
                            if (foundDd == null) foundDd = defectByBatchAndSleeper.get(rawBatchNo.toLowerCase() + ":" + cleanSNo);
                            if (foundDd == null) foundDd = defectByBatchAndSleeper.get(cleanSNo);

                            String abbr = "SD";
                            if (foundDd != null) {
                                abbr = mapToAbbreviation(foundDd.reason, foundDd.moduleId);
                            } else if (!batchResults.isEmpty()) {
                                for (com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult tr : batchResults) {
                                    if ("REJECTED".equalsIgnoreCase(tr.getResult())) {
                                        boolean matchId = s.getSleeperId() != null && s.getSleeperId().equals(tr.getSleeperId());
                                        boolean matchNo = tr.getSleeperNo() != null && tr.getSleeperNo().replaceAll("\\s+", "").equalsIgnoreCase(cleanSNo);
                                        if (matchId || matchNo) {
                                            Long modId = tr.getModuleId();
                                            if (modId == null && tr.getTestHeader() != null && tr.getTestHeader().getModule() != null) {
                                                modId = tr.getTestHeader().getModule().getId();
                                            }
                                            abbr = mapToAbbreviation(tr.getRejectionReason(), modId);
                                            break;
                                        }
                                    }
                                }
                            }
                            formattedBadSleepersList.add(sNo + " (" + abbr + ")");
                        }
                    }
                    if (formattedBadSleepersList.isEmpty() && !rawBadSleepersList.isEmpty()) {
                        for (String sNo : rawBadSleepersList) {
                            if (sNo.contains("(") && sNo.contains(")")) {
                                formattedBadSleepersList.add(sNo);
                            } else {
                                String cleanSNo = sNo.replaceAll("\\s+", "").toLowerCase();
                                DefectDetail foundDd = defectByBatchAndSleeper.get(cleanBatch.toLowerCase() + ":" + cleanSNo);
                                if (foundDd == null) foundDd = defectByBatchAndSleeper.get(rawBatchNo.toLowerCase() + ":" + cleanSNo);
                                if (foundDd == null) foundDd = defectByBatchAndSleeper.get(cleanSNo);

                                String abbr = "SD";
                                if (foundDd != null) {
                                    abbr = mapToAbbreviation(foundDd.reason, foundDd.moduleId);
                                } else if (!batchResults.isEmpty()) {
                                    for (com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult tr : batchResults) {
                                        if ("REJECTED".equalsIgnoreCase(tr.getResult()) && tr.getSleeperNo() != null && tr.getSleeperNo().replaceAll("\\s+", "").equalsIgnoreCase(cleanSNo)) {
                                            Long modId = tr.getModuleId();
                                            if (modId == null && tr.getTestHeader() != null && tr.getTestHeader().getModule() != null) {
                                                modId = tr.getTestHeader().getModule().getId();
                                            }
                                            abbr = mapToAbbreviation(tr.getRejectionReason(), modId);
                                            break;
                                        }
                                    }
                                }
                                formattedBadSleepersList.add(sNo + " (" + abbr + ")");
                            }
                        }
                    }
                    String rejNoStr = String.join(", ", formattedBadSleepersList);

                    // Dynamic classification of rejections by defect type (Surf, Dim, Oth, SBT)
                    int rejSurf = 0;
                    int rejDim = 0;
                    int rejOth = 0;
                    int rejSbt = 0;

                    for (String item : formattedBadSleepersList) {
                        String abbrStr = "";
                        int start = item.indexOf("(");
                        int end = item.indexOf(")");
                        if (start != -1 && end > start) {
                            abbrStr = item.substring(start + 1, end).trim();
                        }
                        String cat = classifyRejectionCategory(item, abbrStr, null);
                        if ("sbt".equalsIgnoreCase(cat)) {
                            rejSbt++;
                        } else if ("dim".equalsIgnoreCase(cat)) {
                            rejDim++;
                        } else if ("surf".equalsIgnoreCase(cat)) {
                            rejSurf++;
                        } else {
                            rejOth++;
                        }
                    }

                    int totalClassified = rejSurf + rejDim + rejOth + rejSbt;
                    if (badCount > 0 && totalClassified < badCount) {
                        rejOth += (badCount - totalClassified);
                    }

                    // Look up MF (Moment of Failure) sleepers from preloaded map (with deduplication)
                    java.util.Set<String> mfSleepersDistinct = new java.util.LinkedHashSet<>();
                    if (mfSleepersByBatch.containsKey(rawBatchNo)) {
                        mfSleepersDistinct.addAll(mfSleepersByBatch.get(rawBatchNo));
                    } else if (mfSleepersByBatch.containsKey(cleanBatch)) {
                        mfSleepersDistinct.addAll(mfSleepersByBatch.get(cleanBatch));
                    }
                    List<String> mfSleepersList = new java.util.ArrayList<>(mfSleepersDistinct);
                    String mfNoStr = String.join(", ", mfSleepersList);
                    int mftCount = mfSleepersList.size();
                    int mftAccepted = mftCount;

                    int normAccepted = Math.max(0, goodCount - etCount - mftAccepted);
                    int etAccepted = etCount;

                    // Populate HeatDetail
                    CallLetterDetailsDto.HeatDetail hd = new CallLetterDetailsDto.HeatDetail();
                    hd.setHeatNo(displayBatchNo);
                    hd.setTcNo("Accepted: " + goodCount + (badCount > 0 ? " | Rejected: " + badCount : ""));
                    int totalBatchOffered = goodCount + badCount;
                    hd.setQtyOffered(String.valueOf(totalBatchOffered));
                    hd.setCastDate(castDateStr);
                    hd.setTotalCasted(totalCasted);
                    hd.setPreviouslyOffered(prevOffered);
                    hd.setGoodCount(goodCount);
                    hd.setBadCount(badCount);
                    hd.setGoodSleepers(goodSleepersList);
                    hd.setBadSleepers(formattedBadSleepersList);
                    hd.setEtSleepers(etSleepersList);
                    hd.setRejNo(rejNoStr);
                    hd.setEtNo(etNoStr);
                    hd.setMfNo(mfNoStr);
                    hd.setNormAccepted(normAccepted);
                    hd.setEtAccepted(etAccepted);
                    hd.setMftAccepted(mftAccepted);
                    hd.setRejSurf(rejSurf);
                    hd.setRejDim(rejDim);
                    hd.setRejOth(rejOth);
                    hd.setRejSbt(rejSbt);
                    hd.setNotOffered(notOffered);
                    heatDetailsList.add(hd);

                    // Populate SleeperBatchDetail
                    CallLetterDetailsDto.SleeperBatchDetail sbd = new CallLetterDetailsDto.SleeperBatchDetail();
                    sbd.setBatchNo(displayBatchNo);
                    sbd.setCastDate(castDateStr);
                    sbd.setTotalCasted(totalCasted);
                    sbd.setPreviouslyOffered(prevOffered);
                    sbd.setGoodSleepers(goodCount);
                    sbd.setBadSleepers(badCount);
                    sbd.setGoodSleepersList(goodSleepersList);
                    sbd.setBadSleepersList(formattedBadSleepersList);
                    sbd.setEtSleepers(etSleepersList);
                    sbd.setRejNo(rejNoStr);
                    sbd.setEtNo(etNoStr);
                    sbd.setMfNo(mfNoStr);
                    sbd.setNormAccepted(normAccepted);
                    sbd.setEtAccepted(etAccepted);
                    sbd.setMftAccepted(mftAccepted);
                    sbd.setRejSurf(rejSurf);
                    sbd.setRejDim(rejDim);
                    sbd.setRejOth(rejOth);
                    sbd.setRejSbt(rejSbt);
                    sbd.setNotOffered(notOffered);
                    batchesList.add(sbd);
                }
                dto.setHeatDetails(heatDetailsList);
                dto.setBatchesSelected(batchesList);

                if (!"Set".equalsIgnoreCase(dto.getCallUnit())) {
                    int totalSleepersAcceptedAndRejected = 0;
                    for (CallLetterDetailsDto.HeatDetail hd : heatDetailsList) {
                        int g = hd.getGoodCount() != null ? hd.getGoodCount() : 0;
                        int b = hd.getBadCount() != null ? hd.getBadCount() : 0;
                        totalSleepersAcceptedAndRejected += (g + b);
                    }
                    if (totalSleepersAcceptedAndRejected > 0) {
                        dto.setCallQty(String.valueOf(totalSleepersAcceptedAndRejected));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing batches for sleeper call: {}", sleeperCall.getCallNo(), e);
        }

        // Calculate cumulative passed quantity
        try {
            List<com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall> allCalls = sleeperInspectionCallRepository.getCalls(rawPoNo, rawSrNo);
            int passedQty = 0;
            if (allCalls != null) {
                for (com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall c : allCalls) {
                    if ("Accepted".equalsIgnoreCase(c.getStatus()) || "Completed".equalsIgnoreCase(c.getStatus()) || "Verified".equalsIgnoreCase(c.getStatus())) {
                        passedQty += (c.getTotalOffered() != null ? c.getTotalOffered() : 0);
                    }
                }
            }
            dto.setFinalAcceptedQty(passedQty > 0 ? passedQty + " Nos." : "0 Nos.");
            dto.setRawMaterialQtyPassed("N/A");
        } catch (Exception e) {
            logger.error("Error calculating sleeper cumulative quantities", e);
        }

        return dto;
    }

    private String mapToAbbreviation(String reason, Long moduleId) {
        if (reason != null && !reason.isBlank()) {
            String r = reason.toUpperCase().trim();
            if (r.contains("TIGHT") && r.contains("SEAT")) return "RST";
            if (r.contains("LOOSE") && r.contains("SEAT")) return "RSL";
            if (r.contains("SEAT") && (r.contains("DEFECT") || r.contains("DAMAGE"))) return "RSD";
            if (r.contains("TOE") && r.contains("GAP") && r.contains("LOOSE")) return "TGL";
            if (r.contains("TOE") && r.contains("GAP") && r.contains("TIGHT")) return "TGT";
            if (r.contains("INSERT") && r.contains("TILT")) return "IT";
            if (r.contains("INSERT") && (r.contains("OUT") || r.contains("MISSING"))) return "IO";
            if (r.contains("INSERT") && r.contains("SINK")) return "IS";
            if (r.contains("OUTER") && r.contains("GAUGE") && (r.contains("LOOSE") || r.contains("+"))) return "OGL";
            if (r.contains("OUTER") && r.contains("GAUGE") && (r.contains("TIGHT") || r.contains("-"))) return "OGT";
            if (r.contains("OUTER") && r.contains("GAUGE")) return "OGL";
            if (r.contains("END") && (r.contains("BROKEN") || r.contains("BREAK"))) return "EB";
            if (r.contains("END") && (r.contains("DAMAGE") || r.contains("DAMAGED"))) return "ED";
            if (r.contains("FTC") || r.contains("TRACK CIRCUIT") || r.contains("NFTC")) return "NFTC";
            if (r.contains("END") && r.contains("HONEY")) return "EHC";
            if (r.contains("SURFACE") && r.contains("HONEY")) return "SHC";
            if (r.contains("HONEY")) return "SHC";
            if (r.contains("CRACK") || r.contains("RC")) return "RC";
            if (r.contains("DAMAGE") || r.contains("DEMOULD") || r.contains("RD")) return "RD";
            if (r.contains("GAUGE") || r.contains("DIMENSION") || r.contains("DIM")) return "RSD";
            if (r.contains("SURFACE") || r.contains("VISUAL") || r.contains("SD")) return "SD";
            if (r.contains("FAILURE") || r.contains("MF")) return "MF";
            if (r.contains("EPOXY") || r.contains("ET")) return "ET";
        }
        if (moduleId != null) {
            if (moduleId == 6L) return "MF";
            if (moduleId == 1L) return "SD";
            if (moduleId == 2L || moduleId == 3L) return "RSD";
            if (moduleId == 4L) return "RD";
        }
        return "SD";
    }

    private String classifyRejectionCategory(String reason, String abbr, Long moduleId) {
        String a = abbr != null ? abbr.toUpperCase().trim() : "";
        String r = reason != null ? reason.toUpperCase().trim() : "";

        // 1. SBT (Moment of Resistance / Static Bending Test / Moment of Failure)
        if ("MF".equals(a) || "SBT".equals(a) || Long.valueOf(6L).equals(moduleId) ||
            r.contains("MOR") || r.contains("STATIC BEND") || r.contains("SBT") || r.contains("FAILURE") || r.contains("MOMENT OF")) {
            return "sbt";
        }

        // 2. Dim (Dimensional Rejection)
        if (java.util.List.of("OGL", "OGT", "RSD", "RSL", "RST", "TGL", "TGT", "RG").contains(a) ||
            Long.valueOf(2L).equals(moduleId) || Long.valueOf(3L).equals(moduleId) ||
            r.contains("DIMENSION") || r.contains("GAUGE") || r.contains("SEAT") || r.contains("TOE GAP") || r.contains("OUTER GAUGE")) {
            return "dim";
        }

        // 3. Surf (Surface Defect Rejection)
        if (java.util.List.of("SD", "SHC", "EHC", "RC", "RD").contains(a) ||
            Long.valueOf(1L).equals(moduleId) || Long.valueOf(4L).equals(moduleId) ||
            r.contains("SURFACE") || r.contains("VISUAL") || r.contains("HONEY") || r.contains("CRACK") || r.contains("DEMOULD") || r.contains("DAMAGE")) {
            return "surf";
        }

        return "oth";
    }

    private String resolveSleeperCaseNo(String rawCaseNo, String rio) {
        if (rawCaseNo == null || rawCaseNo.trim().isEmpty()) {
            return null;
        }
        String trimmedCaseNo = rawCaseNo.trim();
        String[] parts = trimmedCaseNo.split(",");

        if (rio != null && !rio.trim().isEmpty()) {
            String cleanRio = rio.trim().toUpperCase();
            String firstLetter = cleanRio.substring(0, 1);
            for (String part : parts) {
                String p = part.trim();
                if (p.toUpperCase().startsWith(firstLetter)) {
                    return p;
                }
            }
        }

        for (String part : parts) {
            String p = part.trim();
            if (!p.isEmpty()) {
                return p;
            }
        }
        return parts[0].trim();
    }

    private String cleanMfSleeperNo(String s) {
        if (s == null || s.isBlank()) return "";
        String trimmed = s.trim();
        if (trimmed.contains("+")) {
            String[] parts = trimmed.split("\\+");
            String bench = "";
            String mould = "";
            for (String p : parts) {
                String clean = p.trim();
                if (clean.matches("(?i)^shed.*")) continue;
                if (clean.matches("^\\d+$")) bench = clean;
                else if (clean.matches("^[a-zA-Z]$")) mould = clean.toUpperCase();
                else if (clean.matches("^\\d+[a-zA-Z]+$")) return clean.toUpperCase();
            }
            if (!bench.isEmpty() || !mould.isEmpty()) return bench + mould;
        }
        return trimmed;
    }
}
