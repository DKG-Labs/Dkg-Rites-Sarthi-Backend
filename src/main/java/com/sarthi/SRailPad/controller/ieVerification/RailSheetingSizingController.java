package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailSheetingSizingDto;
import com.sarthi.SRailPad.service.ieVerification.RailSheetingSizingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/railpad-workflow/sheeting")
public class RailSheetingSizingController {

    @Autowired
    private RailSheetingSizingService service;

    @PostMapping("/create")
    public ResponseEntity<?> createSheetingSizing(@Valid @RequestBody RailSheetingSizingDto dto) {
        try {
            RailSheetingSizingDto created = service.createSheetingSizing(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSheetingSizing(@PathVariable Long id, @Valid @RequestBody RailSheetingSizingDto dto) {
        try {
            RailSheetingSizingDto updated = service.updateSheetingSizing(id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getSheetingSizing(@PathVariable Long id) {
        try {
            RailSheetingSizingDto dto = service.getSheetingSizingById(id);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getSheetingSizingList(
            @RequestParam String plantId,
            @RequestParam String vendorCode) {
        try {
            List<RailSheetingSizingDto> list = service.getSheetingSizingByPlantAndVendor(plantId, vendorCode);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSheetingSizing(@PathVariable Long id) {
        try {
            service.deleteSheetingSizing(id);
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
