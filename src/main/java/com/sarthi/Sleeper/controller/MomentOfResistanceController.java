package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.MomentOfResistanceRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceResponseDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceTestResponseDTO;
import com.sarthi.Sleeper.service.MomentOfResistanceService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moment-of-resistance")
public class MomentOfResistanceController {

    @Autowired
    private MomentOfResistanceService service;

    /* ================= CREATE ================= */

    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody MomentOfResistanceRequestDTO requestDTO) {

        MomentOfResistanceResponseDTO response =
                service.create(requestDTO);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }

    /* ================= GET BY ID ================= */

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        MomentOfResistanceResponseDTO response =
                service.getById(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }

    /* ================= UPDATE ================= */

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody MomentOfResistanceRequestDTO requestDTO) {

        MomentOfResistanceResponseDTO response =
                service.update(id, requestDTO);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }

    /* ================= GET ALL ================= */

    @GetMapping("/all")
    public ResponseEntity<Object> getAll() {

        List<MomentOfResistanceResponseDTO> list =
                service.getAll();

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }

    /* ================= DELETE ================= */

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        service.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Record deleted successfully"),
                HttpStatus.OK
        );
    }

    @GetMapping("/mrTodayRecord")
    public ResponseEntity<Object> getTodayRecords(
            @RequestParam String plantId,
            @RequestParam String vendorCode,
            @RequestParam String shift,
            @RequestParam int createdBy,
            @RequestParam String date) {

        List<MomentOfResistanceResponseDTO> list =
                service.getRecordsByDate(plantId, vendorCode, shift, createdBy, date);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }
}
