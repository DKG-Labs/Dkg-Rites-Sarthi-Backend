package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.Level1DTO;
import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface SleeperWorkflowService {


  public SleeperWorkflowTransactionDto initiateWorkflow(
            String requestId,
            Long moduleId,
            Long workflowId,
            Long createdBy, String vendorCode, String plantId
    );

   public SleeperWorkflowTransactionDto performTransitionAction(
            SleeperTransitionActionReqDto req);

   public List<SleeperWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName);

   public Page<SleeperWorkflowTransactionDto> allPendingWorkflowTransitionsBasedOnModule(String roleName, int moduleId, Pageable pageable) ;

        public List<SleeperWorkflowTransactionDto> workflowTransitionHistory(
            String requestId);


    public List<SleeperWorkflowTransactionDto> allCompletedWorkflowTransitions();

    public List<SleeperWorkflowTransactionDto> allFinalCompletedWorkflowTransitions();

    public Page<SleeperWorkflowTransactionDto> allCompletedWorkflowTransitions(
            Integer moduleId,
            Pageable pageable);

}
