package com.sarthi.controller;

import com.sarthi.dto.WorkflowDtos.TransitionActionReqDto;
import com.sarthi.service.WorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WorkflowController {

    @Autowired
    WorkflowService workflowService;


    @PostMapping("/initiateWorkflow")
    public ResponseEntity<Object> initiateWorkflow(@RequestParam String requestId, @RequestParam String workflowName, @RequestParam Integer createdBy,  @RequestParam String pincode)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.initiateWorkflow(requestId, createdBy, workflowName, pincode)), HttpStatus.OK);
    }

    @PostMapping("/performTransitionAction")
    public ResponseEntity<Object> performTransitionAction(@RequestBody TransitionActionReqDto transitionActionReqDto)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.performTransitionAction(transitionActionReqDto)), HttpStatus.OK);
    }

    @GetMapping("/allPendingWorkflowTransition")
    public ResponseEntity<Object> allPendingWorkflowTransition(@RequestParam String roleName)  {
        if(roleName.equalsIgnoreCase("Process IE")){
            roleName="IE";
        }
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allPendingWorkflowTransition(roleName)), HttpStatus.OK);
    }

    @GetMapping("/allPendingWorkflowTransitionByPoi")
    public ResponseEntity<Object> allPendingWorkflowTransitionByPoi(@RequestParam String roleName, @RequestParam String poi)  {
        if(roleName.equalsIgnoreCase("Process IE")){
            roleName="IE";
        }
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.getPendingWorkflowByPoi(roleName, poi)), HttpStatus.OK);
    }
    @GetMapping("/allPendingQtyEditTransitions")
    public ResponseEntity<Object> allPendingQtyEditTransitions(@RequestParam String roleName)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allPendingQtyEditTransitions(roleName)), HttpStatus.OK);
    }

    @GetMapping("/workflowTransitionHistory")
    public ResponseEntity<Object> workflowTransitionHistory(@RequestParam String requestId)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.workflowTransitionHistory(requestId)), HttpStatus.OK);
    }

    @GetMapping("/workflowTransitionsPaymentsBlocked")  //vendor payment updation
    public ResponseEntity<Object> workflowTransitionPaymentsBlocked()  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allBlockedWorkflowTransitions()), HttpStatus.OK);
    }

    @GetMapping("/getWorkflowByName")
    public ResponseEntity<Object> getWorkflowByName(@RequestParam String workflowName) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.workflowByWorkflowName(workflowName)), HttpStatus.OK);

    }


    @GetMapping("/getTransitionsByWorkflowId")
    public ResponseEntity<Object> getTransitionsByWorkflowId(@RequestParam Integer workflowId) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.transitionsByWorkflowId(workflowId)), HttpStatus.OK);
    }


    @GetMapping("/callCompleteddata")
    public ResponseEntity<Object> getCallCompletedData(@RequestParam Integer modifiedBy) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService. getInspectionCompletedByModifiedUser(modifiedBy)), HttpStatus.OK);
    }

    @GetMapping("/callSigneddata")
    public ResponseEntity<Object> getCallSignedData(@RequestParam Integer modifiedBy) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.getSignedInspectionByModifiedUser(modifiedBy)), HttpStatus.OK);
    }


    @PostMapping("/api/workflow/withdraw")
    public ResponseEntity<Object> withdrawCall(@RequestBody TransitionActionReqDto dto) {

        return ResponseEntity.ok(
                workflowService.withdrawCall(dto)
        );
    }

    @GetMapping("/dashboardKPIs")
    public ResponseEntity<Object> getDashboardKPIs(@RequestParam(required = false) String rio) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.getDashboardKPIs(rio)), HttpStatus.OK);
    }

    @GetMapping("/allVerifiedWorkflowTransitions")
    public ResponseEntity<Object> allVerifiedWorkflowTransitions(@RequestParam(required = false) String rio) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allVerifiedWorkflowTransitions(rio)), HttpStatus.OK);
    }

    @GetMapping("/allDisposedWorkflowTransitions")
    public ResponseEntity<Object> allDisposedWorkflowTransitions(@RequestParam(required = false) String rio) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allDisposedWorkflowTransitions(rio)), HttpStatus.OK);
    }

}