package com.sarthi.service.Impl;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;

import com.sarthi.dto.QuenchingDefectsDto;

import com.sarthi.dto.TemperingDefectsDto;

import com.sarthi.dto.reports.DashboardSummaryDto;

import com.sarthi.dto.reports.InspectionCallStatusDto;

import com.sarthi.dto.reports.*;

import com.sarthi.dto.summaryDtos.*;

import com.sarthi.entity.*;

import com.sarthi.entity.processmaterial.*;

import com.sarthi.entity.rawmaterial.InspectionCall;

import com.sarthi.repository.*;

import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;

import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;

import com.sarthi.repository.finalmaterial.FinalInspectionLotResultsRepository;

import com.sarthi.repository.processmaterial.*;

import com.sarthi.repository.rawmaterial.InspectionCallRepository;

import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.RailVendorPlantsRepository;
import com.sarthi.SRailPad.repository.RailPadPincodePoIMappingRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionLotRepository;

import com.sarthi.SRailPad.repository.ieVerification.RailIEProductionVerificationRepository;

import com.sarthi.SRailPad.repository.ieVerification.RailFinalInspectionLotResultsRepository;

import com.sarthi.SRailPad.repository.plantDeclaration.RailProductionDeclarationRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;

import com.sarthi.dto.reports.RailPadFinalInspectionSummaryDto;

import com.sarthi.service.reports;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.concurrent.TimeUnit;

