package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.MomentOfResistanceTestRequestDTO;
import com.sarthi.Sleeper.dto.MomentOfResistanceTestResponseDTO;
import com.sarthi.Sleeper.service.MomentOfResistanceTestService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mr-testing")
public class MomentOfResistanceTestController {

        @Autowired
        private MomentOfResistanceTestService service;

        @PostMapping("/create")
        public ResponseEntity<Object> create(
                @RequestBody MomentOfResistanceTestRequestDTO dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.create(dto)),
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

        @PutMapping("/update/{id}")
        public ResponseEntity<Object> update(
                @PathVariable Long id,
                @RequestBody MomentOfResistanceTestRequestDTO dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                    HttpStatus.OK
            );
        }

        @GetMapping("/all")
        public ResponseEntity<Object> getAll() {

            List<MomentOfResistanceTestResponseDTO> list = service.getAll();

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(list),
                    HttpStatus.OK
            );
        }

        @DeleteMapping("/delete/{id}")
        public ResponseEntity<Object> delete(@PathVariable Long id) {

            service.delete(id);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("Deleted successfully"),
                    HttpStatus.OK
            );
        }

    @GetMapping("/mrTestTodayRecord")
    public ResponseEntity<Object> getTodayRecords(
            @RequestParam String plantId,
            @RequestParam String vendorCode,
            @RequestParam String shift,
            @RequestParam int createdBy,
            @RequestParam String date) {

        List<MomentOfResistanceTestResponseDTO> list =
                service.getRecordsByDate(plantId, vendorCode, shift, createdBy, date);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }

}
