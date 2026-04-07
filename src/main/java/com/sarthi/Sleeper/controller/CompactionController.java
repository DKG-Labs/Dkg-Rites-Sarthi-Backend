package com.sarthi.Sleeper.controller;


import com.sarthi.Sleeper.dto.CompactionDtos.CompactionRequestDto;
import com.sarthi.Sleeper.dto.CompactionDtos.CompactionResponseDto;
import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningResponseDto;
import com.sarthi.Sleeper.service.CompactionService;

import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compaction")

public class CompactionController {

    @Autowired
    private CompactionService compactionService;



    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody CompactionRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        compactionService.create(dto)),
                HttpStatus.OK);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody CompactionRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        compactionService.update(id, dto)),
                HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        compactionService.getById(id)),
                HttpStatus.OK);
    }


    @GetMapping("/getAll")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        compactionService.getAll()),
                HttpStatus.OK);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        compactionService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Deleted Successfully"),
                HttpStatus.OK);
    }


    @GetMapping("/compactionData")
    public ResponseEntity<Object> compactionData(  @RequestParam String plantId,
                                                       @RequestParam String vendorCode,
                                                       @RequestParam String shift,
                                                       @RequestParam int createdBy, @RequestParam String date) {

        List<CompactionResponseDto> list =
                compactionService.getRecordsByDate(plantId,vendorCode, shift, createdBy, date);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }
}