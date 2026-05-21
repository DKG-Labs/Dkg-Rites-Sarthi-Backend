package com.sarthi.service.Impl;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.dto.QuenchingDefectsDto;
import com.sarthi.dto.TemperingDefectsDto;
import com.sarthi.dto.reports.DashboardSummaryDto;
import com.sarthi.dto.reports.InspectionCallStatusDto;
import com.sarthi.dto.reports.*;
import com.sarthi.dto.summaryDtos.CallCalculationDto;
import com.sarthi.dto.summaryDtos.PoWiseDefectsData;
import com.sarthi.dto.summaryDtos.ProcessSummaryDto;
import com.sarthi.entity.*;
import com.sarthi.entity.processmaterial.*;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.repository.*;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotResultsRepository;
import com.sarthi.repository.processmaterial.*;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.reports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        @Autowired
        private RmVisualInspectionRepository rmVisualInspectionRepository;
        @Autowired
        private RmMaterialTestingRepository rmMaterialTestingRepository;
        @Autowired
        private FinalInspectionLotResultsRepository finalInspectionLotResultsRepository;
        @Autowired
        private FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;
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

                        /*
                         * // ================= Offered + Rejected =================
                         * List<Object[]> resultList = rmHeatFinalResultRepository
                         * .findOfferedAndRejectedByCallNos(callNos);
                         *
                         * double offered = 0.0;
                         * double rejected = 0.0;
                         *
                         * // if (resultList != null && !resultList.isEmpty()) {
                         * //
                         * // Object[] result = resultList.get(0);
                         * //
                         * // if (result[0] != null)
                         * // offered = ((Number) result[0]).doubleValue();
                         * //
                         * // if (result[1] != null)
                         * // rejected = ((Number) result[1]).doubleValue();
                         * // }
                         * if (resultList != null && !resultList.isEmpty()) {
                         *
                         * for (Object[] result : resultList) {
                         *
                         * if (result[0] != null)
                         * offered += ((Number) result[0]).doubleValue();
                         *
                         * if (result[1] != null)
                         * rejected += ((Number) result[1]).doubleValue();
                         * }
                         * }
                         *
                         * // ================= Final Accepted =================
                         * int accepted = (int) Math.round(offered);
                         * dto.setFinalQuantityAcceptedByRites(accepted);
                         *
                         * // ================= Balance =================
                         * int balance = dto.getPoQty() - accepted;
                         * balance = Math.max(balance, 0); // safety
                         *
                         * dto.setBalancePoQty(balance);
                         */

                        List<Object[]> finalResultList = finalCumulativeResultsRepository
                                .findFinalInspectionQty(callNos);

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
                        // Double processPct = processIeQtyRepository
                        // .findProcessRejectionPctByCallNos(callNos);
                        //
                        // dto.setProcessInspectionRejectionPercentage(
                        // processPct != null ? processPct : 0.0);
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

                        double processRejectionPct = 0.0;

                        if (totalManufactured > 0) {

                                processRejectionPct = (totalRejected * 100.0) / totalManufactured;

                                processRejectionPct = Math.round(processRejectionPct * 100.0) / 100.0;
                        }
                        dto.setProcessInspectionRejectionPercentage(processRejectionPct);
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
                        // List<Object[]> processResultList = processIeQtyRepository
                        // .findProcessSummaryByCallNos(callNos);
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

                        /*
                         * int processAccepted = 0;
                         * double processRejected = 0.0;
                         * double processOffered = 0.0;
                         *
                         * if (processResultList != null && !processResultList.isEmpty()) {
                         *
                         * Object[] row = processResultList.get(0);
                         *
                         * if (row[0] != null)
                         * processAccepted = ((Number) row[0]).intValue();
                         *
                         * if (row[1] != null)
                         * processRejected = ((Number) row[1]).doubleValue();
                         *
                         * if (row[2] != null)
                         * processOffered = ((Number) row[2]).doubleValue();
                         * }
                         *
                         * // ================= Set Process =================
                         * dto.setProcessInspectionMaterialAcceptedNos(processAccepted);
                         *
                         * double processRejectionPct = 0.0;
                         *
                         * if (processOffered > 0) {
                         * processRejectionPct = (processRejected * 100.0) / processOffered;
                         * }
                         *
                         * dto.setProcessInspectionMaterialRejectionPercentage(processRejectionPct);
                         *
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

                /*
                 * Map<String, Object[]> processMap = processIeQtyRepository
                 * .findProcessQtyByCallNos(callNos)
                 * .stream()
                 * .collect(Collectors.toMap(
                 * r -> (String) r[0],
                 * r -> r));
                 */

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
                        /*
                         * else if (callType != null &&
                         * callType.toUpperCase().contains("PROCESS")) {
                         *
                         * Object[] row = processMap.get(callNo);
                         *
                         * if (row != null) {
                         *
                         * double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                         *
                         * double accepted = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
                         *
                         * double rejected = row[3] != null ? ((Number) row[3]).doubleValue() : 0;
                         *
                         * offeredQty = offered;
                         * acceptedQty = accepted;
                         * balanceQty = offered - accepted;
                         *
                         * if (offered > 0) {
                         * rejectionPct = (rejected * 100) / offered;
                         * }
                         * }
                         * }
                         */
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

                        LocalDate date = p.getDateOfInspection() != null
                                ? p.getDateOfInspection()
                                : p.getCreatedAt().toLocalDate();

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

                        // process.setTemperingRejectionQty(
                        // process.getTemperingRejectionQty() + p.getTemperingRejected());

                        Integer totalTemperingRejected = processLineFinalResultRepository.getTotalTemperingRejected(
                                callId,
                                p.getLotNumber(),
                                p.getShift(),
                                startDate,
                                endDate);

                        process.setTemperingRejectionQty(
                                process.getTemperingRejectionQty()
                                        + (totalTemperingRejected != null ? totalTemperingRejected
                                        : 0));
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
                                endDate);

                        QuenchingDefectsDto quenching = new QuenchingDefectsDto();
                        quenching.setQuenchingHardness(quenchingHardness != null ? quenchingHardness : 0);

                        dto.setQuenchingDefects(quenching);

                        List<Object[]> temperingList = processTemperingDataRepository.getTemperingSumByDate(
                                callId,
                                p.getLotNumber(),
                                p.getShift(),
                                startDate,
                                endDate);

                        Object[] temperingSums = (temperingList != null && !temperingList.isEmpty())
                                ? temperingList.get(0)
                                : null;

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
                        // Integer temperingHardness =
                        // processFinalCheckDataRepository.getTemperingHardnessSumByDate(
                        // callId,
                        // p.getLotNumber(),
                        // p.getShift(),
                        // date);

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
                // Modified Logic: Filter PO Issued and PO Quantity specifically for 'Elastic Rail Clips'
                // Implementation specifically placed at the bottom of this file as requested.
                long poIssued = getFilteredPoIssuedCount();
                Long qtyNos = getFilteredPoQuantityNos();
                Double qtyMt = getFilteredPoQuantityMt();

                Long finalQtyPassed = finalCumulativeResultsRepository.sumTotalQtyNowPassed();

                // New calculations
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                LocalDateTime wholeDataStart = LocalDateTime.of(2000, 1, 1, 0, 0);

                // 1. Avg Production / Day (based on last 30 days) - No change here

                // 2. Process Rejection (Revised Logic) - Updated to Whole Data
                double processRejectionPctValue = calculateProcessRejectionPercentageRevisedLogic(wholeDataStart);

                // 3. Final Rejection % - Updated to Whole Data
                List<Object[]> finalRejResults = finalCumulativeResultsRepository
                        .sumFinalRejectionLast30Days(wholeDataStart);
                double finalRejectionPctValue = 0.0;
                if (finalRejResults != null && !finalRejResults.isEmpty() && finalRejResults.get(0) != null) {
                        Object[] row = finalRejResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0) {
                                finalRejectionPctValue = (rejected * 100.0) / offered;
                        }
                }

                // 4. Raw Material Rejection % - Updated to Whole Data
                List<Object[]> rmRejResults = rmHeatFinalResultRepository.sumRmRejectionLast30Days(wholeDataStart);
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
                
                dto.setSleeperPoIssued(poHeaderRepository.countPoByItemCatDescr("PSC Mainline Sleeper"));
                Long sleeperQty = getSleeperPoQuantityNos();
                dto.setSleeperPoQuantityNos(sleeperQty != null ? sleeperQty : 0L);
                Long sleeperSetQty = getSleeperPoQuantitySet();
                dto.setSleeperPoQuantitySet(sleeperSetQty != null ? sleeperSetQty : 0L);

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
                return calculateAvgProductionPerDayNewLogic();
        }

        @Override
        public List<StageRejectionDto> getStageWiseRejection() {
                List<StageRejectionDto> data = new ArrayList<>();

                // Using last 30 days for dynamic stats (consistent with dashboard summary)
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

                // 1. Raw Material Rejection
                List<Object[]> rmResults = rmHeatFinalResultRepository.sumRmRejectionLast30Days(thirtyDaysAgo);
                double rmVal = 0.0;
                if (rmResults != null && !rmResults.isEmpty() && rmResults.get(0) != null) {
                        Object[] row = rmResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0)
                                rmVal = (rejected * 100.0) / offered;
                }
                data.add(new StageRejectionDto("Raw Material", Math.round(rmVal * 100.0) / 100.0, "#2563eb"));

                // 2. Process Rejection (Revised Logic)
                double procVal = calculateProcessRejectionPercentageRevisedLogic(thirtyDaysAgo);
                data.add(new StageRejectionDto("Process", Math.round(procVal * 100.0) / 100.0, "#f59e0b"));

                // 3. Final Rejection
                List<Object[]> finalResults = finalCumulativeResultsRepository
                        .sumFinalRejectionLast30Days(thirtyDaysAgo);
                double finalVal = 0.0;
                if (finalResults != null && !finalResults.isEmpty() && finalResults.get(0) != null) {
                        Object[] row = finalResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0)
                                finalVal = (rejected * 100.0) / offered;
                }
                data.add(new StageRejectionDto("Final", Math.round(finalVal * 100.0) / 100.0, "#ef4444"));

                return data;
        }

        @Override
        public List<StageRejectionDto> getManufacturerRejection() {
                List<StageRejectionDto> data = new ArrayList<>();
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

                // Updated logic: Join process_line_final_result with inventory_entries via heat_number
                // to calculate MPI rejection % based on supplier_name.
                List<Object[]> results = processLineFinalResultRepository.findMpiRejectionBySupplier(thirtyDaysAgo);

                // Define colors for the chart
                String[] colors = { "#ef4444", "#f59e0b", "#10b981", "#3b82f6", "#8b5cf6" };

                if (results != null) {
                        for (int i = 0; i < results.size(); i++) {
                                Object[] row = results.get(i);
                                String name = row[0] != null ? row[0].toString() : "Unknown";
                                double percentage = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;

                                // Color mapping: highest rejection is more red/alarming
                                String color = i < colors.length ? colors[i] : "#64748b";

                                data.add(new StageRejectionDto(name, Math.round(percentage * 100.0) / 100.0, color));
                        }
                }

                return data;
        }

        @Override
        public ProcessPerformanceResponseDto getProcessPerformance() {
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                java.util.Date thirtyDaysAgoDate = java.util.Date
                        .from(thirtyDaysAgo.atZone(java.time.ZoneId.systemDefault()).toInstant());

                List<Object[]> topResults = processLineFinalResultRepository
                        .findTop5ProcessPerformanceRevisedLogic(thirtyDaysAgo);
                List<Object[]> worstResults = processLineFinalResultRepository
                        .findWorst5ProcessPerformanceRevisedLogic(thirtyDaysAgo);

                List<StageRejectionDto> topList = new ArrayList<>();
                List<StageRejectionDto> worstList = new ArrayList<>();

                // Success colors (Greenish)
                String[] successColors = { "#10b981", "#34d399", "#6ee7b7", "#a7f3d0", "#d1fae5" };
                // Defect colors (Reddish)
                String[] defectColors = { "#ef4444", "#f87171", "#fca5a5", "#fecaca", "#fee2e2" };

                if (topResults != null) {
                        for (int i = 0; i < topResults.size(); i++) {
                                Object[] row = topResults.get(i);
                                String name = row[0] != null ? row[0].toString() : "Unknown";
                                double percentage = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                                String color = i < successColors.length ? successColors[i] : "#10b981";
                                topList.add(new StageRejectionDto(name, Math.round(percentage * 100.0) / 100.0, color));
                        }
                }

                if (worstResults != null) {
                        for (int i = 0; i < worstResults.size(); i++) {
                                Object[] row = worstResults.get(i);
                                String name = row[0] != null ? row[0].toString() : "Unknown";
                                double percentage = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                                String color = i < defectColors.length ? defectColors[i] : "#ef4444";
                                worstList.add(new StageRejectionDto(name, Math.round(percentage * 100.0) / 100.0,
                                        color));
                        }
                }

                return new ProcessPerformanceResponseDto(topList, worstList);
        }

        @Override
        public List<StageRejectionDto> getDailyRejectionTrend(String startDate, String endDate) {
                List<StageRejectionDto> trend = new ArrayList<>();
                try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        LocalDateTime lStart;
                        LocalDateTime lEnd;

                        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                                java.util.Date start = sdf.parse(startDate);
                                java.util.Date end = sdf.parse(endDate);
                                lStart = start.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                                        .with(java.time.LocalTime.MIN);
                                lEnd = end.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                                        .with(java.time.LocalTime.MAX);
                        } else {
                                lEnd = LocalDateTime.now();
                                lStart = lEnd.minusDays(30).with(java.time.LocalTime.MIN);
                        }

                        List<Object[]> results = processLineFinalResultRepository
                                .findDailyRejectionTrendRevisedLogic(lStart, lEnd);

                        if (results != null) {
                                for (Object[] row : results) {
                                        String date = row[0] != null ? row[0].toString() : "Unknown";
                                        double percentage = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                                        trend.add(new StageRejectionDto(date, Math.round(percentage * 100.0) / 100.0,
                                                "#8b5cf6"));
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return trend;
        }

        @Override
        public List<StageRejectionDto> getManufacturingStepWiseRejection() {
                List<StageRejectionDto> breakdown = new ArrayList<>();
                LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

                List<Object[]> results = processLineFinalResultRepository.sumStepWiseRejectionLast30Days(last30Days);

                if (results != null && !results.isEmpty()) {
                        Object[] row = results.get(0);
                        double shearing = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double turning = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        double mpi = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
                        double forging = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                        double quenching = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
                        double tempering = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;

                        double totalRejection = shearing + turning + mpi + forging + quenching + tempering;

                        // Define consistent colors from the design
                        String[] colors = { "#3b82f6", "#f59e0b", "#8b5cf6", "#ef4444", "#10b981", "#06b6d4" };

                        if (totalRejection > 0) {
                                breakdown.add(new StageRejectionDto("Shearing",
                                        Math.round((shearing * 100.0) / totalRejection * 100.0) / 100.0,
                                        colors[0]));
                                breakdown.add(new StageRejectionDto("Turning",
                                        Math.round((turning * 100.0) / totalRejection * 100.0) / 100.0,
                                        colors[1]));
                                breakdown.add(new StageRejectionDto("MPI",
                                        Math.round((mpi * 100.0) / totalRejection * 100.0) / 100.0, colors[2]));
                                breakdown.add(new StageRejectionDto("Forging",
                                        Math.round((forging * 100.0) / totalRejection * 100.0) / 100.0,
                                        colors[3]));
                                breakdown.add(new StageRejectionDto("Quenching",
                                        Math.round((quenching * 100.0) / totalRejection * 100.0) / 100.0,
                                        colors[4]));
                                breakdown.add(new StageRejectionDto("Tempering",
                                        Math.round((tempering * 100.0) / totalRejection * 100.0) / 100.0,
                                        colors[5]));
                        } else {
                                // Return zeroed data if no rejections found
                                breakdown.add(new StageRejectionDto("Shearing", 0.0, colors[0]));
                                breakdown.add(new StageRejectionDto("Turning", 0.0, colors[1]));
                                breakdown.add(new StageRejectionDto("MPI", 0.0, colors[2]));
                                breakdown.add(new StageRejectionDto("Forging", 0.0, colors[3]));
                                breakdown.add(new StageRejectionDto("Quenching", 0.0, colors[4]));
                                breakdown.add(new StageRejectionDto("Tempering", 0.0, colors[5]));
                        }
                }
                return breakdown;
        }

        @Override
        public List<InspectionCallStatusDto> getInspectionCallStatus() {
                // Updated to exclude Dummy PO data as requested
                return getInspectionCallStatusWithExclLogic();
        }

        // ================= NEW LOGIC FOR PROCESS REJECTION % =================
        // Logic: (Total pieces rejected (from shearing to tempering) / Total pieces
        // produced in Shearing) * 100
        // Added at the bottom for clarity as requested.
        private double calculateProcessRejectionPercentageNewLogic(LocalDateTime thirtyDaysAgo) {
                List<Object[]> results = processLineFinalResultRepository
                        .sumProcessRejectionNewLogicLast30Days(thirtyDaysAgo);
                if (results != null && !results.isEmpty() && results.get(0) != null) {
                        Object[] row = results.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double shearingProduced = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (shearingProduced > 0) {
                                return (rejected * 100.0) / shearingProduced;
                        }
                }
                return 0.0;
        }

        // ================= NEW LOGIC FOR AVG PRODUCTION / DAY =================
        // Logic: (Sum of total tempering produced in the last 30 days / active production days in the last 30 days)
        private double calculateAvgProductionPerDayNewLogic() {
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                Long temperingSum = processLineFinalResultRepository.sumTemperingManufacturedLast30Days(thirtyDaysAgo);
                if (temperingSum == null || temperingSum == 0) {
                        return 0.0;
                }
                Long activeDays = processLineFinalResultRepository.countDistinctProductionDaysLast30Days(thirtyDaysAgo);
                if (activeDays == null || activeDays == 0) {
                        return 0.0;
                }
                return temperingSum / (double) activeDays;
        }

        // ===== NEW: Pareto Analysis – Top 10 Rejection Parameters (all process stages)
        // =====
        @Override
        public List<StageRejectionDto> getParetoAnalysis() {
                List<Object[]> rows = processLineFinalResultRepository.getParetoAnalysisRejections();
                List<StageRejectionDto> result = new ArrayList<>();

                if (rows == null || rows.isEmpty()) {
                        return result;
                }

                // Colours cycling through a vibrant palette
                String[] palette = { "#2563eb", "#f59e0b", "#ef4444", "#10b981", "#8b5cf6",
                        "#06b6d4", "#f97316", "#ec4899", "#84cc16", "#14b8a6" };

                // Calculate total for cumulative % (Pareto line)
                long grandTotal = rows.stream()
                        .mapToLong(r -> r[1] != null ? ((Number) r[1]).longValue() : 0)
                        .sum();

                long runningTotal = 0;
                for (int i = 0; i < rows.size(); i++) {
                        Object[] row = rows.get(i);
                        String name = (String) row[0];
                        long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
                        runningTotal += count;

                        double cumulative = grandTotal > 0 ? (runningTotal * 100.0 / grandTotal) : 0;

                        StageRejectionDto dto = new StageRejectionDto(name, (double) count,
                                palette[i % palette.length]);
                        dto.setCumulative(Math.round(cumulative * 10.0) / 10.0); // 1 decimal
                        result.add(dto);
                }

                return result;
        }

        // ===== NEW: Inspection Details – Accepted vs. Rejected (RM, Process, Final)
        // =====
        @Override
        public List<InspectionDetailsDto> getInspectionDetails() {
                return getInspectionDetails(null, null);
        }

        /**
         * Updated logic for Inspection Calls Status to exclude data related to DummyPo_001.
         * This considers the requestId in workflow_transition that matches ic_number in inspection_calls.
         */
        private List<InspectionCallStatusDto> getInspectionCallStatusWithExclLogic() {
                String excludePo = "DummyPo_001";
                List<Object[]> results = workflowTransitionRepository.getInspectionCallStatusBreakdownExcludingDummyPo(excludePo);
                List<InspectionCallStatusDto> list = new ArrayList<>();

                long totalUnder = 0;
                long totalPending = 0;

                if (results != null) {
                        for (Object[] row : results) {
                                String category = (String) row[0];
                                long under = row[1] != null ? ((Number) row[1]).longValue() : 0;
                                long pending = row[2] != null ? ((Number) row[2]).longValue() : 0;

                                list.add(new InspectionCallStatusDto(category, under, pending));
                                totalUnder += under;
                                totalPending += pending;
                        }
                }

                // Prepend Total category with aggregated results
                list.add(0, new InspectionCallStatusDto("Total", totalUnder, totalPending));

                return list;
        }

        // ================= REVISED LOGIC FOR PROCESS REJECTION % =================
        // Logic: (Total pieces rejected / Total pieces produced in Shearing) * 100
        // Uses the totalRejected field from process_line_final_result
        private double calculateProcessRejectionPercentageRevisedLogic(LocalDateTime thirtyDaysAgo) {
                List<Object[]> results = processLineFinalResultRepository
                        .sumProcessRejectionRevisedLogicLast30Days(thirtyDaysAgo);
                if (results != null && !results.isEmpty() && results.get(0) != null) {
                        Object[] row = results.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double shearingProduced = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (shearingProduced > 0) {
                                return (rejected * 100.0) / shearingProduced;
                        }
                }
                return 0.0;
        }

        @Override
        public List<InspectionDetailsDto> getInspectionDetails(String startDateStr, String endDateStr) {
                LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.of(2000, 1, 1);
                LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : LocalDate.now();

                List<InspectionDetailsDto> result = new ArrayList<>();

                // 1. RM: accepted_qty_mt, weight_rejected_mt
                List<Object[]> rmData = rmHeatFinalResultRepository.sumRmAcceptedAndRejectedRevisedLogic(startDate, endDate);
                double rmAcc = 0, rmRej = 0;
                if (rmData != null && !rmData.isEmpty()) {
                        Object[] row = rmData.get(0);
                        rmAcc = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                        rmRej = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                }

                // 2. Process: tempering_accepted, total_rejected
                List<Object[]> procData = processLineFinalResultRepository
                        .sumProcessAcceptedAndRejectedRevisedLogic(startDate, endDate);
                double procAcc = 0, procRej = 0;
                if (procData != null && !procData.isEmpty()) {
                        Object[] row = procData.get(0);
                        procAcc = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                        procRej = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                }

                // 3. Final: qty_now_passed, qty_now_rejected
                List<Object[]> finalData = finalCumulativeResultsRepository
                        .sumFinalAcceptedAndRejectedRevisedLogic(startDate, endDate);
                double finalAcc = 0, finalRej = 0;
                if (finalData != null && !finalData.isEmpty()) {
                        Object[] row = finalData.get(0);
                        finalAcc = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                        finalRej = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                }

                // No decimal values (rounding for RM BigDecimals, others are typically integers)
                rmAcc = Math.round(rmAcc);
                rmRej = Math.round(rmRej);
                procAcc = Math.round(procAcc);
                procRej = Math.round(procRej);
                finalAcc = Math.round(finalAcc);
                finalRej = Math.round(finalRej);

                // 4. Calculate Total
                double totalAcc = rmAcc + procAcc + finalAcc;
                double totalRej = rmRej + procRej + finalRej;

                // Add to result in order: Total, RM, Process, Final
                result.add(new InspectionDetailsDto("Total", (long) totalAcc, (long) totalRej));
                result.add(new InspectionDetailsDto("RM", (long) rmAcc, (long) rmRej));
                result.add(new InspectionDetailsDto("Process", (long) procAcc, (long) procRej));
                result.add(new InspectionDetailsDto("Final", (long) finalAcc, (long) finalRej));

                return result;
        }

        @Override
        public List<StageRejectionDto> getMonthlyRejectionTrend(String startDate, String endDate) {
                List<StageRejectionDto> trend = new ArrayList<>();
                try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        LocalDateTime lStart;
                        LocalDateTime lEnd;

                        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                                java.util.Date start = sdf.parse(startDate);
                                java.util.Date end = sdf.parse(endDate);
                                lStart = start.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                                        .with(java.time.LocalTime.MIN);
                                lEnd = end.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                                        .with(java.time.LocalTime.MAX);
                        } else {
                                lEnd = LocalDateTime.now();
                                lStart = lEnd.minusMonths(6).with(java.time.LocalTime.MIN);
                        }

                        List<Object[]> results = processLineFinalResultRepository
                                .findMonthlyRejectionTrend(lStart, lEnd);

                        if (results != null) {
                                for (Object[] row : results) {
                                        String label = row[0] != null ? row[0].toString() : "Unknown";
                                        double percentage = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                                        trend.add(new StageRejectionDto(label, percentage, "#8b5cf6"));
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return trend;
        }

        // =========================================================================
        // FILTERED METRICS LOGIC (Calculated at the bottom for easy reference)
        // =========================================================================

        /**
         * Returns total PO count filtered by 'Elastic Rail Clips' category.
         */
        private long getFilteredPoIssuedCount() {
                return poHeaderRepository.countPoByItemCatDescr("Elastic Rail Clips");
        }

        /**
         * Returns total sum of quantity (Nos.) filtered by 'Elastic Rail Clips' category.
         */
        private Long getFilteredPoQuantityNos() {
                return poItemRepository.sumQtyByItemCatDescrAndUomNos("Elastic Rail Clips");
        }

        /**
         * Returns total sum of quantity (MT) filtered by 'Elastic Rail Clips' category.
         */
        private Double getFilteredPoQuantityMt() {
                return poItemRepository.sumQtyByItemCatDescrAndUomMt("Elastic Rail Clips");
        }

        @Override
        public long getSleeperPoCount() {
                return poHeaderRepository.countPoByItemCatDescr("PSC Mainline Sleeper");
        }

        private Long getSleeperPoQuantityNos() {
                return poItemRepository.sumQtyByItemCatDescrAndUomNos("PSC Mainline Sleeper");
        }

        private Long getSleeperPoQuantitySet() {
        return poItemRepository.sumQtyByItemCatDescrAndUomSet("PSC Mainline Sleeper");
    }

    @Override
    public List<PoIssuedDetailDto> getPoIssuedDetails(String itemCatDescr) {
        return poItemRepository.getPoIssuedDetails(itemCatDescr);
    }


        @Override
        public List<PoWiseDefectsData> getPoWiseDefectsReport(
                LocalDate startDate,
                LocalDate endDate) {

                // =========================================================
                // FETCH POS
                // =========================================================

                List<PoHeader> poHeaders =
                        poHeaderRepository.findElasticRailClipsPoHeaders(
                                startDate.atStartOfDay(),
                                endDate.atTime(LocalTime.MAX)
                        );

                List<PoWiseDefectsData> finalResponse =
                        new ArrayList<>();

                // =========================================================
                // FETCH CALLS
                // =========================================================

                List<InspectionCall> allCalls =
                        inspectionCallRepository.findAll();

                List<String> callNos = allCalls.stream()
                        .map(InspectionCall::getIcNumber)
                        .filter(Objects::nonNull)
                        .toList();

                // =========================================================
                // PROCESS SUMMARY
                // =========================================================

                List<Object[]> processResults =
                        processLineFinalResultRepository
                                .getProcessSummary(callNos);

                // =========================================================
                // RM QUERY RESULTS
                // =========================================================

                List<Object[]> visualResults =
                        rmVisualInspectionRepository
                                .getVisualRejectedWeight(callNos);

                List<Object[]> heatResults =
                        rmHeatFinalResultRepository
                                .getHeatSummary(callNos);

                List<String> inclusionCalls =
                        rmMaterialTestingRepository
                                .getInclusionDefectCalls(callNos);

                List<String> grainCalls =
                        rmMaterialTestingRepository
                                .getGrainSizeDefectCalls(callNos);

                List<String> decarbCalls =
                        rmMaterialTestingRepository
                                .getDecarbDefectCalls(callNos);

                // =========================================================
                // FINAL DEFECTS
                // =========================================================

                List<Object[]> finalDefectResults =
                        finalInspectionLotResultsRepository
                                .getFinalDefectSummary(callNos);

                List<Object[]> finalOfferedResults =
                        finalInspectionLotDetailsRepository
                                .getFinalOfferedQty(callNos);

                // =========================================================
                // PO CALL MAP
                // =========================================================

                Map<String, List<InspectionCall>> poCallMap =
                        allCalls.stream()
                                .collect(Collectors.groupingBy(
                                        InspectionCall::getPoNo));

                // =========================================================
                // PROCESS MAP
                // =========================================================

                Map<String, ProcessSummaryDto> processMap =
                        processResults.stream()
                                .collect(Collectors.toMap(

                                        r -> (String) r[0],

                                        r -> new ProcessSummaryDto(

                                                getInt(r[1]),
                                                getInt(r[2]),

                                                getInt(r[3]),
                                                getInt(r[4]),

                                                getInt(r[5]),
                                                getInt(r[6]),

                                                getInt(r[7]),
                                                getInt(r[8]),

                                                getInt(r[9]),
                                                getInt(r[10]),

                                                getInt(r[11]),
                                                getInt(r[12])
                                        )
                                ));

                // =========================================================
                // RM MAPS
                // =========================================================

                Map<String, BigDecimal> visualMap =
                        visualResults.stream()
                                .collect(Collectors.toMap(
                                        r -> (String) r[0],
                                        r -> r[1] != null
                                                ? (BigDecimal) r[1]
                                                : BigDecimal.ZERO
                                ));

                Map<String, BigDecimal> dimensionalMap =
                        heatResults.stream()
                                .collect(Collectors.toMap(
                                        r -> (String) r[0],
                                        r -> r[1] != null
                                                ? (BigDecimal) r[1]
                                                : BigDecimal.ZERO
                                ));

                Map<String, BigDecimal> offeredMap =
                        heatResults.stream()
                                .collect(Collectors.toMap(
                                        r -> (String) r[0],
                                        r -> r[2] != null
                                                ? (BigDecimal) r[2]
                                                : BigDecimal.ZERO
                                ));

                Set<String> inclusionSet =
                        new HashSet<>(inclusionCalls);

                Set<String> grainSet =
                        new HashSet<>(grainCalls);

                Set<String> decarbSet =
                        new HashSet<>(decarbCalls);

                // =========================================================
                // FINAL MAPS
                // =========================================================

                Map<String, Object[]> finalDefectMap =
                        finalDefectResults.stream()
                                .collect(Collectors.toMap(
                                        r -> (String) r[0],
                                        r -> r
                                ));

                Map<String, Integer> finalOfferedMap =
                        finalOfferedResults.stream()
                                .collect(Collectors.toMap(
                                        r -> (String) r[0],
                                        r -> ((Number) r[1]).intValue()
                                ));

                // =========================================================
                // PRECALCULATE CALLS
                // =========================================================

                Map<String, CallCalculationDto> callCalcMap =
                        new HashMap<>();

                for (InspectionCall call : allCalls) {

                        String callNo = call.getIcNumber();

                        BigDecimal factor =
                                getFactor(call.getErcType());

                        CallCalculationDto calc =
                                new CallCalculationDto();

                        calc.setPoNo(call.getPoNo());

                        // =====================================================
                        // PROCESS
                        // =====================================================

                        ProcessSummaryDto process =
                                processMap.get(callNo);

                        if (process == null) {

                                process = new ProcessSummaryDto(
                                        0,0,
                                        0,0,
                                        0,0,
                                        0,0,
                                        0,0,
                                        0,0
                                );
                        }

                        calc.setProcessQty(process);

                        // =====================================================
                        // RM DEFECTS
                        // =====================================================

                        BigDecimal visual =
                                visualMap.getOrDefault(
                                        callNo,
                                        BigDecimal.ZERO
                                ).multiply(factor);

                        BigDecimal dimensional =
                                dimensionalMap.getOrDefault(
                                        callNo,
                                        BigDecimal.ZERO
                                ).multiply(factor);

                        BigDecimal offered =
                                offeredMap.getOrDefault(
                                        callNo,
                                        BigDecimal.ZERO
                                ).multiply(factor);

                        calc.setRmVmDefect(visual);

                        calc.setRmDimensionalDefect(dimensional);

                        calc.setRmInclusionDefect(
                                inclusionSet.contains(callNo)
                                        ? offered
                                        : BigDecimal.ZERO
                        );

                        calc.setRmGrainSizeDefect(
                                grainSet.contains(callNo)
                                        ? offered
                                        : BigDecimal.ZERO
                        );

                        calc.setRmDecarbDefect(
                                decarbSet.contains(callNo)
                                        ? offered
                                        : BigDecimal.ZERO
                        );

                        // =====================================================
                        // FINAL DEFECTS
                        // =====================================================

                        Object[] finalDefect =
                                finalDefectMap.get(callNo);

                        Integer finalOffered =
                                finalOfferedMap.getOrDefault(
                                        callNo,
                                        0
                                );

                        BigDecimal finalQty =
                                BigDecimal.valueOf(finalOffered)
                                        .multiply(factor);

                        if (finalDefect != null) {

                                calc.setFinalVisualDimDefect(
                                        ((Number) finalDefect[1]).intValue() == 1
                                                ? finalQty
                                                : BigDecimal.ZERO
                                );

                                calc.setFinalHardnessDefect(
                                        ((Number) finalDefect[2]).intValue() == 1
                                                ? finalQty
                                                : BigDecimal.ZERO
                                );

                                calc.setFinalInclusionDefect(
                                        ((Number) finalDefect[3]).intValue() == 1
                                                ? finalQty
                                                : BigDecimal.ZERO
                                );

                                calc.setFinalDeflectionDefect(
                                        ((Number) finalDefect[4]).intValue() == 1
                                                ? finalQty
                                                : BigDecimal.ZERO
                                );

                                calc.setFinalToeLoadDefect(
                                        ((Number) finalDefect[5]).intValue() == 1
                                                ? finalQty
                                                : BigDecimal.ZERO
                                );
                        }

                        callCalcMap.put(callNo, calc);
                }

                // =========================================================
                // MAIN LOOP
                // =========================================================

                for (PoHeader poHeader : poHeaders) {

                        PoWiseDefectsData dto =
                                new PoWiseDefectsData();

                        dto.setZonalRailway(poHeader.getRlyShortName());
                        dto.setVendor(poHeader.getFirmDetails());
                        dto.setTypeOfErc(poHeader.getItemCatDescr());
                        dto.setPoNo(poHeader.getPoNo());

                        if (poHeader.getPoDate() != null) {

                                dto.setPoDate(
                                        poHeader.getPoDate()
                                                .toLocalDate()
                                                .toString());
                        }

                        // =====================================================
                        // PO QTY
                        // =====================================================

                        BigDecimal inspectedQty = BigDecimal.ZERO;
                        BigDecimal acceptedQty = BigDecimal.ZERO;
                        BigDecimal rejectedQty = BigDecimal.ZERO;

                        if (poHeader.getItems() != null) {

                                for (PoItem item : poHeader.getItems()) {

                                        Integer qty =
                                                item.getQty() != null
                                                        ? item.getQty()
                                                        : 0;

                                        Integer cancelledQty =
                                                item.getQtyCancelled() != null
                                                        ? item.getQtyCancelled()
                                                        : 0;

                                        inspectedQty =
                                                inspectedQty.add(
                                                        BigDecimal.valueOf(qty));

                                        acceptedQty =
                                                acceptedQty.add(
                                                        BigDecimal.valueOf(
                                                                qty - cancelledQty));

                                        rejectedQty =
                                                rejectedQty.add(
                                                        BigDecimal.valueOf(
                                                                cancelledQty));
                                }
                        }

                        dto.setQtyInspected(inspectedQty);
                        dto.setQtyAccpeted(acceptedQty);
                        dto.setTotalRejected(rejectedQty);

                        // =====================================================
                        // PROCESS QTY
                        // =====================================================

                        ProcessQtyDto processQty =
                                new ProcessQtyDto();

                        BigDecimal rmVmDefect = BigDecimal.ZERO;
                        BigDecimal rmDimentionalDefect = BigDecimal.ZERO;
                        BigDecimal rmInclusionDefect = BigDecimal.ZERO;
                        BigDecimal rmGrainSizeDefect = BigDecimal.ZERO;
                        BigDecimal rmDecarbDefect = BigDecimal.ZERO;

                        BigDecimal finalVisualDimDefect = BigDecimal.ZERO;
                        BigDecimal finalHardnessDefect = BigDecimal.ZERO;
                        BigDecimal finalInclusionDefect = BigDecimal.ZERO;
                        BigDecimal finalDeflectionDefect = BigDecimal.ZERO;
                        BigDecimal finalToeLoadDefect = BigDecimal.ZERO;

                        // =====================================================
                        // CALLS
                        // =====================================================

                        List<InspectionCall> calls =
                                poCallMap.getOrDefault(
                                        poHeader.getPoNo(),
                                        Collections.emptyList());

                        for (InspectionCall call : calls) {

                                CallCalculationDto calc =
                                        callCalcMap.get(call.getIcNumber());

                                if (calc == null) {
                                        continue;
                                }

                                ProcessSummaryDto p =
                                        calc.getProcessQty();

                                processQty.setShearingProductionQty(
                                        processQty.getShearingProductionQty()
                                                + p.getShearingProductionQty());

                                processQty.setShearingRejectionQty(
                                        processQty.getShearingRejectionQty()
                                                + p.getShearingRejectionQty());

                                processQty.setTurningProductionQty(
                                        processQty.getTurningProductionQty()
                                                + p.getTurningProductionQty());

                                processQty.setTurningRejectionQty(
                                        processQty.getTurningRejectionQty()
                                                + p.getTurningRejectionQty());

                                processQty.setMpiProductionQty(
                                        processQty.getMpiProductionQty()
                                                + p.getMpiProductionQty());

                                processQty.setMpiRejectionQty(
                                        processQty.getMpiRejectionQty()
                                                + p.getMpiRejectionQty());

                                processQty.setForgingProductionQty(
                                        processQty.getForgingProductionQty()
                                                + p.getForgingProductionQty());

                                processQty.setForgingRejectionQty(
                                        processQty.getForgingRejectionQty()
                                                + p.getForgingRejectionQty());

                                processQty.setQuenchingProductionQty(
                                        processQty.getQuenchingProductionQty()
                                                + p.getQuenchingProductionQty());

                                processQty.setQuenchingRejectionQty(
                                        processQty.getQuenchingRejectionQty()
                                                + p.getQuenchingRejectionQty());

                                processQty.setTemperingProductionQty(
                                        processQty.getTemperingProductionQty()
                                                + p.getTemperingProductionQty());

                                processQty.setTemperingRejectionQty(
                                        processQty.getTemperingRejectionQty()
                                                + p.getTemperingRejectionQty());

                                rmVmDefect =
                                        rmVmDefect.add(
                                                calc.getRmVmDefect());

                                rmDimentionalDefect =
                                        rmDimentionalDefect.add(
                                                calc.getRmDimensionalDefect());

                                rmInclusionDefect =
                                        rmInclusionDefect.add(
                                                calc.getRmInclusionDefect());

                                rmGrainSizeDefect =
                                        rmGrainSizeDefect.add(
                                                calc.getRmGrainSizeDefect());

                                rmDecarbDefect =
                                        rmDecarbDefect.add(
                                                calc.getRmDecarbDefect());

                                finalVisualDimDefect =
                                        finalVisualDimDefect.add(
                                                calc.getFinalVisualDimDefect());

                                finalHardnessDefect =
                                        finalHardnessDefect.add(
                                                calc.getFinalHardnessDefect());

                                finalInclusionDefect =
                                        finalInclusionDefect.add(
                                                calc.getFinalInclusionDefect());

                                finalDeflectionDefect =
                                        finalDeflectionDefect.add(
                                                calc.getFinalDeflectionDefect());

                                finalToeLoadDefect =
                                        finalToeLoadDefect.add(
                                                calc.getFinalToeLoadDefect());
                        }

                        // =====================================================
                        // SET DTO
                        // =====================================================

                        dto.setProcessQty(processQty);

                        dto.setRmVmDefect(rmVmDefect);
                        dto.setRmDimentionalDefect(rmDimentionalDefect);
                        dto.setRmInclusionDefect(rmInclusionDefect);
                        dto.setRmGrainSizeDefect(rmGrainSizeDefect);
                        dto.setRmDecarbDefect(rmDecarbDefect);

                        dto.setFinalVisualDimDefect(
                                finalVisualDimDefect);

                        dto.setFinalHardnessDefect(
                                finalHardnessDefect);

                        dto.setFinalInclusionDefect(
                                finalInclusionDefect);

                        dto.setFinalDeflectionDefect(
                                finalDeflectionDefect);

                        dto.setFinalToeLoadDefect(
                                finalToeLoadDefect);

                        finalResponse.add(dto);
                }

                return finalResponse;
        }

        private BigDecimal getFactor(String ercType) {

                if (ercType == null || ercType.isBlank()) {
                        return BigDecimal.ZERO;
                }

                String type = ercType.toUpperCase();

                if (type.contains("III")) {
                        return BigDecimal.valueOf(1086.96);
                }

                if (type.contains("V")) {
                        return BigDecimal.valueOf(919.12);
                }

                return BigDecimal.ZERO;
        }

        private Integer getInt(Object value) {

                if (value == null) {
                        return 0;
                }

                return ((Number) value).intValue();
        }

    @Override
    public List<com.sarthi.dto.reports.InspectionCallDetailDto> getInspectionCallStatusDetails(String stage, String status) {
        List<Object[]> rawList = workflowTransitionRepository.getInspectionCallStatusDetailsRaw(stage, status);
        List<com.sarthi.dto.reports.InspectionCallDetailDto> dtoList = new java.util.ArrayList<>();
        if (rawList != null) {
            for (Object[] row : rawList) {
                dtoList.add(com.sarthi.dto.reports.InspectionCallDetailDto.builder()
                    .inspectionCallNumber(row[0] != null ? row[0].toString() : "")
                    .vendor(row[1] != null ? row[1].toString() : "")
                    .callSubmissionDateTime(row[2] != null ? row[2].toString() : "")
                    .stageOfInspection(row[3] != null ? row[3].toString() : "")
                    .poSrNo(row[4] != null ? row[4].toString() : "")
                    .dpDate(row[5] != null ? row[5].toString() : "")
                    .status(row[6] != null ? row[6].toString() : "")
                    .build());
            }
        }
        return dtoList;
    }
}