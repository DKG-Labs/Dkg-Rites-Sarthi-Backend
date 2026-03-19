package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.BatchInspectionResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.*;
import com.sarthi.Sleeper.service.MorSampleService;
import com.sarthi.Sleeper.service.ProductionFinalInspectionService;
import com.sarthi.Sleeper.service.SleeperInspectionCallService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
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

    @Autowired
    private SleeperInspectionCallService sleeperInspectionCallService;
    
    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;

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
            @PathVariable("batchId") Long batchId) {

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
                @PathVariable("id") Long id,
                @RequestBody MorSampleRequestDto dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                    HttpStatus.OK
            );
        }

        // ================= GET BY ID =================

        @GetMapping("/{id}")
        public ResponseEntity<Object> getById(
                @PathVariable("id") Long id) {

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
                @PathVariable("id") Long id) {

            service.delete(id);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("MOR Sample Deleted Successfully"),
                    HttpStatus.OK
            );
        }

    @GetMapping("/completed-batches")
    public ResponseEntity<Object> getCompletedBatches(
            @RequestParam("sleeperType") String sleeperType,
            @RequestParam("userId") String userId) {

        List<BatchInspectionResponseDto>  result=  inspectionService.getCompletedBatches(sleeperType, userId);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    @PostMapping("/submit-inspection-call")
    public ResponseEntity<Object> submitInspectionCall(@RequestBody SleeperInspectionCallSubmitDto submitDto) {
        String callNo = sleeperInspectionCallService.submitInspectionCall(submitDto);
        String requestId = callNo;
        Long md = 0L;
        Long wid = 2L;
        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(submitDto.getCreatedBy()));

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(callNo),
                HttpStatus.OK
        );
    }
    @GetMapping("/inspection-calls")
    public ResponseEntity<Object> getInspectionCalls(@RequestParam("userId") Long userId) {
        List<SleeperInspectionCallListDto> calls = sleeperInspectionCallService.getVendorInspectionCalls(userId);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(calls),
                HttpStatus.OK
        );
    }

}
