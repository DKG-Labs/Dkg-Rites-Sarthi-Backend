package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.Cement.CementReceiptRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementReceiptResponseDto;
import com.sarthi.Sleeper.service.CementService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cement")
public class CementReceiptController {

    @Autowired
    private CementService service;
    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody CementReceiptRequestDto dto) {
        CementReceiptResponseDto result =  service.create(dto);
        String requestId = String.valueOf(result.getId());
        Long md = 6L;
        Long wid = 1L;
        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(result.getCreatedBy()));


        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody CementReceiptRequestDto dto) {


        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAll()),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "cement Deleted Successfully"),
                HttpStatus.OK);
    }

}
