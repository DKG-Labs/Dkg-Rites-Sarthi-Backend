package com.sarthi.SRailPad.diversion.service;

import com.sarthi.SRailPad.diversion.dto.RailDiversionDtos.*;
import com.sarthi.SRailPad.diversion.entity.*;
import com.sarthi.SRailPad.diversion.repository.*;
import com.sarthi.SRailPad.dto.inspectionCall.ProcessInspectionSaveDto;
import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCompleteDetails;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.ieVerification.RailFinalInspectionLotResultsRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCompleteDetailsRepository;
import com.sarthi.SRailPad.service.inspectionCall.RailProcessCallService;
import com.sarthi.constant.AppConstant;
import com.sarthi.entity.UserMaster;
import com.sarthi.enums.BasketStatus;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;
import com.sarthi.enums.DiversionType;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.exception.UnauthorizedException;
import com.sarthi.repository.UserMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sarthi.enums.DiversionRequestStatus.*;

/**
 * Rail Pad material diversion. Same flow as ERC (raise → CM / SBU → diverted-passed call → IC → IBS),
 * with ERC's gaps fixed: balances are recomputed and locked on the server, SBU approval also creates the
 * call, resubmit works, and diverted calls really reserve / consume / release basket quantity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RailDiversionServiceImpl implements RailDiversionService {

    private static final Long CALL_WORKFLOW_ID = 2L;
    private static final Pattern CALL_NO = Pattern.compile("(RP[PF]-\\d{6,})", Pattern.CASE_INSENSITIVE);

    /** Requests that hold quantity (returned ones too, so the vendor can resubmit). */
    private static final Set<DiversionRequestStatus> HOLDING = EnumSet.of(
            PENDING_CM_APPROVAL, CM_RETURN_DIVERSION, SENT_TO_SBU_HEAD, PENDING_SBU_APPROVAL,
            SBU_HEAD_RETURN_DIVERSION, CM_APPROVE_DIVERSION, SBU_HEAD_APPROVE_DIVERSION, BASKET_CREATED, COMPLETED);
    private static final Set<DiversionRequestStatus> EDITABLE = EnumSet.of(
            DRAFT, PENDING_CM_APPROVAL, CM_RETURN_DIVERSION, SBU_HEAD_RETURN_DIVERSION);
    private static final Set<DiversionRequestStatus> RETURNED = EnumSet.of(CM_RETURN_DIVERSION, SBU_HEAD_RETURN_DIVERSION);
    private static final Set<DiversionRequestStatus> SBU_PENDING = EnumSet.of(SENT_TO_SBU_HEAD, PENDING_SBU_APPROVAL);

    private static final Set<String> VENDOR_ROLES = Set.of("Rail Vendor", "Railpad Vendor");
    private static final Set<String> CM_ROLES = Set.of("Control Manager", "CM", "Controlling Manager");
    private static final Set<String> SBU_ROLES = Set.of("SBU Head");
    private static final Set<String> RAIL_IE_ROLES = Set.of("Rail Main IE", "Rail Process IE", "Railpad IE");

    private final RailDiversionRequestRepository requestRepository;
    private final RailDiversionItemRepository itemRepository;
    private final RailDivertedPassedBasketRepository basketRepository;
    private final RailDiversionCallAllocationRepository allocationRepository;
    private final RailDiversionWorkflowHistoryRepository historyRepository;
    private final RailDiversionSourceQueryRepository sourceQueryRepository;
    private final RailInspectionCallRepository callRepository;
    private final RailWorkflowTransactionRepository workflowRepository;
    private final RailInspectionCompleteDetailsRepository completeDetailsRepository;
    private final RailFinalInspectionLotResultsRepository lotResultsRepository;
    private final RailProcessCallService processCallService;
    private final UserMasterRepository userMasterRepository;

    // ================= Vendor: source ICs and balances =================

    @Override
    @Transactional(readOnly = true)
    public List<SourceIcDto> getEligibleSourceIcs(String plantId, String stage) {
        String vendorCode = vendorCode(requireRole(VENDOR_ROLES, "Only Rail Pad vendors can raise a diversion"));
        DiversionStage st = parseStage(stage);
        List<Object[]> rows = sourceQueryRepository.findIssuedIcs(vendorCode, plantId, st.name());
        Set<String> completeIcs = new HashSet<>(requestRepository.findSourceIcsWithDiversionType(vendorCode, DiversionType.COMPLETE, HOLDING));

        List<SourceIcDto> out = new ArrayList<>();
        for (Object[] r : rows) {
            String callNo = str(r[0]);
            String icNo = str(r[4]);
            if (completeIcs.contains(icNo) || completeIcs.contains(callNo)) continue;
            List<SourceLineDto> lines = computeLines(callNo, icNo, st, null);
            BigDecimal accepted = sum(lines, SourceLineDto::getAcceptedQty);
            BigDecimal diverted = sum(lines, SourceLineDto::getAlreadyDivertedQty);
            BigDecimal available = sum(lines, SourceLineDto::getAvailableQty);
            if (available.signum() <= 0) continue;
            // Older calls store the PO as "PO/SR" with po_sr empty: split so PO and serial are separate
            String poNo = str(r[1]);
            String poSr = str(r[2]);
            if ((poSr == null || poSr.isBlank()) && poNo != null && poNo.contains("/")) {
                poSr = poNo.substring(poNo.lastIndexOf('/') + 1).trim();
                poNo = poNo.substring(0, poNo.lastIndexOf('/')).trim();
            }
            out.add(new SourceIcDto(callNo, icNo, poNo, poSr, str(r[3]), toDate(r[5]),
                    str(r[6]), str(r[7]), accepted, diverted, available));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SourceLineDto> getSourceLines(String sourceIcNo, String stage, Long excludeRequestId) {
        requireRole(VENDOR_ROLES, "Only Rail Pad vendors can view diversion balances");
        String callNo = callNoOf(sourceIcNo);
        return computeLines(callNo, certificateNoOf(callNo, sourceIcNo), parseStage(stage), excludeRequestId);
    }

    /** Per lot (Final) or batch (Process): accepted − used downstream − already diverted. */
    private List<SourceLineDto> computeLines(String callNo, String icNo, DiversionStage stage, Long excludeRequestId) {
        Map<String, BigDecimal> diverted = new HashMap<>();
        for (Object[] row : itemRepository.sumDivertedBySourceIc(icNo, HOLDING, excludeRequestId)) {
            diverted.merge(lineKey(str(row[0]), str(row[1])), toBig(row[2]), BigDecimal::add);
        }
        List<SourceLineDto> lines = new ArrayList<>();
        if (stage == DiversionStage.PROCESS) {
            ProcessInspectionSaveDto dto = processCallService.getAvailableBatchesForFinalCall(callNo);
            if (dto != null && dto.getBatches() != null) {
                for (ProcessInspectionSaveDto.ProcessBatchSaveDto b : dto.getBatches()) {
                    BigDecimal acc = BigDecimal.valueOf(nz(b.getQtyAccepted()));
                    BigDecimal used = BigDecimal.valueOf(nz(b.getPreviouslyOfferedQty()));
                    BigDecimal div = diverted.getOrDefault(lineKey(null, b.getBatchNo()), BigDecimal.ZERO);
                    lines.add(new SourceLineDto(null, b.getBatchNo(), acc, used, div, floor0(acc.subtract(used).subtract(div))));
                }
            }
        } else {
            for (RailFinalInspectionLotResults lot : lotResultsRepository.findAllByCallNo(callNo)) {
                BigDecimal acc = BigDecimal.valueOf(nz(lot.getAcceptedQty()));
                BigDecimal div = diverted.getOrDefault(lineKey(lot.getLotNo(), null), BigDecimal.ZERO);
                lines.add(new SourceLineDto(lot.getLotNo(), null, acc, BigDecimal.ZERO, div, floor0(acc.subtract(div))));
            }
        }
        return lines;
    }

    // ================= Vendor: submit / edit / resubmit / delete =================

    @Override
    public RailDiversionRequest submit(RequestDto dto) {
        UserMaster user = requireRole(VENDOR_ROLES, "Only Rail Pad vendors can raise a diversion");
        RailDiversionRequest req = new RailDiversionRequest();
        applyAndValidate(req, dto, vendorCode(user), null);
        req.setRequestNo(generateRequestNo(req.getStage()));
        req.setDiversionNo("D" + req.getRequestNo());
        req.setStatus(PENDING_CM_APPROVAL);
        req.setCreatedBy(String.valueOf(user.getUserId()));
        RailDiversionRequest saved = requestRepository.save(req);
        history(saved, "VENDOR", user, "SUBMIT", DRAFT, PENDING_CM_APPROVAL, dto.getVendorRemarks());
        return saved;
    }

    @Override
    public RailDiversionRequest update(Long id, RequestDto dto) {
        UserMaster user = requireRole(VENDOR_ROLES, "Only Rail Pad vendors can edit a diversion");
        RailDiversionRequest req = ownRequest(id, user);
        if (!EDITABLE.contains(req.getStatus())) throw invalid("This request can no longer be edited (" + req.getStatus() + ")");
        applyAndValidate(req, dto, vendorCode(user), req.getId());
        req.setUpdatedBy(String.valueOf(user.getUserId()));
        RailDiversionRequest saved = requestRepository.save(req);
        history(saved, "VENDOR", user, "EDIT", req.getStatus(), req.getStatus(), null);
        return saved;
    }

    @Override
    public RailDiversionRequest resubmit(Long id) {
        UserMaster user = requireRole(VENDOR_ROLES, "Only Rail Pad vendors can resubmit a diversion");
        RailDiversionRequest req = ownRequest(id, user);
        if (!RETURNED.contains(req.getStatus())) throw invalid("Only a returned request can be resubmitted");
        // Balance may have changed while it was returned
        validateAgainstBalance(req, req.getId());
        DiversionRequestStatus from = req.getStatus();
        req.setStatus(PENDING_CM_APPROVAL);
        RailDiversionRequest saved = requestRepository.save(req);
        history(saved, "VENDOR", user, "RESUBMIT", from, PENDING_CM_APPROVAL, null);
        return saved;
    }

    @Override
    public void delete(Long id) {
        UserMaster user = requireRole(VENDOR_ROLES, "Only Rail Pad vendors can delete a diversion");
        RailDiversionRequest req = ownRequest(id, user);
        if (!EDITABLE.contains(req.getStatus())) throw invalid("This request can no longer be deleted (" + req.getStatus() + ")");
        DiversionRequestStatus from = req.getStatus();
        req.setStatus(DELETED_BY_VENDOR);
        requestRepository.save(req);
        history(req, "VENDOR", user, "DELETE", from, DELETED_BY_VENDOR, null);
    }

    private void applyAndValidate(RailDiversionRequest req, RequestDto dto, String vendorCode, Long excludeRequestId) {
        if (dto == null) throw invalid("Request is empty");
        DiversionStage stage = parseStage(dto.getStage());
        DiversionType type;
        try { type = DiversionType.valueOf(String.valueOf(dto.getDiversionType()).trim().toUpperCase()); }
        catch (Exception e) { throw invalid("Diversion type must be PARTIAL or COMPLETE"); }
        require(dto.getPlantId(), "Plant");
        require(dto.getCmUserId(), "Controlling Manager");
        require(dto.getSourceIcNo(), "Source IC");
        require(dto.getTargetPoNo(), "Target PO");
        require(dto.getTargetPoSrNo(), "Target PO serial");
        require(dto.getRailwayPermissionNo(), "Railway permission no.");
        require(dto.getRailwayPermissionDocUrl(), "Railway permission letter");
        if (dto.getRailwayPermissionDate() == null) throw invalid("Railway permission date is required");

        String callNo = callNoOf(dto.getSourceIcNo());
        RailInspectionCall src = callRepository.findByCallNo(callNo)
                .orElseThrow(() -> invalid("Source IC not found: " + dto.getSourceIcNo()));
        if (!clean(src.getVendorCode()).equalsIgnoreCase(clean(vendorCode))) throw unauthorized("Source IC belongs to another vendor");
        if ("DIVERTED_PASSED".equalsIgnoreCase(src.getCallCategory())) throw invalid("A diverted IC cannot be diverted again");
        String expectedType = stage == DiversionStage.PROCESS ? "PROCESS" : "FINAL";
        if (!expectedType.equalsIgnoreCase(src.getCallType())) throw invalid("Source IC is not a " + expectedType + " IC");
        // Older calls store the PO as "PO/SR" with po_sr empty: split so PO and serial are separate
        // (same as the source-IC list). source_po_sr_no is NOT NULL, so an empty serial failed the save.
        String srcPoNo = src.getPoNo() != null ? src.getPoNo().trim() : null;
        String srcPoSr = src.getPoSr() != null ? src.getPoSr().trim() : null;
        if ((srcPoSr == null || srcPoSr.isEmpty()) && srcPoNo != null && srcPoNo.contains("/")) {
            srcPoSr = srcPoNo.substring(srcPoNo.lastIndexOf('/') + 1).trim();
            srcPoNo = srcPoNo.substring(0, srcPoNo.lastIndexOf('/')).trim();
        }
        if (srcPoSr == null || srcPoSr.isEmpty()) throw invalid("Source IC has no PO serial number");
        if (dto.getTargetPoNo().trim().equals(srcPoNo) && dto.getTargetPoSrNo().trim().equals(srcPoSr)) {
            throw invalid("Target PO serial must be different from the source PO serial");
        }

        req.setVendorCode(clean(vendorCode));
        req.setCompanyName(dto.getCompanyName());
        req.setPlantId(clean(dto.getPlantId()));
        req.setPlantName(dto.getPlantName());
        req.setPlantAddress(dto.getPlantAddress());
        req.setRioId(firstNonBlank(dto.getRioId(), workflowRepository.findRioByCallNo(callNo), "N"));
        req.setCmUserId(dto.getCmUserId().trim());
        req.setStage(stage);
        req.setDiversionType(type);
        req.setSourcePoNo(srcPoNo);
        req.setSourcePoSrNo(srcPoSr);
        req.setSourceCallNo(callNo);
        req.setSourceIcNo(certificateNoOf(callNo, dto.getSourceIcNo()));
        req.setTargetPoNo(dto.getTargetPoNo().trim());
        req.setTargetPoSrNo(dto.getTargetPoSrNo().trim());
        req.setUnitOfMeasurement(firstNonBlank(dto.getUnitOfMeasurement(), "Nos", null));
        req.setReasonCode(dto.getReasonCode());
        req.setReasonRemarks(dto.getReasonRemarks());
        req.setRailwayPermissionNo(dto.getRailwayPermissionNo().trim());
        req.setRailwayPermissionDate(dto.getRailwayPermissionDate());
        req.setRailwayPermissionDocUrl(dto.getRailwayPermissionDocUrl());
        req.setVendorRemarks(dto.getVendorRemarks());
        List<Object[]> ieRow = sourceQueryRepository.findIssuedIcs(vendorCode, null, expectedType).stream()
                .filter(r -> callNo.equals(str(r[0]))).toList();
        if (ieRow.isEmpty()) throw invalid("Source IC is not issued yet");
        req.setSourceIcDate(toDate(ieRow.get(0)[5]));
        req.setSourceIeUserId(str(ieRow.get(0)[6]));

        // Lines: COMPLETE takes every line's full balance; PARTIAL takes what the vendor typed
        Map<String, BigDecimal> wanted = new LinkedHashMap<>();
        for (ItemDto it : Optional.ofNullable(dto.getItems()).orElse(List.of())) {
            if (it.getDiversionQty() != null && it.getDiversionQty().signum() > 0) {
                wanted.merge(lineKey(it.getLotNo(), it.getBatchNo()), it.getDiversionQty(), BigDecimal::add);
            }
        }
        req.getItems().clear();
        for (SourceLineDto line : computeLines(callNo, req.getSourceIcNo(), stage, excludeRequestId)) {
            String key = lineKey(line.getLotNo(), line.getBatchNo());
            BigDecimal qty = type == DiversionType.COMPLETE ? line.getAvailableQty() : wanted.getOrDefault(key, BigDecimal.ZERO);
            if (qty.signum() <= 0) continue;
            RailDiversionItem item = RailDiversionItem.builder()
                    .diversionRequest(req).lotNo(line.getLotNo()).batchNo(line.getBatchNo())
                    .acceptedQty(line.getAcceptedQty()).downstreamConsumedQty(line.getDownstreamConsumedQty())
                    .availableBalanceQty(line.getAvailableQty()).diversionQty(qty)
                    .remainingBalanceQty(line.getAvailableQty().subtract(qty)).build();
            req.getItems().add(item);
        }
        if (req.getItems().isEmpty()) throw invalid("Enter a diversion quantity for at least one lot / batch");
        req.setTotalDiversionQty(req.getItems().stream().map(RailDiversionItem::getDiversionQty).reduce(BigDecimal.ZERO, BigDecimal::add));
        validateAgainstBalance(req, excludeRequestId);
    }

    /** Server-side check under a lock on the source IC, so two requests cannot over-divert in parallel. */
    private void validateAgainstBalance(RailDiversionRequest req, Long excludeRequestId) {
        requestRepository.lockActiveBySourceIc(req.getSourceIcNo(), HOLDING);
        Map<String, BigDecimal> available = computeLines(req.getSourceCallNo(), req.getSourceIcNo(), req.getStage(), excludeRequestId)
                .stream().collect(Collectors.toMap(l -> lineKey(l.getLotNo(), l.getBatchNo()), SourceLineDto::getAvailableQty, BigDecimal::add));
        for (RailDiversionItem it : req.getItems()) {
            BigDecimal avail = available.getOrDefault(lineKey(it.getLotNo(), it.getBatchNo()), BigDecimal.ZERO);
            if (it.getDiversionQty().compareTo(avail) > 0) {
                throw invalid("Diversion qty " + plain(it.getDiversionQty()) + " for " + label(it) + " is more than the available " + plain(avail));
            }
        }
    }

    // ================= Reads =================

    @Override
    @Transactional(readOnly = true)
    public RailDiversionRequest getById(Long id) {
        UserMaster user = currentUser();
        RailDiversionRequest req = requestRepository.findById(id).orElseThrow(() -> invalid("Diversion request not found"));
        if (hasAnyRole(user, VENDOR_ROLES) && !hasAnyRole(user, CM_ROLES) && !hasAnyRole(user, SBU_ROLES)
                && !clean(req.getVendorCode()).equalsIgnoreCase(vendorCode(user))) {
            throw unauthorized("This request belongs to another vendor");
        }
        return req;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RailDiversionRequest> getVendorRequests() {
        String vc = vendorCode(requireRole(VENDOR_ROLES, "Only Rail Pad vendors can view their diversions"));
        return requestRepository.findByVendorCodeAndStatusNotOrderByCreatedDateDesc(vc, DELETED_BY_VENDOR);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RailDivertedPassedBasket> getVendorBasket() {
        String vc = vendorCode(requireRole(VENDOR_ROLES, "Only Rail Pad vendors can view the basket"));
        return basketRepository.findByVendorCodeOrderByCreatedDateDesc(vc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCompleteSourceSrNos() {
        String vc = vendorCode(requireRole(VENDOR_ROLES, "Only Rail Pad vendors"));
        return requestRepository.findCompleteSourceSrNos(vc, DiversionType.COMPLETE, HOLDING);
    }

    /** Diverted ICs on a target PO serial that still have balance for DIVERTED calls. */
    @Override
    @Transactional(readOnly = true)
    public List<DivertedIcBalanceDto> getDivertedIcBalances(String poNo, String poSrNo, String stage) {
        String vc = vendorCode(requireRole(VENDOR_ROLES, "Only Rail Pad vendors"));
        List<RailDivertedPassedBasket> rows = basketRepository.findByVendorCodeAndTargetPoNoAndTargetPoSrNoAndStageAndStatusIn(
                vc, poNo, poSrNo, parseStage(stage), EnumSet.of(BasketStatus.AVAILABLE, BasketStatus.LOCKED));
        Map<String, List<RailDivertedPassedBasket>> byCall = rows.stream()
                .filter(b -> b.getDivertedCallNo() != null && isIcIssued(b.getDivertedCallNo()))
                .collect(Collectors.groupingBy(RailDivertedPassedBasket::getDivertedCallNo, LinkedHashMap::new, Collectors.toList()));
        List<DivertedIcBalanceDto> out = new ArrayList<>();
        byCall.forEach((callNo, list) -> {
            RailDivertedPassedBasket f = list.get(0);
            out.add(new DivertedIcBalanceDto(callNo, certificateNoOf(callNo, callNo), f.getDiversionRequest().getRequestNo(),
                    f.getStage().name(), f.getSourceIcNo(),
                    sumB(list, RailDivertedPassedBasket::getApprovedDivertedQty), sumB(list, RailDivertedPassedBasket::getAllocatedCallQty),
                    sumB(list, RailDivertedPassedBasket::getConsumedIcQty), sumB(list, RailDivertedPassedBasket::getAvailableBalanceQty)));
        });
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RailDiversionRequest> getCmPending() {
        UserMaster cm = requireRole(CM_ROLES, "Only a Controlling Manager can view this queue");
        return requestRepository.findByCmUserIdAndStatusOrderByCreatedDateDesc(String.valueOf(cm.getUserId()), PENDING_CM_APPROVAL);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RailDiversionRequest> getCmActioned() {
        UserMaster cm = requireRole(CM_ROLES, "Only a Controlling Manager can view this queue");
        return requestRepository.findByCmUserIdAndStatusInOrderByCreatedDateDesc(String.valueOf(cm.getUserId()),
                EnumSet.of(CM_APPROVE_DIVERSION, CM_RETURN_DIVERSION, SENT_TO_SBU_HEAD, SBU_HEAD_APPROVE_DIVERSION,
                        SBU_HEAD_RETURN_DIVERSION, BASKET_CREATED, COMPLETED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RailDiversionRequest> getSbuPending() {
        UserMaster sbu = requireRole(SBU_ROLES, "Only the SBU Head can view this queue");
        return byRio(sbu, SBU_PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RailDiversionRequest> getSbuActioned() {
        UserMaster sbu = requireRole(SBU_ROLES, "Only the SBU Head can view this queue");
        return byRio(sbu, EnumSet.of(SBU_HEAD_APPROVE_DIVERSION, SBU_HEAD_RETURN_DIVERSION, BASKET_CREATED, COMPLETED));
    }

    private List<RailDiversionRequest> byRio(UserMaster user, Set<DiversionRequestStatus> statuses) {
        String rio = user.getRio();
        if (rio == null || rio.isBlank()) return requestRepository.findByStatusInOrderByCreatedDateDesc(statuses);
        String initial = rio.trim().substring(0, 1).toUpperCase();
        return requestRepository.findByStatusInOrderByCreatedDateDesc(statuses).stream()
                .filter(r -> r.getRioId() != null && r.getRioId().trim().toUpperCase().startsWith(initial))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RailDiversionWorkflowHistory> getHistory(Long requestId) {
        getById(requestId); // access check
        return historyRepository.findByDiversionRequestIdOrderByCreatedDateAsc(requestId);
    }

    // ================= CM / SBU actions =================

    @Override
    public RailDiversionRequest cmApprove(ActionDto a) {
        UserMaster cm = requireRole(CM_ROLES, "Only a Controlling Manager can approve");
        RailDiversionRequest req = forAction(a, PENDING_CM_APPROVAL);
        ownCmCheck(req, cm);
        approveAndCreateCall(req, a, cm, "CM", CM_APPROVE_DIVERSION);
        return req;
    }

    @Override
    public RailDiversionRequest cmSendToSbu(ActionDto a) {
        UserMaster cm = requireRole(CM_ROLES, "Only a Controlling Manager can forward to the SBU Head");
        RailDiversionRequest req = forAction(a, PENDING_CM_APPROVAL);
        ownCmCheck(req, cm);
        return move(req, cm, "CM", "CM_SEND_TO_SBU_HEAD", SENT_TO_SBU_HEAD, a.getRemarks());
    }

    @Override
    public RailDiversionRequest cmReturn(ActionDto a) {
        UserMaster cm = requireRole(CM_ROLES, "Only a Controlling Manager can return");
        RailDiversionRequest req = forAction(a, PENDING_CM_APPROVAL);
        ownCmCheck(req, cm);
        require(a.getRemarks(), "Remarks");
        return move(req, cm, "CM", "CM_RETURN_DIVERSION", CM_RETURN_DIVERSION, a.getRemarks());
    }

    @Override
    public RailDiversionRequest sbuApprove(ActionDto a) {
        UserMaster sbu = requireRole(SBU_ROLES, "Only the SBU Head can approve");
        RailDiversionRequest req = forAction(a, SENT_TO_SBU_HEAD, PENDING_SBU_APPROVAL);
        req.setSbuHeadUserId(String.valueOf(sbu.getUserId()));
        approveAndCreateCall(req, a, sbu, "SBU_HEAD", SBU_HEAD_APPROVE_DIVERSION);
        return req;
    }

    @Override
    public RailDiversionRequest sbuReturn(ActionDto a) {
        UserMaster sbu = requireRole(SBU_ROLES, "Only the SBU Head can return");
        RailDiversionRequest req = forAction(a, SENT_TO_SBU_HEAD, PENDING_SBU_APPROVAL);
        require(a.getRemarks(), "Remarks");
        req.setSbuHeadUserId(String.valueOf(sbu.getUserId()));
        return move(req, sbu, "SBU_HEAD", "SBU_HEAD_RETURN_DIVERSION", SBU_HEAD_RETURN_DIVERSION, a.getRemarks());
    }

    /**
     * Approval: re-check balance, create the diverted-passed RPP/RPF call on the target PO (straight into the
     * chosen IE's Issuance of IC list, no re-inspection) and the basket rows downstream diverted calls draw from.
     */
    private void approveAndCreateCall(RailDiversionRequest req, ActionDto a, UserMaster actor, String role, DiversionRequestStatus to) {
        if (a.getAssignedIeUserId() == null) throw invalid("Select the IE who will issue the diverted IC");
        UserMaster ie = userMasterRepository.findByUserId(Math.toIntExact(a.getAssignedIeUserId()))
                .orElseThrow(() -> invalid("Selected IE not found"));
        if (!hasAnyRole(ie, RAIL_IE_ROLES)) throw invalid("Selected user is not a Rail Pad IE");
        validateAgainstBalance(req, req.getId());

        RailInspectionCall call = createDivertedPassedCall(req, ie, actor);
        req.setDivertedCallNo(call.getCallNo());
        for (RailDiversionItem it : req.getItems()) {
            basketRepository.save(RailDivertedPassedBasket.builder()
                    .diversionRequest(req).diversionItem(it).stage(req.getStage())
                    .vendorCode(req.getVendorCode()).plantId(req.getPlantId()).rioId(req.getRioId())
                    .sourcePoNo(req.getSourcePoNo()).sourcePoSrNo(req.getSourcePoSrNo()).sourceIcNo(req.getSourceIcNo())
                    .divertedCallNo(call.getCallNo())
                    .targetPoNo(req.getTargetPoNo()).targetPoSrNo(req.getTargetPoSrNo())
                    .lotNo(it.getLotNo()).batchNo(it.getBatchNo())
                    .approvedDivertedQty(it.getDiversionQty()).availableBalanceQty(it.getDiversionQty())
                    .build());
        }
        move(req, actor, role, to.name(), to, a.getRemarks());
    }

    private RailInspectionCall createDivertedPassedCall(RailDiversionRequest req, UserMaster ie, UserMaster actor) {
        RailInspectionCall src = callRepository.findByCallNo(req.getSourceCallNo())
                .orElseThrow(() -> invalid("Source call not found"));
        boolean process = req.getStage() == DiversionStage.PROCESS;
        LocalDateTime now = LocalDateTime.now();
        int qty = req.getTotalDiversionQty().setScale(0, RoundingMode.HALF_UP).intValue();

        RailInspectionCall c = new RailInspectionCall();
        // Same as ERC: the diverted call takes the request number. Older requests (RDP-/RDF-) get a new call number.
        String reserved = req.getRequestNo() != null ? req.getRequestNo().trim() : "";
        boolean useReserved = reserved.startsWith(process ? "RPP-" : "RPF-")
                && !callRepository.existsByCallNo(reserved) && !workflowRepository.existsByRequestId(reserved);
        c.setCallNo(useReserved ? reserved : nextCallNo(process ? "RPP-" : "RPF-"));
        c.setPoNo(req.getTargetPoNo());
        c.setPoSr(req.getTargetPoSrNo());
        c.setVendorCode(clean(src.getVendorCode()));
        c.setPlantId(clean(src.getPlantId()));
        c.setRailPadType(src.getRailPadType());
        c.setDrawingNo(src.getDrawingNo());
        c.setTotalQty(qty);
        if (src.getNoOfSets() != null) c.setNoOfSets(qty);
        c.setNoOfLots(req.getItems().size());
        c.setInspectionDate(LocalDate.now());
        c.setCallType(process ? "PROCESS" : "FINAL");
        c.setStatus("COMPLETED");
        c.setProcessIcNo(src.getProcessIcNo());
        c.setRemarks("Material diverted from Source IC " + req.getSourceIcNo() + " (" + req.getDiversionType() + ", request " + req.getRequestNo() + ")");
        c.setCallNature("DIVERTED");
        c.setCallCategory("DIVERTED_PASSED");
        c.setIsDivertedMaterial(true);
        c.setDiversionRequestNo(req.getRequestNo());
        c.setSourceIcNo(req.getSourceIcNo());
        c.setCreatedBy(actor.getUserId() != null ? actor.getUserId().longValue() : null);
        c.setCreatedAt(now);
        RailInspectionCall saved = callRepository.save(c);

        // Workflow placed at "inspection finished": the IE's completed-calls list shows it under Issuance of IC
        RailWorkflowTransaction srcTx = workflowRepository.findByRequestIdOrderByWorkflowTransitionIdDesc(src.getCallNo())
                .stream().findFirst().orElse(null);
        String ieRole = hasAnyRole(ie, Set.of("Rail Process IE")) && process ? "Rail Process IE" : "Rail Main IE";
        RailWorkflowTransaction tx = new RailWorkflowTransaction();
        tx.setWorkflowId(CALL_WORKFLOW_ID);
        tx.setModuleId(srcTx != null ? srcTx.getModuleId() : null);
        tx.setRequestId(saved.getCallNo());
        tx.setAction("FINISH");
        tx.setStatus("COMPLETED");
        tx.setJobStatus("COMPLETED");
        tx.setRemarks("Diverted-passed call for diversion " + req.getRequestNo() + " (no re-inspection)");
        tx.setCurrentRole(ieRole);
        tx.setNextRole(ieRole);
        tx.setPoiCode(srcTx != null ? srcTx.getPoiCode() : null);
        tx.setRio(firstNonBlank(srcTx != null ? srcTx.getRio() : null, req.getRioId(), null));
        tx.setAssignedToUser(ie.getUserId().longValue());
        tx.setVendorCode(saved.getVendorCode());
        tx.setPlantId(saved.getPlantId());
        tx.setCreatedBy(actor.getUserId() != null ? actor.getUserId().longValue() : null);
        tx.setCreatedDate(now);
        workflowRepository.save(tx);

        RailInspectionCompleteDetails details = new RailInspectionCompleteDetails();
        details.setCallNo(saved.getCallNo());
        details.setPoNo(saved.getPoNo());
        details.setCertificateNo(certificateNo(tx.getRio(), saved.getCallNo(), ie.getShortName()));
        details.setCreatedOn(now);
        completeDetailsRepository.save(details);
        return saved;
    }

    // ================= Basket allocation for DIVERTED calls =================

    @Override
    public void reserveForCall(String callNo, String divertedCallNo, BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) throw invalid("Offered quantity must be more than 0");
        List<RailDivertedPassedBasket> rows = basketRepository.lockByDivertedCallNo(callNoOf(divertedCallNo));
        if (rows.isEmpty()) throw invalid("No diverted balance found for " + divertedCallNo);
        if (!isIcIssued(rows.get(0).getDivertedCallNo())) throw invalid("The diverted IC " + divertedCallNo + " is not issued yet");
        RailInspectionCall call = callRepository.findByCallNo(callNo).orElse(null);
        RailDivertedPassedBasket first = rows.get(0);
        if (call != null && (!first.getTargetPoNo().equals(call.getPoNo()) || !first.getTargetPoSrNo().equals(call.getPoSr()))) {
            throw invalid("Diverted IC " + divertedCallNo + " belongs to PO " + first.getTargetPoNo() + " / " + first.getTargetPoSrNo());
        }
        // A diverted Final IC is already the final IC on the target PO; only a diverted Process IC feeds new (Final) calls
        if (first.getStage() != DiversionStage.PROCESS) {
            throw invalid("Diverted IC " + divertedCallNo + " is a Final IC; no further call is needed on it");
        }
        if (call != null && !"FINAL".equalsIgnoreCase(call.getCallType())) {
            throw invalid("A diverted Process IC can only be used for a Final call");
        }
        BigDecimal available = sumB(rows, RailDivertedPassedBasket::getAvailableBalanceQty);
        if (qty.compareTo(available) > 0) {
            throw invalid("Offered qty " + plain(qty) + " is more than the diverted balance " + plain(available) + " of " + divertedCallNo);
        }
        BigDecimal left = qty;
        for (RailDivertedPassedBasket b : rows) {
            if (left.signum() <= 0) break;
            BigDecimal take = left.min(b.getAvailableBalanceQty());
            if (take.signum() <= 0) continue;
            b.setAllocatedCallQty(b.getAllocatedCallQty().add(take));
            b.setAvailableBalanceQty(b.getAvailableBalanceQty().subtract(take));
            b.setStatus(b.getAvailableBalanceQty().signum() == 0 ? BasketStatus.LOCKED : BasketStatus.AVAILABLE);
            basketRepository.save(b);
            allocationRepository.save(RailDiversionCallAllocation.builder().callNo(callNo).basket(b).allocatedQty(take).build());
            left = left.subtract(take);
        }
    }

    /** IC issued for a DIVERTED call: accepted qty is consumed, the rejected part goes back to the basket. */
    @Override
    public void consumeForCall(String callNo, BigDecimal acceptedQty) {
        List<RailDiversionCallAllocation> allocs = allocationRepository.findByCallNoAndStatus(callNo, RailDiversionCallAllocation.RESERVED);
        if (allocs.isEmpty()) return;
        BigDecimal toConsume = acceptedQty != null ? acceptedQty : acceptedFromLotResults(callNo);
        for (RailDiversionCallAllocation al : allocs) {
            RailDivertedPassedBasket b = basketRepository.lockByDivertedCallNo(al.getBasket().getDivertedCallNo()).stream()
                    .filter(x -> x.getId().equals(al.getBasket().getId())).findFirst().orElse(al.getBasket());
            BigDecimal consume = toConsume == null ? al.getAllocatedQty() : toConsume.min(al.getAllocatedQty());
            BigDecimal giveBack = al.getAllocatedQty().subtract(consume);
            b.setAllocatedCallQty(floor0(b.getAllocatedCallQty().subtract(al.getAllocatedQty())));
            b.setConsumedIcQty(b.getConsumedIcQty().add(consume));
            b.setAvailableBalanceQty(b.getAvailableBalanceQty().add(giveBack));
            b.setStatus(basketStatus(b));
            basketRepository.save(b);
            al.setConsumedQty(consume);
            al.setStatus(RailDiversionCallAllocation.CONSUMED_IN_IC);
            allocationRepository.save(al);
            if (toConsume != null) toConsume = floor0(toConsume.subtract(consume));
        }
    }

    /** DIVERTED call cancelled / withdrawn: its reserved qty goes back to the basket. */
    @Override
    public void releaseForCall(String callNo) {
        for (RailDiversionCallAllocation al : allocationRepository.findByCallNoAndStatus(callNo, RailDiversionCallAllocation.RESERVED)) {
            RailDivertedPassedBasket b = basketRepository.lockByDivertedCallNo(al.getBasket().getDivertedCallNo()).stream()
                    .filter(x -> x.getId().equals(al.getBasket().getId())).findFirst().orElse(al.getBasket());
            b.setAllocatedCallQty(floor0(b.getAllocatedCallQty().subtract(al.getAllocatedQty())));
            b.setAvailableBalanceQty(b.getAvailableBalanceQty().add(al.getAllocatedQty()));
            b.setStatus(basketStatus(b));
            basketRepository.save(b);
            al.setStatus(RailDiversionCallAllocation.RELEASED);
            allocationRepository.save(al);
        }
    }

    /** Accepted qty of a Final call: NCRGRSP counts sets (max across lots), other types sum the lots. Null if no results. */
    private BigDecimal acceptedFromLotResults(String callNo) {
        List<RailFinalInspectionLotResults> lots = lotResultsRepository.findAllByCallNo(callNo);
        if (lots.isEmpty()) return null;
        boolean ncr = lots.stream().anyMatch(l -> l.getRailpadType() != null && l.getRailpadType().toUpperCase().contains("NCRGRSP"));
        int v = ncr ? lots.stream().mapToInt(l -> nz(l.getAcceptedQty())).max().orElse(0)
                    : lots.stream().mapToInt(l -> nz(l.getAcceptedQty())).sum();
        return BigDecimal.valueOf(v);
    }

    private static BasketStatus basketStatus(RailDivertedPassedBasket b) {
        if (b.getAvailableBalanceQty().signum() > 0) return BasketStatus.AVAILABLE;
        return b.getAllocatedCallQty().signum() > 0 ? BasketStatus.LOCKED : BasketStatus.EXHAUSTED;
    }

    // ================= Helpers =================

    private RailDiversionRequest forAction(ActionDto a, DiversionRequestStatus... allowed) {
        if (a == null || a.getRequestId() == null) throw invalid("Request id is required");
        RailDiversionRequest req = requestRepository.findById(a.getRequestId()).orElseThrow(() -> invalid("Diversion request not found"));
        if (!Arrays.asList(allowed).contains(req.getStatus())) {
            throw invalid("Action not allowed in status " + req.getStatus());
        }
        return req;
    }

    private void ownCmCheck(RailDiversionRequest req, UserMaster cm) {
        if (!String.valueOf(cm.getUserId()).equals(req.getCmUserId())) throw unauthorized("This request is assigned to another Controlling Manager");
    }

    private RailDiversionRequest move(RailDiversionRequest req, UserMaster actor, String role, String action,
                                      DiversionRequestStatus to, String remarks) {
        DiversionRequestStatus from = req.getStatus();
        req.setStatus(to);
        req.setUpdatedBy(String.valueOf(actor.getUserId()));
        RailDiversionRequest saved = requestRepository.save(req);
        history(saved, role, actor, action, from, to, remarks);
        return saved;
    }

    private void history(RailDiversionRequest req, String role, UserMaster user, String action,
                         DiversionRequestStatus from, DiversionRequestStatus to, String remarks) {
        historyRepository.save(RailDiversionWorkflowHistory.builder()
                .diversionRequest(req).actionRole(role).actionUserId(String.valueOf(user.getUserId()))
                .actionUserName(firstNonBlank(user.getFullName(), user.getUsername(), null))
                .action(action).fromStatus(from == null ? null : from.name()).toStatus(to.name()).remarks(remarks).build());
    }

    private RailDiversionRequest ownRequest(Long id, UserMaster user) {
        RailDiversionRequest req = requestRepository.findById(id).orElseThrow(() -> invalid("Diversion request not found"));
        if (!clean(req.getVendorCode()).equalsIgnoreCase(vendorCode(user))) throw unauthorized("This request belongs to another vendor");
        return req;
    }

    private boolean isIcIssued(String callNo) {
        return workflowRepository.findByRequestIdOrderByWorkflowTransitionIdDesc(callNo).stream()
                .anyMatch(t -> {
                    String act = String.valueOf(t.getAction()).toUpperCase();
                    return act.equals("GENERATE_IC") || act.equals("IC_GENERATION") || act.equals("DSC_SIGN_IC")
                            || act.contains("SEND_CALL_TO_IBS") || act.contains("SENT_TO_IBS");
                });
    }

    /**
     * Same as ERC: the request number is a normal call number from the regular Rail Pad sequence
     * (RPP-yyMMddNN / RPF-yyMMddNN). The diversion number is "D" + it, and the diverted call created on
     * approval uses this same number.
     */
    private String generateRequestNo(DiversionStage stage) {
        return nextCallNo(stage == DiversionStage.PROCESS ? "RPP-" : "RPF-");
    }

    /** Same numbering as RailInspectionCallServiceImpl.create (PREFIX-yyMMddNN, unique in calls and workflow). */
    private String nextCallNo(String prefix) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int seq = 1;
        Optional<String> last = callRepository.findLastCallNoByPattern(prefix + datePart + "%");
        if (last.isPresent()) {
            try { seq = Integer.parseInt(last.get().substring(last.get().length() - 2)) + 1; } catch (Exception ignored) { }
        }
        String no = String.format("%s%s%02d", prefix, datePart, seq);
        // a number held by a diversion request (reserved for its diverted call) is not reused
        while (callRepository.existsByCallNo(no) || workflowRepository.existsByRequestId(no) || requestRepository.existsByRequestNo(no)) {
            seq++;
            no = String.format("%s%s%02d", prefix, datePart, seq);
        }
        return no;
    }

    private static String certificateNo(String rio, String callNo, String ieShortName) {
        String r = (rio != null && !rio.isBlank()) ? rio.trim().substring(0, 1).toUpperCase() : "X";
        String s = (ieShortName != null && !ieShortName.isBlank()) ? ieShortName.trim().toUpperCase() : "XX";
        return r + "/" + callNo + "/" + s;
    }

    /** Certificate no. of a call (falls back to what was passed in). */
    private String certificateNoOf(String callNo, String fallback) {
        return completeDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(callNo)
                .map(RailInspectionCompleteDetails::getCertificateNo)
                .filter(s -> s != null && !s.isBlank())
                .orElse(fallback);
    }

    private static String callNoOf(String icOrCallNo) {
        if (icOrCallNo == null) throw new InvalidInputException(err("Source IC is required"));
        Matcher m = CALL_NO.matcher(icOrCallNo);
        return m.find() ? m.group(1).toUpperCase() : icOrCallNo.trim();
    }

    private UserMaster currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserMaster u) || u.getUserId() == null) {
            throw unauthorized("Please log in again");
        }
        return u;
    }

    private UserMaster requireRole(Set<String> roles, String message) {
        UserMaster u = currentUser();
        if (!hasAnyRole(u, roles)) throw unauthorized(message);
        return u;
    }

    private boolean hasAnyRole(UserMaster u, Set<String> roles) {
        List<String> mine = userMasterRepository.findRoleNamesByUserId(u.getUserId());
        return mine != null && mine.stream().filter(Objects::nonNull).map(String::trim).anyMatch(roles::contains);
    }

    /** Vendor login name is the vendor code (stored with or without a leading ':'). */
    private static String vendorCode(UserMaster u) {
        return clean(u.getUsername());
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace(":", "").trim();
    }

    private static String lineKey(String lot, String batch) {
        return (lot == null ? "" : lot.trim().toUpperCase()) + "|" + (batch == null ? "" : batch.trim().toUpperCase());
    }

    private static String label(RailDiversionItem it) {
        return it.getLotNo() != null ? "lot " + it.getLotNo() : "batch " + it.getBatchNo();
    }

    private static DiversionStage parseStage(String stage) {
        try {
            DiversionStage s = DiversionStage.valueOf(String.valueOf(stage).trim().toUpperCase());
            if (s == DiversionStage.RAW_MATERIAL) throw new IllegalArgumentException();
            return s;
        } catch (Exception e) {
            throw new InvalidInputException(err("Stage must be PROCESS or FINAL"));
        }
    }

    private static void require(String v, String field) {
        if (v == null || v.isBlank()) throw new InvalidInputException(err(field + " is required"));
    }

    private static String firstNonBlank(String a, String b, String c) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return c;
    }

    private static String str(Object o) {
        return o == null ? null : o.toString().trim();
    }

    private static LocalDate toDate(Object o) {
        if (o instanceof Timestamp t) return t.toLocalDateTime().toLocalDate();
        if (o instanceof LocalDateTime d) return d.toLocalDate();
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof LocalDate d) return d;
        return null;
    }

    private static BigDecimal toBig(Object o) {
        return o == null ? BigDecimal.ZERO : new BigDecimal(o.toString());
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private static BigDecimal floor0(BigDecimal v) {
        return v.signum() < 0 ? BigDecimal.ZERO : v;
    }

    private static String plain(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }

    private static BigDecimal sum(List<SourceLineDto> l, java.util.function.Function<SourceLineDto, BigDecimal> f) {
        return l.stream().map(f).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumB(List<RailDivertedPassedBasket> l, java.util.function.Function<RailDivertedPassedBasket, BigDecimal> f) {
        return l.stream().map(f).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static ErrorDetails err(String msg) {
        return new ErrorDetails(AppConstant.INVALID_INPUT, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, msg);
    }

    private static InvalidInputException invalid(String msg) {
        return new InvalidInputException(err(msg));
    }

    private static UnauthorizedException unauthorized(String msg) {
        return new UnauthorizedException(new ErrorDetails(AppConstant.UNAUTHORIZED_ACTION,
                AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, msg));
    }
}
