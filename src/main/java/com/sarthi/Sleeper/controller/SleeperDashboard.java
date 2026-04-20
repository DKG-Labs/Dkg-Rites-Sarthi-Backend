package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.service.DashboardService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
