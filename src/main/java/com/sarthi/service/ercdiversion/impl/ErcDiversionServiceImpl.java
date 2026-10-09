package com.sarthi.service.ercdiversion.impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.rawmaterial.RmChemicalAnalysisDto;
import com.sarthi.dto.rawmaterial.RmHeatQuantityDto;
import com.sarthi.dto.ercdiversion.*;
import com.sarthi.entity.InspectionCompleteDetails;
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
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.InspectionCompleteDetailsRepository;
import com.sarthi.repository.ercdiversion.*;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository;
import com.sarthi.service.ercdiversion.ErcDiversionService;
import com.sarthi.service.rawmaterial.RawMaterialInspectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
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

        List<SourceIcEligibilityDto> eligibleList = new ArrayList<>();
        for (Object[] row : rows) {
            String certNo = (String) row[0];
            String callNo = (String) row[1];
            String poNo = (String) row[2];
            String poSerialNo = (String) row[3];
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
            if (call != null && call.getProcessInspectionDetails() != null) {
                for (com.sarthi.entity.processmaterial.ProcessInspectionDetails pid : call.getProcessInspectionDetails()) {
                    int accQty = pid.getQtyAccepted() != null ? pid.getQtyAccepted() : (pid.getOfferedQty() != null ? pid.getOfferedQty() : 0);
                    BigDecimal accepted = BigDecimal.valueOf(accQty);
                    BigDecimal diverted = itemRepository.sumDivertedQuantityForSource(icNo, pid.getHeatNumber(), null, pid.getLotNumber());
                    BigDecimal available = accepted.subtract(diverted);
                    if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

                    lineItems.add(SourceIcLineItemDto.builder()
                            .lotNo(pid.getLotNumber())
                            .heatNo(pid.getHeatNumber())
                            .acceptedQty(accepted)
                            .downstreamConsumedQty(BigDecimal.ZERO)
                            .previouslyDivertedQty(diverted)
                            .availableBalanceQty(available)
                            .unitOfMeasurement("NOS")
                            .build());
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
        entity.setTotalDiversionQty(totalQty);
        entity.setStatus(DiversionRequestStatus.SUBMITTED_TO_CM);

        ErcDiversionRequest saved = requestRepository.save(entity);
        saveHistory(saved, "VENDOR", dto.getVendorCode(), dto.getVendorCode(), "SUBMIT", "DRAFT", "SUBMITTED_TO_CM", dto.getVendorRemarks());

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
        return requestRepository.findByVendorAndOptionalStatus(vendorCode, status)
                .stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiversionRequestDto> getPendingRequestsForCm(String cmUserId) {
        List<DiversionRequestStatus> statuses = List.of(DiversionRequestStatus.SUBMITTED_TO_CM);
        return requestRepository.findByCmUserIdAndStatusInOrderByCreatedDateDesc(cmUserId, statuses)
                .stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiversionRequestDto> getPendingRequestsForSbu(String rioId) {
        List<DiversionRequestStatus> statuses = List.of(DiversionRequestStatus.APPROVED_BY_CM);
        return requestRepository.findByRioIdAndStatusInOrderByCreatedDateDesc(rioId, statuses)
                .stream().map(this::mapEntityToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiversionRequestDto processWorkflowAction(WorkflowActionDto actionDto) {
        log.info("Processing workflow action: {} for request ID: {}", actionDto.getAction(), actionDto.getRequestId());
        ErcDiversionRequest req = requestRepository.findById(actionDto.getRequestId())
                .orElseThrow(() -> createNotFoundException("Request not found: " + actionDto.getRequestId()));

        String fromStatus = req.getStatus().name();
        String toStatus;

        switch (actionDto.getAction().toUpperCase()) {
            case "APPROVE":
                if (req.getStatus() == DiversionRequestStatus.SUBMITTED_TO_CM) {
                    req.setStatus(DiversionRequestStatus.APPROVED_BY_CM);
                    toStatus = "APPROVED_BY_CM";
                } else if (req.getStatus() == DiversionRequestStatus.APPROVED_BY_CM) {
                    req.setStatus(DiversionRequestStatus.APPROVED_BY_SBU);
                    toStatus = "APPROVED_BY_SBU";

                    // Materialize into Basket
                    materializeBasket(req);
                    req.setStatus(DiversionRequestStatus.BASKET_CREATED);
                    toStatus = "BASKET_CREATED";
                } else {
                    throw createException(1004, "Cannot approve in current status: " + req.getStatus());
                }
                break;

            case "RETURN":
                if (actionDto.getRemarks() == null || actionDto.getRemarks().trim().isEmpty()) {
                    throw createException(1005, "Return remarks are mandatory");
                }
                if (req.getStatus() == DiversionRequestStatus.SUBMITTED_TO_CM) {
                    req.setStatus(DiversionRequestStatus.RETURNED_BY_CM);
                    toStatus = "RETURNED_BY_CM";
                } else if (req.getStatus() == DiversionRequestStatus.APPROVED_BY_CM) {
                    req.setStatus(DiversionRequestStatus.RETURNED_BY_SBU);
                    toStatus = "RETURNED_BY_SBU";
                } else {
                    throw createException(1006, "Cannot return in current status: " + req.getStatus());
                }
                break;

            case "RESUBMIT":
                req.setStatus(DiversionRequestStatus.SUBMITTED_TO_CM);
                toStatus = "SUBMITTED_TO_CM";
                break;

            default:
                throw createException(1007, "Unknown action: " + actionDto.getAction());
        }

        ErcDiversionRequest saved = requestRepository.save(req);
        saveHistory(saved, actionDto.getRole(), actionDto.getUserId(), actionDto.getUserName(), actionDto.getAction(), fromStatus, toStatus, actionDto.getRemarks());

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

    private BusinessException createNotFoundException(String message) {
        return new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_RESOURCE, message));
    }

    private BusinessException createException(int code, String message) {
        return new BusinessException(new ErrorDetails(code, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, message));
    }

    // ==================== Private Helpers ====================

    private void materializeBasket(ErcDiversionRequest req) {
        log.info("Materializing basket for approved diversion request: {}", req.getRequestNo());
        for (ErcDiversionItem item : req.getItems()) {
            ErcDivertedPassedBasket basket = ErcDivertedPassedBasket.builder()
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
                    .build();
            basketRepository.save(basket);
        }
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

    private String generateRequestNo(DiversionStage stage) {
        int year = Year.now().getValue();
        String stageCode = stage == DiversionStage.RAW_MATERIAL ? "RM" : (stage == DiversionStage.PROCESS ? "PROC" : "FINAL");
        long count = requestRepository.countByStageAndYear(stage, year) + 1;
        return String.format("DIV/%s/%d/%05d", stageCode, year, count);
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
                .targetPoNo(entity.getTargetPoNo())
                .targetPoSrNo(entity.getTargetPoSrNo())
                .totalDiversionQty(entity.getTotalDiversionQty())
                .unitOfMeasurement(entity.getUnitOfMeasurement())
                .reasonCode(entity.getReasonCode())
                .reasonRemarks(entity.getReasonRemarks())
                .railwayPermissionNo(entity.getRailwayPermissionNo())
                .railwayPermissionDate(entity.getRailwayPermissionDate())
                .railwayPermissionDocUrl(entity.getRailwayPermissionDocUrl())
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
