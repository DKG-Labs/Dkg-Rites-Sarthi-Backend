package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorTestResultDto;
import com.sarthi.Sleeper.service.MorSampleService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mor-sample")
@CrossOrigin("*")
public class MorSampleController {

    @Autowired
    private MorSampleService service;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody MorSampleRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.create(dto)),
                HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody MorSampleRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                HttpStatus.OK);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK);
    }

    @GetMapping("/getAll/{userId}")
    public ResponseEntity<Object> getAll(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAll(userId)),
                HttpStatus.OK);
    }

    @GetMapping("/getHistorical/{userId}")
    public ResponseEntity<Object> getHistorical(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getHistorical(userId)),
                HttpStatus.OK);
    }

    @GetMapping("/getPending/{userId}")
    public ResponseEntity<Object> getPending(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getPendingMorDeclarations(userId)),
                HttpStatus.OK);
    }

    @PostMapping("/saveResults/{id}")
    public ResponseEntity<Object> saveResults(@PathVariable("id") Long id, @RequestBody List<MorTestResultDto> results) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.saveTestResults(id, results)),
                HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK);
    }
}
