package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.dto.WorkflowDtos.TransitionActionReqDto;
import com.sarthi.service.WorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.sarthi.Sleeper.dto.SleeperRemapSubmitDto;

@RestController
@RequestMapping("/api/sleeper-workflow")
public class SleeperWorkflow {

    @Autowired
    SleeperWorkflowService workflowService;


    @PostMapping("/initiateWorkflow")
    public ResponseEntity<Object> initiateWorkflow(@RequestParam String requestId, @RequestParam Long moduleId, @RequestParam Long workflowId, @RequestParam Long createdBy,@RequestParam String vendorCode,@RequestParam String plantId)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.initiateWorkflow(requestId, moduleId, workflowId, createdBy, vendorCode, plantId)), HttpStatus.OK);
    }

    @PostMapping("/performTransitionAction")
    public ResponseEntity<Object> performTransitionAction(@RequestBody SleeperTransitionActionReqDto transitionActionReqDto)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.performTransitionAction(transitionActionReqDto)), HttpStatus.OK);
    }

    @GetMapping("/allPendingWorkflowTransition")
    public ResponseEntity<Object> allPendingWorkflowTransition(
            @RequestParam String roleName,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) String rio,
            @RequestParam(required = false) String plantId) {

        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(workflowService.allPendingWorkflowTransitions(roleName, assignedTo, rio, plantId)), 
                HttpStatus.OK
        );
    }

    @GetMapping("/allPendingWorkflowTransitionModuelWise")
    public ResponseEntity<Object> allPendingWorkflowTransition(
            @RequestParam String roleName,
            @RequestParam Integer moduleId,
            @RequestParam(required = false) String plantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "workflowTransitionId"));

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        workflowService.allPendingWorkflowTransitionsBasedOnModule(
                                roleName,
                                moduleId,
                                plantId,
                                pageable)),
                HttpStatus.OK);
    }

    @GetMapping("/WorkflowTransitionHistory")
    public ResponseEntity<Object> WorkflowTransitionHistory(@RequestParam String requestId)  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.workflowTransitionHistory(requestId)), HttpStatus.OK);
    }

    @GetMapping("/allCompletedCalls")
    public ResponseEntity<Object> AllCompletedTransition()  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allCompletedWorkflowTransitions()), HttpStatus.OK);
    }

    @GetMapping("/pendingVerifiedCalls")
    public ResponseEntity<Object> getPendingVerifiedCalls()  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.getPendingVerifiedCalls()), HttpStatus.OK);
    }

    @GetMapping("/allFInalCallCompletedCalls")
    public ResponseEntity<Object> AllFinalCallCompletedTransition()  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allFinalCompletedWorkflowTransitions()), HttpStatus.OK);
    }

    @GetMapping("/allCompletedWorkflowTransitionModuleWise")
    public ResponseEntity<Object> allCompletedWorkflowTransition(
            @RequestParam Integer moduleId,
            @RequestParam(required = false) String plantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "workflowTransitionId"));

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        workflowService.allCompletedWorkflowTransitions(moduleId, plantId, pageable)),
                HttpStatus.OK);
    }

    @GetMapping("/remap-available-users")
    public ResponseEntity<Object> getRemapAvailableUsers() {
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(workflowService.getSleeperRemapAvailableUsers()),
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
    public ResponseEntity<Object> submitRemap(@RequestBody SleeperRemapSubmitDto dto) {
        try {
            workflowService.submitSleeperRemap(dto);
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

}
