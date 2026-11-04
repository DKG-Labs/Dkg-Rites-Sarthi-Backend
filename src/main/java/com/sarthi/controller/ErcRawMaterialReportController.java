package com.sarthi.controller;

import com.sarthi.service.ercreport.ErcRawMaterialReportService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Railway Board: ERC raw material status per unit (procured, inspected, processed). */
@RestController
@RequestMapping("/api/reports")
public class ErcRawMaterialReportController {

    private final ErcRawMaterialReportService service;

    public ErcRawMaterialReportController(ErcRawMaterialReportService service) {
        this.service = service;
    }

    @GetMapping("/erc-raw-material-status")
    public ResponseEntity<Object> getErcRawMaterialStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(service.getReport(fromDate, toDate)), HttpStatus.OK);
    }
}
