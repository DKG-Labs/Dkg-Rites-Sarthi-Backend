package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.dto.RailTransitionActionReqDto;
import com.sarthi.SRailPad.dto.RailWorkflowTransactionDto;
import com.sarthi.SRailPad.entity.RailTransitionMaster;
import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping;
import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants;
import com.sarthi.SRailPad.repository.*;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.Sleeper.entity.SleeperTransitionMaster;
import com.sarthi.constant.AppConstant;
import com.sarthi.entity.IEFieldsMapping;
import com.sarthi.entity.RoleMaster;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.*;
import com.sarthi.entity.UserMaster;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCompleteDetails;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCompleteDetailsRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
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

    private IeFieldsMappingRepository ieFieldsMappingRepository;

    private RioUserRepository rioUserRepository;

    private RailInspectionCompleteDetailsRepository railInspectionCompleteDetailsRepository;
    private RailInspectionCallRepository railInspectionCallRepository;
    private com.sarthi.repository.VendorMasterRepository vendorMasterRepository;


    @Override
    @Transactional
    public RailWorkflowTransactionDto initiateWorkflow(
            String requestId,
            Long moduleId,
            Long workflowId,
            Long createdBy,
            String vendorCode,
            String plantId,
            String shift) {

        // VALIDATE WORKFLOW + MODULE
        if (!workflowId.equals(2L)) {

            validateWorkflowAndModule(
                    workflowId,
                    moduleId);
        }

        RailWorkflowTransaction tx =
                new RailWorkflowTransaction();


        // FETCH POI USING VENDOR CODE
        RailPadPincodePoIMapping mapping =
                railPadPincodePoIMappingRepository
                        .findByVendorCode(vendorCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "POI mapping not found for vendor"));


        String initialAction =
                workflowId.equals(2L)
                        ? "CREATED"
                        : "CREATE";


        RailTransitionMaster transition =
                railTransitionMasterRepository
                        .findFirstByWorkflowIdAndCurrentActionOrderByTransitionOrderAsc(
                                workflowId.intValue(),
                                initialAction
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Transition not configured"));



        tx.setRequestId(requestId);

        tx.setWorkflowId(workflowId);

        //tx.setModuleId(moduleId);
        if (!workflowId.equals(2L)) {

            tx.setModuleId(moduleId);

        } else {

            tx.setModuleId(null);
        }

        tx.setVendorCode(vendorCode);

        tx.setPlantId(plantId);

        tx.setPoiCode(mapping.getPoiCode());



        tx.setAction(
                transition.getCurrentAction());

        tx.setStatus(
                transition.getCurrentAction());

        tx.setJobStatus(
                transition.getCurrentAction());


        tx.setCurrentRole(
                getRoleName(
                        transition.getCurrentRoleId()));

        tx.setNextRole(
                getRoleName(
                        transition.getNextRoleId()));



        if (workflowId.equals(2L)
                && transition.getNextRoleId() != null
                && transition.getNextRoleId().equals(2)) {

            String pincode =
                    mapping.getPinCode();

            String product = "Rail Pad";

            String stage = "F";


            IEFieldsMapping ieMap =
                    ieFieldsMappingRepository
                            .findByPlantPincodeAndProductAndStageMatch(
                                    pincode,
                                    product,
                                    stage
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "RIO mapping not found"));


            tx.setRio(
                    ieMap.getRio());
        }



        tx.setCreatedBy(createdBy);

        tx.setCreatedDate(LocalDateTime.now());

        tx.setShift(shift);


        RailWorkflowTransaction saved =
                railWorkflowTransactionRepository.save(tx);

        return mapToResponse(saved);
    }


