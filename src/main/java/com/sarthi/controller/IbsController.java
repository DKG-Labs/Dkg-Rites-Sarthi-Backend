package com.sarthi.controller;


import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.service.IbsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class IbsController {

    @Autowired
    private IbsService service;

    @PostMapping("/sarthi/authenticate")
    public ResponseEntity<AuthResponseDto> generateIntegrationToken(
            @RequestBody AuthRequestDto request) {

        return ResponseEntity.ok(
                service.integrationLogin(request)
        );
    }
}
