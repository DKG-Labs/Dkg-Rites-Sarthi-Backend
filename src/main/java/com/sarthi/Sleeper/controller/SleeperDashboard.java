package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.*;
import com.sarthi.Sleeper.service.DashboardService;
import com.sarthi.dto.reports.IeOperationalSlaPerformanceSummaryDto;
import com.sarthi.dto.reports.IeWiseCallStatusWorkloadSummaryDto;
import com.sarthi.dto.reports.InspectionCallsReportDto;
import com.sarthi.util.CommonUtils;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/sleeper-dashboard")
public class SleeperDashboard {

    @Autowired
    private DashboardService  dashboardService;

    @GetMapping("/demoulding-process-rejected-count")
    public ResponseEntity<Object> getRejectedCount() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getRejectedSleepersCount()),
                HttpStatus.OK
        );
    }

    @GetMapping("/final-inspection-call-status-counts")
    public ResponseEntity<Object> getFinalInspectionCallStatusCounts() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getFinalInspectionCallStatusCounts()),
                HttpStatus.OK
        );
    }


    @GetMapping("/Final-inspection-rejected-count")
    public ResponseEntity<Object> getFinalInspectionRejectedCount() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getTotalRejectedCount()),
                HttpStatus.OK
        );
    }

    @GetMapping("/rejection-percentage")
    public ResponseEntity<Object> getRejectionPercentage() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(dashboardService.getRejectionPercentage()),
                HttpStatus.OK
        );
    }

    @GetMapping("/monthly-analysis")
    public ResponseEntity<Object> getMonthlyAnalysis(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getMonthlyAnalysis(startDate, endDate)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/sleeper-powise-monthly-analysis")
    public ResponseEntity<Object> getPoWiseMonthlyAnalysis(
            @RequestParam String plantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getPoWiseAnalysis(
                               plantId,
                               startDate,
                                endDate)
                ),
                HttpStatus.OK
        );
    }



    @GetMapping("/LifeCycle/LotWise")
    public ResponseEntity<Object> getLifeCycleLotWise(
            @RequestParam Long id,
            @RequestParam String batchId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLifecycleReport(id, batchId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/companies")
    public ResponseEntity<Object> getCompanies() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getCompanies()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/plants")
    public ResponseEntity<Object> getPlants(@RequestParam String vendorCode) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getPlants(vendorCode)
                ),
                HttpStatus.OK
        );
    }
    @GetMapping("/batches")
    public ResponseEntity<Object> getBatches(@RequestParam String plantId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getBatches(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level5BatchInspectionData")
    public ResponseEntity<Object> getLevel5api(   @RequestParam Long id,
                                                  @RequestParam String batchId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getBatchChecking(batchId, id)
                ),
                HttpStatus.OK
        );
    }


    @GetMapping("/level4")
    public  ResponseEntity<Object> getLevel4(@RequestParam String callNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel4Report(callNo)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level3")
    public  ResponseEntity<Object> getLevel3(@RequestParam String poNo,
                                                @RequestParam String srNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel3Report(poNo,srNo)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level2")
    public  ResponseEntity<Object> getLevel2(@RequestParam String poNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel2(poNo)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/level1")
    public ResponseEntity<Object> getLevel1(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLevel1(
                                start,
                                end
                        )
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/mpr")
    public ResponseEntity<Object> getMpr(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getMpr(start, end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/manufacturer-performance/{plantId}")
    public ResponseEntity<Object>  getPerformance(
            @PathVariable(required = false) String plantId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLastYearPerformance(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/process-defect-distribution")
    public ResponseEntity<Object>  getProcessDefectDistribution(
            @RequestParam(required = false) String plantId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getProcessDefectDistribution(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/defect-distribution-analysis")
    public ResponseEntity<Object> getDefectDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getDefectReasonDistribution(start,end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/pareto-analysis")
    public ResponseEntity<Object> getParetoAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getParetoAnalysis(start,end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/employee-wise-performance")
    public ResponseEntity<Object> getEmployeeWisePerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getEmployeePerformance(start, end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/shift-wise-production")
    public ResponseEntity<Object> getShiftWiseProductionReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String plantId) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                       dashboardService.getReport(start, end, plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/qtyOfPSCSleepers")
    public ResponseEntity<Object> getQtyOfPscSleeperReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = CommonUtils.parseDateFlexible(startDate, LocalDate.now().minusMonths(1));
        LocalDate end = CommonUtils.parseDateFlexible(endDate, LocalDate.now());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getQtyPscSleeperReport(start, end)
                ),
                HttpStatus.OK
        );
    }


    /**
     * Get distinct company names from vendor_plant table
     * for Sleeper Shift Wise Production Report manufacturer dropdown
     */
    @GetMapping("/vendor-plant/companies")
    public ResponseEntity<Object> getVendorPlantCompanies() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getVendorPlantCompanyNames()
                ),
                HttpStatus.OK
        );
    }

    /**
     * Get plants by company name from vendor_plant table
     * for Sleeper Shift Wise Production Report plant dropdown
     */
    @GetMapping("/vendor-plant/plants")
    public ResponseEntity<Object> getVendorPlantsByCompany(
            @RequestParam String companyName) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getVendorPlantsByCompanyName(companyName)
                ),
                HttpStatus.OK
        );
    }


    @GetMapping("/cm-inspection-calls")
    public ResponseEntity<Object> getInspectionCallsReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<InspectionCallsReportDto>  list = dashboardService.getInspectionCallsReport(startDate, endDate);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm-sleeper-overduecalls")
    public ResponseEntity<Object> getCmSleeperOverDueCalls(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<InspectionCallsReportDto>  list = dashboardService.getSleeperOverduePendingCallsReport(startDate, endDate);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm-sleeper-ie-callWiseStatus")
    public ResponseEntity<Object> getCmSleeperieCallWiseStatus(
            @RequestParam(required = false) String cmEmplId) {

        List<IeWiseCallStatusWorkloadSummaryDto>  list = dashboardService.getSleeperIeWiseCallStatusWorkloadSummary(cmEmplId);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }


    @GetMapping("/cm-sleeper-IECompletedCalls")
    public ResponseEntity<Object> getCmSleeperIeCompletedCalls(
            @RequestParam(required = false) String cmEmplId) {

        List<IeOperationalSlaPerformanceSummaryDto>  list = dashboardService.getSleeperIeOperationalSlaPerformanceSummary(cmEmplId);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/quality-sleeper")
    public ResponseEntity<Object> getQualitySleeper(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<QualitySleeperReportDto>  list = dashboardService.getQualitySleeperReport(startDate,endDate);

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("sleeperIc/{callNo}")
    public SleeperIcProjection getSleeperIcData(
            @PathVariable String callNo) {

        return dashboardService.getSleeperIcData(callNo);
    }


}
