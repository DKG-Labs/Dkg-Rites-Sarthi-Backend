package com.sarthi.SRailPad.controller.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ProductionDeclarationRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ProductionDeclarationResponseDto;
import com.sarthi.SRailPad.service.plantDeclaration.RailProductionDeclarationService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rail-production-declaration")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailProductionDeclarationController {

    private final RailProductionDeclarationService service;

    @PostMapping
    public ResponseEntity<APIResponse> create(@RequestBody ProductionDeclarationRequestDto requestDto) {
        ProductionDeclarationResponseDto response = service.create(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse> update(@PathVariable Long id, @RequestBody ProductionDeclarationRequestDto requestDto) {
        ProductionDeclarationResponseDto response = service.update(id, requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/plant")
    public ResponseEntity<APIResponse> getAllByPlant(@RequestParam String plantId) {
        List<ProductionDeclarationResponseDto> response = service.getAllByPlant(plantId);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        ProductionDeclarationResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(null));
    }
}
