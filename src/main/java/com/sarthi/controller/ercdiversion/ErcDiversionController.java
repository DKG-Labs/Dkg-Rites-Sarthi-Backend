package com.sarthi.controller.ercdiversion;

import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.dto.ercdiversion.*;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;
import com.sarthi.service.ercdiversion.ErcDiversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/erc/diversion")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ERC Material Diversion", description = "Endpoints for ERC Material Diversion and PO Sr. No. Reallocation")
public class ErcDiversionController {

    private final ErcDiversionService diversionService;

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
    public ResponseEntity<APIResponse> getPendingRequestsForCm(@RequestParam String cmUserId) {
        List<DiversionRequestDto> list = diversionService.getPendingRequestsForCm(cmUserId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/sbu/pending-requests")
    @Operation(summary = "Get pending requests for SBU Head")
    public ResponseEntity<APIResponse> getPendingRequestsForSbu(@RequestParam String rioId) {
        List<DiversionRequestDto> list = diversionService.getPendingRequestsForSbu(rioId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @PostMapping("/workflow/action")
    @Operation(summary = "Process workflow action (Approve, Return, Resubmit)")
    public ResponseEntity<APIResponse> processWorkflowAction(@RequestBody WorkflowActionDto actionDto) {
        log.info("Request: Workflow action: {} for request ID: {}", actionDto.getAction(), actionDto.getRequestId());
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

    @GetMapping("/call-source-reference/{callId}")
    @Operation(summary = "Get Source IC historical test reference for IE inspection view")
    public ResponseEntity<APIResponse> getCallSourceReference(@PathVariable Long callId) {
        CallSourceReferenceDto ref = diversionService.getCallSourceReference(callId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(ref), HttpStatus.OK);
    }
}
