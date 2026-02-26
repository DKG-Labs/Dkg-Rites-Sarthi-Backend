package com.sarthi.controller;

import com.sarthi.dto.summaryDtos.ManufacturerInspectionSummaryDTO;
import com.sarthi.dto.summaryDtos.PageResponseDTO;
import com.sarthi.service.SummaryService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PageResponseDTO<ManufacturerInspectionSummaryDTO> data =
                summaryService.getDashboard(page, size, startDate, endDate);

        return ResponseBuilder.getSuccessResponse(data);
    }


    @GetMapping("/monthly-progress")
    public APIResponse getMonthlyProgress(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseBuilder.getSuccessResponse(
                summaryService.getMonthlyProgress(page, size, startDate, endDate));
    }
}
