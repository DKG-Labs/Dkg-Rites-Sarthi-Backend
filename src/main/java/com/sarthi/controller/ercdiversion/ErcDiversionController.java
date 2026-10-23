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
}
