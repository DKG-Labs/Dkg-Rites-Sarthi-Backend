package com.sarthi.SRailPad.controller.ieVerification;

import com.sarthi.SRailPad.dto.ieVerification.RailMixingKneaderMillRequestDto;
import com.sarthi.SRailPad.dto.ieVerification.RailMixingKneaderMillResponseDto;
import com.sarthi.SRailPad.service.ieVerification.RailMixingKneaderMillService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mixing-kneader-mill")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RailMixingKneaderMillController {

    private final RailMixingKneaderMillService service;

    @PostMapping
    public ResponseEntity<APIResponse> create(@RequestBody RailMixingKneaderMillRequestDto requestDto) {
        RailMixingKneaderMillResponseDto response = service.create(requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse> update(@PathVariable Long id, @RequestBody RailMixingKneaderMillRequestDto requestDto) {
        RailMixingKneaderMillResponseDto response = service.update(id, requestDto);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getById(@PathVariable Long id) {
        RailMixingKneaderMillResponseDto response = service.getById(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @GetMapping("/shift")
    public ResponseEntity<APIResponse> getByShift(
            @RequestParam String plantId,
            @RequestParam String shift,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate castingDate) {
        List<RailMixingKneaderMillResponseDto> response = service.getByShiftAndDate(plantId, shift, castingDate);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Record deleted successfully"));
    }
}
