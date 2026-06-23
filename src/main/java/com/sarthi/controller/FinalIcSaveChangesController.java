package com.sarthi.controller;

import com.sarthi.dto.FinalIcSaveChangesDTO;
import com.sarthi.service.FinalIcSaveChangesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/final-ic-save-changes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FinalIcSaveChangesController {

    private final FinalIcSaveChangesService finalIcSaveChangesService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<FinalIcSaveChangesDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get FinalIcSaveChanges for IC (path): {}", icNumber);
        FinalIcSaveChangesDTO dto = finalIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<FinalIcSaveChangesDTO> getByIcNumberQuery(@RequestParam String icNumber) {
        log.info("REST request to get FinalIcSaveChanges for IC (query): {}", icNumber);
        FinalIcSaveChangesDTO dto = finalIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<FinalIcSaveChangesDTO> saveOrUpdate(@RequestBody FinalIcSaveChangesDTO dto) {
        log.info("REST request to save/update FinalIcSaveChanges for IC: {}", dto.getIcNumber());
        try {
            FinalIcSaveChangesDTO saved = finalIcSaveChangesService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving FinalIcSaveChanges: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
