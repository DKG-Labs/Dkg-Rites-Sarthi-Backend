package com.sarthi.controller;

import com.sarthi.dto.ProcessIcSaveChangesDTO;
import com.sarthi.service.ProcessIcSaveChangesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process-ic-save-changes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProcessIcSaveChangesController {

    private final ProcessIcSaveChangesService processIcSaveChangesService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<ProcessIcSaveChangesDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get ProcessIcSaveChanges for IC (path): {}", icNumber);
        ProcessIcSaveChangesDTO dto = processIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ProcessIcSaveChangesDTO> getByIcNumberQuery(@RequestParam String icNumber) {
        log.info("REST request to get ProcessIcSaveChanges for IC (query): {}", icNumber);
        ProcessIcSaveChangesDTO dto = processIcSaveChangesService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<ProcessIcSaveChangesDTO> saveOrUpdate(@RequestBody ProcessIcSaveChangesDTO dto) {
        log.info("REST request to save/update ProcessIcSaveChanges for IC: {}", dto.getIcNumber());
        try {
            ProcessIcSaveChangesDTO saved = processIcSaveChangesService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving ProcessIcSaveChanges: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
