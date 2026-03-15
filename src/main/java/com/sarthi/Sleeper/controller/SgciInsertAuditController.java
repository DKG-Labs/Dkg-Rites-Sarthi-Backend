package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertAuditAuditRequestDto;
import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertAuditAuditResponseDto;
import com.sarthi.Sleeper.service.SgciInsertAuditService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sgci-insert-audit")
public class SgciInsertAuditController {

    @Autowired
    private SgciInsertAuditService service;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody SgciInsertAuditAuditRequestDto dto) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(service.create(dto)), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody SgciInsertAuditAuditRequestDto dto) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(service.update(id, dto)), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(service.getById(id)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(service.getAll()), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Deleted Successfully"), HttpStatus.OK);
    }
}
