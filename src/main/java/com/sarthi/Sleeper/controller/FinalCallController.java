package com.sarthi.Sleeper.controller;

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
@RequestMapping("/api/finalcall-sleeperInspection")
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

}
