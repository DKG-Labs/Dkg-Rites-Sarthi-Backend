package com.sarthi.SRailPad.service;

import com.sarthi.SRailPad.dto.RailTransitionActionReqDto;
import com.sarthi.SRailPad.dto.RailWorkflowTransactionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RailWorkflowService {

    public RailWorkflowTransactionDto initiateWorkflow(
            String requestId,
            Long moduleId,
            Long workflowId,
            Long createdBy,
            String vendorCode,
            String plantId, String shift);

    public RailWorkflowTransactionDto performTransitionAction(
            RailTransitionActionReqDto req);



    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName);

    public List<RailWorkflowTransactionDto> workflowTransitionHistory(String requestId);

    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions();

    public List<RailWorkflowTransactionDto> allFinalCompletedWorkflowTransitions();
    
    public List<String> getMappedCompanyNames(Long userId);

    public List<String> getPlantsByCompanyName(String companyName);
}
