package com.sarthi.controller;

import com.sarthi.dto.NearestPlantResponseDto;
import com.sarthi.service.NearestPlantService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nearest-plant")
public class NearestPlantController {

    @Autowired
    private NearestPlantService nearestPlantService;

    /**
     * Returns the nearest mapped plant(s) to the Process IE's last login location.
     *
     * @param userId the user_id from sarthi_authenticator_verifications
     */
    @GetMapping
    public ResponseEntity<Object> getNearestPlant(@RequestParam String userId) {
        NearestPlantResponseDto result = nearestPlantService.findNearestPlant(userId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }
}
