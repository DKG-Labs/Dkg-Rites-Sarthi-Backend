package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalSecantStiffnessTestRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalSecantStiffnessTestResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailFinalSecantStiffnessTestService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-secant-test")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailFinalSecantStiffnessTestController {

    private final RailFinalSecantStiffnessTestService service;

    @PostMapping
    public ResponseEntity<APIResponse> save(@RequestBody RailFinalSecantStiffnessTestRequestDto requestDto) {
        RailFinalSecantStiffnessTestResponseDto response = service.save(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailFinalSecantStiffnessTestResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/call/{callNo}/lot/{lotNo}")
    public ResponseEntity<APIResponse> getByCallNoAndLotNo(@PathVariable String callNo, @PathVariable String lotNo) {
        try {
            RailFinalSecantStiffnessTestResponseDto response = service.getByCallNoAndLotNo(callNo, lotNo);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(null));
        }
    }

    @GetMapping("/call/{callNo}")
    public ResponseEntity<APIResponse> getByCallNo(@PathVariable String callNo) {
        List<RailFinalSecantStiffnessTestResponseDto> response = service.getByCallNo(callNo);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
