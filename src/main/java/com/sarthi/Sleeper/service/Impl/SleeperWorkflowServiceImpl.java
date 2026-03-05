package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SleeperWorkflowServiceImpl implements SleeperWorkflowService {

  /*  @Autowired
    private SleeperWorkflowRepository repository;

    @Override
    public SleeperWorkflowTransactionDto initiateWorkflow(
            String requestId,
            Long moduleId,
            Long workflowId,
            Long createdBy) {

        SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

        tx.setRequestId(requestId);
        tx.setModuleId(moduleId);
        tx.setWorkflowId(workflowId);

        tx.setAction("SUBMIT");
        tx.setStatus("SUBMITTED");

        tx.setCreatedBy(createdBy);
        tx.setAssignedToUser(assignedToUser);

        SleeperWorkflowTransaction saved = repository.save(tx);

        return mapToDto(saved);
    }


    @Override
    public SleeperWorkflowTransactionDto performTransitionAction(
            SleeperTransitionActionReqDto req) {

        SleeperWorkflowTransaction current =
                repository.findById(req.getWorkflowTransitionId())
                        .orElseThrow(() -> new RuntimeException("Workflow not found"));

        String status = determineStatus(req.getAction());

        SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

        tx.setRequestId(req.getRequestId());
        tx.setModuleId(req.getModuleId());
        tx.setWorkflowId(current.getWorkflowId());

        tx.setAction(req.getAction());
        tx.setStatus(status);
        tx.setRemarks(req.getRemarks());

        tx.setModifiedBy(req.getActionBy());
        tx.setAssignedToUser(1);

        SleeperWorkflowTransaction saved = repository.save(tx);

        return mapToDto(saved);
    }

    @Override
    public List<SleeperWorkflowTransactionDto> allPendingWorkflowTransitions(
            Long userId) {

        List<String> status =
                List.of("SUBMITTED", "RESUBMITTED");

        List<SleeperWorkflowTransaction> list =
                repository.findByAssignedToUserAndStatusIn(userId, status);

        return list.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<SleeperWorkflowTransactionDto> workflowTransitionHistory(
            String requestId) {

        List<SleeperWorkflowTransaction> list =
                repository.findByRequestIdOrderByCreatedDateAsc(requestId);

        return list.stream()
                .map(this::mapToDto)
                .toList();
    }

    private String determineStatus(String action) {

        switch (action) {

            case "SUBMIT":
                return "SUBMITTED";

            case "REQUEST_BACK":
                return "REQUESTED_BACK";

            case "RESUBMIT":
                return "RESUBMITTED";

            case "VERIFY":
                return "VERIFIED";

            case "VERIFY_PO_DETAILS":
                return "VERIFIED";

            default:
                return "PENDING";
        }
    }

    private SleeperWorkflowTransactionDto mapToDto(
            SleeperWorkflowTransaction tx) {

        return SleeperWorkflowTransactionDto.builder()
                .id(tx.getId())
                .moduleId(tx.getModuleId())
                .workflowId(tx.getWorkflowId())
                .requestId(tx.getRequestId())
                .action(tx.getAction())
                .status(tx.getStatus())
                .remarks(tx.getRemarks())
                .assignedToUser(tx.getAssignedToUser())
                .actionBy(tx.getModifiedBy())
                .createdDate(tx.getCreatedDate())
                .build();
    }*/

}
