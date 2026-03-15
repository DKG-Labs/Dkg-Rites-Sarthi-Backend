package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.MorTestRequestDto;
import com.sarthi.Sleeper.service.MorTestService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mor-test")
@RequiredArgsConstructor
public class MorTestController {

    private final MorTestService service;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody MorTestRequestDto dto){

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.create(dto)),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody MorTestRequestDto dto){

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id,dto)),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id){

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<Object> getAll(){

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAll()),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id){

        service.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK
        );
    }
}