/*
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
    } */


    @Override
    @Transactional
    public RailWorkflowTransactionDto performTransitionAction(
            RailTransitionActionReqDto req) {

        RailWorkflowTransaction current =
                railWorkflowTransactionRepository
                        .findById(Math.toIntExact(req.getWorkflowTransitionId()))
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Workflow transition not found"
                                )
                        ));

        // Verify that the transaction is not already in a terminal state
        if ("COMPLETED".equalsIgnoreCase(current.getStatus()) || "COMPLETED".equalsIgnoreCase(current.getJobStatus())) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "This inspection has already been finished."
                    )
            );
        }


        // Also check if there's any newer transaction that has already advanced past this one
        List<RailWorkflowTransaction> allTx = railWorkflowTransactionRepository.findByRequestIdOrderByCreatedDateAsc(current.getRequestId());
        if (allTx != null && !allTx.isEmpty()) {
            RailWorkflowTransaction latestTx = allTx.get(allTx.size() - 1);
            if (latestTx.getWorkflowTransitionId() > current.getWorkflowTransitionId()) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "This transition has already been processed."
                        )
                );
            }
        }



        if(current.getWorkflowId() == 1
                && "Rail Process IE".equalsIgnoreCase(current.getNextRole())) {

            boolean exists =
                    poiIeMappingRepository
                            .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
                                    current.getPoiCode(),
                                    current.getPlantId(),
                                    Math.toIntExact(req.getActionBy()),
                                    "Process IE"
                            );

            if(!exists){
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "User not mapped as Process IE"
                        )
                );
            }
        }


        else if(current.getWorkflowId() == 2
                && "RIO Help Desk".equalsIgnoreCase(current.getNextRole())) {

            String employeeCode =
                    userMasterRepository
                            .findEmployeeCodeByUserId(
                                    Math.toIntExact(req.getActionBy()));

            if(employeeCode == null){
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Employee code not found for user"
                        )
                );
            }

            boolean exists =
                    rioUserRepository.existsByRioAndEmployeeCode(
                            current.getRio(),
                            employeeCode);

            if(!exists){
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "User is not mapped to this RIO"
                        )
                );
            }
        }


        else if(current.getWorkflowId() == 2
                && "Rail Main IE".equalsIgnoreCase(current.getNextRole())) {

            boolean exists =
                    poiIeMappingRepository
                            .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
                                    current.getPoiCode(),
                                    current.getPlantId(),
                                    Math.toIntExact(req.getActionBy()),
                                    "Main IE"
                            );

            if(!exists){
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "User is not mapped as Main IE"
                        )
                );
            }
        }

        String status = null;



