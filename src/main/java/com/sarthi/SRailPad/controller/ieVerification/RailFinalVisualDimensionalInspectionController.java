package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalVisualDimensionalInspectionRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalVisualDimensionalInspectionResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailFinalVisualDimensionalInspectionService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-visual-dimensional-inspection")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailFinalVisualDimensionalInspectionController {

    private final RailFinalVisualDimensionalInspectionService service;

    @PostMapping
    public ResponseEntity<APIResponse> save(@RequestBody RailFinalVisualDimensionalInspectionRequestDto requestDto) {
        RailFinalVisualDimensionalInspectionResponseDto response = service.save(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailFinalVisualDimensionalInspectionResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/call/{callNo}/lot/{lotNo}")
    public ResponseEntity<APIResponse> getByCallNoAndLotNo(@PathVariable String callNo, @PathVariable String lotNo) {
        try {
            RailFinalVisualDimensionalInspectionResponseDto response = service.getByCallNoAndLotNo(callNo, lotNo);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
        } catch (Exception e) {
            // Return 200 with null or empty if not found so frontend can handle it smoothly
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(null));
        }
    }

    @GetMapping("/call/{callNo}")
    public ResponseEntity<APIResponse> getByCallNo(@PathVariable String callNo) {
        List<RailFinalVisualDimensionalInspectionResponseDto> response = service.getByCallNo(callNo);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
