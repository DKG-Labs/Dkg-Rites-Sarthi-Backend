package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.EtDtos.EpoxyTreatedSleeperRequestDTO;
import com.sarthi.Sleeper.dto.EtDtos.EpoxyTreatedSleeperResponseDTO;
import com.sarthi.Sleeper.service.EpoxyTreatedSleeperService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/et")
public class EpoxyTreatedSleeperController {

    @Autowired
    private EpoxyTreatedSleeperService service;

    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;



    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody EpoxyTreatedSleeperRequestDTO dto) {

        EpoxyTreatedSleeperResponseDTO result = service.create(dto);


        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody EpoxyTreatedSleeperRequestDTO dto) {

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
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        service.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK
        );
    }

    @GetMapping("/batch-summary")
    public ResponseEntity<Object> getBatchSummary() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        service.getAllBatchWiseEtSummary()
                ),
                HttpStatus.OK
        );
    }

}
