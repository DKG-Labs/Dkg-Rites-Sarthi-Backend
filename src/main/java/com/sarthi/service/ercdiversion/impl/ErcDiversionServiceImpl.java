package com.sarthi.service.ercdiversion.impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.rawmaterial.RmChemicalAnalysisDto;
import com.sarthi.dto.rawmaterial.RmHeatQuantityDto;
import com.sarthi.dto.ercdiversion.*;
import com.sarthi.entity.InspectionCompleteDetails;
import com.sarthi.entity.WorkflowTransition;
import com.sarthi.entity.ercdiversion.*;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.enums.BasketStatus;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import com.sarthi.entity.certificate.CertificateStorage;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.InspectionCompleteDetailsRepository;
import com.sarthi.repository.WorkflowTransitionRepository;
import com.sarthi.repository.ercdiversion.*;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository;
import com.sarthi.service.ercdiversion.ErcDiversionService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.service.rawmaterial.RawMaterialInspectionService;
import com.sarthi.util.IcNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErcDiversionServiceImpl implements ErcDiversionService {

    private final ErcDiversionRequestRepository requestRepository;
    private final ErcDiversionItemRepository itemRepository;
    private final ErcDivertedPassedBasketRepository basketRepository;
    private final ErcDiversionWorkflowHistoryRepository historyRepository;
    private final ErcDiversionCallAllocationRepository allocationRepository;
    private final InspectionCallRepository inspectionCallRepository;
    private final InspectionCompleteDetailsRepository completeDetailsRepository;
    private final RmInspectionDetailsRepository rmDetailsRepository;
    private final RmHeatQuantityRepository heatQuantityRepository;
    private final RawMaterialInspectionService rawMaterialService;
    private final FinalInspectionLotDetailsRepository finalLotRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final IcNumberGenerator icNumberGenerator;
    private final UserMasterRepository userMasterRepository;
    private final CertificateStorageRepository certificateStorageRepository;
    private final ProcessLineFinalResultRepository processLineFinalResultRepository;

    private static final int DIVERSION_WORKFLOW_ID = 4;
    private static final String ROLE_VENDOR = "1";
    private static final String ROLE_CM = "5";
    private static final String ROLE_SBU_HEAD = "8";

    @Override
    @Transactional(readOnly = true)
    public List<SourceIcEligibilityDto> getEligibleSourceIcs(String vendorCode, String plantId, DiversionStage stage) {
        log.info("Fetching eligible source ICs for vendor: {}, plantId: {}, stage: {}", vendorCode, plantId, stage);

        if (stage == null) {
            return Collections.emptyList();
        }

        String cleanVendor = (vendorCode != null && !vendorCode.trim().isEmpty()) ? vendorCode.trim() : null;
        String cleanPlant = (plantId != null && !plantId.trim().isEmpty()) ? plantId.trim() : null;

        List<Object[]> rows;
        if (stage == DiversionStage.RAW_MATERIAL) {
            rows = completeDetailsRepository.findEligibleRmSourceIcs(
                    cleanVendor,
                    cleanPlant
            );
        } else if (stage == DiversionStage.PROCESS) {
            rows = completeDetailsRepository.findEligibleProcessSourceIcs(
                    cleanVendor,
                    cleanPlant
            );
        } else if (stage == DiversionStage.FINAL) {
            rows = completeDetailsRepository.findEligibleFinalSourceIcs(
                    cleanVendor,
                    cleanPlant
            );
        } else {
            rows = Collections.emptyList();
        }

        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect all certificate numbers to batch-fetch diverted quantities in 1 query
        List<String> certNos = rows.stream()
                .map(r -> (String) r[0])
                .filter(c -> c != null && !c.trim().isEmpty())
                .collect(Collectors.toList());

        Map<String, BigDecimal> divertedMap = new HashMap<>();
        if (!certNos.isEmpty()) {
            List<Object[]> divertedList = itemRepository.sumDivertedQuantitiesBySourceIcNos(certNos);
            for (Object[] div : divertedList) {
                String cNo = (String) div[0];
                BigDecimal dQty = div[1] != null ? (BigDecimal) div[1] : BigDecimal.ZERO;
                divertedMap.put(cNo, dQty);
            }
        }

        // Exclude ICs whose source PO SR no has been COMPLETELY diverted
        Set<String> completeDivSrNos = cleanVendor != null
                ? new HashSet<>(requestRepository.findCompleteSourceSrNos(cleanVendor))
                : Collections.emptySet();

        List<SourceIcEligibilityDto> eligibleList = new ArrayList<>();
        for (Object[] row : rows) {
            String certNo = (String) row[0];
            String callNo = (String) row[1];
            String poNo = (String) row[2];
            String poSerialNo = (String) row[3];

            if (poSerialNo != null && completeDivSrNos.contains(poSerialNo)) {
                continue; // skip — material for this PO SR no has been completely diverted
            }
            LocalDate icDate = null;
            if (row[4] instanceof java.sql.Date) {
                icDate = ((java.sql.Date) row[4]).toLocalDate();
            } else if (row[4] instanceof LocalDate) {
                icDate = (LocalDate) row[4];
            } else if (row[4] != null) {
                try {
                    icDate = LocalDate.parse(row[4].toString());
                } catch (Exception ignored) {}
            }
            if (icDate == null) icDate = LocalDate.now();

            BigDecimal totalAccepted = BigDecimal.ZERO;
            if (row[5] instanceof Number) {
                totalAccepted = BigDecimal.valueOf(((Number) row[5]).doubleValue());
            }

            BigDecimal alreadyDiverted = divertedMap.getOrDefault(certNo, BigDecimal.ZERO);
            BigDecimal available = totalAccepted.subtract(alreadyDiverted);

            String ieName = row.length > 6 && row[6] != null ? row[6].toString() : null;
            String certDocUrl = row.length > 7 && row[7] != null ? row[7].toString() : null;
            String ieUserId = row.length > 8 && row[8] != null ? row[8].toString() : null;

            if (certDocUrl == null || certDocUrl.trim().isEmpty()) {
                certDocUrl = certificateStorageRepository.findAllByCallNumber(callNo).stream()
                        .map(CertificateStorage::getBlobUrl)
                        .filter(url -> url != null && !url.trim().isEmpty())
                        .findFirst()
                        .orElseGet(() -> certificateStorageRepository.findAllByCallNumber(certNo).stream()
                                .map(CertificateStorage::getBlobUrl)
                                .filter(url -> url != null && !url.trim().isEmpty())
                                .findFirst()
                                .orElse(null));
            }

            if (available.compareTo(BigDecimal.ZERO) > 0) {
                eligibleList.add(SourceIcEligibilityDto.builder()
                        .certificateNo(certNo)
                        .callNo(callNo)
                        .poNo(poNo)
                        .poSerialNo(poSerialNo)
                        .icDate(icDate)
                        .totalAcceptedQty(totalAccepted)
                        .totalConsumedQty(BigDecimal.ZERO)
                        .totalDivertedQty(alreadyDiverted)
                        .availableBalanceQty(available)
                        .unitOfMeasurement(stage == DiversionStage.RAW_MATERIAL ? "MT" : "NOS")
                        .ieName(ieName)
                        .ieUserId(ieUserId)
                        .certDocUrl(certDocUrl)
                        .build());
            }
        }

        return eligibleList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SourceIcLineItemDto> getSourceIcLineItems(String icNo, DiversionStage stage) {
        log.info("Fetching line items for source IC: {}, stage: {}", icNo, stage);
        String callNo = extractCallNo(icNo);
        List<SourceIcLineItemDto> lineItems = new ArrayList<>();

        if (stage == DiversionStage.RAW_MATERIAL) {
            List<RmHeatQuantityDto> heats = rawMaterialService.getHeatNumbersByRmIcNumber(callNo);
            for (RmHeatQuantityDto h : heats) {
                BigDecimal accepted = BigDecimal.ZERO;
                if (h.getWeightAcceptedMt() != null) {
                    accepted = BigDecimal.valueOf(h.getWeightAcceptedMt());
                } else if (h.getQtyAccepted() != null && !h.getQtyAccepted().equals("null")) {
                    try {
                        accepted = new BigDecimal(h.getQtyAccepted());
                    } catch (Exception ignored) {}
                }

                BigDecimal diverted = itemRepository.sumDivertedQuantityForSource(icNo, h.getHeatNumber(), h.getTcNumber(), null);
                BigDecimal available = accepted.subtract(diverted);
                if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

                lineItems.add(SourceIcLineItemDto.builder()
                        .heatNo(h.getHeatNumber())
                        .tcNo(h.getTcNumber())
                        .acceptedQty(accepted)
                        .downstreamConsumedQty(BigDecimal.ZERO)
                        .previouslyDivertedQty(diverted)
                        .availableBalanceQty(available)
                        .unitOfMeasurement("MT")
                        .build());
            }
        } else if (stage == DiversionStage.PROCESS) {
            InspectionCall call = inspectionCallRepository.findByIcNumber(callNo).orElse(null);
            if (call != null && call.getProcessInspectionDetails() != null && !call.getProcessInspectionDetails().isEmpty()) {
                for (com.sarthi.entity.processmaterial.ProcessInspectionDetails pid : call.getProcessInspectionDetails()) {
                    String lotNo = pid.getLotNumber();
                    String heatNo = pid.getHeatNumber();

                    // Canonical Process IC calculation (shearing_manufactured - total_rejected)
                    Integer procAccepted = processLineFinalResultRepository
                            .sumAcceptedQtyByCallNoAndLotNumberAndHeatNo(callNo, lotNo, heatNo);

                    int accQty;
                    if (procAccepted != null && procAccepted > 0) {
                        accQty = procAccepted;
                    } else {
                        accQty = pid.getQtyAccepted() != null ? pid.getQtyAccepted() : (pid.getOfferedQty() != null ? pid.getOfferedQty() : 0);
                    }
                    BigDecimal accepted = BigDecimal.valueOf(accQty);

                    // Downstream consumed: Offered earlier in previous Final inspection calls for this heat & lot & source PO
                    Integer offeredInFinal = finalLotRepository.sumOfferedQtyByHeatNumberAndLotNumberAndPo(
                            heatNo, lotNo, call.getPoNo(), call.getPoSerialNo());
                    BigDecimal downstreamConsumed = offeredInFinal != null ? BigDecimal.valueOf(offeredInFinal) : BigDecimal.ZERO;

                    // Previously approved diverted quantity for this source lot
                    BigDecimal diverted = itemRepository.sumDivertedQuantityForSource(icNo, heatNo, null, lotNo);
                    if (diverted == null) diverted = BigDecimal.ZERO;

                    // Available Balance = Accepted in Process - Downstream Consumed in Final - Previously Diverted
                    BigDecimal available = accepted.subtract(downstreamConsumed).subtract(diverted);
                    if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

                    lineItems.add(SourceIcLineItemDto.builder()
                            .lotNo(lotNo)
                            .heatNo(heatNo)
                            .acceptedQty(accepted)
                            .downstreamConsumedQty(downstreamConsumed)
                            .previouslyDivertedQty(diverted)
                            .availableBalanceQty(available)
                            .unitOfMeasurement("NOS")
                            .build());
                }
            } else {
                // Fallback: If processInspectionDetails is empty, load lots directly from process_line_final_result
                List<com.sarthi.entity.processmaterial.ProcessLineFinalResult> pfrList =
                        processLineFinalResultRepository.findByInspectionCallNo(callNo);
                if (pfrList != null && !pfrList.isEmpty()) {
                    Set<String> seenLotHeats = new HashSet<>();
                    for (com.sarthi.entity.processmaterial.ProcessLineFinalResult pfr : pfrList) {
                        String lotNo = pfr.getLotNumber();
                        String heatNo = pfr.getHeatNumber();
                        String lotKey = (lotNo != null ? lotNo : "") + "_" + (heatNo != null ? heatNo : "");
                        if (!seenLotHeats.add(lotKey)) continue;

                        Integer procAccepted = processLineFinalResultRepository
                                .sumAcceptedQtyByCallNoAndLotNumberAndHeatNo(callNo, lotNo, heatNo);
                        int accQty = procAccepted != null ? procAccepted : 0;
                        BigDecimal accepted = BigDecimal.valueOf(accQty);

                        String poNo = call != null ? call.getPoNo() : pfr.getPoNo();
                        String poSerialNo = call != null ? call.getPoSerialNo() : null;
                        Integer offeredInFinal = finalLotRepository.sumOfferedQtyByHeatNumberAndLotNumberAndPo(
                                heatNo, lotNo, poNo, poSerialNo);
                        BigDecimal downstreamConsumed = offeredInFinal != null ? BigDecimal.valueOf(offeredInFinal) : BigDecimal.ZERO;

                        BigDecimal diverted = itemRepository.sumDivertedQuantityForSource(icNo, heatNo, null, lotNo);
                        if (diverted == null) diverted = BigDecimal.ZERO;

                        BigDecimal available = accepted.subtract(downstreamConsumed).subtract(diverted);
                        if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

                        lineItems.add(SourceIcLineItemDto.builder()
                                .lotNo(lotNo)
                                .heatNo(heatNo)
                                .acceptedQty(accepted)
                                .downstreamConsumedQty(downstreamConsumed)
                                .previouslyDivertedQty(diverted)
                                .availableBalanceQty(available)
                                .unitOfMeasurement("NOS")
                                .build());
                    }
                }
            }
        } else if (stage == DiversionStage.FINAL) {
            InspectionCall call = inspectionCallRepository.findByIcNumber(callNo).orElse(null);
            if (call != null && call.getFinalInspectionDetails() != null) {
                Long finalDetailId = call.getFinalInspectionDetails().getId();
                List<FinalInspectionLotDetails> lotList = finalLotRepository.findByFinalDetailId(finalDetailId);
                if (lotList != null && !lotList.isEmpty()) {
                    for (FinalInspectionLotDetails fld : lotList) {
                        int accQty = fld.getQtyAccepted() != null ? fld.getQtyAccepted() : (fld.getOfferedQty() != null ? fld.getOfferedQty() : 0);
                        BigDecimal accepted = BigDecimal.valueOf(accQty);
                        BigDecimal diverted = itemRepository.sumDivertedQuantityForSource(icNo, fld.getHeatNumber(), null, fld.getLotNumber());
                        BigDecimal available = accepted.subtract(diverted);
                        if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

                        lineItems.add(SourceIcLineItemDto.builder()
                                .lotNo(fld.getLotNumber())
                                .heatNo(fld.getHeatNumber())
                                .acceptedQty(accepted)
                                .downstreamConsumedQty(BigDecimal.ZERO)
                                .previouslyDivertedQty(diverted)
                                .availableBalanceQty(available)
                                .unitOfMeasurement("NOS")
                                .build());
                    }
                } else {
                    int totalAcc = call.getFinalInspectionDetails().getTotalAcceptedQty() != null 
                            ? call.getFinalInspectionDetails().getTotalAcceptedQty() 
                            : (call.getFinalInspectionDetails().getTotalOfferedQty() != null ? call.getFinalInspectionDetails().getTotalOfferedQty() : 0);
                    BigDecimal accepted = BigDecimal.valueOf(totalAcc);
                    BigDecimal diverted = itemRepository.sumDivertedQuantityForSource(icNo, null, null, null);
                    BigDecimal available = accepted.subtract(diverted);
                    if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

                    lineItems.add(SourceIcLineItemDto.builder()
                            .lotNo("ALL")
                            .acceptedQty(accepted)
                            .downstreamConsumedQty(BigDecimal.ZERO)
                            .previouslyDivertedQty(diverted)
                            .availableBalanceQty(available)
                            .unitOfMeasurement("NOS")
                            .build());
                }
            }
        }

        return lineItems;
    }

    @Override
    @Transactional
    public DiversionRequestDto saveDraft(DiversionRequestDto dto) {
        log.info("Saving draft diversion request for vendor: {}", dto.getVendorCode());
        ErcDiversionRequest entity = mapDtoToEntity(dto);
        if (entity.getRequestNo() == null || entity.getRequestNo().trim().isEmpty()) {
            entity.setRequestNo(generateRequestNo(entity.getStage()));
        }
        if (entity.getDiversionNo() == null || entity.getDiversionNo().trim().isEmpty()) {
            entity.setDiversionNo(generateDiversionNo(entity.getRequestNo()));
        }
        entity.setStatus(DiversionRequestStatus.DRAFT);
        ErcDiversionRequest saved = requestRepository.save(entity);

        saveHistory(saved, "VENDOR", dto.getVendorCode(), dto.getVendorCode(), "DRAFT_SAVE", null, "DRAFT", "Draft saved by vendor");
        return mapEntityToDto(saved);
    }

    @Override
    @Transactional
    public DiversionRequestDto submitRequest(DiversionRequestDto dto) {
        log.info("Submitting diversion request for vendor: {}", dto.getVendorCode());

        // Validate quantities
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw createException(1001, "At least one material line item is required");
        }

        BigDecimal totalQty = BigDecimal.ZERO;
        for (DiversionItemDto item : dto.getItems()) {
            if (item.getDiversionQty() == null || item.getDiversionQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw createException(1002, "Diversion quantity must be greater than zero");
            }
            if (item.getAvailableBalanceQty() != null && item.getDiversionQty().compareTo(item.getAvailableBalanceQty()) > 0) {
                throw createException(1003, "Diversion quantity cannot exceed available balance for heat: " + item.getHeatNo());
            }
            totalQty = totalQty.add(item.getDiversionQty());
        }

        ErcDiversionRequest entity = mapDtoToEntity(dto);
        if (entity.getRequestNo() == null || entity.getRequestNo().trim().isEmpty()) {
            entity.setRequestNo(generateRequestNo(entity.getStage()));
        }
        if (entity.getDiversionNo() == null || entity.getDiversionNo().trim().isEmpty()) {
            entity.setDiversionNo(generateDiversionNo(entity.getRequestNo()));
        }
        entity.setTotalDiversionQty(totalQty);
        entity.setStatus(DiversionRequestStatus.PENDING_CM_APPROVAL);

        ErcDiversionRequest saved = requestRepository.save(entity);
        saveHistory(saved, "VENDOR", dto.getVendorCode(), dto.getVendorCode(), "SUBMIT_DIVERSION_REQUEST", "DRAFT", "PENDING_CM_APPROVAL", dto.getVendorRemarks());

        // Insert into main WORKFLOW_TRANSITION table
        saveWorkflowTransition(saved.getRequestNo(), "SUBMIT_DIVERSION_REQUEST", 1,
                ROLE_VENDOR, ROLE_CM, "VENDOR", "CONTROLLING_MANAGER",
                "PENDING_CM_APPROVAL", dto.getVendorRemarks(), dto.getVendorCode(),
                dto.getCmUserId(), saved.getRioId());

        return mapEntityToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DiversionRequestDto getRequestById(Long id) {
        ErcDiversionRequest req = requestRepository.findById(id)
                .orElseThrow(() -> createNotFoundException("Diversion request not found: " + id));
        return mapEntityToDto(req);
    }

    @Override
    @Transactional(readOnly = true)
    public DiversionRequestDto getRequestByRequestNo(String requestNo) {
        ErcDiversionRequest req = requestRepository.findByRequestNo(requestNo)
                .orElseThrow(() -> createNotFoundException("Diversion request not found: " + requestNo));
        return mapEntityToDto(req);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiversionRequestDto> getVendorRequests(String vendorCode, DiversionRequestStatus status) {
        return mapLightRows(requestRepository.findByVendorAndOptionalStatusLight(vendorCode, status));
    }

    private Map<Integer, UserMaster> buildUserMap(List<Object[]> rows) {
        List<Integer> ids = rows.stream()
                .map(r -> r[8] != null ? parseIntSafe(r[8].toString()) : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) return Collections.emptyMap();
        Map<Integer, UserMaster> map = new HashMap<>();
        userMasterRepository.findByUserIdIn(ids).forEach(u -> map.put(u.getUserId(), u));
        return map;
    }

    private Integer parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    private List<DiversionRequestDto> mapLightRows(List<Object[]> rows) {
        Map<Integer, UserMaster> userMap = buildUserMap(rows);
        return rows.stream().map(r -> mapLightRowToDto(r, userMap)).collect(Collectors.toList());
    }

    private DiversionRequestDto mapLightRowToDto(Object[] r, Map<Integer, UserMaster> userMap) {
        String cmUserIdStr = (String) r[8];
        Integer cmUserIdInt = cmUserIdStr != null ? parseIntSafe(cmUserIdStr) : null;
        UserMaster cmUser = cmUserIdInt != null ? userMap.get(cmUserIdInt) : null;
        return DiversionRequestDto.builder()
                .id(r[0] != null ? ((Number) r[0]).longValue() : null)
                .requestNo((String) r[1])
                .vendorCode((String) r[2])
                .companyId(r[3] != null ? ((Number) r[3]).longValue() : null)
                .companyName((String) r[4])
                .plantId(r[5] != null ? ((Number) r[5]).longValue() : null)
                .plantName((String) r[6])
                .rioId((String) r[7])
                .cmUserId((String) r[8])
                .stage(r[9] != null ? DiversionStage.valueOf(r[9].toString()) : null)
                .diversionType(r[10] != null ? com.sarthi.enums.DiversionType.valueOf(r[10].toString()) : null)
                .sourcePoNo((String) r[11])
                .sourcePoSrNo((String) r[12])
                .sourceIcNo((String) r[13])
                .sourceCallNo((String) r[14])
                .sourceIcDate(r[15] != null ? (r[15] instanceof java.sql.Date ? ((java.sql.Date) r[15]).toLocalDate() : (LocalDate) r[15]) : null)
                .targetPoNo((String) r[16])
                .targetPoSrNo((String) r[17])
                .totalDiversionQty(r[18] != null ? new java.math.BigDecimal(r[18].toString()) : null)
                .unitOfMeasurement((String) r[19])
                .reasonCode((String) r[20])
                .reasonRemarks((String) r[21])
                .railwayPermissionNo((String) r[22])
                .railwayPermissionDate(r[23] != null ? (r[23] instanceof java.sql.Date ? ((java.sql.Date) r[23]).toLocalDate() : (LocalDate) r[23]) : null)
                .railwayPermissionDocUrl(r[22] != null && r[0] != null ? "/api/erc/diversion/view-doc/" + ((Number) r[0]).longValue() + ".pdf" : null)
                .status(r[24] != null ? DiversionRequestStatus.valueOf(r[24].toString()) : null)
                .createdDate(r[25] != null ? (r[25] instanceof java.sql.Timestamp ? ((java.sql.Timestamp) r[25]).toLocalDateTime() : (java.time.LocalDateTime) r[25]) : null)
                .createdBy(r[26] != null ? r[26].toString() : null)
                .cmUserName(cmUser != null ? cmUser.getFullName() : null)
                .cmEmployeeCode(cmUser != null ? cmUser.getEmployeeCode() : null)
                .diversionNo(r.length > 27 ? (String) r[27] : null)
                .sourceIeName(r.length > 28 ? (String) r[28] : null)
                .sourceIeUserId(r.length > 29 ? (String) r[29] : null)
                .items(Collections.emptyList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiversionRequestDto> getPendingRequestsForCm(String cmUserId) {
        List<DiversionRequestStatus> statuses = List.of(DiversionRequestStatus.PENDING_CM_APPROVAL);
        return mapLightRows(requestRepository.findByCmUserIdAndStatusInLight(cmUserId, statuses));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiversionRequestDto> getApprovedRequestsForCm(String cmUserId) {
        List<DiversionRequestStatus> statuses = List.of(
                DiversionRequestStatus.PENDING_SBU_APPROVAL,
                DiversionRequestStatus.SENT_TO_SBU_HEAD,
                DiversionRequestStatus.CM_APPROVE_DIVERSION,
                DiversionRequestStatus.SBU_HEAD_APPROVE_DIVERSION,
                DiversionRequestStatus.BASKET_CREATED);
        return mapLightRows(requestRepository.findByCmUserIdAndStatusInLight(cmUserId, statuses));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiversionRequestDto> getPendingRequestsForSbu(String rioId) {
        List<DiversionRequestStatus> statuses = List.of(
                DiversionRequestStatus.PENDING_SBU_APPROVAL,
                DiversionRequestStatus.SENT_TO_SBU_HEAD);
        return mapLightRows(requestRepository.findByRioIdAndStatusInLight(rioId, statuses));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiversionRequestDto> getApprovedRequestsForSbu(String rioId) {
        List<DiversionRequestStatus> statuses = List.of(
                DiversionRequestStatus.SBU_HEAD_APPROVE_DIVERSION,
                DiversionRequestStatus.BASKET_CREATED);
        return mapLightRows(requestRepository.findByRioIdAndStatusInLight(rioId, statuses));
    }

    @Override
    @Transactional
    public DiversionRequestDto processWorkflowAction(WorkflowActionDto actionDto) {
        log.info("Processing workflow action: {} for request ID: {}", actionDto.getAction(), actionDto.getRequestId());
        ErcDiversionRequest req = requestRepository.findById(actionDto.getRequestId())
                .orElseThrow(() -> createNotFoundException("Request not found: " + actionDto.getRequestId()));

        String fromStatus = req.getStatus().name();
        String toStatus;

        String wtFromRole, wtToRole, wtFromRoleName, wtToRoleName;
        Integer assignedTo = actionDto.getAssignedToUserId();

        switch (actionDto.getAction().toUpperCase()) {
            case "CM_APPROVE_DIVERSION":
                if (req.getStatus() != DiversionRequestStatus.PENDING_CM_APPROVAL) {
                    throw createException(1004, "Cannot approve in current status: " + req.getStatus());
                }
                materializeBasket(req);
                req.setStatus(DiversionRequestStatus.CM_APPROVE_DIVERSION);
                toStatus = "CM_APPROVE_DIVERSION";
                wtFromRole = ROLE_CM; wtToRole = ROLE_VENDOR;
                wtFromRoleName = "CONTROLLING_MANAGER"; wtToRoleName = "VENDOR";
                break;

            case "CM_SEND_TO_SBU_HEAD":
                if (req.getStatus() != DiversionRequestStatus.PENDING_CM_APPROVAL) {
                    throw createException(1004, "Cannot send to SBU Head in current status: " + req.getStatus());
                }
                req.setStatus(DiversionRequestStatus.SENT_TO_SBU_HEAD);
                toStatus = "SENT_TO_SBU_HEAD";
                wtFromRole = ROLE_CM; wtToRole = ROLE_SBU_HEAD;
                wtFromRoleName = "CONTROLLING_MANAGER"; wtToRoleName = "SBU_HEAD";
                break;

            case "CM_RETURN_DIVERSION":
                if (actionDto.getRemarks() == null || actionDto.getRemarks().trim().isEmpty()) {
                    throw createException(1005, "Return remarks are mandatory");
                }
                if (req.getStatus() != DiversionRequestStatus.PENDING_CM_APPROVAL) {
                    throw createException(1006, "Cannot return in current status: " + req.getStatus());
                }
                req.setStatus(DiversionRequestStatus.CM_RETURN_DIVERSION);
                toStatus = "CM_RETURN_DIVERSION";
                wtFromRole = ROLE_CM; wtToRole = ROLE_VENDOR;
                wtFromRoleName = "CONTROLLING_MANAGER"; wtToRoleName = "VENDOR";
                break;

            case "SBU_HEAD_APPROVE_DIVERSION":
                if (req.getStatus() != DiversionRequestStatus.PENDING_SBU_APPROVAL
                        && req.getStatus() != DiversionRequestStatus.SENT_TO_SBU_HEAD) {
                    throw createException(1004, "Cannot approve in current status: " + req.getStatus());
                }
                materializeBasket(req);
                req.setStatus(DiversionRequestStatus.BASKET_CREATED);
                toStatus = "BASKET_CREATED";
                wtFromRole = ROLE_SBU_HEAD; wtToRole = ROLE_VENDOR;
                wtFromRoleName = "SBU_HEAD"; wtToRoleName = "VENDOR";
                break;

            case "SBU_HEAD_RETURN_DIVERSION":
                if (actionDto.getRemarks() == null || actionDto.getRemarks().trim().isEmpty()) {
                    throw createException(1005, "Return remarks are mandatory");
                }
                if (req.getStatus() != DiversionRequestStatus.PENDING_SBU_APPROVAL
                        && req.getStatus() != DiversionRequestStatus.SENT_TO_SBU_HEAD) {
                    throw createException(1006, "Cannot return in current status: " + req.getStatus());
                }
                req.setStatus(DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION);
                toStatus = "SBU_HEAD_RETURN_DIVERSION";
                wtFromRole = ROLE_SBU_HEAD; wtToRole = ROLE_VENDOR;
                wtFromRoleName = "SBU_HEAD"; wtToRoleName = "VENDOR";
                break;

            case "VENDOR_RESUBMIT_DIVERSION":
            case "RESUBMIT":
                req.setStatus(DiversionRequestStatus.PENDING_CM_APPROVAL);
                toStatus = "PENDING_CM_APPROVAL";
                wtFromRole = ROLE_VENDOR; wtToRole = ROLE_CM;
                wtFromRoleName = "VENDOR"; wtToRoleName = "CONTROLLING_MANAGER";
                break;

            default:
                throw createException(1007, "Unknown action: " + actionDto.getAction());
        }

        ErcDiversionRequest saved = requestRepository.save(req);
        saveHistory(saved, actionDto.getRole(), actionDto.getUserId(), actionDto.getUserName(), actionDto.getAction(), fromStatus, toStatus, actionDto.getRemarks());

        saveWorkflowTransition(saved.getRequestNo(), actionDto.getAction(), null,
                wtFromRole, wtToRole, wtFromRoleName, wtToRoleName,
                toStatus, actionDto.getRemarks(), actionDto.getUserId(),
                assignedTo != null ? String.valueOf(assignedTo) : null, saved.getRioId());

        // On CM approval: create inspection_calls + WT row so the call appears in the IE's Issuance of IC card
        if ("CM_APPROVE_DIVERSION".equalsIgnoreCase(actionDto.getAction()) && assignedTo != null) {
            DiversionStage stage = saved.getStage();
            boolean isProcess = stage == DiversionStage.PROCESS;
            boolean isFinal   = stage == DiversionStage.FINAL;
            // typeOfCall must match values used in WorkflowServiceImpl: "Raw Material", "Process", "Final"
            String typeOfCall = isProcess ? "Process" : isFinal ? "Final" : "Raw Material";

            // 1. Create inspection_calls row — icNumber must match requestId in the WT row below
            InspectionCall divCall = new InspectionCall();
            divCall.setIcNumber(saved.getRequestNo());
            divCall.setDiversionRequestNo(saved.getRequestNo());
            divCall.setIsDivertedMaterial(true);
            divCall.setIsModified(false);
            divCall.setSourceIcNo(saved.getSourceIcNo());
            divCall.setCallCategory("DIVERTED_PASSED");
            divCall.setTypeOfCall(typeOfCall);
            divCall.setVendorId(saved.getVendorCode());
            if (saved.getCompanyId() != null) divCall.setCompanyId(saved.getCompanyId().intValue());
            divCall.setCompanyName(saved.getCompanyName());
            if (saved.getPlantId() != null) divCall.setUnitId(saved.getPlantId().intValue());
            divCall.setUnitName(saved.getPlantName());
            divCall.setUnitAddress(saved.getPlantAddress());
            // poNo/poSerialNo needed for findLiteByIcNumberIn JOIN with PoHeader
            divCall.setPoNo(saved.getTargetPoNo());
            divCall.setPoSerialNo(saved.getTargetPoSrNo());
            divCall.setDesiredInspectionDate(java.time.LocalDate.now());
            divCall.setStatus("CM_APPROVED");
            divCall.setCreatedBy(saved.getVendorCode());
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            divCall.setCreatedAt(now);
            divCall.setUpdatedAt(now);
            inspectionCallRepository.save(divCall);
            log.info("Created inspection_calls row for diversion {} icNumber={} typeOfCall={}", saved.getRequestNo(), saved.getRequestNo(), typeOfCall);

            // 2. Create WT row with status INSPECTION_COMPLETE_CONFIRM so findCompletedByUserRule picks it up
            WorkflowTransition ieWt = new WorkflowTransition();
            ieWt.setWorkflowId(DIVERSION_WORKFLOW_ID);
            ieWt.setRequestId(saved.getRequestNo());
            ieWt.setAction("ASSIGN_TO_IE");
            ieWt.setTransitionId(0);
            ieWt.setCurrentRole(ROLE_CM);
            ieWt.setCurrentRoleName("CONTROLLING_MANAGER");
            ieWt.setNextRole("3");
            ieWt.setNextRoleName("IE");
            ieWt.setStatus("INSPECTION_COMPLETE_CONFIRM");
            ieWt.setRemarks("Diversion call assigned to " + typeOfCall + " IE");
            if (actionDto.getUserId() != null) {
                try { ieWt.setCreatedBy(Integer.parseInt(actionDto.getUserId())); } catch (NumberFormatException ignored) {}
            }
            // modifiedBy = IE userId so the frontend filter `modifiedBy === userId` passes
            ieWt.setModifiedBy(assignedTo);
            // EP (Process) uses processIeUserId for query matching; ER/EF use assignedToUser
            if (isProcess) {
                ieWt.setProcessIeUserId(assignedTo);
            } else {
                ieWt.setAssignedToUser(assignedTo);
            }
            ieWt.setJobStatus("OPEN");
            ieWt.setWorkflowSequence(1);
            ieWt.setRio(saved.getRioId());
            workflowTransitionRepository.save(ieWt);
            log.info("Created INSPECTION_COMPLETE_CONFIRM WT for diversion {} typeOfCall={} userId={}", saved.getRequestNo(), typeOfCall, assignedTo);
        }

        return mapEntityToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BasketItemDto> getAvailableBasketItems(String vendorCode, DiversionStage stage, String targetPoNo, String targetPoSrNo) {
        log.info("Fetching available basket items for vendor: {}, stage: {}, targetPo: {}/{}", vendorCode, stage, targetPoNo, targetPoSrNo);
        List<ErcDivertedPassedBasket> baskets = basketRepository.findAvailableBasketItems(vendorCode, stage, targetPoNo, targetPoSrNo);
        return baskets.stream().map(this::mapBasketToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CallSourceReferenceDto getCallSourceReference(Long callId) {
        log.info("Fetching call source reference for callId: {}", callId);
        InspectionCall call = inspectionCallRepository.findById(callId != null ? callId.intValue() : 0)
                .orElseThrow(() -> createNotFoundException("Call not found: " + callId));

        List<ErcDiversionCallAllocation> allocations = allocationRepository.findByCallId(callId);
        List<BasketItemDto> allocatedItems = allocations.stream().map(a -> mapBasketToDto(a.getBasket())).collect(Collectors.toList());

        String sourceIcNo = call.getSourceIcNo();
        String sourceCallNo = extractCallNo(sourceIcNo);

        List<RmHeatQuantityDto> sourceHeats = new ArrayList<>();
        if (sourceCallNo != null && !sourceCallNo.isEmpty()) {
            try {
                sourceHeats = rawMaterialService.getHeatNumbersByRmIcNumber(sourceCallNo);
            } catch (Exception ignored) {}
        }

        return CallSourceReferenceDto.builder()
                .diversionRequestNo(call.getDiversionRequestNo())
                .sourceIcNo(sourceIcNo)
                .sourceCallNo(sourceCallNo)
                .sourcePoNo(call.getPoNo())
                .sourcePoSrNo(call.getPoSerialNo())
                .stage(call.getTypeOfCall())
                .allocatedItems(allocatedItems)
                .sourceHeatQuantities(sourceHeats)
                .build();
    }

    private void saveWorkflowTransition(String requestId, String action, Integer transitionId,
                                         String currentRole, String nextRole,
                                         String currentRoleName, String nextRoleName,
                                         String status, String remarks,
                                         String createdByUserId, String assignedToUserId, String rio) {
        WorkflowTransition wt = new WorkflowTransition();
        wt.setWorkflowId(DIVERSION_WORKFLOW_ID);
        wt.setRequestId(requestId);
        wt.setAction(action);
        wt.setTransitionId(transitionId != null ? transitionId : 0);
        wt.setCurrentRole(currentRole);
        wt.setNextRole(nextRole);
        wt.setCurrentRoleName(currentRoleName);
        wt.setNextRoleName(nextRoleName);
        wt.setStatus(status);
        wt.setRemarks(remarks);
        if (createdByUserId != null) {
            try { wt.setCreatedBy(Integer.parseInt(createdByUserId)); } catch (NumberFormatException ignored) {}
        }
        if (assignedToUserId != null) {
            try { wt.setAssignedToUser(Integer.parseInt(assignedToUserId)); } catch (NumberFormatException ignored) {}
        }
        wt.setRio(rio);
        wt.setJobStatus("OPEN");
        workflowTransitionRepository.save(wt);
    }

    private BusinessException createNotFoundException(String message) {
        return new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_RESOURCE, message));
    }

    private BusinessException createException(int code, String message) {
        return new BusinessException(new ErrorDetails(code, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, message));
    }

    // ==================== Private Helpers ====================

    private void materializeBasket(ErcDiversionRequest req) {
        log.info("Materializing basket for approved diversion request: {}", req.getRequestNo());
        List<ErcDivertedPassedBasket> baskets = req.getItems().stream().map(item ->
            ErcDivertedPassedBasket.builder()
                    .diversionRequest(req)
                    .diversionItem(item)
                    .stage(req.getStage())
                    .vendorCode(req.getVendorCode())
                    .plantId(req.getPlantId())
                    .rioId(req.getRioId())
                    .sourcePoNo(req.getSourcePoNo())
                    .sourcePoSrNo(req.getSourcePoSrNo())
                    .sourceIcNo(req.getSourceIcNo())
                    .targetPoNo(req.getTargetPoNo())
                    .targetPoSrNo(req.getTargetPoSrNo())
                    .heatNo(item.getHeatNo())
                    .tcNo(item.getTcNo())
                    .lotNo(item.getLotNo())
                    .approvedDivertedQty(item.getDiversionQty())
                    .allocatedCallQty(BigDecimal.ZERO)
                    .consumedIcQty(BigDecimal.ZERO)
                    .availableBalanceQty(item.getDiversionQty())
                    .status(BasketStatus.AVAILABLE)
                    .build()
        ).collect(Collectors.toList());
        basketRepository.saveAll(baskets);
    }

    private void saveHistory(ErcDiversionRequest req, String role, String userId, String userName, String action, String fromStatus, String toStatus, String remarks) {
        ErcDiversionWorkflowHistory history = ErcDiversionWorkflowHistory.builder()
                .diversionRequest(req)
                .actionRole(role != null ? role : "SYSTEM")
                .actionUserId(userId != null ? userId : "SYSTEM")
                .actionUserName(userName)
                .action(action)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .remarks(remarks)
                .build();
        historyRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCompleteDiversionSourceSrNos(String vendorCode) {
        return requestRepository.findCompleteSourceSrNos(vendorCode);
    }

    @Override
    @Transactional(readOnly = true)
    public String getRailwayDocUrl(Long id) {
        return requestRepository.findRailwayDocUrlById(id).orElse(null);
    }

    private static final Set<DiversionRequestStatus> EDITABLE_STATUSES = Set.of(
            DiversionRequestStatus.DRAFT,
            DiversionRequestStatus.PENDING_CM_APPROVAL,
            DiversionRequestStatus.CM_RETURN_DIVERSION,
            DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION
    );

    @Override
    @Transactional
    public void deleteRequest(Long id) {
        ErcDiversionRequest req = requestRepository.findById(id)
                .orElseThrow(() -> createNotFoundException("Request not found: " + id));
        if (!EDITABLE_STATUSES.contains(req.getStatus())) {
            throw createException(1010, "Cannot delete request in status: " + req.getStatus());
        }
        // Soft delete — preserve history and audit trail
        String prevStatus = req.getStatus().name();
        req.setStatus(DiversionRequestStatus.DELETED_BY_VENDOR);
        requestRepository.save(req);
        saveHistory(req, "VENDOR", req.getVendorCode(), req.getVendorCode(), "DELETE_REQUEST", prevStatus, "DELETED_BY_VENDOR", "Request deleted by vendor");
        log.info("Soft-deleted diversion request id={} requestNo={}", id, req.getRequestNo());
    }

    @Override
    @Transactional
    public DiversionRequestDto updateRequest(DiversionRequestDto dto) {
        ErcDiversionRequest existing = requestRepository.findById(dto.getId())
                .orElseThrow(() -> createNotFoundException("Request not found: " + dto.getId()));
        if (!EDITABLE_STATUSES.contains(existing.getStatus())) {
            throw createException(1011, "Cannot edit request in status: " + existing.getStatus());
        }
        // Preserve request number and vendor
        dto.setRequestNo(existing.getRequestNo());
        dto.setVendorCode(existing.getVendorCode());
        // Keep DRAFT status on edit; resubmit is a separate action
        dto.setStatus(existing.getStatus());
        ErcDiversionRequest updated = mapDtoToEntity(dto);
        ErcDiversionRequest saved = requestRepository.save(updated);
        saveHistory(saved, "VENDOR", dto.getVendorCode(), dto.getVendorCode(), "EDIT_DRAFT", existing.getStatus().name(), existing.getStatus().name(), "Request edited by vendor");
        return mapEntityToDto(saved);
    }

    private String generateRequestNo(DiversionStage stage) {
        String typeOfCall = stage == DiversionStage.RAW_MATERIAL ? "Raw Material"
                : stage == DiversionStage.PROCESS ? "Process" : "Final";
        LocalDate today = LocalDate.now();

        long regularCount = inspectionCallRepository.countByTypeOfCallAndCreatedDate(typeOfCall, today);
        long diversionCount = requestRepository.countByStageAndCreatedDate(stage, today);
        long seq = regularCount + diversionCount + 1;

        String requestNo = icNumberGenerator.generateIcNumber(typeOfCall, seq);
        while (inspectionCallRepository.existsByIcNumber(requestNo) || requestRepository.existsByRequestNo(requestNo)) {
            seq++;
            requestNo = icNumberGenerator.generateIcNumber(typeOfCall, seq);
        }
        return requestNo;
    }

    private String generateDiversionNo(String requestNo) {
        // DEP-1024260001 = "D" + "EP-1024260001"
        return "D" + requestNo;
    }

    private String extractCallNo(String certNo) {
        if (certNo == null || certNo.trim().isEmpty()) return null;
        try {
            Optional<InspectionCompleteDetails> icd = completeDetailsRepository.findByCertificateNo(certNo.trim());
            if (icd.isPresent() && icd.get().getCallNo() != null && !icd.get().getCallNo().trim().isEmpty()) {
                return icd.get().getCallNo().trim();
            }
        } catch (Exception e) {
            log.warn("Error finding call number for certificate {}: {}", certNo, e.getMessage());
        }
        if (certNo.contains("/")) {
            String[] parts = certNo.split("/");
            if (parts.length >= 2) return parts[1];
        }
        return certNo;
    }

    private ErcDiversionRequest mapDtoToEntity(DiversionRequestDto dto) {
        ErcDiversionRequest entity = ErcDiversionRequest.builder()
                .id(dto.getId())
                .requestNo(dto.getRequestNo())
                .diversionNo(dto.getDiversionNo())
                .vendorCode(dto.getVendorCode())
                .companyId(dto.getCompanyId())
                .companyName(dto.getCompanyName())
                .plantId(dto.getPlantId() != null ? dto.getPlantId() : 0L)
                .plantName(dto.getPlantName())
                .plantAddress(dto.getPlantAddress())
                .rioId(dto.getRioId())
                .cmUserId(dto.getCmUserId())
                .sbuHeadUserId(dto.getSbuHeadUserId())
                .stage(dto.getStage())
                .diversionType(dto.getDiversionType())
                .sourcePoNo(dto.getSourcePoNo())
                .sourcePoSrNo(dto.getSourcePoSrNo())
                .sourceIcNo(dto.getSourceIcNo())
                .sourceCallNo(dto.getSourceCallNo())
                .sourceIcDate(dto.getSourceIcDate())
                .sourceIeName(dto.getSourceIeName())
                .sourceIeUserId(dto.getSourceIeUserId())
                .targetPoNo(dto.getTargetPoNo())
                .targetPoSrNo(dto.getTargetPoSrNo())
                .totalDiversionQty(dto.getTotalDiversionQty() != null ? dto.getTotalDiversionQty() : BigDecimal.ZERO)
                .unitOfMeasurement(dto.getUnitOfMeasurement())
                .reasonCode(dto.getReasonCode())
                .reasonRemarks(dto.getReasonRemarks())
                .railwayPermissionNo(dto.getRailwayPermissionNo())
                .railwayPermissionDate(dto.getRailwayPermissionDate() != null ? dto.getRailwayPermissionDate() : LocalDate.now())
                .railwayPermissionDocUrl(dto.getRailwayPermissionDocUrl() != null ? dto.getRailwayPermissionDocUrl() : "")
                .vendorRemarks(dto.getVendorRemarks())
                .status(dto.getStatus() != null ? dto.getStatus() : DiversionRequestStatus.DRAFT)
                .build();

        if (dto.getItems() != null) {
            List<ErcDiversionItem> items = dto.getItems().stream().map(itemDto -> ErcDiversionItem.builder()
                    .id(itemDto.getId())
                    .diversionRequest(entity)
                    .heatNo(itemDto.getHeatNo())
                    .tcNo(itemDto.getTcNo())
                    .lotNo(itemDto.getLotNo())
                    .acceptedQty(itemDto.getAcceptedQty() != null ? itemDto.getAcceptedQty() : BigDecimal.ZERO)
                    .downstreamConsumedQty(itemDto.getDownstreamConsumedQty() != null ? itemDto.getDownstreamConsumedQty() : BigDecimal.ZERO)
                    .availableBalanceQty(itemDto.getAvailableBalanceQty() != null ? itemDto.getAvailableBalanceQty() : BigDecimal.ZERO)
                    .diversionQty(itemDto.getDiversionQty() != null ? itemDto.getDiversionQty() : BigDecimal.ZERO)
                    .remainingBalanceQty(itemDto.getRemainingBalanceQty() != null ? itemDto.getRemainingBalanceQty() : BigDecimal.ZERO)
                    .build()).collect(Collectors.toList());
            entity.setItems(items);
        }

        return entity;
    }

    private DiversionRequestDto mapEntityToDto(ErcDiversionRequest entity) {
        List<DiversionItemDto> itemDtos = entity.getItems() != null ? entity.getItems().stream().map(i -> DiversionItemDto.builder()
                .id(i.getId())
                .heatNo(i.getHeatNo())
                .tcNo(i.getTcNo())
                .lotNo(i.getLotNo())
                .acceptedQty(i.getAcceptedQty())
                .downstreamConsumedQty(i.getDownstreamConsumedQty())
                .availableBalanceQty(i.getAvailableBalanceQty())
                .diversionQty(i.getDiversionQty())
                .remainingBalanceQty(i.getRemainingBalanceQty())
                .build()).collect(Collectors.toList()) : new ArrayList<>();

        return DiversionRequestDto.builder()
                .id(entity.getId())
                .requestNo(entity.getRequestNo())
                .diversionNo(entity.getDiversionNo())
                .vendorCode(entity.getVendorCode())
                .companyId(entity.getCompanyId())
                .companyName(entity.getCompanyName())
                .plantId(entity.getPlantId())
                .plantName(entity.getPlantName())
                .plantAddress(entity.getPlantAddress())
                .rioId(entity.getRioId())
                .cmUserId(entity.getCmUserId())
                .sbuHeadUserId(entity.getSbuHeadUserId())
                .stage(entity.getStage())
                .diversionType(entity.getDiversionType())
                .sourcePoNo(entity.getSourcePoNo())
                .sourcePoSrNo(entity.getSourcePoSrNo())
                .sourceIcNo(entity.getSourceIcNo())
                .sourceCallNo(entity.getSourceCallNo())
                .sourceIcDate(entity.getSourceIcDate())
                .sourceIeName(entity.getSourceIeName())
                .sourceIeUserId(entity.getSourceIeUserId())
                .targetPoNo(entity.getTargetPoNo())
                .targetPoSrNo(entity.getTargetPoSrNo())
                .totalDiversionQty(entity.getTotalDiversionQty())
                .unitOfMeasurement(entity.getUnitOfMeasurement())
                .reasonCode(entity.getReasonCode())
                .reasonRemarks(entity.getReasonRemarks())
                .railwayPermissionNo(entity.getRailwayPermissionNo())
                .railwayPermissionDate(entity.getRailwayPermissionDate())
                .railwayPermissionDocUrl(entity.getRailwayPermissionDocUrl() != null && !entity.getRailwayPermissionDocUrl().isEmpty()
                        ? "/api/erc/diversion/view-doc/" + entity.getId() + ".pdf" : null)
                .vendorRemarks(entity.getVendorRemarks())
                .status(entity.getStatus())
                .createdDate(entity.getCreatedDate())
                .createdBy(entity.getCreatedBy())
                .items(itemDtos)
                .build();
    }

    private BasketItemDto mapBasketToDto(ErcDivertedPassedBasket basket) {
        return BasketItemDto.builder()
                .id(basket.getId())
                .diversionRequestId(basket.getDiversionRequest() != null ? basket.getDiversionRequest().getId() : null)
                .diversionRequestNo(basket.getDiversionRequest() != null ? basket.getDiversionRequest().getRequestNo() : null)
                .stage(basket.getStage())
                .vendorCode(basket.getVendorCode())
                .plantId(basket.getPlantId())
                .rioId(basket.getRioId())
                .sourcePoNo(basket.getSourcePoNo())
                .sourcePoSrNo(basket.getSourcePoSrNo())
                .sourceIcNo(basket.getSourceIcNo())
                .targetPoNo(basket.getTargetPoNo())
                .targetPoSrNo(basket.getTargetPoSrNo())
                .heatNo(basket.getHeatNo())
                .tcNo(basket.getTcNo())
                .lotNo(basket.getLotNo())
                .approvedDivertedQty(basket.getApprovedDivertedQty())
                .allocatedCallQty(basket.getAllocatedCallQty())
                .consumedIcQty(basket.getConsumedIcQty())
                .availableBalanceQty(basket.getAvailableBalanceQty())
                .status(basket.getStatus())
                .build();
    }
}
