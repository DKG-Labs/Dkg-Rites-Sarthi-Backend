package com.sarthi.SRailPad.service.Impl.inspectionCall;

import com.sarthi.SRailPad.dto.RailProcessCallDto;
import com.sarthi.SRailPad.dto.RailProcessCallUpdateDto;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallDetails;
import com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallHistory;
import com.sarthi.SRailPad.repository.inspectionCall.RailProcessCallDetailsRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailProcessCallHistoryRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailProcessInspectionResultRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailProcessInspectionBatchRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionBatchRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.RailProductionBatchRepository;
import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionBatch;
import com.sarthi.SRailPad.entity.inspectionCall.RailProcessInspectionResult;
import com.sarthi.SRailPad.entity.inspectionCall.RailProcessInspectionBatch;
import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionVerification;
import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionRejection;
import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo;
import com.sarthi.SRailPad.repository.ieVerification.RailIEProductionVerificationRepository;
import com.sarthi.SRailPad.dto.inspectionCall.ProcessAvailableBatchDto;
import com.sarthi.SRailPad.dto.inspectionCall.ProcessInspectionSaveDto;
import com.sarthi.SRailPad.service.inspectionCall.RailProcessCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RailProcessCallServiceImpl implements RailProcessCallService {

    private final RailProcessCallDetailsRepository processCallDetailsRepository;
    private final RailProcessCallHistoryRepository historyRepository;
    private final RailInspectionCallRepository inspectionCallRepository;

    /** Material diversion: batches of a diverted-passed Process call come from its diversion request. */
    @org.springframework.beans.factory.annotation.Autowired
    private com.sarthi.SRailPad.diversion.repository.RailDiversionRequestRepository railDiversionRequestRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.sarthi.SRailPad.diversion.repository.RailDiversionItemRepository railDiversionItemRepository;

    /** Diversion requests that hold quantity (same set as RailDiversionServiceImpl). */
    private static final java.util.Set<com.sarthi.enums.DiversionRequestStatus> DIVERSION_HOLDING = java.util.EnumSet.of(
            com.sarthi.enums.DiversionRequestStatus.PENDING_CM_APPROVAL, com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION,
            com.sarthi.enums.DiversionRequestStatus.SENT_TO_SBU_HEAD, com.sarthi.enums.DiversionRequestStatus.PENDING_SBU_APPROVAL,
            com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION, com.sarthi.enums.DiversionRequestStatus.CM_APPROVE_DIVERSION,
            com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_APPROVE_DIVERSION, com.sarthi.enums.DiversionRequestStatus.BASKET_CREATED,
            com.sarthi.enums.DiversionRequestStatus.COMPLETED);
    private final RailProductionBatchRepository productionBatchRepository;
    private final RailProcessInspectionResultRepository processInspectionResultRepository;
    private final RailProcessInspectionBatchRepository processInspectionBatchRepository;
    private final RailIEProductionVerificationRepository verificationRepository;
    private final RailInspectionBatchRepository railInspectionBatchRepository;

    @Autowired
    private EntityManager entityManager;

    @jakarta.annotation.PostConstruct
    @Transactional
    public void initSchema() {
        try {
            String[] cols = {"offered_sets", "accepted_sets", "rejected_sets"};
            for (String col : cols) {
                try {
                    entityManager.createNativeQuery("ALTER TABLE rail_process_inspection_result ADD COLUMN IF NOT EXISTS " + col + " INT NULL").executeUpdate();
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    @Override
    public RailProcessCallDto getProcessCallDetails(String callNo) {
        RailProcessCallDetails details = processCallDetailsRepository.findByInspectionCall_CallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Process Call not found with callNo: " + callNo));
        
        RailInspectionCall call = details.getInspectionCall();
        
        RailProcessCallDto dto = new RailProcessCallDto();
        dto.setCallNo(call.getCallNo());
        dto.setPoNo(call.getPoNo());
        dto.setPoSr(call.getPoSr());
        dto.setVendorCode(call.getVendorCode());
        dto.setPlantId(call.getPlantId());
        dto.setRailPadType(call.getRailPadType());
        dto.setTotalQty(call.getTotalQty());
        dto.setStatus(call.getStatus());
        dto.setCreatedAt(call.getCreatedAt());
        
        dto.setDrawingNo(details.getDrawingNo());
        dto.setUom(details.getUom());
        dto.setQtyOnOrder(details.getQtyOnOrder());
        dto.setQtyAcceptedTillNow(details.getQtyAcceptedTillNow());
        dto.setQtyDesiredForFinal(details.getQtyDesiredForFinal());
        dto.setQtyDue(details.getQtyDue());
        dto.setProductionInitiationDate(details.getProductionInitiationDate());
        
        return dto;
    }

    @Override
    @Transactional
    public RailProcessCallDto updateProcessCallDetails(String callNo, RailProcessCallUpdateDto updateDto) {
        RailProcessCallDetails details = processCallDetailsRepository.findByInspectionCall_CallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Process Call not found with callNo: " + callNo));
        
        RailInspectionCall call = details.getInspectionCall();

        // Track and update Drawing No
        if (updateDto.getDrawingNo() != null && !Objects.equals(details.getDrawingNo(), updateDto.getDrawingNo())) {
            recordHistory(details, "drawing_no", details.getDrawingNo(), updateDto.getDrawingNo(), updateDto.getUserId());
            details.setDrawingNo(updateDto.getDrawingNo());
        }

        // Track and update Qty Desired
        if (updateDto.getQtyDesiredForFinal() != null && !Objects.equals(details.getQtyDesiredForFinal(), updateDto.getQtyDesiredForFinal())) {
            recordHistory(details, "qty_desired_for_final", String.valueOf(details.getQtyDesiredForFinal()), String.valueOf(updateDto.getQtyDesiredForFinal()), updateDto.getUserId());
            details.setQtyDesiredForFinal(updateDto.getQtyDesiredForFinal());
            
            // Also update totalQty in parent table
            recordHistory(details, "total_qty", String.valueOf(call.getTotalQty()), String.valueOf(updateDto.getQtyDesiredForFinal()), updateDto.getUserId());
            call.setTotalQty(updateDto.getQtyDesiredForFinal());
            
            // Re-calculate qty due
            int newDue = details.getQtyOnOrder() - (details.getQtyAcceptedTillNow() != null ? details.getQtyAcceptedTillNow() : 0) - updateDto.getQtyDesiredForFinal();
            details.setQtyDue(Math.max(0, newDue));
        }

        // Track and update Production Date
        if (updateDto.getProductionInitiationDate() != null && !Objects.equals(details.getProductionInitiationDate(), updateDto.getProductionInitiationDate())) {
            recordHistory(details, "production_initiation_date", 
                    details.getProductionInitiationDate() != null ? details.getProductionInitiationDate().toString() : null, 
                    updateDto.getProductionInitiationDate().toString(), 
                    updateDto.getUserId());
            details.setProductionInitiationDate(updateDto.getProductionInitiationDate());
            call.setInspectionDate(updateDto.getProductionInitiationDate());
        }

        details.setUpdatedBy(updateDto.getUserId());
        call.setUpdatedBy(updateDto.getUserId());

        processCallDetailsRepository.save(details);
        inspectionCallRepository.save(call);

        return getProcessCallDetails(callNo);
    }

    /**
     * When an IE verification is unblocked / re-saved its batches get new ids, and Process calls that used the
     * old ids no longer reduce the balance (a used batch shows full again). Carry that usage over to the current
     * batch row with the same production date + batch no (+ drawing no) of this PO.
     */
    private void addUsageOnDeletedBatchIds(List<Object[]> results, String poNo, String callNo,
                                           java.util.Map<Long, Integer> acceptedMap, java.util.Map<Long, Integer> rejectedMap) {
        try {
            java.util.Map<String, Long> liveByKey = new java.util.HashMap<>();
            java.util.Set<java.time.LocalDate> dates = new java.util.HashSet<>();
            java.util.Set<String> batchNos = new java.util.HashSet<>();
            java.util.Set<Long> liveIds = new java.util.HashSet<>();
            for (Object[] row : results) {
                com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo i = (com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo) row[0];
                RailIEProductionVerification v = (RailIEProductionVerification) row[1];
                if (i == null || i.getId() == null || i.getBatchNo() == null || v == null || v.getCastingDate() == null) continue;
                liveIds.add(i.getId());
                dates.add(v.getCastingDate());
                batchNos.add(i.getBatchNo());
                liveByKey.putIfAbsent(usageKey(v.getCastingDate(), i.getBatchNo(), i.getDrawingNo()), i.getId());
                liveByKey.putIfAbsent(usageKey(v.getCastingDate(), i.getBatchNo(), null), i.getId());
            }
            if (liveIds.isEmpty() || poNo == null || poNo.isBlank()) return;

            List<RailProcessInspectionBatch> orphans = processInspectionBatchRepository.findUsageOnDeletedBatchIds(
                    poNo.trim(), dates, batchNos, liveIds, callNo != null ? callNo.trim() : "");
            for (RailProcessInspectionBatch pb : orphans) {
                Long liveId = liveByKey.get(usageKey(pb.getProductionDate(), pb.getBatchNo(), pb.getDrawingNo()));
                if (liveId == null) liveId = liveByKey.get(usageKey(pb.getProductionDate(), pb.getBatchNo(), null));
                if (liveId == null) continue;
                acceptedMap.merge(liveId, pb.getQtyAccepted() != null ? pb.getQtyAccepted() : 0, Integer::sum);
                rejectedMap.merge(liveId, pb.getQtyRejected() != null ? pb.getQtyRejected() : 0, Integer::sum);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RailProcessCallServiceImpl.class)
                    .warn("Could not add usage on deleted batch ids for PO {}: {}", poNo, e.getMessage());
        }
    }

    private static String usageKey(java.time.LocalDate date, String batchNo, String drawingNo) {
        String b = batchNo == null ? "" : batchNo.replaceAll("\\s+", "").toUpperCase();
        String d = drawingNo == null ? "*" : drawingNo.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return date + "|" + b + "|" + d;
    }

    @Override
    public ProcessAvailableBatchDto getAvailableBatchesForProcessIc(String poNo, String railPadType, String callNo) {
        List<Object[]> results = verificationRepository.findAvailableInfosForProcessIc(poNo, railPadType);

        List<Long> declarationBatchIds = new ArrayList<>();
        java.util.Set<Long> vIds = new java.util.HashSet<>();
        for (Object[] row : results) {
            com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo i = (com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo) row[0];
            RailIEProductionVerification v = (RailIEProductionVerification) row[1];
            if (i.getId() != null) {
                declarationBatchIds.add(i.getId());
            }
            if (v != null && v.getId() != null) {
                vIds.add(v.getId());
            }
        }

        // Batch-fetch all verification rejections in 1 bulk query upfront (prevents N+1 lazy loading)
        Map<Long, List<RailIEProductionRejection>> rejectionsByVIdMap = new java.util.HashMap<>();
        if (!vIds.isEmpty()) {
            try {
                List<RailIEProductionRejection> allRejections = entityManager
                        .createQuery("SELECT r FROM RailIEProductionRejection r LEFT JOIN FETCH r.productionInfo WHERE r.verification.id IN :vIds", RailIEProductionRejection.class)
                        .setParameter("vIds", vIds)
                        .getResultList();
                for (RailIEProductionRejection r : allRejections) {
                    if (r.getVerification() != null && r.getVerification().getId() != null) {
                        rejectionsByVIdMap.computeIfAbsent(r.getVerification().getId(), k -> new ArrayList<>()).add(r);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Query historical inspections across other calls to calculate cumulative consumption and rejection
        java.util.Map<Long, Integer> acceptedInOtherCallsMap = new java.util.HashMap<>();
        java.util.Map<Long, Integer> rejectedInOtherCallsMap = new java.util.HashMap<>();

        if (!declarationBatchIds.isEmpty()) {
            List<RailProcessInspectionBatch> pastBatches = processInspectionBatchRepository
                    .findByDeclarationBatchIdInAndExcludeCallNo(declarationBatchIds, callNo != null ? callNo.trim() : "");
            for (RailProcessInspectionBatch pb : pastBatches) {
                Long dId = pb.getDeclarationBatchId();
                if (dId != null) {
                    int acc = pb.getQtyAccepted() != null ? pb.getQtyAccepted() : 0;
                    int rej = pb.getQtyRejected() != null ? pb.getQtyRejected() : 0;
                    acceptedInOtherCallsMap.put(dId, acceptedInOtherCallsMap.getOrDefault(dId, 0) + acc);
                    rejectedInOtherCallsMap.put(dId, rejectedInOtherCallsMap.getOrDefault(dId, 0) + rej);
                }
            }
            addUsageOnDeletedBatchIds(results, poNo, callNo, acceptedInOtherCallsMap, rejectedInOtherCallsMap);
        }

        // Rejection still to apply per verification (production entry):
        //   total rejection recorded by the IE  -  rejection already applied by other calls on ANY batch of it.
        // Counting per verification (not per batch) keeps the total right when the IE unblocks and re-enters
        // the rejections with a different batch split after some batches were already used in a call.
        java.util.Map<Long, Integer> rejectionPoolByVId = new java.util.HashMap<>();
        {
            java.util.Map<Long, Integer> appliedByVId = new java.util.HashMap<>();
            java.util.Set<Long> countedInfoIds = new java.util.HashSet<>();
            for (Object[] row : results) {
                com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo i = (com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo) row[0];
                RailIEProductionVerification v = (RailIEProductionVerification) row[1];
                if (v == null || v.getId() == null || i.getId() == null || !countedInfoIds.add(i.getId())) continue;
                appliedByVId.merge(v.getId(), rejectedInOtherCallsMap.getOrDefault(i.getId(), 0), Integer::sum);
            }
            for (Map.Entry<Long, List<RailIEProductionRejection>> e : rejectionsByVIdMap.entrySet()) {
                int total = e.getValue().stream().mapToInt(r -> r.getRejectedQty() != null ? r.getRejectedQty() : 0).sum();
                rejectionPoolByVId.put(e.getKey(), Math.max(0, total - appliedByVId.getOrDefault(e.getKey(), 0)));
            }
        }

        ProcessAvailableBatchDto dto = new ProcessAvailableBatchDto();
        List<ProcessAvailableBatchDto.ProcessBatchDetailDto> batchDtos = new ArrayList<>();

        for (Object[] row : results) {
            com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo i = (com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo) row[0];
            RailIEProductionVerification v = (RailIEProductionVerification) row[1];
            com.sarthi.SRailPad.entity.plantDeclaration.RailProductionDeclaration d = (com.sarthi.SRailPad.entity.plantDeclaration.RailProductionDeclaration) row[2];

            int produced = i.getQuantityProduced() != null ? i.getQuantityProduced() : 0;
            int alreadyAccepted = acceptedInOtherCallsMap.getOrDefault(i.getId(), 0);
            int alreadyRejected = rejectedInOtherCallsMap.getOrDefault(i.getId(), 0);
            int available = Math.max(0, produced - alreadyAccepted - alreadyRejected);

            // If completely consumed in previous calls, do not show in available batches
            if (available <= 0) {
                continue;
            }

            ProcessAvailableBatchDto.ProcessBatchDetailDto bd = new ProcessAvailableBatchDto.ProcessBatchDetailDto();
            bd.setDeclarationBatchId(i.getId());
            bd.setBatchNo(i.getBatchNo());
            bd.setQtyManufactured(produced);
            bd.setAvailableQty(available);
            bd.setAlreadyAcceptedQty(alreadyAccepted);
            bd.setProductionDate(v.getCastingDate() != null ? v.getCastingDate() : d.getProductionDate());
            bd.setDrawingNo(i.getDrawingNo());

            // Check original rejections from verification
            List<RailIEProductionRejection> vRejections = (v != null && v.getId() != null)
                    ? rejectionsByVIdMap.getOrDefault(v.getId(), java.util.Collections.emptyList())
                    : java.util.Collections.emptyList();

            if (!vRejections.isEmpty()) {
                List<com.sarthi.SRailPad.entity.ieVerification.RailIEProductionRejection> batchRejections = vRejections.stream()
                        .filter(r -> {
                            // 1. Batch matching
                            boolean batchMatches = false;
                            if (r.getBatchNo() != null && i.getBatchNo() != null && r.getBatchNo().trim().equals(i.getBatchNo().trim())) {
                                batchMatches = true;
                            } else if (r.getProductionInfo() != null && r.getProductionInfo().getId() != null && r.getProductionInfo().getId().equals(i.getId())) {
                                batchMatches = true;
                            }
                            if (!batchMatches) {
                                return false;
                            }

                            // 2. Drawing matching (crucial for multi-drawing NCRGRSP batches)
                            if (i.getDrawingNo() != null && !i.getDrawingNo().isBlank() &&
                                r.getDrawingNo() != null && !r.getDrawingNo().isBlank()) {
                                String normI = i.getDrawingNo().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
                                String normR = r.getDrawingNo().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
                                return normI.equals(normR);
                            }
                            return true;
                        })
                        .collect(java.util.stream.Collectors.toList());

                if (!batchRejections.isEmpty()) {
                    int originalRejectedQty = batchRejections.stream()
                            .mapToInt(r -> r.getRejectedQty() != null ? r.getRejectedQty() : 0)
                            .sum();

                    // Rejections are consumed once: what this batch still owes, capped by what is still
                    // unapplied for the whole verification (so a re-split after unblock is not counted twice)
                    int pool = rejectionPoolByVId.getOrDefault(v.getId(), 0);
                    int remainingRejection = Math.min(Math.max(0, originalRejectedQty - alreadyRejected), pool);
                    if (remainingRejection > 0) {
                        rejectionPoolByVId.put(v.getId(), pool - remainingRejection);
                        String combinedReasons = batchRejections.stream()
                                .map(r -> r.getReason())
                                .filter(reason -> reason != null && !reason.isBlank())
                                .distinct()
                                .collect(java.util.stream.Collectors.joining(", "));
                        bd.setVerificationRejectedQty(remainingRejection);
                        bd.setVerificationRejectedReason(combinedReasons);

                        List<ProcessAvailableBatchDto.RejectionDetailDto> rejectionDtos;
                        if (remainingRejection == originalRejectedQty) {
                            rejectionDtos = batchRejections.stream().map(r -> {
                                ProcessAvailableBatchDto.RejectionDetailDto rDto = new ProcessAvailableBatchDto.RejectionDetailDto();
                                rDto.setDrawingNo(r.getDrawingNo());
                                rDto.setReason(r.getReason());
                                rDto.setRejectedQty(r.getRejectedQty());
                                return rDto;
                            }).collect(java.util.stream.Collectors.toList());
                        } else {
                            // only part of it is still due: one line so the detail adds up to the total shown
                            ProcessAvailableBatchDto.RejectionDetailDto rDto = new ProcessAvailableBatchDto.RejectionDetailDto();
                            rDto.setDrawingNo(i.getDrawingNo());
                            rDto.setReason(combinedReasons);
                            rDto.setRejectedQty(remainingRejection);
                            rejectionDtos = java.util.List.of(rDto);
                        }
                        bd.setRejections(rejectionDtos);
                    } else {
                        bd.setVerificationRejectedQty(0);
                        bd.setVerificationRejectedReason(null);
                        bd.setRejections(java.util.Collections.emptyList());
                    }
                } else {
                    bd.setVerificationRejectedQty(0);
                    bd.setVerificationRejectedReason(null);
                    bd.setRejections(java.util.Collections.emptyList());
                }
            } else {
                bd.setVerificationRejectedQty(0);
                bd.setVerificationRejectedReason(null);
                bd.setRejections(java.util.Collections.emptyList());
            }

            batchDtos.add(bd);
        }

        dto.setTotalBatchesCount(batchDtos.size());
        dto.setBatches(batchDtos);
        return dto;
    }

    @Override
    @Transactional
    public void saveProcessInspectionResult(ProcessInspectionSaveDto saveDto) {
        RailInspectionCall call = inspectionCallRepository.findByCallNo(saveDto.getCallNo())
                .orElseThrow(() -> new RuntimeException("Process Call not found: " + saveDto.getCallNo()));

        RailProcessInspectionResult result = processInspectionResultRepository.findByInspectionCall_CallNo(saveDto.getCallNo())
                .orElse(new RailProcessInspectionResult());

        result.setInspectionCall(call);
        result.setCallQty(saveDto.getCallQty());
        result.setTotalManufacturedQty(saveDto.getTotalManufacturedQty());
        result.setTotalRejectedQty(saveDto.getTotalRejectedQty());
        result.setTotalAcceptedQty(saveDto.getTotalAcceptedQty());
        result.setOfferedSets(saveDto.getOfferedSets());
        result.setAcceptedSets(saveDto.getAcceptedSets());
        result.setRejectedSets(saveDto.getRejectedSets());
        result.setReasonForRejection(saveDto.getReasonForRejection());
        result.setLotRangeFrom(saveDto.getLotRangeFrom());
        result.setLotRangeTo(saveDto.getLotRangeTo());
        result.setRemarks(saveDto.getRemarks());
        result.setInspectionStartDate(saveDto.getInspectionStartDate());
        result.setInspectionEndDate(saveDto.getInspectionEndDate());
        if (result.getId() == null) {
            result.setCreatedBy(saveDto.getCreatedBy());
        } else {
            result.setUpdatedBy(saveDto.getUpdatedBy());
        }
        result.setShift(saveDto.getShift());
        result.setInspectionDate(saveDto.getInspectionDate());

        if (result.getBatches() != null) {
            result.getBatches().clear();
        } else {
            result.setBatches(new ArrayList<>());
        }

        int totalCalculatedAccepted = 0;
        int totalCalculatedManufactured = 0;
        int totalCalculatedRejected = 0;

        // Batch-fetch all RailIEProductionInfo entries upfront in 1 bulk query (prevents N+1 queries in save loop)
        List<Long> decBatchIds = (saveDto.getBatches() != null)
                ? saveDto.getBatches().stream()
                        .map(ProcessInspectionSaveDto.ProcessBatchSaveDto::getDeclarationBatchId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        Map<Long, RailIEProductionInfo> batchInfoMap = new java.util.HashMap<>();
        if (!decBatchIds.isEmpty()) {
            try {
                List<RailIEProductionInfo> infos = entityManager
                        .createQuery("SELECT i FROM RailIEProductionInfo i LEFT JOIN FETCH i.verification v LEFT JOIN FETCH v.rejections WHERE i.id IN :ids", RailIEProductionInfo.class)
                        .setParameter("ids", decBatchIds)
                        .getResultList();
                for (RailIEProductionInfo info : infos) {
                    batchInfoMap.put(info.getId(), info);
                }
            } catch (Exception ignored) {}
        }

        if (saveDto.getBatches() != null) {
            for (ProcessInspectionSaveDto.ProcessBatchSaveDto bDto : saveDto.getBatches()) {
                RailProcessInspectionBatch batch = new RailProcessInspectionBatch();
                batch.setResult(result);
                batch.setDeclarationBatchId(bDto.getDeclarationBatchId());
                batch.setBatchNo(bDto.getBatchNo());

                String drawingNo = bDto.getDrawingNo();
                String batchRejectionReason = bDto.getReasonForRejection();

                if (bDto.getDeclarationBatchId() != null) {
                    RailIEProductionInfo info = batchInfoMap.get(bDto.getDeclarationBatchId());
                    if (info != null) {
                        if (drawingNo == null || drawingNo.isBlank()) {
                            drawingNo = info.getDrawingNo();
                        }
                        if ((batchRejectionReason == null || batchRejectionReason.isBlank()) && bDto.getQtyRejected() != null && bDto.getQtyRejected() > 0) {
                            if (info.getVerification() != null && info.getVerification().getRejections() != null) {
                                String rejs = info.getVerification().getRejections().stream()
                                        .filter(r -> {
                                            boolean batchMatches = false;
                                            if (r.getBatchNo() != null && info.getBatchNo() != null && r.getBatchNo().trim().equals(info.getBatchNo().trim())) {
                                                batchMatches = true;
                                            } else if (r.getProductionInfo() != null && r.getProductionInfo().getId() != null && r.getProductionInfo().getId().equals(info.getId())) {
                                                batchMatches = true;
                                            }
                                            if (!batchMatches) return false;

                                            if (info.getDrawingNo() != null && !info.getDrawingNo().isBlank() &&
                                                r.getDrawingNo() != null && !r.getDrawingNo().isBlank()) {
                                                String normI = info.getDrawingNo().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
                                                String normR = r.getDrawingNo().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
                                                return normI.equals(normR);
                                            }
                                            return true;
                                        })
                                        .map(r -> (r.getReason() != null ? r.getReason() : "Rejected") + (r.getRejectedQty() != null ? " (" + r.getRejectedQty() + " Nos)" : ""))
                                        .distinct()
                                        .collect(java.util.stream.Collectors.joining(", "));
                                if (!rejs.isBlank()) {
                                    batchRejectionReason = rejs;
                                }
                            }
                        }
                    }
                }

                if ((drawingNo == null || drawingNo.isBlank()) && call.getDrawingNo() != null) {
                    drawingNo = call.getDrawingNo();
                }

                int mQty = bDto.getQtyManufactured() != null ? bDto.getQtyManufactured() : 0;
                int availQty = bDto.getQtyAvailable() != null ? bDto.getQtyAvailable() : mQty;
                int rejQty = bDto.getQtyRejected() != null ? bDto.getQtyRejected() : 0;
                int maxAcc = Math.max(0, availQty - rejQty);
                int accQty = bDto.getQtyAccepted() != null ? bDto.getQtyAccepted() : maxAcc;

                // Validation rules
                if (accQty > maxAcc) {
                    accQty = maxAcc;
                }

                int remQty = Math.max(0, availQty - accQty - rejQty);

                batch.setDrawingNo(drawingNo);
                batch.setReasonForRejection(batchRejectionReason);
                batch.setProductionDate(bDto.getProductionDate());
                batch.setQtyManufactured(mQty);
                batch.setQtyAvailable(availQty);
                batch.setQtyRejected(rejQty);
                batch.setQtyAccepted(accQty);
                batch.setQtyRemaining(remQty);
                result.getBatches().add(batch);

                totalCalculatedManufactured += mQty;
                totalCalculatedRejected += rejQty;
                totalCalculatedAccepted += accQty;
            }
        }

        int totalMfg = saveDto.getTotalManufacturedQty() != null ? saveDto.getTotalManufacturedQty() : totalCalculatedManufactured;
        int totalRej = saveDto.getTotalRejectedQty() != null ? saveDto.getTotalRejectedQty() : totalCalculatedRejected;
        int maxTotalAcc = Math.max(0, totalMfg - totalRej);
        int totalAcc = (saveDto.getTotalAcceptedQty() != null && saveDto.getTotalAcceptedQty() <= maxTotalAcc)
                ? saveDto.getTotalAcceptedQty()
                : (totalCalculatedAccepted <= maxTotalAcc ? totalCalculatedAccepted : maxTotalAcc);

        if (totalRej > 0 && totalAcc > maxTotalAcc) {
            totalAcc = maxTotalAcc;
        }

        result.setCallQty(saveDto.getCallQty());
        result.setTotalManufacturedQty(totalMfg);
        result.setTotalRejectedQty(totalRej);
        result.setTotalAcceptedQty(totalAcc);

        processInspectionResultRepository.save(result);

        if (Boolean.TRUE.equals(saveDto.getIsFinish())) {
            // Update the main process call details
            RailProcessCallDetails details = processCallDetailsRepository.findByInspectionCall_CallNo(saveDto.getCallNo())
                .orElseThrow(() -> new RuntimeException("Process Call Details not found"));

            int acceptedTillNow = details.getQtyAcceptedTillNow() != null ? details.getQtyAcceptedTillNow() : 0;
            details.setQtyAcceptedTillNow(acceptedTillNow + (result.getTotalAcceptedQty() != null ? result.getTotalAcceptedQty() : 0));

            int newDue = details.getQtyOnOrder() - details.getQtyAcceptedTillNow() - details.getQtyDesiredForFinal();
            details.setQtyDue(Math.max(0, newDue));
            processCallDetailsRepository.save(details);
        }
    }

    @Override
    public ProcessInspectionSaveDto getProcessInspectionResult(String callNo) {
        RailProcessInspectionResult result = processInspectionResultRepository.findByInspectionCall_CallNo(callNo)
                .orElse(null);
                
        if (result == null) {
            return null; // Return null if no draft exists
        }

        ProcessInspectionSaveDto dto = new ProcessInspectionSaveDto();
        dto.setCallNo(callNo);
        dto.setCallQty(result.getCallQty());
        dto.setTotalManufacturedQty(result.getTotalManufacturedQty());
        dto.setTotalRejectedQty(result.getTotalRejectedQty());

        int totalMfg = result.getTotalManufacturedQty() != null ? result.getTotalManufacturedQty() : 0;
        int totalRej = result.getTotalRejectedQty() != null ? result.getTotalRejectedQty() : 0;
        int maxTotalAcc = Math.max(0, totalMfg - totalRej);
        int totalAcc = result.getTotalAcceptedQty() != null ? result.getTotalAcceptedQty() : maxTotalAcc;
        if (totalRej > 0 && totalAcc > maxTotalAcc) {
            totalAcc = maxTotalAcc;
        }
        dto.setTotalAcceptedQty(totalAcc);
        dto.setOfferedSets(result.getOfferedSets());
        dto.setAcceptedSets(result.getAcceptedSets());
        dto.setRejectedSets(result.getRejectedSets());

        dto.setReasonForRejection(result.getReasonForRejection());
        dto.setLotRangeFrom(result.getLotRangeFrom());
        dto.setLotRangeTo(result.getLotRangeTo());
        dto.setRemarks(result.getRemarks());
        dto.setInspectionStartDate(result.getInspectionStartDate());
        dto.setInspectionEndDate(result.getInspectionEndDate());
        dto.setCreatedBy(result.getCreatedBy());
        dto.setUpdatedBy(result.getUpdatedBy());
        dto.setShift(result.getShift());
        dto.setInspectionDate(result.getInspectionDate());
        
        // Batch-fetch any missing drawing numbers upfront in 1 bulk query (prevents N+1 queries)
        List<Long> missingDrawingBatchIds = new ArrayList<>();
        if (result.getBatches() != null) {
            for (RailProcessInspectionBatch b : result.getBatches()) {
                if (b.getDrawingNo() == null && b.getDeclarationBatchId() != null) {
                    missingDrawingBatchIds.add(b.getDeclarationBatchId());
                }
            }
        }

        Map<Long, String> drawingNoByBatchIdMap = new java.util.HashMap<>();
        if (!missingDrawingBatchIds.isEmpty()) {
            try {
                List<Object[]> rows = entityManager
                        .createQuery("SELECT i.id, i.drawingNo FROM RailIEProductionInfo i WHERE i.id IN :ids", Object[].class)
                        .setParameter("ids", missingDrawingBatchIds)
                        .getResultList();
                for (Object[] r : rows) {
                    if (r[0] != null && r[1] != null) {
                        drawingNoByBatchIdMap.put((Long) r[0], (String) r[1]);
                    }
                }
            } catch (Exception ignored) {}
        }

        List<ProcessInspectionSaveDto.ProcessBatchSaveDto> batchDtos = new ArrayList<>();
        if (result.getBatches() != null) {
            for (RailProcessInspectionBatch b : result.getBatches()) {
                ProcessInspectionSaveDto.ProcessBatchSaveDto bd = new ProcessInspectionSaveDto.ProcessBatchSaveDto();
                bd.setDeclarationBatchId(b.getDeclarationBatchId());
                bd.setBatchNo(b.getBatchNo());
                bd.setDrawingNo(b.getDrawingNo());
                bd.setReasonForRejection(b.getReasonForRejection());
                bd.setProductionDate(b.getProductionDate());
                bd.setQtyManufactured(b.getQtyManufactured());
                bd.setQtyAvailable(b.getQtyAvailable());
                bd.setQtyRejected(b.getQtyRejected());
                
                int mQty = b.getQtyManufactured() != null ? b.getQtyManufactured() : 0;
                int rQty = b.getQtyRejected() != null ? b.getQtyRejected() : 0;
                int netAcc = Math.max(0, mQty - rQty);
                int acc = b.getQtyAccepted() != null ? b.getQtyAccepted() : netAcc;
                if (rQty > 0 && acc > netAcc) {
                    acc = netAcc;
                }
                bd.setQtyAccepted(acc);
                int remQty = Math.max(0, (b.getQtyAvailable() != null ? b.getQtyAvailable() : mQty) - acc - rQty);
                bd.setQtyRemaining(remQty);
                
                if (bd.getDrawingNo() == null && b.getDeclarationBatchId() != null) {
                    bd.setDrawingNo(drawingNoByBatchIdMap.get(b.getDeclarationBatchId()));
                }
                
                batchDtos.add(bd);
            }
        }
        dto.setBatches(batchDtos);
        
        return dto;
    }

    @Override
    public ProcessInspectionSaveDto getAvailableBatchesForFinalCall(String callNo) {
        return getAvailableBatchesForFinalCall(callNo, null);
    }

    @Override
    public ProcessInspectionSaveDto getAvailableBatchesForFinalCall(String callNo, String excludeCallNo) {
        ProcessInspectionSaveDto dto = getProcessInspectionResult(callNo);
        // Material diversion: a diverted-passed Process call has no inspection of its own; its batches are the
        // source call's batches limited to the diverted quantities (remaining = diverted − offered in Final calls).
        RailInspectionCall divertedCall = callNo == null ? null
                : inspectionCallRepository.findByCallNo(callNo.trim())
                    .filter(c -> "DIVERTED_PASSED".equalsIgnoreCase(c.getCallCategory()) && c.getDiversionRequestNo() != null)
                    .orElse(null);
        if (divertedCall != null && railDiversionRequestRepository != null) {
            com.sarthi.SRailPad.diversion.entity.RailDiversionRequest req =
                    railDiversionRequestRepository.findByRequestNo(divertedCall.getDiversionRequestNo()).orElse(null);
            if (req != null) {
                ProcessInspectionSaveDto src = getProcessInspectionResult(req.getSourceCallNo());
                java.util.Map<String, ProcessInspectionSaveDto.ProcessBatchSaveDto> srcByBatch = new java.util.HashMap<>();
                if (src != null && src.getBatches() != null) {
                    src.getBatches().forEach(b -> { if (b.getBatchNo() != null) srcByBatch.put(b.getBatchNo().trim().toUpperCase(), b); });
                }
                ProcessInspectionSaveDto out = src != null ? src : new ProcessInspectionSaveDto();
                List<ProcessInspectionSaveDto.ProcessBatchSaveDto> batches = new ArrayList<>();
                for (com.sarthi.SRailPad.diversion.entity.RailDiversionItem it : req.getItems()) {
                    if (it.getBatchNo() == null) continue;
                    ProcessInspectionSaveDto.ProcessBatchSaveDto b = srcByBatch.getOrDefault(it.getBatchNo().trim().toUpperCase(),
                            new ProcessInspectionSaveDto.ProcessBatchSaveDto());
                    int diverted = it.getDiversionQty().setScale(0, java.math.RoundingMode.HALF_UP).intValue();
                    b.setBatchNo(it.getBatchNo());
                    b.setQtyManufactured(diverted);
                    b.setQtyRejected(0);
                    b.setQtyAccepted(diverted);
                    batches.add(b);
                }
                out.setBatches(batches);
                dto = out;
            }
        }
        // Rail pad type of the process call, so the final call form can match it to the selected type
        if (dto != null && callNo != null) {
            final ProcessInspectionSaveDto typeTarget = dto; // dto may be replaced above for diverted calls
            inspectionCallRepository.findByCallNo(callNo.trim())
                    .ifPresent(call -> typeTarget.setRailPadType(call.getRailPadType()));
        }
        if (dto == null || dto.getBatches() == null || dto.getBatches().isEmpty()) {
            return dto;
        }

        List<String> batchNos = dto.getBatches().stream()
                .map(ProcessInspectionSaveDto.ProcessBatchSaveDto::getBatchNo)
                .filter(b -> b != null && !b.trim().isEmpty())
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        // Build total offered quantity per batch/drawing key
        java.util.Map<String, Integer> remainingOfferedMap = new java.util.HashMap<>();
        if (!batchNos.isEmpty()) {
            String processIc = callNo != null ? callNo.trim() : "";
            List<Object[]> summaryList = (excludeCallNo != null && !excludeCallNo.trim().isEmpty())
                    ? railInspectionBatchRepository.findOfferedSummaryByBatchNosExcludingCall(batchNos, processIc, excludeCallNo.trim())
                    : railInspectionBatchRepository.findOfferedSummaryByBatchNos(batchNos, processIc);
            for (Object[] row : summaryList) {
                String bNo = row[0] != null ? row[0].toString().trim() : null;
                String dNo = row[1] != null ? row[1].toString().trim() : "";
                Long qtyLong = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                int sumQty = qtyLong.intValue();

                if (bNo != null && !bNo.isEmpty()) {
                    String cleanExactD = dNo.trim().toUpperCase();
                    String normD = normalizeDrawingNo(dNo);
                    String normB = normalizeBatchKey(bNo);

                    // Exact Drawing mappings
                    if (!cleanExactD.isEmpty()) {
                        remainingOfferedMap.put(bNo + "|" + cleanExactD, remainingOfferedMap.getOrDefault(bNo + "|" + cleanExactD, 0) + sumQty);
                        if (!normB.isEmpty() && !normB.equals(bNo)) {
                            remainingOfferedMap.put(normB + "|" + cleanExactD, remainingOfferedMap.getOrDefault(normB + "|" + cleanExactD, 0) + sumQty);
                        }
                    }

                    // Normalized Drawing mappings
                    if (!normD.isEmpty()) {
                        remainingOfferedMap.put(bNo + "|" + normD, remainingOfferedMap.getOrDefault(bNo + "|" + normD, 0) + sumQty);
                        if (!normB.isEmpty() && !normB.equals(bNo)) {
                            remainingOfferedMap.put(normB + "|" + normD, remainingOfferedMap.getOrDefault(normB + "|" + normD, 0) + sumQty);
                        }
                    }
                }
            }
        }

        List<ProcessInspectionSaveDto.ProcessBatchSaveDto> availableBatches = new ArrayList<>();
        for (ProcessInspectionSaveDto.ProcessBatchSaveDto b : dto.getBatches()) {
            String bNo = b.getBatchNo() != null ? b.getBatchNo().trim() : "";
            String dNo = b.getDrawingNo() != null ? b.getDrawingNo().trim() : "";

            int mQty = b.getQtyManufactured() != null ? b.getQtyManufactured() : 0;
            int rQty = b.getQtyRejected() != null ? b.getQtyRejected() : 0;
            int maxAcc = Math.max(0, mQty - rQty);
            int netAccepted = b.getQtyAccepted() != null ? b.getQtyAccepted() : maxAcc;
            if (rQty > 0 && netAccepted > maxAcc) {
                netAccepted = maxAcc;
            }

            String cleanExactD = dNo.trim().toUpperCase();
            String normD = normalizeDrawingNo(dNo);
            String normB = normalizeBatchKey(bNo);

            // Determine matching key for available offered pool (strictly batch + drawing)
            String matchedKey = null;
            if (!normD.isEmpty() && remainingOfferedMap.containsKey(bNo + "|" + normD)) {
                matchedKey = bNo + "|" + normD;
            } else if (!cleanExactD.isEmpty() && remainingOfferedMap.containsKey(bNo + "|" + cleanExactD)) {
                matchedKey = bNo + "|" + cleanExactD;
            } else if (!normB.isEmpty() && !normD.isEmpty() && remainingOfferedMap.containsKey(normB + "|" + normD)) {
                matchedKey = normB + "|" + normD;
            } else if (!normB.isEmpty() && !cleanExactD.isEmpty() && remainingOfferedMap.containsKey(normB + "|" + cleanExactD)) {
                matchedKey = normB + "|" + cleanExactD;
            }

            int alreadyOfferedForThisBatch = 0;
            if (matchedKey != null && remainingOfferedMap.containsKey(matchedKey)) {
                int poolAvail = remainingOfferedMap.get(matchedKey);
                alreadyOfferedForThisBatch = Math.min(netAccepted, poolAvail);
                remainingOfferedMap.put(matchedKey, Math.max(0, poolAvail - alreadyOfferedForThisBatch));
            }

            b.setQtyAccepted(netAccepted);
            b.setPreviouslyOfferedQty(alreadyOfferedForThisBatch);
            b.setQtyRemaining(Math.max(0, netAccepted - alreadyOfferedForThisBatch));
            availableBatches.add(b);
        }

        // Material diversion: qty diverted away from this (source) Process IC is no longer available for its own Final calls.
        // Only qtyRemaining is reduced; accepted / previously offered stay as they are (diversion balances use those).
        if (divertedCall == null && railDiversionItemRepository != null && callNo != null) {
            java.util.Map<String, Integer> divertedByBatch = new java.util.HashMap<>();
            for (Object[] row : railDiversionItemRepository.sumDivertedByBatchForSourceCall(callNo.trim(), DIVERSION_HOLDING)) {
                if (row[0] != null) divertedByBatch.merge(row[0].toString().trim().toUpperCase(),
                        ((Number) row[1]).intValue(), Integer::sum);
            }
            if (!divertedByBatch.isEmpty()) {
                for (ProcessInspectionSaveDto.ProcessBatchSaveDto b : availableBatches) {
                    Integer div = b.getBatchNo() == null ? null : divertedByBatch.get(b.getBatchNo().trim().toUpperCase());
                    if (div != null && div > 0) {
                        b.setQtyRemaining(Math.max(0, (b.getQtyRemaining() == null ? 0 : b.getQtyRemaining()) - div));
                    }
                }
            }
        }
        dto.setBatches(availableBatches);
        return dto;
    }

    private String normalizeBatchKey(String bNo) {
        if (bNo == null) return "";
        return bNo.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String normalizeDrawingNo(String dNo) {
        if (dNo == null) return "";
        String clean = dNo.trim().toUpperCase()
                .replaceAll("(?i)^(RDSO[/\\-_\\s]*)", "")
                .replaceAll("(?i)^(RT|T)[/\\-_\\s]*", "")
                .replaceAll("[\\-_\\s/]", "");
        return clean.isEmpty() ? dNo.trim().toUpperCase() : clean;
    }

    private void recordHistory(RailProcessCallDetails details, String fieldName, String oldValue, String newValue, Long userId) {
        RailProcessCallHistory history = new RailProcessCallHistory();
        history.setProcessCallDetail(details);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setUpdatedBy(userId);
        historyRepository.save(history);
    }
}
