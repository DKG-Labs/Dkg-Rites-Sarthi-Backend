package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthTestRequestDto;
import com.sarthi.Sleeper.service.WaterCubeSampleService;
import com.sarthi.Sleeper.service.WaterCubeStrengthTestService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/water-cube-sample")
public class WaterCubeSampleController {

    @Autowired
    private WaterCubeSampleService service;

    @Autowired
    private WaterCubeStrengthTestService waterCubeStrengthTestService;

    // ================= CREATE =================
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody WaterCubeSampleRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.create(dto)),
                HttpStatus.OK);
    }

    // ================= UPDATE =================
    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody WaterCubeSampleRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                HttpStatus.OK);
    }

    // ================= GET BY ID =================
    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK);
    }

    // ================= GET ALL =================
    @GetMapping("/getAll")
    public ResponseEntity<Object> getAll() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAll()),
                HttpStatus.OK);
    }

    // ================= GET BY USER =================
    @GetMapping("/getByUser/{userId}")
    public ResponseEntity<Object> getByUser(@PathVariable Long userId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getByUser(userId)),
                HttpStatus.OK);
    }

    // ================= SAVE TEST RESULTS =================
    @PostMapping("/save-test-result")
    public ResponseEntity<?> saveTestResult(@RequestBody WaterCubeStrengthTestRequestDto requestDto) {
        return waterCubeStrengthTestService.saveTestResult(requestDto);
    }

    @GetMapping("/test-results/user/{userId}")
    public ResponseEntity<?> getTestResultsByUser(@PathVariable Long userId) {
        return waterCubeStrengthTestService.getTestResultsByUser(userId);
    }

    // ================= DELETE =================
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Water Cube Sample Declaration Deleted Successfully"),
                HttpStatus.OK);
    }
}
