package com.sarthi.controller;

import com.sarthi.dto.RemapDetailsDto;
import com.sarthi.dto.RemapSubmitDto;
import com.sarthi.service.WorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/call-desk")
public class CallDeskRemapController {

    @Autowired
    private WorkflowService workflowService;

    @GetMapping("/remap-details")
    public ResponseEntity<Object> getRemapDetails(@RequestParam String callNo, @RequestParam String stage) {
        try {
            RemapDetailsDto details = workflowService.getRemapDetails(callNo, stage);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(details),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @GetMapping("/remap-poi-details")
    public ResponseEntity<Object> getRemapPoiDetails(@RequestParam String callNo) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(workflowService.getRemapPoiDetails(callNo)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/remap-assigned-user")
    public ResponseEntity<Object> getRemapAssignedUser(@RequestParam String callNo, @RequestParam String stage, @RequestParam String poiCode) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(workflowService.getRemapAssignedUser(callNo, stage, poiCode)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/remap-available-employees")
    public ResponseEntity<Object> getRemapAvailableEmployees(@RequestParam String stage) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(workflowService.getRemapAvailableEmployees(stage)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @PostMapping("/remap-submit")
    public ResponseEntity<Object> submitRemap(@RequestBody RemapSubmitDto dto) {
        try {
            workflowService.submitRemap(dto);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("Remapping successful"),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            // Known business validation errors – return 400 with clean message
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", "Remapping failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
