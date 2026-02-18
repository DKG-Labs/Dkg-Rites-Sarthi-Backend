package com.sarthi.Sleeper.controller;


import com.sarthi.Sleeper.dto.DemouldingInspectionRequestDTO;
import com.sarthi.Sleeper.dto.DemouldingInspectionResponseDTO;
import com.sarthi.Sleeper.service.DemouldingInspectionService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/DemouldingInspection")
public class DemouldingInspectionController {
    @Autowired
    private DemouldingInspectionService demouldingInspectionService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody DemouldingInspectionRequestDTO dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(demouldingInspectionService.create(dto)),
                HttpStatus.OK);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody DemouldingInspectionRequestDTO dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        demouldingInspectionService.update(id, dto)),
                HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {
        DemouldingInspectionResponseDTO list = demouldingInspectionService.getById(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK);
    }


    @GetMapping("/all")
    public ResponseEntity<Object> getAll() {
       List<DemouldingInspectionResponseDTO> list = demouldingInspectionService.getAll();

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
        HttpStatus.OK);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        demouldingInspectionService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Deleted Successfully"),
                HttpStatus.OK);
    }


}
