package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.RawMaterialSourceRequestDto;
import com.sarthi.Sleeper.dto.RawMaterialSourceResponseDto;
import com.sarthi.Sleeper.service.RawMaterialSourceService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/raw-material-source")

public class RawMaterialSourceController {

    @Autowired
    private RawMaterialSourceService service;
    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody RawMaterialSourceRequestDto dto) {

        RawMaterialSourceResponseDto result =  service.create(dto);
        String requestId = String.valueOf(result.getId());
        Long md = 3L;
        Long wid = 1L;
        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(result.getCreatedBy()), result.getVendorCode(),result.getPlantId());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody RawMaterialSourceRequestDto dto) {

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
