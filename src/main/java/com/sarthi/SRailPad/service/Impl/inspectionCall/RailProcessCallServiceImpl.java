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
    private final RailProductionBatchRepository productionBatchRepository;
    private final RailProcessInspectionResultRepository processInspectionResultRepository;
    private final RailProcessInspectionBatchRepository processInspectionBatchRepository;
    private final RailIEProductionVerificationRepository verificationRepository;
    private final RailInspectionBatchRepository railInspectionBatchRepository;

    @Autowired
    private EntityManager entityManager;

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

    @Override
    public ProcessAvailableBatchDto getAvailableBatchesForProcessIc(String poNo, String railPadType, String callNo) {
        List<Object[]> results = verificationRepository.findAvailableInfosForProcessIc(poNo, railPadType, callNo != null ? callNo : "");

        ProcessAvailableBatchDto dto = new ProcessAvailableBatchDto();
        dto.setTotalBatchesCount(results.size());
        
        List<ProcessAvailableBatchDto.ProcessBatchDetailDto> batchDtos = new ArrayList<>();
        for (Object[] row : results) {
            com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo i = (com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfo) row[0];
            RailIEProductionVerification v = (RailIEProductionVerification) row[1];
            com.sarthi.SRailPad.entity.plantDeclaration.RailProductionDeclaration d = (com.sarthi.SRailPad.entity.plantDeclaration.RailProductionDeclaration) row[2];

            ProcessAvailableBatchDto.ProcessBatchDetailDto bd = new ProcessAvailableBatchDto.ProcessBatchDetailDto();
            bd.setDeclarationBatchId(i.getId()); // Mapping declarationBatchId to RailIEProductionInfo ID
            bd.setBatchNo(i.getBatchNo());
            bd.setQtyManufactured(i.getQuantityProduced());
            bd.setProductionDate(v.getCastingDate() != null ? v.getCastingDate() : d.getProductionDate());
            bd.setDrawingNo(i.getDrawingNo());
            
            if (v.getRejections() != null) {
                List<com.sarthi.SRailPad.entity.ieVerification.RailIEProductionRejection> batchRejections = v.getRejections().stream()
                        .filter(r -> r.getBatchNo() != null && r.getBatchNo().equals(i.getBatchNo()))
                        .filter(r -> {
                            if (i.getDrawingNo() == null || i.getDrawingNo().isBlank()) return true;
                            if (r.getDrawingNo() == null || r.getDrawingNo().isBlank()) return true;
                            return i.getDrawingNo().equals(r.getDrawingNo());
                        })
                        .collect(java.util.stream.Collectors.toList());

                if (!batchRejections.isEmpty()) {
                    int totalRejectedQty = batchRejections.stream()
                            .mapToInt(r -> r.getRejectedQty() != null ? r.getRejectedQty() : 0)
                            .sum();
                    String combinedReasons = batchRejections.stream()
                            .map(r -> r.getReason())
                            .filter(reason -> reason != null && !reason.isBlank())
                            .distinct()
                            .collect(java.util.stream.Collectors.joining(", "));
                    bd.setVerificationRejectedQty(totalRejectedQty);
                    bd.setVerificationRejectedReason(combinedReasons);
                    
                    List<ProcessAvailableBatchDto.RejectionDetailDto> rejectionDtos = batchRejections.stream().map(r -> {
                        ProcessAvailableBatchDto.RejectionDetailDto rDto = new ProcessAvailableBatchDto.RejectionDetailDto();
                        rDto.setDrawingNo(r.getDrawingNo());
                        rDto.setReason(r.getReason());
                        rDto.setRejectedQty(r.getRejectedQty());
                        return rDto;
                    }).collect(java.util.stream.Collectors.toList());
                    bd.setRejections(rejectionDtos);
                }
            }

            batchDtos.add(bd);
        }
        
        dto.setBatches(batchDtos);
        // Returning a List of ProcessAvailableBatchDto is better. I will adjust the return type in controller.
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

        if (saveDto.getBatches() != null) {
            for (ProcessInspectionSaveDto.ProcessBatchSaveDto bDto : saveDto.getBatches()) {
                RailProcessInspectionBatch batch = new RailProcessInspectionBatch();
                batch.setResult(result);
                batch.setDeclarationBatchId(bDto.getDeclarationBatchId());
                batch.setBatchNo(bDto.getBatchNo());
                batch.setProductionDate(bDto.getProductionDate());
                batch.setQtyManufactured(bDto.getQtyManufactured());
                batch.setQtyRejected(bDto.getQtyRejected());
                batch.setQtyAccepted(bDto.getQtyAccepted());
                result.getBatches().add(batch);
            }
        }

        processInspectionResultRepository.save(result);

        if (Boolean.TRUE.equals(saveDto.getIsFinish())) {
            // Update the main process call details
            RailProcessCallDetails details = processCallDetailsRepository.findByInspectionCall_CallNo(saveDto.getCallNo())
                .orElseThrow(() -> new RuntimeException("Process Call Details not found"));
            
            int acceptedTillNow = details.getQtyAcceptedTillNow() != null ? details.getQtyAcceptedTillNow() : 0;
            details.setQtyAcceptedTillNow(acceptedTillNow + saveDto.getTotalAcceptedQty());
            
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
        dto.setTotalAcceptedQty(result.getTotalAcceptedQty());
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
        
        List<ProcessInspectionSaveDto.ProcessBatchSaveDto> batchDtos = new ArrayList<>();
        if (result.getBatches() != null) {
            for (RailProcessInspectionBatch b : result.getBatches()) {
                ProcessInspectionSaveDto.ProcessBatchSaveDto bd = new ProcessInspectionSaveDto.ProcessBatchSaveDto();
                bd.setDeclarationBatchId(b.getDeclarationBatchId());
                bd.setBatchNo(b.getBatchNo());
                bd.setProductionDate(b.getProductionDate());
                bd.setQtyManufactured(b.getQtyManufactured());
                bd.setQtyRejected(b.getQtyRejected());
                bd.setQtyAccepted(b.getQtyAccepted());
                
                if (b.getDeclarationBatchId() != null) {
                    try {
                        String drawingNo = (String) entityManager.createNativeQuery("SELECT drawing_no FROM rail_ie_production_info WHERE id = :id")
                            .setParameter("id", b.getDeclarationBatchId())
                            .getSingleResult();
                        bd.setDrawingNo(drawingNo);
                    } catch (Exception e) {
                        // ignore
                    }
                }
                
                batchDtos.add(bd);
            }
        }
        dto.setBatches(batchDtos);
        
        return dto;
    }

    @Override
    public ProcessInspectionSaveDto getAvailableBatchesForFinalCall(String callNo) {
        ProcessInspectionSaveDto dto = getProcessInspectionResult(callNo);
        if (dto == null || dto.getBatches() == null || dto.getBatches().isEmpty()) {
            return dto;
        }

        List<String> batchNos = dto.getBatches().stream()
                .map(ProcessInspectionSaveDto.ProcessBatchSaveDto::getBatchNo)
                .filter(b -> b != null && !b.trim().isEmpty())
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Integer> offeredMap = new java.util.HashMap<>();
        if (!batchNos.isEmpty()) {
            List<Object[]> summaryList = railInspectionBatchRepository.findOfferedSummaryByBatchNos(batchNos);
            for (Object[] row : summaryList) {
                String bNo = (String) row[0];
                String dNo = (String) row[1];
                Long qtyLong = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                int sumQty = qtyLong.intValue();

                if (bNo != null) {
                    if (dNo != null && !dNo.trim().isEmpty()) {
                        String cleanD = dNo.replace("RDSO/", "").trim();
                        offeredMap.put(bNo + "|" + dNo.trim(), sumQty);
                        offeredMap.put(bNo + "|" + cleanD, sumQty);
                        offeredMap.put(bNo + "|RDSO/" + cleanD, sumQty);
                    }
                    offeredMap.put(bNo + "|ALL", offeredMap.getOrDefault(bNo + "|ALL", 0) + sumQty);
                }
            }
        }

        List<ProcessInspectionSaveDto.ProcessBatchSaveDto> availableBatches = new ArrayList<>();
        for (ProcessInspectionSaveDto.ProcessBatchSaveDto b : dto.getBatches()) {
            String bNo = b.getBatchNo();
            String dNo = b.getDrawingNo();
            int alreadyOffered = 0;

            if (dNo != null && !dNo.trim().isEmpty()) {
                String cleanD = dNo.replace("RDSO/", "").trim();
                if (offeredMap.containsKey(bNo + "|" + dNo.trim())) {
                    alreadyOffered = offeredMap.get(bNo + "|" + dNo.trim());
                } else if (offeredMap.containsKey(bNo + "|" + cleanD)) {
                    alreadyOffered = offeredMap.get(bNo + "|" + cleanD);
                } else if (offeredMap.containsKey(bNo + "|RDSO/" + cleanD)) {
                    alreadyOffered = offeredMap.get(bNo + "|RDSO/" + cleanD);
                }
            } else {
                alreadyOffered = offeredMap.getOrDefault(bNo + "|ALL", 0);
            }

            int remainingAccepted = Math.max(0, b.getQtyAccepted() - alreadyOffered);
            b.setQtyAccepted(remainingAccepted);
            availableBatches.add(b);
        }
        dto.setBatches(availableBatches);
        return dto;
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
