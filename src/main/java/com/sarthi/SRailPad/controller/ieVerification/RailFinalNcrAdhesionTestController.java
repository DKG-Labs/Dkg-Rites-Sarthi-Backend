package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrAdhesionTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalNcrAdhesionTestResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailFinalNcrAdhesionTestService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-ncr-adhesion-test")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailFinalNcrAdhesionTestController {

    private final RailFinalNcrAdhesionTestService service;

    @PostMapping
    public ResponseEntity<APIResponse> save(@RequestBody RailFinalNcrAdhesionTestRequestDto requestDto) {
        RailFinalNcrAdhesionTestResponseDto response = service.save(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailFinalNcrAdhesionTestResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/call/{callNo}/lot/{lotNo}")
    public ResponseEntity<APIResponse> getByCallNoAndLotNo(@PathVariable String callNo, @PathVariable String lotNo) {
        try {
            RailFinalNcrAdhesionTestResponseDto response = service.getByCallNoAndLotNo(callNo, lotNo);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(null));
        }
    }

    @GetMapping("/call/{callNo}")
    public ResponseEntity<APIResponse> getByCallNo(@PathVariable String callNo) {
        List<RailFinalNcrAdhesionTestResponseDto> response = service.getByCallNo(callNo);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