//        if(current.getWorkflowId() == 1){
//            status = determineJobStatus(req.getAction());
//        }

        RailWorkflowTransaction tx =
                new RailWorkflowTransaction();

        tx.setRequestId(req.getRequestId());
        tx.setModuleId(req.getModuleId());
        tx.setWorkflowId(current.getWorkflowId());

        tx.setAction(req.getAction());
        tx.setStatus(status);
        tx.setRemarks(req.getRemarks());

        tx.setShift(current.getShift());
        tx.setPoiCode(current.getPoiCode());
        tx.setPlantId(current.getPlantId());
        tx.setVendorCode(current.getVendorCode());



        if(current.getWorkflowId().equals(2L)) {

            List<RailTransitionMaster> transitions =
                    railTransitionMasterRepository
                            .findByWorkflowIdAndCurrentRoleIdAndCurrentAction(
                                    current.getWorkflowId().intValue(),
                                    getRoleId(current.getNextRole()),
                                    req.getAction()
                            );

            RailTransitionMaster transition = null;

            if(transitions.size() == 1){

                transition = transitions.get(0);

            } else {

                List<RailTransitionMaster> trans = null;

                if(req.getAction().equalsIgnoreCase("PO_VERIFICATION")
                        || req.getAction().equalsIgnoreCase("MAIN_IE_SCHEDULE_CALL")
                        || req.getAction().equalsIgnoreCase("PAUSE")
                        || req.getAction().equalsIgnoreCase("FINISH")
                        || req.getAction().equalsIgnoreCase("RESUME")
                        || req.getAction().equalsIgnoreCase("WITHHELD")
                        || req.getAction().equalsIgnoreCase("RESCHEDULE_CALL")) {

                    trans =
                            railTransitionMasterRepository
                                    .findByWorkflowIdAndCurrentRoleIdAndCurrentAction(
                                            current.getWorkflowId().intValue(),
                                            getRoleId(current.getCurrentRole()),
                                            current.getAction()
                                    );

                    transition = trans.stream()
                            .filter(t ->
                                    t.getNextAction()
                                            .equalsIgnoreCase(req.getAction()))
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(
                                    new ErrorDetails(
                                            AppConstant.ERROR_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_VALIDATION,
                                            "Transition not configured for action: " + req.getAction()
                                    )));

                    tx.setCurrentRole(current.getNextRole());
                }
            }

            tx.setCurrentRole(current.getNextRole());
            if(current.getWorkflowId() == 2) {
                tx.setJobStatus(determineJobStatus(req.getAction()));
            }

            if(transition == null) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "No valid transition found for action: " + req.getAction()
                        ));
            }

            if(transition.getNextRoleId() != null) {

                tx.setNextRole(
                        getRoleName(
                                transition.getNextRoleId()));
            }

            if(transition.getNextRoleId() == null) {

                tx.setStatus(AppConstant.COMPLETED_TYPE);

            } else {

                tx.setStatus(AppConstant.PENDING_TYPE);
            }

            if(transition.getNextRoleId() != null
                    && transition.getNextRoleId().equals(2)) {

                RailPadPincodePoIMapping mapping =
                        railPadPincodePoIMappingRepository
                                .findByVendorCode(current.getVendorCode())
                                .orElseThrow(() -> new BusinessException(
                                        new ErrorDetails(
                                                AppConstant.ERROR_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_VALIDATION,
                                                "Vendor mapping not found"
                                        )
                                ));

                String stage = "F";
                String product = "Rail Pad";

                String pinCode = mapping.getPinCode();

                IEFieldsMapping map = ieFieldsMappingRepository
                        .findByPlantPincodeAndProductAndStageMatch(
                                pinCode,
                                product,
                                stage
                        )
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "No IE mapping found"
                                )
                        ));

                String rio = map.getRio();

                tx.setRio(rio);
            }

        }



        else {

            if(req.getAction().equalsIgnoreCase("RETURN_TO_VENDOR")) {

                tx.setCurrentRole("Rail Process IE");
                tx.setNextRole("Rail Vendor");

            }

            else if(req.getAction().equalsIgnoreCase("RESUBMIT")) {
                tx.setCurrentRole("Rail Vendor");
                tx.setNextRole("Rail Process IE");
            } else {
                tx.setCurrentRole("Rail Process IE");
            }

            if ("VERIFY".equalsIgnoreCase(req.getAction())) {
                tx.setStatus("COMPLETED");
                tx.setJobStatus("COMPLETED");
            } else if ("RETURN_TO_VENDOR".equalsIgnoreCase(req.getAction())) {
                tx.setStatus("RETURNED");
                tx.setJobStatus("RETURNED");
            } else if ("RESUBMIT".equalsIgnoreCase(req.getAction())) {
                System.out.println("[Workflow Service] Action is RESUBMIT, setting status to RESUBMITTED");
                tx.setStatus("RESUBMITTED");
                tx.setJobStatus("RESUBMITTED");
            } else {
                System.out.println("[Workflow Service] Action is " + req.getAction() + ", setting status to PENDING");
                tx.setStatus("PENDING");
                tx.setJobStatus("PENDING");
            }
            System.out.println("[Workflow Service] Final Status before save: " + tx.getStatus());
        }

        tx.setAssignedToUser(req.getActionBy());

        tx.setCreatedBy(current.getCreatedBy());
        tx.setModifiedBy(req.getActionBy());

        tx.setCreatedDate(LocalDateTime.now());
        tx.setUpdatedDate(LocalDateTime.now());

        RailWorkflowTransaction saved =
                railWorkflowTransactionRepository.save(tx);

        // --- Save to inspection_complete_details when Railpad inspection is FINISHED ---
        if (current.getWorkflowId().equals(2L) && "COMPLETED".equalsIgnoreCase(tx.getStatus())) {
            Optional<RailInspectionCall> callOpt = railInspectionCallRepository.findByCallNo(tx.getRequestId());
            if (callOpt.isPresent()) {
                RailInspectionCall ic = callOpt.get();
                UserMaster user = userMasterRepository.findById(Math.toIntExact(req.getActionBy())).orElse(null);
                
                String rio = tx.getRio() != null ? tx.getRio() : current.getRio();
                String userShortName = user != null && user.getShortName() != null ? user.getShortName() : "XX";
                
                RailInspectionCompleteDetails details = new RailInspectionCompleteDetails();
                details.setCallNo(ic.getCallNo());
                details.setPoNo(ic.getPoNo());
                details.setCertificateNo(generateCertificateNo(rio, ic.getCallNo(), userShortName));
                details.setCreatedOn(LocalDateTime.now());
                
                railInspectionCompleteDetailsRepository.save(details);
            }
        }

        return mapToResponse(saved);
    }

    private String generateCertificateNo(String rioName, String callNo, String userShortName) {
        String rioFirstLetter = (rioName != null && !rioName.isEmpty())
                ? rioName.substring(0, 1).toUpperCase()
                : "X";
        return rioFirstLetter + "/" + callNo + "/" + userShortName.toUpperCase();
    }

    private String determineJobStatus(String action) {

        switch (action.toUpperCase()) {

            case "CREATED":
                return "CREATED";

            case "VERIFY":
                return "RIO_VERIFIED";

            case "MAIN_IE_SCHEDULE_CALL":
                return "SCHEDULED";

            case "RESCHEDULE_CALL":
                return "RESCHEDULE";

            case "INITIATE_CALL":
                return "INITIATED";

            case "PO_VERIFICATION":
                return "PO_VERIFICATION";

            case "FINISH":
            case "COMPLETED":
                return "COMPLETED";

            case "RESUBMIT":
                return "RESUBMITTED";

            case "PAUSE":
                return "PAUSED";

            case "WITHHELD":
                return "WITHHELD";

            case "RESUME":
                return "RESUME";

            case "IC_ISSUE":
                return "IC_ISSUE";

            case "IC_GENERATION":
                return "GENERATED";

            default:
                return "PENDING";
        }
    }


    private RailWorkflowTransactionDto mapToResponse(RailWorkflowTransaction tx) {
        return mapToResponse(tx, new java.util.HashMap<>());
    }

    @SuppressWarnings("unchecked")
    private RailWorkflowTransactionDto mapToResponse(RailWorkflowTransaction tx, java.util.Map<String, Object> cache) {
        if (cache == null) {
            cache = new java.util.HashMap<>();
        }

        RailWorkflowTransactionDto dto = new RailWorkflowTransactionDto();

        dto.setWorkflowTransitionId(Long.valueOf(tx.getWorkflowTransitionId()));
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
        if (tx.getVendorCode() != null && !tx.getVendorCode().isEmpty()) {
            String vendorNameCacheKey = "vendorName_" + tx.getVendorCode();
            String vName = (String) cache.computeIfAbsent(vendorNameCacheKey, k ->
                vendorMasterRepository.findByVendorCode(tx.getVendorCode())
                    .map(com.sarthi.entity.VendorMaster::getVendorName)
                    .orElse(null)
            );
            dto.setVendorName(vName);
        }
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
            if (tx.getPoiCode() != null) {
                String vendorIdCacheKey = "vendorId_" + tx.getPoiCode();
                vendorId = (String) cache.computeIfAbsent(vendorIdCacheKey, k ->
                        railPadPincodePoIMappingRepository
                                .findVendorCodeByPoiCode(tx.getPoiCode())
                                .orElse(null)
                );

                if (vendorId == null) {
                    System.err.println("[WARN] Vendor not found for POI: " + tx.getPoiCode());
                }
            }
        } else {
            // Process IE / Main IE mappings
            if (tx.getPoiCode() != null && tx.getPlantId() != null) {
                String ieType = tx.getWorkflowId().equals(2L) ? "Main IE" : "Process IE";
                String mappingCacheKey = "mapping_" + tx.getPoiCode() + "_" + tx.getPlantId() + "_" + ieType;
                mappings = (List<RailPoiIeMapping>) cache.computeIfAbsent(mappingCacheKey, k ->
                        poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(
                                tx.getPoiCode(),
                                tx.getPlantId(),
                                ieType
                        )
                );
            }
        }

        // Accessible users
        if (mappings != null) {
            userIds = mappings.stream()
                    .map(RailPoiIeMapping::getIeUserId)
                    .toList();
        }

        // Assign vendor user
        if (vendorId != null) {
            final String finalVendorId = vendorId;
            String vendorUserCacheKey = "vendorUser_" + vendorId;
            Long vendorUserId = (Long) cache.computeIfAbsent(vendorUserCacheKey, k ->
                    railVendorPlantsRepository
                            .findVendorUserIdByVendorCode(finalVendorId)
                            .orElse(null)
            );
            if (vendorUserId == null) {
                System.err.println("[WARN] Vendor user not found for vendor code: " + finalVendorId);
            } else {
                dto.setAssignedToUser(vendorUserId);
            }
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

        if (actionBy == null) {
            throw new RuntimeException("Action by user ID cannot be null");
        }

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
                                role != null && role.equalsIgnoreCase(expectedRole));

        if (!allowed) {

            throw new RuntimeException(
                    "User is not allowed to perform this action. Expected role: "
                            + expectedRole);
        }
    }



    @Override
    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName) {

        List<RailWorkflowTransaction> list = null;

        if(roleName.equalsIgnoreCase("Rail Main IE")) {

            list = railWorkflowTransactionRepository
                    .findLatestByRole(roleName);

        } else {

            list = railWorkflowTransactionRepository
                    .findLastPendingRequestsByRole(roleName);
        }

        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<RailWorkflowTransactionDto>
    workflowTransitionHistory(String requestId) {

        List<RailWorkflowTransaction> list =
                railWorkflowTransactionRepository
                        .findByRequestIdOrderByCreatedDateAsc(requestId);

        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
                .toList();
    }


    @Override
    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions() {

        List<RailWorkflowTransaction> list =
                railWorkflowTransactionRepository
                        .findCompletedRequests();

        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
                .toList();
    }


    @Override
    public List<RailWorkflowTransactionDto> allFinalCompletedWorkflowTransitions() {

        List<RailWorkflowTransaction> list =
                railWorkflowTransactionRepository
                        .findFinalCompletedRequests();

        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
                .toList();
    }

    @Override
    public List<String> getMappedCompanyNames(Long userId) {
        List<RailPoiIeMapping> ieMappings = poiIeMappingRepository.findByIeUserId(Math.toIntExact(userId));
        List<String> companyNames = new ArrayList<>();

        for (RailPoiIeMapping ieMapping : ieMappings) {
            List<RailPadPincodePoIMapping> poiMappings = railPadPincodePoIMappingRepository.findByPoiCode(ieMapping.getPoiCode());
            for (RailPadPincodePoIMapping poiMapping : poiMappings) {
                if (!companyNames.contains(poiMapping.getCompanyName())) {
                    companyNames.add(poiMapping.getCompanyName());
                }
            }
        }
        return companyNames;
    }

    @Override
    public List<String> getPlantsByCompanyName(String companyName) {
        List<RailPadPincodePoIMapping> poiMappings = railPadPincodePoIMappingRepository.findByCompanyName(companyName);
        List<String> plantIds = new ArrayList<>();

        for (RailPadPincodePoIMapping poiMapping : poiMappings) {
            List<RailVendorPlants> plants = railVendorPlantsRepository.findByVendorCode(poiMapping.getVendorCode());
            for (RailVendorPlants plant : plants) {
                if (!plantIds.contains(plant.getPlantId())) {
                    plantIds.add(plant.getPlantId());
                }
            }
        }
        return plantIds;
    }
}