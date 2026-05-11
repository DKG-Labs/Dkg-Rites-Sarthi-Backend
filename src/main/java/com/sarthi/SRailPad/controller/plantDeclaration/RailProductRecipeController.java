package com.sarthi.SRailPad.controller.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ProductRecipeRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ProductRecipeResponseDto;
import com.sarthi.SRailPad.service.plantDeclaration.RailProductRecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rail-product-recipe")
@CrossOrigin("*")
public class RailProductRecipeController {

    @Autowired
    private RailProductRecipeService service;

    @PostMapping
    public ResponseEntity<ProductRecipeResponseDto> create(@RequestBody ProductRecipeRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductRecipeResponseDto> update(@PathVariable Long id, @RequestBody ProductRecipeRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductRecipeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/vendor/{vendorCode}")
    public ResponseEntity<List<ProductRecipeResponseDto>> getByVendor(@PathVariable String vendorCode) {
        return ResponseEntity.ok(service.getAllByVendorCode(vendorCode));
    }

    @GetMapping("/plant")
    public ResponseEntity<List<ProductRecipeResponseDto>> getByPlant(@RequestParam String plantId) {
        return ResponseEntity.ok(service.getAllByPlantId(plantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
