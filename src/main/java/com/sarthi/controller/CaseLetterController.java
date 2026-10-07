package com.sarthi.controller;

import com.sarthi.entity.certificate.CaseLetterDocument;
import com.sarthi.service.CaseLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/case-letter")
@Tag(name = "Case Letter Management", description = "Endpoints for merging, compressing, storing and downloading Case Letters")
@Slf4j
public class CaseLetterController {

    @Autowired
    private CaseLetterService caseLetterService;

    @GetMapping("/metadata/{callNo}")
    @Operation(summary = "Get document metadata and existing case letter status for a call")
    public ResponseEntity<Map<String, Object>> getCallMetadata(
            @PathVariable String callNo,
            @RequestParam(required = false, defaultValue = "ERC") String moduleType
    ) {
        Map<String, Object> metadata = caseLetterService.getCallDocumentsMetadata(callNo, moduleType);
        return ResponseEntity.ok(metadata);
    }

    @PostMapping(value = "/merge-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Merge multiple uploaded PDFs in memory and return the merged PDF for preview")
    public ResponseEntity<byte[]> mergePreview(
            @RequestParam("files") List<MultipartFile> files
    ) {
        try {
            byte[] mergedBytes = caseLetterService.mergeUploadedFiles(files);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview_case_letter.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(mergedBytes.length)
                    .body(mergedBytes);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for merge-preview: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error in merge-preview: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Merge, compress and save Case Letter to Azure Blob Storage")
    public ResponseEntity<Map<String, Object>> saveCaseLetter(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("callNo") String callNo,
            @RequestParam(value = "icNumber", required = false) String icNumber,
            @RequestParam(value = "moduleType", required = false, defaultValue = "ERC") String moduleType,
            @RequestParam(value = "stage", required = false) String stage,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy
    ) {
        try {
            CaseLetterDocument doc = caseLetterService.saveCaseLetter(
                    files, callNo, icNumber, moduleType, stage, uploadedBy
            );
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Case letter generated and saved successfully",
                    "data", doc
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Bad request for saveCaseLetter: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error saving Case Letter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to save Case Letter: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/view/{callNo}")
    @Operation(summary = "View saved Case Letter PDF inline in browser")
    public ResponseEntity<byte[]> viewCaseLetter(@PathVariable String callNo) {
        try {
            byte[] pdfBytes = caseLetterService.getLatestCaseLetterPdf(callNo);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Case_Letter_" + callNo + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.warn("Error viewing Case Letter for {}: {}", callNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/download/{callNo}")
    @Operation(summary = "Download saved Case Letter PDF")
    public ResponseEntity<byte[]> downloadCaseLetter(@PathVariable String callNo) {
        try {
            byte[] pdfBytes = caseLetterService.getLatestCaseLetterPdf(callNo);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Case_Letter_" + callNo + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.warn("Error downloading Case Letter for {}: {}", callNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/info/{callNo}")
    @Operation(summary = "Get info of latest saved Case Letter")
    public ResponseEntity<Map<String, Object>> getCaseLetterInfo(@PathVariable String callNo) {
        CaseLetterDocument doc = caseLetterService.getLatestCaseLetter(callNo);
        if (doc == null) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        return ResponseEntity.ok(Map.of(
                "exists", true,
                "data", doc
        ));
    }

    @DeleteMapping("/delete/{callNo}")
    @Operation(summary = "Delete saved Case Letter for a call")
    public ResponseEntity<Map<String, Object>> deleteCaseLetter(
            @PathVariable String callNo,
            @RequestParam(required = false, defaultValue = "IE") String requestedBy
    ) {
        boolean deleted = caseLetterService.deleteCaseLetter(callNo, requestedBy);
        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Case Letter deleted successfully"
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error",
                "message", "No active Case Letter found to delete"
        ));
    }

    @DeleteMapping("/delete-by-id/{id}")
    @Operation(summary = "Delete saved Case Letter by ID")
    public ResponseEntity<Map<String, Object>> deleteCaseLetterById(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "IE") String requestedBy
    ) {
        boolean deleted = caseLetterService.deleteCaseLetterById(id, requestedBy);
        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Case Letter deleted successfully"
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error",
                "message", "No Case Letter found with given ID"
        ));
    }
}
