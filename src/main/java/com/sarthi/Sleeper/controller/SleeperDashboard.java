package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.BatchDTO;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.CompanyDTO;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.Level4BatchDTO;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.PlantDTO;
import com.sarthi.Sleeper.service.DashboardService;
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
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getMpr(start, end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/manufacturer-performance/{plantId}")
    public ResponseEntity<Object>  getPerformance(
            @RequestParam String plantId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getLastYearPerformance(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/process-defect-distribution")
    public ResponseEntity<Object>  getProcessDefectDistribution(
            @RequestParam String plantId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getProcessDefectDistribution(plantId)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/defect-distribution-analysis")
    public ResponseEntity<Object> getDefectDistribution(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getDefectReasonDistribution(start,end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/pareto-analysis")
    public ResponseEntity<Object> getParetoAnalysis(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        dashboardService.getParetoAnalysis(start,end)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/employee-wise-performance")
    public ResponseEntity<Object> getEmployeeWisePerformance(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);

        LocalDate end = LocalDate.parse(endDate, formatter);

        return new ResponseEntity<>(

                ResponseBuilder.getSuccessResponse(
                        dashboardService.getEmployeePerformance(start, end)

                ),

                HttpStatus.OK
        );
    }

    @GetMapping("/shift-wise-production")
    public ResponseEntity<Object> getShiftWiseProductionReport(
            @RequestParam String startDate,
            @RequestParam String endDate, @RequestParam String plantId) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);

        LocalDate end = LocalDate.parse(endDate, formatter);

        return new ResponseEntity<>(

                ResponseBuilder.getSuccessResponse(
                       dashboardService.getReport(start, end, plantId)
                ),

                HttpStatus.OK
        );
    }

    @GetMapping("/qtyOfPSCSleepers")
    public ResponseEntity<Object> getQtyOfPscSleeperReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate start = LocalDate.parse(startDate, formatter);

        LocalDate end = LocalDate.parse(endDate, formatter);

        return new ResponseEntity<>(

                ResponseBuilder.getSuccessResponse(
                        dashboardService.getQtyPscSleeperReport(start, end)
                ),

                HttpStatus.OK
        );
    }

}
