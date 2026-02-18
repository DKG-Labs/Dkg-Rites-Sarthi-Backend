package com.sarthi.Sleeper.controller;


import com.sarthi.Sleeper.dto.HtsWirePlacementRequestDTO;
import com.sarthi.Sleeper.dto.HtsWirePlacementResponseDTO;
import com.sarthi.Sleeper.service.HtsWirePlacementService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/HtsWirePlacement")
public class HtsWirePlacementController {

    @Autowired
    private HtsWirePlacementService htsWirePlacementService;


    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody HtsWirePlacementRequestDTO requestDTO) {

        HtsWirePlacementResponseDTO response =
                htsWirePlacementService.create(requestDTO);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        HtsWirePlacementResponseDTO response =
                htsWirePlacementService.getById(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }


    @GetMapping("/all")
    public ResponseEntity<Object> getAll() {

        List<HtsWirePlacementResponseDTO> list = htsWirePlacementService.getAll();

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }



    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody HtsWirePlacementRequestDTO requestDTO) {

        HtsWirePlacementResponseDTO response =
                htsWirePlacementService.update(id, requestDTO);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        htsWirePlacementService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Record deleted successfully"),
                HttpStatus.OK
        );
    }

}
