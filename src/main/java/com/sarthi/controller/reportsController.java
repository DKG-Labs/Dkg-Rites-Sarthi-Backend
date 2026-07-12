package com.sarthi.controller;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;

import com.sarthi.dto.reports.FourthLevelInspectionDto;
import com.sarthi.dto.reports.PoInspection1stLevelStatusDto;
import com.sarthi.dto.reports.PoInspection3rdLevelCallStatusDto;
import com.sarthi.dto.reports.PoIssuedDetailDto;
import com.sarthi.dto.reports.IcAnnexuresReportDto;

import com.sarthi.dto.reports.*;

import com.sarthi.dto.summaryDtos.PoWiseDefectsData;
import com.sarthi.service.reports;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.exception.ErrorDetails;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.sarthi.repository.WorkflowTransitionRepository;
import com.sarthi.dto.reports.IcIssuedCountDto;

@RestController
@RequestMapping("/api/reports")
public class reportsController {
    @Autowired
    private reports reportService;

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;

    @GetMapping("/icIssuedCounts")
    public ResponseEntity<Object> getIcIssuedCounts(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> result = workflowTransitionRepository.getIcIssuedCounts(
                (vendorPlantCode != null && vendorPlantCode.trim().isEmpty()) ? null : vendorPlantCode,
                (zonalRailway != null && zonalRailway.trim().isEmpty()) ? null : zonalRailway,
                (startDate != null && startDate.trim().isEmpty()) ? null : startDate,
                (endDate != null && !endDate.trim().isEmpty()) ? endDate + " 23:59:59" : null);

        IcIssuedCountDto dto = new IcIssuedCountDto();
        if (result != null) {
            long rmCount = result.get("rmCount") != null ? ((Number) result.get("rmCount")).longValue() : 0;
            long processCount = result.get("processCount") != null ? ((Number) result.get("processCount")).longValue()
                    : 0;
            long finalCount = result.get("finalCount") != null ? ((Number) result.get("finalCount")).longValue() : 0;
            dto.setRmCount(rmCount);
            dto.setProcessCount(processCount);
            dto.setFinalCount(finalCount);
            dto.setTotal(rmCount + processCount + finalCount);
        }
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto), HttpStatus.OK);
    }

    @GetMapping("/1stLevelReportPoData")
    public ResponseEntity<Object> get1stLevelReportPoData() {
        List<PoInspection1stLevelStatusDto> list = reportService.getPoInspection1stLevelStatusList();
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/2ndLevelReportPoSerialData/{poNumber}")
    public ResponseEntity<Object> getClustersByRegion(@PathVariable String poNumber) {
        List<PoInspection2ndLevelSerialStatusDto> list = reportService.getSerialStatusByPoNo(poNumber);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/3rdLevelReportICData/{poNo}/{callNo}")
    public ResponseEntity<Object> getInspectionDataBasedOnSerialNo(@PathVariable String poNo,
            @PathVariable String callNo) {
        List<PoInspection3rdLevelCallStatusDto> list = reportService.getCallWiseStatusBy(poNo, callNo);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/3rdLevelReportICData")
    public ResponseEntity<Object> getInspectionDataBasedOnSerialNo(

            @RequestParam String callNo,
            @RequestParam String poNo,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        Page<PoInspection3rdLevelCallStatusDto> result = reportService.getCallWiseStatusBySerialNo(poNo, callNo, page,
                size);
        ;

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK);
    }

    @GetMapping("/4thLevelReportICData/{callNo}")
    public ResponseEntity<Object> getProcessDataCallWise(@PathVariable String callNo) {
        List<FourthLevelInspectionDto> list = reportService.getFourthLevelReport(callNo);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/poWise")
    public ResponseEntity<Object> getPoWise(@RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        List<PoWiseDefectsData> list = reportService.getPoWiseDefectsReport(startDate, endDate);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/newPoWise")
    public ResponseEntity<Object> getNewPoWise(@RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(
                reportService.getPoInspectionTracking(page, size, startDate, endDate)), HttpStatus.OK);
    }

    @GetMapping("/dashboardSummary")
    public ResponseEntity<Object> getDashboardSummary(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getDashboardSummary(vendorPlantCode, zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/railpad-final-accepted-rejected")
    public ResponseEntity<Object> getRailPadFinalAcceptedRejected() {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadFinalInspectionSummary()),
                HttpStatus.OK);
    }

    @GetMapping("/railpad/1stLevelReportPoData")
    public ResponseEntity<Object> getRailPad1stLevelReportPoData() {
        List<RailPadPoLifeCycle1stLevelDto> list = reportService.getRailPadPo1stLevelStatus();
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/railpad/2ndLevelReportPoSerialData/{poNo}")
    public ResponseEntity<Object> getRailPad2ndLevelReportPoSerialData(@PathVariable String poNo) {
        List<RailPadPoLifeCycle2ndLevelDto> list = reportService.getRailPadPo2ndLevelStatus(poNo);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/railpad/3rdLevelReportICData/{poNo}/{serialNo}")
    public ResponseEntity<Object> getRailPad3rdLevelReportICData(@PathVariable String poNo,
            @PathVariable String serialNo) {
        List<RailPadPoLifeCycle3rdLevelDto> list = reportService.getRailPadPo3rdLevelStatus(poNo, serialNo);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/railpad/monthly-progress")
    public ResponseEntity<Object> getRailPadMonthlyProgress(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false) String rio,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String vendor) {

        if (endDate == null) {
            endDate = java.time.LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusMonths(6);
        }

        com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMprReportDto> result = reportService
                .getRailPadMprReport(page, size, startDate, endDate, rio, zone, vendor);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }

    @GetMapping("/railpad/monthly-analysis")
    public ResponseEntity<Object> getRailPadMonthlyAnalysis(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false) String rio,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String vendor) {

        if (endDate == null) {
            endDate = java.time.LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusMonths(6);
        }

        com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.reports.RailPadMauReportDto> result = reportService
                .getRailPadMauReport(page, size, startDate, endDate, rio, zone, vendor);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }

    @GetMapping("/railpad/closed-loop/manufacturers")
    public ResponseEntity<Object> getRailPadClosedLoopManufacturers() {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadClosedLoopManufacturers()),
                HttpStatus.OK);
    }

    @GetMapping("/railpad/closed-loop/plants")
    public ResponseEntity<Object> getRailPadClosedLoopPlants(@RequestParam String vendorCode) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadClosedLoopPlants(vendorCode)),
                HttpStatus.OK);
    }

    @GetMapping("/railpad/closed-loop/lots")
    public ResponseEntity<Object> getRailPadClosedLoopLots(@RequestParam String plantId, @RequestParam int year) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadClosedLoopLots(plantId, year)),
                HttpStatus.OK);
    }

    @GetMapping("/railpad/closed-loop/details/{lotId}")
    public ResponseEntity<Object> getRailPadClosedLoopDetails(@PathVariable Long lotId) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadLotClosedLoopDetails(lotId)),
                HttpStatus.OK);
    }

    @GetMapping("/process/ic-numbers/{userId}")
    public ResponseEntity<List<String>> getIcNumbers(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getProcessIcNumbersByUserId(userId));
    }

    @GetMapping("/avgProductionPerDay")
    public ResponseEntity<Object> getAvgProductionPerDay(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate != null && endDate != null) {
            return new ResponseEntity<Object>(
                    ResponseBuilder.getSuccessResponse(reportService.getAvgProductionPerDayWithFilters(startDate,
                            endDate, vendorPlantCode, zonalRailway)),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Object>(
                    ResponseBuilder.getSuccessResponse(reportService.getAvgProductionPerDay()),
                    HttpStatus.OK);
        }
    }

    @GetMapping("/qualityRejection")
    public ResponseEntity<Object> getQualityRejection() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getStageWiseRejection()),
                HttpStatus.OK);
    }

    @GetMapping("/manufacturerRejection")
    public ResponseEntity<Object> getManufacturerRejection() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getManufacturerRejection()),
                HttpStatus.OK);
    }

    @GetMapping("/processPerformance")
    public ResponseEntity<Object> getProcessPerformance() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getProcessPerformance()),
                HttpStatus.OK);
    }

    @GetMapping("/dailyRejectionTrend")
    public ResponseEntity<Object> getDailyRejectionTrend(@RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getDailyRejectionTrend(startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/manufacturingStepWiseRejection")
    public ResponseEntity<Object> getManufacturingStepWiseRejection() {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getManufacturingStepWiseRejection()),
                HttpStatus.OK);
    }

    @GetMapping("/inspectionCallStatus")
    public ResponseEntity<Object> getInspectionCallStatus(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getInspectionCallStatus(vendorPlantCode, zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/paretoAnalysis")
    public ResponseEntity<Object> getParetoAnalysis(@RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String product) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getParetoAnalysis(startDate, endDate, product)),
                HttpStatus.OK);
    }

    @GetMapping("/inspectionDetails")
    public ResponseEntity<Object> getInspectionDetails(@RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getInspectionDetails(startDate, endDate, vendorPlantCode, zonalRailway)),
                HttpStatus.OK);
    }

    @GetMapping("/monthlyRejectionTrend")
    public ResponseEntity<Object> getMonthlyRejectionTrend(@RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String product) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getMonthlyRejectionTrend(startDate, endDate, product)),
                HttpStatus.OK);
    }

    @GetMapping("/sleeperPoIssuedCount")
    public ResponseEntity<Object> getSleeperPoIssuedCount() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getSleeperPoCount()),
                HttpStatus.OK);
    }

    @GetMapping("/poIssuedDetails")
    public ResponseEntity<Object> getPoIssuedDetails(
            @RequestParam String itemCatDescr,
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getPoIssuedDetails(itemCatDescr, vendorPlantCode,
                        zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/inspectionCallStatusDetails")
    public ResponseEntity<Object> getInspectionCallStatusDetails(
            @RequestParam String stage,
            @RequestParam String status,
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getInspectionCallStatusDetails(stage, status,
                        vendorPlantCode, zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/railPadInspectionCallStatusDetails")
    public ResponseEntity<Object> getRailPadInspectionCallStatusDetails(
            @RequestParam String status) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadInspectionCallStatusDetails(status)),
                HttpStatus.OK);
    }

    @GetMapping("/sqcReport")
    public ResponseEntity<Object> getSqcReport() {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getSqcReport()),
                HttpStatus.OK);
    }

    @GetMapping("/railPadShiftWiseProduction")
    public ResponseEntity<Object> getRailPadShiftWiseProduction(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) String plant) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getRailPadShiftWiseProductionReport(startDate, endDate, vendor, plant)),
                HttpStatus.OK);
    }

    @GetMapping("/railPadManufacturers")
    public ResponseEntity<Object> getRailPadManufacturers() {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadDistinctVendors()),
                HttpStatus.OK);
    }

    @GetMapping("/railPadPlaces")
    public ResponseEntity<Object> getRailPadPlaces(@RequestParam(required = false) String vendor) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadDistinctPlants(vendor)),
                HttpStatus.OK);
    }

    @GetMapping("/railPadVendorWiseQuality")
    public ResponseEntity<Object> getRailPadVendorWiseQuality(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadVendorWiseQualityReport(startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/railPadQualityReport")
    public ResponseEntity<Object> getRailPadQualityReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getRailPadQualityReport(startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/companies")
    public ResponseEntity<Object> getAllCompanies() {
        List<String> list = reportService.getAllCompanies();
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/downloadIcAnnexures")
    public ResponseEntity<Object> getDownloadIcAnnexuresReport(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate
    ) {
        try {
            List<IcAnnexuresReportDto> list = reportService.getDownloadIcAnnexuresReport(product, vendorPlantCode, zonalRailway, startDate, endDate);
            return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorDetails errorDetails = new ErrorDetails(
                    500,
                    500,
                    "ERROR",
                    "Error: " + e.getMessage() + " | Type: " + e.getClass().getName());
            return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cm-inspection-calls")
    public ResponseEntity<Object> getInspectionCallsReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<InspectionCallsReportDto> list = reportService.getInspectionCallsReport(startDate, endDate);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm-erc-overduecalls")
    public ResponseEntity<Object> getCmOverDueErcCalls(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<InspectionCallsReportDto> list = reportService.getOverduePendingInspectionCallsReport(startDate, endDate);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm-ie-wise-callStatus")
    public ResponseEntity<Object> getCmIeWiseCallStatus(@RequestParam(required = false) String cmEmpId) {

        List<IeWiseCallStatusWorkloadSummaryDto> list = reportService.getIeWiseCallStatusWorkloadSummary(cmEmpId);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm-completed-calls-analysis")
    public ResponseEntity<Object> getCmIeCompletedCalls(@RequestParam(required = false) String cmEmpId) {

        List<IeOperationalSlaPerformanceSummaryDto> list = reportService.getIeOperationalSlaPerformanceSummary(cmEmpId);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);

    }

    @GetMapping("/railpad/performance")
    public ResponseEntity<Object> getRailPadPerformance(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false) String rio,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String vendor) {

        try {
            if (endDate == null) {
                endDate = java.time.LocalDate.now();
            }
            if (startDate == null) {
                startDate = endDate.minusMonths(6);
            }

            com.sarthi.dto.summaryDtos.PageResponseDTO<com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO> result = reportService
                    .getRailPadPerformanceReport(page, size, startDate, endDate, rio, zone, vendor);
            return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorDetails errorDetails = new ErrorDetails(
                    500,
                    500,
                    "ERROR",
                    "Error fetching Rail Pad performance matrix: " + e.getMessage());
            return new ResponseEntity<Object>(ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ercDashboardTotalCalls")
    public ResponseEntity<Object> getTotalCallsDashboardSummary(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getTotalCallsSummary(vendorPlantCode, zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/ercDashboardOpenCalls")
    public ResponseEntity<Object> getOpenCalls(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getOpenCalls(vendorPlantCode, zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/ercDashboardUnderInspectionCalls")
    public ResponseEntity<Object> getUnderInspectionCalls(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getUnderInspectionCalls(vendorPlantCode, zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/ercDashboardPendingCalls")
    public ResponseEntity<Object> getPendingCalls(
            @RequestParam(required = false) String vendorPlantCode,
            @RequestParam(required = false) String zonalRailway,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        reportService.getPendingCalls(vendorPlantCode, zonalRailway, startDate, endDate)),
                HttpStatus.OK);
    }

}
