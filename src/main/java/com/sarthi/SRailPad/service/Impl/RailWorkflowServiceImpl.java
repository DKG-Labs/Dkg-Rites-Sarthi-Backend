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
import com.sarthi.SRailPad.dto.RailpadRemapSubmitDto;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCompleteDetails;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCompleteDetailsRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.util.NotificationService;
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
    private PoHeaderRepository poHeaderRepository;

    private NotificationService notificationService;

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


        String rawVendorCode = vendorCode != null ? vendorCode.trim() : "";
        String cleanVendorCode = rawVendorCode.startsWith(":") ? rawVendorCode.substring(1) : rawVendorCode;
        String colonVendorCode = rawVendorCode.startsWith(":") ? rawVendorCode : ":" + rawVendorCode;

        String rawPlantId = plantId != null ? plantId.trim() : "";
        String cleanPlantId = rawPlantId.startsWith(":") ? rawPlantId.substring(1) : rawPlantId;
        String colonPlantId = rawPlantId.startsWith(":") ? rawPlantId : ":" + rawPlantId;

        com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants plant =
                railVendorPlantsRepository.findByPlantId(rawPlantId)
                .orElseGet(() -> railVendorPlantsRepository.findByPlantId(cleanPlantId)
                .orElseGet(() -> railVendorPlantsRepository.findByPlantId(colonPlantId).orElse(null)));

        String companyName = (plant != null && plant.getCompanyName() != null) ? plant.getCompanyName() : "";

        // FETCH POI USING VENDOR CODE AND COMPANY NAME WITH MULTI-LEVEL FALLBACK
        RailPadPincodePoIMapping mapping = null;

        if (companyName != null && !companyName.isEmpty()) {
            mapping = railPadPincodePoIMappingRepository
                    .findByVendorCodeAndCompanyName(rawVendorCode, companyName)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCodeAndCompanyName(colonVendorCode, companyName)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCodeAndCompanyName(cleanVendorCode, companyName)
                    .orElse(null)));
        }

        if (mapping == null) {
            mapping = railPadPincodePoIMappingRepository
                    .findByVendorCode(rawVendorCode)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCode(colonVendorCode)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCode(cleanVendorCode)
                    .orElse(null)));
        }

        if (mapping == null && plant != null && plant.getCompanyName() != null && !plant.getCompanyName().isEmpty()) {
            List<RailPadPincodePoIMapping> listByComp = railPadPincodePoIMappingRepository.findByCompanyName(plant.getCompanyName());
            if (listByComp != null && !listByComp.isEmpty()) {
                mapping = listByComp.get(0);
            }
        }

        if (mapping == null) {
            List<RailPadPincodePoIMapping> allMappings = railPadPincodePoIMappingRepository.findAll();
            mapping = allMappings.stream()
                    .filter(m -> m.getVendorCode() != null && (m.getVendorCode().contains(cleanVendorCode) || cleanVendorCode.contains(m.getVendorCode().replace(":", ""))))
                    .findFirst()
                    .orElseGet(() -> allMappings.isEmpty() ? null : allMappings.get(0));
        }

        if (mapping == null) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No Main ie has been mapped for this plant id"
                    )
            );
        }

        String poiCode = mapping.getPoiCode();

        // 1. Call Raising fails if no Main IE is mapped to the vendor plant/POI
        if (workflowId != null && workflowId.equals(2L)) {
            boolean hasMainIe = poiIeMappingRepository.hasMainIeMapping(rawPlantId, cleanPlantId, colonPlantId, poiCode);
            if (!hasMainIe) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "No Main ie has been mapped for this plant id"
                        )
                );
            }
        }

        // 2. Production Declaration fails if no Process IE is mapped to the company/plant
        if (workflowId != null && workflowId.equals(1L) && Long.valueOf(3L).equals(moduleId)) {
            boolean hasProcessIe = poiIeMappingRepository.hasProcessIeMapping(rawPlantId, cleanPlantId, colonPlantId, poiCode);
            if (!hasProcessIe) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "No Process IE has been mapped for this plant ID"
                        )
                );
            }
        }


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

        if (workflowId.equals(1L)) {
            if (moduleId != null && moduleId == 3L) {
                tx.setNextRole("Rail Process IE");
            } else {
                tx.setNextRole("Rail Main IE");
            }
        }



        if (workflowId.equals(2L)
                && transition.getNextRoleId() != null
                && transition.getNextRoleId().equals(2)) {

            String pincode = mapping != null ? mapping.getPinCode() : null;
            String product = "Rail Pad";
            String stage = "F";

            IEFieldsMapping ieMap = null;

            if (pincode != null && !pincode.isEmpty()) {
                // Tier 1: pincode + "Rail Pad"
                ieMap = ieFieldsMappingRepository.findByPinCodeProductAndStageMatch(pincode, product, stage).orElse(null);
                // Tier 2: pincode + "ERC"
                if (ieMap == null) {
                    ieMap = ieFieldsMappingRepository.findByPinCodeProductAndStageMatch(pincode, "ERC", stage).orElse(null);
                }
                // Tier 3: plantPincode + "Rail Pad"
                if (ieMap == null) {
                    ieMap = ieFieldsMappingRepository.findByPlantPincodeAndProductAndStageMatch(pincode, product, stage).orElse(null);
                }
                // Tier 4: plantPincode + "ERC"
                if (ieMap == null) {
                    ieMap = ieFieldsMappingRepository.findByPlantPincodeAndProductAndStageMatch(pincode, "ERC", stage).orElse(null);
                }
                // Tier 5: Prefix matching (e.g. Haryana pincode prefix "12")
                if (ieMap == null && pincode.length() >= 2) {
                    String prefix2 = pincode.substring(0, 2);
                    List<IEFieldsMapping> allList = ieFieldsMappingRepository.findAll();
                    ieMap = allList.stream()
                            .filter(m -> m.getPinCode() != null && m.getPinCode().startsWith(prefix2))
                            .findFirst()
                            .orElse(null);
                }
            }

            // Fallback default RIO if no exact pincode record exists
            String rio = "NRIO";
            if (ieMap != null && ieMap.getRio() != null && !ieMap.getRio().isEmpty()) {
                rio = ieMap.getRio();
            }

            tx.setRio(rio);
            String productType = "Rail Pad";
            notificationService.sendInspectionCallAssignedToRio(productType, requestId, rio);

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

        // First check recent transactions for requestId to get the most recent transaction & ID
        RailWorkflowTransaction current = null;
        if (req.getRequestId() != null && !req.getRequestId().isEmpty()) {
            List<RailWorkflowTransaction> allTx = railWorkflowTransactionRepository.findByRequestIdOrderByCreatedDateAsc(req.getRequestId());
            if (allTx != null && !allTx.isEmpty()) {
                current = allTx.get(allTx.size() - 1);
            }
        }
        if (current == null) {
            current = railWorkflowTransactionRepository
                    .findById(Math.toIntExact(req.getWorkflowTransitionId()))
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Workflow transition not found"
                            )
                    ));
        }

        // Verify that the transaction is not already in a terminal state
        if (("COMPLETED".equalsIgnoreCase(current.getStatus()) || "COMPLETED".equalsIgnoreCase(current.getJobStatus())) 
            && !req.getAction().equalsIgnoreCase("IC_ISSUE") && !req.getAction().equalsIgnoreCase("IC_GENERATION")) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "This inspection has already been finished."
                    )
            );
        }



        if(current.getWorkflowId() == 1) {
            Long modId = req.getModuleId() != null ? req.getModuleId() : current.getModuleId();
            String requiredIeType = (modId != null && modId == 3) ? "Process IE" : "Main IE";

            boolean exists = poiIeMappingRepository
                    .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
                            current.getPoiCode(),
                            current.getPlantId(),
                            Math.toIntExact(req.getActionBy()),
                            requiredIeType
                    );

            if (!exists && current.getPlantId() != null) {
                List<RailPoiIeMapping> userMappings = poiIeMappingRepository
                        .findByIeUserId(Math.toIntExact(req.getActionBy()));
                exists = userMappings.stream().anyMatch(m ->
                        m.getIeType() != null &&
                        (m.getIeType().replace(" ", "_").equalsIgnoreCase(requiredIeType.replace(" ", "_")) ||
                         m.getIeType().toLowerCase().contains(requiredIeType.toLowerCase()))
                );
            }

            if(!exists){
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "User not mapped as " + requiredIeType
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


        else if (current.getWorkflowId() == 2
                && (current.getNextRole() == null || current.getNextRole().toLowerCase().contains("main"))) {

            boolean exists = poiIeMappingRepository
                    .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
                            current.getPoiCode(),
                            current.getPlantId(),
                            Math.toIntExact(req.getActionBy()),
                            "Main IE"
                    );

            if (!exists) {
                // Fallback check: handle "MAIN_IE", "Main IE", or "MAIN" in rail_poi_ie_mapping
                List<RailPoiIeMapping> userMappings = poiIeMappingRepository
                        .findByIeUserId(Math.toIntExact(req.getActionBy()));
                exists = userMappings.stream().anyMatch(m ->
                        m.getIeType() != null &&
                        (m.getIeType().replace(" ", "_").equalsIgnoreCase("MAIN_IE") ||
                         m.getIeType().toLowerCase().contains("main"))
                );
            }

            if (!exists) {
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

            } else if (req.getAction().equalsIgnoreCase("IC_ISSUE") || req.getAction().equalsIgnoreCase("IC_GENERATION")) {
                
                transition = new RailTransitionMaster();
                transition.setNextRoleId(null); // Keep it in a terminal state
                tx.setCurrentRole(current.getNextRole() != null ? current.getNextRole() : current.getCurrentRole());
                
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
            Long modId = req.getModuleId() != null ? req.getModuleId() : current.getModuleId();
            String ieRole = (modId != null && modId == 3) ? "Rail Process IE" : "Rail Main IE";

            if(req.getAction().equalsIgnoreCase("RETURN_TO_VENDOR")) {

                tx.setCurrentRole(ieRole);
                tx.setNextRole("Rail Vendor");

            }

            else if(req.getAction().equalsIgnoreCase("RESUBMIT")) {
                tx.setCurrentRole("Rail Vendor");
                tx.setNextRole(ieRole);
            } else {
                tx.setCurrentRole(ieRole);
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

        if (current.getWorkflowId() == 2
                && "RIO Help Desk".equalsIgnoreCase(current.getNextRole())
                && "VERIFY".equalsIgnoreCase(req.getAction())) {

            notificationService.sendRailPadCallRegisteredNotification(
                    req.getRequestId(),
                    current.getPlantId(),
                    "CALL_REGISTERED"
            );
        }


        // --- Save to inspection_complete_details when Railpad inspection is FINISHED ---
        if (current.getWorkflowId().equals(2L) && "COMPLETED".equalsIgnoreCase(tx.getStatus()) && req.getAction().equalsIgnoreCase("FINISH")) {
            Optional<RailInspectionCall> callOpt = railInspectionCallRepository.findByCallNo(tx.getRequestId());
            if (callOpt.isPresent()) {
                Optional<RailInspectionCompleteDetails> existingOpt = railInspectionCompleteDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(tx.getRequestId());
                if (existingOpt.isEmpty()) {
                    RailInspectionCall ic = callOpt.get();
                UserMaster user = userMasterRepository.findById(Math.toIntExact(req.getActionBy())).orElse(null);
                
                String userShortName = user != null && user.getShortName() != null ? user.getShortName() : "XX";
                
                String rlyShortName = "X";
                if (ic.getPoNo() != null) {
                    String basePoNo = ic.getPoNo();
                    if (basePoNo.contains("/")) {
                        basePoNo = basePoNo.substring(0, basePoNo.indexOf("/"));
                    }
                    com.sarthi.entity.PoHeader poHeader = poHeaderRepository.findByPoNo(basePoNo).orElse(null);
                    if (poHeader != null && poHeader.getRlyShortName() != null) {
                        rlyShortName = poHeader.getRlyShortName();
                    }
                }
                
                RailInspectionCompleteDetails details = new RailInspectionCompleteDetails();
                details.setCallNo(ic.getCallNo());
                details.setPoNo(ic.getPoNo());
                details.setCertificateNo(generateCertificateNo(rlyShortName, ic.getCallNo(), userShortName));
                details.setCreatedOn(LocalDateTime.now());
                
                railInspectionCompleteDetailsRepository.save(details);
                }
            }
        }

        return mapToResponse(saved);
    }

    private String generateCertificateNo(String rlyShortName, String callNo, String userShortName) {
        String rlyPrefix = (rlyShortName != null && !rlyShortName.isEmpty())
                ? rlyShortName.toUpperCase()
                : "X";
        // Convert user short name to Title Case or just use it as-is if it's already correctly cased
        // We'll capitalize the first letter to ensure it matches "Suryaprakash" format
        if (userShortName != null && userShortName.length() > 0) {
            userShortName = userShortName.substring(0, 1).toUpperCase() + userShortName.substring(1).toLowerCase();
        }
        return rlyPrefix + "/" + callNo + "/" + userShortName;
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
        
        String vendorNameCacheKey = "vendorName_" + (tx.getVendorCode() != null ? tx.getVendorCode() : "") + "_" + (tx.getPlantId() != null ? tx.getPlantId() : "") + "_" + (tx.getRequestId() != null ? tx.getRequestId() : "");
        String vName = (String) cache.computeIfAbsent(vendorNameCacheKey, k -> {
            String name = null;

            // 1. Try VendorMaster by vendorCode
            if (tx.getVendorCode() != null && !tx.getVendorCode().trim().isEmpty()) {
                name = vendorMasterRepository.findByVendorCode(tx.getVendorCode().trim())
                    .map(com.sarthi.entity.VendorMaster::getVendorName)
                    .orElse(null);
            }

            // 2. Try RailVendorPlants by vendorCode
            if ((name == null || name.trim().isEmpty()) && tx.getVendorCode() != null && !tx.getVendorCode().trim().isEmpty()) {
                name = railVendorPlantsRepository.findByVendorCode(tx.getVendorCode().trim())
                    .stream()
                    .map(com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants::getCompanyName)
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .findFirst()
                    .orElse(null);
            }

            // 3. Try RailVendorPlants by plantId
            if ((name == null || name.trim().isEmpty()) && tx.getPlantId() != null && !tx.getPlantId().trim().isEmpty()) {
                String pId = tx.getPlantId().trim();
                name = railVendorPlantsRepository.findByPlantId(pId)
                    .map(com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants::getCompanyName)
                    .orElse(null);
                if (name == null || name.trim().isEmpty()) {
                    String altPlantId = pId.startsWith(":") ? pId.substring(1) : ":" + pId;
                    name = railVendorPlantsRepository.findByPlantId(altPlantId)
                        .map(com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants::getCompanyName)
                        .orElse(null);
                }
            }

            // 4. Try RailInspectionCall by requestId / callNo
            if ((name == null || name.trim().isEmpty()) && tx.getRequestId() != null) {
                try {
                    name = railInspectionCallRepository.findByCallNo(tx.getRequestId())
                        .map(com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall::getVendorName)
                        .orElse(null);
                } catch (Exception ignored) {}
            }

            return name;
        });

        dto.setVendorName(vName != null && !vName.trim().isEmpty() ? vName : tx.getVendorCode());
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
                String ieType = tx.getWorkflowId().equals(2L) ? "MAIN_IE" : "PROCESS_IE";
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

        if (dto.getAssignedToUser() == null && userIds != null && !userIds.isEmpty()) {
            dto.setAssignedToUser(userIds.get(0).longValue());
        }

        if (dto.getAssignedToUser() != null) {
            String userCacheKey = "user_" + dto.getAssignedToUser();
            UserMaster user = (UserMaster) cache.computeIfAbsent(userCacheKey, k ->
                    userMasterRepository.findById(Math.toIntExact(dto.getAssignedToUser())).orElse(null)
            );
            if (user != null) {
                dto.setAssignedToUserName(user.getFullName());
                dto.setAssignedToUserEmployeeCode(user.getEmployeeCode());
            }
        }

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
    public List<String> getMappedPlantIdsForUser(Integer userId, String ieType) {
        List<RailPoiIeMapping> mappings = poiIeMappingRepository.findByIeUserId(userId);
        return mappings.stream()
                .filter(m -> m.getIeType() != null && m.getIeType().replace(" ", "_").equalsIgnoreCase(ieType.replace(" ", "_")))
                .map(RailPoiIeMapping::getPlantId)
                .distinct()
                .toList();
    }


    @Override
    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName) {
        return allPendingWorkflowTransitions(roleName, null, null);
    }

    @Override
    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName, String plantId) {
        return allPendingWorkflowTransitions(roleName, plantId, null);
    }

    @Override
    public List<RailWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName, String plantId, Long workflowId) {

        List<RailWorkflowTransaction> list = null;

        if (workflowId != null) {
            if (plantId != null && !plantId.trim().isEmpty()) {
                if (roleName.equalsIgnoreCase("Rail Main IE")) {
                    list = railWorkflowTransactionRepository
                            .findLatestByRoleAndPlantIdAndWorkflowId(roleName, plantId.trim(), workflowId);
                } else {
                    list = railWorkflowTransactionRepository
                            .findLastPendingRequestsByRoleAndPlantIdAndWorkflowId(roleName, plantId.trim(), workflowId);
                }
            } else {
                if (roleName.equalsIgnoreCase("Rail Main IE")) {
                    list = railWorkflowTransactionRepository
                            .findLatestByRoleAndPlantIdAndWorkflowId(roleName, null, workflowId);
                } else {
                    list = railWorkflowTransactionRepository
                            .findLastPendingRequestsByRoleAndPlantIdAndWorkflowId(roleName, null, workflowId);
                }
            }
        } else {
            if (plantId != null && !plantId.trim().isEmpty()) {
                if (roleName.equalsIgnoreCase("Rail Main IE")) {
                    list = railWorkflowTransactionRepository
                            .findLatestByRoleAndPlantId(roleName, plantId.trim());
                } else {
                    list = railWorkflowTransactionRepository
                            .findLastPendingRequestsByRoleAndPlantId(roleName, plantId.trim());
                }
            } else {
                if (roleName.equalsIgnoreCase("Rail Main IE")) {
                    list = railWorkflowTransactionRepository
                            .findLatestByRole(roleName);
                } else {
                    list = railWorkflowTransactionRepository
                            .findLastPendingRequestsByRole(roleName);
                }
            }
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
        return allCompletedWorkflowTransitions(null, null, null);
    }

    @Override
    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions(Long userId, String plantId) {
        return allCompletedWorkflowTransitions(userId, plantId, null);
    }

    @Override
    public List<RailWorkflowTransactionDto> allCompletedWorkflowTransitions(Long userId, String plantId, Long workflowId) {

        List<RailWorkflowTransaction> list;
        if (workflowId != null) {
            list = railWorkflowTransactionRepository.findCompletedRequestsByPlantIdAndWorkflowId(plantId != null ? plantId.trim() : null, workflowId);
        } else if (plantId != null && !plantId.trim().isEmpty()) {
            list = railWorkflowTransactionRepository.findCompletedRequestsByPlantId(plantId.trim());
        } else {
            list = railWorkflowTransactionRepository.findCompletedRequests();
        }

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
        List<String> poiCodes = poiIeMappingRepository.findDistinctPoiCodesByIeUserId(Math.toIntExact(userId));
        if (poiCodes.isEmpty()) {
            return new ArrayList<>();
        }

        return railPadPincodePoIMappingRepository.findDistinctCompanyNamesByPoiCodeIn(poiCodes);
    }

    @Override
    public List<String> getPlantsByCompanyName(String companyName) {
        List<String> vendorCodes = railPadPincodePoIMappingRepository.findDistinctVendorCodesByCompanyName(companyName);
        if (vendorCodes.isEmpty()) {
            return new ArrayList<>();
        }

        return railVendorPlantsRepository.findDistinctPlantIdsByVendorCodeIn(vendorCodes);
    }

    @Override
    public List<RailWorkflowTransactionDto> getPendingVerifiedCalls() {
        List<String> pendingActions = java.util.Arrays.asList(
            "VERIFY",
            "MAIN_IE_SCHEDULE_CALL",
            "INITIATE_CALL",
            "PO_VERIFICATION",
            "PAUSE",
            "RESUME",
            "RESCHEDULE_CALL"
        );
        List<RailWorkflowTransaction> list = railWorkflowTransactionRepository.findPendingVerifiedCalls(pendingActions);
        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
                .toList();
    }

    @Override
    public List<java.util.Map<String, Object>> getRailpadRemapAvailableUsers() {
        List<UserMaster> users = userMasterRepository.findByRoleNameContaining("Main IE");
        List<java.util.Map<String, Object>> available = new ArrayList<>();
        for (UserMaster u : users) {
            java.util.Map<String, Object> emp = new java.util.HashMap<>();
            emp.put("userId", u.getUserId());
            emp.put("employeeCode", u.getEmployeeCode());
            emp.put("fullName", u.getFullName());
            emp.put("role", "Rail Main IE");
            available.add(emp);
        }
        return available;
    }

    @Override
    @Transactional
    public void submitRailpadRemap(RailpadRemapSubmitDto dto) {
        String callNo = dto.getCallNo();
        String plantId = dto.getPlantId();
        Integer oldUserId = dto.getOldUserId();
        Integer newUserId = dto.getNewUserId();

        if (callNo == null || plantId == null || newUserId == null) {
            throw new IllegalArgumentException("callNo, plantId, and newUserId are required for remapping");
        }

        // Update IE mapping in POI-IE mapping
        poiIeMappingRepository.updateIeUserIdByPlantId(plantId, oldUserId, newUserId);

        // Update workflow transaction assignedToUser for latest transaction
        List<RailWorkflowTransaction> txList = railWorkflowTransactionRepository.findByRequestIdOrderByCreatedDateAsc(callNo);
        if (txList != null && !txList.isEmpty()) {
            RailWorkflowTransaction tx = txList.get(txList.size() - 1);
            tx.setAssignedToUser(Long.valueOf(newUserId));
            railWorkflowTransactionRepository.save(tx);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public String saveRailpadMapping(com.sarthi.SRailPad.dto.RailpadPoiIeMappingReqDto req) {
        List<RailPoiIeMapping> existingMappings = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(req.getPoiCode(), req.getPlantId(), req.getIeType());
        
        if (existingMappings != null && !existingMappings.isEmpty()) {
            throw new RuntimeException("Mapping already exists for this POI and Plant ID for the given IE type");
        }

        RailPoiIeMapping newMapping = new RailPoiIeMapping();
        newMapping.setPoiCode(req.getPoiCode());
        newMapping.setPlantId(req.getPlantId());
        newMapping.setIeUserId(req.getIeUserId());
        newMapping.setIeType(req.getIeType());
        newMapping.setCreatedDate(java.time.LocalDateTime.now());

        poiIeMappingRepository.save(newMapping);

        return "Railpad mapping created successfully";
    }
}