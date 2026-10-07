package com.sarthi.SRailPad.controller.plantDeclaration;

import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedQAPRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedQAPResponseDto;
import com.sarthi.SRailPad.service.plantDeclaration.RailApprovedQAPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rail-approved-qap")
@CrossOrigin("*")
public class RailApprovedQAPController {

    @Autowired
    private RailApprovedQAPService service;

    @PostMapping
    public ResponseEntity<ApprovedQAPResponseDto> create(@RequestBody ApprovedQAPRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovedQAPResponseDto> update(@PathVariable Long id, @RequestBody ApprovedQAPRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovedQAPResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/vendor/{vendorCode}")
    public ResponseEntity<List<ApprovedQAPResponseDto>> getByVendor(@PathVariable String vendorCode) {
        return ResponseEntity.ok(service.getAllByVendorCode(vendorCode));
    }

    @GetMapping("/plant")
    public ResponseEntity<List<ApprovedQAPResponseDto>> getByPlant(@RequestParam String plantId) {
        return ResponseEntity.ok(service.getAllByPlantId(plantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/unblock/{id}", method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<Object> unblockApprovedQAP(
            @PathVariable Long id,
            @RequestBody(required = false) com.sarthi.SRailPad.dto.RailUnblockReqDto unblockDto) {
        service.unblockApprovedQAP(id, unblockDto);
        return ResponseEntity.ok(com.sarthi.util.ResponseBuilder.getSuccessResponse("Approved QAP baseline unblocked and returned to pending list successfully"));
    }
}
