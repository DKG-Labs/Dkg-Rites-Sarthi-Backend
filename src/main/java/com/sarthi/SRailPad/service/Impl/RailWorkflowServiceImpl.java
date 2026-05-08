package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.dto.RailTransitionActionReqDto;
import com.sarthi.SRailPad.dto.RailWorkflowTransactionDto;
import com.sarthi.SRailPad.entity.RailTransitionMaster;
import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping;
import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import com.sarthi.SRailPad.repository.*;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.entity.RoleMaster;
import com.sarthi.repository.RoleMasterRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.UserRoleMasterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RailWorkflowServiceImpl implements RailWorkflowService {


    private RailModuleRepository railModuleRepository;
    private RailTransitionMasterRepository railTransitionMasterRepository;
    private RailWorkflowTransactionRepository railWorkflowTransactionRepository;
    private RailWorkflowRepository railWorkflowRepository;

    private UserMasterRepository userMasterRepository;
    private UserRoleMasterRepository userRoleMasterRepository;
    private RoleMasterRepository roleMasterRepository;
    private RailPoiIeMappingRepository poiIeMappingRepository;
    private RailVendorPlantsRepository railVendorPlantsRepository;
    private RailPadPincodePoIMappingRepository railPadPincodePoIMappingRepository;


    @Override
    public RailWorkflowTransactionDto initiateWorkflow(
            String requestId,
            Long moduleId,
            Long workflowId,
            Long createdBy,
            String vendorCode,
            String plantId, String shift) {

        validateWorkflowAndModule(workflowId, moduleId);

        RailWorkflowTransaction tx = new RailWorkflowTransaction();

        RailTransitionMaster transition =
                railTransitionMasterRepository
                        .findFirstByWorkflowIdAndCurrentActionOrderByTransitionOrderAsc(
                                workflowId.intValue(),
                                "CREATE"
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Transition not configured"));

        RailPadPincodePoIMapping mapping =
                railPadPincodePoIMappingRepository
                        .findByVendorCode(vendorCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "POI mapping not found for vendor"));
        tx.setRequestId(requestId);
        tx.setWorkflowId(workflowId);
        tx.setModuleId(moduleId);

        tx.setVendorCode(vendorCode);
        tx.setPlantId(plantId);
        tx.setPoiCode(mapping.getPoiCode());

        tx.setAction(transition.getCurrentAction());

        tx.setStatus("CREATED");

        tx.setCurrentRole(getRoleName(transition.getCurrentRoleId()));
        tx.setNextRole(getRoleName(transition.getNextRoleId()));

        tx.setCreatedBy(createdBy);
        tx.setCreatedDate(LocalDateTime.now());

        tx.setJobStatus("CREATED");
        tx.setShift(shift);

        RailWorkflowTransaction saved = railWorkflowTransactionRepository.save(tx);

        return mapToResponse(saved);
    }



    @Override
    public RailWorkflowTransactionDto performTransitionAction(
            RailTransitionActionReqDto req) {

        RailWorkflowTransaction current =
                railWorkflowTransactionRepository
                        .findById(Math.toIntExact(req.getWorkflowTransitionId()))
                        .orElseThrow(() ->
                                new RuntimeException("Workflow not found"));

        // Validate logged-in user role
        validateNextRole(
                req.getActionBy(),
                current.getNextRole());

        Integer currentRoleId =
                getRoleId(current.getNextRole());

        RailTransitionMaster transition =
                railTransitionMasterRepository.findByWorkflowIdAndCurrentRoleIdAndNextAction(
                        Math.toIntExact(current.getWorkflowId()),
                currentRoleId,
                req.getAction()
        ).orElseThrow(() ->
                                new RuntimeException(
                                        "Transition not configured"));

        RailWorkflowTransaction tx =
                new RailWorkflowTransaction();

        tx.setWorkflowId(current.getWorkflowId());

        tx.setModuleId(current.getModuleId());

        tx.setRequestId(current.getRequestId());

        tx.setShift(req.getShift());

        // NEXT ACTION
        tx.setAction(transition.getNextAction());

        tx.setRemarks(req.getRemarks());

        tx.setVendorCode(current.getVendorCode());

        tx.setPlantId(current.getPlantId());

        tx.setPoiCode(current.getPoiCode());

        // CURRENT ROLE
        tx.setCurrentRole(
                getRoleName(
                        transition.getCurrentRoleId()));

        // NEXT ROLE
        if (transition.getNextRoleId() != null) {

            tx.setNextRole(
                    getRoleName(
                            transition.getNextRoleId()));
        }

        tx.setCreatedBy(current.getCreatedBy());

        tx.setModifiedBy(req.getActionBy());

        tx.setAssignedToUser(req.getActionBy());

        tx.setCreatedDate(LocalDateTime.now());

        tx.setUpdatedDate(LocalDateTime.now());


        // STATUS LOGIC

        if ("VERIFY".equalsIgnoreCase(
                transition.getNextAction())) {

            tx.setStatus("COMPLETED");

            tx.setJobStatus("COMPLETED");

        }
        else if ("RETURN_TO_VENDOR".equalsIgnoreCase(
                transition.getNextAction())) {

            tx.setStatus("RETURNED");

            tx.setJobStatus("RETURNED");

        }
        else {

            tx.setStatus("PENDING");

            tx.setJobStatus("PENDING");
        }


        RailWorkflowTransaction saved =
                railWorkflowTransactionRepository.save(tx);

        return mapToResponse(saved);
    }


    private RailWorkflowTransactionDto mapToResponse(
            RailWorkflowTransaction tx) {

        RailWorkflowTransactionDto dto =
                new RailWorkflowTransactionDto();

        dto.setWorkflowTransitionId(
                Long.valueOf(tx.getWorkflowTransitionId()));

        dto.setWorkflowId(tx.getWorkflowId());

        dto.setModuleId(tx.getModuleId());

        dto.setRequestId(tx.getRequestId());

        dto.setAction(tx.getAction());

        dto.setStatus(tx.getStatus());

        dto.setRemarks(tx.getRemarks());

        dto.setCurrentRole(tx.getCurrentRole());

        dto.setNextRole(tx.getNextRole());

        dto.setShift(tx.getShift());

        dto.setVendorCode(tx.getVendorCode());

        dto.setPlantId(tx.getPlantId());

        dto.setPoiCode(tx.getPoiCode());

        dto.setAssignedToUser(tx.getAssignedToUser());

        dto.setCreatedBy(tx.getCreatedBy());

        dto.setModifiedBy(tx.getModifiedBy());

        dto.setCreatedDate(tx.getCreatedDate());

        dto.setUpdatedDate(tx.getUpdatedDate());

        dto.setRio(tx.getRio());

        dto.setJobStatus(tx.getJobStatus());
        List<RailPoiIeMapping> mappings = null;

        String vendorId = null;

        List<Integer> userIds = new ArrayList<>();


        // If next role is Vendor
        if ("Rail Vendor".equalsIgnoreCase(tx.getNextRole())) {

            vendorId =
                    railPadPincodePoIMappingRepository
                            .findVendorCodeByPoiCode(tx.getPoiCode())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Vendor not found for POI"));

        } else {

            // Process IE / Main IE mappings

            mappings =
                    poiIeMappingRepository
                            .findByPoiCodeAndPlantId(
                                    tx.getPoiCode(),
                                    tx.getPlantId()
                            );
        }


        // Accessible users
        if (mappings != null) {

            userIds = mappings.stream()
                    .map(RailPoiIeMapping::getIeUserId)
                    .toList();
        }


        // Assign vendor user
        if (vendorId != null) {
            Long vendorUserId =
                    railVendorPlantsRepository
                            .findVendorUserIdByVendorCode(vendorId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Vendor user not found"));

            dto.setAssignedToUser(vendorUserId);

           // dto.setAssignedToUser(Long.valueOf(vendorId));
        }

        dto.setAccessibleUserIds(userIds);

        return dto;
    }

    private String getRoleName(Integer roleId) {

        return roleMasterRepository.findById(roleId)
                .map(RoleMaster::getRoleName)
                .orElse(null);
    }

    private Integer getRoleId(String roleName) {

        return roleMasterRepository.findByRoleName(roleName)
                .map(RoleMaster::getRoleId)
                .orElse(null);
    }

    private void validateWorkflowAndModule(
            Long workflowId,
            Long moduleId) {

        boolean workflowExists =
                railWorkflowRepository.existsById(workflowId);

        if (!workflowExists) {

            throw new RuntimeException(
                    "Workflow not found : " + workflowId);
        }

        boolean moduleValid =
                railModuleRepository.existsByIdAndWorkflowId(
                        moduleId,
                        workflowId
                );

        if (!moduleValid) {

            throw new RuntimeException(
                    "Module does not belong to workflow");
        }
    }

    private void validateNextRole(
            Long actionBy,
            String expectedRole) {

        List<String> userRoles =
                userMasterRepository
                        .findRoleNamesByUserId(
                                Math.toIntExact(actionBy));

        if (userRoles == null || userRoles.isEmpty()) {

            throw new RuntimeException(
                    "No roles mapped to user");
        }

        boolean allowed =
                userRoles.stream()
                        .anyMatch(role ->
                                role.equalsIgnoreCase(expectedRole));

        if (!allowed) {

            throw new RuntimeException(
                    "User is not allowed to perform this action. Expected role: "
                            + expectedRole);
        }
    }





}
