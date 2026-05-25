package com.sarthi.controller;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.dto.reports.FourthLevelInspectionDto;
import com.sarthi.dto.reports.PoInspection1stLevelStatusDto;
import com.sarthi.dto.reports.PoInspection3rdLevelCallStatusDto;
import com.sarthi.dto.reports.PoIssuedDetailDto;
import com.sarthi.dto.summaryDtos.PoWiseDefectsData;
import com.sarthi.service.reports;
import com.sarthi.util.ResponseBuilder;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class reportsController {
    @Autowired
    private reports reportService;

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
        List<PoWiseDefectsData> list = reportService.getPoWiseDefectsReport(startDate,endDate);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/dashboardSummary")
    public ResponseEntity<Object> getDashboardSummary() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getDashboardSummary()),
                HttpStatus.OK);
    }


    @GetMapping("/process/ic-numbers/{userId}")
    public ResponseEntity<List<String>> getIcNumbers(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getProcessIcNumbersByUserId(userId));
    }

    @GetMapping("/avgProductionPerDay")
    public ResponseEntity<Object> getAvgProductionPerDay() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getAvgProductionPerDay()),
                HttpStatus.OK);
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
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getDailyRejectionTrend(startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/manufacturingStepWiseRejection")
    public ResponseEntity<Object> getManufacturingStepWiseRejection() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getManufacturingStepWiseRejection()),
                HttpStatus.OK);
    }

    @GetMapping("/inspectionCallStatus")
    public ResponseEntity<Object> getInspectionCallStatus() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getInspectionCallStatus()),
                HttpStatus.OK);
    }

    @GetMapping("/paretoAnalysis")
    public ResponseEntity<Object> getParetoAnalysis(@RequestParam(required = false) String startDate,
                                                    @RequestParam(required = false) String endDate,
                                                    @RequestParam(required = false) String product) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getParetoAnalysis(startDate, endDate, product)),
                HttpStatus.OK);
    }

    @GetMapping("/inspectionDetails")
    public ResponseEntity<Object> getInspectionDetails(@RequestParam(required = false) String startDate,
                                                        @RequestParam(required = false) String endDate) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getInspectionDetails(startDate, endDate)),
                HttpStatus.OK);
    }

    @GetMapping("/monthlyRejectionTrend")
    public ResponseEntity<Object> getMonthlyRejectionTrend(@RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate,
                                                          @RequestParam(required = false) String product) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getMonthlyRejectionTrend(startDate, endDate, product)),
                HttpStatus.OK);
    }

    @GetMapping("/sleeperPoIssuedCount")
    public ResponseEntity<Object> getSleeperPoIssuedCount() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getSleeperPoCount()),
                HttpStatus.OK);
    }

    @GetMapping("/poIssuedDetails")
    public ResponseEntity<Object> getPoIssuedDetails(@RequestParam String itemCatDescr) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getPoIssuedDetails(itemCatDescr)),
                HttpStatus.OK);
    }

    @GetMapping("/inspectionCallStatusDetails")
    public ResponseEntity<Object> getInspectionCallStatusDetails(
            @RequestParam String stage, 
            @RequestParam String status) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(reportService.getInspectionCallStatusDetails(stage, status)),
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
                ResponseBuilder.getSuccessResponse(reportService.getRailPadShiftWiseProductionReport(startDate, endDate, vendor, plant)),
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
}

