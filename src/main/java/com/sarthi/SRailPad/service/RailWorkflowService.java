package com.sarthi.SRailPad.service;

import com.sarthi.SRailPad.dto.RailTransitionActionReqDto;
import com.sarthi.SRailPad.dto.RailWorkflowTransactionDto;
import org.springframework.stereotype.Service;

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



}
