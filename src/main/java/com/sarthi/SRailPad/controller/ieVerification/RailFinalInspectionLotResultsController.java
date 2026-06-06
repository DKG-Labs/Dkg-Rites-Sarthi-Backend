package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailFinalInspectionLotResultsRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailFinalInspectionLotResultsResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailFinalInspectionLotResultsService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/final-inspection-lot-results")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailFinalInspectionLotResultsController {

    private final RailFinalInspectionLotResultsService service;

    @PostMapping
    public ResponseEntity<APIResponse> save(@RequestBody RailFinalInspectionLotResultsRequestDto requestDto) {
        RailFinalInspectionLotResultsResponseDto response = service.save(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailFinalInspectionLotResultsResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/call/{callNo}")
    public ResponseEntity<APIResponse> getByCallNo(@PathVariable String callNo) {
        List<RailFinalInspectionLotResultsResponseDto> response = service.getByCallNo(callNo);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/shift")
    public ResponseEntity<APIResponse> getByShift(
            @RequestParam String plantId,
            @RequestParam String shift,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfInspection) {
        List<RailFinalInspectionLotResultsResponseDto> response = service.getByShiftAndDate(plantId, shift, dateOfInspection);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
