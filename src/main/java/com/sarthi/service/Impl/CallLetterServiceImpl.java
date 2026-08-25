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
            Optional<com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall> sleeperOpt = sleeperInspectionCallRepository.findByCallNo(requestId);
            if (sleeperOpt.isPresent()) {
                return enrichFromSleeperCall(sleeperOpt.get(), dto);
            }

            logger.warn("No InspectionCall or SleeperInspectionCall found for requestId: {}", requestId);
            return dto;
        }
        InspectionCall ic = icOpt.get();

        dto.setTypeOfCall(ic.getTypeOfCall());
        dto.setProductType(ic.getTypeOfCall());

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
                    dto.setConsigneeDetail(pi.getConsigneeDetail());
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

    private CallLetterDetailsDto enrichFromSleeperCall(com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall sleeperCall, CallLetterDetailsDto dto) {
        dto.setRequestId(sleeperCall.getCallNo());
        dto.setTypeOfCall("Final Inspection");
        dto.setProductType("Prestressed Concrete Sleepers (" + (sleeperCall.getSleeperType() != null ? sleeperCall.getSleeperType() : "") + ")");
        dto.setCallQty(String.valueOf(sleeperCall.getTotalOffered() != null ? sleeperCall.getTotalOffered() : 0));
        dto.setCallUnit("Nos.");
        dto.setOfferedInstallmentNo(sleeperCall.getCallNo());

        if (sleeperCall.getDesiredInspectionDate() != null) {
            dto.setDesiredInspectionDate(sleeperCall.getDesiredInspectionDate().format(DATE_FMT));
        } else if (sleeperCall.getCreatedAt() != null) {
            dto.setDesiredInspectionDate(sleeperCall.getCreatedAt().format(DATE_FMT));
        }

        // Contact info from creator
        if (sleeperCall.getCreatedBy() != null) {
            try {
                Optional<UserMaster> userOpt = userMasterRepository.findById(sleeperCall.getCreatedBy().intValue());
                if (userOpt.isPresent()) {
                    UserMaster u = userOpt.get();
                    dto.setContactPersonName(u.getFullName() != null ? u.getFullName() : u.getUsername());
                    dto.setContactMobile(u.getMobileNumber());
                    dto.setContactEmail(u.getEmail());
                }
            } catch (Exception e) {
                logger.error("Error looking up user for sleeper call createdBy: {}", sleeperCall.getCreatedBy(), e);
            }
        }

        // RIO info from workflow
        try {
            com.sarthi.entity.WorkflowTransition wt = workflowTransitionRepository
                    .findFirstByRequestIdAndRioIsNotNullOrderByWorkflowTransitionIdDesc(sleeperCall.getCallNo());
            if (wt != null && wt.getRio() != null) {
                dto.setRio(wt.getRio());
            }
        } catch (Exception e) {
            logger.error("Error fetching RIO for sleeper call: {}", sleeperCall.getCallNo(), e);
        }

        // IE Details from workflow
        try {
            List<com.sarthi.entity.WorkflowTransition> transitions = workflowTransitionRepository
                    .findByRequestIdOrderByWorkflowTransitionIdDesc(sleeperCall.getCallNo());
            if (transitions != null) {
                for (com.sarthi.entity.WorkflowTransition transition : transitions) {
                    Integer ieUserId = transition.getAssignedToUser() != null ? transition.getAssignedToUser() : transition.getProcessIeUserId();
                    if (ieUserId != null) {
                        Optional<UserMaster> ieUserOpt = userMasterRepository.findById(ieUserId);
                        if (ieUserOpt.isPresent()) {
                            UserMaster ieUser = ieUserOpt.get();
                            dto.setIeName(ieUser.getFullName() != null ? ieUser.getFullName() : ieUser.getUsername());
                            dto.setIeMobile(ieUser.getMobileNumber());
                            break;
                        }
                    }
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
                    if (ph.getFirmDetails() != null) {
                        dto.setVendorName(ph.getFirmDetails());
                        dto.setManufacturerName(ph.getFirmDetails());
                    }
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
                    dto.setUom(pi.getUom() != null ? pi.getUom() : "Nos.");
                    dto.setConsigneeDetail(pi.getConsigneeDetail());
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

        // Batches to heatDetails
        if (sleeperCall.getBatchesSelected() != null && !sleeperCall.getBatchesSelected().isEmpty()) {
            List<CallLetterDetailsDto.HeatDetail> heatDetailsList = new java.util.ArrayList<>();
            for (com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch batch : sleeperCall.getBatchesSelected()) {
                CallLetterDetailsDto.HeatDetail hd = new CallLetterDetailsDto.HeatDetail();
                hd.setHeatNo("Batch " + (batch.getBatchNo() != null ? batch.getBatchNo() : "-"));
                int goodCount = batch.getGoodSleepers() != null ? batch.getGoodSleepers().size() : 0;
                int badCount = batch.getBadSleepers() != null ? batch.getBadSleepers().size() : 0;
                hd.setTcNo("Good: " + goodCount + (badCount > 0 ? " | Rejected: " + badCount : ""));
                hd.setQtyOffered(String.valueOf(goodCount));
                heatDetailsList.add(hd);
            }
            dto.setHeatDetails(heatDetailsList);
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
}
