package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.Cement.Cement7DayStrengthRequestDto;
import com.sarthi.Sleeper.dto.Cement.Cement7DayStrengthResponseDto;
import com.sarthi.Sleeper.service.Cement7DayStrengthService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cement-7-day-strength")
public class Cement7DayStrengthController {

    @Autowired
    private Cement7DayStrengthService service;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Cement7DayStrengthRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.create(dto)),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Cement7DayStrengthRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
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
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK
        );
    }
}
