package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailRheometerTestDto;
import com.sarthi.SRailPad.service.ieVerification.RailRheometerTestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/railpad-workflow/rheometer")
public class RailRheometerTestController {

    @Autowired
    private RailRheometerTestService service;

    @PostMapping("/create")
    public ResponseEntity<?> createRheometerTest(@Valid @RequestBody RailRheometerTestDto dto) {
        try {
            RailRheometerTestDto created = service.createRheometerTest(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRheometerTest(@PathVariable Long id, @Valid @RequestBody RailRheometerTestDto dto) {
        try {
            RailRheometerTestDto updated = service.updateRheometerTest(id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getRheometerTest(@PathVariable Long id) {
        try {
            RailRheometerTestDto dto = service.getRheometerTestById(id);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getRheometerTestList(
            @RequestParam String plantId,
            @RequestParam String vendorCode) {
        try {
            List<RailRheometerTestDto> list = service.getRheometerTestByPlantAndVendor(plantId, vendorCode);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRheometerTest(@PathVariable Long id) {
        try {
            service.deleteRheometerTest(id);
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
