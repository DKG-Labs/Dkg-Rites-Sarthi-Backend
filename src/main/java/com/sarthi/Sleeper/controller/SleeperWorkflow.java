package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.dto.WorkflowDtos.TransitionActionReqDto;
import com.sarthi.service.WorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sleeper-workflow")
public class SleeperWorkflow {

    @Autowired
    SleeperWorkflowService workflowService;


    @PostMapping("/initiateWorkflow")
    public ResponseEntity<Object> initiateWorkflow(@RequestParam String requestId, @RequestParam Long moduleId, @RequestParam Long workflowId, @RequestParam Long createdBy)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.initiateWorkflow(requestId, moduleId, workflowId, createdBy)), HttpStatus.OK);
    }

    @PostMapping("/performTransitionAction")
    public ResponseEntity<Object> performTransitionAction(@RequestBody SleeperTransitionActionReqDto transitionActionReqDto)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.performTransitionAction(transitionActionReqDto)), HttpStatus.OK);
    }

    @GetMapping("/allPendingWorkflowTransition")
    public ResponseEntity<Object> allPendingWorkflowTransition(@RequestParam String roleName)  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allPendingWorkflowTransitions(roleName)), HttpStatus.OK);
    }

    @GetMapping("/WorkflowTransitionHistory")
    public ResponseEntity<Object> WorkflowTransitionHistory(@RequestParam String requestId)  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.workflowTransitionHistory(requestId)), HttpStatus.OK);
    }

    @GetMapping("/allCompletedCalls")
    public ResponseEntity<Object> AllCompletedTransition()  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allCompletedWorkflowTransitions()), HttpStatus.OK);
    }




}
