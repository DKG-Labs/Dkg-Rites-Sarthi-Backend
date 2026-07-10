package com.sarthi.controller;

import com.sarthi.dto.FeedbackTransitionActionReqDto;
import com.sarthi.service.FeedbackWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback-workflow")
@RequiredArgsConstructor
public class ProcessIeFeedbackController {

        private final FeedbackWorkflowService feedbackWorkflowService;

        @PostMapping("/initiateWorkflow")
        public ResponseEntity<Object> initiateFeedbackWorkflow(
                @RequestParam String feedbackId,
                @RequestParam Integer createdBy,
                @RequestParam String productType,
                @RequestParam(required = false) String poiCode,
                @RequestParam(required = false) String plantId,
                @RequestParam Integer roleId) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            feedbackWorkflowService.initiateFeedbackWorkflow(
                                    feedbackId,
                                    createdBy,
                                    productType,
                                    poiCode,
                                    plantId,
                                    roleId
                            )
                    ),
                    HttpStatus.OK
            );
        }

        @PostMapping("/performTransitionAction")
        public ResponseEntity<Object> performTransitionAction(
                @RequestBody FeedbackTransitionActionReqDto reqDto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            feedbackWorkflowService.feedbackPerformTransition(reqDto)
                    ),
                    HttpStatus.OK
            );
        }


    @GetMapping("/pending")
    public ResponseEntity<Object> getPendingFeedbacks(
            @RequestParam Integer roleId,
            @RequestParam String productType) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        feedbackWorkflowService.getPendingFeedbacks(
                                roleId,
                                productType)),
                HttpStatus.OK);
    }

}
