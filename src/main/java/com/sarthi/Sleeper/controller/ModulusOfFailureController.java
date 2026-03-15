package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.ModulusOfFailureRequestDto;
import com.sarthi.Sleeper.dto.ModulusOfFailureResponseDto;
import com.sarthi.Sleeper.service.ModulusOfFailureService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modulus-of-failure")
@RequiredArgsConstructor
public class ModulusOfFailureController {

    private final ModulusOfFailureService service;


    // ================= CREATE =================

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody ModulusOfFailureRequestDto dto) {

        ModulusOfFailureResponseDto result = service.create(dto);



        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }


    // ================= UPDATE =================

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody ModulusOfFailureRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                HttpStatus.OK
        );
    }


    // ================= GET BY ID =================

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK
        );
    }


    // ================= GET ALL =================

    @GetMapping
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAll()),
                HttpStatus.OK
        );
    }


    // ================= DELETE =================

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        service.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Modulus Of Failure record deleted successfully"),
                HttpStatus.OK
        );
    }
}