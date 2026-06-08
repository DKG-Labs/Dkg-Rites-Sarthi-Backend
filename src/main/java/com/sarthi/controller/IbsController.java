package com.sarthi.controller;


import com.sarthi.dto.IBS.IbsAcknowledgementDto;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.service.IbsService;

import com.sarthi.service.JwtService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class IbsController {

    @Autowired
    private IbsService service;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/sarthi/authenticate")
    public ResponseEntity<AuthResponseDto> generateIntegrationToken(
            @RequestBody AuthRequestDto request) {

        return ResponseEntity.ok(
                service.integrationLogin(request)
        );
    }

    @GetMapping("/ibs/call-registration-inspection-data")
    public ResponseEntity<Object> getCallData(
            @RequestHeader(value = "Authorization",
                    required = false) String authHeader) {

      jwtService.validateToken(authHeader);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        service.getAllGeneratedIcCalls()
                ),
                HttpStatus.OK
        );
    }

    @PostMapping("/ibs/call-data/acknowledgement")
    public ResponseEntity<Object> acknowledgeCallData(
            @RequestHeader(value = "Authorization",
                    required = false) String authHeader,  @RequestBody IbsAcknowledgementDto dto
    ) {

        jwtService.validateToken(authHeader);
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(
                        service.acknowledgeCallData(dto)
                ),
                HttpStatus.OK
        );
    }


}
