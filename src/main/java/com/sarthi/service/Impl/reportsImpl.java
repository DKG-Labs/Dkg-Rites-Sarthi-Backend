package com.sarthi.service.Impl;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.dto.QuenchingDefectsDto;
import com.sarthi.dto.TemperingDefectsDto;
import com.sarthi.dto.reports.DashboardSummaryDto;
import com.sarthi.dto.reports.*;
import com.sarthi.entity.processmaterial.*;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.repository.*;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.processmaterial.*;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.reports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class reportsImpl implements reports {
        @Autowired
        private PoHeaderRepository poHeaderRepository;
        @Autowired
        private PoItemRepository poItemRepository;
        @Autowired
        private InspectionCallRepository inspectionCallRepository;
        @Autowired
        private RmHeatFinalResultRepository rmHeatFinalResultRepository;

        @Autowired
        private ProcessIeQtyRepository processIeQtyRepository;
        @Autowired
        private WorkflowTransitionRepository workflowTransitionRepository;
        @Autowired
        private InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;
        @Autowired
        private ProcessLineFinalResultRepository processLineFinalResultRepository;
        @Autowired
        private ProcessShearingDataRepository processShearingDataRepository;

        @Autowired
        private ProcessTurningDataRepository processTurningDataRepository;

        @Autowired
        private ProcessForgingDataRepository processForgingDataRepository;
        @Autowired
        private ProcessFinalCheckDataRepository processFinalCheckDataRepository;
        @Autowired
        private ProcessTestingFinishingDataRepository processTestingFinishingDataRepository;
        @Autowired
        private ProcessQuenchingDataRepository processQuenchingDataRepository;
        @Autowired
        private ProcessTemperingDataRepository processTemperingDataRepository;

        @Autowired
        private FinalCumulativeResultsRepository finalCumulativeResultsRepository;

        /*
         * @Override
         * public List<PoInspection1stLevelStatusDto>
         * getPoInspection1stLevelStatusList() {
         * List<PoInspection1stLevelStatusDto> list =
         * poHeaderRepository.fetchPoInspectionStatus();
         * 
         * AtomicInteger counter = new AtomicInteger(1);
         * list.forEach(dto -> dto.setSlNo(counter.getAndIncrement()));
         * 
         * return list;
         * }
         */

        @Override
        public List<PoInspection1stLevelStatusDto> getPoInspection1stLevelStatusList() {

                List<PoInspection1stLevelStatusDto> list = poHeaderRepository.fetchPoInspectionStatus();

                AtomicInteger counter = new AtomicInteger(1);

                for (PoInspection1stLevelStatusDto dto : list) {

                        // ================= Serial No =================
                        dto.setSlNo(counter.getAndIncrement());

                        // ================= RM Rejection % =================
                        Double rmPct = inspectionCallRepository.findRmRejectionPct(dto.getPoNo());

                        dto.setRawMaterialRejectionPercentage(
                                        rmPct != null ? rmPct : 0.0);

                        // ================= Get Call Numbers =================
                        List<String> callNos = inspectionCallRepository.findCallNumbersByPo(dto.getPoNo());

                        if (callNos == null || callNos.isEmpty()) {

                                dto.setFinalQuantityAcceptedByRites(0);
                                dto.setBalancePoQty(dto.getPoQty());
                                dto.setProcessInspectionRejectionPercentage(0.0);

                                continue;
                        }

                       /* // ================= Offered + Rejected =================
                        List<Object[]> resultList = rmHeatFinalResultRepository
                                        .findOfferedAndRejectedByCallNos(callNos);

                        double offered = 0.0;
                        double rejected = 0.0;

                        // if (resultList != null && !resultList.isEmpty()) {
                        //
                        // Object[] result = resultList.get(0);
                        //
                        // if (result[0] != null)
                        // offered = ((Number) result[0]).doubleValue();
                        //
                        // if (result[1] != null)
                        // rejected = ((Number) result[1]).doubleValue();
                        // }
                        if (resultList != null && !resultList.isEmpty()) {

                                for (Object[] result : resultList) {

                                        if (result[0] != null)
                                                offered += ((Number) result[0]).doubleValue();

                                        if (result[1] != null)
                                                rejected += ((Number) result[1]).doubleValue();
                                }
                        }

                        // ================= Final Accepted =================
                        int accepted = (int) Math.round(offered);
                        dto.setFinalQuantityAcceptedByRites(accepted);

                        // ================= Balance =================
                        int balance = dto.getPoQty() - accepted;
                        balance = Math.max(balance, 0); // safety

                        dto.setBalancePoQty(balance);*/

                        List<Object[]> finalResultList =
                                finalCumulativeResultsRepository.findFinalInspectionQty(callNos);

                        double passed = 0.0;
                        double rejected = 0.0;

                        if (finalResultList != null && !finalResultList.isEmpty()) {

                                Object[] finalResult = finalResultList.get(0);

                                if (finalResult[0] != null)
                                        passed = ((Number) finalResult[0]).doubleValue();

                                if (finalResult[1] != null)
                                        rejected = ((Number) finalResult[1]).doubleValue();
                        }

// Final Accepted
                        int accepted = (int) Math.round(passed);
                        dto.setFinalQuantityAcceptedByRites(accepted);

                        int balance = dto.getPoQty() - accepted;
                        dto.setBalancePoQty(Math.max(balance, 0));

                        double finalRejectPct = 0.0;

                        if (passed + rejected > 0) {
                                finalRejectPct = (rejected * 100.0) / (passed + rejected);
                        }

                        dto.setFinalInspectionRejectionPercentage(finalRejectPct);

                        // ================= Process Rejection % =================
                        Double processPct = processIeQtyRepository
                                        .findProcessRejectionPctByCallNos(callNos);

                        dto.setProcessInspectionRejectionPercentage(
                                        processPct != null ? processPct : 0.0);
                }

                return list;
        }

        @Override
        public List<PoInspection2ndLevelSerialStatusDto> getSerialStatusByPoNo(String poNo) {

                List<PoInspection2ndLevelSerialStatusDto> list = poItemRepository.fetchSerialStatusByPoNo(poNo);

                AtomicInteger counter = new AtomicInteger(1);

                for (PoInspection2ndLevelSerialStatusDto dto : list) {

                        // ============ Sl No ============
                        dto.setSlNo(counter.getAndIncrement());

                        // ============ Get Call Numbers ============
                        List<String> callNos = inspectionCallRepository
                                        .findCallNosByPoAndSerial(poNo, dto.getRlyPoSrNo());

                        if (callNos == null || callNos.isEmpty()) {

                                dto.setRawMaterialAcceptedMt(0.0);
                                dto.setRawMaterialRejectionPercentage(0.0);

                                dto.setProcessInspectionMaterialAcceptedNos(0);
                                dto.setProcessInspectionMaterialRejectionPercentage(0.0);

                                continue;
                        }

                        // ================= RM Summary =================
                        // ================= RM Summary =================
                        List<Object[]> rmResultList = rmHeatFinalResultRepository
                                        .findRmSummaryByCallNos(callNos);

                        double rmAccepted = 0.0;
                        double rmRejected = 0.0;
                        double rmOffered = 0.0;

                        if (rmResultList != null && !rmResultList.isEmpty()) {

                                Object[] row = rmResultList.get(0);

                                // row[0] = callNo (String) → ignore

                                if (row[1] != null)
                                        rmOffered = ((Number) row[1]).doubleValue();

                                if (row[2] != null)
                                        rmAccepted = ((Number) row[2]).doubleValue();

                                if (row[3] != null)
                                        rmRejected = ((Number) row[3]).doubleValue();
                        }

                        // ================= Set RM =================
                        dto.setRawMaterialAcceptedMt(rmAccepted);

                        double rmRejectionPct = 0.0;

                        if (rmOffered > 0) {
                                rmRejectionPct = (rmRejected * 100.0) / rmOffered;
                        }

                        dto.setRawMaterialRejectionPercentage(rmRejectionPct);

                        // ================= Process Summary =================
//                        List<Object[]> processResultList = processIeQtyRepository
//                                        .findProcessSummaryByCallNos(callNos);
                        List<Object[]> processRows = processLineFinalResultRepository
                                .findProcessLineSummaryByCallNos(callNos);

                        double totalManufactured = 0;
                        double totalRejected = 0;

                        if (processRows != null) {

                                for (Object[] row : processRows) {

                                        if (row[0] != null)
                                                totalManufactured += ((Number) row[0]).doubleValue();

                                        if (row[1] != null)
                                                totalRejected += ((Number) row[1]).doubleValue();
                                }
                        }

                        int processAccepted = (int) (totalManufactured - totalRejected);

                        dto.setProcessInspectionMaterialAcceptedNos(processAccepted);

                        double processRejectionPct = 0.0;

                        if (totalManufactured > 0) {

                                processRejectionPct = (totalRejected * 100.0) / totalManufactured;

                                processRejectionPct = Math.round(processRejectionPct * 100.0) / 100.0;
                        }

                        dto.setProcessInspectionMaterialRejectionPercentage(processRejectionPct);

                      /*  int processAccepted = 0;
                        double processRejected = 0.0;
                        double processOffered = 0.0;

                        if (processResultList != null && !processResultList.isEmpty()) {

                                Object[] row = processResultList.get(0);

                                if (row[0] != null)
                                        processAccepted = ((Number) row[0]).intValue();

                                if (row[1] != null)
                                        processRejected = ((Number) row[1]).doubleValue();

                                if (row[2] != null)
                                        processOffered = ((Number) row[2]).doubleValue();
                        }

                        // ================= Set Process =================
                        dto.setProcessInspectionMaterialAcceptedNos(processAccepted);

                        double processRejectionPct = 0.0;

                        if (processOffered > 0) {
                                processRejectionPct = (processRejected * 100.0) / processOffered;
                        }

                        dto.setProcessInspectionMaterialRejectionPercentage(processRejectionPct);

                       */
                          }

                return list;
        }
        /*
         * @Override
         * public List<PoInspection3rdLevelCallStatusDto>
         * getCallWiseStatusBySerialNo(String serialNo) {
         * 
         * List<InspectionCall> calls =
         * inspectionCallRepository.findBySerialNo(serialNo);
         * 
         * List<PoInspection3rdLevelCallStatusDto> result = new ArrayList<>();
         * 
         * AtomicInteger counter = new AtomicInteger(1);
         * 
         * for (InspectionCall call : calls) {
         * 
         * String callNo = call.getIcNumber();
         * 
         * 
         * // ============ Get Start & End Date (Single Query) ============
         * List<Object[]> dateList =
         * workflowTransitionRepository
         * .findStartAndEndDateByRequestId(callNo);
         * 
         * Date startDate = null;
         * Date completionDate = null;
         * 
         * if (dateList != null && !dateList.isEmpty()) {
         * 
         * Object[] dates = dateList.get(0);
         * 
         * if (dates[0] != null)
         * startDate = (Date) dates[0];
         * 
         * if (dates[1] != null)
         * completionDate = (Date) dates[1];
         * }
         * 
         * // ============ Mandays ============
         * Integer mandays = null;
         * 
         * if (startDate != null && completionDate != null) {
         * 
         * long diff =
         * completionDate.getTime() - startDate.getTime();
         * 
         * mandays = (int) TimeUnit.MILLISECONDS.toDays(diff);
         * 
         * if (mandays == 0) mandays = 1;
         * }
         * 
         * 
         * // ============ Build DTO ============
         * PoInspection3rdLevelCallStatusDto dto =
         * new PoInspection3rdLevelCallStatusDto(
         * 
         * counter.getAndIncrement(),
         * 
         * serialNo,
         * callNo,
         * call.getTypeOfCall(),
         * call.getDesiredInspectionDate(),
         * 
         * startDate,
         * completionDate,
         * 
         * mandays,
         * 
         * null,
         * null,
         * null,
         * 
         * null,
         * call.getRemarks(),
         * 
         * callNo
         * );
         * 
         * result.add(dto);
         * }
         * 
         * return result;
         * }
         * 
         */

        @Override
        public List<PoInspection3rdLevelCallStatusDto> getCallWiseStatusBy(String poNo, String serialNo) {

                List<InspectionCall> calls = inspectionCallRepository.findByPoNoAndSerialNo(poNo, serialNo);

                List<PoInspection3rdLevelCallStatusDto> result = new ArrayList<>();

                AtomicInteger counter = new AtomicInteger(1);

                for (InspectionCall call : calls) {

                        String callNo = call.getIcNumber();
                        String callType = call.getTypeOfCall();

                        // ================= Workflow Dates =================
                        List<Object[]> dateList = workflowTransitionRepository
                                        .findStartAndEndDateByRequestId(callNo);

                        Date startDate = null;
                        Date completionDate = null;

                        if (dateList != null && !dateList.isEmpty()) {

                                Object[] dates = dateList.get(0);

                                if (dates[0] != null)
                                        startDate = (Date) dates[0];

                                if (dates[1] != null)
                                        completionDate = (Date) dates[1];
                        }

                        String certificateNo = inspectionCompleteDetailsRepository
                                        .findCertificateNoByCallNo(callNo);

                        // dto.setIcNumber(
                        // certificateNo != null ? certificateNo : "-"
                        // );

                        // ================= Mandays =================
                        Integer mandays = null;

                        if (startDate != null && completionDate != null) {

                                long diff = completionDate.getTime() - startDate.getTime();

                                mandays = (int) TimeUnit.MILLISECONDS.toDays(diff);

                                if (mandays == 0)
                                        mandays = 1;
                        }

                        // ================= Qty Variables =================
                        Double offeredQty = null;
                        Double acceptedQty = null;
                        Double balanceQty = null;
                        Double rejectionPct = null;

                        // ================= RAW MATERIAL =================
                        if (callType != null &&
                                        (callType.toUpperCase().contains("RM")
                                                        || callType.toUpperCase().contains("RAW"))) {

                                List<Object[]> rmList = rmHeatFinalResultRepository
                                                .findRmSummaryByCallNos(
                                                                List.of(callNo));

                                if (rmList != null && !rmList.isEmpty()) {

                                        Object[] row = rmList.get(0);

                                        double offered = 0;
                                        double accepted = 0;
                                        double rejected = 0;

                                        if (row[2] != null)
                                                offered = ((Number) row[2]).doubleValue();

                                        if (row[0] != null)
                                                accepted = ((Number) row[0]).doubleValue();

                                        if (row[1] != null)
                                                rejected = ((Number) row[1]).doubleValue();

                                        offeredQty = offered;
                                        acceptedQty = accepted;
                                        balanceQty = offered - accepted;

                                        if (offered > 0) {
                                                rejectionPct = (rejected * 100.0) / offered;
                                        }
                                }
                        }

                        // ================= PROCESS =================
                        else if (callType != null &&
                                        callType.toUpperCase().contains("PROCESS")) {

                                List<Object[]> processList = processIeQtyRepository
                                                .findProcessQtyByCallNo(callNo);

                                if (processList != null && !processList.isEmpty()) {

                                        Object[] row = processList.get(0);

                                        double offered = 0;
                                        int accepted = 0;
                                        double rejected = 0;

                                        if (row[0] != null)
                                                offered = ((Number) row[0]).doubleValue();

                                        if (row[1] != null)
                                                accepted = ((Number) row[1]).intValue();

                                        if (row[2] != null)
                                                rejected = ((Number) row[2]).doubleValue();

                                        offeredQty = offered;
                                        acceptedQty = (double) accepted;
                                        balanceQty = offered - accepted;

                                        if (offered > 0) {
                                                rejectionPct = (rejected * 100.0) / offered;
                                        }
                                }
                        }

                        // ================= FINAL =================
                        // else → leave null

                        // ================= Build DTO =================
                        PoInspection3rdLevelCallStatusDto dto = new PoInspection3rdLevelCallStatusDto(

                                        counter.getAndIncrement(),

                                        serialNo,
                                        callNo,
                                        callType,
                                        call.getDesiredInspectionDate(),

                                        startDate,
                                        completionDate,

                                        mandays,

                                        offeredQty,
                                        acceptedQty,
                                        balanceQty,

                                        rejectionPct,
                                        call.getRemarks(),

                                        certificateNo != null ? certificateNo : "-");

                        result.add(dto);
                }

                return result;
        }

        @Override
        public Page<PoInspection3rdLevelCallStatusDto> getCallWiseStatusBySerialNo(
                        String poNo,
                        String serialNo,
                        int page,
                        int size) {

                Pageable pageable = PageRequest.of(page, size);

                // Get paginated calls
                Page<InspectionCall> callPage = inspectionCallRepository.findByPoNoAndSerialNo(poNo, serialNo,
                                pageable);

                List<InspectionCall> calls = callPage.getContent();

                if (calls.isEmpty()) {
                        return Page.empty(pageable);
                }

                // Collect call numbers
                List<String> callNos = calls.stream()
                                .map(InspectionCall::getIcNumber)
                                .toList();

                // Bulk fetch all related data

                Map<String, Object[]> workflowMap = workflowTransitionRepository
                                .findStartAndEndDateByRequestIds(callNos)
                                .stream()
                                .collect(Collectors.toMap(
                                                r -> (String) r[0],
                                                r -> r));

                Map<String, String> certificateMap = inspectionCompleteDetailsRepository
                                .findCertificateNosByCallNos(callNos)
                                .stream()
                                .collect(Collectors.toMap(
                                                r -> (String) r[0],
                                                r -> (String) r[1]));

              /*  Map<String, Object[]> processMap = processIeQtyRepository
                                .findProcessQtyByCallNos(callNos)
                                .stream()
                                .collect(Collectors.toMap(
                                                r -> (String) r[0],
                                                r -> r));*/

                Map<String, Object[]> processMap = processLineFinalResultRepository
                        .findProcessSummaryByCallNos(callNos)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> (String) r[0],
                                r -> r));

                Map<String, Object[]> rmMap = rmHeatFinalResultRepository
                                .findRmSummaryByCallNos(callNos)
                                .stream()
                                .collect(Collectors.toMap(
                                                r -> r[0].toString(),
                                                r -> r));

                AtomicInteger counter = new AtomicInteger(page * size + 1);

                List<PoInspection3rdLevelCallStatusDto> dtoList = new ArrayList<>();
                System.out.println("RM MAP KEYS = " + rmMap.keySet());

                // Build DTO
                for (InspectionCall call : calls) {

                        String callNo = call.getIcNumber();
                        String callType = call.getTypeOfCall();
                        System.out.println("CALL NO = " + callNo);

                        // ===== Workflow =====
                        Date startDate = null;
                        Date completionDate = null;

                        Object[] wf = workflowMap.get(callNo);

                        if (wf != null) {
                                startDate = (Date) wf[1];
                                completionDate = (Date) wf[2];
                        }

                        // ===== Certificate =====
                        String certificateNo = certificateMap.getOrDefault(callNo, "-");

                        // ===== Mandays =====
                        Integer mandays = null;

                        if (startDate != null && completionDate != null) {

                                long diff = completionDate.getTime() - startDate.getTime();

                                mandays = (int) TimeUnit.MILLISECONDS.toDays(diff);

                                if (mandays == 0)
                                        mandays = 1;
                        }

                        // ===== Qty =====
                        Double offeredQty = null;
                        Double acceptedQty = null;
                        Double balanceQty = null;
                        Double rejectionPct = null;

                        // RAW MATERIAL
                        // RAW MATERIAL
                        if (callType != null &&
                                        callType.toUpperCase().contains("RAW MATERIAL")) {

                                Object[] row = rmMap.get(callNo);

                                if (row != null) {

                                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0;

                                        double accepted = row[2] != null ? ((Number) row[2]).doubleValue() : 0;

                                        double rejected = row[3] != null ? ((Number) row[3]).doubleValue() : 0;

                                        offeredQty = offered;
                                        acceptedQty = accepted;
                                        balanceQty = offered - accepted;

                                        if (offered > 0) {
                                                rejectionPct = (rejected * 100) / offered;
                                        }
                                }
                        }

                        // PROCESS
                        /*  else if (callType != null &&
                                        callType.toUpperCase().contains("PROCESS")) {

                                Object[] row = processMap.get(callNo);

                                if (row != null) {

                                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0;

                                        double accepted = row[2] != null ? ((Number) row[2]).doubleValue() : 0;

                                        double rejected = row[3] != null ? ((Number) row[3]).doubleValue() : 0;

                                        offeredQty = offered;
                                        acceptedQty = accepted;
                                        balanceQty = offered - accepted;

                                        if (offered > 0) {
                                                rejectionPct = (rejected * 100) / offered;
                                        }
                                }
                        }*/
                        else if (callType != null && callType.toUpperCase().contains("PROCESS")) {

                                Object[] row = processMap.get(callNo);

                                if (row != null) {

                                        double manufactured = row[1] != null ? ((Number) row[1]).doubleValue() : 0;

                                        double rejected = row[2] != null ? ((Number) row[2]).doubleValue() : 0;

                                        offeredQty = manufactured;
                                        acceptedQty = manufactured - rejected;
                                        balanceQty = rejected;

                                        if (manufactured > 0) {

                                                rejectionPct = (rejected * 100.0) / manufactured;

                                                rejectionPct = Math.round(rejectionPct * 100.0) / 100.0;
                                        }
                                }
                        }

                        // ===== DTO =====
                        PoInspection3rdLevelCallStatusDto dto = new PoInspection3rdLevelCallStatusDto(

                                        counter.getAndIncrement(),

                                        serialNo,
                                        callNo,
                                        callType,
                                        call.getDesiredInspectionDate(),

                                        startDate,
                                        completionDate,

                                        mandays,

                                        offeredQty,
                                        acceptedQty,
                                        balanceQty,

                                        rejectionPct,
                                        call.getRemarks(),

                                        certificateNo);

                        dtoList.add(dto);
                }

                return new PageImpl<>(
                                dtoList,
                                pageable,
                                callPage.getTotalElements());
        }

        /*
         * 
         * public List<FourthLevelInspectionDto> getFourthLevelReport(String callId) {
         * 
         * InspectionCall call = inspectionCallRepository.findByIcNumber(callId)
         * .orElseThrow(() -> new RuntimeException("Call not found"));
         * 
         * FourthLevelInspectionDto dto = new FourthLevelInspectionDto();
         * 
         * List<ProcessLineFinalResult> processList =
         * processLineFinalResultRepository.findByInspectionCallNo(callId);
         * 
         * BasicDetailsDto basic = new BasicDetailsDto();
         * 
         * basic.setDate(call.getDate());
         * basic.setShift(call.getShift());
         * basic.setRlyName(call.getRlyName());
         * basic.setPoSrNo(call.getPoSrNo());
         * basic.setLotNumber(call.getLotNumber());
         * basic.setTotalAcceptedQty(call.getAcceptedQty());
         * basic.setTotalRejectionQty(call.getRejectedQty());
         * 
         * dto.setBasicDetails(basic);
         * 
         * 
         * // ================= PROCESS QTY =================
         * ProcessQtyDto process = new ProcessQtyDto();
         * 
         * process.setShearingProductionQty(call.getShearingProd());
         * process.setShearingRejectionQty(call.getShearingRej());
         * 
         * process.setTurningProductionQty(call.getTurningProd());
         * process.setTurningRejectionQty(call.getTurningRej());
         * 
         * process.setMpiProductionQty(call.getMpiProd());
         * process.setMpiRejectionQty(call.getMpiRej());
         * 
         * process.setForgingProductionQty(call.getForgingProd());
         * process.setForgingRejectionQty(call.getForgingRej());
         * 
         * process.setQuenchingProductionQty(call.getQuenchingProd());
         * process.setQuenchingRejectionQty(call.getQuenchingRej());
         * 
         * process.setTemperingProductionQty(call.getTemperingProd());
         * process.setTemperingRejectionQty(call.getTemperingRej());
         * 
         * dto.setProcessQty(process);
         * 
         * 
         * // ================= SHEARING DEFECTS =================
         * ShearingDefectsDto shearing = new ShearingDefectsDto();
         * 
         * shearing.setLengthOfCutBar(call.getLengthCut());
         * shearing.setOvalityImproperDiaAtEnd(call.getOvality());
         * shearing.setSharpEdges(call.getSharpEdges());
         * shearing.setCrackedEdges(call.getCrackedEdges());
         * 
         * dto.setShearingDefects(shearing);
         * 
         * 
         * // ================= TURNING DEFECTS =================
         * TurningDefectsDto turning = new TurningDefectsDto();
         * 
         * turning.setParallelLength(call.getParallelLength());
         * turning.setFullTurningLength(call.getFullTurning());
         * turning.setTurningDia(call.getTurningDia());
         * 
         * dto.setTurningDefects(turning);
         * 
         * 
         * // ================= FORGING DEFECTS =================
         * ForgingDefectsDto forging = new ForgingDefectsDto();
         * 
         * forging.setForgingTemperature(call.getForgingTemp());
         * forging.setForgingStabilisationRejection(call.getForgingStable());
         * forging.setImproperForging(call.getImproperForging());
         * forging.setForgingMarksNotches(call.getMarks());
         * 
         * dto.setForgingDefects(forging);
         * 
         * 
         * // ================= DIMENSIONAL =================
         * DimensionalDefectsDto dimensional = new DimensionalDefectsDto();
         * 
         * dimensional.setBoxGauge(call.getBoxGauge());
         * dimensional.setFlatBearingArea(call.getFlatArea());
         * dimensional.setFallingGauge(call.getFallingGauge());
         * 
         * dto.setDimensionalDefects(dimensional);
         * 
         * 
         * // ================= VISUAL =================
         * VisualDefectsDto visual = new VisualDefectsDto();
         * 
         * visual.setSurfaceDefect(call.getSurfaceDefect());
         * visual.setEmbossingDefect(call.getEmbossing());
         * visual.setMarking(call.getMarking());
         * 
         * dto.setVisualDefects(visual);
         * 
         * 
         * // ================= TESTING =================
         * TestingDefectsDto testing = new TestingDefectsDto();
         * 
         * testing.setTemperingHardness(call.getTemperingHardness());
         * testing.setToeLoad(call.getToeLoad());
         * testing.setWeight(call.getWeight());
         * 
         * dto.setTestingDefects(testing);
         * 
         * 
         * // ================= FINISHING =================
         * FinishingDefectsDto finishing = new FinishingDefectsDto();
         * 
         * finishing.setPaintIdentification(call.getPaintId());
         * finishing.setErcCoating(call.getErcCoating());
         * 
         * dto.setFinishingDefects(finishing);
         * 
         * 
         * return dto;
         * }
         * 
         */

        /*
         * 
         * public List<FourthLevelInspectionDto> getFourthLevelReport(String callId) {
         * 
         * // Get call master
         * InspectionCall call = inspectionCallRepository
         * .findByIcNumber(callId)
         * .orElseThrow(() -> new RuntimeException("Call not found"));
         * 
         * 
         * // Get all process rows
         * List<ProcessLineFinalResult> processList =
         * processLineFinalResultRepository
         * .findByInspectionCallNo(callId);
         * 
         * 
         * List<FourthLevelInspectionDto> result = new ArrayList<>();
         * 
         * 
         * // Each process row → one DTO
         * for (ProcessLineFinalResult p : processList) {
         * 
         * FourthLevelInspectionDto dto =
         * new FourthLevelInspectionDto();
         * 
         * if (p.getLotNumber() == null || p.getShift()== null) {
         * // log.warn("Skipping record. lotNo={}, shift={}", lotNo, shift);
         * continue;
         * }
         * 
         * LocalDate date = p.getCreatedAt().toLocalDate();
         * LocalDateTime startDate = date.atStartOfDay();
         * LocalDateTime endDate = date.atTime(23, 59, 59);
         * // ================= BASIC =================
         * BasicDetailsDto basic = new BasicDetailsDto();
         * 
         * basic.setDate(p.getCreatedAt().toLocalDate());
         * basic.setShift(p.getShift());
         * basic.setRlyName("");
         * basic.setPoSrNo(call.getPoSerialNo());
         * basic.setLotNumber(p.getLotNumber());
         * basic.setTotalAcceptedQty(p.getTotalAccepted());
         * basic.setTotalRejectionQty(p.getTotalRejected());
         * 
         * dto.setBasicDetails(basic);
         * 
         * 
         * // ================= PROCESS =================
         * ProcessQtyDto process = new ProcessQtyDto();
         * 
         * process.setShearingProductionQty(p.getShearingManufactured());
         * process.setShearingRejectionQty(p.getShearingRejected());
         * 
         * process.setTurningProductionQty(p.getTurningManufactured());
         * process.setTurningRejectionQty(p.getTurningRejected());
         * 
         * process.setMpiProductionQty(p.getMpiManufactured());
         * process.setMpiRejectionQty(p.getMpiRejected());
         * 
         * process.setForgingProductionQty(p.getForgingManufactured());
         * process.setForgingRejectionQty(p.getForgingRejected());
         * 
         * process.setQuenchingProductionQty(p.getQuenchingManufactured());
         * process.setQuenchingRejectionQty(p.getQuenchingRejected());
         * 
         * process.setTemperingProductionQty(p.getTemperingManufactured());
         * process.setTemperingRejectionQty(p.getTemperingRejected());
         * 
         * dto.setProcessQty(process);
         * 
         * System.out.println("CALL = " + callId);
         * System.out.println("LOT  = " + p.getLotNumber());
         * System.out.println("SHIFT= " + p.getShift());
         * System.out.println("START= " + startDate);
         * System.out.println("END  = " + endDate);
         * 
         * 
         * 
         * // ================= SHEARING DEFECTS =================
         * 
         * // Get result list
         * List<Object[]> list =
         * processShearingDataRepository
         * .getShearingSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * startDate,
         * endDate
         * );
         * 
         * // Extract first row
         * Object[] sums = null;
         * 
         * if (list != null && !list.isEmpty()) {
         * sums = list.get(0); // Get first record
         * }
         * 
         * // Debug
         * if (sums != null) {
         * System.out.println("Shearing = " + Arrays.toString(sums));
         * }
         * 
         * // Map to DTO
         * ShearingDefectsDto shearing = new ShearingDefectsDto();
         * 
         * if (sums != null && sums.length == 4) {
         * 
         * shearing.setLengthOfCutBar(
         * ((Number) sums[0]).intValue());
         * 
         * shearing.setOvalityImproperDiaAtEnd(
         * ((Number) sums[1]).intValue());
         * 
         * shearing.setSharpEdges(
         * ((Number) sums[2]).intValue());
         * 
         * shearing.setCrackedEdges(
         * ((Number) sums[3]).intValue());
         * }
         * 
         * dto.setShearingDefects(shearing);
         * 
         * 
         * 
         * 
         * 
         * // ================= TURNING DEFECTS =================
         * List<Object[]> tList =
         * processTurningDataRepository.getTurningSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * startDate,
         * endDate
         * );
         * 
         * Object[] tSums = null;
         * 
         * if (tList != null && !tList.isEmpty()) {
         * tSums = tList.get(0);
         * }
         * 
         * System.out.println("Turning = " + Arrays.toString(tSums));
         * 
         * 
         * 
         * TurningDefectsDto turning = new TurningDefectsDto();
         * 
         * if (tSums != null && tSums.length == 3) {
         * 
         * turning.setParallelLength(
         * ((Number) tSums[0]).intValue());
         * 
         * turning.setFullTurningLength(
         * ((Number) tSums[1]).intValue());
         * 
         * turning.setTurningDia(
         * ((Number) tSums[2]).intValue());
         * }
         * 
         * dto.setTurningDefects(turning);
         * 
         * 
         * // ================= FORGING DEFECTS =================
         * 
         * List<Object[]> fList =
         * processForgingDataRepository.getForgingSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * startDate,
         * endDate
         * );
         * 
         * Object[] fSums = null;
         * 
         * if (fList != null && !fList.isEmpty()) {
         * fSums = fList.get(0);
         * }
         * 
         * System.out.println("Forging = " + Arrays.toString(fSums));
         * 
         * ForgingDefectsDto forging = new ForgingDefectsDto();
         * 
         * if (fSums != null && fSums.length == 4) {
         * 
         * forging.setForgingTemperature(
         * ((Number) fSums[0]).intValue());
         * 
         * forging.setForgingStabilisationRejection(
         * ((Number) fSums[1]).intValue());
         * 
         * forging.setImproperForging(
         * ((Number) fSums[2]).intValue());
         * 
         * forging.setForgingMarksNotches(
         * ((Number) fSums[3]).intValue());
         * }
         * 
         * dto.setForgingDefects(forging);
         * 
         * 
         * List<Object[]> vList =
         * processFinalCheckDataRepository.getVisualDefectsSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * startDate,
         * endDate
         * );
         * 
         * Object[] visualSums = null;
         * 
         * if (vList != null && !vList.isEmpty()) {
         * visualSums = vList.get(0);
         * }
         * 
         * System.out.println("Visual = " + Arrays.toString(visualSums));
         * 
         * VisualDefectsDto visual = new VisualDefectsDto();
         * 
         * if (visualSums != null && visualSums.length == 2) {
         * 
         * visual.setSurfaceDefect(
         * ((Number) visualSums[0]).intValue());
         * 
         * visual.setMarking(
         * ((Number) visualSums[1]).intValue());
         * }
         * 
         * dto.setVisualDefects(visual);
         * 
         * 
         * Integer forgingEmbossing =
         * processForgingDataRepository
         * .getForgingEmbossingSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * Integer finalEmbossing =
         * processFinalCheckDataRepository
         * .getFinalEmbossingSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * 
         * int totalEmbossing =
         * (forgingEmbossing != null ? forgingEmbossing : 0)
         * + (finalEmbossing != null ? finalEmbossing : 0);
         * 
         * 
         * visual.setEmbossingDefect(totalEmbossing);
         * 
         * dto.setVisualDefects(visual);
         * 
         * // Tempering hardness (Final Check)
         * Integer temperingHardness =
         * processFinalCheckDataRepository
         * .getTemperingHardnessSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * // Testing + Finishing
         * List<Object[]> tfList =
         * processTestingFinishingDataRepository
         * .getTestingFinishingSumByDate(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * startDate,
         * endDate
         * );
         * 
         * Object[] tfSums = null;
         * 
         * if (tfList != null && !tfList.isEmpty()) {
         * tfSums = tfList.get(0);
         * }
         * 
         * System.out.println("Testing+Finishing = " + Arrays.toString(tfSums));
         * 
         * 
         * // ========== Testing ==========
         * TestingDefectsDto testing = new TestingDefectsDto();
         * 
         * testing.setTemperingHardness(
         * temperingHardness != null ? temperingHardness : 0);
         * 
         * if (tfSums != null && tfSums.length == 4) {
         * 
         * testing.setToeLoad(
         * ((Number) tfSums[0]).intValue());
         * 
         * testing.setWeight(
         * ((Number) tfSums[1]).intValue());
         * }
         * 
         * dto.setTestingDefects(testing);
         * 
         * 
         * // ========== Finishing ==========
         * FinishingDefectsDto finishing = new FinishingDefectsDto();
         * 
         * if (tfSums != null && tfSums.length == 4) {
         * 
         * finishing.setPaintIdentification(
         * ((Number) tfSums[2]).intValue());
         * 
         * finishing.setErcCoating(
         * ((Number) tfSums[3]).intValue());
         * }
         * 
         * dto.setFinishingDefects(finishing);
         * 
         * // ===== BOX GAUGE =====
         * Integer quenchingBox =
         * processQuenchingDataRepository
         * .getQuenchingBoxGaugeSum(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * Integer finalBox =
         * processFinalCheckDataRepository
         * .getFinalBoxGaugeSum(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * int totalBoxGauge =
         * (quenchingBox != null ? quenchingBox : 0)
         * + (finalBox != null ? finalBox : 0);
         * 
         * 
         * Integer quenchFlat =
         * processQuenchingDataRepository
         * .getQuenchingFlatBearingSum(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * Integer quenchFall =
         * processQuenchingDataRepository
         * .getQuenchingFallingGaugeSum(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * Integer finalFlat =
         * processFinalCheckDataRepository
         * .getFinalFlatBearingSum(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * Integer finalFall =
         * processFinalCheckDataRepository
         * .getFinalFallingGaugeSum(
         * callId,
         * p.getLotNumber(),
         * p.getShift(),
         * p.getCreatedAt().toLocalDate()
         * );
         * 
         * // Safe sum
         * int flatBearing =
         * (quenchFlat != null ? quenchFlat : 0)
         * + (finalFlat != null ? finalFlat : 0);
         * 
         * int fallingGauge =
         * (quenchFall != null ? quenchFall : 0)
         * + (finalFall != null ? finalFall : 0);
         * 
         * 
         * 
         * 
         * DimensionalDefectsDto dimensional = new DimensionalDefectsDto();
         * 
         * dimensional.setBoxGauge(totalBoxGauge);
         * dimensional.setFlatBearingArea(flatBearing);
         * dimensional.setFallingGauge(fallingGauge);
         * 
         * dto.setDimensionalDefects(dimensional);
         * 
         * 
         * 
         * result.add(dto);
         * }
         * 
         * 
         * return result;
         * }
         */
        public List<FourthLevelInspectionDto> getFourthLevelReport(String callId) {

                // Get call master
                InspectionCall call = inspectionCallRepository
                                .findByIcNumber(callId)
                                .orElseThrow(() -> new RuntimeException("Call not found"));

                // Get all process rows
                List<ProcessLineFinalResult> processList = processLineFinalResultRepository
                                .findByInspectionCallNo(callId);

                // Group by date + shift + lot
                Map<String, FourthLevelInspectionDto> resultMap = new LinkedHashMap<>();

                // Each process row
                for (ProcessLineFinalResult p : processList) {

                        if (p.getLotNumber() == null || p.getShift() == null) {
                                continue;
                        }

                        LocalDate date = p.getCreatedAt().toLocalDate();

                        // Create key for grouping
                        String key = date + "|" +
                                        p.getShift() + "|" +
                                        p.getLotNumber();

                        // Get existing DTO if present
                        FourthLevelInspectionDto dto = resultMap.get(key);

                        // If first time, create new DTO
                        if (dto == null) {

                                dto = new FourthLevelInspectionDto();

                                // ================= BASIC =================
                                BasicDetailsDto basic = new BasicDetailsDto();

                                basic.setDate(date);
                                basic.setShift(p.getShift());
                                basic.setRlyName("");
                                basic.setPoSrNo(call.getPoSerialNo());
                                basic.setLotNumber(p.getLotNumber());

                                // Init with zero for sum
                                basic.setTotalAcceptedQty(0);
                                basic.setTotalRejectionQty(0);

                                dto.setBasicDetails(basic);

                                // ================= PROCESS =================
                                dto.setProcessQty(new ProcessQtyDto());

                                resultMap.put(key, dto);
                        }

                        // ================= DATE RANGE =================
                        LocalDateTime startDate = date.atStartOfDay();
                        LocalDateTime endDate = date.atTime(23, 59, 59);

                        // ================= BASIC (SUM) =================
                        BasicDetailsDto basic = dto.getBasicDetails();

                        basic.setTotalAcceptedQty(
                                        basic.getTotalAcceptedQty() + p.getTotalAccepted());

                        basic.setTotalRejectionQty(
                                        basic.getTotalRejectionQty() + p.getTotalRejected());

                        // ================= PROCESS (SUM) =================
                        ProcessQtyDto process = dto.getProcessQty();

                        process.setShearingProductionQty(
                                        process.getShearingProductionQty() + p.getShearingManufactured());

                        process.setShearingRejectionQty(
                                        process.getShearingRejectionQty() + p.getShearingRejected());

                        process.setTurningProductionQty(
                                        process.getTurningProductionQty() + p.getTurningManufactured());

                        process.setTurningRejectionQty(
                                        process.getTurningRejectionQty() + p.getTurningRejected());

                        process.setMpiProductionQty(
                                        process.getMpiProductionQty() + p.getMpiManufactured());

                        process.setMpiRejectionQty(
                                        process.getMpiRejectionQty() + p.getMpiRejected());

                        process.setForgingProductionQty(
                                        process.getForgingProductionQty() + p.getForgingManufactured());

                        process.setForgingRejectionQty(
                                        process.getForgingRejectionQty() + p.getForgingRejected());

                        process.setQuenchingProductionQty(
                                        process.getQuenchingProductionQty() + p.getQuenchingManufactured());

                        process.setQuenchingRejectionQty(
                                        process.getQuenchingRejectionQty() + p.getQuenchingRejected());

                        process.setTemperingProductionQty(
                                        process.getTemperingProductionQty() + p.getTemperingManufactured());

//                        process.setTemperingRejectionQty(
//                                        process.getTemperingRejectionQty() + p.getTemperingRejected());

                        Integer totalTemperingRejected =
                                processLineFinalResultRepository.getTotalTemperingRejected(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        startDate,
                                        endDate);

                        process.setTemperingRejectionQty(
                                process.getTemperingRejectionQty()
                                        + (totalTemperingRejected != null ? totalTemperingRejected : 0));
                        // ================= SHEARING DEFECTS =================
                        List<Object[]> list = processShearingDataRepository.getShearingSumByDate(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        startDate,
                                        endDate);

                        Object[] sums = (list != null && !list.isEmpty()) ? list.get(0) : null;

                        ShearingDefectsDto shearing = new ShearingDefectsDto();

                        if (sums != null && sums.length == 4) {

                                shearing.setLengthOfCutBar(((Number) sums[0]).intValue());
                                shearing.setOvalityImproperDiaAtEnd(((Number) sums[1]).intValue());
                                shearing.setSharpEdges(((Number) sums[2]).intValue());
                                shearing.setCrackedEdges(((Number) sums[3]).intValue());
                        }

                        dto.setShearingDefects(shearing);

                        // ================= TURNING DEFECTS =================
                        List<Object[]> tList = processTurningDataRepository.getTurningSumByDate(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        startDate,
                                        endDate);

                        Object[] tSums = (tList != null && !tList.isEmpty()) ? tList.get(0) : null;

                        TurningDefectsDto turning = new TurningDefectsDto();

                        if (tSums != null && tSums.length == 3) {

                                turning.setParallelLength(((Number) tSums[0]).intValue());
                                turning.setFullTurningLength(((Number) tSums[1]).intValue());
                                turning.setTurningDia(((Number) tSums[2]).intValue());
                        }

                        dto.setTurningDefects(turning);

                        Integer quenchingHardness = processQuenchingDataRepository.getQuenchingHardnessSum(
                                callId,
                                p.getLotNumber(),
                                p.getShift(),
                                startDate,
                                endDate
                        );

                        QuenchingDefectsDto quenching = new QuenchingDefectsDto();
                        quenching.setQuenchingHardness(quenchingHardness != null ? quenchingHardness : 0);

                        dto.setQuenchingDefects(quenching);

                        List<Object[]> temperingList = processTemperingDataRepository.getTemperingSumByDate(
                                callId,
                                p.getLotNumber(),
                                p.getShift(),
                                startDate,
                                endDate
                        );

                        Object[] temperingSums = (temperingList != null && !temperingList.isEmpty()) ? temperingList.get(0) : null;

                        TemperingDefectsDto tempering = new TemperingDefectsDto();

                        if (temperingSums != null && temperingSums.length == 2) {

                                tempering.setTemperingTemp(((Number) temperingSums[0]).intValue());
                                tempering.setTemperingDuration(((Number) temperingSums[1]).intValue());
                        }

                        dto.setTemperingDefects(tempering);
                        // ================= FORGING DEFECTS =================
                        List<Object[]> fList = processForgingDataRepository.getForgingSumByDate(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        startDate,
                                        endDate);

                        Object[] fSums = (fList != null && !fList.isEmpty()) ? fList.get(0) : null;

                        ForgingDefectsDto forging = new ForgingDefectsDto();

                        if (fSums != null && fSums.length == 4) {

                                forging.setForgingTemperature(((Number) fSums[0]).intValue());
                                forging.setForgingStabilisationRejection(((Number) fSums[1]).intValue());
                                forging.setImproperForging(((Number) fSums[2]).intValue());
                                forging.setForgingMarksNotches(((Number) fSums[3]).intValue());
                        }

                        dto.setForgingDefects(forging);

                        // ================= VISUAL DEFECTS =================
                        List<Object[]> vList = processFinalCheckDataRepository.getVisualDefectsSumByDate(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        startDate,
                                        endDate);

                        Object[] visualSums = (vList != null && !vList.isEmpty()) ? vList.get(0) : null;

                        VisualDefectsDto visual = new VisualDefectsDto();

                        if (visualSums != null && visualSums.length == 2) {

                                visual.setSurfaceDefect(((Number) visualSums[0]).intValue());
                                visual.setMarking(((Number) visualSums[1]).intValue());
                        }

                        Integer forgingEmbossing = processForgingDataRepository.getForgingEmbossingSumByDate(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        Integer finalEmbossing = processFinalCheckDataRepository.getFinalEmbossingSumByDate(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        int totalEmbossing = (forgingEmbossing != null ? forgingEmbossing : 0)
                                        + (finalEmbossing != null ? finalEmbossing : 0);

                        visual.setEmbossingDefect(totalEmbossing);

                        dto.setVisualDefects(visual);

                        // ================= TESTING =================
//                        Integer temperingHardness = processFinalCheckDataRepository.getTemperingHardnessSumByDate(
//                                        callId,
//                                        p.getLotNumber(),
//                                        p.getShift(),
//                                        date);

                        Integer temperingHardness = processFinalCheckDataRepository.getTemperingHardnessSumByDate(
                                callId,
                                p.getLotNumber(),
                                p.getShift(),
                                startDate,
                                endDate);
                        List<Object[]> tfList = processTestingFinishingDataRepository.getTestingFinishingSumByDate(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        startDate,
                                        endDate);

                        Object[] tfSums = (tfList != null && !tfList.isEmpty()) ? tfList.get(0) : null;

                        TestingDefectsDto testing = new TestingDefectsDto();

                        testing.setTemperingHardness(
                                        temperingHardness != null ? temperingHardness : 0);

                        if (tfSums != null && tfSums.length == 4) {

                                testing.setToeLoad(((Number) tfSums[0]).intValue());
                                testing.setWeight(((Number) tfSums[1]).intValue());
                        }

                        dto.setTestingDefects(testing);

                        // ================= FINISHING =================
                        FinishingDefectsDto finishing = new FinishingDefectsDto();

                        if (tfSums != null && tfSums.length == 4) {

                                finishing.setPaintIdentification(((Number) tfSums[2]).intValue());
                                finishing.setErcCoating(((Number) tfSums[3]).intValue());
                        }

                        dto.setFinishingDefects(finishing);

                        // ================= DIMENSIONAL =================
                        Integer quenchingBox = processQuenchingDataRepository.getQuenchingBoxGaugeSum(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        Integer finalBox = processFinalCheckDataRepository.getFinalBoxGaugeSum(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        int totalBoxGauge = (quenchingBox != null ? quenchingBox : 0)
                                        + (finalBox != null ? finalBox : 0);

                        Integer quenchFlat = processQuenchingDataRepository.getQuenchingFlatBearingSum(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        Integer quenchFall = processQuenchingDataRepository.getQuenchingFallingGaugeSum(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        Integer finalFlat = processFinalCheckDataRepository.getFinalFlatBearingSum(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        Integer finalFall = processFinalCheckDataRepository.getFinalFallingGaugeSum(
                                        callId,
                                        p.getLotNumber(),
                                        p.getShift(),
                                        date);

                        int flatBearing = (quenchFlat != null ? quenchFlat : 0)
                                        + (finalFlat != null ? finalFlat : 0);

                        int fallingGauge = (quenchFall != null ? quenchFall : 0)
                                        + (finalFall != null ? finalFall : 0);

                        DimensionalDefectsDto dimensional = new DimensionalDefectsDto();

                        dimensional.setBoxGauge(totalBoxGauge);
                        dimensional.setFlatBearingArea(flatBearing);
                        dimensional.setFallingGauge(fallingGauge);

                        dto.setDimensionalDefects(dimensional);

                }

                // ================= RETURN FINAL RESULT =================
                return new ArrayList<>(resultMap.values());
        }

        @Override
        public DashboardSummaryDto getDashboardSummary() {
                long poIssued = poHeaderRepository.countTotalPo();

                Long qtyNos = poItemRepository.sumQtyByUomNos();
                Double qtyMt = poItemRepository.sumQtyByUomMt();

                Long finalQtyPassed = finalCumulativeResultsRepository.sumTotalQtyNowPassed();

                // New calculations for last 30 days
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                java.util.Date thirtyDaysAgoDate = java.util.Date
                                .from(thirtyDaysAgo.atZone(java.time.ZoneId.systemDefault()).toInstant());

                // 1. Avg Production / Day (based on last 30 days)

                // 2. Process Rejection %
                List<Object[]> processRejResults = processIeQtyRepository
                                .sumProcessRejectionLast30Days(thirtyDaysAgoDate);
                double processRejectionPctValue = 0.0;
                if (processRejResults != null && !processRejResults.isEmpty() && processRejResults.get(0) != null) {
                        Object[] row = processRejResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0) {
                                processRejectionPctValue = (rejected * 100.0) / offered;
                        }
                }

                // 3. Final Rejection %
                List<Object[]> finalRejResults = finalCumulativeResultsRepository
                                .sumFinalRejectionLast30Days(thirtyDaysAgo);
                double finalRejectionPctValue = 0.0;
                if (finalRejResults != null && !finalRejResults.isEmpty() && finalRejResults.get(0) != null) {
                        Object[] row = finalRejResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0) {
                                finalRejectionPctValue = (rejected * 100.0) / offered;
                        }
                }

                // 4. Raw Material Rejection %
                List<Object[]> rmRejResults = rmHeatFinalResultRepository.sumRmRejectionLast30Days(thirtyDaysAgo);
                double rmRejectionPctValue = 0.0;
                if (rmRejResults != null && !rmRejResults.isEmpty() && rmRejResults.get(0) != null) {
                        Object[] row = rmRejResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0) {
                                rmRejectionPctValue = (rejected * 100.0) / offered;
                        }
                }

                DashboardSummaryDto dto = new DashboardSummaryDto();
                dto.setPoIssued(poIssued);
                dto.setPoQuantityNos(qtyNos != null ? qtyNos : 0L);
                dto.setPoQuantityMt(qtyMt != null ? qtyMt : 0.0);
                dto.setFinalInspectionQuantity(finalQtyPassed != null ? finalQtyPassed : 0L);

                dto.setAvgProductionPerDay(getAvgProductionPerDay());
                dto.setProcessRejectionPercentage(processRejectionPctValue);
                dto.setFinalRejectionPercentage(finalRejectionPctValue);
                dto.setRmRejectionPercentage(rmRejectionPctValue);

                return dto;
        }

        public List<String> getProcessIcNumbersByUserId(Long userId) {

                List<String> icNumbers = inspectionCallRepository.findIcNumbersByUserId(userId);

                if (icNumbers == null || icNumbers.isEmpty()) {
                        throw new RuntimeException("No IC Numbers found for this user");
                }

                return icNumbers;
        }

        @Override
        public double getAvgProductionPerDay() {
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                Long shearingSum = processLineFinalResultRepository.sumShearingManufacturedLast30Days(thirtyDaysAgo);
                return shearingSum != null ? shearingSum / 30.0 : 0.0;
        }

}
