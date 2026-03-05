package com.sarthi.controller;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.dto.reports.FourthLevelInspectionDto;
import com.sarthi.dto.reports.PoInspection1stLevelStatusDto;
import com.sarthi.dto.reports.PoInspection3rdLevelCallStatusDto;
import com.sarthi.service.reports;
import com.sarthi.util.ResponseBuilder;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/dashboardSummary")
    public ResponseEntity<Object> getDashboardSummary() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(reportService.getDashboardSummary()),
                HttpStatus.OK);
    }

    @GetMapping("/process/ic-numbers/{userId}")
    public ResponseEntity<List<String>> getIcNumbers(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getProcessIcNumbersByUserId(userId));
    }
}
