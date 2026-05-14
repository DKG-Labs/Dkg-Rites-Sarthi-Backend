package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.IEProductionVerificationRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.IEProductionVerificationResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailIEProductionVerificationService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ie-production-verification")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailIEProductionVerificationController {

    private final RailIEProductionVerificationService service;

    @PostMapping
    public ResponseEntity<APIResponse> create(@RequestBody IEProductionVerificationRequestDto requestDto) {
        IEProductionVerificationResponseDto response = service.create(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        IEProductionVerificationResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<APIResponse> getByRequestId(@PathVariable Long requestId) {
        IEProductionVerificationResponseDto response = service.getByRequestId(requestId);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping
    public ResponseEntity<APIResponse> getAll() {
        List<IEProductionVerificationResponseDto> response = service.getAll();
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<APIResponse> deleteByRequestId(@PathVariable Long requestId) {
        service.deleteByRequestId(requestId);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }

    @GetMapping("/accepted-inventory")
    public ResponseEntity<APIResponse> getAcceptedInventory(
            @RequestParam String productionUnit,
            @RequestParam(required = false) String productType) {
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(service.getAcceptedInventory(productionUnit, productType)));
    }
}
