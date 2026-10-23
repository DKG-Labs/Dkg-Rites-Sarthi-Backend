package com.sarthi.Sleeper.controller;


import com.sarthi.Sleeper.dto.MoistureAnalysisListDTO;
import com.sarthi.Sleeper.dto.MoistureAnalysisRequestDTO;
import com.sarthi.Sleeper.dto.MoistureAnalysisResponseDTO;
import com.sarthi.Sleeper.dto.MouldPreparationResponseDTO;
import com.sarthi.Sleeper.service.MoistureAnalysisEntryService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/MoistureAnalysis")
public class MoistureAnalysisEntryController {
    @Autowired
    private MoistureAnalysisEntryService moistureAnalysisEntryService;

        @PostMapping("/create")
        public ResponseEntity<Object> create(
                @RequestBody MoistureAnalysisRequestDTO dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            moistureAnalysisEntryService.create(dto)),
                    HttpStatus.OK);
        }

        @GetMapping("/all")
        public ResponseEntity<Object> getAll() {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                          moistureAnalysisEntryService.getAll()),
                    HttpStatus.OK);
        }

        @PutMapping("/update/{id}")
        public ResponseEntity<Object> update(
                @PathVariable Long id,
                @RequestBody MoistureAnalysisRequestDTO dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                           moistureAnalysisEntryService.update(id, dto)),
                    HttpStatus.OK);
        }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        MoistureAnalysisResponseDTO response =
               moistureAnalysisEntryService.getById(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }
    @GetMapping("/lastFiveMoisture")
    public ResponseEntity<Object> getLastFiveMoisture() {

        List<MoistureAnalysisListDTO> response =
                 moistureAnalysisEntryService.getLastFiveMoistureRecords();

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(response),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id){

        moistureAnalysisEntryService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK
        );
    }

}
