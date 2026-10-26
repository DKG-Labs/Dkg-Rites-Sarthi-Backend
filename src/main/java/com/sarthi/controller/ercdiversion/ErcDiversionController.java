package com.sarthi.controller.ercdiversion;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.dto.ercdiversion.*;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;
import com.sarthi.repository.ercdiversion.ErcDiversionWorkflowHistoryRepository;
import com.sarthi.service.ercdiversion.ErcDiversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/erc/diversion")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ERC Material Diversion", description = "Endpoints for ERC Material Diversion and PO Sr. No. Reallocation")
public class ErcDiversionController {

    private final ErcDiversionService diversionService;
    private final ErcDiversionWorkflowHistoryRepository diversionHistoryRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Value("${azure.storage.connection-string}")
    private String azureConnectionString;

    @Value("${azure.storage.diversion-docs-container-name:sarthi-diversion-docs}")
    private String diversionDocsContainer;

    private volatile BlobContainerClient diversionBlobContainer;

    private BlobContainerClient getDiversionContainer() {
        if (diversionBlobContainer == null) {
            synchronized (this) {
                if (diversionBlobContainer == null) {
                    BlobServiceClient client = new BlobServiceClientBuilder()
                            .connectionString(azureConnectionString)
                            .buildClient();
                    String name = (diversionDocsContainer != null && !diversionDocsContainer.isBlank())
                            ? diversionDocsContainer.trim() : "sarthi-diversion-docs";
                    BlobContainerClient c = client.getBlobContainerClient(name);
                    if (!c.exists()) c = client.createBlobContainer(name);
                    diversionBlobContainer = c;
                }
            }
        }
        return diversionBlobContainer;
    }

    @GetMapping("/eligible-source-ics")
    @Operation(summary = "Get eligible source ICs for diversion")
    public ResponseEntity<APIResponse> getEligibleSourceIcs(
            @RequestParam(required = false) String vendorCode,
            @RequestParam(required = false) String plantId,
            @RequestParam(defaultValue = "RAW_MATERIAL") DiversionStage stage) {
        log.info("Request: Get eligible source ICs for vendor: {}, plantId: {}, stage: {}", vendorCode, plantId, stage);
        List<SourceIcEligibilityDto> result = diversionService.getEligibleSourceIcs(vendorCode, plantId, stage);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }

    @GetMapping("/source-ic-details")
    @Operation(summary = "Get line item breakdown for a source IC")
    public ResponseEntity<APIResponse> getSourceIcLineItems(
            @RequestParam String icNo,
            @RequestParam(defaultValue = "RAW_MATERIAL") DiversionStage stage) {
        log.info("Request: Get line items for source IC: {}, stage: {}", icNo, stage);
        List<SourceIcLineItemDto> result = diversionService.getSourceIcLineItems(icNo, stage);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }

    @PostMapping("/save-draft")
    @Operation(summary = "Save diversion request as draft")
    public ResponseEntity<APIResponse> saveDraft(@RequestBody DiversionRequestDto requestDto) {
        log.info("Request: Save draft diversion request for vendor: {}", requestDto.getVendorCode());
        DiversionRequestDto saved = diversionService.saveDraft(requestDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.OK);
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit diversion request for approval")
    public ResponseEntity<APIResponse> submitRequest(@RequestBody DiversionRequestDto requestDto) {
        log.info("Request: Submit diversion request for vendor: {}", requestDto.getVendorCode());
        DiversionRequestDto submitted = diversionService.submitRequest(requestDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(submitted), HttpStatus.OK);
    }

    @GetMapping("/request/{id}")
    @Operation(summary = "Get diversion request by ID")
    public ResponseEntity<APIResponse> getRequestById(@PathVariable Long id) {
        DiversionRequestDto req = diversionService.getRequestById(id);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(req), HttpStatus.OK);
    }

