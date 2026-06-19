package com.sarthi.controller;

import com.sarthi.dto.ProcessIcEditDTO;
import com.sarthi.service.ProcessIcEditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process-ic-edit")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProcessIcEditController {

    private final ProcessIcEditService processIcEditService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<ProcessIcEditDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get Process IC Edit for IC (path): {}", icNumber);
        ProcessIcEditDTO dto = processIcEditService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ProcessIcEditDTO> getByIcNumberQuery(@RequestParam String icNumber) {
        log.info("REST request to get Process IC Edit for IC (query): {}", icNumber);
        ProcessIcEditDTO dto = processIcEditService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<ProcessIcEditDTO> saveOrUpdate(@RequestBody ProcessIcEditDTO dto) {
        log.info("REST request to save/update Process IC Edit for IC: {}", dto.getIcNumber());
        try {
            ProcessIcEditDTO saved = processIcEditService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving Process IC Edit: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
