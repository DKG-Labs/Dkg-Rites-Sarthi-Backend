package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.BatchDTO;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.CompanyDTO;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.PlantDTO;
import com.sarthi.Sleeper.service.DashboardService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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





}
