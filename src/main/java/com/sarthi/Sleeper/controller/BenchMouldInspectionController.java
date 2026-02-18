package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.BenchMouldDtos.BenchMouldInspectionRequestDto;
import com.sarthi.Sleeper.service.BenchMouldInspectionService;
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
    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody BenchMouldInspectionRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        benchMouldInspectionService.create(dto)),
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
