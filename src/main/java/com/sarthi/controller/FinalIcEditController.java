package com.sarthi.controller;

import com.sarthi.dto.FinalIcEditDTO;
import com.sarthi.service.FinalIcEditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/final-ic-edit")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FinalIcEditController {

    private final FinalIcEditService finalIcEditService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<FinalIcEditDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get Final IC Edit for IC: {}", icNumber);
        FinalIcEditDTO dto = finalIcEditService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<FinalIcEditDTO> saveOrUpdate(@RequestBody FinalIcEditDTO dto) {
        log.info("REST request to save/update Final IC Edit for IC: {}", dto.getIcNumber());
        try {
            FinalIcEditDTO saved = finalIcEditService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving Final IC Edit: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
