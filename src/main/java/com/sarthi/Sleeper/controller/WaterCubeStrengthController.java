package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthRequestDto;
import com.sarthi.Sleeper.service.WaterCubeStrengthService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/water-cube-strength")
@CrossOrigin("*")
public class WaterCubeStrengthController {

    @Autowired
    private WaterCubeStrengthService service;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody WaterCubeStrengthRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.create(dto)),
                HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") Long id, @RequestBody WaterCubeStrengthRequestDto dto) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                HttpStatus.OK);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK);
    }

    @GetMapping("/getByUser/{userId}")
    public ResponseEntity<Object> getByUser(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getByUser(userId)),
                HttpStatus.OK);
    }

    @GetMapping("/getByDeclaration/{declarationId}")
    public ResponseEntity<Object> getByDeclaration(@PathVariable("declarationId") Long declarationId) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getByDeclaration(declarationId)),
                HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK);
    }
}
