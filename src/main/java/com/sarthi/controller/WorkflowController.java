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

    @GetMapping("/api/call/po-item-details")
    public ResponseEntity<Object> getCallPoItemDetails(
            @RequestParam(required = false) String callNo,
            @RequestParam(required = false) String poNo,
            @RequestParam(required = false) String itemSrNo) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(
                workflowService.getPoItemCalculationDetails(callNo, poNo, itemSrNo)), HttpStatus.OK);
    }


    @DeleteMapping("/inspection-complete/{requestId}")
    public ResponseEntity<String> deleteInspectionCompleteRequest(
            @PathVariable String requestId,
            @RequestParam Integer deletedBy) {

        workflowService.deleteInspectionCompleteRequest(
                requestId,
                deletedBy
        );

        return ResponseEntity.ok(
                "Request " + requestId + " deleted successfully"
        );
    }

        @DeleteMapping("/esign/{requestId}")
        public ResponseEntity<String> deleteEsignTransition(
                @PathVariable String requestId,
                @RequestParam Integer deletedBy) {

            workflowService.deleteEsignTransition(
                    requestId,
                    deletedBy
            );

            return ResponseEntity.ok(
                    "ESign transition deleted successfully for request ID: "
                            + requestId
            );
        }

    @GetMapping("/cancelledCallsForPayment")
    public ResponseEntity<Object> getCancelledCallsForPayment(
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String vendorCode) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(workflowService.getCancelledCallsForPayment(plantId, vendorCode)),
                HttpStatus.OK
        );
    }

    @GetMapping("/checkPlantPaymentBlock")
    public ResponseEntity<Object> checkPlantPaymentBlock(
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String vendorCode) {
        boolean isBlocked = workflowService.isPlantBlockedForCallRaising(plantId, vendorCode);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(java.util.Map.of(
                        "blocked", isBlocked,
                        "plantId", plantId != null ? plantId : "",
                        "vendorCode", vendorCode != null ? vendorCode : ""
                )),
                HttpStatus.OK
        );
    }

    @GetMapping("/cancellationDetails/{callNo}")
    public ResponseEntity<Object> getCancellationDetails(@PathVariable String callNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(workflowService.getCancellationDetails(callNo)),
                HttpStatus.OK
        );
    }

    @GetMapping("/getCancellationDetails")
    public ResponseEntity<Object> getCancellationDetailsByParam(@RequestParam String callNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(workflowService.getCancellationDetails(callNo)),
                HttpStatus.OK
        );
    }

    /**
     * Proxies the IBS get-bill-details API call from the ERC vendor frontend.
     * Request body: { callNo, caseNo, callDate (DD-MM-YYYY), ibsCallSno }
     */
    @PostMapping("/verify-ibs-payment")
    public ResponseEntity<Object> verifyIbsPayment(@RequestBody java.util.Map<String, Object> req) {
        try {
            String caseNo = (String) req.get("caseNo");
            String callDate = (String) req.get("callDate");
            Object snoObj = req.get("ibsCallSno");
            int ibsCallSno = 0;
            if (snoObj instanceof Integer) {
                ibsCallSno = (Integer) snoObj;
            } else if (snoObj instanceof String && !((String) snoObj).isBlank()) {
                try { ibsCallSno = Integer.parseInt(((String) snoObj).trim()); } catch (NumberFormatException ignored) {}
            }

            if (caseNo == null || caseNo.isBlank() || callDate == null || callDate.isBlank() || ibsCallSno == 0) {
                return new ResponseEntity<>(
                        java.util.Map.of("status", "error", "message", "caseNo, callDate, and ibsCallSno are required."),
                        HttpStatus.BAD_REQUEST
                );
            }

            java.util.Map<String, Object> ibsResponse = workflowService.verifyIbsPayment(caseNo, callDate, ibsCallSno);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(ibsResponse), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", "IBS verification failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Marks a cancelled call's payment as "Approved by RITES Finance" after IBS confirms the bill.
     * Request body: { callNo }
     */
    @PostMapping("/mark-payment-approved")
    public ResponseEntity<Object> markPaymentApproved(@RequestBody java.util.Map<String, Object> req) {
        try {
            String callNo = (String) req.get("callNo");
            if (callNo == null || callNo.isBlank()) {
                return new ResponseEntity<>(
                        java.util.Map.of("status", "error", "message", "callNo is required."),
                        HttpStatus.BAD_REQUEST
                );
            }
            workflowService.markPaymentApprovedByIbs(callNo.trim());
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            java.util.Map.of("message", "Payment approved successfully. Call raising is now unblocked.", "callNo", callNo.trim())
                    ),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    java.util.Map.of("status", "error", "message", "Failed to mark payment approved: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}