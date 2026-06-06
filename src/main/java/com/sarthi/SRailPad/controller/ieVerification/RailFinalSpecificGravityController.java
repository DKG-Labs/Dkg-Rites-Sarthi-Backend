package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalSpecificGravityRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalSpecificGravityResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailFinalSpecificGravityService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-specific-gravity")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailFinalSpecificGravityController {

    private final RailFinalSpecificGravityService service;

    @PostMapping
    public ResponseEntity<APIResponse> save(@RequestBody RailFinalSpecificGravityRequestDto requestDto) {
        RailFinalSpecificGravityResponseDto response = service.save(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailFinalSpecificGravityResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/call/{callNo}/lot/{lotNo}")
    public ResponseEntity<APIResponse> getByCallNoAndLotNo(@PathVariable String callNo, @PathVariable String lotNo) {
        try {
            RailFinalSpecificGravityResponseDto response = service.getByCallNoAndLotNo(callNo, lotNo);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
        } catch (Exception e) {
            // Return 200 with null response if not found so frontend handles it cleanly
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(null));
        }
    }

    @GetMapping("/call/{callNo}")
    public ResponseEntity<APIResponse> getByCallNo(@PathVariable String callNo) {
        List<RailFinalSpecificGravityResponseDto> response = service.getByCallNo(callNo);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