    @GetMapping("/vendor/requests")
    @Operation(summary = "Get all diversion requests for a vendor")
    public ResponseEntity<APIResponse> getVendorRequests(
            @RequestParam String vendorCode,
            @RequestParam(required = false) DiversionRequestStatus status) {
        List<DiversionRequestDto> list = diversionService.getVendorRequests(vendorCode, status);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm/pending-requests")
    @Operation(summary = "Get pending requests for Controlling Manager")
    public ResponseEntity<APIResponse> getPendingRequestsForCm(@RequestParam(required = false) String cmUserId) {
        if (cmUserId == null || cmUserId.isBlank()) {
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(List.of()), HttpStatus.OK);
        }
        List<DiversionRequestDto> list = diversionService.getPendingRequestsForCm(cmUserId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/cm/approved-requests")
    @Operation(summary = "Get approved/forwarded requests for Controlling Manager")
    public ResponseEntity<APIResponse> getApprovedRequestsForCm(@RequestParam(required = false) String cmUserId) {
        if (cmUserId == null || cmUserId.isBlank()) {
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(List.of()), HttpStatus.OK);
        }
        List<DiversionRequestDto> list = diversionService.getApprovedRequestsForCm(cmUserId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/sbu/pending-requests")
    @Operation(summary = "Get pending requests for SBU Head")
    public ResponseEntity<APIResponse> getPendingRequestsForSbu(@RequestParam(required = false) String rioId) {
        if (rioId == null || rioId.isBlank()) {
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(List.of()), HttpStatus.OK);
        }
        List<DiversionRequestDto> list = diversionService.getPendingRequestsForSbu(rioId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/sbu/approved-requests")
    @Operation(summary = "Get approved requests for SBU Head")
    public ResponseEntity<APIResponse> getApprovedRequestsForSbu(@RequestParam(required = false) String rioId) {
        if (rioId == null || rioId.isBlank()) {
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(List.of()), HttpStatus.OK);
        }
        List<DiversionRequestDto> list = diversionService.getApprovedRequestsForSbu(rioId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @PostMapping("/workflow/action")
    @Operation(summary = "Process workflow action")
    public ResponseEntity<APIResponse> processWorkflowAction(@RequestBody WorkflowActionDto actionDto) {
        log.info("Request: Workflow action: {} for request ID: {}", actionDto.getAction(), actionDto.getRequestId());
        DiversionRequestDto updated = diversionService.processWorkflowAction(actionDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
    }

    @PostMapping("/cm/approve")
    @Operation(summary = "CM approves diversion request")
    public ResponseEntity<APIResponse> cmApprove(@RequestBody WorkflowActionDto actionDto) {
        actionDto.setAction("CM_APPROVE_DIVERSION");
        DiversionRequestDto updated = diversionService.processWorkflowAction(actionDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
    }

    @PostMapping("/cm/send-to-sbu")
    @Operation(summary = "CM sends diversion request to SBU Head for approval")
    public ResponseEntity<APIResponse> cmSendToSbu(@RequestBody WorkflowActionDto actionDto) {
        actionDto.setAction("CM_SEND_TO_SBU_HEAD");
        DiversionRequestDto updated = diversionService.processWorkflowAction(actionDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
    }

    @PostMapping("/cm/return")
    @Operation(summary = "CM returns diversion request to vendor")
    public ResponseEntity<APIResponse> cmReturn(@RequestBody WorkflowActionDto actionDto) {
        actionDto.setAction("CM_RETURN_DIVERSION");
        DiversionRequestDto updated = diversionService.processWorkflowAction(actionDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
    }

    @PostMapping("/sbu/approve")
    @Operation(summary = "SBU Head approves diversion request")
    public ResponseEntity<APIResponse> sbuApprove(@RequestBody WorkflowActionDto actionDto) {
        actionDto.setAction("SBU_HEAD_APPROVE_DIVERSION");
        DiversionRequestDto updated = diversionService.processWorkflowAction(actionDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
    }

    @PostMapping("/sbu/return")
    @Operation(summary = "SBU Head returns diversion request to vendor")
    public ResponseEntity<APIResponse> sbuReturn(@RequestBody WorkflowActionDto actionDto) {
        actionDto.setAction("SBU_HEAD_RETURN_DIVERSION");
        DiversionRequestDto updated = diversionService.processWorkflowAction(actionDto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
    }

    @GetMapping("/basket/available")
    @Operation(summary = "Get available items in Diverted & Passed Material Basket")
    public ResponseEntity<APIResponse> getAvailableBasketItems(
            @RequestParam String vendorCode,
            @RequestParam(defaultValue = "RAW_MATERIAL") DiversionStage stage,
            @RequestParam String targetPoNo,
            @RequestParam String targetPoSrNo) {
        List<BasketItemDto> list = diversionService.getAvailableBasketItems(vendorCode, stage, targetPoNo, targetPoSrNo);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/complete-source-sr-nos")
    @Operation(summary = "Get source PO SR numbers with COMPLETE diversion for a vendor")
    public ResponseEntity<APIResponse> getCompleteDiversionSourceSrNos(@RequestParam String vendorCode) {
        List<String> srNos = diversionService.getCompleteDiversionSourceSrNos(vendorCode);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(srNos), HttpStatus.OK);
    }

    @DeleteMapping("/request/{id}")
    @Operation(summary = "Delete a diversion request (only DRAFT or returned statuses allowed)")
    public ResponseEntity<APIResponse> deleteRequest(@PathVariable Long id) {
        diversionService.deleteRequest(id);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(Map.of("message", "Deleted successfully")), HttpStatus.OK);
    }

    @PutMapping("/request/{id}")
    @Operation(summary = "Update a diversion request (only DRAFT or returned statuses allowed)")
    public ResponseEntity<APIResponse> updateRequest(@PathVariable Long id, @RequestBody DiversionRequestDto dto) {
        dto.setId(id);
        DiversionRequestDto updated = diversionService.updateRequest(dto);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
    }

    @GetMapping(value = {"/view-doc/{id}", "/view-doc/{id}.pdf"}, produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "View / stream railway permission doc as PDF")
    public ResponseEntity<byte[]> viewRailwayDoc(@PathVariable Long id) {
        try {
            byte[] pdfBytes = downloadRailwayDocBytes(id);
            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Railway_Permission_" + id + ".pdf\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.warn("Error viewing Railway Permission Doc for request {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping(value = {"/download-doc/{id}", "/download-doc/{id}.pdf"}, produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download railway permission doc as PDF")
    public ResponseEntity<byte[]> downloadRailwayDoc(@PathVariable Long id) {
        try {
            byte[] pdfBytes = downloadRailwayDocBytes(id);
            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Railway_Permission_" + id + ".pdf\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.warn("Error downloading Railway Permission Doc for request {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/doc-url/{id}")
    @Operation(summary = "Get railway permission doc URL and signed data for a diversion request")
    public ResponseEntity<APIResponse> getRailwayDocUrl(@PathVariable Long id) {
        String rawUrl = diversionService.getRailwayDocUrl(id);
        String viewUrl = "/api/erc/diversion/view-doc/" + id + ".pdf";
        String base64Data = null;
        try {
            byte[] bytes = downloadRailwayDocBytes(id);
            if (bytes != null && bytes.length > 0) {
                base64Data = java.util.Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            log.warn("Failed to generate base64 for railway doc {}: {}", id, e.getMessage());
        }

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("railwayPermissionDocUrl", viewUrl);
        resp.put("viewUrl", viewUrl);
        resp.put("signedData", base64Data != null ? base64Data : "");
        resp.put("rawBlobUrl", rawUrl != null ? rawUrl : "");
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(resp), HttpStatus.OK);
    }

    private byte[] downloadRailwayDocBytes(Long id) {
        String docUrl = diversionService.getRailwayDocUrl(id);
        if (docUrl == null || docUrl.trim().isEmpty()) {
            return null;
        }
        String cleanUrl = docUrl.trim();

        // 1. If stored as Base64 data URL
        if (cleanUrl.startsWith("data:")) {
            try {
                String b64 = cleanUrl.contains(",") ? cleanUrl.split(",")[1] : cleanUrl;
                return java.util.Base64.getDecoder().decode(b64.trim());
            } catch (Exception e) {
                log.warn("Failed to decode base64 doc for diversion request {}: {}", id, e.getMessage());
            }
        }

        // 2. Download from Azure container (sarthi-diversion-docs)
        try {
            String blobName = null;
            String containerName = (diversionDocsContainer != null && !diversionDocsContainer.isBlank())
                    ? diversionDocsContainer.trim() : "sarthi-diversion-docs";

            if (cleanUrl.contains(containerName + "/")) {
                blobName = cleanUrl.substring(cleanUrl.indexOf(containerName + "/") + containerName.length() + 1);
            } else if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                try {
                    java.net.URI uri = new java.net.URI(cleanUrl);
                    String path = uri.getPath();
                    if (path.startsWith("/")) path = path.substring(1);
                    int slashIdx = path.indexOf('/');
                    if (slashIdx >= 0) {
                        blobName = path.substring(slashIdx + 1);
                    } else {
                        blobName = path;
                    }
                } catch (Exception ignored) {}
            } else {
                blobName = cleanUrl;
            }

            if (blobName != null && !blobName.isBlank()) {
                String decodedName = java.net.URLDecoder.decode(blobName, java.nio.charset.StandardCharsets.UTF_8);
                BlobContainerClient container = getDiversionContainer();

                BlobClient blobClient = container.getBlobClient(decodedName);
                if (!blobClient.exists() && !decodedName.equals(blobName)) {
                    blobClient = container.getBlobClient(blobName);
                }

                if (blobClient.exists()) {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    blobClient.downloadStream(out);
                    log.info("Downloaded railway permission doc for request {} ({} bytes)", id, out.size());
                    return out.toByteArray();
                } else {
                    log.warn("Blob '{}' (or '{}') does not exist in container '{}'", decodedName, blobName, containerName);
                }
            }
        } catch (Exception e) {
            log.error("Failed to download railway doc from Azure for request {}: {}", id, e.getMessage(), e);
        }

        return null;
    }

    @GetMapping("/call-source-reference/{callId}")
    @Operation(summary = "Get Source IC historical test reference for IE inspection view")
    public ResponseEntity<APIResponse> getCallSourceReference(@PathVariable Long callId) {
        CallSourceReferenceDto ref = diversionService.getCallSourceReference(callId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(ref), HttpStatus.OK);
    }

    @GetMapping("/history/{id}")
    @Operation(summary = "Get workflow history for a diversion request")
    public ResponseEntity<APIResponse> getDiversionHistory(@PathVariable Long id) {
        List<com.sarthi.entity.ercdiversion.ErcDiversionWorkflowHistory> history =
                diversionHistoryRepository.findByDiversionRequestIdOrderByCreatedDateAsc(id);
        List<java.util.Map<String, Object>> result = history.stream().map(h -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("actionType", h.getAction());
            m.put("actionTimestamp", h.getCreatedDate());
            m.put("actorUsername", h.getActionUserName() != null ? h.getActionUserName() : h.getActionUserId());
            m.put("actorRole", h.getActionRole());
            m.put("fromStatus", h.getFromStatus());
            m.put("toStatus", h.getToStatus());
            m.put("actionRemarks", h.getRemarks());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }

    @PostMapping("/upload-railway-doc")
    @Operation(summary = "Upload Railway Permission document for a diversion request")
    public ResponseEntity<APIResponse> uploadRailwayDoc(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(Map.of("error", "No file provided")), HttpStatus.BAD_REQUEST);
            }
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "railway_doc.pdf";
            String blobName = "diversion/railway/" + UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._\\-]", "_");

            BlobClient blobClient = getDiversionContainer().getBlobClient(blobName);
            try (InputStream is = file.getInputStream()) {
                blobClient.upload(is, file.getSize(), true);
            }
            String url = blobClient.getBlobUrl();
            log.info("Uploaded railway permission doc: {}", url);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(Map.of("url", url, "blobName", blobName)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to upload railway permission doc", e);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(Map.of("error", "Upload failed: " + e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-ic-number")
    @Operation(summary = "Fetch IC Number of a call from IC edit tables")
    public ResponseEntity<APIResponse> getIcNumber(
            @RequestParam("callNo") String callNo,
            @RequestParam(value = "stage", required = false) String stage) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(Map.of("error", "callNo is required")), HttpStatus.BAD_REQUEST);
        }
        String cleanCall = callNo.trim();
        if (cleanCall.contains("/") && cleanCall.length() > cleanCall.indexOf('/') + 1) {
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(Map.of("callNo", cleanCall, "icNumber", cleanCall)), HttpStatus.OK);
        }

        String resolvedIc = resolveIcFromEditTables(cleanCall, stage);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(Map.of(
                "callNo", cleanCall,
                "icNumber", resolvedIc != null ? resolvedIc : cleanCall
        )), HttpStatus.OK);
    }

    private String resolveIcFromEditTables(String callNo, String stage) {
        java.util.List<String> tables = new java.util.ArrayList<>();
        if ("PROCESS".equalsIgnoreCase(stage) || "STAGE_II_MANUFACTURING".equalsIgnoreCase(stage)) {
            tables.add("process_ic_edit");
            tables.add("rm_ic_edit");
            tables.add("final_ic_edit");
        } else if ("RAW_MATERIAL".equalsIgnoreCase(stage) || "STAGE_I_RAW_MATERIAL".equalsIgnoreCase(stage)) {
            tables.add("rm_ic_edit");
            tables.add("process_ic_edit");
            tables.add("final_ic_edit");
        } else if ("FINAL".equalsIgnoreCase(stage) || "STAGE_III_FINISHED".equalsIgnoreCase(stage)) {
            tables.add("final_ic_edit");
            tables.add("sleeper_final_ic_edit");
            tables.add("process_ic_edit");
            tables.add("rm_ic_edit");
        } else {
            tables.add("process_ic_edit");
            tables.add("rm_ic_edit");
            tables.add("final_ic_edit");
            tables.add("sleeper_final_ic_edit");
        }
        tables.add("railpad_process_ic_edit");
        tables.add("railpad_final_ic_edit");

        for (String table : tables) {
            try {
                String sql = "SELECT ic_number FROM " + table + " " +
                             "WHERE ic_number = ? " +
                             "   OR SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1) = ? " +
                             "   OR ic_number LIKE CONCAT('%/', ?, '/%') " +
                             "   OR ic_number LIKE CONCAT('%', ?, '%') " +
                             "ORDER BY id DESC LIMIT 1";
                java.util.List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("ic_number"), callNo, callNo, callNo, callNo);
                if (list != null && !list.isEmpty() && list.get(0) != null && !list.get(0).isBlank()) {
                    log.info("Resolved IC Number '{}' for call '{}' from table '{}'", list.get(0), callNo, table);
                    return list.get(0).trim();
                }
            } catch (Exception ex) {
                log.debug("Error querying table {} for call {}: {}", table, callNo, ex.getMessage());
            }
        }

        try {
            String csSql = "SELECT ic_number FROM certificate_storage " +
                           "WHERE ic_number LIKE CONCAT('%/', ?, '/%') " +
                           "   OR ic_number LIKE CONCAT('%', ?, '%') " +
                           "   OR file_name LIKE CONCAT('%', ?, '%') " +
                           "ORDER BY id DESC LIMIT 1";
            java.util.List<String> list = jdbcTemplate.query(csSql, (rs, rowNum) -> rs.getString("ic_number"), callNo, callNo, callNo);
            if (list != null && !list.isEmpty() && list.get(0) != null && !list.get(0).isBlank()) {
                return list.get(0).trim();
            }
        } catch (Exception ignored) {}

        return null;
    }
}
