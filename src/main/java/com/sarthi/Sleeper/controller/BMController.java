package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMRequestDTO;
import com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos.BMResponseDTO;
import com.sarthi.Sleeper.service.BMService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bench-mould-stress-longline")
public class BMController {

    @Autowired
    private BMService bmService;
    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;


    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody BMRequestDTO dto) {

        BMResponseDTO result = bmService.create(dto);
        String requestId = String.valueOf(result.getId());
        Long md = 2L;
        Long wid = 1L;
        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(result.getCreatedBy()));

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody BMRequestDTO dto) {

        BMResponseDTO result = bmService.update(id, dto);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK);
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {

        BMResponseDTO result = bmService.getById(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK);
    }


    @GetMapping("/getAll")
    public ResponseEntity<Object> getAll() {

        List<BMResponseDTO> result = bmService.getAll();

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {

        bmService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted successfully"),
                HttpStatus.OK);
    }
}