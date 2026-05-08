package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailTransitionActionReqDto;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(workflowService.performTransitionAction(railTransitionActionReqDto)), HttpStatus.OK);
    }
}
