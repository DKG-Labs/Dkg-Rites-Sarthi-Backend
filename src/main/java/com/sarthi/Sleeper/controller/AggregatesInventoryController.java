package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.Aggregates.AggregatesRequestDto;

import com.sarthi.Sleeper.dto.Aggregates.AggregatesResponseDto;
import com.sarthi.Sleeper.service.AggregatesService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aggregates")
public class AggregatesInventoryController {

    @Autowired
    private AggregatesService service;
    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;

    // ================= CREATE =================

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody AggregatesRequestDto dto) {

        AggregatesResponseDto result = service.create(dto);
        String requestId = String.valueOf(result.getId());
        Long md = 1L;
        Long wid = 1L;
        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(result.getCreatedBy()));

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }


    // ================= UPDATE =================

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody AggregatesRequestDto dto) {

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
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK
        );
    }
}