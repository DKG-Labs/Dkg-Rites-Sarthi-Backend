package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailTransitionActionReqDto;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sarthi.SRailPad.dto.RailpadRemapSubmitDto;

@RestController
@RequestMapping("/api/railpad-workflow")
@AllArgsConstructor
public class RailWorkFlowController {

    private RailWorkflowService workflowService;

    @PostMapping("/initiateWorkflow")
    public ResponseEntity<Object> initiateWorkflow(@RequestParam String requestId, @RequestParam Long moduleId, @RequestParam Long workflowId, @RequestParam Long createdBy, @RequestParam String vendorCode, @RequestParam String plantId,  @RequestParam String shift)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.initiateWorkflow(requestId, moduleId, workflowId, createdBy, vendorCode, plantId, shift)), HttpStatus.OK);
    }

    @PostMapping("/performTransitionAction")
    public ResponseEntity<Object> performTransitionAction(@RequestBody RailTransitionActionReqDto railTransitionActionReqDto)  {
        System.out.println("[Workflow Controller] performTransitionAction hit for Request: " + railTransitionActionReqDto.getRequestId() + ", Action: " + railTransitionActionReqDto.getAction());
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.performTransitionAction(railTransitionActionReqDto)), HttpStatus.OK);
    }

    @GetMapping("/allPendingWorkflowTransition")
    public ResponseEntity<Object> allPendingWorkflowTransition(
            @RequestParam String roleName,
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) Long workflowId)  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allPendingWorkflowTransitions(roleName, plantId, workflowId)), HttpStatus.OK);
    }

    @GetMapping("/WorkflowTransitionHistory")
    public ResponseEntity<Object> WorkflowTransitionHistory(@RequestParam String requestId)  {
        System.out.println("[Workflow Controller] WorkflowTransitionHistory hit for Request: " + requestId);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.workflowTransitionHistory(requestId)), HttpStatus.OK);
    }

    @GetMapping("/allCompletedCalls")
    public ResponseEntity<Object> AllCompletedTransition(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) Long workflowId)  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allCompletedWorkflowTransitions(userId, plantId, workflowId)), HttpStatus.OK);
    }

    @GetMapping("/allFInalCallCompletedCalls")
    public ResponseEntity<Object> AllFinalCallCompletedTransition()  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allFinalCompletedWorkflowTransitions()), HttpStatus.OK);
    }

    @GetMapping("/getMappedCompanyNames")
    public ResponseEntity<Object> getMappedCompanyNames(@RequestParam Long userId) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.getMappedCompanyNames(userId)), HttpStatus.OK);
    }

    @GetMapping("/getPlantsByCompanyName")
    public ResponseEntity<Object> getPlantsByCompanyName(@RequestParam String companyName) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.getPlantsByCompanyName(companyName)), HttpStatus.OK);
    }

    @GetMapping("/mapped-plant-ids")
    public ResponseEntity<Object> getMappedPlantIds(@RequestParam Integer userId, @RequestParam String ieType) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(workflowService.getMappedPlantIdsForUser(userId, ieType)), HttpStatus.OK);
    }

    @GetMapping("/pendingVerifiedCalls")
    public ResponseEntity<Object> getPendingVerifiedCalls() {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(workflowService.getPendingVerifiedCalls()),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/remap-available-users")
    public ResponseEntity<Object> getRemapAvailableUsers() {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(workflowService.getRailpadRemapAvailableUsers()),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/remap-submit")
    public ResponseEntity<Object> submitRemap(@RequestBody RailpadRemapSubmitDto dto) {
        try {
            workflowService.submitRailpadRemap(dto);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("Remapping successful"),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", "Remapping failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/cancelledCallsForPayment")
    public ResponseEntity<Object> getCancelledCallsForPayment(
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String vendorCode) {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(workflowService.getCancelledCallsForPayment(plantId, vendorCode)),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", "Failed to fetch cancelled calls: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/checkPlantPaymentBlock")
    public ResponseEntity<Object> checkPlantPaymentBlock(
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String vendorCode) {
        try {
            boolean isBlocked = workflowService.isPlantBlockedForCallRaising(plantId, vendorCode);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("isBlocked", isBlocked);
            if (isBlocked) {
                result.put("message", "Call raising is blocked for this plant due to pending cancellation charges.");
            }
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(result),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", "Failed to check plant block status: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
