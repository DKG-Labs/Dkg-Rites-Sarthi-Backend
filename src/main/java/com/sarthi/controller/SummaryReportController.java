package com.sarthi.controller;

import com.sarthi.dto.summaryDtos.LotWiseClosedLoopDTO;
import com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO;
import com.sarthi.dto.summaryDtos.MonthlyAnalysisDTO;
import com.sarthi.service.SummaryService;
import com.sarthi.dto.summaryDtos.PageResponseDTO;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/SummaryReports")
public class SummaryReportController {

    @Autowired
    private SummaryService summaryService;

    @GetMapping("/dashboard")
    public APIResponse getDashboard(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String rio,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String vendor) {

        PageResponseDTO<ManufacturerInspectionSummaryDTO> data = summaryService.getDashboard(page, size, startDate,
                endDate, rio, zone, vendor);

        return ResponseBuilder.getSuccessResponse(data);
    }

    @GetMapping("/monthly-progress")
    public APIResponse getMonthlyProgress(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String rio,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String vendor) {

        return ResponseBuilder.getSuccessResponse(
                summaryService.getMonthlyProgress(page, size, startDate, endDate, rio, zone, vendor));
    }

    @GetMapping("/Manufature_wise_analysis")
    public APIResponse getMonthlyManufatureWiseAnalysis(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String rio,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String vendor) {

        return ResponseBuilder.getSuccessResponse(
                summaryService.getMonthlyAnalysis(page, size, startDate, endDate, rio, zone, vendor));
    }

    @GetMapping("/lot-closed-loop")
    public ResponseEntity<?> getLotClosedLoop(
            @RequestParam String callNo,
            @RequestParam String lotNo) {

        List<LotWiseClosedLoopDTO> response = summaryService.getClosedLoop(callNo, lotNo);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/lot-numbers")
    public ResponseEntity<?> getLotNumbers(
            @RequestParam String requestId) {

        List<String> data = summaryService.getLotNumbers(requestId);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/request-ids")
    public ResponseEntity<?> getRequestIds(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                summaryService.getRequestIds(startDate, endDate));
    }

    @GetMapping("/company-month-wise")
    public APIResponse getCompanyMonthWiseData(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String companyName) {

        return ResponseBuilder.getSuccessResponse(
                summaryService.getComapanyWiseMonthlyAnalysis(page, size, startDate, endDate, companyName));
    }

}
