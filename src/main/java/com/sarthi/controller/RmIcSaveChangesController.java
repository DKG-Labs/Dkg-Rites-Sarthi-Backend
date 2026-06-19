package com.sarthi.controller;

import com.sarthi.dto.RmIcSaveChangesDTO;
import com.sarthi.service.RmIcSaveChangesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rm-ic-save-changes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RmIcSaveChangesController {

    private final RmIcSaveChangesService rmIcSaveChangesService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<RmIcSaveChangesDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get RmIcSaveChanges for IC (path): {}", icNumber);
        RmIcSaveChangesDTO dto = rmIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<RmIcSaveChangesDTO> getByIcNumberQuery(@RequestParam String icNumber) {
        log.info("REST request to get RmIcSaveChanges for IC (query): {}", icNumber);
        RmIcSaveChangesDTO dto = rmIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<RmIcSaveChangesDTO> saveOrUpdate(@RequestBody RmIcSaveChangesDTO dto) {
        log.info("REST request to save/update RmIcSaveChanges for IC: {}", dto.getIcNumber());
        try {
            RmIcSaveChangesDTO saved = rmIcSaveChangesService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving RmIcSaveChanges: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
