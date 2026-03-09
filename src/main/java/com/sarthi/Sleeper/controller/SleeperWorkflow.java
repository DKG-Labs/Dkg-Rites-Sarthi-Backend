package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.dto.WorkflowDtos.TransitionActionReqDto;
import com.sarthi.service.WorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sleeper-workflow")
public class SleeperWorkflow {

    @Autowired
    SleeperWorkflowService workflowService;


    @PostMapping("/initiateWorkflow")
    public ResponseEntity<Object> initiateWorkflow(@RequestParam String requestId, @RequestParam String workflowName, @RequestParam Long workflowId, @RequestParam Long moduleId, @RequestParam Long createdBy)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.initiateWorkflow(requestId, workflowId, moduleId, createdBy)), HttpStatus.OK);
    }

    @PostMapping("/performTransitionAction")
    public ResponseEntity<Object> performTransitionAction(@RequestBody SleeperTransitionActionReqDto transitionActionReqDto)  {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.performTransitionAction(transitionActionReqDto)), HttpStatus.OK);
    }

    @GetMapping("/allPendingWorkflowTransition")
    public ResponseEntity<Object> allPendingWorkflowTransition(@RequestParam Long userid)  {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.allPendingWorkflowTransitions(userid)), HttpStatus.OK);
    }
}
