package com.sarthi.Sleeper.controller;


import com.sarthi.Sleeper.dto.MouldPreparationRequestDTO;
import com.sarthi.Sleeper.dto.MouldPreparationResponseDTO;
import com.sarthi.Sleeper.service.MouldPreparationService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/MouldPreparation")
public class MouldPreparationController {

    @Autowired
    private MouldPreparationService mouldPreparationService;

    @PostMapping("/create")
    public ResponseEntity<Object> createMouldPreparation(
            @RequestBody MouldPreparationRequestDTO requestDTO) {

        MouldPreparationResponseDTO response =
                mouldPreparationService.create(requestDTO);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        MouldPreparationResponseDTO response =
                mouldPreparationService.getById(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }



    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateMouldPreparation(
            @PathVariable Long id,
            @RequestBody MouldPreparationRequestDTO requestDTO) {

        MouldPreparationResponseDTO response =
                mouldPreparationService.update(id, requestDTO);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll() {

        List<MouldPreparationResponseDTO> list =
                mouldPreparationService.getAll();

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        mouldPreparationService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Record deleted successfully"),
                HttpStatus.OK
        );
    }


}
