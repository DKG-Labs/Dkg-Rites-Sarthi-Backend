package com.sarthi.controller;

import com.sarthi.entity.certificate.IcAnnexureDocument;
import com.sarthi.service.IcAnnexureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ic-annexures")
@RequiredArgsConstructor
@Tag(name = "IC Annexure Documents Controller", description = "Endpoints for uploading, listing, and managing annexures and other documents attached to ICs")
public class IcAnnexureController {

    private final IcAnnexureService icAnnexureService;

    @Operation(summary = "Upload an annexure/document for an Inspection Call")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadAnnexure(
            @RequestParam("file") MultipartFile file,
            @RequestParam("callNo") String callNo,
            @RequestParam(value = "icNumber", required = false) String icNumber,
            @RequestParam(value = "moduleType", defaultValue = "SLEEPER") String moduleType,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            IcAnnexureDocument saved = icAnnexureService.uploadAnnexure(file, callNo, icNumber, moduleType, uploadedBy);
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("data", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception ex) {
            log.error("Failed to upload annexure for call {}: {}", callNo, ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "Failed to upload document: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Get list of uploaded annexures for an Inspection Call")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAnnexures(
            @RequestParam("callNo") String callNo,
            @RequestParam(value = "moduleType", required = false) String moduleType
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<IcAnnexureDocument> docs = icAnnexureService.getAnnexuresByCall(callNo, moduleType);
            response.put("success", true);
            response.put("data", docs);
            response.put("count", docs.size());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch annexures for call {}: {}", callNo, ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "Failed to fetch annexures: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Delete an uploaded annexure document")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnnexure(
            @PathVariable("id") Long id,
            @RequestParam(value = "requestedBy", required = false) String requestedBy
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            icAnnexureService.deleteAnnexure(id, requestedBy);
            response.put("success", true);
            response.put("message", "Document deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            log.error("Failed to delete annexure {}: {}", id, ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "Failed to delete document: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Download or stream an uploaded annexure document")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadAnnexure(@PathVariable("id") Long id) {
        return icAnnexureService.downloadAnnexure(id);
    }
}
