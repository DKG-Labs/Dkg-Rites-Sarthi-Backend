package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicTgaRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalPeriodicTgaResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailFinalPeriodicTgaService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-periodic-tga")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailFinalPeriodicTgaController {

    private final RailFinalPeriodicTgaService service;

    @PostMapping
    public ResponseEntity<APIResponse> save(@RequestBody RailFinalPeriodicTgaRequestDto requestDto) {
        RailFinalPeriodicTgaResponseDto response = service.save(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailFinalPeriodicTgaResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/call/{callNo}/lot/{lotNo}")
    public ResponseEntity<APIResponse> getByCallNoAndLotNo(@PathVariable String callNo, @PathVariable String lotNo) {
        try {
            RailFinalPeriodicTgaResponseDto response = service.getByCallNoAndLotNo(callNo, lotNo);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
        } catch (Exception e) {
            // Return 200 with null response if not found so frontend handles it cleanly
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(null));
        }
    }

    @GetMapping("/call/{callNo}")
    public ResponseEntity<APIResponse> getByCallNo(@PathVariable String callNo) {
        List<RailFinalPeriodicTgaResponseDto> response = service.getByCallNo(callNo);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
