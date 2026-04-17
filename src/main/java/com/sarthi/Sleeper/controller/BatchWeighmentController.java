package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.BatchIdNumberDto;
import com.sarthi.Sleeper.dto.BatchWeighmentDtos.BatchWeighmentRequestDto;
import com.sarthi.Sleeper.dto.BatchWeighmentDtos.BatchWeighmentResponseDto;
import com.sarthi.Sleeper.dto.DemouldingInspectionResponseDTO;
import com.sarthi.Sleeper.service.BatchWeighmentService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/batch-weighment")
@RequiredArgsConstructor
public class BatchWeighmentController {

    @Autowired
    private final BatchWeighmentService batchWeighmentService;


    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody BatchWeighmentRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        batchWeighmentService.create(dto)),
                HttpStatus.OK);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody BatchWeighmentRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        batchWeighmentService.update(id, dto)),
                HttpStatus.OK);
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        batchWeighmentService.getById(id)),
                HttpStatus.OK);
    }


    @GetMapping("/get-all")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        batchWeighmentService.getAll()),
                HttpStatus.OK);
    }



    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        batchWeighmentService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Deleted Successfully"),
                HttpStatus.OK);
    }


    @GetMapping("/batchWeighmentData")
    public ResponseEntity<Object> batchWeighmentByDate(  @RequestParam String plantId,
                                                          @RequestParam String vendorCode,
                                                          @RequestParam String shift,
                                                          @RequestParam int createdBy, @RequestParam String date) {

        List<BatchWeighmentResponseDto> list =
                batchWeighmentService.getRecordsByDate(plantId,vendorCode, shift, createdBy, date);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }

    @GetMapping("/batchNosForCompaction")
    public ResponseEntity<Object> getAllBatchs(@RequestParam LocalDate
                                                           entryDate,@RequestParam String location) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        batchWeighmentService.getBatchIdsAndNumbers(
        entryDate,location)),
                HttpStatus.OK);
    }
}