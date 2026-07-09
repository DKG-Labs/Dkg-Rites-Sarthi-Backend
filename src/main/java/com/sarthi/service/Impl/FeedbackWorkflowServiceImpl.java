package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.FeedbackTransitionActionReqDto;
import com.sarthi.dto.FeedbackWorkflowTransitionDto;
import com.sarthi.entity.*;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.repository.*;
import com.sarthi.service.FeedbackWorkflowService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FeedbackWorkflowServiceImpl implements FeedbackWorkflowService {

    @Autowired
    private UserMasterRepository userMasterRepository;
    @Autowired
    private RoleMasterRepository roleMasterRepository;
    @Autowired
    private WorkflowMasterRepository workflowMasterRepository;
    @Autowired
    private TransitionMasterRepository transitionMasterRepository;
    @Autowired
    private UserRoleMasterRepository userRoleMasterRepository;
    @Autowired
    private PincodePoIMappingRepository pincodePoIMappingRepository;
    @Autowired
    private FeedbackWorkflowTransitionRepository feedbackWorkflowTransitionRepository;

    private String roleNameById(Integer roleId) {
        if (Objects.nonNull(roleId)) {
            return roleMasterRepository.findById(roleId).orElse(new RoleMaster()).getRoleName();
        } else {
            return null;
        }
    }
    private void validateUserRoleForTransition(
            Integer userId,
            Integer requiredRoleId,
            String actionName) {

        boolean hasRole = userRoleMasterRepository
                .existsByUserIdAndRoleId(userId, requiredRoleId);

        if (!hasRole) {

            String roleName = roleNameById(requiredRoleId);

            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            String.format(
                                    "User is not authorized to perform '%s'. Required role: %s",
                                    actionName,
                                    roleName
                            )
                    )
            );
        }
    }
    @Override
    @Transactional
    public FeedbackWorkflowTransitionDto initiateFeedbackWorkflow(
            String feedbackId,
            Integer createdBy,
            String productType,
            String poiCode,
            Integer plantId) {


        String vendorCode =
                pincodePoIMappingRepository.findVendorCodeByPoiCode(poiCode);

        // Validate User
        UserMaster user = userMasterRepository.findByUserId(createdBy)
                .orElseThrow(() -> new InvalidInputException(
                        new ErrorDetails(
                                AppConstant.USER_NOT_FOUND,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "User not found"
                        )));

        // Workflow Validation
        WorkflowMaster workflow = workflowMasterRepository.findById(3)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Feedback Workflow not found"
                        )));

        // First Transition
        TransitionMaster transition = transitionMasterRepository
                .findByWorkflowIdAndTransitionOrder(
                        workflow.getWorkflowId(),
                        1)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Initial feedback transition not found"
                        )));

        // Role Validation
     validateUserRoleForTransition(createdBy,transition.getCurrentRoleId(),transition.getTransitionName());

        // Prevent Duplicate Workflow
        boolean exists =
                feedbackWorkflowTransitionRepository
                        .existsByFeedbackId(feedbackId);

        if (exists) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Feedback workflow already initiated."
                    )
            );
        }

       UserMaster um = userMasterRepository.findByUserName(vendorCode)
               .orElseThrow(() -> new BusinessException(
                       new ErrorDetails(
                               AppConstant.ERROR_CODE_RESOURCE,
                               AppConstant.ERROR_TYPE_CODE_RESOURCE,
                               AppConstant.ERROR_TYPE_VALIDATION,
                               "vendor not found"
                       )));


        // Create First Transition
        FeedbackWorkflowTransition entry =
                new FeedbackWorkflowTransition();

        entry.setFeedbackId(feedbackId);

        entry.setWorkflowId(workflow.getWorkflowId());

        entry.setTransitionId(
                transition.getTransitionId());

        entry.setProductType(productType);

        entry.setPoiCode(poiCode);

        entry.setVendorCode(vendorCode);

        entry.setPlantId(plantId);

        entry.setCurrentRoleId(
                transition.getCurrentRoleId());

        entry.setCurrentRoleName(
                roleNameById(
                        transition.getCurrentRoleId()));

        entry.setNextRoleId(
                transition.getNextRoleId());

        entry.setNextRoleName(
                roleNameById(
                        transition.getNextRoleId()));

        entry.setAction(
                transition.getTransitionName());

        entry.setCurrentStatus("Created");

        entry.setAssignedToUser(um.getUserId());
        entry.setNextStatus("PENDING_RECTIFICATION");

        entry.setProcessIeUserId(createdBy);

        entry.setCreatedBy(createdBy);

        entry.setCreatedDate(new Date());

        feedbackWorkflowTransitionRepository.save(entry);

        return mapFeedbackWorkflowTransition(entry);
    }

    private FeedbackWorkflowTransitionDto mapFeedbackWorkflowTransition(
            FeedbackWorkflowTransition entity) {

        FeedbackWorkflowTransitionDto dto =
                new FeedbackWorkflowTransitionDto();

        dto.setFeedbackWorkflowTransitionId(
                entity.getFeedbackWorkflowTransitionId());

        dto.setFeedbackId(
                entity.getFeedbackId());

        dto.setWorkflowId(
                entity.getWorkflowId());

        dto.setTransitionId(
                entity.getTransitionId());

        dto.setProductType(
                entity.getProductType());

        dto.setPoiCode(
                entity.getPoiCode());

        dto.setVendorCode(
                entity.getVendorCode());

        dto.setPlantId(
                entity.getPlantId());

        dto.setCurrentRoleId(
                entity.getCurrentRoleId());

        dto.setCurrentRoleName(
                entity.getCurrentRoleName());

        dto.setNextRoleId(
                entity.getNextRoleId());

        dto.setNextRoleName(
                entity.getNextRoleName());

        dto.setAction(
                entity.getAction());

        dto.setCurrentStatus(
                entity.getCurrentStatus());

        dto.setNextStatus(
                entity.getNextStatus());

        dto.setRemarks(
                entity.getRemarks());

        dto.setAssignedToUser(
                entity.getAssignedToUser());

        dto.setProcessIeUserId(
                entity.getProcessIeUserId());

        dto.setCreatedBy(
                entity.getCreatedBy());

        dto.setModifiedBy(
                entity.getModifiedBy());

        dto.setCreatedDate(
                entity.getCreatedDate());

        dto.setModifiedDate(
                entity.getModifiedDate());

        return dto;
    }

    @Override
    @Transactional
    public FeedbackWorkflowTransitionDto feedbackPerformTransition(
            FeedbackTransitionActionReqDto req) {

        FeedbackWorkflowTransition current =
                feedbackWorkflowTransitionRepository
                        .findById(req.getWorkflowTransitionId())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Workflow transition not found")));

        List<TransitionMaster> transitions =
                transitionMasterRepository
                        .findByWorkflowIdAndCurrentRoleIdAndCurrentAction(
                                current.getWorkflowId(),
                                current.getNextRoleId(),
                                req.getAction());

        TransitionMaster transition = transitions.stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Transition not configured")));

        validateTransitionAccess(req.getActionBy(), transition);



        UserMaster um = userMasterRepository.findByUserName(current.getVendorCode())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "vendor not found"
                        )));


        FeedbackWorkflowTransition tx =
                new FeedbackWorkflowTransition();

        if(req.getAction().equalsIgnoreCase("RESEND_FOR_RECTIFICATION") ){
            tx.setAssignedToUser(um.getUserId());
        }
        tx.setFeedbackId(current.getFeedbackId());

        tx.setWorkflowId(current.getWorkflowId());

        tx.setTransitionId(transition.getTransitionId());

        tx.setProductType(current.getProductType());

        tx.setPoiCode(current.getPoiCode());

        tx.setVendorCode(current.getVendorCode());

        tx.setPlantId(current.getPlantId());

        tx.setProcessIeUserId(current.getProcessIeUserId());

        tx.setCurrentRoleId(
                transition.getCurrentRoleId());

        tx.setCurrentRoleName(
                roleNameById(
                        transition.getCurrentRoleId()));

        tx.setNextRoleId(
                transition.getNextRoleId());

        tx.setNextRoleName(
                transition.getNextRoleId() != null
                        ? roleNameById(
                        transition.getNextRoleId())
                        : null);

        tx.setAction(req.getAction());

        tx.setCurrentStatus(
                current.getNextStatus());

        tx.setNextStatus(
                transition.getNextAction());

        tx.setRemarks(req.getRemarks());

        tx.setCreatedBy(current.getCreatedBy());

        tx.setModifiedBy(req.getActionBy());

        tx.setCreatedDate(new Date());

        feedbackWorkflowTransitionRepository.save(tx);

        return mapFeedbackWorkflowTransition(tx);
    }

    private void validateTransitionAccess(
            Integer userId,
            TransitionMaster transition) {

        boolean hasRole =
                userRoleMasterRepository
                        .existsByUserIdAndRoleId(
                                userId,
                                transition.getCurrentRoleId());

        if (!hasRole) {
            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "You are not authorized to perform action : "
                                    + transition.getTransitionName()
                    )
            );
        }
    }

    @Override
    public List<FeedbackWorkflowTransitionDto> getPendingFeedbacks(
            Integer roleId,
            String productType) {

        List<FeedbackWorkflowTransition> transitions =
                feedbackWorkflowTransitionRepository
                        .findPendingFeedbacks(
                                roleId,
                                productType);

        return transitions.stream()
                .map(this::mapFeedbackWorkflowTransition)
                .toList();
    }






}
