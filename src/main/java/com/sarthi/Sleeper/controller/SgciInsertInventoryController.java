package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertRequestDto;
import com.sarthi.Sleeper.service.SgciInsertInventoryService;
import com.sarthi.util.ResponseBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sgci-insert")

public class SgciInsertInventoryController {

    @Autowired
        private SgciInsertInventoryService service;

        // ================= CREATE =================

        @PostMapping
        public ResponseEntity<Object> create(
                @RequestBody SgciInsertRequestDto dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.create(dto)),
                    HttpStatus.OK
            );
        }

        // ================= UPDATE =================

        @PutMapping("/{id}")
        public ResponseEntity<Object> update(
                @PathVariable Long id,
                @RequestBody SgciInsertRequestDto dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                    HttpStatus.OK
            );
        }

        // ================= GET BY ID =================

        @GetMapping("/{id}")
        public ResponseEntity<Object> getById(
                @PathVariable Long id) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.getById(id)),
                    HttpStatus.OK
            );
        }

        // ================= GET ALL =================

        @GetMapping
        public ResponseEntity<Object> getAll() {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(service.getAll()),
                    HttpStatus.OK
            );
        }

        // ================= DELETE =================

        @DeleteMapping("/{id}")
        public ResponseEntity<Object> delete(
                @PathVariable Long id) {

            service.delete(id);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                    HttpStatus.OK
            );
        }

}
