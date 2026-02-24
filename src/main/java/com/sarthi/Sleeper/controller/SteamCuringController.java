package com.sarthi.Sleeper.controller;


import com.sarthi.Sleeper.dto.SteamCuring.SteamCuringRequestDto;
import com.sarthi.Sleeper.service.SteamCuringService;

import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/steam-curing")
@RequiredArgsConstructor
public class SteamCuringController {

    @Autowired
    private SteamCuringService steamCuringService;


    // ================= CREATE =================

    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody SteamCuringRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCuringService.create(dto)),
                HttpStatus.OK);
    }


    // ================= UPDATE =================

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody SteamCuringRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCuringService.update(id, dto)),
                HttpStatus.OK);
    }


    // ================= GET BY ID =================

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCuringService.getById(id)),
                HttpStatus.OK);
    }


    // ================= GET ALL =================

    @GetMapping("/getAll")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCuringService.getAll()),
                HttpStatus.OK);
    }


    // ================= DELETE =================

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        steamCuringService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Deleted Successfully"),
                HttpStatus.OK);
    }
}
