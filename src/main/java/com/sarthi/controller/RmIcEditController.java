package com.sarthi.controller;

import com.sarthi.dto.RmIcEditDTO;
import com.sarthi.service.RmIcEditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rm-ic-edit")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RmIcEditController {

    private final RmIcEditService rmIcEditService;

    @GetMapping("/{icNumber}")
    public ResponseEntity<RmIcEditDTO> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get RM IC Edit for IC: {}", icNumber);
        RmIcEditDTO dto = rmIcEditService.getByIcNumber(icNumber);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<RmIcEditDTO> saveOrUpdate(@RequestBody RmIcEditDTO dto) {
        log.info("REST request to save/update RM IC Edit for IC: {}", dto.getIcNumber());
        try {
            RmIcEditDTO saved = rmIcEditService.saveOrUpdate(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving RM IC Edit: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