import java.util.concurrent.CompletableFuture;

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

        @Autowired

        private RailWorkflowTransactionRepository railWorkflowTransactionRepository;

        @Autowired
        private RailIEProductionVerificationRepository railIEProductionVerificationRepository;

        @Autowired
        private RailProductionDeclarationRepository railProductionDeclarationRepository;

        @Autowired
        private RailFinalInspectionLotResultsRepository railFinalInspectionLotResultsRepository;
        @Autowired
        private RailInspectionCallRepository railInspectionCallRepository;
        @Autowired
        private PincodePoIMappingRepository pincodePoIMappingRepository;
        @Autowired
        private RailVendorPlantsRepository railVendorPlantsRepository;
        @Autowired
        private RailPadPincodePoIMappingRepository railPadPincodePoIMappingRepository;
        @Autowired
        private RailInspectionLotRepository railInspectionLotRepository;

        /*
         * 
         * @Override
         * 
         * public List<PoInspection1stLevelStatusDto>
         * 
         * getPoInspection1stLevelStatusList() {
         * 
         * List<PoInspection1stLevelStatusDto> list =
         * 
         * poHeaderRepository.fetchPoInspectionStatus();
         *
         * 
         * 
         * AtomicInteger counter = new AtomicInteger(1);
         * 
         * list.forEach(dto -> dto.setSlNo(counter.getAndIncrement()));
         *
         * 
         * 
         * return list;
         * 
         * }
         * 
         */

        @Override

        public List<PoInspection1stLevelStatusDto> getPoInspection1stLevelStatusList() {
                List<PoInspection1stLevelStatusDto> list = poHeaderRepository.fetchPoInspectionStatus();
                if (list == null || list.isEmpty()) return list;

                List<String> poNos = list.stream().map(PoInspection1stLevelStatusDto::getPoNo).collect(java.util.stream.Collectors.toList());

                // 1. RM Rejection Pct Map
                java.util.Map<String, Double> rmRejectionMap = new java.util.HashMap<>();
                List<Object[]> rmResults = inspectionCallRepository.findRmRejectionPctForPos(poNos);
                if (rmResults != null) {
                    for (Object[] row : rmResults) {
                        String po = (String) row[0];
                        Double pct = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                        rmRejectionMap.put(po, pct);
                    }
                }

                // 2. PO to Call Numbers Map
                java.util.Map<String, List<String>> poToCallsMap = new java.util.HashMap<>();
                List<Object[]> callRows = inspectionCallRepository.findCallNumbersByPos(poNos);
                List<String> allCallNos = new java.util.ArrayList<>();
                if (callRows != null) {
                    for (Object[] row : callRows) {
                        String po = (String) row[0];
                        String callNo = (String) row[1];
                        poToCallsMap.computeIfAbsent(po, k -> new java.util.ArrayList<>()).add(callNo);
                        allCallNos.add(callNo);
                    }
                }

                // 3. Final Inspection Map (CallNo -> [passed, rejected])
                java.util.Map<String, double[]> finalMap = new java.util.HashMap<>();
                if (!allCallNos.isEmpty()) {
                    List<Object[]> finalResults = finalCumulativeResultsRepository.findFinalInspectionQtyBatched(allCallNos);
                    if (finalResults != null) {
                        for (Object[] row : finalResults) {
                            String callNo = (String) row[0];
                            double passed = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                            double rejected = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;
                            finalMap.put(callNo, new double[]{passed, rejected});
                        }
                    }
                }

                // 4. Process Line Map (CallNo -> [manufactured, rejected])
                java.util.Map<String, double[]> processMap = new java.util.HashMap<>();
                if (!allCallNos.isEmpty()) {
                    List<Object[]> processResults = processLineFinalResultRepository.findProcessLineSummaryByCallNosBatched(allCallNos);
                    if (processResults != null) {
                        for (Object[] row : processResults) {
                            String callNo = (String) row[0];
                            double manufactured = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                            double rejected = (row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0;
                            processMap.put(callNo, new double[]{manufactured, rejected});
                        }
                    }
                }

                AtomicInteger counter = new AtomicInteger(1);

                for (PoInspection1stLevelStatusDto dto : list) {
                        dto.setSlNo(counter.getAndIncrement());

                        dto.setRawMaterialRejectionPercentage(rmRejectionMap.getOrDefault(dto.getPoNo(), 0.0));

                        List<String> callNos = poToCallsMap.getOrDefault(dto.getPoNo(), java.util.Collections.emptyList());

                        if (callNos.isEmpty()) {
                                dto.setFinalQuantityAcceptedByRites(0);
                                dto.setBalancePoQty(dto.getPoQty());
                                dto.setProcessInspectionRejectionPercentage(0.0);
                                continue;
                        }

                        double passed = 0.0;
                        double rejected = 0.0;
                        for (String callNo : callNos) {
                            double[] res = finalMap.get(callNo);
                            if (res != null) {
                                passed += res[0];
                                rejected += res[1];
                            }
                        }

                        int accepted = (int) Math.round(passed);
                        dto.setFinalQuantityAcceptedByRites(accepted);
                        int balance = dto.getPoQty() - accepted;
                        dto.setBalancePoQty(Math.max(balance, 0));

                        double finalRejectPct = 0.0;
                        if (passed + rejected > 0) {
                                finalRejectPct = (rejected * 100.0) / (passed + rejected);
                        }
                        dto.setFinalInspectionRejectionPercentage(finalRejectPct);

                        double totalManufactured = 0;
                        double totalRejected = 0;
                        for (String callNo : callNos) {
                            double[] res = processMap.get(callNo);
                            if (res != null) {
                                totalManufactured += res[0];
                                totalRejected += res[1];
                            }
                        }

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
                         * 
                         * int processAccepted = 0;
                         * 
                         * double processRejected = 0.0;
                         * 
                         * double processOffered = 0.0;
                         *
                         * 
                         * 
                         * if (processResultList != null && !processResultList.isEmpty()) {
                         *
                         * 
                         * 
                         * Object[] row = processResultList.get(0);
                         *
                         * 
                         * 
                         * if (row[0] != null)
                         * 
                         * processAccepted = ((Number) row[0]).intValue();
                         *
                         * 
                         * 
                         * if (row[1] != null)
                         * 
                         * processRejected = ((Number) row[1]).doubleValue();
                         *
                         * 
                         * 
                         * if (row[2] != null)
                         * 
                         * processOffered = ((Number) row[2]).doubleValue();
                         * 
                         * }
                         *
                         * 
                         * 
                         * // ================= Set Process =================
                         * 
                         * dto.setProcessInspectionMaterialAcceptedNos(processAccepted);
                         *
                         * 
                         * 
                         * double processRejectionPct = 0.0;
                         *
                         * 
                         * 
                         * if (processOffered > 0) {
                         * 
                         * processRejectionPct = (processRejected * 100.0) / processOffered;
                         * 
                         * }
                         *
                         * 
                         * 
                         * dto.setProcessInspectionMaterialRejectionPercentage(processRejectionPct);
                         *
                         * 
                         * 
                         */

                }

                return list;

        }

        /*
         * 
         * @Override
         * 
         * public List<PoInspection3rdLevelCallStatusDto>
         * 
         * getCallWiseStatusBySerialNo(String serialNo) {
         *
         * 
         * 
         * List<InspectionCall> calls =
         * 
         * inspectionCallRepository.findBySerialNo(serialNo);
         *
         * 
         * 
         * List<PoInspection3rdLevelCallStatusDto> result = new ArrayList<>();
         *
         * 
         * 
         * AtomicInteger counter = new AtomicInteger(1);
         *
         * 
         * 
         * for (InspectionCall call : calls) {
         *
         * 
         * 
         * String callNo = call.getIcNumber();
         *
         *
         * 
         * 
         * 
         * // ============ Get Start & End Date (Single Query) ============
         * 
         * List<Object[]> dateList =
         * 
         * workflowTransitionRepository
         * 
         * .findStartAndEndDateByRequestId(callNo);
         *
         * 
         * 
         * Date startDate = null;
         * 
         * Date completionDate = null;
         *
         * 
         * 
         * if (dateList != null && !dateList.isEmpty()) {
         *
         * 
         * 
         * Object[] dates = dateList.get(0);
         *
         * 
         * 
         * if (dates[0] != null)
         * 
         * startDate = (Date) dates[0];
         *
         * 
         * 
         * if (dates[1] != null)
         * 
         * completionDate = (Date) dates[1];
         * 
         * }
         *
         * 
         * 
         * // ============ Mandays ============
         * 
         * Integer mandays = null;
         *
         * 
         * 
         * if (startDate != null && completionDate != null) {
         *
         * 
         * 
         * long diff =
         * 
         * completionDate.getTime() - startDate.getTime();
         *
         * 
         * 
         * mandays = (int) TimeUnit.MILLISECONDS.toDays(diff);
         *
         * 
         * 
         * if (mandays == 0) mandays = 1;
         * 
         * }
         *
         *
         * 
         * 
         * 
         * // ============ Build DTO ============
         * 
         * PoInspection3rdLevelCallStatusDto dto =
         * 
         * new PoInspection3rdLevelCallStatusDto(
         *
         * 
         * 
         * counter.getAndIncrement(),
         *
         * 
         * 
         * serialNo,
         * 
         * callNo,
         * 
         * call.getTypeOfCall(),
         * 
         * call.getDesiredInspectionDate(),
         *
         * 
         * 
         * startDate,
         * 
         * completionDate,
         *
         * 
         * 
         * mandays,
         *
         * 
         * 
         * null,
         * 
         * null,
         * 
         * null,
         *
         * 
         * 
         * null,
         * 
         * call.getRemarks(),
         *
         * 
         * 
         * callNo
         * 
         * );
         *
         * 
         * 
         * result.add(dto);
         * 
         * }
         *
         * 
         * 
         * return result;
         * 
         * }
         *
         * 
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
                 * 
                 * Map<String, Object[]> processMap = processIeQtyRepository
                 * 
                 * .findProcessQtyByCallNos(callNos)
                 * 
                 * .stream()
                 * 
                 * .collect(Collectors.toMap(
                 * 
                 * r -> (String) r[0],
                 * 
                 * r -> r));
                 * 
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
                Map<String, Object[]> finalMap = finalCumulativeResultsRepository
                                .findFinalSummaryByCallNos(callNos)
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

                        java.util.Date startDate = null;

                        java.util.Date completionDate = null;

                        Object[] wf = workflowMap.get(callNo);

                        if (wf != null) {

                                // createdDate is stored as Timestamp in DB; Timestamp extends java.util.Date
                                startDate = wf[1] != null ? new java.util.Date(((java.util.Date) wf[1]).getTime())
                                                : null;

                                completionDate = wf[2] != null ? new java.util.Date(((java.util.Date) wf[2]).getTime())
                                                : null;

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
                         * 
                         * else if (callType != null &&
                         * 
                         * callType.toUpperCase().contains("PROCESS")) {
                         *
                         * 
                         * 
                         * Object[] row = processMap.get(callNo);
                         *
                         * 
                         * 
                         * if (row != null) {
                         *
                         * 
                         * 
                         * double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                         *
                         * 
                         * 
                         * double accepted = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
                         *
                         * 
                         * 
                         * double rejected = row[3] != null ? ((Number) row[3]).doubleValue() : 0;
                         *
                         * 
                         * 
                         * offeredQty = offered;
                         * 
                         * acceptedQty = accepted;
                         * 
                         * balanceQty = offered - accepted;
                         *
                         * 
                         * 
                         * if (offered > 0) {
                         * 
                         * rejectionPct = (rejected * 100) / offered;
                         * 
                         * }
                         * 
                         * }
                         * 
                         * }
                         * 
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

                        // FINAL
                        else if (callType != null &&
                                        callType.toUpperCase().contains("FINAL")) {

                                Object[] row = finalMap.get(callNo);

                                if (row != null) {

                                        double offered = row[1] != null
                                                        ? ((Number) row[1]).doubleValue()
                                                        : 0;

                                        double accepted = row[2] != null
                                                        ? ((Number) row[2]).doubleValue()
                                                        : 0;

                                        double balance = offered - accepted;

                                        offeredQty = offered;
                                        acceptedQty = accepted;
                                        balanceQty = balance;

                                        if (offered > 0) {
                                                rejectionPct = (balance * 100.0) / offered;
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
         * 
         * 
         * public List<FourthLevelInspectionDto> getFourthLevelReport(String callId) {
         *
         * 
         * 
         * InspectionCall call = inspectionCallRepository.findByIcNumber(callId)
         * 
         * .orElseThrow(() -> new RuntimeException("Call not found"));
         *
         * 
         * 
         * FourthLevelInspectionDto dto = new FourthLevelInspectionDto();
         *
         * 
         * 
         * List<ProcessLineFinalResult> processList =
         * 
         * processLineFinalResultRepository.findByInspectionCallNo(callId);
         *
         * 
         * 
         * BasicDetailsDto basic = new BasicDetailsDto();
         *
         * 
         * 
         * basic.setDate(call.getDate());
         * 
         * basic.setShift(call.getShift());
         * 
         * basic.setRlyName(call.getRlyName());
         * 
         * basic.setPoSrNo(call.getPoSrNo());
         * 
         * basic.setLotNumber(call.getLotNumber());
         * 
         * basic.setTotalAcceptedQty(call.getAcceptedQty());
         * 
         * basic.setTotalRejectionQty(call.getRejectedQty());
         *
         * 
         * 
         * dto.setBasicDetails(basic);
         *
         *
         * 
         * 
         * 
         * // ================= PROCESS QTY =================
         * 
         * ProcessQtyDto process = new ProcessQtyDto();
         *
         * 
         * 
         * process.setShearingProductionQty(call.getShearingProd());
         * 
         * process.setShearingRejectionQty(call.getShearingRej());
         *
         * 
         * 
         * process.setTurningProductionQty(call.getTurningProd());
         * 
         * process.setTurningRejectionQty(call.getTurningRej());
         *
         * 
         * 
         * process.setMpiProductionQty(call.getMpiProd());
         * 
         * process.setMpiRejectionQty(call.getMpiRej());
         *
         * 
         * 
         * process.setForgingProductionQty(call.getForgingProd());
         * 
         * process.setForgingRejectionQty(call.getForgingRej());
         *
         * 
         * 
         * process.setQuenchingProductionQty(call.getQuenchingProd());
         * 
         * process.setQuenchingRejectionQty(call.getQuenchingRej());
         *
         * 
         * 
         * process.setTemperingProductionQty(call.getTemperingProd());
         * 
         * process.setTemperingRejectionQty(call.getTemperingRej());
         *
         * 
         * 
         * dto.setProcessQty(process);
         *
         *
         * 
         * 
         * 
         * // ================= SHEARING DEFECTS =================
         * 
         * ShearingDefectsDto shearing = new ShearingDefectsDto();
         *
         * 
         * 
         * shearing.setLengthOfCutBar(call.getLengthCut());
         * 
         * shearing.setOvalityImproperDiaAtEnd(call.getOvality());
         * 
         * shearing.setSharpEdges(call.getSharpEdges());
         * 
         * shearing.setCrackedEdges(call.getCrackedEdges());
         *
         * 
         * 
         * dto.setShearingDefects(shearing);
         *
         *
         * 
         * 
         * 
         * // ================= TURNING DEFECTS =================
         * 
         * TurningDefectsDto turning = new TurningDefectsDto();
         *
         * 
         * 
         * turning.setParallelLength(call.getParallelLength());
         * 
         * turning.setFullTurningLength(call.getFullTurning());
         * 
         * turning.setTurningDia(call.getTurningDia());
         *
         * 
         * 
         * dto.setTurningDefects(turning);
         *
         *
         * 
         * 
         * 
         * // ================= FORGING DEFECTS =================
         * 
         * ForgingDefectsDto forging = new ForgingDefectsDto();
         *
         * 
         * 
         * forging.setForgingTemperature(call.getForgingTemp());
         * 
         * forging.setForgingStabilisationRejection(call.getForgingStable());
         * 
         * forging.setImproperForging(call.getImproperForging());
         * 
         * forging.setForgingMarksNotches(call.getMarks());
         *
         * 
         * 
         * dto.setForgingDefects(forging);
         *
         *
         * 
         * 
         * 
         * // ================= DIMENSIONAL =================
         * 
         * DimensionalDefectsDto dimensional = new DimensionalDefectsDto();
         *
         * 
         * 
         * dimensional.setBoxGauge(call.getBoxGauge());
         * 
         * dimensional.setFlatBearingArea(call.getFlatArea());
         * 
         * dimensional.setFallingGauge(call.getFallingGauge());
         *
         * 
         * 
         * dto.setDimensionalDefects(dimensional);
         *
         *
         * 
         * 
         * 
         * // ================= VISUAL =================
         * 
         * VisualDefectsDto visual = new VisualDefectsDto();
         *
         * 
         * 
         * visual.setSurfaceDefect(call.getSurfaceDefect());
         * 
         * visual.setEmbossingDefect(call.getEmbossing());
         * 
         * visual.setMarking(call.getMarking());
         *
         * 
         * 
         * dto.setVisualDefects(visual);
         *
         *
         * 
         * 
         * 
         * // ================= TESTING =================
         * 
         * TestingDefectsDto testing = new TestingDefectsDto();
         *
         * 
         * 
         * testing.setTemperingHardness(call.getTemperingHardness());
         * 
         * testing.setToeLoad(call.getToeLoad());
         * 
         * testing.setWeight(call.getWeight());
         *
         * 
         * 
         * dto.setTestingDefects(testing);
         *
         *
         * 
         * 
         * 
         * // ================= FINISHING =================
         * 
         * FinishingDefectsDto finishing = new FinishingDefectsDto();
         *
         * 
         * 
         * finishing.setPaintIdentification(call.getPaintId());
         * 
         * finishing.setErcCoating(call.getErcCoating());
         *
         * 
         * 
         * dto.setFinishingDefects(finishing);
         *
         *
         * 
         * 
         * 
         * return dto;
         * 
         * }
         *
         * 
         * 
         */

        /*
         *
         * 
         * 
         * public List<FourthLevelInspectionDto> getFourthLevelReport(String callId) {
         *
         * 
         * 
         * // Get call master
         * 
         * InspectionCall call = inspectionCallRepository
         * 
         * .findByIcNumber(callId)
         * 
         * .orElseThrow(() -> new RuntimeException("Call not found"));
         *
         *
         * 
         * 
         * 
         * // Get all process rows
         * 
         * List<ProcessLineFinalResult> processList =
         * 
         * processLineFinalResultRepository
         * 
         * .findByInspectionCallNo(callId);
         *
         *
         * 
         * 
         * 
         * List<FourthLevelInspectionDto> result = new ArrayList<>();
         *
         *
         * 
         * 
         * 
         * // Each process row → one DTO
         * 
         * for (ProcessLineFinalResult p : processList) {
         *
         * 
         * 
         * FourthLevelInspectionDto dto =
         * 
         * new FourthLevelInspectionDto();
         *
         * 
         * 
         * if (p.getLotNumber() == null || p.getShift()== null) {
         * 
         * // log.warn("Skipping record. lotNo={}, shift={}", lotNo, shift);
         * 
         * continue;
         * 
         * }
         *
         * 
         * 
         * LocalDate date = p.getCreatedAt().toLocalDate();
         * 
         * LocalDateTime startDate = date.atStartOfDay();
         * 
         * LocalDateTime endDate = date.atTime(23, 59, 59);
         * 
         * // ================= BASIC =================
         * 
         * BasicDetailsDto basic = new BasicDetailsDto();
         *
         * 
         * 
         * basic.setDate(p.getCreatedAt().toLocalDate());
         * 
         * basic.setShift(p.getShift());
         * 
         * basic.setRlyName("");
         * 
         * basic.setPoSrNo(call.getPoSerialNo());
         * 
         * basic.setLotNumber(p.getLotNumber());
         * 
         * basic.setTotalAcceptedQty(p.getTotalAccepted());
         * 
         * basic.setTotalRejectionQty(p.getTotalRejected());
         *
         * 
         * 
         * dto.setBasicDetails(basic);
         *
         *
         * 
         * 
         * 
         * // ================= PROCESS =================
         * 
         * ProcessQtyDto process = new ProcessQtyDto();
         *
         * 
         * 
         * process.setShearingProductionQty(p.getShearingManufactured());
         * 
         * process.setShearingRejectionQty(p.getShearingRejected());
         *
         * 
         * 
         * process.setTurningProductionQty(p.getTurningManufactured());
         * 
         * process.setTurningRejectionQty(p.getTurningRejected());
         *
         * 
         * 
         * process.setMpiProductionQty(p.getMpiManufactured());
         * 
         * process.setMpiRejectionQty(p.getMpiRejected());
         *
         * 
         * 
         * process.setForgingProductionQty(p.getForgingManufactured());
         * 
         * process.setForgingRejectionQty(p.getForgingRejected());
         *
         * 
         * 
         * process.setQuenchingProductionQty(p.getQuenchingManufactured());
         * 
         * process.setQuenchingRejectionQty(p.getQuenchingRejected());
         *
         * 
         * 
         * process.setTemperingProductionQty(p.getTemperingManufactured());
         * 
         * process.setTemperingRejectionQty(p.getTemperingRejected());
         *
         * 
         * 
         * dto.setProcessQty(process);
         *
         * 
         * 
         * System.out.println("CALL = " + callId);
         * 
         * System.out.println("LOT  = " + p.getLotNumber());
         * 
         * System.out.println("SHIFT= " + p.getShift());
         * 
         * System.out.println("START= " + startDate);
         * 
         * System.out.println("END  = " + endDate);
         *
         *
         *
         * 
         * 
         * 
         * 
         * // ================= SHEARING DEFECTS =================
         *
         * 
         * 
         * // Get result list
         * 
         * List<Object[]> list =
         * 
         * processShearingDataRepository
         * 
         * .getShearingSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * startDate,
         * 
         * endDate
         * 
         * );
         *
         * 
         * 
         * // Extract first row
         * 
         * Object[] sums = null;
         *
         * 
         * 
         * if (list != null && !list.isEmpty()) {
         * 
         * sums = list.get(0); // Get first record
         * 
         * }
         *
         * 
         * 
         * // Debug
         * 
         * if (sums != null) {
         * 
         * System.out.println("Shearing = " + Arrays.toString(sums));
         * 
         * }
         *
         * 
         * 
         * // Map to DTO
         * 
         * ShearingDefectsDto shearing = new ShearingDefectsDto();
         *
         * 
         * 
         * if (sums != null && sums.length == 4) {
         *
         * 
         * 
         * shearing.setLengthOfCutBar(
         * 
         * ((Number) sums[0]).intValue());
         *
         * 
         * 
         * shearing.setOvalityImproperDiaAtEnd(
         * 
         * ((Number) sums[1]).intValue());
         *
         * 
         * 
         * shearing.setSharpEdges(
         * 
         * ((Number) sums[2]).intValue());
         *
         * 
         * 
         * shearing.setCrackedEdges(
         * 
         * ((Number) sums[3]).intValue());
         * 
         * }
         *
         * 
         * 
         * dto.setShearingDefects(shearing);
         *
         *
         *
         *
         *
         * 
         * 
         * 
         * 
         * 
         * 
         * // ================= TURNING DEFECTS =================
         * 
         * List<Object[]> tList =
         * 
         * processTurningDataRepository.getTurningSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * startDate,
         * 
         * endDate
         * 
         * );
         *
         * 
         * 
         * Object[] tSums = null;
         *
         * 
         * 
         * if (tList != null && !tList.isEmpty()) {
         * 
         * tSums = tList.get(0);
         * 
         * }
         *
         * 
         * 
         * System.out.println("Turning = " + Arrays.toString(tSums));
         *
         *
         *
         * 
         * 
         * 
         * 
         * TurningDefectsDto turning = new TurningDefectsDto();
         *
         * 
         * 
         * if (tSums != null && tSums.length == 3) {
         *
         * 
         * 
         * turning.setParallelLength(
         * 
         * ((Number) tSums[0]).intValue());
         *
         * 
         * 
         * turning.setFullTurningLength(
         * 
         * ((Number) tSums[1]).intValue());
         *
         * 
         * 
         * turning.setTurningDia(
         * 
         * ((Number) tSums[2]).intValue());
         * 
         * }
         *
         * 
         * 
         * dto.setTurningDefects(turning);
         *
         *
         * 
         * 
         * 
         * // ================= FORGING DEFECTS =================
         *
         * 
         * 
         * List<Object[]> fList =
         * 
         * processForgingDataRepository.getForgingSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * startDate,
         * 
         * endDate
         * 
         * );
         *
         * 
         * 
         * Object[] fSums = null;
         *
         * 
         * 
         * if (fList != null && !fList.isEmpty()) {
         * 
         * fSums = fList.get(0);
         * 
         * }
         *
         * 
         * 
         * System.out.println("Forging = " + Arrays.toString(fSums));
         *
         * 
         * 
         * ForgingDefectsDto forging = new ForgingDefectsDto();
         *
         * 
         * 
         * if (fSums != null && fSums.length == 4) {
         *
         * 
         * 
         * forging.setForgingTemperature(
         * 
         * ((Number) fSums[0]).intValue());
         *
         * 
         * 
         * forging.setForgingStabilisationRejection(
         * 
         * ((Number) fSums[1]).intValue());
         *
         * 
         * 
         * forging.setImproperForging(
         * 
         * ((Number) fSums[2]).intValue());
         *
         * 
         * 
         * forging.setForgingMarksNotches(
         * 
         * ((Number) fSums[3]).intValue());
         * 
         * }
         *
         * 
         * 
         * dto.setForgingDefects(forging);
         *
         *
         * 
         * 
         * 
         * List<Object[]> vList =
         * 
         * processFinalCheckDataRepository.getVisualDefectsSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * startDate,
         * 
         * endDate
         * 
         * );
         *
         * 
         * 
         * Object[] visualSums = null;
         *
         * 
         * 
         * if (vList != null && !vList.isEmpty()) {
         * 
         * visualSums = vList.get(0);
         * 
         * }
         *
         * 
         * 
         * System.out.println("Visual = " + Arrays.toString(visualSums));
         *
         * 
         * 
         * VisualDefectsDto visual = new VisualDefectsDto();
         *
         * 
         * 
         * if (visualSums != null && visualSums.length == 2) {
         *
         * 
         * 
         * visual.setSurfaceDefect(
         * 
         * ((Number) visualSums[0]).intValue());
         *
         * 
         * 
         * visual.setMarking(
         * 
         * ((Number) visualSums[1]).intValue());
         * 
         * }
         *
         * 
         * 
         * dto.setVisualDefects(visual);
         *
         *
         * 
         * 
         * 
         * Integer forgingEmbossing =
         * 
         * processForgingDataRepository
         * 
         * .getForgingEmbossingSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * Integer finalEmbossing =
         * 
         * processFinalCheckDataRepository
         * 
         * .getFinalEmbossingSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         *
         * 
         * 
         * 
         * int totalEmbossing =
         * 
         * (forgingEmbossing != null ? forgingEmbossing : 0)
         * 
         * + (finalEmbossing != null ? finalEmbossing : 0);
         *
         *
         * 
         * 
         * 
         * visual.setEmbossingDefect(totalEmbossing);
         *
         * 
         * 
         * dto.setVisualDefects(visual);
         *
         * 
         * 
         * // Tempering hardness (Final Check)
         * 
         * Integer temperingHardness =
         * 
         * processFinalCheckDataRepository
         * 
         * .getTemperingHardnessSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * // Testing + Finishing
         * 
         * List<Object[]> tfList =
         * 
         * processTestingFinishingDataRepository
         * 
         * .getTestingFinishingSumByDate(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * startDate,
         * 
         * endDate
         * 
         * );
         *
         * 
         * 
         * Object[] tfSums = null;
         *
         * 
         * 
         * if (tfList != null && !tfList.isEmpty()) {
         * 
         * tfSums = tfList.get(0);
         * 
         * }
         *
         * 
         * 
         * System.out.println("Testing+Finishing = " + Arrays.toString(tfSums));
         *
         *
         * 
         * 
         * 
         * // ========== Testing ==========
         * 
         * TestingDefectsDto testing = new TestingDefectsDto();
         *
         * 
         * 
         * testing.setTemperingHardness(
         * 
         * temperingHardness != null ? temperingHardness : 0);
         *
         * 
         * 
         * if (tfSums != null && tfSums.length == 4) {
         *
         * 
         * 
         * testing.setToeLoad(
         * 
         * ((Number) tfSums[0]).intValue());
         *
         * 
         * 
         * testing.setWeight(
         * 
         * ((Number) tfSums[1]).intValue());
         * 
         * }
         *
         * 
         * 
         * dto.setTestingDefects(testing);
         *
         *
         * 
         * 
         * 
         * // ========== Finishing ==========
         * 
         * FinishingDefectsDto finishing = new FinishingDefectsDto();
         *
         * 
         * 
         * if (tfSums != null && tfSums.length == 4) {
         *
         * 
         * 
         * finishing.setPaintIdentification(
         * 
         * ((Number) tfSums[2]).intValue());
         *
         * 
         * 
         * finishing.setErcCoating(
         * 
         * ((Number) tfSums[3]).intValue());
         * 
         * }
         *
         * 
         * 
         * dto.setFinishingDefects(finishing);
         *
         * 
         * 
         * // ===== BOX GAUGE =====
         * 
         * Integer quenchingBox =
         * 
         * processQuenchingDataRepository
         * 
         * .getQuenchingBoxGaugeSum(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * Integer finalBox =
         * 
         * processFinalCheckDataRepository
         * 
         * .getFinalBoxGaugeSum(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * int totalBoxGauge =
         * 
         * (quenchingBox != null ? quenchingBox : 0)
         * 
         * + (finalBox != null ? finalBox : 0);
         *
         *
         * 
         * 
         * 
         * Integer quenchFlat =
         * 
         * processQuenchingDataRepository
         * 
         * .getQuenchingFlatBearingSum(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * Integer quenchFall =
         * 
         * processQuenchingDataRepository
         * 
         * .getQuenchingFallingGaugeSum(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * Integer finalFlat =
         * 
         * processFinalCheckDataRepository
         * 
         * .getFinalFlatBearingSum(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * Integer finalFall =
         * 
         * processFinalCheckDataRepository
         * 
         * .getFinalFallingGaugeSum(
         * 
         * callId,
         * 
         * p.getLotNumber(),
         * 
         * p.getShift(),
         * 
         * p.getCreatedAt().toLocalDate()
         * 
         * );
         *
         * 
         * 
         * // Safe sum
         * 
         * int flatBearing =
         * 
         * (quenchFlat != null ? quenchFlat : 0)
         * 
         * + (finalFlat != null ? finalFlat : 0);
         *
         * 
         * 
         * int fallingGauge =
         * 
         * (quenchFall != null ? quenchFall : 0)
         * 
         * + (finalFall != null ? finalFall : 0);
         *
         *
         *
         *
         * 
         * 
         * 
         * 
         * 
         * DimensionalDefectsDto dimensional = new DimensionalDefectsDto();
         *
         * 
         * 
         * dimensional.setBoxGauge(totalBoxGauge);
         * 
         * dimensional.setFlatBearingArea(flatBearing);
         * 
         * dimensional.setFallingGauge(fallingGauge);
         *
         * 
         * 
         * dto.setDimensionalDefects(dimensional);
         *
         *
         *
         * 
         * 
         * 
         * 
         * result.add(dto);
         * 
         * }
         *
         *
         * 
         * 
         * 
         * return result;
         * 
         * }
         * 
         */

        public List<FourthLevelInspectionDto> getFourthLevelReport(String callId) {

                // Get call master

                InspectionCall call = inspectionCallRepository

                                .findByIcNumber(callId)

                                .orElseThrow(() -> new RuntimeException("Call not found"));

                String rlyShortName = "";
                if (call.getPoNo() != null) {
                        PoHeader po = poHeaderRepository.findByPoNo(call.getPoNo()).orElse(null);
                        if (po != null && po.getRlyShortName() != null) {
                                rlyShortName = po.getRlyShortName();
                        }
                }

                String displayPoSrNo = call.getPoSerialNo();
                if (!rlyShortName.isEmpty() && displayPoSrNo != null) {
                        displayPoSrNo = rlyShortName + "/" + displayPoSrNo;
                }

                // Get all process rows

                List<ProcessLineFinalResult> processList = processLineFinalResultRepository

                                .findByInspectionCallNo(callId);

                // Group by date + shift + lot

                Map<String, FourthLevelInspectionDto> resultMap = new LinkedHashMap<>();

                // =============================================
                // BUILD CREATED_AT MAP FIRST
                // =============================================
                Map<String, Set<LocalDateTime>> createdAtMap = new HashMap<>();

                for (ProcessLineFinalResult p : processList) {

                        LocalDate date = p.getDateOfInspection() != null
                                        ? p.getDateOfInspection()
                                        : p.getCreatedAt().toLocalDate();

                        String key = date + "|"
                                        + p.getShift() + "|"
                                        + p.getLotNumber();

                        createdAtMap
                                        .computeIfAbsent(key, k -> new HashSet<>())
                                        .add(p.getCreatedAt());
                }

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

                                basic.setRlyName(rlyShortName);

                                basic.setPoSrNo(displayPoSrNo);

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

                        // LocalDateTime createdAt = p.getCreatedAt();

                        // LocalDateTime startDate = createdAt.minusMinutes(2);

                        // LocalDateTime endDate = createdAt.plusMinutes(2);

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

                        if (dto != null && dto.getShearingDefects() != null) {
                                continue;
                        }
                        Set<LocalDateTime> createdAtList = createdAtMap.get(key);

                        System.out.println("created uday" + createdAtMap);

                        System.out.println("created uday1" + createdAtList);

                        for (LocalDateTime createdAt : createdAtList) {

                                LocalDateTime startDate = createdAt.minusMinutes(2);

                                LocalDateTime endDate = createdAt.plusMinutes(2);

                                Integer totalTemperingRejected = processLineFinalResultRepository
                                                .getTotalTemperingRejected(

                                                                callId,

                                                                p.getLotNumber(),

                                                                p.getShift(),

                                                                startDate,

                                                                endDate);

                                process.setTemperingRejectionQty(

                                                process.getTemperingRejectionQty()

                                                                + (totalTemperingRejected != null
                                                                                ? totalTemperingRejected
                                                                                : 0));

                                // ================= SHEARING DEFECTS =================

                                List<Object[]> list = processShearingDataRepository.getShearingSumByDate(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate,

                                                endDate);

                                Object[] sums = (list != null && !list.isEmpty()) ? list.get(0) : null;

                                System.out.println(
                                                "DATE=" + date
                                                                + ", SHIFT=" + p.getShift()
                                                                + ", LOT=" + p.getLotNumber()
                                                                + ", CREATED_AT=" + createdAt
                                                                + ", SHEARING=" + Arrays.toString(sums));

                                ShearingDefectsDto shearing = dto.getShearingDefects();

                                if (shearing == null) {

                                        shearing = new ShearingDefectsDto();

                                        shearing.setLengthOfCutBar(0);

                                        shearing.setOvalityImproperDiaAtEnd(0);

                                        shearing.setSharpEdges(0);

                                        shearing.setCrackedEdges(0);
                                }

                                if (sums != null && sums.length == 4) {

                                        shearing.setLengthOfCutBar(

                                                        shearing.getLengthOfCutBar()

                                                                        + ((Number) sums[0]).intValue());

                                        shearing.setOvalityImproperDiaAtEnd(

                                                        shearing.getOvalityImproperDiaAtEnd()

                                                                        + ((Number) sums[1]).intValue());

                                        shearing.setSharpEdges(

                                                        shearing.getSharpEdges()

                                                                        + ((Number) sums[2]).intValue());

                                        shearing.setCrackedEdges(

                                                        shearing.getCrackedEdges()

                                                                        + ((Number) sums[3]).intValue());
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

                                TurningDefectsDto turning = dto.getTurningDefects();

                                if (turning == null) {

                                        turning = new TurningDefectsDto();

                                        turning.setParallelLength(0);

                                        turning.setFullTurningLength(0);

                                        turning.setTurningDia(0);
                                }

                                if (tSums != null && tSums.length == 3) {

                                        turning.setParallelLength(

                                                        turning.getParallelLength()

                                                                        + ((Number) tSums[0]).intValue());

                                        turning.setFullTurningLength(

                                                        turning.getFullTurningLength()

                                                                        + ((Number) tSums[1]).intValue());

                                        turning.setTurningDia(

                                                        turning.getTurningDia()

                                                                        + ((Number) tSums[2]).intValue());
                                }

                                dto.setTurningDefects(turning);

                                // ================= QUENCHING DEFECTS =================

                                Integer quenchingHardness = processQuenchingDataRepository.getQuenchingHardnessSum(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate,

                                                endDate);

                                QuenchingDefectsDto quenching = dto.getQuenchingDefects();

                                if (quenching == null) {

                                        quenching = new QuenchingDefectsDto();

                                        quenching.setQuenchingHardness(0);
                                }

                                quenching.setQuenchingHardness(
                                                quenching.getQuenchingHardness()
                                                                + (quenchingHardness != null ? quenchingHardness : 0));

                                Integer qBoxGauge = processQuenchingDataRepository.getQuenchingBoxGaugeSum(
                                                callId, p.getLotNumber(), p.getShift(), startDate, endDate);
                                quenching.setBoxGaugeRejected((quenching.getBoxGaugeRejected() != null ? quenching.getBoxGaugeRejected() : 0) + (qBoxGauge != null ? qBoxGauge : 0));

                                Integer qFlat = processQuenchingDataRepository.getQuenchingFlatBearingSum(
                                                callId, p.getLotNumber(), p.getShift(), startDate, endDate);
                                quenching.setFlatBearingAreaRejected((quenching.getFlatBearingAreaRejected() != null ? quenching.getFlatBearingAreaRejected() : 0) + (qFlat != null ? qFlat : 0));

                                Integer qFall = processQuenchingDataRepository.getQuenchingFallingGaugeSum(
                                                callId, p.getLotNumber(), p.getShift(), startDate, endDate);
                                quenching.setFallingGaugeRejected((quenching.getFallingGaugeRejected() != null ? quenching.getFallingGaugeRejected() : 0) + (qFall != null ? qFall : 0));

                                dto.setQuenchingDefects(quenching);

                                // ================= TEMPERING DEFECTS =================

                                List<Object[]> temperingList = processTemperingDataRepository.getTemperingSumByDate(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate,

                                                endDate);

                                Object[] temperingSums = (temperingList != null && !temperingList.isEmpty())

                                                ? temperingList.get(0)

                                                : null;

                                TemperingDefectsDto tempering = dto.getTemperingDefects();

                                if (tempering == null) {

                                        tempering = new TemperingDefectsDto();

                                        tempering.setTemperingTemp(0);

                                        tempering.setTemperingDuration(0);
                                }

                                if (temperingSums != null && temperingSums.length == 2) {

                                        tempering.setTemperingTemp(

                                                        tempering.getTemperingTemp()

                                                                        + ((Number) temperingSums[0]).intValue());

                                        tempering.setTemperingDuration(

                                                        tempering.getTemperingDuration()

                                                                        + ((Number) temperingSums[1]).intValue());
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

                                System.out.println(
                                                "FORGING => DATE=" + date
                                                                + ", CREATED_AT=" + createdAt
                                                                + ", VALUES=" + Arrays.toString(fSums));
                                ForgingDefectsDto forging = dto.getForgingDefects();

                                if (forging == null) {

                                        forging = new ForgingDefectsDto();

                                        forging.setForgingTemperature(0);

                                        forging.setForgingStabilisationRejection(0);

                                        forging.setImproperForging(0);

                                        forging.setForgingMarksNotches(0);
                                }

                                if (fSums != null && fSums.length == 4) {

                                        forging.setForgingTemperature(

                                                        forging.getForgingTemperature()

                                                                        + ((Number) fSums[0]).intValue());

                                        forging.setForgingStabilisationRejection(

                                                        forging.getForgingStabilisationRejection()

                                                                        + ((Number) fSums[1]).intValue());

                                        forging.setImproperForging(

                                                        forging.getImproperForging()

                                                                        + ((Number) fSums[2]).intValue());

                                        forging.setForgingMarksNotches(

                                                        forging.getForgingMarksNotches()

                                                                        + ((Number) fSums[3]).intValue());
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

                                VisualDefectsDto visual = dto.getVisualDefects();

                                if (visual == null) {

                                        visual = new VisualDefectsDto();

                                        visual.setSurfaceDefect(0);

                                        visual.setMarking(0);

                                        visual.setEmbossingDefect(0);
                                }

                                if (visualSums != null && visualSums.length == 2) {

                                        visual.setSurfaceDefect(

                                                        visual.getSurfaceDefect()

                                                                        + ((Number) visualSums[0]).intValue());

                                        visual.setMarking(

                                                        visual.getMarking()

                                                                        + ((Number) visualSums[1]).intValue());
                                }

                                Integer forgingEmbossing = processForgingDataRepository.getForgingEmbossingSumByDate(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate, endDate);

                                Integer finalEmbossing = processFinalCheckDataRepository.getFinalEmbossingSumByDate(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate, endDate);

                                visual.setEmbossingDefect(

                                                visual.getEmbossingDefect()

                                                                + (forgingEmbossing != null ? forgingEmbossing : 0)

                                                                + (finalEmbossing != null ? finalEmbossing : 0));

                                dto.setVisualDefects(visual);

                                // ================= TESTING DEFECTS =================

                                Integer temperingHardness = processFinalCheckDataRepository
                                                .getTemperingHardnessSumByDate(

                                                                callId,

                                                                p.getLotNumber(),

                                                                p.getShift(),

                                                                startDate,

                                                                endDate);

                                List<Object[]> tfList = processTestingFinishingDataRepository
                                                .getTestingFinishingSumByDate(

                                                                callId,

                                                                p.getLotNumber(),

                                                                p.getShift(),

                                                                startDate,

                                                                endDate);

                                Object[] tfSums = (tfList != null && !tfList.isEmpty()) ? tfList.get(0) : null;

                                TestingDefectsDto testing = dto.getTestingDefects();

                                if (testing == null) {

                                        testing = new TestingDefectsDto();

                                        testing.setTemperingHardness(0);

                                        testing.setToeLoad(0);

                                        testing.setWeight(0);
                                }

                                testing.setTemperingHardness(

                                                testing.getTemperingHardness()

                                                                + (temperingHardness != null ? temperingHardness : 0));

                                if (tfSums != null && tfSums.length == 4) {

                                        testing.setToeLoad(

                                                        testing.getToeLoad()

                                                                        + ((Number) tfSums[0]).intValue());

                                        testing.setWeight(

                                                        testing.getWeight()

                                                                        + ((Number) tfSums[1]).intValue());
                                }

                                dto.setTestingDefects(testing);

                                // ================= FINISHING DEFECTS =================

                                FinishingDefectsDto finishing = dto.getFinishingDefects();

                                if (finishing == null) {

                                        finishing = new FinishingDefectsDto();

                                        finishing.setPaintIdentification(0);

                                        finishing.setErcCoating(0);
                                }

                                if (tfSums != null && tfSums.length == 4) {

                                        finishing.setPaintIdentification(

                                                        finishing.getPaintIdentification()

                                                                        + ((Number) tfSums[2]).intValue());

                                        finishing.setErcCoating(

                                                        finishing.getErcCoating()

                                                                        + ((Number) tfSums[3]).intValue());
                                }

                                dto.setFinishingDefects(finishing);

                                Integer boxGauge = processQuenchingDataRepository.getQuenchingBoxGaugeSum(
                                                callId,
                                                p.getLotNumber(),
                                                p.getShift(),
                                                startDate,
                                                endDate);

                                Integer finalBox = processFinalCheckDataRepository.getFinalBoxGaugeSum(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate, endDate);

                                Integer quenchFlat = processQuenchingDataRepository.getQuenchingFlatBearingSum(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate, endDate);

                                Integer quenchFall = processQuenchingDataRepository.getQuenchingFallingGaugeSum(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate, endDate);

                                Integer finalFlat = processFinalCheckDataRepository.getFinalFlatBearingSum(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),

                                                startDate, endDate);

                                Integer finalFall = processFinalCheckDataRepository.getFinalFallingGaugeSum(

                                                callId,

                                                p.getLotNumber(),

                                                p.getShift(),
                                                startDate, endDate);

                                DimensionalDefectsDto dimensional = dto.getDimensionalDefects();

                                if (dimensional == null) {

                                        dimensional = new DimensionalDefectsDto();

                                        dimensional.setBoxGauge(0);

                                        dimensional.setFlatBearingArea(0);

                                        dimensional.setFallingGauge(0);
                                }

                                dimensional.setBoxGauge(

                                                dimensional.getBoxGauge()

                                                                + (boxGauge != null ? boxGauge : 0)

                                                                + (finalBox != null ? finalBox : 0));

                                dimensional.setFlatBearingArea(

                                                dimensional.getFlatBearingArea()

                                                                + (quenchFlat != null ? quenchFlat : 0)

                                                                + (finalFlat != null ? finalFlat : 0));

                                dimensional.setFallingGauge(

                                                dimensional.getFallingGauge()

                                                                + (quenchFall != null ? quenchFall : 0)

                                                                + (finalFall != null ? finalFall : 0));

                                dto.setDimensionalDefects(dimensional);
                        }
                }

                System.out.println("last all  date " + createdAtMap);

                // ================= RETURN FINAL RESULT =================

                return new ArrayList<>(resultMap.values());

        }

        @Override
        public DashboardSummaryDto getDashboardSummary() {
                return getDashboardSummary(null, null, null, null);
        }

        public DashboardSummaryDto getDashboardSummary(String vendorPlantCode, String zonalRailway, String startDateStr,
                        String endDateStr) {
                String vCode = vendorPlantCode == null ? "" : vendorPlantCode;
                String zCode = zonalRailway == null ? "" : zonalRailway;

                LocalDate parsedStartDate = (startDateStr == null || startDateStr.trim().isEmpty()) ? LocalDate.of(2000, 1, 1)
                                : LocalDate.parse(startDateStr);
                LocalDate parsedEndDate = (endDateStr == null || endDateStr.trim().isEmpty()) ? LocalDate.now()
                                : LocalDate.parse(endDateStr);

                // ── Run all independent DB queries in PARALLEL to reduce latency ──────────────
                CompletableFuture<Long> cfPoIssued = CompletableFuture.supplyAsync(() ->
                                poHeaderRepository.countFilteredPoByItemCatDescr("Elastic Rail Clips", null, null, vCode, zCode));

                CompletableFuture<Long> cfQtyNos = CompletableFuture.supplyAsync(() ->
                                poItemRepository.sumFilteredQtyByItemCatDescrAndUomNos("Elastic Rail Clips", null, null, vCode, zCode));

                CompletableFuture<Double> cfQtyMt = CompletableFuture.supplyAsync(() ->
                                poItemRepository.sumFilteredQtyByItemCatDescrAndUomMt("Elastic Rail Clips", null, null, vCode, zCode));

                CompletableFuture<Long> cfFinalQtyPassed = CompletableFuture.supplyAsync(() -> {
                        List<Object[]> res = finalCumulativeResultsRepository.sumFinalAcceptedAndRejectedRevisedLogic(parsedStartDate, parsedEndDate, vCode, zCode);
                        if (res != null && !res.isEmpty() && res.get(0) != null) {
                                Object[] row = res.get(0);
                                return row[0] != null ? ((Number) row[0]).longValue() : 0L;
                        }
                        return 0L;
                });

                CompletableFuture<Double> cfAvgProd = CompletableFuture.supplyAsync(() ->
                                getAvgProductionPerDayWithFilters(parsedStartDate, parsedEndDate, vCode, zCode));

                CompletableFuture<Double> cfProcRej = CompletableFuture.supplyAsync(() ->
                                calculateProcessRejectionPercentageRevisedLogicWithFilters(parsedStartDate, parsedEndDate, vCode, zCode));

                CompletableFuture<List<Object[]>> cfFinalRejResults = CompletableFuture.supplyAsync(() ->
                                finalCumulativeResultsRepository.sumFinalRejectionWithFilters(parsedStartDate, parsedEndDate, vCode, zCode));

                CompletableFuture<List<Object[]>> cfRmRejResults = CompletableFuture.supplyAsync(() ->
                                rmHeatFinalResultRepository.sumRmRejectionWithFilters(parsedStartDate, parsedEndDate, vCode, zCode));

                CompletableFuture<Long> cfSleeperPoIssued = CompletableFuture.supplyAsync(() ->
                                poHeaderRepository.countPoByItemCatDescr("PSC Mainline Sleeper"));

                CompletableFuture<Long> cfSleeperQtyNos = CompletableFuture.supplyAsync(() ->
                                getSleeperPoQuantityNos());

                CompletableFuture<Long> cfSleeperQtySet = CompletableFuture.supplyAsync(() ->
                                getSleeperPoQuantitySet());

                CompletableFuture<Long> cfRailPadPoIssued = CompletableFuture.supplyAsync(() ->
                                poHeaderRepository.countPoByItemCatDescr("Rail Pads"));

                CompletableFuture<Long> cfRailPadQtyNos = CompletableFuture.supplyAsync(() ->
                                getRailPadPoQuantityNos());

                CompletableFuture<Long> cfRailPadQtySet = CompletableFuture.supplyAsync(() ->
                                getRailPadPoQuantitySet());

                CompletableFuture<List<Object[]>> cfCallCounts = CompletableFuture.supplyAsync(() ->
                                railWorkflowTransactionRepository.getRailPadInspectionCallCounts());

                CompletableFuture<RailPadFinalInspectionSummaryDto> cfFinalSummary = CompletableFuture.supplyAsync(() ->
                                getRailPadFinalInspectionSummary());

                CompletableFuture<Long> cfTotalRejection = CompletableFuture.supplyAsync(() ->
                                railIEProductionVerificationRepository.sumAllRejectedQty());

                CompletableFuture<Long> cfProductionDeclared = CompletableFuture.supplyAsync(() ->
                                railIEProductionVerificationRepository.sumAllTotalPiecesProduced());

                java.time.LocalDate thirtyDaysAgoDate = java.time.LocalDate.now().minusDays(30);
                CompletableFuture<Long> cfRpPiecesSum = CompletableFuture.supplyAsync(() ->
                                railIEProductionVerificationRepository.sumTotalPiecesProducedLast30Days(thirtyDaysAgoDate));

                CompletableFuture<Long> cfRpPlantCount = CompletableFuture.supplyAsync(() ->
                                railIEProductionVerificationRepository.countDistinctPlantDaysLast30Days(thirtyDaysAgoDate));

                // Wait for all futures to complete
                CompletableFuture.allOf(cfPoIssued, cfQtyNos, cfQtyMt, cfFinalQtyPassed, cfAvgProd,
                                cfProcRej, cfFinalRejResults, cfRmRejResults, cfSleeperPoIssued,
                                cfSleeperQtyNos, cfSleeperQtySet, cfRailPadPoIssued, cfRailPadQtyNos,
                                cfRailPadQtySet, cfCallCounts, cfFinalSummary, cfTotalRejection,
                                cfProductionDeclared, cfRpPiecesSum, cfRpPlantCount).join();

                // ── Collect results ───────────────────────────────────────────────────────────
                long poIssued = cfPoIssued.join();
                Long qtyNos = cfQtyNos.join();
                Double qtyMt = cfQtyMt.join();
                Long finalQtyPassed = cfFinalQtyPassed.join();
                double avgProductionPerDayWithFilters = cfAvgProd.join();
                double processRejectionPctValue = cfProcRej.join();

                List<Object[]> finalRejResults = cfFinalRejResults.join();
                double finalRejectionPctValue = 0.0;
                if (finalRejResults != null && !finalRejResults.isEmpty() && finalRejResults.get(0) != null) {
                        Object[] row = finalRejResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0) {
                                finalRejectionPctValue = (rejected * 100.0) / offered;
                        }
                }

                List<Object[]> rmRejResults = cfRmRejResults.join();
                double rmRejectionPctValue = 0.0;
                if (rmRejResults != null && !rmRejResults.isEmpty() && rmRejResults.get(0) != null) {
                        Object[] row = rmRejResults.get(0);
                        double rejected = row[0] != null ? ((Number) row[0]).doubleValue() : 0.0;
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        if (offered > 0) {
                                rmRejectionPctValue = (rejected * 100.0) / offered;
                        }
                }

                List<Object[]> callCounts = cfCallCounts.join();
                long underInspectionCount = 0L;
                long pendingCount = 0L;
                if (callCounts != null && !callCounts.isEmpty() && callCounts.get(0) != null) {
                        Object[] row = callCounts.get(0);
                        underInspectionCount = row[0] != null ? ((Number) row[0]).longValue() : 0L;
                        pendingCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                }

                RailPadFinalInspectionSummaryDto finalSummary = cfFinalSummary.join();
                long totalRejection = cfTotalRejection.join();
                long finalRejection = finalSummary.getRejectedQtyNos() + finalSummary.getRejectedQtySet();
                long productionDeclared = cfProductionDeclared.join();
                double railPadRejPercentage = 0.0;
                if (productionDeclared > 0) {
                        railPadRejPercentage = (double) (totalRejection + finalRejection) * 100.0 / (double) productionDeclared;
                }

                Long rpPiecesSum = cfRpPiecesSum.join();
                Long rpPlantCount = cfRpPlantCount.join();
                double railPadAvg = 0.0;
                if (rpPiecesSum != null && rpPiecesSum > 0 && rpPlantCount != null && rpPlantCount > 0) {
                        railPadAvg = rpPiecesSum / (double) rpPlantCount;
                }

                // ── Build DTO ─────────────────────────────────────────────────────────────────
                DashboardSummaryDto dto = new DashboardSummaryDto();
                dto.setPoIssued(poIssued);
                dto.setPoQuantityNos(qtyNos != null ? qtyNos : 0L);
                dto.setPoQuantityMt(qtyMt != null ? qtyMt : 0.0);
                dto.setFinalInspectionQuantity(finalQtyPassed != null ? finalQtyPassed : 0L);
                dto.setAvgProductionPerDay(avgProductionPerDayWithFilters);
                dto.setProcessRejectionPercentage(processRejectionPctValue);
                dto.setFinalRejectionPercentage(finalRejectionPctValue);
                dto.setRmRejectionPercentage(rmRejectionPctValue);
                dto.setSleeperPoIssued(cfSleeperPoIssued.join());
                dto.setSleeperPoQuantityNos(cfSleeperQtyNos.join() != null ? cfSleeperQtyNos.join() : 0L);
                dto.setSleeperPoQuantitySet(cfSleeperQtySet.join() != null ? cfSleeperQtySet.join() : 0L);
                dto.setRailPadPoIssued(cfRailPadPoIssued.join());
                dto.setRailPadPoQuantityNos(cfRailPadQtyNos.join() != null ? cfRailPadQtyNos.join() : 0L);
                dto.setRailPadPoQuantitySet(cfRailPadQtySet.join() != null ? cfRailPadQtySet.join() : 0L);
                dto.setUnderInspectionCalls(underInspectionCount);
                dto.setPendingCalls(pendingCount);
                dto.setRejectedInProcess(totalRejection);
                dto.setRejectedInFinal(finalRejection);
                dto.setRailPadRejectionPercentage(Math.round(railPadRejPercentage * 100.0) / 100.0);
                dto.setRailPadAvgProductionPerDay(Math.round(railPadAvg * 100.0) / 100.0);
                dto.setTotalAcceptedNos(finalSummary.getAcceptedQtyNos());
                dto.setTotalAcceptedSet(finalSummary.getAcceptedQtySet());

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
        public double getAvgProductionPerDayWithFilters(java.time.LocalDate startDate, java.time.LocalDate endDate,
                        String vendorPlantCode, String zonalRailway) {
                return calculateAvgProductionPerDayNewLogicWithFilters(startDate, endDate, vendorPlantCode,
                                zonalRailway);
        }

        @Override
        public List<StageRejectionDto> getStageWiseRejection(String startDate, String endDate) {
                return getStageWiseRejection();
        }

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
        public List<StageRejectionDto> getManufacturerRejection(String startDate, String endDate) {
                return getManufacturerRejection();
        }

        public List<StageRejectionDto> getManufacturerRejection() {

                List<StageRejectionDto> data = new ArrayList<>();

                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

                // Updated logic: Join process_line_final_result with inventory_entries via
                // heat_number

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
        public ProcessPerformanceResponseDto getProcessPerformance(String startDate, String endDate) {
                return getProcessPerformance();
        }

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

        public List<StageRejectionDto> getManufacturingStepWiseRejection(String startDate, String endDate) {

                List<StageRejectionDto> breakdown = new ArrayList<>();

              //  LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
                LocalDateTime fromDate = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime toDate = LocalDate.parse(endDate).atTime(23, 59, 59);
               // List<Object[]> results = processLineFinalResultRepository.sumStepWiseRejectionLast30Days(last30Days);
                List<Object[]> results =
                        processLineFinalResultRepository.sumStepWiseRejection(fromDate, toDate);
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
        public List<InspectionCallStatusDto> getInspectionCallStatus(String vendorPlantCode, String zonalRailway,
                        String startDate, String endDate) {

                // Updated to exclude Dummy PO data as requested

                return getInspectionCallStatusWithExclLogic(vendorPlantCode, zonalRailway, startDate, endDate);

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

        // Logic: (Sum of total tempering produced in the last 30 days / active
        // production days in the last 30 days)

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

        private double calculateAvgProductionPerDayNewLogicWithFilters(java.time.LocalDate startDate,
                        java.time.LocalDate endDate, String vendorPlantCode, String zonalRailway) {
                Long temperingSum = processLineFinalResultRepository.sumTemperingManufacturedWithFilters(startDate,
                                endDate, vendorPlantCode, zonalRailway);
                if (temperingSum == null || temperingSum == 0) {
                        return 0.0;
                }
                Long activeDays = processLineFinalResultRepository.countDistinctProductionDaysWithFilters(startDate,
                                endDate, vendorPlantCode, zonalRailway);
                if (activeDays == null || activeDays == 0) {
                        return 0.0;
                }
                return temperingSum / (double) activeDays;
        }

        // ===== NEW: Pareto Analysis – Top 10 Rejection Parameters (all process stages)

        // =====

        private volatile java.util.List<StageRejectionDto> paretoAnalysisCache = null;
        private volatile long paretoCacheLastUpdated = 0;

        @Override
        public List<StageRejectionDto> getParetoAnalysis(String startDate, String endDate) {
                if (paretoAnalysisCache == null || System.currentTimeMillis() - paretoCacheLastUpdated > 300000) {
                        synchronized (this) {
                                if (paretoAnalysisCache == null || System.currentTimeMillis() - paretoCacheLastUpdated > 300000) {



                                        LocalDateTime lStart = LocalDate.parse(startDate).atStartOfDay();

                                        LocalDateTime lEnd = LocalDate.parse(endDate).atTime(23, 59, 59);

                                        List<Object[]> rows =
                                                processLineFinalResultRepository
                                                        .getParetoAnalysisRejections(lStart, lEnd);

                                      //  List<Object[]> rows = processLineFinalResultRepository.getParetoAnalysisRejections(startDate,endDate);
                                        List<StageRejectionDto> result = new ArrayList<>();
                                        if (rows != null && !rows.isEmpty()) {
                                                String[] palette = { "#2563eb", "#f59e0b", "#ef4444", "#10b981", "#8b5cf6",
                                                                "#06b6d4", "#f97316", "#ec4899", "#84cc16", "#14b8a6" };
                                               // long grandTotal = rows.stream()
                                                //                .mapToLong(r -> r[1] != null ? ((Number) r[1]).longValue() : 0)
                                                     //           .sum();
                                                // Fetch grand total of ALL defects (not just Top 10)
                                                Long grandTotalObj = processLineFinalResultRepository
                                                        .getTotalDefects(lStart, lEnd);

                                                long grandTotal = grandTotalObj != null ? grandTotalObj : 0;
                                                long runningTotal = 0;
                                                for (int i = 0; i < rows.size(); i++) {
                                                        Object[] row = rows.get(i);
                                                        String name = (String) row[0];
                                                        long count = row[1] != null ? ((Number) row[1]).longValue() : 0;

                                                        double percentage = grandTotal > 0
                                                                ? (count * 100.0 / grandTotal)
                                                                : 0;

                                                        runningTotal += count;
                                                        double cumulative = grandTotal > 0 ? (runningTotal * 100.0 / grandTotal) : 0;
                                                        StageRejectionDto dto = new StageRejectionDto(name, (double) count,
                                                                        palette[i % palette.length]);
                                                        dto.setCumulative(Math.round(cumulative * 10.0) / 10.0);
                                                        dto.setPercentage(
                                                                Math.round(percentage * 10.0) / 10.0);
                                                        result.add(dto);
                                                }
                                        }
                                        paretoAnalysisCache = result;
                                        paretoCacheLastUpdated = System.currentTimeMillis();
                                }
                        }
                }
                return paretoAnalysisCache;
        }



        @Override

        public List<StageRejectionDto> getParetoAnalysis(String startDate, String endDate, String product) {

                if (product != null
                                && (product.equalsIgnoreCase("Rail Pad") || product.equalsIgnoreCase("Rail Pads"))) {

                        List<StageRejectionDto> result = new ArrayList<>();

                        try {

                                java.time.LocalDateTime lStart = null;

                                java.time.LocalDateTime lEnd = null;

                                if (startDate != null && !startDate.isEmpty() && endDate != null
                                                && !endDate.isEmpty()) {

                                        lStart = java.time.LocalDate.parse(startDate).atStartOfDay();

                                        lEnd = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);

                                } else {

                                        lEnd = java.time.LocalDateTime.now();

                                        lStart = lEnd.minusMonths(6).with(java.time.LocalTime.MIN);

                                }

                                List<Object[]> rows = railIEProductionVerificationRepository
                                                .findRailPadParetoAnalysisRejections(lStart, lEnd);

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
                                int colorIndex = 0;
                                for (int i = 0; i < rows.size(); i++) {
                                        Object[] row = rows.get(i);
                                        String name = row[0] != null ? row[0].toString() : "Unknown";
                                        long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
                                        if (count <= 0) {
                                                continue;
                                        }
                                        runningTotal += count;

                                        double cumulative = grandTotal > 0 ? (runningTotal * 100.0 / grandTotal) : 0;

                                        StageRejectionDto dto = new StageRejectionDto(name, (double) count,
                                                        palette[colorIndex % palette.length]);
                                        colorIndex++;
                                        dto.setCumulative(Math.round(cumulative * 10.0) / 10.0); // 1 decimal
                                        result.add(dto);
                                }

                        } catch (Exception e) {

                                e.printStackTrace();

                        }

                        return result;

                } else {

                        return getParetoAnalysis(startDate,endDate);

                }

        }

        // ===== NEW: Inspection Details – Accepted vs. Rejected (RM, Process, Final)

        // =====

        @Override

        public List<InspectionDetailsDto> getInspectionDetails() {

                return getInspectionDetails(null, null, null, null);

        }

        /**
         * 
         * Updated logic for Inspection Calls Status to exclude data related to
         * DummyPo_001.
         * 
         * This considers the requestId in workflow_transition that matches ic_number in
         * inspection_calls.
         * 
         */

        private List<InspectionCallStatusDto> getInspectionCallStatusWithExclLogic(String vendorPlantCode,
                        String zonalRailway, String startDate, String endDate) {

                String excludePo = "DummyPo_001";

                List<Object[]> results = workflowTransitionRepository.getInspectionCallStatusBreakdownExcludingDummyPo(
                                excludePo,
                                vendorPlantCode == null ? "" : vendorPlantCode,
                                zonalRailway == null ? "" : zonalRailway,
                                (startDate == null || startDate.isEmpty()) ? null : startDate,
                                (endDate == null || endDate.isEmpty()) ? null : endDate + " 23:59:59");

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

        private double calculateProcessRejectionPercentageRevisedLogicWithFilters(LocalDate startDate,
                        LocalDate endDate, String vendorPlantCode, String zonalRailway) {
                List<Object[]> results = processLineFinalResultRepository
                                .sumProcessRejectionRevisedLogicWithFilters(startDate, endDate, vendorPlantCode,
                                                zonalRailway);
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

        public List<InspectionDetailsDto> getInspectionDetails(String startDateStr, String endDateStr,
                        String vendorPlantCode, String zonalRailway) {
                LocalDate startDate = (startDateStr != null && !startDateStr.trim().isEmpty()) ? LocalDate.parse(startDateStr) : LocalDate.of(2000, 1, 1);

                LocalDate endDate = (endDateStr != null && !endDateStr.trim().isEmpty()) ? LocalDate.parse(endDateStr) : LocalDate.now();

                String vendor = vendorPlantCode == null ? "" : vendorPlantCode;

                String zone = zonalRailway == null ? "" : zonalRailway;

                // Run RM, Process, Final queries in PARALLEL
                CompletableFuture<List<Object[]>> cfRm = CompletableFuture.supplyAsync(() ->
                                rmHeatFinalResultRepository.sumRmAcceptedAndRejectedRevisedLogic(startDate, endDate, vendor, zone));

                CompletableFuture<List<Object[]>> cfProc = CompletableFuture.supplyAsync(() ->
                                processLineFinalResultRepository.sumProcessAcceptedAndRejectedRevisedLogic(startDate, endDate, vendor, zone));

                CompletableFuture<List<Object[]>> cfFinal = CompletableFuture.supplyAsync(() ->
                                finalCumulativeResultsRepository.sumFinalAcceptedAndRejectedRevisedLogic(startDate, endDate, vendor, zone));

                CompletableFuture.allOf(cfRm, cfProc, cfFinal).join();

                List<Object[]> rmData = cfRm.join();
                double rmAcc = 0, rmRej = 0;
                if (rmData != null && !rmData.isEmpty()) {
                        Object[] row = rmData.get(0);
                        rmAcc = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                        rmRej = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                }

                List<Object[]> procData = cfProc.join();
                double procAcc = 0, procRej = 0;
                if (procData != null && !procData.isEmpty()) {
                        Object[] row = procData.get(0);
                        procAcc = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                        procRej = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                }

                List<Object[]> finalData = cfFinal.join();
                double finalAcc = 0, finalRej = 0;
                if (finalData != null && !finalData.isEmpty()) {
                        Object[] row = finalData.get(0);
                        finalAcc = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                        finalRej = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                }

                rmAcc = Math.round(rmAcc);
                rmRej = Math.round(rmRej);
                procAcc = Math.round(procAcc);
                procRej = Math.round(procRej);
                finalAcc = Math.round(finalAcc);
                finalRej = Math.round(finalRej);

                double totalAcc = rmAcc + procAcc + finalAcc;
                double totalRej = rmRej + procRej + finalRej;

                List<InspectionDetailsDto> result = new ArrayList<>();
                result.add(new InspectionDetailsDto("Total", (long) totalAcc, (long) totalRej));
                result.add(new InspectionDetailsDto("RM", (long) rmAcc, (long) rmRej));
                result.add(new InspectionDetailsDto("Process", (long) procAcc, (long) procRej));
                result.add(new InspectionDetailsDto("Final", (long) finalAcc, (long) finalRej));

                return result;

        }


        @Override
        public InspectionDetailsDto getProcessOverallRejectionAllTime() {
                List<Object[]> procData = processLineFinalResultRepository.sumProcessAcceptedAndRejectedAllTime();
                double procAcc = 0, procRej = 0;
                if (procData != null && !procData.isEmpty()) {
                        Object[] row = procData.get(0);
                        procAcc = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
                        procRej = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                }
                return new InspectionDetailsDto("Process", (long) Math.round(procAcc), (long) Math.round(procRej));
        }

        @Override

        public List<StageRejectionDto> getMonthlyRejectionTrend(String startDate, String endDate) {

                return getMonthlyRejectionTrend(startDate, endDate, "ERC");

        }

        @Override

        public List<StageRejectionDto> getMonthlyRejectionTrend(String startDate, String endDate, String product) {

                List<StageRejectionDto> trend = new ArrayList<>();

                try {

                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");

                        java.time.LocalDateTime lStart = null;

                        java.time.LocalDateTime lEnd = null;

                        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {

                                lStart = java.time.LocalDate.parse(startDate).atStartOfDay();

                                lEnd = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);

                        } else {

                                lEnd = java.time.LocalDateTime.now();

                                lStart = lEnd.minusMonths(6).with(java.time.LocalTime.MIN);

                        }

                        if (product != null && (product.equalsIgnoreCase("Rail Pad")
                                        || product.equalsIgnoreCase("Rail Pads"))) {

                                List<Object[]> results = railIEProductionVerificationRepository
                                                .findMonthlyRejectionTrend(lStart, lEnd);

                                List<Object[]> finalRejections = railFinalInspectionLotResultsRepository
                                                .findMonthlyFinalRejections(lStart, lEnd);

                                Map<String, Long> finalRejMap = new HashMap<>();
                                if (finalRejections != null) {
                                        for (Object[] row : finalRejections) {
                                                String my = row[0] != null ? row[0].toString().trim().toUpperCase()
                                                                : "";
                                                long val = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                                                finalRejMap.put(my, val);
                                        }
                                }

                                if (results != null) {
                                        for (Object[] row : results) {
                                                String label = row[0] != null ? row[0].toString() : "Unknown";
                                                double percentage = row[3] != null ? ((Number) row[3]).doubleValue()
                                                                : 0.0;
                                                long totalProduced = row[2] != null ? ((Number) row[2]).longValue()
                                                                : 0L;

                                                String key = label.trim().toUpperCase();
                                                long finalRejQty = finalRejMap.getOrDefault(key, 0L);

                                                double finalPct = 0.0;
                                                if (totalProduced > 0) {
                                                        finalPct = (double) finalRejQty * 100.0
                                                                        / (double) totalProduced;
                                                        finalPct = Math.round(finalPct * 100.0) / 100.0;
                                                }

                                                StageRejectionDto dto = new StageRejectionDto(label, percentage,
                                                                "#10b981");
                                                dto.setFinalValue(finalPct);
                                                trend.add(dto);
                                        }
                                }

                        } else {

                                List<Object[]> results = processLineFinalResultRepository

                                                .findMonthlyRejectionTrend(lStart, lEnd);

                                if (results != null) {

                                        for (Object[] row : results) {

                                                String label = row[0] != null ? row[0].toString() : "Unknown";

                                                double percentage = row[3] != null ? ((Number) row[3]).doubleValue()
                                                                : 0.0;

                                                trend.add(new StageRejectionDto(label, percentage, "#8b5cf6"));

                                        }

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
         * 
         * Returns total PO count filtered by 'Elastic Rail Clips' category.
         * 
         */

        private long getFilteredPoIssuedCount() {

                return poHeaderRepository.countPoByItemCatDescr("Elastic Rail Clips");

        }

        /**
         * 
         * Returns total sum of quantity (Nos.) filtered by 'Elastic Rail Clips'
         * category.
         * 
         */

        private Long getFilteredPoQuantityNos() {

                return poItemRepository.sumQtyByItemCatDescrAndUomNos("Elastic Rail Clips");

        }

        /**
         * 
         * Returns total sum of quantity (MT) filtered by 'Elastic Rail Clips' category.
         * 
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
        public List<PoIssuedDetailDto> getPoIssuedDetails(String itemCatDescr, String vendorPlantCode,
                        String zonalRailway, String startDateStr, String endDateStr) {
                String vCode = vendorPlantCode == null ? "" : vendorPlantCode;
                String zCode = zonalRailway == null ? "" : zonalRailway;
                String startDStr = startDateStr == null ? "" : startDateStr;
                String endDStr = endDateStr == null ? "" : endDateStr;

                java.time.LocalDateTime startDate = startDStr.isEmpty() ? java.time.LocalDateTime.of(1970, 1, 1, 0, 0)
                                : java.time.LocalDate.parse(startDStr).atStartOfDay();
                java.time.LocalDateTime endDate = endDStr.isEmpty() ? java.time.LocalDateTime.of(2100, 12, 31, 23, 59)
                                : java.time.LocalDate.parse(endDStr).atTime(23, 59, 59);

                List<PoIssuedDetailDto> list = poItemRepository.getPoIssuedDetails(itemCatDescr, vCode, zCode,
                                startDate, endDate);
                if (list != null) {
                        for (PoIssuedDetailDto dto : list) {
                                long poQty = dto.getPoQuantity() != null ? dto.getPoQuantity() : 0L;
                                long acceptedQty = dto.getAcceptedQtyAfterFinalInspection() != null
                                                ? dto.getAcceptedQtyAfterFinalInspection()
                                                : 0L;
                                dto.setBalanceQuantity(poQty - acceptedQty);
                        }
                }
                return list;

        }

        @Override
        public PageResponseDTO<PoWiseInspectionTrackingDTO> getPoInspectionTracking(
                        int page,
                        int size,
                        LocalDate startDate,
                        LocalDate endDate) {

                Pageable pageable = PageRequest.of(page, size);

                Page<Object[]> poPage = inspectionCallRepository.fetchPoInspectionTracking(
                                startDate,
                                endDate,
                                pageable);

                AtomicInteger counter = new AtomicInteger(page * size + 1);

                List<PoWiseInspectionTrackingDTO> content = poPage.getContent()
                                .stream()
                                .map(row -> {

                                        PoWiseInspectionTrackingDTO dto = new PoWiseInspectionTrackingDTO();

                                        dto.setSno(counter.getAndIncrement());

                                        dto.setZonalRailway((String) row[0]);
                                        dto.setVendor((String) row[1]);
                                        dto.setErcType((String) row[2]);
                                        dto.setPoNumber((String) row[3]);

                                        if (row[4] instanceof java.sql.Timestamp ts) {
                                                dto.setPoDate(ts.toLocalDateTime().toLocalDate());
                                        } else if (row[4] instanceof java.sql.Date dt) {
                                                dto.setPoDate(dt.toLocalDate());
                                        }

                                        dto.setSpecification((String) row[5]);

                                        dto.setPoQty(
                                                        row[6] == null ? 0D : ((Number) row[6]).doubleValue());

                                        dto.setProcessInspectedQty(
                                                        row[7] == null ? 0D : ((Number) row[7]).doubleValue());

                                        dto.setProcessAcceptedQty(
                                                        row[8] == null ? 0D : ((Number) row[8]).doubleValue());

                                        dto.setOfferedForFinalInspectionQty(
                                                        row[9] == null ? 0D : ((Number) row[9]).doubleValue());

                                        dto.setFinalAcceptedQty(
                                                        row[10] == null ? 0D : ((Number) row[10]).doubleValue());

                                        dto.setNoOfIcIssued(
                                                        row[11] == null ? 0L : ((Number) row[11]).longValue());

                                        if (row[12] != null) {

                                                if (row[12] instanceof java.sql.Date dt) {
                                                        dto.setLastIcIssuedDate(dt.toLocalDate());
                                                } else if (row[12] instanceof java.sql.Timestamp ts) {
                                                        dto.setLastIcIssuedDate(
                                                                        ts.toLocalDateTime().toLocalDate());
                                                }
                                        }

                                        /* Raw Material Defects */

                                        dto.setChemicalCompositionRej(
                                                        row[13] == null ? 0L : ((Number) row[13]).longValue());

                                        dto.setDiameterBarRej(
                                                        row[14] == null ? 0L : ((Number) row[14]).longValue());

                                        dto.setGrainSizeRej(
                                                        row[15] == null ? 0L : ((Number) row[15]).longValue());

                                        dto.setInclusionRatingRej(
                                                        row[16] == null ? 0L : ((Number) row[16]).longValue());

                                        dto.setDepthOfDecarbRej(
                                                        row[17] == null ? 0L : ((Number) row[17]).longValue());

                                        dto.setHardnessRawRej(
                                                        row[18] == null ? 0L : ((Number) row[18]).longValue());

                                        dto.setShearingRej(
                                                        row[19] == null ? 0L : ((Number) row[19]).longValue());

                                        dto.setMpiRej(
                                                        row[20] == null ? 0L : ((Number) row[20]).longValue());

                                        dto.setTurningRej(
                                                        row[21] == null ? 0L : ((Number) row[21]).longValue());

                                        dto.setForgingRej(
                                                        row[22] == null ? 0L : ((Number) row[22]).longValue());

                                        dto.setQuenchingRej(
                                                        row[23] == null ? 0L : ((Number) row[23]).longValue());

                                        dto.setTemperingRej(
                                                        row[24] == null ? 0L : ((Number) row[24]).longValue());

                                        dto.setDimensionFinishedErcRej(
                                                        row[25] == null ? 0L : ((Number) row[25]).longValue());

                                        dto.setHardnessProcessRej(
                                                        row[26] == null ? 0L : ((Number) row[26]).longValue());

                                        dto.setDepthOfDecarburizationRej(
                                                        row[26] == null ? 0L : ((Number) row[26]).longValue());

                                        dto.setDimensionToleranceRej(
                                                        row[27] == null ? 0L : ((Number) row[27]).longValue());

                                        dto.setApplicationAndDeflectionTestRej(
                                                        row[28] == null ? 0L : ((Number) row[28]).longValue());

                                        dto.setToeLoadTestRej(
                                                        row[29] == null ? 0L : ((Number) row[29]).longValue());

                                        dto.setWeightRej(
                                                        row[30] == null ? 0L : ((Number) row[30]).longValue());

                                        dto.setVisualTestRej(
                                                        row[31] == null ? 0L : ((Number) row[31]).longValue());

                                        dto.setMicroStructureRej(
                                                        row[32] == null ? 0L : ((Number) row[32]).longValue());

                                        dto.setFreedomFromDefectsRej(
                                                        row[33] == null ? 0L : ((Number) row[33]).longValue());

                                        dto.setOtherRejections(
                                                        row[34] == null ? 0L : ((Number) row[34]).longValue());

                                        dto.setRemarks(
                                                        row[35] == null ? null : row[35].toString());

                                        Long totalRejections =

                                                        dto.getChemicalCompositionRej()
                                                                        + dto.getDiameterBarRej()
                                                                        + dto.getGrainSizeRej()
                                                                        + dto.getInclusionRatingRej()
                                                                        + dto.getDepthOfDecarbRej()
                                                                        + dto.getHardnessRawRej()

                                                                        + dto.getShearingRej()
                                                                        + dto.getMpiRej()
                                                                        + dto.getTurningRej()
                                                                        + dto.getForgingRej()
                                                                        + dto.getQuenchingRej()
                                                                        + dto.getTemperingRej()
                                                                        + dto.getDimensionFinishedErcRej()
                                                                        + dto.getHardnessProcessRej()

                                                                        + dto.getDepthOfDecarburizationRej()
                                                                        + dto.getDimensionToleranceRej()
                                                                        + dto.getApplicationAndDeflectionTestRej()
                                                                        + dto.getToeLoadTestRej()
                                                                        + dto.getWeightRej()
                                                                        + dto.getVisualTestRej()
                                                                        + dto.getMicroStructureRej()
                                                                        + dto.getFreedomFromDefectsRej()
                                                                        + dto.getOtherRejections();

                                        dto.setTotalRejections(totalRejections);

                                        Double processInspectedQty = dto.getProcessInspectedQty() == null
                                                        ? 0D
                                                        : dto.getProcessInspectedQty();

                                        double rejectionPercentage = 0D;

                                        if (processInspectedQty > 0) {
                                                rejectionPercentage = (totalRejections * 100.0) / processInspectedQty;
                                        }

                                        dto.setRejectionPercentage(
                                                        Math.round(rejectionPercentage * 100.0) / 100.0);
                                        return dto;
                                })
                                .toList();

                PageResponseDTO<PoWiseInspectionTrackingDTO> response = new PageResponseDTO<>();

                response.setContent(content);
                response.setPage(page);
                response.setSize(size);
                response.setTotalElements(poPage.getTotalElements());
                response.setTotalPages(poPage.getTotalPages());

                return response;
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

                                                0, 0,

                                                0, 0,

                                                0, 0,

                                                0, 0,

                                                0, 0,

                                                0, 0

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
                        BigDecimal totalRmRejection = rmVmDefect
                                        .add(rmDimentionalDefect)
                                        .add(rmInclusionDefect)
                                        .add(rmGrainSizeDefect)
                                        .add(rmDecarbDefect);

                        BigDecimal totalProcessRejection = BigDecimal.valueOf(
                                        processQty.getShearingRejectionQty()
                                                        + processQty.getTurningRejectionQty()
                                                        + processQty.getMpiRejectionQty()
                                                        + processQty.getForgingRejectionQty()
                                                        + processQty.getQuenchingRejectionQty()
                                                        + processQty.getTemperingRejectionQty());

                        BigDecimal totalFinalRejection = finalVisualDimDefect
                                        .add(finalHardnessDefect)
                                        .add(finalInclusionDefect)
                                        .add(finalDeflectionDefect)
                                        .add(finalToeLoadDefect);

                        BigDecimal totalRejection = totalRmRejection
                                        .add(totalProcessRejection)
                                        .add(totalFinalRejection);

                        BigDecimal agePercentage = BigDecimal.ZERO;

                        if (inspectedQty.compareTo(BigDecimal.ZERO) > 0) {
                                agePercentage = totalRejection
                                                .multiply(BigDecimal.valueOf(100))
                                                .divide(inspectedQty, 2, RoundingMode.HALF_UP);
                        }

                        dto.setAgePercentage(agePercentage);

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

        /*
         * 
         * @Override
         * 
         * public List<com.sarthi.dto.reports.InspectionCallDetailDto>
         * getInspectionCallStatusDetails(String stage, String status) {
         * 
         * List<Object[]> rawList =
         * workflowTransitionRepository.getInspectionCallStatusDetailsRaw(stage,
         * status);
         * 
         * List<com.sarthi.dto.reports.InspectionCallDetailDto> dtoList = new
         * java.util.ArrayList<>();
         * 
         * if (rawList != null) {
         * 
         * for (Object[] row : rawList) {
         * 
         * dtoList.add(com.sarthi.dto.reports.InspectionCallDetailDto.builder()
         * 
         * .inspectionCallNumber(row[0] != null ? row[0].toString() : "")
         * 
         * .vendor(row[1] != null ? row[1].toString() : "")
         * 
         * .callSubmissionDateTime(row[2] != null ? row[2].toString() : "")
         * 
         * .stageOfInspection(row[3] != null ? row[3].toString() : "")
         * 
         * .poSrNo(row[4] != null ? row[4].toString() : "")
         * 
         * .dpDate(row[5] != null ? row[5].toString() : "")
         * 
         * .status(row[6] != null ? row[6].toString() : "")
         * 
         * .build());
         * 
         * }
         * 
         * }
         * 
         * return dtoList;
         * 
         * }
         */
        public List<com.sarthi.dto.reports.InspectionCallDetailDto> getInspectionCallStatusDetails(
                        String stage,
                        String status,
                        String vendorPlantCode,
                        String zonalRailway,
                        String startDate,
                        String endDate) {

                List<Object[]> rawList = workflowTransitionRepository.getInspectionCallStatusDetailsRaw(
                                stage,
                                status,
                                vendorPlantCode == null ? "" : vendorPlantCode,
                                zonalRailway == null ? "" : zonalRailway,
                                (startDate == null || startDate.isEmpty()) ? null : startDate,
                                (endDate == null || endDate.isEmpty()) ? null : endDate + " 23:59:59");

                List<InspectionCallDetailDto> dtoList = new ArrayList<>();

                if (rawList != null) {

                        for (Object[] row : rawList) {

                                dtoList.add(
                                                InspectionCallDetailDto.builder()
                                                                .inspectionCallNumber(
                                                                                row[0] != null ? row[0].toString() : "")
                                                                .vendor(
                                                                                row[1] != null ? row[1].toString() : "")
                                                                .callSubmissionDateTime(
                                                                                row[2] != null ? row[2].toString() : "")
                                                                .stageOfInspection(
                                                                                row[3] != null ? row[3].toString() : "")
                                                                .poSrNo(
                                                                                row[4] != null ? row[4].toString() : "")
                                                                .dpDate(
                                                                                row[5] != null ? row[5].toString() : "")
                                                                .mainStatus(
                                                                                row[6] != null ? row[6].toString() : "")
                                                                .subStatus(
                                                                                row[7] != null ? row[7].toString() : "")
                                                                .build());
                        }
                }

                return dtoList;
        }

        @Override

        public List<com.sarthi.dto.reports.InspectionCallDetailDto> getRailPadInspectionCallStatusDetails(
                        String status) {

                List<Object[]> rawList = railWorkflowTransactionRepository
                                .getRailPadInspectionCallStatusDetailsRaw(status);

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

        @Override

        public List<com.sarthi.dto.reports.SqcReportDto> getSqcReport() {

                // ERC MK-V specification limits for Turning Diameter

                final double USL = 20.84;

                final double LSL = 20.47;

                // Step 1: Get all company + unit + ic_number rows

                List<Object[]> rawRows = inspectionCallRepository.findCompanyUnitIcNumbers();

                // Step 2: Group ic_numbers by (companyName, unitAddress)

                // We'll use LinkedHashMap to maintain insertion order

                java.util.Map<String, java.util.Map<String, java.util.List<String>>> grouped = new java.util.LinkedHashMap<>();

                for (Object[] row : rawRows) {

                        String companyName = row[0] != null ? row[0].toString() : "";

                        String unitAddress = row[1] != null ? row[1].toString() : "";

                        String icNumber = row[2] != null ? row[2].toString() : "";

                        if (companyName.isBlank() || unitAddress.isBlank() || icNumber.isBlank())
                                continue;

                        grouped

                                        .computeIfAbsent(companyName, k -> new java.util.LinkedHashMap<>())

                                        .computeIfAbsent(unitAddress, k -> new java.util.ArrayList<>())

                                        .add(icNumber);

                }

                List<com.sarthi.dto.reports.SqcReportDto> result = new java.util.ArrayList<>();

                int slNo = 1;

                // Step 3: For each company unit, collect latest 30 dia values across all
                // ic_numbers

                for (java.util.Map.Entry<String, java.util.Map<String, java.util.List<String>>> companyEntry : grouped
                                .entrySet()) {

                        String companyName = companyEntry.getKey();

                        for (java.util.Map.Entry<String, java.util.List<String>> unitEntry : companyEntry.getValue()
                                        .entrySet()) {

                                String unitAddress = unitEntry.getKey();

                                List<String> icNumbers = unitEntry.getValue();

                                // Collect dia values (all records from the latest 30 days of the last record
                                // entered for this company unit)

                                List<Double> diaValues = new java.util.ArrayList<>();

                                List<Object[]> rows = processTurningDataRepository
                                                .findDiaByCompanyAndUnitForLatest30Days(companyName, unitAddress);

                                for (Object[] dRow : rows) {

                                        // dia_1

                                        if (dRow[0] != null) {

                                                double v = ((Number) dRow[0]).doubleValue();

                                                if (v > 0)
                                                        diaValues.add(v);

                                        }

                                        // dia_2

                                        if (dRow[1] != null) {

                                                double v = ((Number) dRow[1]).doubleValue();

                                                if (v > 0)
                                                        diaValues.add(v);

                                        }

                                        // dia_3

                                        if (dRow[2] != null) {

                                                double v = ((Number) dRow[2]).doubleValue();

                                                if (v > 0)
                                                        diaValues.add(v);

                                        }

                                }

                                double cp = 0.0;

                                double cpk = 0.0;

                                double sqcRating = 0.0;

                                double ucl = 0.0;

                                double lcl = 0.0;

                                int n = diaValues.size();

                                if (n >= 2) {

                                        double mean = diaValues.stream().mapToDouble(Double::doubleValue).sum() / n;

                                        // Compute moving ranges (absolute differences between successive measurements)

                                        java.util.List<Double> movingRanges = new java.util.ArrayList<>();

                                        for (int i = 1; i < diaValues.size(); i++) {

                                                movingRanges.add(Math.abs(diaValues.get(i) - diaValues.get(i - 1)));

                                        }

                                        double avgMovingRange = movingRanges.stream().mapToDouble(Double::doubleValue)
                                                        .sum() / movingRanges.size();

                                        // Estimate standard deviation using d2 constant for n=2 (1.128)

                                        double stdDev = avgMovingRange / 1.128;

                                        if (stdDev != 0) {

                                                cp = (USL - LSL) / (6.0 * stdDev);

                                                double cpu = (USL - mean) / (3.0 * stdDev);

                                                double cpl = (mean - LSL) / (3.0 * stdDev);

                                                cpk = Math.min(cpu, cpl);

                                                sqcRating = (0.5 * cpk) + (0.5 * cp);

                                                // UCL = mean + 3*stdDev, LCL = mean - 3*stdDev (Excel: E2 +/- 3*G2)

                                                ucl = Math.round((mean + 3.0 * stdDev) * 100000.0) / 100000.0;

                                                lcl = Math.round((mean - 3.0 * stdDev) * 100000.0) / 100000.0;

                                                // Round to 2 decimal places

                                                cp = Math.round(cp * 100.0) / 100.0;

                                                cpk = Math.round(cpk * 100.0) / 100.0;

                                                sqcRating = Math.round(sqcRating * 100.0) / 100.0;

                                        }

                                }

                                result.add(new com.sarthi.dto.reports.SqcReportDto(

                                                slNo++, companyName, unitAddress, cp, cpk, sqcRating, n,
                                                new java.util.ArrayList<>(diaValues), ucl, lcl));

                        }

                }

                return result;

        }

        /**
         * 
         * Returns total sum of quantity (Nos.) filtered by 'Rail Pads' category.
         * 
         */

        private Long getRailPadPoQuantityNos() {

                return poItemRepository.sumQtyByItemCatDescrAndUomNos("Rail Pads");

        }

        /**
         * 
         * Returns total sum of quantity (Set) filtered by 'Rail Pads' category.
         * 
         */

        private Long getRailPadPoQuantitySet() {

                return poItemRepository.sumQtyByItemCatDescrAndUomSet("Rail Pads");

        }

        @Override
        public RailPadFinalInspectionSummaryDto getRailPadFinalInspectionSummary() {
                long acceptedQtyNos = 0L;
                long acceptedQtySet = 0L;
                long rejectedQtyNos = 0L;
                long rejectedQtySet = 0L;

                java.util.List<Object[]> results = railFinalInspectionLotResultsRepository
                                .findAcceptedAndRejectedQtyByUom();
                if (results != null) {
                        for (Object[] row : results) {
                                String uom = row[0] != null ? row[0].toString().trim().toUpperCase() : "";
                                long acc = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                                long rej = row[2] != null ? ((Number) row[2]).longValue() : 0L;

                                if (uom.contains("SET")) {
                                        acceptedQtySet += acc;
                                        rejectedQtySet += rej;
                                } else {
                                        acceptedQtyNos += acc;
                                        rejectedQtyNos += rej;
                                }
                        }
                }

                return RailPadFinalInspectionSummaryDto.builder()
                                .acceptedQtyNos(acceptedQtyNos)
                                .acceptedQtySet(acceptedQtySet)
                                .rejectedQtyNos(rejectedQtyNos)
                                .rejectedQtySet(rejectedQtySet)
                                .build();
        }

        @Override

        public List<com.sarthi.dto.reports.RailPadShiftWiseProductionDto> getRailPadShiftWiseProductionReport(

                        String startDate, String endDate, String vendorCode, String plantId) {

                LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate)
                                : LocalDate.now().minusDays(30);

                LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

                String vCode = (vendorCode == null || vendorCode.trim().isEmpty()
                                || "All Manufacturers".equalsIgnoreCase(vendorCode)) ? null : vendorCode.trim();

                String pId = (plantId == null || plantId.trim().isEmpty() || "All Places".equalsIgnoreCase(plantId))
                                ? null
                                : plantId.trim();

                List<Object[]> rows = railIEProductionVerificationRepository.getRailPadShiftWiseProductionReport(start,
                                end, vCode, pId);

                List<com.sarthi.dto.reports.RailPadShiftWiseProductionDto> result = new ArrayList<>();

                for (Object[] row : rows) {

                        com.sarthi.dto.reports.RailPadShiftWiseProductionDto dto = new com.sarthi.dto.reports.RailPadShiftWiseProductionDto();

                        if (row[0] != null) {

                                if (row[0] instanceof java.sql.Date) {

                                        dto.setDate(((java.sql.Date) row[0]).toLocalDate());

                                } else if (row[0] instanceof LocalDate) {

                                        dto.setDate((LocalDate) row[0]);

                                } else {

                                        dto.setDate(LocalDate.parse(row[0].toString()));

                                }

                        }

                        dto.setShift(row[1] != null ? row[1].toString() : "");

                        dto.setPoNo(row[2] != null ? row[2].toString() : "");

                        dto.setNoOfBatches(row[3] != null ? ((Number) row[3]).longValue() : 0L);

                        dto.setProducedQty(row[4] != null ? ((Number) row[4]).longValue() : 0L);

                        dto.setAcceptedQty(row[5] != null ? ((Number) row[5]).longValue() : 0L);

                        dto.setRejectedQty(row[6] != null ? ((Number) row[6]).longValue() : 0L);

                        dto.setVendorName(row[7] != null ? row[7].toString() : "");

                        dto.setVendorCode(row[8] != null ? row[8].toString() : "");

                        dto.setPlantId(row[9] != null ? row[9].toString() : "");

                        if (row.length > 10 && row[10] != null) {
                                dto.setPlantName(row[10].toString());
                        } else {
                                dto.setPlantName("");
                        }

                        result.add(dto);

                }

                return result;

        }

        @Override

        public List<java.util.Map<String, String>> getRailPadDistinctVendors() {

                List<Object[]> rows = poHeaderRepository.findDistinctRailPadVendors();

                List<java.util.Map<String, String>> result = new ArrayList<>();

                for (Object[] row : rows) {

                        java.util.Map<String, String> map = new HashMap<>();

                        map.put("vendorName", row[0] != null ? row[0].toString() : "");

                        map.put("vendorCode", row[1] != null ? row[1].toString() : "");

                        result.add(map);

                }

                return result;

        }

        @Override

        public List<String> getRailPadDistinctPlants(String vendorCode) {

                return railIEProductionVerificationRepository.findDistinctPlants(vendorCode);

        }

        @Override

        public List<com.sarthi.dto.reports.RailPadQualityReportDto> getRailPadQualityReport(String startDate,
                        String endDate) {

                LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate)
                                : LocalDate.now().minusYears(1);

                LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

                List<PoHeader> railPadPoHeaders = poHeaderRepository.findRailPadPoHeadersWithItems();

                // Fetch bulk verification stats

                List<Object[]> verificationStatsList = railIEProductionVerificationRepository
                                .findVerificationStatsGroupedByPo(start, end);

                Map<String, Object[]> verificationStatsMap = new HashMap<>();

                for (Object[] row : verificationStatsList) {

                        if (row[0] != null) {

                                verificationStatsMap.put(row[0].toString(), row);

                        }

                }

                // Fetch bulk final inspection stats

                List<Object[]> finalStatsList = railIEProductionVerificationRepository
                                .findFinalInspectionStatsGroupedByPo(start, end);

                Map<String, Object[]> finalStatsMap = new HashMap<>();

                for (Object[] row : finalStatsList) {

                        if (row[0] != null) {

                                finalStatsMap.put(row[0].toString(), row);

                        }

                }

                // Fetch bulk process rejections

                List<Object[]> rejectionsList = railIEProductionVerificationRepository
                                .findProcessRejectionsGroupedByPo(start, end);

                Map<String, List<Object[]>> rejectionsMap = new HashMap<>();

                for (Object[] row : rejectionsList) {

                        if (row[0] != null) {

                                String poNoKey = row[0].toString();

                                rejectionsMap.computeIfAbsent(poNoKey, k -> new ArrayList<>()).add(row);

                        }

                }

                // Fetch product types declared by vendor

                List<Object[]> productTypesList = railProductionDeclarationRepository.findProductTypesGroupedByPo(start,
                                end);

                Map<String, String> productTypesMap = new HashMap<>();

                for (Object[] row : productTypesList) {

                        if (row[0] != null && row[1] != null) {

                                productTypesMap.put(row[0].toString(), row[1].toString());

                        }

                }

                List<com.sarthi.dto.reports.RailPadQualityReportDto> resultList = new ArrayList<>();

                int index = 1;

                for (PoHeader po : railPadPoHeaders) {
                        // Check if production has been declared
                        Object[] vStats = verificationStatsMap.get(po.getPoNo());
                        Object[] fStats = finalStatsMap.get(po.getPoNo());
                        List<Object[]> rList = rejectionsMap.get(po.getPoNo());

                        if (vStats == null && fStats == null && rList == null) {
                                continue;
                        }

                        com.sarthi.dto.reports.RailPadQualityReportDto dto = new com.sarthi.dto.reports.RailPadQualityReportDto();
                        dto.setSNo(index++);

                        dto.setZonalRailway(po.getRlyShortName() != null ? po.getRlyShortName() : "");

                        dto.setVendor(po.getFirmDetails() != null ? po.getFirmDetails() : "");

                        dto.setPoNo(po.getPoNo() != null ? po.getPoNo() : "");

                        dto.setPoDate(po.getPoDate() != null ? po.getPoDate().toLocalDate().toString() : "");

                        String type = productTypesMap.get(po.getPoNo());

                        if (type == null) {

                                type = "";

                        }

                        long orderedQty = 0;
                        String uom = "";

                        if (po.getItems() != null && !po.getItems().isEmpty()) {

                                if (type.isEmpty()) {

                                        type = po.getItems().get(0).getItemDesc() != null
                                                        ? po.getItems().get(0).getItemDesc()
                                                        : "";

                                }

                                uom = po.getItems().get(0).getUom() != null ? po.getItems().get(0).getUom() : "";

                                for (PoItem item : po.getItems()) {

                                        orderedQty += item.getQty() != null ? item.getQty() : 0;

                                }

                        }

                        dto.setTypeOfRubberPad(type);

                        dto.setTotalPoQty(orderedQty);
                        dto.setUom(uom);

                        dto.setSpecification("IRS-T-55-2025");

                        // Map bulk verification stats

                        Object[] verificationStats = verificationStatsMap.get(po.getPoNo());

                        if (verificationStats != null) {

                                dto.setQtyInspected(verificationStats[1] != null
                                                ? ((Number) verificationStats[1]).longValue()
                                                : 0L);

                                dto.setQtyAccepted(verificationStats[2] != null
                                                ? ((Number) verificationStats[2]).longValue()
                                                : 0L);

                        }

                        // Map bulk final inspection stats

                        Object[] finalStats = finalStatsMap.get(po.getPoNo());

                        if (finalStats != null) {

                                dto.setIcIssuedQty(finalStats[1] != null ? ((Number) finalStats[1]).longValue() : 0L);

                                if (finalStats[2] != null) {

                                        dto.setLastDateIcIssued(finalStats[2].toString());

                                }

                        }

                        // Map bulk rejections and defects

                        long rm = 0, compounding = 0, mixing = 0, curing = 0, cutting = 0, rheometer = 0,
                                        visualFinishing = 0;

                        long hardness = 0, gravity = 0, rubber = 0, ash = 0, rebound = 0, dimension = 0, weight = 0,
                                        surface = 0, compression = 0, visualTest = 0, other = 0;

                        List<Object[]> rejections = rejectionsMap.get(po.getPoNo());

                        if (rejections != null) {

                                for (Object[] row : rejections) {

                                        String reason = row[1] != null ? row[1].toString().toLowerCase() : "";

                                        long qty = row[2] != null ? ((Number) row[2]).longValue() : 0;

                                        if (reason.contains("finishing") || reason.contains("visual check") ||
                                                        reason.contains("short moulding") || reason.contains("bubbles")
                                                        ||
                                                        reason.contains("blisters") || reason.contains("uneven edges")
                                                        ||
                                                        reason.contains("surface roughness")
                                                        || reason.contains("improper side cut")) {
                                                visualFinishing += qty;
                                        } else if (reason.contains("hardness")) {
                                                hardness += qty;
                                        } else if (reason.contains("gravity")) {
                                                gravity += qty;
                                        } else if (reason.contains("rubber")) {
                                                rubber += qty;
                                        } else if (reason.contains("ash")) {
                                                ash += qty;
                                        } else if (reason.contains("rebound") || reason.contains("resilience")) {
                                                rebound += qty;
                                        } else if (reason.contains("dimension")) {
                                                dimension += qty;
                                        } else if (reason.contains("weight")) {
                                                weight += qty;
                                        } else if (reason.contains("surface")) {
                                                surface += qty;
                                        } else if (reason.contains("compression")) {
                                                compression += qty;
                                        } else if (reason.contains("visual test")) {
                                                visualTest += qty;
                                        } else if (reason.contains("material") || reason.contains("raw")) {
                                                rm += qty;
                                        } else if (reason.contains("compounding")) {
                                                compounding += qty;
                                        } else if (reason.contains("mixing")) {
                                                mixing += qty;
                                        } else if (reason.contains("curing")) {
                                                curing += qty;
                                        } else if (reason.contains("cutting")) {
                                                cutting += qty;
                                        } else if (reason.contains("rheo")) {
                                                rheometer += qty;
                                        } else {
                                                other += qty;
                                        }

                                }

                        }

                        dto.setRawMaterialCheck(rm);

                        dto.setCompounding(compounding);

                        dto.setMixing(mixing);

                        dto.setCuring(curing);

                        dto.setCutting(cutting);

                        dto.setRheometer(rheometer);

                        dto.setVisualCheckFinishing(visualFinishing);

                        dto.setHardness(hardness);

                        dto.setSpecificGravity(gravity);

                        dto.setRubberContent(rubber);

                        dto.setAshContent(ash);

                        dto.setReboundResilience(rebound);

                        dto.setDimension(dimension);

                        dto.setWeight(weight);

                        dto.setSurfaceDefect(surface);

                        dto.setCompressionSet(compression);

                        dto.setVisualTest(visualTest);

                        dto.setOtherRejection(other);

                        // Rejection Percentage

                        long totalRejections = rm + compounding + mixing + curing + cutting + rheometer
                                        + visualFinishing +

                                        hardness + gravity + rubber + ash + rebound + dimension + weight + surface
                                        + compression + visualTest + other;

                        if (dto.getQtyInspected() > 0) {

                                double pct = (double) totalRejections * 100.0 / (double) dto.getQtyInspected();

                                dto.setRejectionPercent(Math.round(pct * 100.0) / 100.0);

                        } else {

                                dto.setRejectionPercent(0.0);

                        }

                        dto.setRemarks(totalRejections > 0 ? "Defects observed" : "Satisfactory");

                        resultList.add(dto);

                }

                return resultList;

        }

        @Override

        public List<String> getAllCompanies() {

                return pincodePoIMappingRepository.getAllCompanies();

        }

        @Override
        public List<com.sarthi.dto.reports.IcAnnexuresReportDto> getDownloadIcAnnexuresReport(String product, String vendorPlantCode, String zonalRailway, java.time.LocalDate startDate, java.time.LocalDate endDate) {
                List<Object[]> rawList = workflowTransitionRepository.findDownloadIcAnnexuresReportRaw(vendorPlantCode, zonalRailway, startDate, endDate);
                List<com.sarthi.dto.reports.IcAnnexuresReportDto> resultList = new java.util.ArrayList<>();

                if (rawList == null) {
                        return resultList;
                }

                String filterProduct = (product != null) ? product.trim().toLowerCase() : "";

                for (Object[] row : rawList) {
                        String itemCatDescr = row[8] != null ? row[8].toString() : "";

                        com.sarthi.dto.reports.IcAnnexuresReportDto dto = com.sarthi.dto.reports.IcAnnexuresReportDto
                                        .builder()
                                        .vendorName(row[0] != null ? row[0].toString() : "")
                                        .railwayShortName(row[1] != null ? row[1].toString() : "")
                                        .poNumberOnly(row[2] != null ? row[2].toString() : "")
                                        .poSerialNumber(row[3] != null ? row[3].toString() : "")
                                        .callNumber(row[4] != null ? row[4].toString() : "")
                                        .icNumber(row[5] != null ? row[5].toString() : "")
                                        .stage(row[6] != null ? row[6].toString() : "")
                                        .icIssuedDate(row[7] != null ? row[7].toString() : "")
                                        .itemCatDescr(itemCatDescr)
                                        .build();

                        if (!filterProduct.isEmpty()) {
                                String catLower = itemCatDescr.toLowerCase();
                                if (filterProduct.contains("sleeper") || "sleeper".equals(filterProduct)) {
                                        if (catLower.contains("sleeper")) {
                                                resultList.add(dto);
                                        }
                                } else if (filterProduct.contains("pad") || "rail pad".equals(filterProduct)) {
                                        if (catLower.contains("pad")) {
                                                resultList.add(dto);
                                        }
                                } else if (filterProduct.contains("erc") || "erc".equals(filterProduct)) {
                                        if (catLower.contains("clip") || catLower.contains("erc")) {
                                                resultList.add(dto);
                                        }
                                } else {
                                        if (catLower.contains(filterProduct)) {
                                                resultList.add(dto);
                                        }
                                }
                        } else {
                                resultList.add(dto);
                        }
                }

                return resultList;
        }

        @Override
        public List<InspectionCallsReportDto> getInspectionCallsReport(
                        String startDate,
                        String endDate) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                LocalDate start = (startDate != null && !startDate.isBlank())
                                ? LocalDate.parse(startDate, formatter)
                                : LocalDate.now().minusYears(1);

                LocalDate end = (endDate != null && !endDate.isBlank())
                                ? LocalDate.parse(endDate, formatter)
                                : LocalDate.now();

                LocalDateTime startDateTime = start.atStartOfDay();

                LocalDateTime endDateTime = end.atTime(23, 59, 59);

                List<Object[]> results = inspectionCallRepository.getInspectionCallsReport(
                                startDateTime,
                                endDateTime);

                List<InspectionCallsReportDto> response = new ArrayList<>();

                for (Object[] obj : results) {

                        InspectionCallsReportDto dto = new InspectionCallsReportDto();

                        dto.setCallNumber(
                                        obj[0] != null ? obj[0].toString() : null);

                        dto.setProductAndStageOfInspection(
                                        obj[1] != null ? obj[1].toString() : null);

                        dto.setPoNumber(
                                        obj[2] != null ? obj[2].toString() : null);

                        dto.setDeliveryDate(
                                        obj[3] != null
                                                        ? ((Timestamp) obj[3]).toLocalDateTime()
                                                        : null);

                        dto.setExpectedDeliveryDate(
                                        obj[4] != null
                                                        ? ((Timestamp) obj[4]).toLocalDateTime()
                                                        : null);

                        dto.setVendorName(
                                        obj[5] != null ? obj[5].toString() : null);

                        dto.setInspectionDesiredDate(
                                        obj[6] != null
                                                        ? ((Date) obj[6]).toLocalDate()
                                                        : null);

                        dto.setCallDate(
                                        obj[7] != null
                                                        ? ((Timestamp) obj[7]).toLocalDateTime()
                                                        : null);

                        dto.setIeName(
                                        obj[8] != null ? obj[8].toString() : null);

                        dto.setCmName(
                                        obj[9] != null ? obj[9].toString() : null);

                        dto.setRitesRio(
                                        obj[10] != null ? obj[10].toString() : null);

                        dto.setStatus(
                                        obj[11] != null ? obj[11].toString() : null);

                        response.add(dto);
                }

                return response;
        }

        @Override
        public List<InspectionCallsReportDto> getOverduePendingInspectionCallsReport(
                        String startDate,
                        String endDate) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                LocalDate start = (startDate != null && !startDate.isBlank())
                                ? LocalDate.parse(startDate, formatter)
                                : LocalDate.now().minusYears(1);

                LocalDate end = (endDate != null && !endDate.isBlank())
                                ? LocalDate.parse(endDate, formatter)
                                : LocalDate.now();

                LocalDateTime startDateTime = start.atStartOfDay();

                LocalDateTime endDateTime = end.atTime(23, 59, 59);

                List<Object[]> results = inspectionCallRepository
                                .getOverduePendingInspectionCallsReport(
                                                startDateTime,
                                                endDateTime);

                return results.stream().map(obj -> {

                        InspectionCallsReportDto dto = new InspectionCallsReportDto();

                        dto.setCallNumber(
                                        obj[0] != null ? obj[0].toString() : null);

                        dto.setProductAndStageOfInspection(
                                        obj[1] != null ? obj[1].toString() : null);

                        dto.setPoNumber(
                                        obj[2] != null ? obj[2].toString() : null);

                        dto.setDeliveryDate(
                                        obj[3] != null
                                                        ? ((Timestamp) obj[3]).toLocalDateTime()
                                                        : null);

                        dto.setExpectedDeliveryDate(
                                        obj[4] != null
                                                        ? ((Timestamp) obj[4]).toLocalDateTime()
                                                        : null);

                        dto.setVendorName(
                                        obj[5] != null ? obj[5].toString() : null);

                        dto.setInspectionDesiredDate(
                                        obj[6] != null
                                                        ? ((Date) obj[6]).toLocalDate()
                                                        : null);

                        dto.setCallDate(
                                        obj[7] != null
                                                        ? ((Timestamp) obj[7]).toLocalDateTime()
                                                        : null);

                        dto.setIeName(
                                        obj[8] != null ? obj[8].toString() : null);

                        dto.setCmName(
                                        obj[9] != null ? obj[9].toString() : null);

                        dto.setRitesRio(
                                        obj[10] != null ? obj[10].toString() : null);

                        dto.setStatus(
                                        obj[11] != null ? obj[11].toString() : null);

                        return dto;

                }).toList();
        }

        @Override
        public List<IeWiseCallStatusWorkloadSummaryDto> getIeWiseCallStatusWorkloadSummary(
                        String cmEmployeeCode) {

                List<Object[]> results = inspectionCallRepository
                                .getIeWiseCallStatusWorkloadSummary(
                                                cmEmployeeCode);

                return results.stream().map(obj -> {

                        IeWiseCallStatusWorkloadSummaryDto dto = new IeWiseCallStatusWorkloadSummaryDto();

                        dto.setIeId(
                                        obj[0] != null ? obj[0].toString() : null);

                        dto.setIeName(
                                        obj[1] != null ? obj[1].toString() : null);

                        dto.setNoOfCallsPending(
                                        obj[2] != null
                                                        ? ((Number) obj[2]).longValue()
                                                        : 0L);

                        dto.setNoOfCallsUnderInspection(
                                        obj[3] != null
                                                        ? ((Number) obj[3]).longValue()
                                                        : 0L);

                        dto.setNoOfCallsPendingForIc(
                                        obj[4] != null
                                                        ? ((Number) obj[4]).longValue()
                                                        : 0L);

                        dto.setNoOfCallsOverdue(
                                        obj[5] != null
                                                        ? ((Number) obj[5]).longValue()
                                                        : 0L);

                        return dto;

                }).toList();
        }

        @Override
        public List<IeOperationalSlaPerformanceSummaryDto> getIeOperationalSlaPerformanceSummary(
                        String cmEmployeeCode) {

                List<Object[]> results = inspectionCallRepository
                                .getIeOperationalSlaPerformanceSummary(
                                                cmEmployeeCode);

                return results.stream().map(obj -> {

                        IeOperationalSlaPerformanceSummaryDto dto = new IeOperationalSlaPerformanceSummaryDto();

                        dto.setIeId(
                                        obj[0] != null ? obj[0].toString() : null);

                        dto.setIeName(
                                        obj[1] != null ? obj[1].toString() : null);

                        dto.setTotalCalls(
                                        obj[2] != null
                                                        ? ((Number) obj[2]).longValue()
                                                        : 0L);

                        dto.setOverdueCallsAttended(
                                        obj[3] != null
                                                        ? ((Number) obj[3]).longValue()
                                                        : 0L);

                        dto.setCallsCancelled(
                                        obj[4] != null
                                                        ? ((Number) obj[4]).longValue()
                                                        : 0L);

                        dto.setCallsAccepted(
                                        obj[5] != null
                                                        ? ((Number) obj[5]).longValue()
                                                        : 0L);

                        dto.setCallsRejected(
                                        obj[6] != null
                                                        ? ((Number) obj[6]).longValue()
                                                        : 0L);

                        dto.setCallsPartiallyAcceptedRejected(
                                        obj[7] != null
                                                        ? ((Number) obj[7]).longValue()
                                                        : 0L);

                        dto.setCallsWithheld(
                                        obj[8] != null
                                                        ? ((Number) obj[8]).longValue()
                                                        : 0L);

                        dto.setIcIssued(
                                        obj[9] != null
                                                        ? ((Number) obj[9]).longValue()
                                                        : 0L);

                        return dto;

                }).toList();
        }

        @Override
        public List<RailPadPoLifeCycle1stLevelDto> getRailPadPo1stLevelStatus() {
                List<PoHeader> poHeaders = poHeaderRepository.findRailPadPoHeadersWithItems();
                List<Object[]> acceptedQtyList = railFinalInspectionLotResultsRepository.findAcceptedQtyByPo();

                // Fetch railpad types from final inspection lot results, inspection calls, and
                // production declarations
                List<Object[]> typeList = railFinalInspectionLotResultsRepository.findDistinctRailpadTypesGroupByPo();
                List<Object[]> callTypeList = railInspectionCallRepository.findDistinctRailpadTypesGroupByPo();
                List<Object[]> prodTypeList = railProductionDeclarationRepository.findDistinctProductTypesGroupByPo();

                Map<String, Long> acceptedQtyMap = new HashMap<>();
                for (Object[] row : acceptedQtyList) {
                        if (row[0] != null && row[1] != null) {
                                acceptedQtyMap.put(row[0].toString().trim(), ((Number) row[1]).longValue());
                        }
                }

                // Combine all types into a set per PO to ensure we capture clean types
                Map<String, Set<String>> combinedTypes = new HashMap<>();

                for (Object[] row : typeList) {
                        if (row[0] != null && row[1] != null) {
                                String po = row[0].toString().trim();
                                String types = row[1].toString();
                                combinedTypes.computeIfAbsent(po, k -> new LinkedHashSet<>())
                                                .addAll(Arrays.asList(types.split(",\\s*")));
                        }
                }
                for (Object[] row : callTypeList) {
                        if (row[0] != null && row[1] != null) {
                                String po = row[0].toString().trim();
                                String types = row[1].toString();
                                combinedTypes.computeIfAbsent(po, k -> new LinkedHashSet<>())
                                                .addAll(Arrays.asList(types.split(",\\s*")));
                        }
                }
                for (Object[] row : prodTypeList) {
                        if (row[0] != null && row[1] != null) {
                                String po = row[0].toString().trim();
                                String types = row[1].toString();
                                combinedTypes.computeIfAbsent(po, k -> new LinkedHashSet<>())
                                                .addAll(Arrays.asList(types.split(",\\s*")));
                        }
                }

                Map<String, String> typeMap = new HashMap<>();
                for (Map.Entry<String, Set<String>> entry : combinedTypes.entrySet()) {
                        typeMap.put(entry.getKey(), String.join(", ", entry.getValue()));
                }

                List<RailPadPoLifeCycle1stLevelDto> list = new ArrayList<>();
                AtomicInteger counter = new AtomicInteger(1);

                for (PoHeader po : poHeaders) {
                        String poNo = po.getPoNo();
                        long totalQty = po.getItems().stream()
                                        .mapToLong(pi -> pi.getQty() != null ? pi.getQty().longValue() : 0L)
                                        .sum();
                        long acceptedQty = acceptedQtyMap.getOrDefault(poNo.trim(), 0L);
                        long overallPoBalance = totalQty - acceptedQty;
                        if (overallPoBalance < 0)
                                overallPoBalance = 0L;

                        String vendorName = po.getFirmDetails() != null ? po.getFirmDetails()
                                        : (po.getVendorDetails() != null ? po.getVendorDetails().split("~")[0] : "");

                        String railPadType = typeMap.getOrDefault(poNo.trim(), "");
                        if (railPadType.isEmpty() && !po.getItems().isEmpty()) {
                                List<String> itemDescs = po.getItems().stream()
                                                .map(pi -> parseRailPadType(pi.getItemDesc()))
                                                .filter(Objects::nonNull)
                                                .filter(s -> !s.isEmpty())
                                                .distinct()
                                                .toList();
                                railPadType = String.join(", ", itemDescs);
                        }

                        RailPadPoLifeCycle1stLevelDto dto = new RailPadPoLifeCycle1stLevelDto(
                                        counter.getAndIncrement(),
                                        po.getRlyShortName(),
                                        poNo,
                                        po.getPoDate(),
                                        vendorName,
                                        po.getInspectingAgency(),
                                        totalQty,
                                        acceptedQty,
                                        overallPoBalance,
                                        railPadType);
                        list.add(dto);
                }
                return list;
        }

        @Override
        public List<RailPadPoLifeCycle2ndLevelDto> getRailPadPo2ndLevelStatus(String poNo) {
                Optional<PoHeader> poOpt = poHeaderRepository.findByPoNoWithItems(poNo);
                if (poOpt.isEmpty()) {
                        return new ArrayList<>();
                }
                PoHeader po = poOpt.get();
                List<PoItem> items = po.getItems();

                // 1. Process rejections: total pieces produced and rejected for the entire PO
                List<Object[]> processRejList = railIEProductionVerificationRepository
                                .findProcessRejectionSumsByPo(poNo);
                double processProduced = 0.0;
                double processRejected = 0.0;
                if (processRejList != null && !processRejList.isEmpty()) {
                        Object[] row = processRejList.get(0);
                        if (row[0] != null)
                                processProduced = ((Number) row[0]).doubleValue();
                        if (row[1] != null)
                                processRejected = ((Number) row[1]).doubleValue();
                }
                double processRejectionPercentage = 0.0;
                if (processProduced > 0) {
                        processRejectionPercentage = (processRejected * 100.0) / processProduced;
                        processRejectionPercentage = Math.round(processRejectionPercentage * 100.0) / 100.0;
                }

                List<RailPadPoLifeCycle2ndLevelDto> list = new ArrayList<>();
                AtomicInteger counter = new AtomicInteger(1);

                for (PoItem item : items) {
                        String poSr = item.getItemSrNo();

                        // 2. Fetch distinct rail pad type(s)
                        List<String> types = railFinalInspectionLotResultsRepository
                                        .findDistinctRailpadTypesByPoAndSr(poNo, poSr);
                        String railPadType = (types != null && !types.isEmpty()) ? String.join(", ", types)
                                        : parseRailPadType(item.getItemDesc());

                        // 3. Accepted Qty
                        Long acceptedQtyLong = railFinalInspectionLotResultsRepository.sumAcceptedQtyByPoAndSr(poNo,
                                        poSr);
                        int acceptedQty = acceptedQtyLong != null ? acceptedQtyLong.intValue() : 0;

                        // 4. Balance Qty
                        int poSrNoQty = item.getQty() != null ? item.getQty() : 0;
                        int balanceQty = poSrNoQty - acceptedQty;
                        if (balanceQty < 0)
                                balanceQty = 0;

                        // 5. Distinct calls count
                        Long noOfIcsLong = railInspectionCallRepository.countCallsByPoAndSr(poNo, poSr);
                        int noOfIcs = noOfIcsLong != null ? noOfIcsLong.intValue() : 0;

                        // 6. Final rejection %: sum(rejected_qty) * 100 / sum(offered_qty) for that
                        // serial
                        List<Object[]> finalRejList = railFinalInspectionLotResultsRepository
                                        .findFinalRejectionSumsByPoAndSr(poNo, poSr);
                        double finalOffered = 0.0;
                        double finalRejected = 0.0;
                        if (finalRejList != null && !finalRejList.isEmpty()) {
                                Object[] row = finalRejList.get(0);
                                if (row[0] != null)
                                        finalOffered = ((Number) row[0]).doubleValue();
                                if (row[1] != null)
                                        finalRejected = ((Number) row[1]).doubleValue();
                        }
                        double finalRejectionPercentage = 0.0;
                        if (finalOffered > 0) {
                                finalRejectionPercentage = (finalRejected * 100.0) / finalOffered;
                                finalRejectionPercentage = Math.round(finalRejectionPercentage * 100.0) / 100.0;
                        }

                        // 7. Total rejection %
                        double totalRejectionPercentage = processRejectionPercentage + finalRejectionPercentage;
                        totalRejectionPercentage = Math.round(totalRejectionPercentage * 100.0) / 100.0;

                        RailPadPoLifeCycle2ndLevelDto dto = new RailPadPoLifeCycle2ndLevelDto(
                                        counter.getAndIncrement(),
                                        poSr,
                                        railPadType,
                                        item.getConsigneeDetail(),
                                        item.getDeliveryDate(),
                                        item.getExtendedDeliveryDate(),
                                        poSrNoQty,
                                        balanceQty,
                                        noOfIcs,
                                        processRejectionPercentage,
                                        finalRejectionPercentage,
                                        totalRejectionPercentage);
                        list.add(dto);
                }
                return list;
        }

        @Override
        public List<RailPadPoLifeCycle3rdLevelDto> getRailPadPo3rdLevelStatus(String poNo, String serialNo) {
                List<Object[]> rows = railFinalInspectionLotResultsRepository.findCallsDetailByPoAndSr(poNo, serialNo);
                List<RailPadPoLifeCycle3rdLevelDto> list = new ArrayList<>();
                AtomicInteger counter = new AtomicInteger(1);

                for (Object[] row : rows) {
                        String callNo = row[0] != null ? row[0].toString() : "";
                        double offered = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                        double accepted = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
                        double rejected = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

                        double rejectionPercentage = 0.0;
                        if (offered > 0) {
                                rejectionPercentage = (rejected * 100.0) / offered;
                                rejectionPercentage = Math.round(rejectionPercentage * 100.0) / 100.0;
                        }

                        RailPadPoLifeCycle3rdLevelDto dto = new RailPadPoLifeCycle3rdLevelDto(
                                        counter.getAndIncrement(),
                                        callNo,
                                        offered,
                                        accepted,
                                        rejected,
                                        rejectionPercentage);
                        list.add(dto);
                }
                return list;
        }

        @Override
        public com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMprReportDto> getRailPadMprReport(
                        int page,
                        int size,
                        java.time.LocalDate startDate,
                        java.time.LocalDate endDate,
                        String rio,
                        String zone,
                        String vendor) {

                String zoneParam = (zone != null && !zone.trim().isEmpty() && !zone.equalsIgnoreCase("all"))
                                ? zone.trim()
                                : null;
                String vendorParam = (vendor != null && !vendor.trim().isEmpty() && !vendor.equalsIgnoreCase("all"))
                                ? vendor.trim()
                                : null;
                String rioParam = (rio != null && !rio.trim().isEmpty() && !rio.equalsIgnoreCase("all")) ? rio.trim()
                                : null;

                List<Object[]> rows = poHeaderRepository.fetchRailPadMonthlyProgress(startDate, endDate, rioParam,
                                zoneParam, vendorParam);
                List<com.sarthi.dto.reports.RailPadMprReportDto> allList = new ArrayList<>();

                for (Object[] row : rows) {
                        String rly = row[0] != null ? row[0].toString() : "";
                        String poNo = row[1] != null ? row[1].toString() : "";
                        String manufacturer = row[2] != null ? row[2].toString() : "";
                        Double poQty = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                        String uom = row[4] != null ? row[4].toString() : "Nos.";
                        Double dispatchedMonthly = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
                        Double totalDispatched = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
                        Double balance = poQty - totalDispatched;
                        if (balance < 0)
                                balance = 0.0;

                        allList.add(new com.sarthi.dto.reports.RailPadMprReportDto(
                                        rly, poNo, manufacturer, poQty, uom, dispatchedMonthly, totalDispatched,
                                        balance));
                }

                // In-memory pagination
                int totalElements = allList.size();
                int totalPages = (int) Math.ceil((double) totalElements / size);
                if (totalPages == 0)
                        totalPages = 1;

                int startIdx = page * size;
                int endIdx = Math.min(startIdx + size, totalElements);
                List<com.sarthi.dto.reports.RailPadMprReportDto> content = new ArrayList<>();
                if (startIdx < totalElements) {
                        content = allList.subList(startIdx, endIdx);
                }

                com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMprReportDto> response = new com.sarthi.dto.summaryDtos.PageResponseDTO<>();
                response.setContent(content);
                response.setPage(page);
                response.setSize(size);
                response.setTotalElements(totalElements);
                response.setTotalPages(totalPages);
                response.setLast(page >= totalPages - 1);

                return response;
        }

        @Override
        public com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMauReportDto> getRailPadMauReport(
                        int page,
                        int size,
                        java.time.LocalDate startDate,
                        java.time.LocalDate endDate,
                        String rio,
                        String zone,
                        String vendor) {

                String zoneParam = (zone != null && !zone.trim().isEmpty() && !zone.equalsIgnoreCase("all"))
                                ? zone.trim()
                                : null;
                String vendorParam = (vendor != null && !vendor.trim().isEmpty() && !vendor.equalsIgnoreCase("all"))
                                ? vendor.trim()
                                : null;
                String rioParam = (rio != null && !rio.trim().isEmpty() && !rio.equalsIgnoreCase("all")) ? rio.trim()
                                : null;

                List<Object[]> rows = railVendorPlantsRepository.fetchRailPadMonthlyAnalysis(startDate, endDate,
                                rioParam, zoneParam, vendorParam);
                List<com.sarthi.dto.reports.RailPadMauReportDto> allList = new ArrayList<>();

                for (Object[] row : rows) {
                        String plantName = row[0] != null ? row[0].toString() : "";
                        String rowRio = row[1] != null ? row[1].toString() : "N/A";
                        Long production = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                        Long acceptance = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                        Long processRejection = row[4] != null ? ((Number) row[4]).longValue() : 0L;
                        Long finalRejection = row[5] != null ? ((Number) row[5]).longValue() : 0L;

                        double processRejPct = 0.0;
                        double finalRejPct = 0.0;
                        double totalRejPct = 0.0;

                        if (production > 0) {
                                processRejPct = Math.round(((double) processRejection / production * 100) * 100.0)
                                                / 100.0;
                                finalRejPct = Math.round(((double) finalRejection / production * 100) * 100.0) / 100.0;
                                totalRejPct = Math
                                                .round(((double) (finalRejection + processRejection) / production * 100)
                                                                * 100.0)
                                                / 100.0;
                        }

                        allList.add(new com.sarthi.dto.reports.RailPadMauReportDto(
                                        plantName, rowRio, production, acceptance, processRejection, processRejPct,
                                        finalRejection, finalRejPct, totalRejPct));
                }

                // In-memory pagination
                int totalElements = allList.size();
                int totalPages = (int) Math.ceil((double) totalElements / size);
                if (totalPages == 0)
                        totalPages = 1;

                int startIdx = page * size;
                int endIdx = Math.min(startIdx + size, totalElements);
                List<com.sarthi.dto.reports.RailPadMauReportDto> content = new ArrayList<>();
                if (startIdx < totalElements) {
                        content = allList.subList(startIdx, endIdx);
                }

                com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMauReportDto> response = new com.sarthi.dto.summaryDtos.PageResponseDTO<>();
                response.setContent(content);
                response.setPage(page);
                response.setSize(size);
                response.setTotalElements(totalElements);
                response.setTotalPages(totalPages);
                response.setLast(page >= totalPages - 1);

                return response;
        }

        @Override
        public java.util.List<java.util.Map<String, String>> getRailPadClosedLoopManufacturers() {
                List<Object[]> rows = railPadPincodePoIMappingRepository.findDistinctManufacturers();
                List<java.util.Map<String, String>> list = new ArrayList<>();
                for (Object[] r : rows) {
                        java.util.Map<String, String> map = new java.util.HashMap<>();
                        map.put("vendorCode", r[0] != null ? r[0].toString() : "");
                        map.put("companyName", r[1] != null ? r[1].toString() : "");
                        list.add(map);
                }
                return list;
        }

        @Override
        public java.util.List<java.util.Map<String, String>> getRailPadClosedLoopPlants(String vendorCode) {
                List<com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants> plants = railVendorPlantsRepository
                                .findByVendorCode(vendorCode);
                List<java.util.Map<String, String>> list = new ArrayList<>();
                for (com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants p : plants) {
                        java.util.Map<String, String> map = new java.util.HashMap<>();
                        map.put("plantId", p.getPlantId());
                        map.put("plantName", p.getPlantName());
                        list.add(map);
                }
                return list;
        }

        @Override
        public java.util.List<java.util.Map<String, Object>> getRailPadClosedLoopLots(String plantId, int year) {
                List<Object[]> rows = railInspectionLotRepository.findLotsByPlantAndYear(plantId, year);
                List<java.util.Map<String, Object>> list = new ArrayList<>();
                for (Object[] r : rows) {
                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        map.put("lotId", r[0] != null ? ((Number) r[0]).longValue() : 0L);
                        map.put("lotNo", r[1] != null ? r[1].toString() : "");
                        map.put("callNo", r[2] != null ? r[2].toString() : "");
                        list.add(map);
                }
                return list;
        }

        @Override
        public com.sarthi.dto.reports.RailPadLotClosedLoopDto getRailPadLotClosedLoopDetails(Long lotId) {
                Optional<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionLot> lotOpt = railInspectionLotRepository
                                .findById(lotId);
                if (lotOpt.isEmpty()) {
                        return null;
                }
                com.sarthi.SRailPad.entity.inspectionCall.RailInspectionLot lot = lotOpt.get();
                com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall call = lot.getInspectionCall();
                if (call == null) {
                        return null;
                }

                String callNo = call.getCallNo();
                String lotNo = lot.getLotNo();
                Integer lotSize = lot.getLotSize();

                Optional<com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults> finalResultOpt = railFinalInspectionLotResultsRepository
                                .findByCallNoAndLotNo(callNo, lotNo);

                String rlyPoSrNo = "";
                LocalDate dateOfInspection = null;
                Integer acceptedQty = 0;
                Integer rejectedQty = 0;
                String overallStatus = "PENDING";

                if (finalResultOpt.isPresent()) {
                        com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults finalRes = finalResultOpt
                                        .get();
                        rlyPoSrNo = finalRes.getRlyPoSrNo();
                        if (rlyPoSrNo != null && rlyPoSrNo.startsWith("N/A/")) {
                                if (call.getPoNo() != null) {
                                        String barePoNo = call.getPoNo().contains("/") ? call.getPoNo().split("/")[0]
                                                        : call.getPoNo();
                                        Optional<com.sarthi.entity.PoHeader> poOpt = poHeaderRepository
                                                        .findByPoNo(barePoNo);
                                        if (poOpt.isPresent()) {
                                                String rly = poOpt.get().getRlyShortName();
                                                if (rly == null || rly.trim().isEmpty()) {
                                                        rly = poOpt.get().getRlyCd();
                                                }
                                                if (rly != null && !rly.trim().isEmpty()) {
                                                        rlyPoSrNo = rly + rlyPoSrNo.substring(3);
                                                }
                                        }
                                }
                        }
                        dateOfInspection = finalRes.getDateOfInspection();
                        acceptedQty = finalRes.getAcceptedQty() != null ? finalRes.getAcceptedQty() : 0;
                        rejectedQty = finalRes.getRejectedQty() != null ? finalRes.getRejectedQty() : 0;
                        overallStatus = finalRes.getOverallStatus();
                } else {
                        String rlyShortName = "N/A";
                        if (call.getPoNo() != null) {
                                String barePoNo = call.getPoNo().contains("/") ? call.getPoNo().split("/")[0]
                                                : call.getPoNo();
                                Optional<com.sarthi.entity.PoHeader> poOpt = poHeaderRepository.findByPoNo(barePoNo);
                                if (poOpt.isPresent()) {
                                        rlyShortName = poOpt.get().getRlyShortName();
                                        if (rlyShortName == null || rlyShortName.trim().isEmpty()) {
                                                rlyShortName = poOpt.get().getRlyCd();
                                        }
                                }
                        }
                        if (rlyShortName == null || rlyShortName.trim().isEmpty()) {
                                rlyShortName = "N/A";
                        }
                        String poSr = call.getPoSr() != null ? call.getPoSr() : "N/A";
                        rlyPoSrNo = rlyShortName + "/" + call.getPoNo() + "/" + poSr;
                        dateOfInspection = null;
                }

                if (rlyPoSrNo != null && rlyPoSrNo.endsWith("/N/A")) {
                        rlyPoSrNo = rlyPoSrNo.substring(0, rlyPoSrNo.length() - 4);
                }

                List<Object[]> batchRows = railInspectionLotRepository.findBatchesByLotId(lotId);
                List<com.sarthi.dto.reports.RailPadLotClosedLoopDto.BatchDto> batchesList = new ArrayList<>();

                long totalProduction = 0L;
                long totalProcessRejection = 0L;
                LocalDate productionDate = null;

                for (Object[] r : batchRows) {
                        String batchNo = r[0] != null ? r[0].toString() : "";
                        LocalDate prodDate = r[1] != null
                                        ? (r[1] instanceof java.sql.Date ? ((java.sql.Date) r[1]).toLocalDate()
                                                        : (LocalDate) r[1])
                                        : null;
                        Integer qty = r[2] != null ? ((Number) r[2]).intValue() : 0;

                        batchesList.add(new com.sarthi.dto.reports.RailPadLotClosedLoopDto.BatchDto(
                                        batchNo, prodDate, qty));

                        if (prodDate != null) {
                                productionDate = prodDate;
                        }

                        List<Object[]> prodInfo = railInspectionLotRepository
                                        .findProductionByPlantAndBatch(call.getPlantId(), batchNo);
                        if (!prodInfo.isEmpty()) {
                                Object[] pRow = prodInfo.get(0);
                                totalProduction += pRow[0] != null ? ((Number) pRow[0]).longValue() : 0L;
                                if (pRow[1] != null && productionDate == null) {
                                        productionDate = pRow[1] instanceof java.sql.Date
                                                        ? ((java.sql.Date) pRow[1]).toLocalDate()
                                                        : (LocalDate) pRow[1];
                                }
                        }

                        List<Object[]> procRejInfo = railInspectionLotRepository
                                        .findProcessRejectionByPlantAndBatch(call.getPlantId(), batchNo);
                        if (!procRejInfo.isEmpty()) {
                                Object[] prRow = procRejInfo.get(0);
                                totalProcessRejection += prRow[0] != null ? ((Number) prRow[0]).longValue() : 0L;
                        }
                }

                List<com.sarthi.dto.reports.RailPadLotClosedLoopDto.StageDto> stagesList = new ArrayList<>();

                stagesList.add(new com.sarthi.dto.reports.RailPadLotClosedLoopDto.StageDto(
                                "Produced",
                                productionDate,
                                (int) totalProduction,
                                "Total declared production: " + totalProduction + " Nos."));

                stagesList.add(new com.sarthi.dto.reports.RailPadLotClosedLoopDto.StageDto(
                                "Rejection in Process",
                                productionDate,
                                (int) totalProcessRejection,
                                "Process inspection rejected: " + totalProcessRejection + " Nos."));

                stagesList.add(new com.sarthi.dto.reports.RailPadLotClosedLoopDto.StageDto(
                                "Final Inspection",
                                dateOfInspection,
                                lotSize,
                                overallStatus + " (Accepted: " + acceptedQty + ", Rejected: " + rejectedQty + ")"));

                return new com.sarthi.dto.reports.RailPadLotClosedLoopDto(
                                rlyPoSrNo, lotSize, dateOfInspection, acceptedQty, rejectedQty, overallStatus,
                                batchesList, stagesList);
        }

        private static String parseRailPadType(String desc) {
                if (desc == null || desc.isEmpty()) {
                        return "";
                }
                String lower = desc.toLowerCase();

                boolean is10mm = lower.contains("10 mm") || lower.contains("10mm") || lower.contains("10.00mm")
                                || lower.contains("10.00 mm");
                boolean is6mm = lower.contains("6 mm") || lower.contains("6mm") || lower.contains("6.00mm")
                                || lower.contains("6.00 mm") || lower.contains("6.2 mm") || lower.contains("6.2mm");
                boolean is12mm = lower.contains("12 mm") || lower.contains("12mm") || lower.contains("12.00mm");

                boolean isComposite = lower.contains("composite") || lower.contains("cgrsp");
                boolean isNylon = lower.contains("nylon") || lower.contains("ncrgrsp") || lower.contains("reinforced");

                if (is10mm) {
                        if (isComposite)
                                return "10.00mm CGRSP";
                        if (isNylon)
                                return "10.00mm NCRGRSP";
                        return "10.00mm GRSP";
                }
                if (is6mm) {
                        if (isComposite) {
                                if (lower.contains("6.2"))
                                        return "6.20mm CGRSP";
                                return "6.00mm CGRSP";
                        }
                        if (isNylon)
                                return "6.00mm NCRGRSP";
                        return "6.00mm GRSP";
                }
                if (is12mm) {
                        return "12.00mm GRSP";
                }

                if (lower.contains("rt-8746") || lower.contains("t-8746") || lower.contains("t-8747")) {
                        return "10.00mm CGRSP";
                }
                if (lower.contains("t-6618") || lower.contains("t-8327")) {
                        return "6.20mm CGRSP";
                }
                if (lower.contains("t-4218") || lower.contains("t-4865") || lower.contains("t-6154")
                                || lower.contains("t-3902") || lower.contains("t-10263")) {
                        return "6.00mm NCRGRSP";
                }

                if (lower.contains("composite"))
                        return "CGRSP";
                if (lower.contains("nylon"))
                        return "NCRGRSP";
                if (lower.contains("grooved"))
                        return "GRSP";

                return desc.length() > 30 ? desc.substring(0, 30) + "..." : desc;
        }

        @Override
        public List<com.sarthi.dto.reports.RailPadVendorWiseQualityDto> getRailPadVendorWiseQualityReport(
                        String startDate, String endDate) {
                LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate)
                                : LocalDate.now().minusDays(30);
                LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

                List<Object[]> rows = railIEProductionVerificationRepository.getRailPadVendorWiseQualityReport(start,
                                end);
                List<com.sarthi.dto.reports.RailPadVendorWiseQualityDto> result = new ArrayList<>();

                for (Object[] row : rows) {
                        String companyName = row[0] != null ? row[0].toString() : "";
                        String plantName = row[1] != null ? row[1].toString() : "";
                        long inspected = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                        long accepted = row[3] != null ? ((Number) row[3]).longValue() : 0L;
                        long rejected = row[4] != null ? ((Number) row[4]).longValue() : 0L;

                        String manufacture = companyName;
                        if (!plantName.isEmpty() && !companyName.contains(plantName)) {
                                manufacture = companyName + " - " + plantName;
                        } else if (!plantName.isEmpty()) {
                                manufacture = plantName;
                        }

                        String rejectionPercent = "0.00%";
                        if (inspected > 0) {
                                double pct = ((double) rejected / inspected) * 100.0;
                                rejectionPercent = String.format("%.2f%%", pct);
                        }

                        result.add(new com.sarthi.dto.reports.RailPadVendorWiseQualityDto(
                                        manufacture, inspected, accepted, rejected, rejectionPercent));
                }

                return result;
        }

        @Override
        public com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO> getRailPadPerformanceReport(
                        int page,
                        int size,
                        java.time.LocalDate startDate,
                        java.time.LocalDate endDate,
                        String rio,
                        String zone,
                        String vendor) {

                String zoneParam = (zone != null && !zone.trim().isEmpty() && !zone.equalsIgnoreCase("all"))
                                ? zone.trim()
                                : null;
                String vendorParam = (vendor != null && !vendor.trim().isEmpty() && !vendor.equalsIgnoreCase("all"))
                                ? vendor.trim()
                                : null;
                String rioParam = (rio != null && !rio.trim().isEmpty() && !rio.equalsIgnoreCase("all")) ? rio.trim()
                                : null;

                List<Object[]> processRows = railIEProductionVerificationRepository.fetchProcessPerformance(startDate,
                                endDate, rioParam, zoneParam, vendorParam);
                List<Object[]> finalRows = railFinalInspectionLotResultsRepository.fetchFinalPerformance(startDate,
                                endDate, rioParam, zoneParam, vendorParam);

                List<com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO> allList = new ArrayList<>();

                if (processRows != null) {
                        for (Object[] row : processRows) {
                                com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO dto = new com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO();
                                dto.setManufacturerName(row[0] != null ? row[0].toString() : "");
                                dto.setRio(row[1] != null ? row[1].toString() : "N/A");
                                dto.setUsername(row[2] != null ? row[2].toString() : "N/A");
                                dto.setStage(row[3] != null ? row[3].toString() : "PROCESS");
                                double inspected = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
                                double accepted = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
                                double rejected = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
                                dto.setInspectedQty(inspected);
                                dto.setAcceptedQty(accepted);
                                dto.setRejectedQty(rejected);
                                if (inspected > 0) {
                                        dto.setRejectionPercentage((rejected * 100.0) / inspected);
                                } else {
                                        dto.setRejectionPercentage(0.0);
                                }
                                allList.add(dto);
                        }
                }

                if (finalRows != null) {
                        for (Object[] row : finalRows) {
                                com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO dto = new com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO();
                                dto.setManufacturerName(row[0] != null ? row[0].toString() : "");
                                dto.setRio(row[1] != null ? row[1].toString() : "N/A");
                                dto.setUsername(row[2] != null ? row[2].toString() : "N/A");
                                dto.setStage(row[3] != null ? row[3].toString() : "FINAL");
                                double inspected = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
                                double accepted = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
                                double rejected = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
                                dto.setInspectedQty(inspected);
                                dto.setAcceptedQty(accepted);
                                dto.setRejectedQty(rejected);
                                if (inspected > 0) {
                                        dto.setRejectionPercentage((rejected * 100.0) / inspected);
                                } else {
                                        dto.setRejectionPercentage(0.0);
                                }
                                allList.add(dto);
                        }
                }

                // In-memory pagination
                int totalElements = allList.size();
                int totalPages = (int) Math.ceil((double) totalElements / size);
                if (totalPages == 0)
                        totalPages = 1;

                int startIdx = page * size;
                int endIdx = Math.min(startIdx + size, totalElements);
                List<com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO> content = new ArrayList<>();
                if (startIdx < totalElements) {
                        content = allList.subList(startIdx, endIdx);
                }

                com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO> response = new com.sarthi.dto.summaryDtos.PageResponseDTO<>();
                response.setContent(content);
                response.setPage(page);
                response.setSize(size);
                response.setTotalElements(totalElements);
                response.setTotalPages(totalPages);
                response.setLast(page >= totalPages - 1);

                return response;
        }

        @Override
        public TotalCallsSummaryDTO getTotalCallsSummary(String vendorPlantCode, String zonalRailway, String startDate,
                        String endDate) {

                String parsedStartDate = (startDate == null || startDate.isEmpty()) ? null : startDate;
                String parsedEndDate = (endDate == null || endDate.isEmpty()) ? null : endDate + " 23:59:59";
                String parsedVendor = vendorPlantCode == null ? "" : vendorPlantCode;
                String parsedZone = zonalRailway == null ? "" : zonalRailway;

                // Run all 3 count queries in PARALLEL
                CompletableFuture<Long> cfOpen = CompletableFuture.supplyAsync(() ->
                                workflowTransitionRepository.getTotalOpenCallsWithFilters(parsedStartDate, parsedEndDate, parsedVendor, parsedZone));

                CompletableFuture<Long> cfUnder = CompletableFuture.supplyAsync(() ->
                                workflowTransitionRepository.getTotalUnderInspectionCallsWithFilters(parsedStartDate, parsedEndDate, parsedVendor, parsedZone));

                CompletableFuture<Long> cfPending = CompletableFuture.supplyAsync(() ->
                                workflowTransitionRepository.getTotalPendingCallsWithFilters(parsedStartDate, parsedEndDate, parsedVendor, parsedZone));

                CompletableFuture.allOf(cfOpen, cfUnder, cfPending).join();

                return new TotalCallsSummaryDTO(cfOpen.join(), cfUnder.join(), cfPending.join());
        }


        @Override
        public List<InspectionCallDetailDto> getUnderInspectionCalls(String vendorPlantCode, String zonalRailway,
                        String startDate, String endDate) {
                String vCode = (vendorPlantCode == null || vendorPlantCode.trim().isEmpty()) ? null : vendorPlantCode;
                String zCode = (zonalRailway == null || zonalRailway.trim().isEmpty()) ? null : zonalRailway;
                String sDate = (startDate == null || startDate.trim().isEmpty()) ? null : startDate;
                String eDate = (endDate == null || endDate.trim().isEmpty()) ? null : endDate + " 23:59:59";

                List<Object[]> results = workflowTransitionRepository.getUnderInspectionCalls(vCode, zCode, sDate,
                                eDate);

                return results.stream()
                                .map(this::convertToInspectionCallDto)
                                .toList();
        }

        @Override
        public List<InspectionCallDetailDto> getPendingCalls(String vendorPlantCode, String zonalRailway,
                        String startDate, String endDate) {
                String vCode = (vendorPlantCode == null || vendorPlantCode.trim().isEmpty()) ? null : vendorPlantCode;
                String zCode = (zonalRailway == null || zonalRailway.trim().isEmpty()) ? null : zonalRailway;
                String sDate = (startDate == null || startDate.trim().isEmpty()) ? null : startDate;
                String eDate = (endDate == null || endDate.trim().isEmpty()) ? null : endDate + " 23:59:59";

                List<Object[]> results = workflowTransitionRepository.getPendingCalls(vCode, zCode, sDate, eDate);

                return results.stream()
                                .map(this::convertToInspectionCallDto)
                                .toList();
        }

        @Override
        public List<InspectionCallDetailDto> getOpenCalls(String vendorPlantCode, String zonalRailway, String startDate,
                        String endDate) {
                String vCode = (vendorPlantCode == null || vendorPlantCode.trim().isEmpty()) ? null : vendorPlantCode;
                String zCode = (zonalRailway == null || zonalRailway.trim().isEmpty()) ? null : zonalRailway;
                String sDate = (startDate == null || startDate.trim().isEmpty()) ? null : startDate;
                String eDate = (endDate == null || endDate.trim().isEmpty()) ? null : endDate + " 23:59:59";

                List<Object[]> results = workflowTransitionRepository.getOpenCalls(vCode, zCode, sDate, eDate);

                return results.stream()
                                .map(this::convertToInspectionCallDto)
                                .toList();
        }

        private InspectionCallDetailDto convertToInspectionCallDto(Object[] row) {

                String status = row[6] != null ? row[6].toString() : "";

                return InspectionCallDetailDto.builder()
                                .inspectionCallNumber((String) row[0])
                                .vendor((String) row[1])
                                .callSubmissionDateTime((String) row[2])
                                .stageOfInspection(determineStage((String) row[0]))
                                .poSrNo((String) row[4])
                                .dpDate((String) row[5])
                                .status(status)
                                .mainStatus(getMainStatus(status))
                                .subStatus(getSubStatus(status))
                                .build();
        }

        private String determineStage(String inspectionCallNumber) {

                if (inspectionCallNumber == null) {
                        return "-";
                }

                if (inspectionCallNumber.startsWith("ER")) {
                        return "RM Stage";
                }

                if (inspectionCallNumber.startsWith("EP")) {
                        return "Process Stage";
                }

                if (inspectionCallNumber.startsWith("EF")) {
                        return "Final Stage";
                }

                return "-";
        }

        private String getMainStatus(String status) {

                return switch (status) {

                        case "Created",
                                        "VERIFIED",
                                        "RETURNED",
                                        "CALL_REGISTERED",
                                        "IE_SCHEDULED",
                                        "INITIATE_INSPECTION",
                                        "REQUEST_CORRECTION_TO_CM" ->
                                "Pending";

                        case "VERIFY_PO_DETAILS",
                                        "PAUSE_INSPECTION_RESUME_NEXT_DAY",
                                        "ENTER_SHIFT_DETAILS_AND_START_INSPECTION",
                                        "WITHHELD" ->
                                "Under Inspection";

                        default -> "Completed";
                };
        }

        private String getSubStatus(String status) {

                return switch (status) {

                        case "Created" -> "Call Raised";
                        case "VERIFIED", "CALL_REGISTERED" -> "Call Registered";
                        case "RETURNED" -> "Returned To Vendor";
                        case "IE_SCHEDULED" -> "Call Scheduled";
                        case "INITIATE_INSPECTION" -> "Call Initiated";
                        case "VERIFY_PO_DETAILS" -> "Inspection Started";
                        case "PAUSE_INSPECTION_RESUME_NEXT_DAY" -> "Paused For Next Schedule";
                        case "ENTER_SHIFT_DETAILS_AND_START_INSPECTION" -> "Under Inspection";
                        case "INSPECTION_COMPLETE_CONFIRM" -> "IC Issuance Pending";
                        case "GENERATE_IC", "DSC_SIGN_IC" -> "IC Issued";
                        case "CANCELLED" -> "Cancelled";
                        case "WITHHELD" -> "Withheld";

                        default -> status;
                };
        }

        @Override
        public String getRegionByCallNo(String callNo) {
            String requestId = callNo;
            if (callNo != null && callNo.contains("/")) {
                String[] parts = callNo.split("/");
                if (parts.length >= 2) {
                    requestId = parts[1];
                }
            }
            String rio = workflowTransitionRepository.findRioByCallNoAndStatusCreated(requestId);
            String regionName = "RITES LIMITED, NORTHERN REGION, DELHI";
            if (rio != null) {
                switch(rio.toUpperCase()) {
                    case "NRIO": regionName = "RITES LIMITED, NORTHERN REGION, DELHI"; break;
                    case "WRIO": regionName = "RITES LIMITED, WESTERN REGION, MUMBAI"; break;
                    case "SRIO": regionName = "RITES LIMITED, SOUTHEN REGION, CHENNAI"; break;
                    case "ERIO": regionName = "RITES LIMITED, EASTERN REGION, KOLKATA"; break;
                    case "CRIO": regionName = "RITES LIMITED, CENTRAL REGION, BHILAI"; break;
                }
            }
            return regionName;
        }
}
