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
import com.sarthi.SRailPad.entity.RailCallCancellationDetail;
import com.sarthi.SRailPad.entity.RailVendorFinancialLiability;
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
import java.util.Map;
import java.util.Objects;
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
    private com.sarthi.repository.PoItemRepository poItemRepository;
    private PincodePoIMappingRepository pincodePoIMappingRepository;

    private NotificationService notificationService;
    private RailCallCancellationDetailRepository railCallCancellationDetailRepository;
    private RailVendorFinancialLiabilityRepository railVendorFinancialLiabilityRepository;
    private com.sarthi.SRailPad.repository.inspectionCall.RailInspectionBatchRepository railInspectionBatchRepository;
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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

        RailWorkflowTransaction current = null;

        // 1. First priority: Check if workflowTransitionId is provided, valid, and matches moduleId
        if (req.getWorkflowTransitionId() != null && req.getWorkflowTransitionId() > 0) {
            RailWorkflowTransaction txById = railWorkflowTransactionRepository
                    .findById(Math.toIntExact(req.getWorkflowTransitionId()))
                    .orElse(null);
            if (txById != null && (req.getModuleId() == null || req.getModuleId().equals(txById.getModuleId()))) {
                current = txById;
            }
        }

        // 2. Second priority: Find by requestId (filtered by moduleId if provided)
        if (current == null && req.getRequestId() != null && !req.getRequestId().isEmpty()) {
            List<RailWorkflowTransaction> allTx = railWorkflowTransactionRepository.findByRequestIdOrderByCreatedDateAsc(req.getRequestId());
            if (allTx != null && !allTx.isEmpty()) {
                if (req.getModuleId() != null) {
                    List<RailWorkflowTransaction> moduleTx = allTx.stream()
                            .filter(t -> t.getModuleId() != null && t.getModuleId().equals(req.getModuleId()))
                            .collect(java.util.stream.Collectors.toList());
                    if (!moduleTx.isEmpty()) {
                        current = moduleTx.get(moduleTx.size() - 1);
                    }
                }
                if (current == null) {
                    current = allTx.get(allTx.size() - 1);
                }
            }
        }

        if (current == null) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Workflow transition not found"
                    )
            );
        }

        // Verify that the transaction is not already in a terminal state
        if (("COMPLETED".equalsIgnoreCase(current.getStatus()) || "COMPLETED".equalsIgnoreCase(current.getJobStatus())) 
            && !req.getAction().equalsIgnoreCase("IC_ISSUE") 
            && !req.getAction().equalsIgnoreCase("IC_GENERATION")
            && !req.getAction().equalsIgnoreCase("DSC_SIGN_IC")
            && !req.getAction().equalsIgnoreCase("GENERATE_IC")) {
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



        RailTransitionMaster transition = null;

        if ((req.getAction().equalsIgnoreCase("VERIFY_MATERIAL_AVAILABILITY") && "NO".equalsIgnoreCase(req.getMaterialAvailable()))
                || req.getAction().equalsIgnoreCase("CANCEL_CALL")
                || req.getAction().equalsIgnoreCase("CANCELLED")) {
            
            tx.setCurrentRole(current.getNextRole() != null ? current.getNextRole() : current.getCurrentRole());
            tx.setNextRole(null);
            tx.setStatus("CANCELLED");
            tx.setJobStatus("CANCELLED");
            tx.setAction("VERIFY_MATERIAL_AVAILABILITY");

            String cancelRemarks = req.getRemarks() != null && !req.getRemarks().isEmpty()
                    ? req.getRemarks()
                    : "Cancelled - Material Not Available";
            tx.setRemarks(cancelRemarks);

            // 1. Update rail_inspection_call status to CANCELLED
            try {
                Optional<RailInspectionCall> icOpt = railInspectionCallRepository.findByCallNo(req.getRequestId());
                if (icOpt.isPresent()) {
                    RailInspectionCall ic = icOpt.get();
                    ic.setStatus("CANCELLED");
                    railInspectionCallRepository.save(ic);
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Could not update RailInspectionCall status: " + ex.getMessage());
            }

            // 2. Release/Reset batch quantities for this call in rail_inspection_batch
            try {
                if (jdbcTemplate != null) {
                    jdbcTemplate.update(
                            "UPDATE rail_inspection_batch b " +
                            "JOIN rail_inspection_lot l ON b.lot_id = l.id " +
                            "JOIN rail_inspection_call c ON l.inspection_call_id = c.id " +
                            "SET b.qty_to_use = 0, b.quantity = 0, b.balance_qty = 0 " +
                            "WHERE c.call_no = ?",
                            req.getRequestId()
                    );
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Could not reset rail_inspection_batch quantities: " + ex.getMessage());
            }

            // 3. Save to rail_call_cancellation_details
            try {
                String dynamicVendorCode = req.getVendorCode();
                if (dynamicVendorCode == null || dynamicVendorCode.isEmpty()) {
                    dynamicVendorCode = current.getVendorCode() != null ? current.getVendorCode() : "";
                }

                String creatorId = req.getActionBy() != null ? String.valueOf(req.getActionBy()) :
                                  (req.getUpdatedBy() != null ? req.getUpdatedBy() : null);

                RailCallCancellationDetail cancellationDetail = new RailCallCancellationDetail();
                cancellationDetail.setCallNumber(req.getRequestId());
                cancellationDetail.setVendorCode(dynamicVendorCode);
                cancellationDetail.setCancellationBasis(req.getCancellationBasis() != null ? req.getCancellationBasis() : "NON_CHARGEABLE");
                cancellationDetail.setVisitStatus(req.getVisitStatus());
                cancellationDetail.setReasons(req.getCancellationReasons() != null ? String.join("; ", req.getCancellationReasons()) : req.getRemarks());
                cancellationDetail.setCancellationDescription(req.getCancellationDescription());
                cancellationDetail.setMaterialValue(req.getMaterialValue());
                cancellationDetail.setPercentage(req.getCancellationPercentage());
                cancellationDetail.setCalculatedCharges(req.getCalculatedCharges());
                cancellationDetail.setMaximumCap(req.getMaximumCap());
                cancellationDetail.setFinalCancellationCharges(req.getFinalCancellationCharges() != null ? req.getFinalCancellationCharges() : java.math.BigDecimal.ZERO);
                cancellationDetail.setDocumentName(req.getDocumentName());
                cancellationDetail.setActionBy(req.getActionBy() != null ? req.getActionBy() : 0L);
                cancellationDetail.setCreatedBy(creatorId);
                cancellationDetail.setUpdatedBy(creatorId);

                railCallCancellationDetailRepository.save(cancellationDetail);

                // 4. If CHARGEABLE and charges > 0, log vendor financial liability
                if ("CHARGEABLE".equalsIgnoreCase(req.getCancellationBasis()) && 
                    req.getFinalCancellationCharges() != null && 
                    req.getFinalCancellationCharges().compareTo(java.math.BigDecimal.ZERO) > 0) {

                    RailVendorFinancialLiability liability = new RailVendorFinancialLiability();
                    liability.setCallNumber(req.getRequestId());
                    liability.setVendorCode(dynamicVendorCode);
                    liability.setLiabilityType("CANCELLATION_CHARGES");
                    liability.setAmount(req.getFinalCancellationCharges());
                    liability.setPaymentStatus("PENDING");
                    liability.setCreatedBy(creatorId);
                    liability.setUpdatedBy(creatorId);

                    railVendorFinancialLiabilityRepository.save(liability);
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Failed to persist RailCallCancellationDetail / RailVendorFinancialLiability: " + ex.getMessage());
            }

            tx.setCreatedBy(current.getCreatedBy());
            tx.setModifiedBy(req.getActionBy());
            tx.setCreatedDate(LocalDateTime.now());
            tx.setUpdatedDate(LocalDateTime.now());

            RailWorkflowTransaction saved = railWorkflowTransactionRepository.save(tx);
            return mapToResponse(saved);
        } else if (req.getAction().equalsIgnoreCase("IC_ISSUE") 
                || req.getAction().equalsIgnoreCase("IC_GENERATION")
                || req.getAction().equalsIgnoreCase("GENERATE_IC")
                || req.getAction().equalsIgnoreCase("DSC_SIGN_IC")) {
            
            transition = new RailTransitionMaster();
            transition.setNextRoleId(null); // Keep it in a terminal state
            tx.setCurrentRole(current.getNextRole() != null ? current.getNextRole() : current.getCurrentRole());
            tx.setStatus(AppConstant.COMPLETED_TYPE);
            tx.setJobStatus(AppConstant.COMPLETED_TYPE);
            
        } else if(current.getWorkflowId().equals(2L)) {

            List<RailTransitionMaster> transitions =
                    railTransitionMasterRepository
                            .findByWorkflowIdAndCurrentRoleIdAndCurrentAction(
                                    current.getWorkflowId().intValue(),
                                    getRoleId(current.getNextRole()),
                                    req.getAction()
                            );

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

        } else {
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

        // Determine assignedToUser based on nextRole & plant mapping
        Long targetAssignedUser = req.getActionBy();
        if ("Rail Main IE".equalsIgnoreCase(tx.getNextRole()) || "VERIFY".equalsIgnoreCase(req.getAction())) {
            Optional<RailPoiIeMapping> mappingOpt = poiIeMappingRepository
                    .findByPlantIdAndIeType(current.getPlantId(), "Main IE");
            if (mappingOpt.isEmpty()) {
                mappingOpt = poiIeMappingRepository.findByPlantIdAndIeType(current.getPlantId(), "MAIN_IE");
            }
            if (mappingOpt.isPresent() && mappingOpt.get().getIeUserId() != null) {
                targetAssignedUser = mappingOpt.get().getIeUserId().longValue();
            }
        } else if ("Rail Process IE".equalsIgnoreCase(tx.getNextRole())) {
            Optional<RailPoiIeMapping> mappingOpt = poiIeMappingRepository
                    .findByPlantIdAndIeType(current.getPlantId(), "Process IE");
            if (mappingOpt.isEmpty()) {
                mappingOpt = poiIeMappingRepository.findByPlantIdAndIeType(current.getPlantId(), "PROCESS_IE");
            }
            if (mappingOpt.isPresent() && mappingOpt.get().getIeUserId() != null) {
                targetAssignedUser = mappingOpt.get().getIeUserId().longValue();
            }
        }
        tx.setAssignedToUser(targetAssignedUser);

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
                
                String rio = railWorkflowTransactionRepository.findRioByCallNo(tx.getRequestId());
                if (rio == null || rio.trim().isEmpty()) {
                    rio = current.getRio();
                }
                
                RailInspectionCompleteDetails details = new RailInspectionCompleteDetails();
                details.setCallNo(ic.getCallNo());
                details.setPoNo(ic.getPoNo());
                details.setCertificateNo(generateCertificateNo(rio, ic.getCallNo(), userShortName));
                details.setCreatedOn(LocalDateTime.now());
                
                railInspectionCompleteDetailsRepository.save(details);
                }
            }
        }

        return mapToResponse(saved);
    }

    private String generateCertificateNo(String rioName, String callNo, String userShortName) {
        String rioFirstLetter = (rioName != null && !rioName.trim().isEmpty())
                ? rioName.trim().substring(0, 1).toUpperCase()
                : "X";
        String userSuffix = (userShortName != null && !userShortName.trim().isEmpty())
                ? userShortName.trim().toUpperCase()
                : "XX";
        return rioFirstLetter + "/" + callNo + "/" + userSuffix;
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
        
        String vendorNameCacheKey = "vendorName_" + (tx.getVendorCode() != null ? tx.getVendorCode() : "") + "_" + (tx.getPlantId() != null ? tx.getPlantId() : "");
        String vName = null;
        if (cache.containsKey(vendorNameCacheKey)) {
            vName = (String) cache.get(vendorNameCacheKey);
        } else {
            // 1. Try VendorMaster by vendorCode
            if (tx.getVendorCode() != null && !tx.getVendorCode().trim().isEmpty()) {
                vName = vendorMasterRepository.findByVendorCode(tx.getVendorCode().trim())
                    .map(com.sarthi.entity.VendorMaster::getVendorName)
                    .orElse(null);
            }

            // 2. Try RailVendorPlants by vendorCode
            if ((vName == null || vName.trim().isEmpty()) && tx.getVendorCode() != null && !tx.getVendorCode().trim().isEmpty()) {
                vName = railVendorPlantsRepository.findByVendorCode(tx.getVendorCode().trim())
                    .stream()
                    .map(com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants::getCompanyName)
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .findFirst()
                    .orElse(null);
            }

            // 3. Try RailVendorPlants by plantId
            if ((vName == null || vName.trim().isEmpty()) && tx.getPlantId() != null && !tx.getPlantId().trim().isEmpty()) {
                String pId = tx.getPlantId().trim();
                vName = railVendorPlantsRepository.findByPlantId(pId)
                    .map(com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants::getCompanyName)
                    .orElse(null);
                if (vName == null || vName.trim().isEmpty()) {
                    String altPlantId = pId.startsWith(":") ? pId.substring(1) : ":" + pId;
                    vName = railVendorPlantsRepository.findByPlantId(altPlantId)
                        .map(com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants::getCompanyName)
                        .orElse(null);
                }
            }

            // 4. Try RailInspectionCall by requestId / callNo
            if ((vName == null || vName.trim().isEmpty()) && tx.getRequestId() != null) {
                try {
                    vName = railInspectionCallRepository.findByCallNo(tx.getRequestId())
                        .map(com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall::getVendorName)
                        .orElse(null);
                } catch (Exception ignored) {}
            }
            cache.put(vendorNameCacheKey, vName);
        }

        dto.setVendorName(vName != null && !vName.trim().isEmpty() ? vName : tx.getVendorCode());
        dto.setPlantId(tx.getPlantId());
        dto.setPoiCode(tx.getPoiCode());

        // Fetch RailInspectionCall details
        com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall railCall = null;
        if (tx.getRequestId() != null) {
            String callCacheKey = "call_" + tx.getRequestId();
            if (cache.containsKey(callCacheKey)) {
                railCall = (com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall) cache.get(callCacheKey);
            } else {
                railCall = railInspectionCallRepository.findByCallNo(tx.getRequestId()).orElse(null);
                cache.put(callCacheKey, railCall);
            }
        }

        if (railCall != null) {
            if (vName == null || vName.trim().isEmpty()) {
                vName = railCall.getVendorName();
                if (vName != null && !vName.trim().isEmpty()) {
                    dto.setVendorName(vName);
                }
            }
            if (railCall.getInspectionDate() != null) {
                dto.setDesiredInspectionDate(railCall.getInspectionDate());
            }
            if (railCall.getRailPadType() != null && !railCall.getRailPadType().trim().isEmpty()) {
                dto.setRailPadType(railCall.getRailPadType());
            }
        }

        // Determine Stage of Inspection (RPP = Process, RPF / RFF = Final)
        String reqId = tx.getRequestId() != null ? tx.getRequestId().trim().toUpperCase() : "";
        String stage = "Final";
        if (reqId.startsWith("RPP")) {
            stage = "Process";
        } else if (reqId.startsWith("RPF") || reqId.startsWith("RFF")) {
            stage = "Final";
        } else if (railCall != null && "PROCESS".equalsIgnoreCase(railCall.getCallType())) {
            stage = "Process";
        } else if (tx.getWorkflowId() != null && tx.getWorkflowId().equals(1L)) {
            stage = "Process";
        }

        dto.setStageOfInspection(stage);
        dto.setProductType(stage);
        dto.setProductStage(stage);
        dto.setCallType(stage);

        String rawPoNo = railCall != null ? railCall.getPoNo() : null;
        String poSr = railCall != null ? railCall.getPoSr() : null;
        String poNo = null;

        if (rawPoNo != null && !rawPoNo.trim().isEmpty()) {
            if (rawPoNo.contains("/")) {
                String[] parts = rawPoNo.split("/");
                poNo = parts[0].trim();
                if (poSr == null || poSr.trim().isEmpty()) {
                    poSr = parts.length > 1 ? parts[1].trim() : "";
                }
            } else {
                poNo = rawPoNo.trim();
            }
        }

        dto.setPoNo(poNo);
        dto.setPoSr(poSr);

        // Fetch PO Header & PO Item details for Rly/PO/POSR, DP Date & Ext DP Date
        String rlyShortName = "";
        if (poNo != null && !poNo.isEmpty()) {
            try {
                final String finalPoNo = poNo;
                String poHeaderCacheKey = "poHeader_" + finalPoNo;
                com.sarthi.entity.PoHeader poHeader = null;
                if (cache.containsKey(poHeaderCacheKey)) {
                    poHeader = (com.sarthi.entity.PoHeader) cache.get(poHeaderCacheKey);
                } else {
                    poHeader = poHeaderRepository.findByPoNo(finalPoNo).orElse(null);
                    cache.put(poHeaderCacheKey, poHeader);
                }

                if (poHeader != null) {
                    rlyShortName = poHeader.getRlyShortName() != null && !poHeader.getRlyShortName().trim().isEmpty() ? poHeader.getRlyShortName().trim()
                            : (poHeader.getRlyCd() != null ? poHeader.getRlyCd().trim() : "");

                    String poItemCacheKey = "poItem_" + poHeader.getId() + "_" + (poSr != null ? poSr.trim() : "");
                    final String targetSr = poSr != null ? poSr.trim() : "";
                    com.sarthi.entity.PoItem poItem = null;
                    if (cache.containsKey(poItemCacheKey)) {
                        poItem = (com.sarthi.entity.PoItem) cache.get(poItemCacheKey);
                    } else {
                        List<com.sarthi.entity.PoItem> items = poItemRepository.findByPoHeader_Id(poHeader.getId());
                        poItem = items.stream()
                                .filter(i -> i.getItemSrNo() != null && i.getItemSrNo().trim().equalsIgnoreCase(targetSr))
                                .findFirst()
                                .orElse(items.isEmpty() ? null : items.get(0));
                        cache.put(poItemCacheKey, poItem);
                    }

                    if (poItem != null) {
                        if (poItem.getDeliveryDate() != null) {
                            dto.setDpDate(poItem.getDeliveryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                        if (poItem.getExtendedDeliveryDate() != null) {
                            dto.setExtDpDate(poItem.getExtendedDeliveryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error mapping PO details: " + e.getMessage());
            }
        }

        dto.setRlyShortName(rlyShortName);

        // Format RLY / PO / PO SR NO (e.g. NCR / 08241015101234 / 001)
        StringBuilder rlyPoSrBuilder = new StringBuilder();
        if (rlyShortName != null && !rlyShortName.trim().isEmpty()) {
            rlyPoSrBuilder.append(rlyShortName.trim());
        }
        if (poNo != null && !poNo.trim().isEmpty()) {
            if (rlyPoSrBuilder.length() > 0) rlyPoSrBuilder.append(" / ");
            rlyPoSrBuilder.append(poNo.trim());
        }
        if (poSr != null && !poSr.trim().isEmpty()) {
            if (rlyPoSrBuilder.length() > 0) rlyPoSrBuilder.append(" / ");
            rlyPoSrBuilder.append(poSr.trim());
        }
        dto.setRlyPoSrNo(rlyPoSrBuilder.length() > 0 ? rlyPoSrBuilder.toString() : (rawPoNo != null ? rawPoNo : "-"));

        // Fetch Place of Inspection (company_name, unit_name, address)
        String poiCodeToSearch = tx.getPoiCode();
        String vendorCodeToSearch = tx.getVendorCode() != null ? tx.getVendorCode() : (railCall != null ? railCall.getVendorCode() : null);

        RailPadPincodePoIMapping poiMapping = null;
        if (poiCodeToSearch != null && !poiCodeToSearch.trim().isEmpty()) {
            String poiCacheKey = "railpad_poi_" + poiCodeToSearch.trim();
            if (cache.containsKey(poiCacheKey)) {
                poiMapping = (RailPadPincodePoIMapping) cache.get(poiCacheKey);
            } else {
                List<RailPadPincodePoIMapping> list = railPadPincodePoIMappingRepository.findByPoiCode(poiCodeToSearch.trim());
                poiMapping = (list != null && !list.isEmpty()) ? list.get(0) : null;
                cache.put(poiCacheKey, poiMapping);
            }
        }
        if (poiMapping == null && vendorCodeToSearch != null && !vendorCodeToSearch.trim().isEmpty()) {
            String vendorPoiCacheKey = "railpad_vendor_poi_" + vendorCodeToSearch.trim();
            if (cache.containsKey(vendorPoiCacheKey)) {
                poiMapping = (RailPadPincodePoIMapping) cache.get(vendorPoiCacheKey);
            } else {
                poiMapping = railPadPincodePoIMappingRepository.findByVendorCode(vendorCodeToSearch.trim()).orElse(null);
                cache.put(vendorPoiCacheKey, poiMapping);
            }
        }

        if (poiMapping != null) {
            List<String> addressParts = new ArrayList<>();
            if (poiMapping.getCompanyName() != null && !poiMapping.getCompanyName().trim().isEmpty()) {
                addressParts.add(poiMapping.getCompanyName().trim());
            }
            if (poiMapping.getUnitName() != null && !poiMapping.getUnitName().trim().isEmpty()) {
                addressParts.add(poiMapping.getUnitName().trim());
            }
            if (poiMapping.getAddress() != null && !poiMapping.getAddress().trim().isEmpty()) {
                addressParts.add(poiMapping.getAddress().trim());
            }
            if (!addressParts.isEmpty()) {
                dto.setPlaceOfInspection(String.join(", ", addressParts));
            }
        }
        if (dto.getPlaceOfInspection() == null || dto.getPlaceOfInspection().trim().isEmpty()) {
            dto.setPlaceOfInspection(tx.getPoiCode() != null ? tx.getPoiCode() : "-");
        }

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
                if (cache.containsKey(vendorIdCacheKey)) {
                    vendorId = (String) cache.get(vendorIdCacheKey);
                } else {
                    vendorId = railPadPincodePoIMappingRepository.findVendorCodeByPoiCode(tx.getPoiCode()).orElse(null);
                    cache.put(vendorIdCacheKey, vendorId);
                }
            }
        } else {
            // Process IE / Main IE mappings
            if (tx.getPoiCode() != null && tx.getPlantId() != null) {
                String ieType = tx.getWorkflowId().equals(2L) ? "MAIN_IE" : "PROCESS_IE";
                String mappingCacheKey = "mapping_" + tx.getPoiCode() + "_" + tx.getPlantId() + "_" + ieType;
                if (cache.containsKey(mappingCacheKey)) {
                    mappings = (List<RailPoiIeMapping>) cache.get(mappingCacheKey);
                } else {
                    mappings = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(tx.getPoiCode(), tx.getPlantId(), ieType);
                    cache.put(mappingCacheKey, mappings);
                }
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
            Long vendorUserId = null;
            if (cache.containsKey(vendorUserCacheKey)) {
                vendorUserId = (Long) cache.get(vendorUserCacheKey);
            } else {
                vendorUserId = railVendorPlantsRepository.findVendorUserIdByVendorCode(finalVendorId).orElse(null);
                cache.put(vendorUserCacheKey, vendorUserId);
            }
            if (vendorUserId != null) {
                dto.setAssignedToUser(vendorUserId);
            }
        }

        dto.setAccessibleUserIds(userIds);

        if (dto.getAssignedToUser() == null && userIds != null && !userIds.isEmpty()) {
            dto.setAssignedToUser(userIds.get(0).longValue());
        }

        if (dto.getAssignedToUser() != null) {
            String userCacheKey = "user_" + dto.getAssignedToUser();
            UserMaster user = null;
            if (cache.containsKey(userCacheKey)) {
                user = (UserMaster) cache.get(userCacheKey);
            } else {
                user = userMasterRepository.findById(Math.toIntExact(dto.getAssignedToUser())).orElse(null);
                cache.put(userCacheKey, user);
            }
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
        if (list != null && !list.isEmpty()) {
            preloadCache(list, cache);
        }
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
        if (list != null && !list.isEmpty()) {
            preloadCache(list, cache);
        }
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
        if (list != null && !list.isEmpty()) {
            preloadCache(list, cache);
        }
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
        if (list != null && !list.isEmpty()) {
            preloadCache(list, cache);
        }
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
        if (list != null && !list.isEmpty()) {
            preloadCache(list, cache);
        }
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
                .toList();
    }

    private void preloadCache(List<RailWorkflowTransaction> list, java.util.Map<String, Object> cache) {
        if (list == null || list.isEmpty() || cache == null) return;

        // 1. Preload calls
        List<String> requestIds = list.stream()
                .map(RailWorkflowTransaction::getRequestId)
                .filter(r -> r != null && !r.trim().isEmpty())
                .distinct()
                .toList();

        Map<String, RailInspectionCall> callMap = new java.util.HashMap<>();
        if (!requestIds.isEmpty()) {
            try {
                List<RailInspectionCall> calls = railInspectionCallRepository.findByCallNoIn(requestIds);
                for (RailInspectionCall c : calls) {
                    if (c.getCallNo() != null) {
                        callMap.put(c.getCallNo(), c);
                        cache.put("call_" + c.getCallNo(), c);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preloading calls: " + e.getMessage());
            }
        }
        for (String reqId : requestIds) {
            String callCacheKey = "call_" + reqId;
            if (!cache.containsKey(callCacheKey)) {
                cache.put(callCacheKey, null);
            }
        }

        // 2. Preload PO Headers
        List<String> poNos = new ArrayList<>();
        for (RailInspectionCall c : callMap.values()) {
            if (c.getPoNo() != null && !c.getPoNo().trim().isEmpty()) {
                String p = c.getPoNo().trim();
                poNos.add(p.contains("/") ? p.split("/")[0].trim() : p);
            }
        }
        poNos = poNos.stream().distinct().toList();

        Map<String, com.sarthi.entity.PoHeader> poHeaderMap = new java.util.HashMap<>();
        if (!poNos.isEmpty()) {
            try {
                List<com.sarthi.entity.PoHeader> headers = poHeaderRepository.findByPoNoIn(poNos);
                for (com.sarthi.entity.PoHeader ph : headers) {
                    if (ph.getPoNo() != null) {
                        poHeaderMap.put(ph.getPoNo(), ph);
                        cache.put("poHeader_" + ph.getPoNo(), ph);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preloading PO headers: " + e.getMessage());
            }
        }
        for (String poNo : poNos) {
            String poHeaderCacheKey = "poHeader_" + poNo;
            if (!cache.containsKey(poHeaderCacheKey)) {
                cache.put(poHeaderCacheKey, null);
            }
        }

        // 3. Preload PO Items
        List<Long> headerIds = poHeaderMap.values().stream().map(com.sarthi.entity.PoHeader::getId).filter(Objects::nonNull).toList();
        if (!headerIds.isEmpty()) {
            try {
                List<com.sarthi.entity.PoItem> poItems = poItemRepository.findByPoHeader_IdIn(headerIds);
                for (com.sarthi.entity.PoItem item : poItems) {
                    if (item.getPoHeader() != null && item.getItemSrNo() != null) {
                        String key = "poItem_" + item.getPoHeader().getId() + "_" + item.getItemSrNo().trim();
                        cache.put(key, item);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preloading PO items: " + e.getMessage());
            }
        }

        // 4. Preload POI Mappings
        List<String> poiCodes = list.stream().map(RailWorkflowTransaction::getPoiCode).filter(p -> p != null && !p.trim().isEmpty()).distinct().toList();
        if (!poiCodes.isEmpty()) {
            try {
                List<RailPadPincodePoIMapping> poiList = railPadPincodePoIMappingRepository.findByPoiCodeIn(poiCodes);
                for (RailPadPincodePoIMapping p : poiList) {
                    if (p.getPoiCode() != null) {
                        cache.put("railpad_poi_" + p.getPoiCode().trim(), p);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preloading POI mappings: " + e.getMessage());
            }
        }
        for (String poiCode : poiCodes) {
            String poiCacheKey = "railpad_poi_" + poiCode.trim();
            if (!cache.containsKey(poiCacheKey)) {
                cache.put(poiCacheKey, null);
            }
        }

        // 5. Preload UserMaster for assigned users
        List<Integer> userIds = list.stream()
                .map(RailWorkflowTransaction::getAssignedToUser)
                .filter(Objects::nonNull)
                .map(Math::toIntExact)
                .distinct()
                .toList();

        if (!userIds.isEmpty()) {
            try {
                List<UserMaster> users = userMasterRepository.findAllById(userIds);
                for (UserMaster u : users) {
                    if (u.getUserId() != null) {
                        cache.put("user_" + u.getUserId().longValue(), u);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error preloading users: " + e.getMessage());
            }
        }
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
    public String resolveRailpadPoiCode(String plantId, String providedPoiCode) {
        if (providedPoiCode != null && !providedPoiCode.isBlank() && providedPoiCode.trim().toUpperCase().startsWith("POI")) {
            return providedPoiCode.trim().toUpperCase();
        }

        String rawPlantId = plantId != null ? plantId.trim() : "";
        String cleanPlantId = rawPlantId.startsWith(":") ? rawPlantId.substring(1) : rawPlantId;
        String colonPlantId = rawPlantId.startsWith(":") ? rawPlantId : ":" + rawPlantId;

        RailVendorPlants plant = null;
        if (!rawPlantId.isEmpty()) {
            plant = railVendorPlantsRepository.findByPlantId(rawPlantId)
                    .orElseGet(() -> railVendorPlantsRepository.findByPlantId(cleanPlantId)
                    .orElseGet(() -> railVendorPlantsRepository.findByPlantId(colonPlantId).orElse(null)));
        }

        String companyName = (plant != null && plant.getCompanyName() != null) ? plant.getCompanyName().trim() : "";
        String vendorCode = "";
        if (plant != null && plant.getVendorCode() != null && !plant.getVendorCode().isBlank()) {
            vendorCode = plant.getVendorCode().trim();
        } else if (cleanPlantId.contains("/")) {
            vendorCode = cleanPlantId.substring(0, cleanPlantId.indexOf("/")).trim();
        } else if (providedPoiCode != null && !providedPoiCode.isBlank()) {
            vendorCode = providedPoiCode.trim();
        }

        String cleanVendorCode = vendorCode.startsWith(":") ? vendorCode.substring(1) : vendorCode;
        String colonVendorCode = vendorCode.startsWith(":") ? vendorCode : ":" + vendorCode;

        RailPadPincodePoIMapping mapping = null;
        if (!companyName.isEmpty() && !cleanVendorCode.isEmpty()) {
            mapping = railPadPincodePoIMappingRepository
                    .findByVendorCodeAndCompanyName(vendorCode, companyName)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCodeAndCompanyName(colonVendorCode, companyName)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCodeAndCompanyName(cleanVendorCode, companyName)
                    .orElse(null)));
        }

        if (mapping == null && !cleanVendorCode.isEmpty()) {
            mapping = railPadPincodePoIMappingRepository
                    .findByVendorCode(vendorCode)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCode(colonVendorCode)
                    .orElseGet(() -> railPadPincodePoIMappingRepository.findByVendorCode(cleanVendorCode)
                    .orElse(null)));
        }

        if (mapping == null && !companyName.isEmpty()) {
            List<RailPadPincodePoIMapping> listByComp = railPadPincodePoIMappingRepository.findByCompanyName(companyName);
            if (listByComp != null && !listByComp.isEmpty()) {
                mapping = listByComp.get(0);
            }
        }

        if (mapping != null && mapping.getPoiCode() != null && !mapping.getPoiCode().isBlank()) {
            return mapping.getPoiCode().trim().toUpperCase();
        }

        if (pincodePoIMappingRepository != null && !cleanVendorCode.isEmpty()) {
            List<com.sarthi.entity.PincodePoIMapping> generalList = pincodePoIMappingRepository.findByVendorCode(cleanVendorCode);
            if (generalList != null && !generalList.isEmpty() && generalList.get(0).getPoiCode() != null) {
                return generalList.get(0).getPoiCode().trim().toUpperCase();
            }
        }

        return (providedPoiCode != null && !providedPoiCode.isBlank()) ? providedPoiCode.trim() : null;
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

        String cleanPlantId = plantId.replace(":", "");
        String correctPoiCode = resolveRailpadPoiCode(plantId, null);

        // Update IE mapping in POI-IE mapping
        poiIeMappingRepository.updateIeUserIdByPlantId(plantId, oldUserId, newUserId);

        // Ensure any mappings for this plantId have proper poiCode (instead of vendorCode)
        if (correctPoiCode != null && correctPoiCode.startsWith("POI")) {
            List<RailPoiIeMapping> mappings = poiIeMappingRepository.findAll();
            for (RailPoiIeMapping m : mappings) {
                if (m.getPlantId() != null && m.getPlantId().replace(":", "").equalsIgnoreCase(cleanPlantId)) {
                    if (m.getPoiCode() == null || !m.getPoiCode().toUpperCase().startsWith("POI")) {
                        m.setPoiCode(correctPoiCode);
                        poiIeMappingRepository.save(m);
                    }
                }
            }
        }

        // Update workflow transaction assignedToUser for latest transaction
        List<RailWorkflowTransaction> txList = railWorkflowTransactionRepository.findByRequestIdOrderByCreatedDateAsc(callNo);
        if (txList != null && !txList.isEmpty()) {
            RailWorkflowTransaction tx = txList.get(txList.size() - 1);
            tx.setAssignedToUser(Long.valueOf(newUserId));
            if (correctPoiCode != null && (tx.getPoiCode() == null || !tx.getPoiCode().startsWith("POI"))) {
                tx.setPoiCode(correctPoiCode);
            }
            railWorkflowTransactionRepository.save(tx);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public String saveRailpadMapping(com.sarthi.SRailPad.dto.RailpadPoiIeMappingReqDto req) {
        if (req == null || req.getIeUserId() == null) {
            throw new RuntimeException("IE User ID is required for mapping");
        }

        String reqPlantId = req.getPlantId() != null ? req.getPlantId().trim() : "";
        String cleanReqPlantId = reqPlantId.replace(":", "");
        boolean isProcessIe = req.getIeType() != null && 
            (req.getIeType().toUpperCase().contains("PROCESS"));

        // Resolve accurate POI Code
        String poiCodeToSave = resolveRailpadPoiCode(req.getPlantId(), req.getPoiCode());

        // If updating an existing mapping (ID provided)
        if (req.getId() != null) {
            java.util.Optional<RailPoiIeMapping> existingOpt = poiIeMappingRepository.findById(req.getId());
            if (existingOpt.isPresent()) {
                RailPoiIeMapping existing = existingOpt.get();
                // Check if updating ieUserId violates single-plant rule for Process IE
                if (isProcessIe) {
                    List<RailPoiIeMapping> userMappings = poiIeMappingRepository.findByIeUserId(req.getIeUserId());
                    if (userMappings != null) {
                        for (RailPoiIeMapping m : userMappings) {
                            if (!m.getId().equals(req.getId())) {
                                boolean mIsProcess = m.getIeType() != null && m.getIeType().toUpperCase().contains("PROCESS");
                                if (mIsProcess) {
                                    String existingPlant = m.getPlantId() != null ? m.getPlantId().trim().replace(":", "") : "";
                                    if (!existingPlant.isEmpty() && !existingPlant.equalsIgnoreCase(cleanReqPlantId)) {
                                        throw new RuntimeException("This Process IE is already mapped to another Plant ID (" + m.getPlantId() + ")");
                                    }
                                }
                            }
                        }
                    }
                }
                
                existing.setPoiCode(poiCodeToSave);
                existing.setPlantId(req.getPlantId());
                existing.setIeUserId(req.getIeUserId());
                existing.setIeType(isProcessIe ? "Process IE" : "Main IE");
                poiIeMappingRepository.save(existing);
                return "Railpad mapping updated successfully";
            }
        }

        // 1. Check existing mappings for THIS ieUserId when creating new
        List<RailPoiIeMapping> userMappings = poiIeMappingRepository.findByIeUserId(req.getIeUserId());
        if (userMappings != null) {
            for (RailPoiIeMapping m : userMappings) {
                String existingPlant = m.getPlantId() != null ? m.getPlantId().trim().replace(":", "") : "";
                boolean existingIsProcess = m.getIeType() != null && m.getIeType().toUpperCase().contains("PROCESS");

                if (existingIsProcess == isProcessIe && existingPlant.equalsIgnoreCase(cleanReqPlantId)) {
                    if (poiCodeToSave != null && !poiCodeToSave.isEmpty() && !poiCodeToSave.equalsIgnoreCase(m.getPoiCode())) {
                        m.setPoiCode(poiCodeToSave);
                        poiIeMappingRepository.save(m);
                    }
                    return "Railpad mapping already exists for this Plant ID";
                }
            }
        }

        // 2. For Main IE, if a Main IE mapping already exists for this plant ID, update the Main IE
        if (!isProcessIe) {
            java.util.Optional<RailPoiIeMapping> existingMainIe = poiIeMappingRepository.findByPlantIdAndIeType(req.getPlantId(), req.getIeType());
            if (existingMainIe.isPresent()) {
                RailPoiIeMapping mainMapping = existingMainIe.get();
                mainMapping.setIeUserId(req.getIeUserId());
                if (poiCodeToSave != null && !poiCodeToSave.isEmpty()) {
                    mainMapping.setPoiCode(poiCodeToSave);
                }
                poiIeMappingRepository.save(mainMapping);
                return "Railpad Main IE mapping updated successfully";
            }
        }

        // 3. Create new mapping (Multiple Process IEs per plant ID are allowed)
        RailPoiIeMapping newMapping = new RailPoiIeMapping();
        newMapping.setPoiCode(poiCodeToSave);
        newMapping.setPlantId(req.getPlantId());
        newMapping.setIeUserId(req.getIeUserId());
        newMapping.setIeType(isProcessIe ? "Process IE" : "Main IE");
        newMapping.setCreatedDate(java.time.LocalDateTime.now());

        poiIeMappingRepository.save(newMapping);

        return "Railpad mapping created successfully";
    }
}