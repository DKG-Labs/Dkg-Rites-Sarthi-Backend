package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.SleeperModuleRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowMasterRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.constant.AppConstant;
import com.sarthi.entity.WorkflowTransition;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SleeperWorkflowServiceImpl implements SleeperWorkflowService {

    @Autowired
    private SleeperWorkflowRepository repository;
    @Autowired
    private SleeperWorkflowMasterRepository workflowRepository;

    @Autowired
    private SleeperModuleRepository moduleRepository;

        @Override
        public SleeperWorkflowTransactionDto initiateWorkflow(
                String requestId,
                Long moduleId,
                Long workflowId,
                Long createdBy) {

            validateWorkflowAndModule(workflowId, moduleId);

            SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

            tx.setRequestId(requestId);
            tx.setModuleId(moduleId);
            tx.setWorkflowId(workflowId);

            tx.setAction("SUBMIT");
            tx.setStatus("SUBMITTED");

            tx.setCreatedBy(createdBy);
            tx.setAssignedToUser(createdBy);
            tx.setCreatedDate(LocalDateTime.now());

            SleeperWorkflowTransaction saved = repository.save(tx);

            return mapToDto(saved);
        }

        @Override
        public SleeperWorkflowTransactionDto performTransitionAction(
                SleeperTransitionActionReqDto req) {

            SleeperWorkflowTransaction current = repository
                    .findById(req.getWorkflowTransitionId())
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Workflow transition not found"
                            )
                    ));


            String status = determineStatus(req.getAction());

         //   Long nextUser = determineNextUser(req, current);

            SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

            tx.setRequestId(req.getRequestId());
            tx.setModuleId(req.getModuleId());
            tx.setWorkflowId(current.getWorkflowId());

            tx.setAction(req.getAction());
            tx.setStatus(status);
            tx.setRemarks(req.getRemarks());

            tx.setAssignedToUser(req.getActionBy());

            tx.setCreatedBy(current.getCreatedBy());
            tx.setModifiedBy(req.getActionBy());
            tx.setCreatedDate(LocalDateTime.now());

            SleeperWorkflowTransaction saved = repository.save(tx);

            return mapToDto(saved);
        }

        @Override
        public List<SleeperWorkflowTransactionDto> allPendingWorkflowTransitions(Long userId) {

            List<String> status =
                    List.of("SUBMITTED", "RESUBMITTED");

            List<SleeperWorkflowTransaction> list =
                    repository.findByAssignedToUserAndStatusIn(userId, status);

            return list.stream()
                    .map(this::mapToDto)
                    .toList();
        }

        @Override
        public List<SleeperWorkflowTransactionDto> workflowTransitionHistory(String requestId) {

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
                    return "VERIFIED_LOCKED";

                default:
                    return "PENDING";
            }
        }



        private SleeperWorkflowTransactionDto mapToDto(
                SleeperWorkflowTransaction tx) {

            return SleeperWorkflowTransactionDto.builder()
                    .workflowTransitionId(Long.valueOf(tx.getWorkflowTransitionId()))
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
        }


    private void validateWorkflowAndModule(Long workflowId, Long moduleId) {

        boolean workflowExists =
                workflowRepository.existsById(workflowId);

        if (!workflowExists) {
            throw new RuntimeException("Workflow not found: " + workflowId);
        }

        boolean moduleValid =
                moduleRepository.existsByIdAndWorkflowId(moduleId, workflowId);

        if (!moduleValid) {
            throw new RuntimeException(
                    "Module does not belong to workflow");
        }
    }


}
