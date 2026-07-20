package com.sarthi.SRailPad.service;

import com.sarthi.SRailPad.dto.RailTransitionActionReqDto;
import com.sarthi.SRailPad.dto.RailWorkflowTransactionDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.sarthi.SRailPad.dto.RailpadRemapSubmitDto;

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



    public List<RailWorkflowTransactionDto> getPendingVerifiedCalls();

    public List<Map<String, Object>> getRailpadRemapAvailableUsers();

    public void submitRailpadRemap(RailpadRemapSubmitDto dto);

    public List<String> getMappedPlantIdsForUser(Integer userId, String ieType);

    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName);

    public List<RailWorkflowTransactionDto> workflowTransitionHistory(String requestId);

    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions();

    public List<RailWorkflowTransactionDto> allFinalCompletedWorkflowTransitions();
    
    public List<String> getMappedCompanyNames(Long userId);

    public List<String> getPlantsByCompanyName(String companyName);
}
