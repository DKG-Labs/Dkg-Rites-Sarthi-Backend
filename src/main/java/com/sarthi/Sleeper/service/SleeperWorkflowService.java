package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SleeperWorkflowService {


  public SleeperWorkflowTransactionDto initiateWorkflow(
            String requestId,
            Long moduleId,
            Long workflowId,
            Long createdBy
    );

   public SleeperWorkflowTransactionDto performTransitionAction(
            SleeperTransitionActionReqDto req);

   public List<SleeperWorkflowTransactionDto> allPendingWorkflowTransitions(
            Long userId);

   public List<SleeperWorkflowTransactionDto> workflowTransitionHistory(
            String requestId);


}
