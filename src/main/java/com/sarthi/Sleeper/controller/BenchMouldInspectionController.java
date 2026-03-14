package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.BenchMouldDtos.BenchMouldInspectionRequestDto;
import com.sarthi.Sleeper.dto.BenchMouldDtos.BenchMouldInspectionResponseDto;
import com.sarthi.Sleeper.service.BenchMouldInspectionService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bench-mould-inspection")
public class BenchMouldInspectionController {

    @Autowired
    private BenchMouldInspectionService benchMouldInspectionService;

    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;
    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody BenchMouldInspectionRequestDto dto) {

        BenchMouldInspectionResponseDto result = benchMouldInspectionService.create(dto);
        String requestId = String.valueOf(result.getId());
//        Long md = 2L;
//        Long wid = 1L;
//        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, (long) result.getCreatedBy());

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK);
    }



    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody BenchMouldInspectionRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        benchMouldInspectionService.update(id, dto)),
                HttpStatus.OK);
    }



    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        benchMouldInspectionService.getById(id)),
                HttpStatus.OK);
    }



    @GetMapping("/get-all")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        benchMouldInspectionService.getAll()),
                HttpStatus.OK);
    }



    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        benchMouldInspectionService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Bench & Mould Inspection Deleted Successfully"),
                HttpStatus.OK);
    }
}
