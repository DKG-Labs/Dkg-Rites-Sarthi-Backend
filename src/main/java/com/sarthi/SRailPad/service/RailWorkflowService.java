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

    /**
     * Proxies a call to the IBS get-bill-details API and returns the raw JSON response as a Map.
     * @param caseNo     IBS case number (e.g. N26060373)
     * @param callDate   Call receive date formatted as DD-MM-YYYY
     * @param ibsCallSno IBS call serial number
     * @return Map representing the IBS API JSON response
     */
    public java.util.Map<String, Object> verifyIbsPayment(String caseNo, String callDate, int ibsCallSno);

    /**
     * Marks a cancelled call's payment status as "Approved by RITES Finance" in the
     * RailVendorFinancialLiability table, which unblocks call raising for that plant.
     * @param callNo The internal call number (e.g. RPF-082526001)
     */
    public void markPaymentApprovedByIbs(String callNo);
}
