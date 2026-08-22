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

                boolean hasProcessQty = processIeQtyRepository.existsByRequestId(requestId);

                // Integer offeredEarlier = RmHeatFinalResultRepository.

                // 🔹 CASE 2: No process qty → return offeredQty only
                if (!hasProcessQty) {
                        return List.of(
                                        new InspectionQtySummaryResponse(
                                                        null, // lotNumber
                                                        offeredQty, // offeredQty
                                                        null, // acceptedQty
                                                        null, // manufacturedQty
                                                        null
                                        // rejectedQty
                                        ));
                }

                // Process qty exists → lot-wise list
                List<InspectionQtySummaryView> list = processIeQtyRepository.getLotWiseQtySummary(requestId);

                return list.stream()
                                .map(v -> new InspectionQtySummaryResponse(
                                                v.getLotNumber(),
                                                v.getOfferedQty(),
                                                v.getAcceptedQty(),
                                                v.getManufacturedQty(),
                                                v.getRejectedQty()))
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

                // 1. Gather candidates for the specific RM IC Number
                List<String> rmCallCandidates = new java.util.ArrayList<>();
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

                InspectionCall ic = null;
                if (lookupCallNo != null) {
                        ic = inspectionCallRepository.findByIcNumber(lookupCallNo).orElse(null);
                        if (ic == null && lookupCallNo.contains("/")) {
                                for (String cand : rmCallCandidates) {
                                        ic = inspectionCallRepository.findByIcNumber(cand).orElse(null);
                                        if (ic != null) break;
                                }
                        }
                }
                if (ic != null && ic.getIcNumber() != null && !rmCallCandidates.contains(ic.getIcNumber())) {
                        rmCallCandidates.add(ic.getIcNumber());
                }

                Long rmIcId = ic != null ? ic.getId() : null;
                List<String> safeCandidates = !rmCallCandidates.isEmpty() ? rmCallCandidates : java.util.List.of("__NONE__");

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

                // 3. Process inspection calls for "manufactured" and "offered earlier"
                // When an RM IC is specified, scope strictly to calls / details under this RM IC!
                TotalManufaturedQtyOfPoDto dto = new TotalManufaturedQtyOfPoDto();
                Integer offeredEarlier = 0;

                if (rmIcId != null || !rmCallCandidates.isEmpty()) {
                        List<String> rmProcessCalls = processInspectionDetailsRepository.findProcessCallNumbersByRmIc(rmIcId, safeCandidates);
                        if (rmProcessCalls != null && !rmProcessCalls.isEmpty()) {
                                dto = processIeQtyRepository.sumProcessQty(rmProcessCalls, heatNo);
                                if (dto == null) {
                                        dto = new TotalManufaturedQtyOfPoDto();
                                }
                        }
                        offeredEarlier = processInspectionDetailsRepository.sumOfferedQtyByRmIcAndHeatNo(rmIcId, safeCandidates, heatNo);
                } else {
                        // Fallback: If no RM IC was specified, scope by PO
                        List<String> processCallNos = new java.util.ArrayList<>();
                        if (poSerialNo != null && !poSerialNo.isBlank()) {
                                List<String> poCalls = (vendorCode != null && !vendorCode.isBlank())
                                                ? inspectionCallRepository.findCallNumbersByVendorAndPo(vendorCode.trim(), poSerialNo.trim())
                                                : inspectionCallRepository.findCallNumbersByPoNo(poSerialNo.trim());
                                if (poCalls != null) {
                                        processCallNos.addAll(poCalls);
                                }
                        }

                        if (!processCallNos.isEmpty()) {
                                dto = processIeQtyRepository.sumProcessQty(processCallNos, heatNo);
                                if (dto == null) {
                                        dto = new TotalManufaturedQtyOfPoDto();
                                }
                                offeredEarlier = processInspectionDetailsRepository.sumOfferedQtyByCallNosAndHeatNo(processCallNos, heatNo);
                        }

                        if (weightAcceptedMt.compareTo(BigDecimal.ZERO) == 0 && !processCallNos.isEmpty()) {
                                rmAcceptedQty = rmHeatFinalResultRepository.sumRmAcceptedQty(processCallNos, heatNo);
                                weightAcceptedMt = rmHeatFinalResultRepository.sumWeightAcceptedMt(processCallNos, heatNo);
                        }
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
                int qty = processIeQtyRepository.sumInspectedQtyByRequestIdAndLotNumberAndHeatNo(requestId, lotNumber, heatNo);
                if (qty == 0 && lotNumber != null && !lotNumber.isBlank()) {
                        qty = processIeQtyRepository.sumInspectedQtyByRequestIdAndLotNumber(requestId, lotNumber);
                }
                if (qty > 0) {
                        return qty;
                }

                if (lotNumber != null && !lotNumber.isBlank()) {
                        Integer temperingAccepted = processLineFinalResultRepository
                                        .sumTemperingAcceptedByLotNumberAndHeatNo(lotNumber, heatNo);
                        if (temperingAccepted != null && temperingAccepted > 0) {
                                return temperingAccepted;
                        }
                }
                return 0;
        }

}
