package com.sarthi.SRailPad.controller.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGResponseDto;
import com.sarthi.SRailPad.service.plantDeclaration.RailApprovedAshSGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rail-approved-ash-sg")
@CrossOrigin("*")
public class RailApprovedAshSGController {

    @Autowired
    private RailApprovedAshSGService service;

    @PostMapping
    public ResponseEntity<ApprovedAshSGResponseDto> create(@RequestBody ApprovedAshSGRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovedAshSGResponseDto> update(@PathVariable Long id, @RequestBody ApprovedAshSGRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovedAshSGResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/vendor/{vendorCode}")
    public ResponseEntity<List<ApprovedAshSGResponseDto>> getByVendor(@PathVariable String vendorCode) {
        return ResponseEntity.ok(service.getAllByVendorCode(vendorCode));
    }

    @GetMapping("/plant")
    public ResponseEntity<List<ApprovedAshSGResponseDto>> getByPlant(@RequestParam String plantId) {
        return ResponseEntity.ok(service.getAllByPlantId(plantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
