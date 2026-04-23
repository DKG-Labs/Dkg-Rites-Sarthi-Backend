package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.service.MainIeInspectionService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/main-ie")
public class MainIeInspectionController {
    @Autowired
    private MainIeInspectionService mainIeInspectionService;

    @GetMapping("/inspection-call-summary/{callNo}")
    public ResponseEntity<Object> getInspectionCallSummary(
            @PathVariable String callNo) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        mainIeInspectionService.getInspectionCallSummary(callNo)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/inspection-call/batch-wise/{callNo}")
    public ResponseEntity<Object> getBatchWiseDetails(
            @PathVariable String callNo) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        mainIeInspectionService.getBatchWiseDetails(callNo)
                ),
                HttpStatus.OK
        );
    }
}
