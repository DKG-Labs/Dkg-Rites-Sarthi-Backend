package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallInspectionHeaderRequest;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallInspectionHeaderResponse;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallRequestDto;
import com.sarthi.Sleeper.dto.FinalCalDtos.FinalCallResponseDto;
import com.sarthi.Sleeper.service.FinalCallService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/MainIe-finalcallsleeperInspection")
@RequiredArgsConstructor
public class FinalCallController {

    private final FinalCallService service;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody FinalCallRequestDto request) {

        FinalCallResponseDto result = service.create(request);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.CREATED
        );

    }
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(

            @RequestBody FinalCallRequestDto request) {

        FinalCallResponseDto result = service.update(request);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    // ================= GET BY CALL NO =================
    @GetMapping("/call-no/{callNo}")
    public ResponseEntity<Object> getByCallNo(@PathVariable String callNo) {

        List<FinalCallResponseDto> result = service.getByCallNo(callNo);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    @PostMapping("/finalCallHeader/save")
    public ResponseEntity<Object> createHeader(
            @RequestBody FinalCallInspectionHeaderRequest request) {

        FinalCallInspectionHeaderResponse result = service.create(request);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.CREATED
        );
    }

    // ================= UPDATE (BY CALL NO) =================
    @PutMapping("/finalCallHeader/update")
    public ResponseEntity<Object> updateHeader(
            @RequestBody FinalCallInspectionHeaderRequest request) {

        FinalCallInspectionHeaderResponse result = service.update(request);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    // ================= GET BY CALL NO =================
    @GetMapping("/finalCallHeader/{callNo}")
    public ResponseEntity<Object> getHeaderByCallNo(@PathVariable String callNo) {

        FinalCallInspectionHeaderResponse result = service.getHeaderByCallNo(callNo);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    // ================= SAVE FINAL RESULT (HEADER & BATCHES) =================
    @PostMapping("/saveFinalResult")
    public ResponseEntity<Object> saveFinalResult(
            @RequestBody com.sarthi.Sleeper.dto.FinalCalDtos.SleeperFinalResultRequestDto request) {

        com.sarthi.Sleeper.entity.FInalCall.SleeperFinalResult result = service.saveOrUpdateSleeperFinalResult(request);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    // ================= GET FINAL RESULT BY CALL NO =================
    @GetMapping("/getFinalResult/{callNo}")
    public ResponseEntity<Object> getFinalResult(@PathVariable String callNo) {

        com.sarthi.Sleeper.entity.FInalCall.SleeperFinalResult result = service.getSleeperFinalResult(callNo);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }
}
