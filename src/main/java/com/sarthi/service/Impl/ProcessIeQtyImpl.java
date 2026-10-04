package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.InspectionQtySummaryResponse;
import com.sarthi.dto.InspectionQtySummaryView;
import com.sarthi.dto.TotalManufaturedQtyOfPoDto;
import com.sarthi.entity.RmHeatFinalResult;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.ProcessIeQtyRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.ProcessIeQtyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProcessIeQtyImpl implements ProcessIeQtyService {

        @Autowired
        private ProcessIeQtyRepository processIeQtyRepository;
        @Autowired
        private InspectionCallRepository inspectionCallRepository;
        @Autowired
        private ProcessInspectionDetailsRepository processInspectionDetailsRepository;
        @Autowired
        private RmHeatFinalResultRepository rmHeatFinalResultRepository;
        @Autowired
        private com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository processLineFinalResultRepository;

        // @Override
        // public InspectionQtySummaryResponse getQtySummary(String requestId) {
        //
        // InspectionQtySummaryView view =
        // processIeQtyRepository.getQtySummaryByRequestId(requestId);
        //
        // if (view == null) {
        // return new InspectionQtySummaryResponse(0, 0, 0);
        // }
        //
        // return new InspectionQtySummaryResponse(
        // view.getAcceptedQty(),
        // view.getTotalOfferedQty(),
        // view.getTotalManufactureQty()
        // );
        // }
        /*
         * @Override
         * public InspectionQtySummaryResponse getQtySummary(String requestId) {
         * 
         * 
         * boolean hasProcessQty =
         * processIeQtyRepository.existsByRequestId(requestId);
         * 
         * 
         * if (hasProcessQty) {
         * InspectionCall ic =
         * inspectionCallRepository
         * .findByIcNumber(requestId)
         * .orElseThrow(() -> new BusinessException(
         * new ErrorDetails(
         * AppConstant.ERROR_CODE_RESOURCE,
         * AppConstant.ERROR_TYPE_CODE_RESOURCE,
         * AppConstant.ERROR_TYPE_VALIDATION,
         * "Invalid Inspection Call: " + requestId
         * )
         * ));
         * 
         * 
         * Integer totalOfferedQty =
         * processInspectionDetailsRepository
         * .sumOfferedQtyByIcId(ic.getId());
         * 
         * // InspectionQtySummaryView view =
         * // processIeQtyRepository.getQtySummaryByRequestId(requestId);
         * 
         * List<InspectionQtySummaryView> list =
         * processIeQtyRepository.getLotWiseQtySummary(requestId);
         * 
         * 
         * // if (view == null) {
         * // return new InspectionQtySummaryResponse(0, 0, 0, 0);
         * // }
         * 
         * return new InspectionQtySummaryResponse(
         * view.getAcceptedQty(),
         * totalOfferedQty,
         * view.getTotalManufactureQty(),
         * view.getTotalRejectedQty()
         * );
         * }
         * 
         * 
         * InspectionCall ic =
         * inspectionCallRepository
         * .findByIcNumber(requestId)
         * .orElseThrow(() -> new BusinessException(
         * new ErrorDetails(
         * AppConstant.ERROR_CODE_RESOURCE,
         * AppConstant.ERROR_TYPE_CODE_RESOURCE,
         * AppConstant.ERROR_TYPE_VALIDATION,
         * "Invalid Inspection Call: " + requestId
         * )
         * ));
         * 
         * 
         * Integer totalOfferedQty =
         * processInspectionDetailsRepository
         * .sumOfferedQtyByIcId(ic.getId());
         * System.out.print("totsl"+totalOfferedQty);
         * 
         * 
         * return new InspectionQtySummaryResponse(
         * 0, // acceptedQty
         * totalOfferedQty, // offeredQty from lots
         * 0 , // manufactureQty,
         * 0
         * );
         * }
         */
        @Override
        public List<InspectionQtySummaryResponse> getQtySummary(String requestId) {

                InspectionCall ic = inspectionCallRepository
                                .findByIcNumber(requestId)
                                .orElseThrow(() -> new BusinessException(
                                                new ErrorDetails(
                                                                AppConstant.ERROR_CODE_RESOURCE,
                                                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                                AppConstant.ERROR_TYPE_VALIDATION,
                                                                "Invalid Inspection Call: " + requestId)));

                Integer offeredQty = processInspectionDetailsRepository
                                .sumOfferedQtyByIcId(ic.getId());

                // Strictly fetch lot-wise summary from process_line_final_result:
                // manufactured = shearingManufactured, rejected = totalRejected, accepted = shearingManufactured - totalRejected
                List<InspectionQtySummaryView> list = processLineFinalResultRepository
                                .getLotWiseQtySummaryFromFinalResult(requestId);

                // If no records exist in process_line_final_result → return offeredQty only
                if (list == null || list.isEmpty()) {
                        return List.of(
                                        new InspectionQtySummaryResponse(
                                                        null, // lotNumber
                                                        offeredQty, // offeredQty
                                                        null, // acceptedQty
                                                        null, // manufacturedQty
                                                        null  // rejectedQty
                                        ));
                }

                // Process qty exists → lot-wise list
                return list.stream()
                                .map(v -> {
                                        Integer lotOffered = (v.getOfferedQty() != null && v.getOfferedQty() > 0)
                                                        ? v.getOfferedQty()
                                                        : offeredQty;
                                        return new InspectionQtySummaryResponse(
                                                        v.getLotNumber(),
                                                        lotOffered,
                                                        v.getAcceptedQty(),
                                                        v.getManufacturedQty(),
                                                        v.getRejectedQty());
                                })
                                .toList();
        }

        @Override
        public String getpoNumberByCallNo(String requestId) {

                InspectionCall ic = inspectionCallRepository
                                .findByIcNumber(requestId)
                                .orElseThrow(() -> new BusinessException(
                                                new ErrorDetails(
                                                                AppConstant.ERROR_CODE_RESOURCE,
                                                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                                AppConstant.ERROR_TYPE_VALIDATION,
                                                                "Invalid Inspection Call: " + requestId)));
                String input = ic.getPoSerialNo();
                String result = input.substring(input.lastIndexOf("/") + 1);

                return result;
        }

        @Override
        public TotalManufaturedQtyOfPoDto getTotalManufaturedQtyPo(String heatNo, String poSerialNo) {
                return getTotalManufaturedQtyPo(heatNo, poSerialNo, null, null);
        }

        @Override
        public TotalManufaturedQtyOfPoDto getTotalManufaturedQtyPo(String heatNo, String poSerialNo, String callNo) {
                return getTotalManufaturedQtyPo(heatNo, poSerialNo, callNo, null);
        }

        @Override
        public TotalManufaturedQtyOfPoDto getTotalManufaturedQtyPo(String heatNo, String poSerialNo, String callNo, String vendorCode) {
                String lookupCallNo = (callNo != null && !callNo.isBlank()) ? callNo.trim() : null;

                // If callNo was not passed explicitly, check if poSerialNo is an inspection call number
                if (lookupCallNo == null && poSerialNo != null) {
                        if (poSerialNo.startsWith("E") || poSerialNo.startsWith("W/") || poSerialNo.startsWith("N/") 
                                        || poSerialNo.startsWith("S/") || poSerialNo.startsWith("C/") || (poSerialNo.contains("-") && poSerialNo.contains("/"))) {
                                lookupCallNo = poSerialNo.trim();
                        }
                }

                // 1. Gather candidates for RM IC and Process IC
                List<String> rmCallCandidates = new java.util.ArrayList<>();
                List<String> processCallNos = new java.util.ArrayList<>();

                InspectionCall ic = null;
                if (lookupCallNo != null) {
                        ic = inspectionCallRepository.findByIcNumber(lookupCallNo).orElse(null);
                        if (ic == null && lookupCallNo.contains("/")) {
                                String[] parts = lookupCallNo.split("/");
                                for (String part : parts) {
                                        if (part.contains("-")) {
                                                ic = inspectionCallRepository.findByIcNumber(part.trim()).orElse(null);
                                                if (ic != null) break;
                                        }
                                }
                        }
                }

                if (ic != null && "Process".equalsIgnoreCase(ic.getTypeOfCall())) {
                        // Current call is a Process Inspection Call
                        processCallNos.add(ic.getIcNumber());
                        List<ProcessInspectionDetails> pids = processInspectionDetailsRepository.findByIcId(ic.getId());
                        if (pids != null) {
                                for (ProcessInspectionDetails pid : pids) {
                                        if (pid.getRmIcNumber() != null && !pid.getRmIcNumber().isBlank()) {
                                                String rmIc = pid.getRmIcNumber().trim();
                                                if (!rmCallCandidates.contains(rmIc)) {
                                                        rmCallCandidates.add(rmIc);
                                                }
                                        }
                                }
                        }
                        if (!rmCallCandidates.isEmpty()) {
                                List<String> siblingCalls = processInspectionDetailsRepository.findProcessCallNumbersByRmIc(null, rmCallCandidates);
                                if (siblingCalls != null) {
                                        for (String sc : siblingCalls) {
                                                if (!processCallNos.contains(sc)) processCallNos.add(sc);
                                        }
                                }
                        }
                } else {
                        // Lookup call is an RM IC directly or unclassified
                        if (lookupCallNo != null) {
                                rmCallCandidates.add(lookupCallNo);
                                if (lookupCallNo.contains("/")) {
                                        String[] parts = lookupCallNo.split("/");
                                        for (String part : parts) {
                                                if (part.contains("-")) {
                                                        rmCallCandidates.add(part.trim());
                                                }
                                        }
                                }
                        }
                        if (ic != null && ic.getIcNumber() != null && !rmCallCandidates.contains(ic.getIcNumber())) {
                                rmCallCandidates.add(ic.getIcNumber());
                        }

                        Long rmIcId = ic != null ? ic.getId() : null;
                        List<String> linkedCalls = processInspectionDetailsRepository.findProcessCallNumbersByRmIc(rmIcId, rmCallCandidates);
                        if (linkedCalls != null) {
                                processCallNos.addAll(linkedCalls);
                        }
                }

                // Fallback: If no process calls found yet, resolve via PO
                if (processCallNos.isEmpty() && poSerialNo != null && !poSerialNo.isBlank()) {
                        List<String> poCalls = (vendorCode != null && !vendorCode.isBlank())
                                        ? inspectionCallRepository.findCallNumbersByVendorAndPo(vendorCode.trim(), poSerialNo.trim())
                                        : inspectionCallRepository.findCallNumbersByPoNo(poSerialNo.trim());
                        if (poCalls != null) {
                                processCallNos.addAll(poCalls);
                        }
                }

                // 2. Fetch RM accepted weights strictly from the specific RM IC
                BigDecimal rmAcceptedQty = BigDecimal.ZERO;
                BigDecimal weightAcceptedMt = BigDecimal.ZERO;
                String sealingType = null;
                String steelStampNumber = null;
                String hologramDetails = null;

                if (!rmCallCandidates.isEmpty()) {
                        List<RmHeatFinalResult> exactRmResults = rmHeatFinalResultRepository
                                        .findByInspectionCallNoInAndHeatNo(rmCallCandidates, heatNo);
                        if (!exactRmResults.isEmpty()) {
                                RmHeatFinalResult exactResult = exactRmResults.get(0);
                                weightAcceptedMt = exactResult.getWeightAcceptedMt() != null ? exactResult.getWeightAcceptedMt() : BigDecimal.ZERO;
                                rmAcceptedQty = exactResult.getAcceptedQtyMt() != null ? exactResult.getAcceptedQtyMt() : BigDecimal.ZERO;
                                if (rmAcceptedQty.compareTo(BigDecimal.ZERO) == 0 && weightAcceptedMt.compareTo(BigDecimal.ZERO) > 0) {
                                        rmAcceptedQty = weightAcceptedMt.multiply(new BigDecimal("1000")).divide(new BigDecimal("1.14"), 0, java.math.RoundingMode.HALF_UP);
                                }
                                sealingType = exactResult.getSealingType();
                                steelStampNumber = exactResult.getSteelStampNumber();
                                hologramDetails = exactResult.getHologramDetails();
                        }
                }

                // 3. Process quantities strictly from process_line_final_result (shearingManufactured, totalRejected, accepted)
                TotalManufaturedQtyOfPoDto dto = new TotalManufaturedQtyOfPoDto();
                if (!processCallNos.isEmpty()) {
                        List<Object[]> procRows = processLineFinalResultRepository.sumProcessQtyByCallNosAndHeatNo(processCallNos, heatNo);
                        if (procRows != null && !procRows.isEmpty() && procRows.get(0) != null) {
                                Object[] r = procRows.get(0);
                                long mfg = r[0] != null ? ((Number) r[0]).longValue() : 0L;
                                long rej = r[1] != null ? ((Number) r[1]).longValue() : 0L;
                                long acc = r[2] != null ? ((Number) r[2]).longValue() : 0L;
                                dto.setManufaturedQty(BigDecimal.valueOf(mfg));
                                dto.setRejectedQty(BigDecimal.valueOf(rej));
                                dto.setAcceptedQty(BigDecimal.valueOf(acc));
                        }
                }

                // 4. Offered Earlier
                Integer offeredEarlier = 0;
                if (!rmCallCandidates.isEmpty()) {
                        offeredEarlier = processInspectionDetailsRepository.sumOfferedQtyByRmIcAndHeatNo(null, rmCallCandidates, heatNo);
                }
                if ((offeredEarlier == null || offeredEarlier == 0) && !processCallNos.isEmpty()) {
                        offeredEarlier = processInspectionDetailsRepository.sumOfferedQtyByCallNosAndHeatNo(processCallNos, heatNo);
                }

                dto.setRmAcceptedQty(rmAcceptedQty);
                dto.setHeatNo(heatNo);
                dto.setWeightAcceptedMt(weightAcceptedMt);
                dto.setOfferedEarlier(offeredEarlier != null ? offeredEarlier : 0);

                if (sealingType != null) dto.setSealingType(sealingType);
                if (steelStampNumber != null) dto.setSteelStampNumber(steelStampNumber);
                if (hologramDetails != null) dto.setHologramDetails(hologramDetails);

                return dto;
        }

        @Override
        public int getAcceptedQtyForLot(String requestId, String lotNumber, String heatNo) {
                if (lotNumber == null || lotNumber.isBlank()) {
                        return 0;
                }
                // Strictly follow Process IC logic: calculate accepted quantity from process_line_final_result (single query, no N+1, no fallbacks)
                Integer procAccepted = processLineFinalResultRepository
                                .sumAcceptedQtyByCallNoAndLotNumberAndHeatNo(requestId, lotNumber, heatNo);
                return procAccepted != null ? procAccepted : 0;
        }

}
