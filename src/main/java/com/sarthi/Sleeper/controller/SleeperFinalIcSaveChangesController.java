package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperFinalIcSaveChangesDTO;
import com.sarthi.Sleeper.service.SleeperFinalIcSaveChangesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sleeper-final-ic-save-changes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SleeperFinalIcSaveChangesController {

    private final SleeperFinalIcSaveChangesService sleeperFinalIcSaveChangesService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<SleeperFinalIcSaveChangesDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get Sleeper Final IC Save Changes for IC (path): {}", icNumber);
        SleeperFinalIcSaveChangesDTO dto = sleeperFinalIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<SleeperFinalIcSaveChangesDTO> getByIcNumberQuery(@RequestParam String icNumber) {
        log.info("REST request to get Sleeper Final IC Save Changes for IC (query): {}", icNumber);
        SleeperFinalIcSaveChangesDTO dto = sleeperFinalIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> saveOrUpdate(@RequestBody SleeperFinalIcSaveChangesDTO dto) {
        log.info("REST request to save/update Sleeper Final IC Save Changes for IC: {}", dto != null ? dto.getIcNumber() : null);
        try {
            SleeperFinalIcSaveChangesDTO saved = sleeperFinalIcSaveChangesService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving Sleeper Final IC Save Changes: ", e);
            return ResponseEntity.internalServerError().body(java.util.Map.of(
                "success", false,
                "error", e.getMessage() != null ? e.getMessage() : "Unknown internal server error"
            ));
        }
    }
}
