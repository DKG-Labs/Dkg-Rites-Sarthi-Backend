package com.sarthi.service;

import com.sarthi.dto.DashboardKPIsDto;
import com.sarthi.dto.IcWorkflowTransitionDto;
import com.sarthi.dto.WorkflowDto;
import com.sarthi.dto.WorkflowDtos.TransitionActionReqDto;
import com.sarthi.dto.WorkflowDtos.TransitionDto;
import com.sarthi.dto.WorkflowDtos.WorkflowTransitionDto;
import com.sarthi.entity.WorkflowTransition;
import org.springframework.stereotype.Service;

import java.util.List;


public interface WorkflowService {

    public WorkflowTransitionDto initiateWorkflow(String requestId, Integer createdBy, String workflowName, String pincode);

    public WorkflowTransitionDto performTransitionAction(TransitionActionReqDto req);

    public List<WorkflowTransitionDto> allPendingWorkflowTransition(String roleName);

    public List<WorkflowTransitionDto> workflowTransitionHistory(String requestId);

    public List<WorkflowTransitionDto> allPendingQtyEditTransitions(String roleName);

    public List<WorkflowTransitionDto> allBlockedWorkflowTransitions();

    public DashboardKPIsDto getDashboardKPIs(String rio);
    
    public List<WorkflowTransitionDto> allVerifiedWorkflowTransitions(String rio);

    public WorkflowDto workflowByWorkflowName(String workflowName);

    public List<TransitionDto> transitionsByWorkflowId(Integer workflowId);

    public List<IcWorkflowTransitionDto> getInspectionCompletedByModifiedUser(Integer modifiedBy);

    public List<IcWorkflowTransitionDto> getSignedInspectionByModifiedUser(Integer modifiedBy);

    public String withdrawCall(TransitionActionReqDto dto);
}