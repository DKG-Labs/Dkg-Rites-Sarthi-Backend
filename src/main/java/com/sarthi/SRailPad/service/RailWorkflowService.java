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

    public void submitRailpadPendingRemap(RailpadRemapSubmitDto dto);

    public String saveRailpadMapping(com.sarthi.SRailPad.dto.RailpadPoiIeMappingReqDto req);

    public String resolveRailpadPoiCode(String plantId, String providedPoiCode);

    public List<String> getMappedMainIeNameByCallNo(String callNo);

    public List<String> getMappedPlantIdsForUser(Integer userId, String ieType);

    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName);

    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName, String plantId);

    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName, String plantId, Long workflowId);

    public List<RailWorkflowTransactionDto> workflowTransitionHistory(String requestId);

    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions();

    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions(Long userId, String plantId);

    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions(Long userId, String plantId, Long workflowId);

    public List<RailWorkflowTransactionDto> allFinalCompletedWorkflowTransitions();
    
    public List<String> getMappedCompanyNames(Long userId);

    public List<String> getPlantsByCompanyName(String companyName);

    public List<com.sarthi.SRailPad.dto.RailCancelledPaymentCallDto> getCancelledCallsForPayment(String plantId, String vendorCode);

    public boolean isPlantBlockedForCallRaising(String plantId, String vendorCode);

    public com.sarthi.SRailPad.entity.RailCallCancellationDetail getCancellationDetails(String callNo);
}
