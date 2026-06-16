package com.sarthi.controller;

import com.sarthi.dto.ProcessIeMappingRequestDto;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mapping")
@CrossOrigin("*")
public class MappingController {

    @Autowired
    private MappingService mappingService;

    @PostMapping("/processIe")
    public ResponseEntity<Object> createMappingProcessIe(
            @RequestBody ProcessIeMappingRequestDto dto,
            @RequestParam("userId") Long userId,
            @RequestParam("createdBy") String createdBy) {

        try {
            Object obj = mappingService.mapProcessIe(userId, dto, createdBy);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(obj));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.BAD_REQUEST.value(),
                            1000,
                            "error",
                            "Mapping already exists for the selected User and POI."
                    ))
            );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            5000,
                            "error",
                            "Failed to save mapping: " + ex.getMessage()
                    ))
            );
        }
    }


    @GetMapping("/all")
    public ResponseEntity<Object> getAllMappings() {
        try {
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(mappingService.getAllMappings()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            5000,
                            "error",
                            "Failed to retrieve mappings: " + ex.getMessage()
                    ))
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteMapping(@PathVariable("id") String id) {
        try {
            mappingService.deleteMapping(id);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Mapping deleted successfully"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            5000,
                            "error",
                            "Failed to delete mapping: " + ex.getMessage()
                    ))
            );
        }
    }

}
