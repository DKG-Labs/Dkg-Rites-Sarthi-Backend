package com.sarthi.SRailPad.controller.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.PlantSetupRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.PlantSetupResponseDto;
import com.sarthi.SRailPad.service.plantDeclaration.PlantSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rail-plant-setup")
@CrossOrigin("*")
public class PlantSetupController {

    @Autowired
    private PlantSetupService service;

    @PostMapping
    public ResponseEntity<PlantSetupResponseDto> create(@RequestBody PlantSetupRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantSetupResponseDto> update(@PathVariable Long id, @RequestBody PlantSetupRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantSetupResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PlantSetupResponseDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/vendor/{vendorCode}")
    public ResponseEntity<List<PlantSetupResponseDto>> getByVendor(@PathVariable String vendorCode) {
        return ResponseEntity.ok(service.getAllByVendorCode(vendorCode));
    }

    @GetMapping("/plant")
    public ResponseEntity<List<PlantSetupResponseDto>> getByPlant(@RequestParam String plantId) {
        return ResponseEntity.ok(service.getAllByPlantId(plantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
