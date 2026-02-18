package com.sarthi.Sleeper.controller;


import com.sarthi.Sleeper.dto.SteamCubeDtos.SteamCubeSampleDeclarationRequestDto;
import com.sarthi.Sleeper.service.SteamCubeService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/SteamCube")
public class SteamCubeController {
    @Autowired
    private SteamCubeService steamCubeService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody SteamCubeSampleDeclarationRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeService.create(dto)),
                HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody SteamCubeSampleDeclarationRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeService.update(id, dto)),
                HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeService.getById(id)),
                HttpStatus.OK);
    }

    @GetMapping("/get-all")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeService.getAll()),
                HttpStatus.OK);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        steamCubeService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Steam Cube Sample Declaration Deleted Successfully"),
                HttpStatus.OK);
    }




}
