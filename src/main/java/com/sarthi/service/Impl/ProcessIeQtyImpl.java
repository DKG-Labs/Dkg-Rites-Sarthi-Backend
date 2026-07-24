package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.InspectionQtySummaryResponse;
import com.sarthi.dto.InspectionQtySummaryView;
import com.sarthi.dto.TotalManufaturedQtyOfPoDto;
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

                List<String> callNos = inspectionCallRepository.findCallNumbersByPoNo(poSerialNo);

                TotalManufaturedQtyOfPoDto dto = processIeQtyRepository.sumProcessQty(callNos, heatNo);

                BigDecimal rmAcceptedQty = rmHeatFinalResultRepository.sumRmAcceptedQty(callNos, heatNo);

                BigDecimal weightAcceptedMt = rmHeatFinalResultRepository.sumWeightAcceptedMt(callNos, heatNo);

                // Calculate "Offered Earlier" - total offered quantity across all process ICs
                // for this heat and PO
                Integer offeredEarlier = processInspectionDetailsRepository.sumOfferedQtyByCallNosAndHeatNo(callNos,
                                heatNo);
                // Integer offeredEarlier =
                // rmHeatFinalResultRepository
                // .sumOfferedEarlierByHeatNoAndInspectionCallNos(heatNo, callNos);

                dto.setRmAcceptedQty(rmAcceptedQty);
                dto.setHeatNo(heatNo);
                dto.setWeightAcceptedMt(weightAcceptedMt);
                dto.setOfferedEarlier(offeredEarlier != null ? offeredEarlier : 0);

                // Fetch sealing info from RM inspection results
                List<com.sarthi.entity.RmHeatFinalResult> rmResults = rmHeatFinalResultRepository.findByInspectionCallNoInAndHeatNo(callNos, heatNo);
                if (!rmResults.isEmpty()) {
                        rmResults.stream()
                                .filter(r -> r.getSealingType() != null)
                                .findFirst()
                                .ifPresent(latest -> {
                                        dto.setSealingType(latest.getSealingType());
                                        dto.setSteelStampNumber(latest.getSteelStampNumber());
                                        dto.setHologramDetails(latest.getHologramDetails());
                                });
                }

                return dto;
        }

        @Override
        public int getAcceptedQtyForLot(String requestId, String lotNumber, String heatNo) {
                if (lotNumber != null && !lotNumber.isBlank()) {
                        Integer temperingAccepted = processLineFinalResultRepository
                                        .sumTemperingAcceptedByLotNumberAndHeatNo(lotNumber, heatNo);
                        if (temperingAccepted != null && temperingAccepted > 0) {
                                return temperingAccepted;
                        }
                }
                return processIeQtyRepository.sumInspectedQtyByRequestIdAndLotNumberAndHeatNo(requestId, lotNumber,
                                heatNo);
        }

}
