package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailMouldVerificationDto;
import com.sarthi.SRailPad.service.ieVerification.RailMouldVerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/railpad-workflow/mould-verification")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RailMouldVerificationController {

    @Autowired
    private RailMouldVerificationService service;

    @PostMapping("/create")
    public ResponseEntity<?> createMouldVerification(@Valid @RequestBody RailMouldVerificationDto dto) {
        try {
            RailMouldVerificationDto savedDto = service.createMouldVerification(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMouldVerification(@PathVariable Long id, @Valid @RequestBody RailMouldVerificationDto dto) {
        try {
            RailMouldVerificationDto updatedDto = service.updateMouldVerification(id, dto);
            return ResponseEntity.ok(updatedDto);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMouldVerification(@PathVariable Long id) {
        try {
            service.deleteMouldVerification(id);
            return ResponseEntity.ok("Record deleted successfully.");
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getMouldVerifications(@RequestParam String plantId, @RequestParam String vendorCode) {
        try {
            List<RailMouldVerificationDto> dtoList = service.getMouldVerifications(plantId, vendorCode);
            return ResponseEntity.ok(dtoList);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
