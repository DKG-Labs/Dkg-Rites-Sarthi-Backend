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


    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public CallLetterDetailsDto getCallLetterDetails(String requestId) {
        logger.info("Fetching call letter details for requestId: {}", requestId);

        CallLetterDetailsDto dto = new CallLetterDetailsDto();
        dto.setRequestId(requestId);

        // -------------------------------------------------------
        // 1. Fetch the core inspection call row
        // -------------------------------------------------------
        Optional<InspectionCall> icOpt = inspectionCallRepository.findByIcNumber(requestId);
        if (icOpt.isEmpty()) {
            logger.warn("No InspectionCall found for requestId: {}", requestId);
            return dto;
        }
        InspectionCall ic = icOpt.get();

        dto.setTypeOfCall(ic.getTypeOfCall());
        dto.setProductType(ic.getTypeOfCall());
        dto.setPlaceOfInspection(ic.getPlaceOfInspection());
        dto.setDesiredInspectionDate(
                ic.getDesiredInspectionDate() != null ? ic.getDesiredInspectionDate().toString() : null);
        dto.setOfferedInstallmentNo(ic.getIcNumber());

        // Fetch contact details from UserMaster based on vendorId
        if (ic.getVendorId() != null) {
            Optional<UserMaster> vendorUserOpt = userMasterRepository.findFirstByUserName(ic.getVendorId());
            if (vendorUserOpt.isPresent()) {
                UserMaster vendorUser = vendorUserOpt.get();
                dto.setContactPersonName(vendorUser.getFullName());
                dto.setContactMobile(vendorUser.getMobileNumber());
                dto.setContactEmail(vendorUser.getEmail());
            }
        }

        // Raw PO number stored on the IC (e.g. "26255265205057")
        String rawPoNo = ic.getPoNo();

        // poSerialNo may be stored as a composite like "26255265205057 / 012" — extract the last part only
        String rawPoSerialNo = ic.getPoSerialNo();
        String poSerialNo = null;
        if (rawPoSerialNo != null) {
            String[] parts = rawPoSerialNo.split("/");
            poSerialNo = parts[parts.length - 1].trim();  // take last segment, e.g. "012"
        }
        logger.info("Resolved poNo: {}, poSerialNo: {} (raw: {})", rawPoNo, poSerialNo, rawPoSerialNo);

        // -------------------------------------------------------
        // 2. Fetch PO Header
        // -------------------------------------------------------
        if (rawPoNo != null) {
            Optional<PoHeader> phOpt = poHeaderRepository.findByPoNo(rawPoNo);
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
        }

        // -------------------------------------------------------
        // 3. Fetch matching PO Item
        // -------------------------------------------------------
        if (rawPoNo != null && poSerialNo != null) {
            Optional<PoItem> piOpt = poItemRepository.findByPoHeader_PoNoAndItemSrNo(rawPoNo, poSerialNo);
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
        }

        // -------------------------------------------------------
        // 4. Calculate cumulative quantities passed (Raw Material & Final)
        // -------------------------------------------------------
        if (rawPoNo != null && poSerialNo != null) {
            try {
                // Fetch only required columns (icNumber, poSerialNo) to avoid N+1 query loading child entities
                List<Object[]> results = inspectionCallRepository.findIcNumbersAndSerialNumbersByPoNo(rawPoNo);
                final String targetSerialNo = poSerialNo;
                List<String> callNos = results.stream()
                        .filter(row -> {
                            String cRawSerial = (String) row[1];
                            if (cRawSerial == null) return false;
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
                dto.setRawMaterialQtyPassed(totalRmAccepted.setScale(3, java.math.RoundingMode.HALF_UP).toPlainString() + " MT");

                // Calculate Final Accepted Qty (sum of qtyNowPassed from FinalCumulativeResults)
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
                logger.error("Error calculating cumulative passed quantities for PO: {}, Serial: {}", rawPoNo, poSerialNo, e);
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
        if (callType == null) callType = "";

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
        if (rm == null) return;

        // Item description from RM details takes priority if PO item not available
        if (dto.getItemDesc() == null && rm.getItemDescription() != null) {
            dto.setItemDesc(rm.getItemDescription());
        }

        // Call quantity
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
        } else if (rm.getOfferedQtyErc() != null) {
            dto.setCallQty(rm.getOfferedQtyErc().toString());
            dto.setCallUnit(rm.getUnitOfMeasurement() != null ? rm.getUnitOfMeasurement() : "Nos.");
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
        if (rly != null && !rly.isBlank()) sb.append(rly).append(" / ");
        if (poNo != null && !poNo.isBlank()) sb.append(poNo);
        if (srNo != null && !srNo.isBlank()) sb.append(" / ").append(srNo);
        return sb.toString();
    }
}
