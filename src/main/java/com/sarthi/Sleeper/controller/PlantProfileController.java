package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.PlantProfile.PlantProfileRequestDto;
import com.sarthi.Sleeper.dto.PlantProfile.PlantProfileResponseDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import com.sarthi.Sleeper.service.PlantProfileService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plant-profile")
public class PlantProfileController {

    @Autowired
    private PlantProfileService service;
    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;


    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody PlantProfileRequestDto dto) {

        PlantProfileResponseDto result = service.create(dto);

        String requestId = String.valueOf(result.getId());
        Long md = 1L;
        Long wid = 1L;
        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(result.getCreatedBy()));

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody PlantProfileRequestDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.update(id, dto)),
                HttpStatus.OK
        );
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable("id") Long id) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getById(id)),
                HttpStatus.OK
        );
    }

    // ================= GET SHEDS BY VENDOR =================
    @GetMapping("/vendor/{vendorId}/{plantId}/sheds")
    public ResponseEntity<Object> getDistinctShedsByVendorCode(
            @RequestParam("vendorId") Long vendorId, @RequestParam("plantId") String plantId) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getShedsByPlantType(vendorId, plantId)),
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
            @PathVariable("id") Long id) {

        service.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK
        );
    }

    @GetMapping("/plant-details")
    public ResponseEntity<Object> getPlantDetails(@RequestParam String vendorCode) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        service.getPlantDetails(vendorCode)),
                HttpStatus.OK);
    }
}