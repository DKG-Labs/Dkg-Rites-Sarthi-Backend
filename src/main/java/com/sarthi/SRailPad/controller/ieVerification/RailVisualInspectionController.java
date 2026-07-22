package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailVisualInspectionDto;
import com.sarthi.SRailPad.service.ieVerification.RailVisualInspectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rail-visual-inspection")
@CrossOrigin(origins = "*")
public class RailVisualInspectionController {

    @Autowired
    private RailVisualInspectionService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RailVisualInspectionDto dto) {
        try {
            RailVisualInspectionDto created = service.create(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody RailVisualInspectionDto dto) {
        try {
            RailVisualInspectionDto updated = service.update(id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{plantId}/{vendorCode}")
    public ResponseEntity<?> getList(@PathVariable String plantId, @PathVariable String vendorCode) {
        try {
            List<RailVisualInspectionDto> list = service.getList(plantId, vendorCode);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Visual Inspection deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
