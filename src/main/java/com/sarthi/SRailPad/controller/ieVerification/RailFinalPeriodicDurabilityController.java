package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicDurabilityRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicDurabilityResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailFinalPeriodicDurabilityService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-periodic-durability")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailFinalPeriodicDurabilityController {

    private final RailFinalPeriodicDurabilityService service;

    @PostMapping
    public ResponseEntity<APIResponse> save(@RequestBody RailFinalPeriodicDurabilityRequestDto requestDto) {
        RailFinalPeriodicDurabilityResponseDto response = service.save(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailFinalPeriodicDurabilityResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/call/{callNo}/lot/{lotNo}")
    public ResponseEntity<APIResponse> getByCallNoAndLotNo(@PathVariable String callNo, @PathVariable String lotNo) {
        try {
            RailFinalPeriodicDurabilityResponseDto response = service.getByCallNoAndLotNo(callNo, lotNo);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
        } catch (Exception e) {
            // Return 200 with null response if not found so frontend handles it cleanly
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(null));
        }
    }

    @GetMapping("/call/{callNo}")
    public ResponseEntity<APIResponse> getByCallNo(@PathVariable String callNo) {
        List<RailFinalPeriodicDurabilityResponseDto> response = service.getByCallNo(callNo);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
