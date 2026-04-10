package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SteamCubeTestingDtos.SteamCubeTestingRequestDto;
import com.sarthi.Sleeper.dto.SteamCubeTestingDtos.SteamCubeTestingResponseDto;
import com.sarthi.Sleeper.service.SteamCubeTestingService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/steam-cube-testing")
@RequiredArgsConstructor
public class SteamCubeTestingController {

    @Autowired
    private final SteamCubeTestingService steamCubeTestingService;


    @PostMapping("/create")
    public ResponseEntity<Object> create(
            @RequestBody SteamCubeTestingRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeTestingService.create(dto)),
                HttpStatus.OK);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody SteamCubeTestingRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeTestingService.update(id, dto)),
                HttpStatus.OK);
    }



    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeTestingService.getById(id)),
                HttpStatus.OK);
    }


    @GetMapping("/get-all")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        steamCubeTestingService.getAll()),
                HttpStatus.OK);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) {

        steamCubeTestingService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        "Deleted Successfully"),
                HttpStatus.OK);
    }


    @GetMapping("/steamCubeTestingData")
    public ResponseEntity<Object> getByDate(
            @RequestParam String plantId,
            @RequestParam String vendorCode,
            @RequestParam String shift,
            @RequestParam int createdBy, @RequestParam String date)  {

        List<SteamCubeTestingResponseDto> list =
                steamCubeTestingService.getByDate(plantId,vendorCode, shift, createdBy, date);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(list),
                HttpStatus.OK
        );
    }
}
