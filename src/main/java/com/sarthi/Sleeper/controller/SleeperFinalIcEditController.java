package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperFinalIcEditDTO;
import com.sarthi.Sleeper.service.SleeperFinalIcEditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sleeper-final-ic-edit")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SleeperFinalIcEditController {

    private final SleeperFinalIcEditService sleeperFinalIcEditService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<SleeperFinalIcEditDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get Sleeper Final IC Edit for IC (path): {}", icNumber);
        SleeperFinalIcEditDTO dto = sleeperFinalIcEditService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<SleeperFinalIcEditDTO> getByIcNumberQuery(@RequestParam String icNumber) {
        log.info("REST request to get Sleeper Final IC Edit for IC (query): {}", icNumber);
        SleeperFinalIcEditDTO dto = sleeperFinalIcEditService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> saveOrUpdate(@RequestBody SleeperFinalIcEditDTO dto) {
        log.info("REST request to save/update Sleeper Final IC Edit for IC: {}", dto != null ? dto.getIcNumber() : null);
        try {
            SleeperFinalIcEditDTO saved = sleeperFinalIcEditService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving Sleeper Final IC Edit: ", e);
            return ResponseEntity.internalServerError().body(java.util.Map.of(
                "success", false,
                "error", e.getMessage() != null ? e.getMessage() : "Unknown internal server error"
            ));
        }
    }
}
