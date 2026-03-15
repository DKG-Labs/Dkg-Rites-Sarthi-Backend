package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.*;
import com.sarthi.Sleeper.service.MorSampleService;
import com.sarthi.Sleeper.service.ProductionFinalInspectionService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/FinalInspectionController")
public class FinalProductionInspectionController {

    @Autowired
    private ProductionFinalInspectionService inspectionService;

    @Autowired
    private MorSampleService service;

    @PostMapping("/save")
    public ResponseEntity<String> saveInspection(
            @RequestBody InspectionSaveRequestDto dto) {

        inspectionService.saveInspection(dto);

        return ResponseEntity.ok("Inspection Saved Successfully");
    }
    @GetMapping("/inspection/batches")
    public List<BatchTestingListResponseDto> getAllBatches() {
        return inspectionService.getAllBatchTesting();
    }

    @GetMapping("/inspection/batch/{batchId}")
    public BatchInspectionDetailDto getBatchInspection(
            @PathVariable Long batchId) {

        return inspectionService.getBatchInspection(batchId);
    }





        @PostMapping
        public ResponseEntity<Object> create(
                @RequestBody MorSampleRequestDto dto) {

            MorSampleResponseDto result = service.create(dto);


            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(result),
                    HttpStatus.OK
            );
        }

        // ================= UPDATE =================

        @PutMapping("/{id}")
        public ResponseEntity<Object> update(
                @PathVariable Long id,
                @RequestBody MorSampleRequestDto dto) {

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

        @GetMapping("/getAll/{userId}")
        public ResponseEntity<Object> getAll(@PathVariable("userId") Long userId) {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.getAll(userId)),
                    HttpStatus.OK
            );
        }

        // ================= DELETE =================

        @DeleteMapping("/{id}")
        public ResponseEntity<Object> delete(
                @PathVariable Long id) {

            service.delete(id);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("MOR Sample Deleted Successfully"),
                    HttpStatus.OK
            );
        }


}
