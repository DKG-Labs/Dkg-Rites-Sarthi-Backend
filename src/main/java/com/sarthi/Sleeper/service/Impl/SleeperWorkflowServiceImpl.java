package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.entity.SleeperTransitionMaster;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.repository.*;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.constant.AppConstant;
import com.sarthi.entity.*;
import com.sarthi.Sleeper.dto.SleeperRemapSubmitDto;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.repository.*;
import com.sarthi.util.NotificationService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCompleteDetails;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcEdit;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcSaveChanges;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCompleteDetailsRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperFinalIcEditRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperFinalIcSaveChangesRepository;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SleeperWorkflowServiceImpl implements SleeperWorkflowService {

    @Autowired
    private SleeperWorkflowRepository repository;
    @Autowired
    private SleeperWorkflowMasterRepository workflowRepository;

    @Autowired
    private SleeperModuleRepository moduleRepository;
    @Autowired
    private SleeperPincodePoIMappingRepository sleeperPincodePoIMappingRepository;

    @Autowired
    private UserRoleMasterRepository userRoleMasterRepository;
    @Autowired
    private UserMasterRepository userMasterRepository;
    @Autowired
    private SleeperPoiIeMappingRepository poiIeMappingRepository;
    @Autowired
    private SleeperTransitionMasterRepository sleeperTransitionMasterRepository;
    @Autowired
    private RoleMasterRepository roleMasterRepository;
    @Autowired
    private IeFieldsMappingRepository ieFieldsMappingRepository;
    @Autowired
    private RioUserRepository rioUserRepository;
    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;

    @Autowired
    private MixDesignRepository mixDesignRepository;
    @Autowired
    private PlantProfileRepository plantProfileRepository;
    @Autowired
    private RawMaterialSourceRepository rawMaterialSourceRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private VendorPlantRepository vendorPlantRepository;
    @Autowired
    private SleeperInspectionCallRepository sleeperInspectionCallRepository;
    @Autowired
    private SleeperInspectionCompleteDetailsRepository sleeperInspectionCompleteDetailsRepository;
    @Autowired
    private SleeperFinalIcEditRepository sleeperFinalIcEditRepository;
    @Autowired
    private SleeperFinalIcSaveChangesRepository sleeperFinalIcSaveChangesRepository;
    @Autowired
    private PoHeaderRepository poHeaderRepository;
    @Autowired
    private PoItemRepository poItemRepository;


    public void validateUser(Integer userId) {
        if (!userMasterRepository.existsById(userId)) {
            throw new InvalidInputException(
                    new ErrorDetails(AppConstant.USER_NOT_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION, "User not found."));
        }
    }

    /*
     * @Override
     * public SleeperWorkflowTransactionDto initiateWorkflow(
     * String requestId,
     * Long moduleId,
     * Long workflowId,
     * Long createdBy) {
     * 
     * validateUser(Math.toIntExact(createdBy));
     * validateWorkflowAndModule(workflowId, moduleId);
     * 
     * SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();
     * 
     * 
     * SleeperPincodePoIMapping mapping =
     * sleeperPincodePoIMappingRepository.findByVendorCode(String.valueOf(createdBy)
     * );
     * 
     * tx.setRequestId(requestId);
     * tx.setModuleId(moduleId);
     * tx.setWorkflowId(workflowId);
     * 
     * tx.setCurrentRole("Vendor");
     * tx.setNextRole("IE");
     * tx.setAction(AppConstant.CREATED_TYPE);
     * tx.setStatus(AppConstant.CREATED_TYPE);
     * 
     * tx.setPoiCode(mapping.getPoiCode());
     * tx.setCreatedBy(createdBy);
     * tx.setCreatedDate(LocalDateTime.now());
     * 
     * SleeperWorkflowTransaction saved = repository.save(tx);
     * 
     * return mapToResponse(saved);
     * }
     */
    @Override
    public SleeperWorkflowTransactionDto initiateWorkflow(
            String requestId,
            Long moduleId,
            Long workflowId,
            Long createdBy, String vendorCode, String plantId) {

        validateUser(Math.toIntExact(createdBy));
        if (workflowId == 1) {
            validateWorkflowAndModule(workflowId, moduleId);
        }
        SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

        SleeperPincodePoIMapping mapping = sleeperPincodePoIMappingRepository
                .findByVendorCode(String.valueOf(createdBy));
        if (mapping == null && vendorCode != null && !vendorCode.trim().isEmpty()) {
            mapping = sleeperPincodePoIMappingRepository.findByVendorCode(vendorCode.trim());
            if (mapping == null) {
                mapping = sleeperPincodePoIMappingRepository.findByVendorCode(vendorCode.replaceAll("^[:\\s]+", "").trim());
            }
        }

        tx.setRequestId(requestId);
        tx.setModuleId(moduleId);
        tx.setWorkflowId(workflowId);
        tx.setVendorCode(vendorCode);
        tx.setPlantId(plantId);

        // workflowId = 2 use TRANSITION_MASTER
        if (workflowId == 2) {

            SleeperTransitionMaster transition = sleeperTransitionMasterRepository
                    .findFirstByWorkflowIdAndCurrentActionOrderByTransitionOrderAsc(
                            workflowId.intValue(), AppConstant.CREATED_TYPE)
                    .orElseThrow(() -> new RuntimeException("Transition not configured"));

            tx.setCurrentRole(getRoleName(transition.getCurrentRoleId()));
            tx.setNextRole(getRoleName(transition.getNextRoleId()));

            tx.setAction(transition.getCurrentAction());
            tx.setStatus(AppConstant.CREATED_TYPE);
            if (transition.getNextRoleId().equals(2)) {
                String rio = null;

                // Step 1: Pick up RIO from vendor_plant of that plantId
                if (plantId != null && !plantId.trim().isEmpty()) {
                    List<com.sarthi.Sleeper.entity.VendorPlant> matchingPlants = vendorPlantRepository.findMatchingPlants(plantId.trim());
                    if (matchingPlants != null && !matchingPlants.isEmpty()) {
                        for (com.sarthi.Sleeper.entity.VendorPlant vp : matchingPlants) {
                            if (vp.getRio() != null && !vp.getRio().trim().isEmpty()) {
                                rio = vp.getRio().trim();
                                break;
                            }
                        }
                    }
                    if (rio == null) {
                        String cleanPlant = plantId.replaceAll("^[:\\s]+", "").trim();
                        var vpOpt = vendorPlantRepository.findByPlantId(plantId);
                        if (vpOpt.isEmpty() && !cleanPlant.isEmpty()) {
                            vpOpt = vendorPlantRepository.findByPlantId(cleanPlant);
                        }
                        if (vpOpt.isPresent() && vpOpt.get().getRio() != null && !vpOpt.get().getRio().trim().isEmpty()) {
                            rio = vpOpt.get().getRio().trim();
                        }
                    }
                }

                // Fallback to vendorCode lookup in vendor_plant
                if (rio == null && vendorCode != null && !vendorCode.trim().isEmpty()) {
                    String cleanVendor = vendorCode.replaceAll("^[:\\s]+", "").trim();
                    List<com.sarthi.Sleeper.entity.VendorPlant> vpList = vendorPlantRepository.findByVendorCode(cleanVendor);
                    if (vpList != null && !vpList.isEmpty()) {
                        for (com.sarthi.Sleeper.entity.VendorPlant vp : vpList) {
                            if (vp.getRio() != null && !vp.getRio().trim().isEmpty()) {
                                rio = vp.getRio().trim();
                                break;
                            }
                        }
                    }
                }

                // Fallback to IEFieldsMapping if not found in vendor_plant
                if (rio == null && mapping != null && mapping.getPoiCode() != null) {
                    try {
                        SleeperPincodePoIMapping poi = sleeperPincodePoIMappingRepository.findByPoiCode(mapping.getPoiCode()).orElse(null);
                        if (poi != null && poi.getPinCode() != null) {
                            String stage = "F";
                            String product = "Sleeper";
                            IEFieldsMapping map = ieFieldsMappingRepository
                                    .findByPinCodeProductAndStageMatch(poi.getPinCode(), product, stage).orElse(null);
                            if (map != null && map.getRio() != null) {
                                rio = map.getRio().trim();
                            }
                        }
                    } catch (Exception e) {
                        // ignore fallback error
                    }
                }

                if (rio != null) {
                    tx.setRio(rio);
                    String productType = "SLEEPER";
                    notificationService.sendInspectionCallAssignedToRio(productType, requestId, rio);
                }
            }

        } else {
            // workflowId = 1
            tx.setCurrentRole("Vendor");
            tx.setNextRole("IE");

            tx.setAction(AppConstant.CREATED_TYPE);
            tx.setStatus(AppConstant.CREATED_TYPE);
        }

        if (mapping != null) {
            tx.setPoiCode(mapping.getPoiCode());
        }
        tx.setCreatedBy(createdBy);
        tx.setCreatedDate(LocalDateTime.now());

        SleeperWorkflowTransaction saved = repository.save(tx);

        return mapToResponse(saved);
    }

    private String getRoleName(Integer roleId) {

        return roleMasterRepository.findById(roleId)
                .map(RoleMaster::getRoleName)
                .orElse(null);
    }

    private SleeperWorkflowTransactionDto mapToResponse(SleeperWorkflowTransaction tx) {

        SleeperWorkflowTransactionDto dto = new SleeperWorkflowTransactionDto();

        dto.setWorkflowTransitionId(Long.valueOf(tx.getWorkflowTransitionId()));
        dto.setWorkflowId(tx.getWorkflowId());
        dto.setModuleId(tx.getModuleId());
        dto.setRequestId(tx.getRequestId());
        dto.setAction(tx.getAction());
        dto.setStatus(tx.getStatus());
        dto.setRemarks(tx.getRemarks());
        dto.setJobStatus(tx.getJobStatus());

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
        System.out.println(tx.getPoiCode());
        // Fetch users who can access this POI

        List<SleeperPoiIeMapping> mappings = null;
        String vendorId = null;

        List<Integer> userIds = new ArrayList<>();
        if (tx.getWorkflowId().equals(2L)) {
            // Only Main IE for workflow 2
            // mappings = poiIeMappingRepository.findByPoiCodeAndIeType(tx.getPoiCode(),
            // "Main IE");

            mappings = poiIeMappingRepository
                    .findByPoiCodeAndPlantIdAndIeType(
                            tx.getPoiCode(),
                            tx.getPlantId(),
                            "Main IE");
        } else {
            if ("Vendor".equalsIgnoreCase(tx.getNextRole())) {
                vendorId = sleeperPincodePoIMappingRepository
                        .findVendorCodeByPoiCode(tx.getPoiCode())
                        .orElseThrow(() -> new RuntimeException("Vendor not found for POI "));
            } else {
                // Existing logic
                // mappings = poiIeMappingRepository.findByPoiCode(tx.getPoiCode());

                mappings = poiIeMappingRepository
                        .findByPoiCodeAndPlantId(
                                tx.getPoiCode(),
                                tx.getPlantId());
            }
        }
        if (mappings != null && !mappings.isEmpty()) {
            userIds = mappings.stream()
                    .map(SleeperPoiIeMapping::getIeUserId)
                    .toList();
            
            // If the call is pending for Main IE, the assigned user is the mapped IE
            if ("Main IE".equalsIgnoreCase(tx.getNextRole()) && !userIds.isEmpty()) {
                dto.setAssignedToUser(Long.valueOf(userIds.get(0)));
            }
        }
        if (vendorId != null) {
            dto.setAssignedToUser(Long.valueOf(vendorId));
        }

        dto.setAccessibleUserIds(userIds);

        // 1. Enrich PO & Inspection Call details for Sleeper Calls
        if (tx.getRequestId() != null) {
            sleeperInspectionCallRepository.findByCallNo(tx.getRequestId()).ifPresent(call -> {
                dto.setPoNo(call.getPoNo());
                dto.setPoSr(call.getSrNo());
                dto.setDesiredInspectionDate(call.getDesiredInspectionDate());
                dto.setStageOfInspection("Final");
                dto.setProductType("Sleeper");

                // PO Header lookup
                String rlyShort = null;
                String rawPoNo = call.getPoNo();
                if (call.getPoNo() != null) {
                    var poHeaderOpt = poHeaderRepository.findByPoNo(call.getPoNo());
                    if (poHeaderOpt.isPresent()) {
                        PoHeader poHeader = poHeaderOpt.get();
                        rlyShort = poHeader.getRlyShortName();
                        dto.setRlyShortName(rlyShort);
                        if (dto.getVendorName() == null) {
                            dto.setVendorName(poHeader.getVendorDetails());
                        }
                    }
                }

                // Format RLY / PO / SR NO (e.g. NCR / 08241015101234 / 001)
                StringBuilder rlyPoSrBuilder = new StringBuilder();
                if (rlyShort != null && !rlyShort.trim().isEmpty()) {
                    rlyPoSrBuilder.append(rlyShort.trim());
                }
                if (call.getPoNo() != null && !call.getPoNo().trim().isEmpty()) {
                    if (rlyPoSrBuilder.length() > 0) rlyPoSrBuilder.append(" / ");
                    rlyPoSrBuilder.append(call.getPoNo().trim());
                }
                if (call.getSrNo() != null && !call.getSrNo().trim().isEmpty()) {
                    if (rlyPoSrBuilder.length() > 0) rlyPoSrBuilder.append(" / ");
                    rlyPoSrBuilder.append(call.getSrNo().trim());
                }
                dto.setRlyPoSrNo(rlyPoSrBuilder.length() > 0 ? rlyPoSrBuilder.toString() : (rawPoNo != null ? rawPoNo : "-"));

                // PO Item lookup for DP Date and Ext DP Date
                if (call.getPoNo() != null && call.getSrNo() != null) {
                    var poItemOpt = poItemRepository.findByPoHeader_PoNoAndItemSrNo(call.getPoNo(), call.getSrNo());
                    if (poItemOpt.isPresent()) {
                        PoItem item = poItemOpt.get();
                        if (item.getDeliveryDate() != null) {
                            dto.setDpDate(item.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                        if (item.getExtendedDeliveryDate() != null) {
                            dto.setExtDpDate(item.getExtendedDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                    }
                }
            });
        }

        // 2. Vendor Name & Place of Inspection from vendor_plant
        if (tx.getPlantId() != null && !tx.getPlantId().trim().isEmpty()) {
            List<com.sarthi.Sleeper.entity.VendorPlant> vpList = vendorPlantRepository.findMatchingPlants(tx.getPlantId());
            if (vpList != null && !vpList.isEmpty()) {
                com.sarthi.Sleeper.entity.VendorPlant vp = vpList.get(0);
                if (dto.getVendorName() == null && vp.getCompanyName() != null) {
                    dto.setVendorName(vp.getCompanyName());
                }
                if (vp.getPlantName() != null) {
                    dto.setPlaceOfInspection(vp.getPlantName() + (tx.getPoiCode() != null ? " (" + tx.getPoiCode() + ")" : ""));
                }
            }
        }
        if (dto.getPlaceOfInspection() == null && tx.getPoiCode() != null) {
            dto.setPlaceOfInspection(tx.getPoiCode());
        }

        // 3. Assigned IE Name
        if (dto.getAssignedToUser() != null) {
            userMasterRepository.findById(dto.getAssignedToUser().intValue()).ifPresent(user -> {
                dto.setAssignedToUserName(user.getFullName());
                dto.setIeName(user.getFullName());
                dto.setAssignedToUserEmployeeCode(user.getEmployeeCode());
            });
        } else {
            // If call is not yet assigned (e.g. at RIO Help Desk), fetch mapped Main IE for that plant
            List<SleeperPoiIeMapping> ieMaps = null;
            if (tx.getPoiCode() != null && tx.getPlantId() != null) {
                ieMaps = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(tx.getPoiCode(), tx.getPlantId(), "Main IE");
                if (ieMaps == null || ieMaps.isEmpty()) {
                    ieMaps = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(tx.getPoiCode(), tx.getPlantId(), "MAIN_IE");
                }
            }
            if ((ieMaps == null || ieMaps.isEmpty()) && tx.getPlantId() != null) {
                ieMaps = poiIeMappingRepository.findByPlantIdAndIeType(tx.getPlantId(), "Main IE");
                if (ieMaps == null || ieMaps.isEmpty()) {
                    ieMaps = poiIeMappingRepository.findByPlantIdAndIeType(tx.getPlantId(), "MAIN_IE");
                }
            }
            if (ieMaps != null && !ieMaps.isEmpty()) {
                Integer mappedIeId = ieMaps.get(0).getIeUserId();
                userMasterRepository.findById(mappedIeId).ifPresent(user -> {
                    dto.setAssignedToUserName(user.getFullName());
                    dto.setIeName(user.getFullName());
                    dto.setAssignedToUserEmployeeCode(user.getEmployeeCode());
                });
            }
        }

        return dto;
    }

    /*
     * @Override
     * public SleeperWorkflowTransactionDto performTransitionAction(
     * SleeperTransitionActionReqDto req) {
     * 
     * SleeperWorkflowTransaction current = repository
     * .findById(req.getWorkflowTransitionId())
     * .orElseThrow(() -> new BusinessException(
     * new ErrorDetails(
     * AppConstant.ERROR_CODE_RESOURCE,
     * AppConstant.ERROR_TYPE_CODE_RESOURCE,
     * AppConstant.ERROR_TYPE_VALIDATION,
     * "Workflow transition not found"
     * )
     * ));
     * 
     * // Validate next role
     * // validateNextRole(req.getActionBy(), current.getNextRole());
     * 
     * validateUserForPoi(current.getPoiCode(), req.getActionBy());
     * String status = determineStatus(req.getAction());
     * 
     * SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();
     * 
     * tx.setRequestId(req.getRequestId());
     * tx.setModuleId(req.getModuleId());
     * tx.setWorkflowId(current.getWorkflowId());
     * 
     * tx.setAction(req.getAction());
     * tx.setStatus(status);
     * tx.setRemarks(req.getRemarks());
     * 
     * tx.setShift(current.getShift());
     * 
     * tx.setPoiCode(current.getPoiCode());
     * 
     * if(req.getAction().equals("REQUEST_BACK")) {
     * tx.setCurrentRole("IE");
     * tx.setNextRole("Vendor");
     * }
     * else if(req.getAction().equals("RESUBMIT")){
     * tx.setCurrentRole("Vendor");
     * tx.setNextRole("IE");
     * }
     * else{
     * tx.setCurrentRole("IE");
     * }
     * tx.setAssignedToUser(req.getActionBy());
     * 
     * tx.setCreatedBy(current.getCreatedBy());
     * tx.setModifiedBy(req.getActionBy());
     * tx.setCreatedDate(LocalDateTime.now());
     * 
     * SleeperWorkflowTransaction saved = repository.save(tx);
     * 
     * return mapToResponse(saved);
     * }
     * 
     */
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
                                "Workflow transition not found")));

        if ("UNLOCK".equalsIgnoreCase(req.getAction())) {
            repository.deleteById(req.getWorkflowTransitionId());

            List<SleeperWorkflowTransaction> existingTxs = repository.findByRequestIdOrderByCreatedDateAsc(req.getRequestId());
            boolean hasRemaining = existingTxs.stream()
                    .anyMatch(t -> t.getModuleId() != null && t.getModuleId().equals(req.getModuleId()) 
                                   && !t.getWorkflowTransitionId().equals(current.getWorkflowTransitionId()));

            if (!hasRemaining) {
                SleeperWorkflowTransaction newTx = new SleeperWorkflowTransaction();
                newTx.setRequestId(req.getRequestId());
                newTx.setModuleId(req.getModuleId());
                newTx.setWorkflowId(current.getWorkflowId() != null ? current.getWorkflowId() : 1L);
                newTx.setVendorCode(current.getVendorCode());
                newTx.setPlantId(current.getPlantId());
                newTx.setPoiCode(current.getPoiCode());
                newTx.setCurrentRole("Vendor");
                newTx.setNextRole("IE");
                newTx.setAction(AppConstant.CREATED_TYPE);
                newTx.setStatus(AppConstant.CREATED_TYPE);
                newTx.setCreatedBy(current.getCreatedBy());
                newTx.setCreatedDate(LocalDateTime.now());
                repository.save(newTx);
            }

            return mapToResponse(current);
        }

        if (current.getWorkflowId() == 1 && "IE".equalsIgnoreCase(current.getNextRole())) {
            // validateUserForPoi(current.getPoiCode(), req.getActionBy());
            validateUserForPoi(current.getPoiCode(), current.getPlantId(), req.getActionBy());
        } else if (current.getWorkflowId() == 2
                && "RIO Help Desk".equalsIgnoreCase(current.getNextRole())) {

            // Get employee code from user_master
            String employeeCode = userMasterRepository
                    .findEmployeeCodeByUserId(Math.toIntExact(req.getActionBy()));

            if (employeeCode == null) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Employee code not found for user"));
            }

            // Validate RIO mapping
            boolean exists = rioUserRepository.existsByRioAndEmployeeCode(current.getRio(), employeeCode);

            if (!exists) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "User is not mapped to this RIO"));
            }
        } else if (current.getWorkflowId() == 2
                && "Main IE".equalsIgnoreCase(current.getNextRole())) {

            /*
             * boolean exists = poiIeMappingRepository
             * .existsByPoiCodeAndIeUserIdAndIeType(
             * current.getPoiCode(),
             * Math.toIntExact(req.getActionBy()),
             * "Main IE");
             */

            boolean exists = poiIeMappingRepository
                    .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(
                            current.getPoiCode(),
                            current.getPlantId(),
                            Math.toIntExact(req.getActionBy()),
                            "Main IE");

            if (!exists) {
                exists = poiIeMappingRepository.existsByPoiCodeAndPlantIdAndIeUserId(current.getPoiCode(), current.getPlantId(), Math.toIntExact(req.getActionBy()))
                        || poiIeMappingRepository.findByPlantId(current.getPlantId()).stream().anyMatch(m -> m.getIeUserId().equals(Math.toIntExact(req.getActionBy())))
                        || (current.getAssignedToUser() != null && current.getAssignedToUser().equals(req.getActionBy()));
            }

            if (!exists) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "User is not mapped as Main IE for this POI / Plant"));
            }
        }
        String status = null;

        if (current.getWorkflowId() == 1) {
            status = determineStatus(req.getAction());
        }

        SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

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
        tx.setRio(current.getRio());

        // Workflow 2 → Special actions handling
        if (req.getAction().equalsIgnoreCase("IC_ISSUE")) {
            tx.setCurrentRole(current.getNextRole() != null ? current.getNextRole() : current.getCurrentRole());
            tx.setNextRole(current.getNextRole() != null ? current.getNextRole() : "Main IE");
            tx.setStatus(AppConstant.PENDING_TYPE);
            tx.setJobStatus("IC_ISSUE");
        } else if (req.getAction().equalsIgnoreCase("IC_GENERATION")
                || req.getAction().equalsIgnoreCase("GENERATE_IC")
                || req.getAction().equalsIgnoreCase("DSC_SIGN_IC")) {
            
            tx.setCurrentRole(current.getNextRole() != null ? current.getNextRole() : current.getCurrentRole());
            tx.setNextRole(null); // Keep it in a terminal state
            tx.setStatus(AppConstant.COMPLETED_TYPE);
            tx.setJobStatus("GENERATED");
            
        } else if (current.getWorkflowId().equals(2L)) {

            List<SleeperTransitionMaster> transitions =
                    sleeperTransitionMasterRepository
                            .findByWorkflowIdAndCurrentRoleIdAndCurrentAction(
                                    current.getWorkflowId().intValue(),
                                    getRoleId(current.getNextRole()),
                                    req.getAction()
                            );

            SleeperTransitionMaster transition = null;

            if (transitions.size() == 1) {

                transition = transitions.get(0);

            } else {

                List<SleeperTransitionMaster> trans = null;

                if (req.getAction().equalsIgnoreCase("PO_VERIFICATION")
                        || req.getAction().equalsIgnoreCase("MAIN_IE_SCHEDULE_CALL")
                        || req.getAction().equalsIgnoreCase("PAUSE")
                        || req.getAction().equalsIgnoreCase("FINISH")
                        || req.getAction().equalsIgnoreCase("RESUME")
                        || req.getAction().equalsIgnoreCase("WITHHELD")
                        || req.getAction().equalsIgnoreCase("RESCHEDULE_CALL")
                        || req.getAction().equalsIgnoreCase("IC_ISSUE")
                        || req.getAction().equalsIgnoreCase("IC_GENERATION")) {

                    trans =
                            sleeperTransitionMasterRepository
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
                            .orElse(null);

                    if (transition == null) {
                        List<SleeperTransitionMaster> allWfTransitions = sleeperTransitionMasterRepository.findAll().stream()
                                 .filter(t -> t.getWorkflowId() != null && t.getWorkflowId().equals(current.getWorkflowId().intValue()))
                                .toList();
                        Integer nextRoleId = getRoleId(current.getNextRole());
                        Integer currentRoleId = getRoleId(current.getCurrentRole());
                        transition = allWfTransitions.stream()
                                .filter(t -> (t.getCurrentRoleId() != null && (t.getCurrentRoleId().equals(nextRoleId) || t.getCurrentRoleId().equals(currentRoleId)))
                                        && (req.getAction().equalsIgnoreCase(t.getNextAction()) || req.getAction().equalsIgnoreCase(t.getCurrentAction())))
                                .findFirst()
                                .orElse(null);
                    }

                    if (transition == null && !req.getAction().equalsIgnoreCase("IC_ISSUE") && !req.getAction().equalsIgnoreCase("IC_GENERATION")) {
                        throw new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Transition not configured for action: " + req.getAction()
                                ));
                    }

                    tx.setCurrentRole(current.getNextRole());
                }
            }

            tx.setCurrentRole(current.getNextRole());
            if (current.getWorkflowId() == 2) {
                tx.setJobStatus(determineJobStatus(req.getAction()));
            }

            if (transition == null) {
                if (req.getAction().equalsIgnoreCase("IC_ISSUE") || req.getAction().equalsIgnoreCase("IC_GENERATION")) {
                    tx.setNextRole(current.getNextRole() != null ? current.getNextRole() : "Main IE");
                    tx.setStatus(req.getAction().equalsIgnoreCase("IC_GENERATION") ? AppConstant.COMPLETED_TYPE : AppConstant.PENDING_TYPE);
                } else {
                    throw new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "No valid transition found for action: " + req.getAction()
                            ));
                }
            } else {
                if (transition.getNextRoleId() != null) {
                    tx.setNextRole(getRoleName(transition.getNextRoleId()));
                }
                if (transition.getNextRoleId() == null) {
                    tx.setStatus(AppConstant.COMPLETED_TYPE);
                } else {
                    tx.setStatus(AppConstant.PENDING_TYPE);
                }
            }
            if (tx.getNextRole() != null && "RIO Help Desk".equalsIgnoreCase(tx.getNextRole())) {
                if (tx.getRio() == null && tx.getPlantId() != null) {
                    List<com.sarthi.Sleeper.entity.VendorPlant> vpList = vendorPlantRepository.findMatchingPlants(tx.getPlantId());
                    if (vpList != null && !vpList.isEmpty()) {
                        for (com.sarthi.Sleeper.entity.VendorPlant vp : vpList) {
                            if (vp.getRio() != null && !vp.getRio().trim().isEmpty()) {
                                tx.setRio(vp.getRio().trim());
                                break;
                            }
                        }
                    }
                }
                if (tx.getRio() == null && current.getPoiCode() != null) {
                    try {
                        SleeperPincodePoIMapping poi = sleeperPincodePoIMappingRepository.findByPoiCode(current.getPoiCode()).orElse(null);
                        if (poi != null && poi.getPinCode() != null) {
                            String stage = "F";
                            String product = "Sleeper";
                            IEFieldsMapping map = ieFieldsMappingRepository
                                    .findByPinCodeProductAndStageMatch(poi.getPinCode(), product, stage).orElse(null);
                            if (map != null && map.getRio() != null) {
                                tx.setRio(map.getRio().trim());
                            }
                        }
                    } catch (Exception e) {
                        // ignore fallback error
                    }
                }
            }
        } else {
            // Existing workflow logic (workflowId = 1)

            if (req.getAction().equals("REQUEST_BACK")) {
                tx.setCurrentRole("IE");
                tx.setNextRole("Vendor");
            } else if (req.getAction().equals("RESUBMIT")) {
                tx.setCurrentRole("Vendor");
                tx.setNextRole("IE");
            } else {
                tx.setCurrentRole("IE");
            }
        }

        // Step 3: Pick the mapped Main IE user from sleeper_poi_ie_mapping when next role is Main IE
        if ("Main IE".equalsIgnoreCase(tx.getNextRole())) {
            List<SleeperPoiIeMapping> ieMappings = null;
            if (tx.getPoiCode() != null && tx.getPlantId() != null) {
                ieMappings = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(tx.getPoiCode(), tx.getPlantId(), "Main IE");
                if (ieMappings == null || ieMappings.isEmpty()) {
                    ieMappings = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(tx.getPoiCode(), tx.getPlantId(), "MAIN_IE");
                }
            }
            if ((ieMappings == null || ieMappings.isEmpty()) && tx.getPlantId() != null) {
                ieMappings = poiIeMappingRepository.findByPlantIdAndIeType(tx.getPlantId(), "Main IE");
                if (ieMappings == null || ieMappings.isEmpty()) {
                    ieMappings = poiIeMappingRepository.findByPlantIdAndIeType(tx.getPlantId(), "MAIN_IE");
                }
            }
            if ((ieMappings == null || ieMappings.isEmpty()) && tx.getPoiCode() != null) {
                ieMappings = poiIeMappingRepository.findByPoiCodeAndIeType(tx.getPoiCode(), "Main IE");
                if (ieMappings == null || ieMappings.isEmpty()) {
                    ieMappings = poiIeMappingRepository.findByPoiCodeAndIeType(tx.getPoiCode(), "MAIN_IE");
                }
            }

            if (ieMappings != null && !ieMappings.isEmpty()) {
                Integer mappedIeUserId = ieMappings.get(0).getIeUserId();
                tx.setAssignedToUser(Long.valueOf(mappedIeUserId));
            } else if (current.getAssignedToUser() != null) {
                tx.setAssignedToUser(current.getAssignedToUser());
            } else {
                tx.setAssignedToUser(req.getActionBy());
            }
        } else if ("Vendor".equalsIgnoreCase(tx.getNextRole())) {
            tx.setAssignedToUser(current.getCreatedBy());
        } else {
            tx.setAssignedToUser(req.getActionBy());
        }

        tx.setCreatedBy(current.getCreatedBy());
        tx.setModifiedBy(req.getActionBy());
        tx.setCreatedDate(LocalDateTime.now());

        SleeperWorkflowTransaction saved = repository.save(tx);

        // --- Save to sleeper_inspection_complete_details when Sleeper inspection is FINISHED, IC ISSUED, or IC GENERATED ---
        if ("COMPLETED".equalsIgnoreCase(tx.getStatus())
                || "IC_GENERATION".equalsIgnoreCase(req.getAction())
                || "FINISH".equalsIgnoreCase(req.getAction())
                || "IC_ISSUE".equalsIgnoreCase(req.getAction())) {
            Optional<SleeperInspectionCall> callOpt = sleeperInspectionCallRepository.findByCallNo(tx.getRequestId());
            if (callOpt.isPresent()) {
                Optional<SleeperInspectionCompleteDetails> existingOpt = sleeperInspectionCompleteDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(tx.getRequestId());
                if (existingOpt.isEmpty()) {
                    SleeperInspectionCall ic = callOpt.get();
                    UserMaster user = null;
                    if (req.getActionBy() != null) {
                        user = userMasterRepository.findById(Math.toIntExact(req.getActionBy())).orElse(null);
                    }
                    if (user == null && tx.getAssignedToUser() != null) {
                        user = userMasterRepository.findById(Math.toIntExact(tx.getAssignedToUser())).orElse(null);
                    }
                    if (user == null && current.getAssignedToUser() != null) {
                        user = userMasterRepository.findById(Math.toIntExact(current.getAssignedToUser())).orElse(null);
                    }
                    String userShortName = (user != null && user.getShortName() != null && !user.getShortName().trim().isEmpty())
                            ? user.getShortName().trim().toUpperCase()
                            : "NV";

                    String rio = tx.getRio();
                    if (rio == null || rio.trim().isEmpty()) {
                        rio = current.getRio();
                    }
                    if (rio == null || rio.trim().isEmpty()) {
                        rio = "C";
                    }

                    SleeperInspectionCompleteDetails details = new SleeperInspectionCompleteDetails();
                    details.setCallNo(ic.getCallNo());
                    details.setPoNo(ic.getPoNo());
                    details.setCertificateNo(generateCertificateNo(rio, ic.getCallNo(), userShortName));
                    details.setCreatedOn(LocalDateTime.now());

                    sleeperInspectionCompleteDetailsRepository.save(details);

                    if (req.getBookNo() != null || req.getSetNo() != null) {
                        try {
                            SleeperFinalIcEdit edit = sleeperFinalIcEditRepository.findByIcNumber(ic.getCallNo())
                                    .orElse(new SleeperFinalIcEdit());
                            edit.setIcNumber(ic.getCallNo());
                            edit.setBookNo(req.getBookNo());
                            edit.setSetNo(req.getSetNo());
                            sleeperFinalIcEditRepository.save(edit);
                        } catch (Exception e) {
                            log.error("Error saving SleeperFinalIcEdit: ", e);
                        }
                    }
                }
            }
        }

        // Send notification after RIO Help Desk verifies the call
        if (current.getWorkflowId() == 2
                && "RIO Help Desk".equalsIgnoreCase(current.getNextRole())
                && "VERIFY_CALL".equalsIgnoreCase(req.getAction())) {

            notificationService.sendSleeperCallRegisteredNotification(
                    req.getRequestId(),
                    current.getPlantId(),
                  "CALL_REGISTERED"
            );
        }
        return mapToResponse(saved);
    }

    private String generateCertificateNo(String rioName, String callNo, String userShortName) {
        String rioFirstLetter = (rioName != null && !rioName.trim().isEmpty())
                ? rioName.trim().substring(0, 1).toUpperCase()
                : "C";
        String userSuffix = (userShortName != null && !userShortName.trim().isEmpty())
                ? userShortName.trim().toUpperCase()
                : "NV";
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

            case "REJECT":
                return "REJECTED";

            case "INITIATE_CALL":
                return "INITIATED";

            case "PO_VERIFICATION":
                return "PO_VERIFICATION";

            case "FINISH":
            case "COMPLETED":
                return "COMPLETED";

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

    private Integer getRoleId(String roleName) {

        return roleMasterRepository.findByRoleName(roleName)
                .map(RoleMaster::getRoleId)
                .orElse(null);
    }

    private String determineStatus(String action) {

        switch (action) {

            case "CREATED":
                return AppConstant.CREATED_TYPE;

            case "VERIFY":
                return AppConstant.COMPLETED_TYPE;

            case "REQUEST_BACK":
            case "RESUBMIT":
                return AppConstant.PENDING_TYPE;

            default:
                return AppConstant.PENDING_TYPE;
        }
    }

    private void validateWorkflowAndModule(Long workflowId, Long moduleId) {

        boolean workflowExists = workflowRepository.existsById(workflowId);

        if (!workflowExists) {
            throw new RuntimeException("Workflow not found: " + workflowId);
        }

        boolean moduleValid = moduleRepository.existsByIdAndWorkflowId(moduleId, workflowId);

        if (!moduleValid) {
            throw new RuntimeException(
                    "Module does not belong to workflow");
        }
    }

    private void validateNextRole(Long actionBy, String expectedRole) {

        String userRole = userMasterRepository.findRoleNameByUserId(Math.toIntExact(actionBy));

        if (userRole == null || !userRole.equalsIgnoreCase(expectedRole)) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "User is not allowed to perform this action. Expected role: " + expectedRole));
        }
    }

    /*
     * private void validateUserForPoi(String poiCode, Long actionBy) {
     * 
     * boolean exists = poiIeMappingRepository
     * .existsByPoiCodeAndIeUserId(poiCode, Math.toIntExact(actionBy));
     * 
     * if (!exists) {
     * throw new BusinessException(
     * new ErrorDetails(
     * AppConstant.ERROR_CODE_RESOURCE,
     * AppConstant.ERROR_TYPE_CODE_VALIDATION,
     * AppConstant.ERROR_TYPE_VALIDATION,
     * "User is not mapped to this POI"
     * )
     * );
     * }
     * }
     */

    private void validateUserForPoi(String poiCode, String plantId, Long actionBy) {

        boolean exists = poiIeMappingRepository
                .existsByPoiCodeAndPlantIdAndIeUserId(
                        poiCode,
                        plantId,
                        Math.toIntExact(actionBy));

        if (!exists) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "User is not mapped to this POI + Plant"));
        }
    }

    @Override
    public List<SleeperWorkflowTransactionDto> allPendingWorkflowTransitions(String roleName) {
        return allPendingWorkflowTransitions(roleName, null, null, null);
    }

    @Override
    public List<SleeperWorkflowTransactionDto> allPendingWorkflowTransitions(
            String roleName, Long assignedTo, String rio, String plantId) {

        List<SleeperWorkflowTransaction> list = null;
        if (roleName.equalsIgnoreCase("Main IE")) {
            if (assignedTo != null) {
                list = repository.findLatestByRoleAndAssignedTo(roleName, assignedTo, plantId);
            } else {
                list = repository.findLatestByRole(roleName);
            }
        } else if (roleName.equalsIgnoreCase("RIO Help Desk")) {
            if (rio != null && !rio.trim().isEmpty()) {
                list = repository.findLatestByRoleAndRio(roleName, rio.trim(), plantId);
            } else {
                list = repository.findLastPendingRequestsByRole(roleName);
            }
        } else {
            list = repository.findLastPendingRequestsByRole(roleName);
        }
        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<SleeperWorkflowTransactionDto> allPendingWorkflowTransitionsBasedOnModule(
            String roleName,
            int moduleId,
            String plantId,
            Pageable pageable) {

        String cleanPlantId = plantId != null ? plantId.replaceAll("^[:\\s]+", "").trim() : null;
        if (cleanPlantId != null && cleanPlantId.isEmpty()) {
            cleanPlantId = null;
        }

        if (moduleId == 11) {
            try {
                List<ProductionDeclaration> allDeclarations = productionDeclarationRepository.findAll();
                List<SleeperWorkflowTransaction> existingTxs = repository.findByModuleId(11L);
                java.util.Map<String, SleeperWorkflowTransaction> existingMap = existingTxs.stream()
                        .filter(t -> t.getRequestId() != null)
                        .collect(java.util.stream.Collectors.toMap(
                                SleeperWorkflowTransaction::getRequestId,
                                t -> t,
                                (t1, t2) -> t1.getWorkflowTransitionId() > t2.getWorkflowTransitionId() ? t1 : t2
                        ));

                List<SleeperWorkflowTransaction> toSave = new java.util.ArrayList<>();

                for (ProductionDeclaration pd : allDeclarations) {
                    if (pd.getId() == null) continue;
                    String reqId = String.valueOf(pd.getId());
                    SleeperWorkflowTransaction existingTx = existingMap.get(reqId);

                    String targetPlantId = pd.getPlantId();
                    if (targetPlantId == null || targetPlantId.trim().isEmpty()) {
                        targetPlantId = cleanPlantId;
                    }

                    if (existingTx == null) {
                        SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();
                        tx.setRequestId(reqId);
                        tx.setModuleId(11L);
                        tx.setWorkflowId(1L);
                        tx.setCurrentRole("Vendor");
                        tx.setNextRole("IE");
                        tx.setAction(AppConstant.CREATED_TYPE);
                        tx.setStatus(AppConstant.CREATED_TYPE);
                        tx.setCreatedBy(pd.getCreatedBy());
                        tx.setVendorCode(pd.getVendorCode());
                        tx.setPlantId(targetPlantId);
                        tx.setCreatedDate(pd.getCreatedDate() != null ? pd.getCreatedDate() : LocalDateTime.now());
                        toSave.add(tx);
                    } else {
                        boolean modified = false;
                        if (existingTx.getPlantId() == null || existingTx.getPlantId().trim().isEmpty()) {
                            existingTx.setPlantId(targetPlantId);
                            modified = true;
                        }
                        if (existingTx.getNextRole() == null || existingTx.getNextRole().trim().isEmpty()) {
                            existingTx.setNextRole("IE");
                            modified = true;
                        }
                        if (modified) {
                            toSave.add(existingTx);
                        }
                    }
                }
                if (!toSave.isEmpty()) {
                    repository.saveAll(toSave);
                }
            } catch (Exception e) {
                System.err.println("Error auto-syncing production declaration workflow transitions: " + e.getMessage());
            }
        }

        Page<SleeperWorkflowTransaction> page = repository.findLastPendingRequestsByRole(roleName, moduleId, cleanPlantId, pageable);

        return page.map(this::mapToModuleWisePendingResponse);
    }

    private SleeperWorkflowTransactionDto mapToModuleWisePendingResponse(SleeperWorkflowTransaction tx) {

        SleeperWorkflowTransactionDto dto = new SleeperWorkflowTransactionDto();

        dto.setWorkflowTransitionId(Long.valueOf(tx.getWorkflowTransitionId()));
        dto.setWorkflowId(tx.getWorkflowId());
        dto.setModuleId(tx.getModuleId());
        dto.setRequestId(tx.getRequestId());
        dto.setAction(tx.getAction());
        dto.setStatus(tx.getStatus());
        dto.setRemarks(tx.getRemarks());
        dto.setJobStatus(tx.getJobStatus());

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
        System.out.println(tx.getPoiCode());
        // Fetch users who can access this POI

        List<SleeperPoiIeMapping> mappings = null;
        String vendorId = null;
        if (tx.getModuleId() != null && tx.getModuleId() == 11) {

            productionDeclarationRepository
                    .findProductionDetailsByRequestId(Long.valueOf(tx.getRequestId()))
                    .ifPresent(p -> {
                        dto.setProductionUnit(p.getProductionUnit());
                        dto.setBatchNumber(p.getBatchNumber());
                        dto.setCastingDate(p.getCastingDate());
                        dto.setTotalCastedSleepers(p.getTotalCastedSleepers());
                    });
        }

        if (tx.getModuleId() != null && tx.getModuleId() == 4) {

            mixDesignRepository.findById(Long.valueOf(tx.getRequestId()))
                    .ifPresent(mix -> {

                        dto.setMixId(mix.getIdentification());
                        dto.setConcreteGrade(mix.getConcreteGrade());
                        dto.setAuthorityOfApproval(mix.getAuthorityOfApproval());

                    });
        }



        if (tx.getModuleId() != null && tx.getModuleId() == 1) {

            plantProfileRepository.findById(Long.valueOf(tx.getRequestId()))
                    .ifPresent(profile -> {

                        dto.setPlantName(profile.getPlantNameLocation());
                        dto.setVendorCode(profile.getVendorCode());
                        dto.setPlantType(profile.getPlantType());
                        dto.setNumberOfSheds(profile.getNumberOfSheds());

                    });
        }

        if (tx.getModuleId() != null && tx.getModuleId() == 3) {

            rawMaterialSourceRepository
                    .findById(Long.valueOf(tx.getRequestId()))
                    .ifPresent(raw -> {

                        dto.setRawMaterialType(raw.getRawMaterialType());
                        dto.setSupplierName(raw.getSupplierName());
                        dto.setApprovalReference(raw.getApprovalReference());
                        dto.setValidFrom(raw.getValidFrom());
                        dto.setValidTo(raw.getValidTo());

                    });
        }






        List<Integer> userIds = new ArrayList<>();
        if (tx.getWorkflowId().equals(2L)) {
            // Only Main IE for workflow 2
            // mappings = poiIeMappingRepository.findByPoiCodeAndIeType(tx.getPoiCode(),
            // "Main IE");

            mappings = poiIeMappingRepository
                    .findByPoiCodeAndPlantIdAndIeType(
                            tx.getPoiCode(),
                            tx.getPlantId(),
                            "Main IE");
        } else {
            if ("Vendor".equalsIgnoreCase(tx.getNextRole())) {
                vendorId = sleeperPincodePoIMappingRepository
                        .findVendorCodeByPoiCode(tx.getPoiCode())
                        .orElseThrow(() -> new RuntimeException("Vendor not found for POI "));
            } else {
                // Existing logic
                // mappings = poiIeMappingRepository.findByPoiCode(tx.getPoiCode());

                mappings = poiIeMappingRepository
                        .findByPoiCodeAndPlantId(
                                tx.getPoiCode(),
                                tx.getPlantId());
            }
        }
        if (mappings != null) {
            userIds = mappings.stream()
                    .map(SleeperPoiIeMapping::getIeUserId)
                    .toList();
            if ("Main IE".equalsIgnoreCase(tx.getNextRole()) && !userIds.isEmpty() && dto.getAssignedToUser() == null) {
                dto.setAssignedToUser(Long.valueOf(userIds.get(0)));
            }
        }
        if (vendorId != null) {
            dto.setAssignedToUser(Long.valueOf(vendorId));
        }

        dto.setAccessibleUserIds(userIds);

        if (dto.getAssignedToUser() != null) {
            userMasterRepository.findById(dto.getAssignedToUser().intValue()).ifPresent(user -> {
                dto.setAssignedToUserName(user.getFullName());
                dto.setIeName(user.getFullName());
                dto.setAssignedToUserEmployeeCode(user.getEmployeeCode());
            });
        }

        return dto;
    }

    /*
     * @Override
     * public List<SleeperWorkflowTransactionDto> getCompletedRequests() {
     * 
     * List<SleeperWorkflowTransaction> list =
     * repository.findLastCompletedRequests();
     * 
     * return list.stream()
     * .map(this::mapToResponse)
     * .toList();
     * }
     */

    @Override
    public List<SleeperWorkflowTransactionDto> workflowTransitionHistory(String requestId) {
        List<SleeperWorkflowTransaction> list = repository.findByRequestIdOrderByCreatedDateAsc(requestId);

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SleeperWorkflowTransactionDto> allCompletedWorkflowTransitions() {

        List<SleeperWorkflowTransaction> list = repository.findCompletedRequests();

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SleeperWorkflowTransactionDto> getPendingVerifiedCalls() {
        List<String> pendingActions = java.util.Arrays.asList(
            "VERIFY",
            "MAIN_IE_SCHEDULE_CALL",
            "INITIATE_CALL",
            "PO_VERIFICATION",
            "PAUSE",
            "RESUME",
            "RESCHEDULE_CALL"
        );
        List<SleeperWorkflowTransaction> list = repository.findPendingVerifiedCalls(pendingActions);
        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<SleeperWorkflowTransactionDto> allCompletedWorkflowTransitions(
            Integer moduleId,
            String plantId,
            Pageable pageable) {

        Page<SleeperWorkflowTransaction> page = repository.findCompletedRequests(moduleId, plantId, pageable);

        return page.map(this::mapToModuleWisePendingResponse);
    }

    @Override
    public List<SleeperWorkflowTransactionDto> allFinalCompletedWorkflowTransitions() {

        List<SleeperWorkflowTransaction> list = repository.findFinalCompletedRequests();

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<java.util.Map<String, Object>> getSleeperRemapAvailableUsers() {
        List<Integer> mainIeUserIds = poiIeMappingRepository.findDistinctMainIeUserIds();
        List<java.util.Map<String, Object>> available = new ArrayList<>();
        if (mainIeUserIds != null && !mainIeUserIds.isEmpty()) {
            List<UserMaster> users = userMasterRepository.findAllById(mainIeUserIds);
            for (UserMaster u : users) {
                java.util.Map<String, Object> emp = new java.util.HashMap<>();
                emp.put("userId", u.getUserId());
                emp.put("employeeCode", u.getEmployeeCode());
                emp.put("fullName", u.getFullName());
                emp.put("name", u.getFullName());
                emp.put("id", u.getUserId());
                emp.put("role", "Main IE");
                emp.put("roleName", "Main IE");
                available.add(emp);
            }
        }
        return available;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void submitSleeperRemap(SleeperRemapSubmitDto dto) {
        String plantId = dto.getPlantId();
        Integer oldUserId = dto.getOldUserId();
        Integer newUserId = dto.getNewUserId();
        String callNo = dto.getCallNo();

        if (callNo == null || newUserId == null) {
            throw new RuntimeException("Call No and new user ID are required.");
        }

        List<SleeperWorkflowTransaction> txList = repository.findByRequestIdOrderByCreatedDateAsc(callNo);
        SleeperWorkflowTransaction latestTx = (txList != null && !txList.isEmpty()) ? txList.get(txList.size() - 1) : null;

        if (plantId == null && latestTx != null) {
            plantId = latestTx.getPlantId();
        }

        if (plantId != null && oldUserId != null) {
            poiIeMappingRepository.updateIeUserIdByPlantId(plantId, oldUserId, newUserId);
        }

        if (latestTx != null) {
            latestTx.setAssignedToUser(Long.valueOf(newUserId));
            repository.save(latestTx);
        }
    }
}
