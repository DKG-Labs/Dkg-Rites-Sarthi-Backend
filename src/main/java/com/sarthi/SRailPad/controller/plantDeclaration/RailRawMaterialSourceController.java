package com.sarthi.SRailPad.controller.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.RawMaterialSourceRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.RawMaterialSourceResponseDto;
import com.sarthi.SRailPad.service.plantDeclaration.RailRawMaterialSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rail-raw-material-source")
@CrossOrigin("*")
public class RailRawMaterialSourceController {

    @Autowired
    private RailRawMaterialSourceService service;

    @PostMapping
    public ResponseEntity<RawMaterialSourceResponseDto> create(@RequestBody RawMaterialSourceRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RawMaterialSourceResponseDto> update(@PathVariable Long id, @RequestBody RawMaterialSourceRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RawMaterialSourceResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/vendor/{vendorCode}")
    public ResponseEntity<List<RawMaterialSourceResponseDto>> getByVendor(@PathVariable String vendorCode) {
        return ResponseEntity.ok(service.getAllByVendorCode(vendorCode));
    }

    @GetMapping("/plant")
    public ResponseEntity<List<RawMaterialSourceResponseDto>> getByPlant(@RequestParam String plantId) {
        return ResponseEntity.ok(service.getAllByPlantId(plantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
