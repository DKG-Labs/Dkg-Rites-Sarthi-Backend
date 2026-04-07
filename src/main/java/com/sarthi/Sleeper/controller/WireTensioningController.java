package com.sarthi.Sleeper.controller;



import com.sarthi.Sleeper.dto.BatchWeighmentDtos.BatchWeighmentResponseDto;
import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningRequestDto;


import com.sarthi.Sleeper.dto.WireTensioningDtos.WireTensioningResponseDto;
import com.sarthi.Sleeper.service.wireTensioningService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wire-tensioning")
@RequiredArgsConstructor
public class WireTensioningController {

    @Autowired
    private wireTensioningService wiretensioningService;


    // ================= CREATE =================

    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody WireTensioningRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        wiretensioningService.create(dto)),
                HttpStatus.OK);
    }


    // ================= UPDATE =================

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody WireTensioningRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        wiretensioningService.update(id, dto)),
                HttpStatus.OK);
    }


    // ================= GET BY ID =================

    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        wiretensioningService.getById(id)),
                HttpStatus.OK);
    }


    // ================= GET ALL =================

    @GetMapping("/get-all")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        wiretensioningService.getAll()),
                HttpStatus.OK);
    }


    // ================= DELETE =================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        wiretensioningService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Deleted Successfully"),
                HttpStatus.OK);
    }


    @GetMapping("/wireTensioningData")
    public ResponseEntity<Object> wireTensioningData(  @RequestParam String plantId,
                                                         @RequestParam String vendorCode,
                                                         @RequestParam String shift,
                                                         @RequestParam int createdBy, @RequestParam String date) {

        List<WireTensioningResponseDto> list =
                wiretensioningService.getRecordsByDate(plantId,vendorCode, shift, createdBy, date);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }

}