package com.sarthi.service;

import com.sarthi.dto.FeedbackTransitionActionReqDto;
import com.sarthi.dto.FeedbackWorkflowTransitionDto;
import com.sarthi.dto.WorkflowDtos.TransitionActionReqDto;
import com.sarthi.dto.WorkflowDtos.WorkflowTransitionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FeedbackWorkflowService {

    public FeedbackWorkflowTransitionDto initiateFeedbackWorkflow(
            String feedbackId,
            Integer createdBy,
            String productType,
            String poiCode,
            String plantId,
            Integer roleId);


    public FeedbackWorkflowTransitionDto feedbackPerformTransition(
            FeedbackTransitionActionReqDto req);

    public List<FeedbackWorkflowTransitionDto> getPendingFeedbacks(
            Integer roleId,
            String productType);

}
