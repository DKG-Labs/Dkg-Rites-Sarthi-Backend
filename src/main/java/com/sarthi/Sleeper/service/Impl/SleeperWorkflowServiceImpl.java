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

import com.sarthi.Sleeper.entity.FinalInspection.SleeperCallCancellationDetail;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperCallCancellationDetailRepository;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperVendorFinancialLiability;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperVendorFinancialLiabilityRepository;
import com.sarthi.Sleeper.dto.SleeperCancelledPaymentCallDto;
import java.math.BigDecimal;
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
    @Autowired
    private SleeperScheduleRepository sleeperScheduleRepository;
    @Autowired
    private SleeperCallCancellationDetailRepository sleeperCallCancellationDetailRepository;
    @Autowired
    private SleeperVendorFinancialLiabilityRepository sleeperVendorFinancialLiabilityRepository;

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
        return mapToResponse(tx, new java.util.HashMap<>());
    }

    @SuppressWarnings("unchecked")
    private SleeperWorkflowTransactionDto mapToResponse(SleeperWorkflowTransaction tx, java.util.Map<String, Object> cache) {
        if (cache == null) {
            cache = new java.util.HashMap<>();
        }

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

        // Fetch users who can access this POI
        List<SleeperPoiIeMapping> mappings = null;
        String vendorId = null;

        List<Integer> userIds = new ArrayList<>();
        String mappingCacheKey = "ieMap_" + (tx.getPoiCode() != null ? tx.getPoiCode() : "") + "_" + (tx.getPlantId() != null ? tx.getPlantId() : "") + "_" + tx.getWorkflowId();
        if (cache.containsKey(mappingCacheKey)) {
            mappings = (List<SleeperPoiIeMapping>) cache.get(mappingCacheKey);
        } else {
            if (tx.getWorkflowId().equals(2L)) {
                mappings = poiIeMappingRepository
                        .findByPoiCodeAndPlantIdAndIeType(
                                tx.getPoiCode(),
                                tx.getPlantId(),
                                "Main IE");
            } else {
                if ("Vendor".equalsIgnoreCase(tx.getNextRole())) {
                    vendorId = sleeperPincodePoIMappingRepository
                            .findVendorCodeByPoiCode(tx.getPoiCode())
                            .orElse(null);
                } else {
                    mappings = poiIeMappingRepository
                            .findByPoiCodeAndPlantId(
                                    tx.getPoiCode(),
                                    tx.getPlantId());
                }
            }
            cache.put(mappingCacheKey, mappings);
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
            String callKey = "call_" + tx.getRequestId();
            com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall call = null;
            if (cache.containsKey(callKey)) {
                call = (com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall) cache.get(callKey);
            } else {
                call = sleeperInspectionCallRepository.findByCallNo(tx.getRequestId()).orElse(null);
                cache.put(callKey, call);
            }

            if (call != null) {
                if ((dto.getPlantId() == null || dto.getPlantId().trim().isEmpty()) && call.getPlantId() != null) {
                    dto.setPlantId(call.getPlantId());
                }
                dto.setPoNo(call.getPoNo());
                dto.setPoSr(call.getSrNo());
                int off = call.getTotalOffered() != null ? call.getTotalOffered() : 0;
                int rej = call.getTotalRejected() != null ? call.getTotalRejected() : 0;
                int totalOffered = off + rej;
                dto.setOfferedQty(totalOffered > 0 ? totalOffered : (dto.getOfferedQty() != null ? dto.getOfferedQty() : 0));
                dto.setAcceptedQty(call.getTotalOffered() != null ? call.getTotalOffered() : (dto.getOfferedQty() != null ? dto.getOfferedQty() : 0));
                
                // Lookup certificate details from SLEEPER_INSPECTION_COMPLETE_DETAILS
                String certKey = "cert_" + tx.getRequestId();
                SleeperInspectionCompleteDetails certDetails = null;
                if (cache.containsKey(certKey)) {
                    certDetails = (SleeperInspectionCompleteDetails) cache.get(certKey);
                } else if (sleeperInspectionCompleteDetailsRepository != null) {
                    certDetails = sleeperInspectionCompleteDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(tx.getRequestId()).orElse(null);
                    cache.put(certKey, certDetails);
                }

                if (certDetails != null && certDetails.getCertificateNo() != null && !certDetails.getCertificateNo().trim().isEmpty()) {
                    dto.setIcNo(certDetails.getCertificateNo().trim());
                    if (certDetails.getCreatedOn() != null) {
                        dto.setIcDate(certDetails.getCreatedOn().toLocalDate());
                    } else {
                        dto.setIcDate(tx.getUpdatedDate() != null ? tx.getUpdatedDate().toLocalDate() : (tx.getCreatedDate() != null ? tx.getCreatedDate().toLocalDate() : null));
                    }
                } else {
                    dto.setIcNo("IC-" + tx.getRequestId());
                    dto.setIcDate(tx.getUpdatedDate() != null ? tx.getUpdatedDate().toLocalDate() : (tx.getCreatedDate() != null ? tx.getCreatedDate().toLocalDate() : null));
                }

                dto.setUom("Nos.");
                dto.setDesiredInspectionDate(call.getDesiredInspectionDate());
                dto.setCallDate(call.getCreatedAt() != null ? call.getCreatedAt() : tx.getCreatedDate());
                dto.setStageOfInspection("Final");
                dto.setProductType("Sleeper");
                if (call.getSleeperType() != null && !call.getSleeperType().trim().isEmpty()) {
                    dto.setSleeperType(call.getSleeperType().trim());
                }

                // PO Header lookup
                String rlyShort = null;
                String rawPoNo = call.getPoNo();
                if (call.getPoNo() != null) {
                    String poKey = "poHeader_" + call.getPoNo();
                    PoHeader poHeader = null;
                    if (cache.containsKey(poKey)) {
                        poHeader = (PoHeader) cache.get(poKey);
                    } else {
                        poHeader = poHeaderRepository.findByPoNo(call.getPoNo()).orElse(null);
                        cache.put(poKey, poHeader);
                    }

                    if (poHeader != null) {
                        rlyShort = poHeader.getRlyShortName();
                        dto.setRlyShortName(rlyShort);

                        // Resolve plant RIO for Sleeper Case No.
                        String plantRio = tx.getRio();
                        if (plantRio == null || plantRio.trim().isEmpty()) {
                            String pId = tx.getPlantId() != null ? tx.getPlantId() : call.getPlantId();
                            if (pId != null && !pId.trim().isEmpty()) {
                                String vpKey = "vpList_" + pId.trim();
                                List<com.sarthi.Sleeper.entity.VendorPlant> vpList = null;
                                if (cache.containsKey(vpKey)) {
                                    vpList = (List<com.sarthi.Sleeper.entity.VendorPlant>) cache.get(vpKey);
                                } else {
                                    vpList = vendorPlantRepository.findMatchingPlants(pId.trim());
                                    cache.put(vpKey, vpList);
                                }

                                if (vpList != null && !vpList.isEmpty()) {
                                    for (com.sarthi.Sleeper.entity.VendorPlant vp : vpList) {
                                        if (vp.getRio() != null && !vp.getRio().trim().isEmpty()) {
                                            plantRio = vp.getRio().trim();
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        dto.setCaseNo(resolveSleeperCaseNo(poHeader.getCaseNo(), plantRio));
                        if (dto.getRio() == null && plantRio != null) {
                            dto.setRio(plantRio);
                        }

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
                    String itemKey = "poItem_" + call.getPoNo() + "_" + call.getSrNo();
                    PoItem item = null;
                    if (cache.containsKey(itemKey)) {
                        item = (PoItem) cache.get(itemKey);
                    } else {
                        item = poItemRepository.findByPoHeader_PoNoAndItemSrNo(call.getPoNo(), call.getSrNo()).orElse(null);
                        cache.put(itemKey, item);
                    }

                    if (item != null) {
                        if (item.getDeliveryDate() != null) {
                            dto.setDpDate(item.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                        if (item.getExtendedDeliveryDate() != null) {
                            dto.setExtDpDate(item.getExtendedDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                    }
                }
            }

            // Schedule Date lookup
            try {
                String schedKey = "sched_" + tx.getRequestId();
                com.sarthi.Sleeper.entity.FInalCall.SleeperSchedule sched = null;
                if (cache.containsKey(schedKey)) {
                    sched = (com.sarthi.Sleeper.entity.FInalCall.SleeperSchedule) cache.get(schedKey);
                } else {
                    sched = sleeperScheduleRepository.findByCallNo(tx.getRequestId()).orElse(null);
                    cache.put(schedKey, sched);
                }
                if (sched != null) {
                    dto.setScheduleDate(sched.getScheduleDate());
                }
            } catch (Exception e) {
                log.warn("Error fetching sleeper schedule for call {}: {}", tx.getRequestId(), e.getMessage());
            }
        }

        // 2. Vendor Name & Place of Inspection from vendor_plant
        if (tx.getPlantId() != null && !tx.getPlantId().trim().isEmpty()) {
            String vpKey = "vpList_" + tx.getPlantId().trim();
            List<com.sarthi.Sleeper.entity.VendorPlant> vpList = null;
            if (cache.containsKey(vpKey)) {
                vpList = (List<com.sarthi.Sleeper.entity.VendorPlant>) cache.get(vpKey);
            } else {
                vpList = vendorPlantRepository.findMatchingPlants(tx.getPlantId().trim());
                cache.put(vpKey, vpList);
            }

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
            String userKey = "user_" + dto.getAssignedToUser();
            UserMaster user = null;
            if (cache.containsKey(userKey)) {
                user = (UserMaster) cache.get(userKey);
            } else {
                user = userMasterRepository.findById(dto.getAssignedToUser().intValue()).orElse(null);
                cache.put(userKey, user);
            }

            if (user != null) {
                dto.setAssignedToUserName(user.getFullName());
                dto.setIeName(user.getFullName());
                dto.setAssignedToUserEmployeeCode(user.getEmployeeCode());
            }
        } else {
            // If call is not yet assigned (e.g. at RIO Help Desk), fetch mapped Main IE for that plant
            List<SleeperPoiIeMapping> ieMaps = null;
            if (tx.getPoiCode() != null && tx.getPlantId() != null) {
                String ieKey1 = "ieMapMain_" + tx.getPoiCode() + "_" + tx.getPlantId();
                if (cache.containsKey(ieKey1)) {
                    ieMaps = (List<SleeperPoiIeMapping>) cache.get(ieKey1);
                } else {
                    ieMaps = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(tx.getPoiCode(), tx.getPlantId(), "Main IE");
                    if (ieMaps == null || ieMaps.isEmpty()) {
                        ieMaps = poiIeMappingRepository.findByPoiCodeAndPlantIdAndIeType(tx.getPoiCode(), tx.getPlantId(), "MAIN_IE");
                    }
                    cache.put(ieKey1, ieMaps);
                }
            }
            if ((ieMaps == null || ieMaps.isEmpty()) && tx.getPlantId() != null) {
                String ieKey2 = "ieMapPlant_" + tx.getPlantId();
                if (cache.containsKey(ieKey2)) {
                    ieMaps = (List<SleeperPoiIeMapping>) cache.get(ieKey2);
                } else {
                    ieMaps = poiIeMappingRepository.findByPlantIdAndIeType(tx.getPlantId(), "Main IE");
                    if (ieMaps == null || ieMaps.isEmpty()) {
                        ieMaps = poiIeMappingRepository.findByPlantIdAndIeType(tx.getPlantId(), "MAIN_IE");
                    }
                    cache.put(ieKey2, ieMaps);
                }
            }
            if (ieMaps != null && !ieMaps.isEmpty()) {
                Integer mappedIeId = ieMaps.get(0).getIeUserId();
                String userKey = "user_" + mappedIeId;
                UserMaster user = null;
                if (cache.containsKey(userKey)) {
                    user = (UserMaster) cache.get(userKey);
                } else {
                    user = userMasterRepository.findById(mappedIeId).orElse(null);
                    cache.put(userKey, user);
                }
                if (user != null) {
                    dto.setAssignedToUserName(user.getFullName());
                    dto.setIeName(user.getFullName());
                    dto.setAssignedToUserEmployeeCode(user.getEmployeeCode());
                }
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
        if (req.getAction().equalsIgnoreCase("CANCEL")
                || req.getAction().equalsIgnoreCase("CANCEL_CALL")
                || req.getAction().equalsIgnoreCase("CANCELLED")
                || (req.getAction().equalsIgnoreCase("VERIFY_MATERIAL_AVAILABILITY") && "NO".equalsIgnoreCase(req.getMaterialAvailable()))) {

            tx.setCurrentRole(current.getNextRole() != null ? current.getNextRole() : current.getCurrentRole());
            tx.setNextRole(null);
            tx.setStatus("CANCELLED");
            tx.setJobStatus("CANCELLED");
            tx.setAction("CANCEL");

            String cancelRemarks = req.getRemarks() != null && !req.getRemarks().isEmpty()
                    ? req.getRemarks()
                    : (req.getCancellationDescription() != null ? req.getCancellationDescription() : "Call Cancelled");
            tx.setRemarks(cancelRemarks);

            // 1. Update sleeper_inspection_call status to CANCELLED
            try {
                if (sleeperInspectionCallRepository != null) {
                    Optional<SleeperInspectionCall> icOpt = sleeperInspectionCallRepository.findByCallNo(req.getRequestId());
                    if (icOpt.isPresent()) {
                        SleeperInspectionCall ic = icOpt.get();
                        ic.setStatus("CANCELLED");
                        sleeperInspectionCallRepository.save(ic);
                    }
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Could not update SleeperInspectionCall status: " + ex.getMessage());
            }

            // 2. Save to sleeper_call_cancellation_details
            try {
                if (sleeperCallCancellationDetailRepository != null) {
                    String dynamicVendorCode = req.getVendorCode();
                    if (dynamicVendorCode == null || dynamicVendorCode.isEmpty()) {
                        dynamicVendorCode = current.getVendorCode() != null ? current.getVendorCode() : "";
                    }

                    String creatorId = req.getActionBy() != null ? String.valueOf(req.getActionBy()) :
                                      (req.getUpdatedBy() != null ? req.getUpdatedBy() : null);

                    SleeperCallCancellationDetail cancellationDetail = new SleeperCallCancellationDetail();
                    cancellationDetail.setCallNumber(req.getRequestId());
                    cancellationDetail.setVendorCode(dynamicVendorCode);
                    cancellationDetail.setCancellationBasis(req.getCancellationBasis() != null ? req.getCancellationBasis() : "NON_CHARGEABLE");
                    cancellationDetail.setVisitStatus(req.getVisitStatus());
                    
                    String reasonsStr = req.getReasons();
                    if (reasonsStr == null && req.getCancellationReasons() != null) {
                        reasonsStr = String.join("; ", req.getCancellationReasons());
                    }
                    if (reasonsStr == null) {
                        reasonsStr = cancelRemarks;
                    }
                    cancellationDetail.setReasons(reasonsStr);
                    cancellationDetail.setCancellationDescription(req.getCancellationDescription() != null ? req.getCancellationDescription() : cancelRemarks);
                    cancellationDetail.setMaterialValue(req.getMaterialValue());
                    cancellationDetail.setPercentage(req.getPercentage() != null ? req.getPercentage() : req.getCancellationPercentage());
                    cancellationDetail.setCalculatedCharges(req.getCalculatedCharges());
                    cancellationDetail.setMaximumCap(req.getMaximumCap());
                    cancellationDetail.setFinalCancellationCharges(req.getFinalCancellationCharges() != null ? req.getFinalCancellationCharges() : BigDecimal.ZERO);
                    cancellationDetail.setDocumentName(req.getDocumentName());
                    cancellationDetail.setActionBy(req.getActionBy() != null ? req.getActionBy() : 0L);
                    cancellationDetail.setCreatedBy(creatorId);
                    cancellationDetail.setUpdatedBy(creatorId);

                    sleeperCallCancellationDetailRepository.save(cancellationDetail);

                    if (sleeperVendorFinancialLiabilityRepository != null && !"NON_CHARGEABLE".equalsIgnoreCase(cancellationDetail.getCancellationBasis())) {
                        SleeperVendorFinancialLiability liability = new SleeperVendorFinancialLiability();
                        liability.setCallNumber(cancellationDetail.getCallNumber());
                        liability.setVendorCode(cancellationDetail.getVendorCode());
                        liability.setLiabilityType("CANCELLATION_CHARGES");
                        BigDecimal chargeAmt = cancellationDetail.getFinalCancellationCharges() != null ?
                                cancellationDetail.getFinalCancellationCharges() : BigDecimal.ZERO;
                        // Add 18% GST
                        BigDecimal totalWithGst = chargeAmt.multiply(BigDecimal.valueOf(1.18)).setScale(2, java.math.RoundingMode.HALF_UP);
                        liability.setAmount(totalWithGst);
                        liability.setPaymentStatus("PENDING");
                        sleeperVendorFinancialLiabilityRepository.save(liability);
                    }
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Failed to persist SleeperCallCancellationDetail / SleeperVendorFinancialLiability: " + ex.getMessage());
            }

            tx.setCreatedBy(current.getCreatedBy());
            tx.setModifiedBy(req.getActionBy());
            tx.setCreatedDate(LocalDateTime.now());
            tx.setUpdatedDate(LocalDateTime.now());

            SleeperWorkflowTransaction saved = repository.save(tx);
            return mapToResponse(saved);
        } else if (req.getAction().equalsIgnoreCase("IC_ISSUE")) {
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
            tx.setJobStatus("IC_GENERATION");
            
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
            case "GENERATE_IC":
            case "DSC_SIGN_IC":
                return "IC_GENERATION";

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

        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
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
        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        return list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
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
        return allFinalCompletedWorkflowTransitions(null);
    }

    @Override
    public List<SleeperWorkflowTransactionDto> allFinalCompletedWorkflowTransitions(String plantId) {
        String cleanPlantId = (plantId != null && !plantId.trim().isEmpty()) ? plantId.trim() : null;
        List<SleeperWorkflowTransaction> list = repository.findFinalCompletedRequests();

        java.util.Map<String, Object> cache = new java.util.HashMap<>();
        List<SleeperWorkflowTransactionDto> dtos = list.stream()
                .map(tx -> this.mapToResponse(tx, cache))
                .toList();

        if (cleanPlantId != null && !cleanPlantId.isEmpty()) {
            dtos = dtos.stream()
                    .filter(dto -> isPlantMatch(dto.getPlantId(), cleanPlantId))
                    .toList();
        }

        return dtos;
    }

    private boolean isPlantMatch(String callPlantId, String targetPlantId) {
        if (targetPlantId == null || targetPlantId.trim().isEmpty()) return true;
        if (callPlantId == null || callPlantId.trim().isEmpty()) return false;

        String cleanCall = callPlantId.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String cleanTarget = targetPlantId.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (cleanCall.isEmpty() || cleanTarget.isEmpty()) return false;

        if (cleanCall.equals(cleanTarget)) return true;

        if (cleanCall.contains(cleanTarget) || cleanTarget.contains(cleanCall)) {
            String[] callParts = callPlantId.split("[/:]");
            String[] targetParts = targetPlantId.split("[/:]");
            if (callParts.length > 1 && targetParts.length > 1) {
                String callUnit = callParts[callParts.length - 1].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                String targetUnit = targetParts[targetParts.length - 1].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                if (!callUnit.isEmpty() && !targetUnit.isEmpty()) {
                    return callUnit.equals(targetUnit) || callUnit.contains(targetUnit) || targetUnit.contains(callUnit);
                }
            }
            return true;
        }
        return false;
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

    private String resolveSleeperCaseNo(String rawCaseNo, String rio) {
        if (rawCaseNo == null || rawCaseNo.trim().isEmpty()) {
            return null;
        }
        String trimmedCaseNo = rawCaseNo.trim();
        String[] parts = trimmedCaseNo.split(",");

        if (rio != null && !rio.trim().isEmpty()) {
            String cleanRio = rio.trim().toUpperCase();
            String firstLetter = cleanRio.substring(0, 1);
            for (String part : parts) {
                String p = part.trim();
                if (p.toUpperCase().startsWith(firstLetter)) {
                    return p;
                }
            }
            return null;
        }

        for (String part : parts) {
            String p = part.trim();
            if (!p.isEmpty()) {
                return p;
            }
        }
        return parts[0].trim();
    }

    @Override
    public List<SleeperCancelledPaymentCallDto> getCancelledCallsForPayment(String plantId, String vendorCode) {
        String effectivePlantId = (plantId != null && !plantId.trim().isEmpty() && !"1".equals(plantId.trim())) 
                ? plantId.replace(":", "").trim() : null;
        String effectiveVendorCode = (vendorCode != null && !vendorCode.trim().isEmpty()) 
                ? vendorCode.replace(":", "").trim() : null;

        java.util.LinkedHashSet<String> allCallNos = new java.util.LinkedHashSet<>();

        // 1. Batch preload cancellation details (Filtered by vendor if available)
        java.util.Map<String, SleeperCallCancellationDetail> cancelMap = new java.util.HashMap<>();
        if (sleeperCallCancellationDetailRepository != null) {
            try {
                List<SleeperCallCancellationDetail> cancels = (effectiveVendorCode != null && !effectiveVendorCode.isBlank())
                        ? sleeperCallCancellationDetailRepository.findByVendorCode(effectiveVendorCode)
                        : sleeperCallCancellationDetailRepository.findAll();
                if (cancels != null) {
                    cancels.forEach(cd -> {
                        if (cd.getCallNumber() != null && !cd.getCallNumber().isBlank()) {
                            String cn = cd.getCallNumber().trim();
                            allCallNos.add(cn);
                            cancelMap.put(cn, cd);
                        }
                    });
                }
            } catch (Exception ex) {
                log.warn("Error reading sleeper_call_cancellation_details: {}", ex.getMessage());
            }
        }

        // 2. Batch preload sleeper vendor financial liabilities (Filtered by vendor if available)
        java.util.Map<String, SleeperVendorFinancialLiability> liabilityMap = new java.util.HashMap<>();
        if (sleeperVendorFinancialLiabilityRepository != null) {
            try {
                List<SleeperVendorFinancialLiability> liabs = (effectiveVendorCode != null && !effectiveVendorCode.isBlank())
                        ? sleeperVendorFinancialLiabilityRepository.findByVendorCode(effectiveVendorCode)
                        : sleeperVendorFinancialLiabilityRepository.findAll();
                if (liabs != null) {
                    liabs.forEach(l -> {
                        if (l.getCallNumber() != null && !l.getCallNumber().isBlank()) {
                            String cn = l.getCallNumber().trim();
                            allCallNos.add(cn);
                            liabilityMap.put(cn, l);
                        }
                    });
                }
            } catch (Exception ex) {
                log.warn("Error reading sleeper_vendor_financial_liability: {}", ex.getMessage());
            }
        }

        // If no candidate calls exist, return empty immediately (instant response)
        if (allCallNos.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> callNoList = new java.util.ArrayList<>(allCallNos);

        // 3. Fast indexed batch fetch SleeperWorkflowTransactions for only candidate calls
        java.util.Map<String, SleeperWorkflowTransaction> txMap = new java.util.HashMap<>();
        if (repository != null && !callNoList.isEmpty()) {
            try {
                List<SleeperWorkflowTransaction> txList = repository.findByRequestIdIn(callNoList);
                if (txList != null) {
                    txList.forEach(t -> {
                        if (t.getRequestId() != null && !t.getRequestId().isBlank()) {
                            String reqId = t.getRequestId().trim();
                            SleeperWorkflowTransaction existing = txMap.get(reqId);
                            if (existing == null || (t.getWorkflowTransitionId() != null && (existing.getWorkflowTransitionId() == null || t.getWorkflowTransitionId() > existing.getWorkflowTransitionId()))) {
                                txMap.put(reqId, t);
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                log.warn("Error reading cancelled sleeper workflow transactions: {}", ex.getMessage());
            }
        }

        // 4. Batch fetch SleeperInspectionCalls in a single query (Eliminates N+1)
        java.util.Map<String, SleeperInspectionCall> callMap = new java.util.HashMap<>();
        if (sleeperInspectionCallRepository != null && !callNoList.isEmpty()) {
            try {
                List<SleeperInspectionCall> calls = sleeperInspectionCallRepository.findByCallNoIn(callNoList);
                if (calls != null) {
                    calls.forEach(c -> {
                        if (c.getCallNo() != null) callMap.put(c.getCallNo().trim(), c);
                    });
                }
            } catch (Exception ex) {
                log.warn("Error batch fetching SleeperInspectionCalls: {}", ex.getMessage());
            }
        }

        // 5. Batch fetch PoHeaders in a single query (Eliminates N+1)
        java.util.Set<String> poNos = new java.util.HashSet<>();
        callMap.values().forEach(c -> {
            if (c.getPoNo() != null && !c.getPoNo().isBlank()) {
                String barePoNo = c.getPoNo().contains("/") ? c.getPoNo().split("/")[0].trim() : c.getPoNo().trim();
                if (!barePoNo.isBlank()) poNos.add(barePoNo);
            }
        });
        java.util.Map<String, PoHeader> poHeaderMap = new java.util.HashMap<>();
        if (poHeaderRepository != null && !poNos.isEmpty()) {
            try {
                List<PoHeader> headers = poHeaderRepository.findByPoNoIn(new java.util.ArrayList<>(poNos));
                if (headers != null) {
                    headers.forEach(h -> {
                        if (h.getPoNo() != null) poHeaderMap.put(h.getPoNo().trim(), h);
                    });
                }
            } catch (Exception ex) {
                log.warn("Error batch fetching PoHeaders: {}", ex.getMessage());
            }
        }

        // 6. Preload VendorPlants map (Eliminates N+1)
        java.util.Map<String, com.sarthi.Sleeper.entity.VendorPlant> vendorPlantMap = new java.util.HashMap<>();
        if (vendorPlantRepository != null) {
            try {
                vendorPlantRepository.findAll().forEach(vp -> {
                    if (vp.getPlantId() != null) {
                        vendorPlantMap.put(vp.getPlantId().trim(), vp);
                        vendorPlantMap.put(vp.getPlantId().replace(":", "").trim(), vp);
                    }
                });
            } catch (Exception ex) {
                log.warn("Error preloading VendorPlants: {}", ex.getMessage());
            }
        }

        List<SleeperCancelledPaymentCallDto> result = new ArrayList<>();

        for (String callNo : allCallNos) {
            SleeperInspectionCall callEntity = callMap.get(callNo);
            SleeperWorkflowTransaction latestTx = txMap.get(callNo);
            SleeperCallCancellationDetail cancelDetail = cancelMap.get(callNo);
            SleeperVendorFinancialLiability liability = liabilityMap.get(callNo);

            String callPlantId = null;
            if (callEntity != null && callEntity.getPlantId() != null && !callEntity.getPlantId().isBlank()) {
                callPlantId = callEntity.getPlantId();
            } else if (latestTx != null && latestTx.getPlantId() != null) {
                callPlantId = latestTx.getPlantId();
            }

            String callVendorCode = null;
            if (cancelDetail != null && cancelDetail.getVendorCode() != null && !cancelDetail.getVendorCode().isBlank()) {
                callVendorCode = cancelDetail.getVendorCode();
            } else if (latestTx != null && latestTx.getVendorCode() != null) {
                callVendorCode = latestTx.getVendorCode();
            }

            // Apply vendor filter if provided
            if (effectiveVendorCode != null && !effectiveVendorCode.isBlank()) {
                String cleanCallVendor = (callVendorCode != null) ? callVendorCode.replace(":", "").trim() : "";
                String cleanReqVendor = effectiveVendorCode.replace(":", "").trim();
                if (!cleanCallVendor.equalsIgnoreCase(cleanReqVendor) 
                        && !cleanCallVendor.toLowerCase().contains(cleanReqVendor.toLowerCase())
                        && !cleanReqVendor.toLowerCase().contains(cleanCallVendor.toLowerCase())) {
                    continue;
                }
            }

            // Apply plant filter if provided
            if (effectivePlantId != null && !effectivePlantId.isBlank()) {
                String cleanCallPlant = (callPlantId != null) ? callPlantId.replace(":", "").trim() : "";
                if (!cleanCallPlant.equalsIgnoreCase(effectivePlantId)) {
                    continue;
                }
            }

            double base = 0.0;
            boolean isNonChargeable = false;
            String cancelRemarks = (latestTx != null) ? latestTx.getRemarks() : null;
            String action = (latestTx != null) ? latestTx.getAction() : "CANCEL";
            Long txId = (latestTx != null) ? latestTx.getWorkflowTransitionId() : 0L;
            java.time.LocalDateTime createdDate = (latestTx != null && latestTx.getCreatedDate() != null) ? latestTx.getCreatedDate() : java.time.LocalDateTime.now();
            String documentName = null;

            if (cancelDetail != null) {
                if (cancelDetail.getDocumentName() != null && !cancelDetail.getDocumentName().isBlank()) {
                    documentName = cancelDetail.getDocumentName();
                }
                if ("NON_CHARGEABLE".equalsIgnoreCase(cancelDetail.getCancellationBasis())) {
                    isNonChargeable = true;
                    base = 0.0;
                } else {
                    if (cancelDetail.getFinalCancellationCharges() != null) {
                        base = cancelDetail.getFinalCancellationCharges().doubleValue();
                    } else if (cancelDetail.getCalculatedCharges() != null) {
                        base = cancelDetail.getCalculatedCharges().doubleValue();
                    }
                }
                if (cancelRemarks == null && cancelDetail.getCancellationDescription() != null) {
                    cancelRemarks = cancelDetail.getCancellationDescription();
                }
            }

            if (cancelRemarks != null) {
                String rem = cancelRemarks.toUpperCase();
                if (rem.contains("NON_CHARGEABLE") || rem.contains("NON-CHARGEABLE")) {
                    isNonChargeable = true;
                    base = 0.0;
                }
            }

            if (base == 0.0 && !isNonChargeable && liability != null && liability.getAmount() != null) {
                base = liability.getAmount().doubleValue() / 1.18;
            }

            // Skip non-chargeable calls or zero charges
            if (isNonChargeable || base <= 0.0) {
                continue;
            }

            SleeperCancelledPaymentCallDto dto = new SleeperCancelledPaymentCallDto();
            dto.setWorkflowTransitionId(txId != null ? txId.intValue() : 0);
            dto.setCallNo(callNo);
            dto.setStatus("CANCELLED");
            dto.setCancelRemarks(cancelRemarks);
            dto.setAction(action);
            dto.setPlantId(callPlantId);
            dto.setVendorCode(callVendorCode != null ? callVendorCode : vendorCode);
            dto.setCreatedDate(createdDate);
            dto.setDocumentName(documentName);

            String plantRio = (latestTx != null) ? latestTx.getRio() : null;
            if ((plantRio == null || plantRio.isBlank()) && callPlantId != null) {
                String cleanPId = callPlantId.replace(":", "").trim();
                com.sarthi.Sleeper.entity.VendorPlant vp = vendorPlantMap.get(cleanPId);
                if (vp == null) {
                    vp = vendorPlantMap.get(callPlantId);
                }
                if (vp != null && vp.getRio() != null) {
                    plantRio = vp.getRio().trim();
                }
            }
            dto.setRio(plantRio);

            if (callEntity != null) {
                dto.setPoNo(callEntity.getPoNo());
                long off = callEntity.getTotalOffered() != null ? callEntity.getTotalOffered().longValue() : 0L;
                long rej = callEntity.getTotalRejected() != null ? callEntity.getTotalRejected().longValue() : 0L;
                dto.setOfferedQty(off + rej);
                dto.setCallDate(callEntity.getCreatedAt() != null ? callEntity.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);
                dto.setSleeperType(callEntity.getSleeperType());

                // Fetch Case No using plant RIO logic from preloaded map
                String rawPoNo = callEntity.getPoNo();
                String barePoNo = rawPoNo;
                if (barePoNo != null && barePoNo.contains("/")) {
                    barePoNo = barePoNo.split("/")[0].trim();
                }
                if (barePoNo != null) {
                    PoHeader header = poHeaderMap.get(barePoNo);
                    if (header != null && header.getCaseNo() != null) {
                        String matchedCaseNo = resolveSleeperCaseNo(header.getCaseNo(), plantRio);
                        dto.setIbsCaseNo(matchedCaseNo != null ? matchedCaseNo : header.getCaseNo());
                    }
                }
            }

            // Determine RIO Email
            String rioEmail = "nrinspn.fin@rites.com";
            if (plantRio != null) {
                String uRio = plantRio.toUpperCase();
                if (uRio.contains("EAST") || uRio.contains("ER")) rioEmail = "callletter.er@rites.com";
                else if (uRio.contains("WEST") || uRio.contains("WR")) rioEmail = "dfo.wrio@rites.com";
                else if (uRio.contains("SOUTH") || uRio.contains("SR")) rioEmail = "dfo.srio@rites.com";
                else if (uRio.contains("CENT") || uRio.contains("CR")) rioEmail = "dfo.crio@rites.com";
                else if (uRio.contains("NORTH") || uRio.contains("NR")) rioEmail = "nrinspn.fin@rites.com";
            }
            dto.setRioEmail(rioEmail);

            double gst = Math.round((base * 18.0) / 100.0);
            dto.setBasePayableAmount(base);
            dto.setGst(gst);
            dto.setTotalPayableAmount(base + gst);
            dto.setBankAccountDetails("SBI A/c: 39482910482, IFSC: SBIN0001234, Branch: RITES Central");
            dto.setPaymentReason("Cancellation");
            dto.setChargeType("Cancellation");

            String paymentStatus = "Payment Pending";
            if (liability != null && liability.getPaymentStatus() != null) {
                String ps = liability.getPaymentStatus().trim();
                if ("PAID".equalsIgnoreCase(ps) 
                        || "COMPLETED".equalsIgnoreCase(ps) 
                        || "PAYMENT COMPLETED".equalsIgnoreCase(ps) 
                        || "APPROVED".equalsIgnoreCase(ps) 
                        || "Approved by RITES Finance".equalsIgnoreCase(ps)) {
                    paymentStatus = "Approved by RITES Finance";
                } else if ("PENDING".equalsIgnoreCase(ps) || "PAYMENT PENDING".equalsIgnoreCase(ps)) {
                    paymentStatus = "Payment Pending";
                } else if (!ps.isEmpty()) {
                    paymentStatus = ps;
                }
            }
            dto.setPaymentStatus(paymentStatus);

            result.add(dto);
        }
        return result;
    }

    @Override
    public boolean isPlantBlockedForCallRaising(String plantId, String vendorCode) {
        List<SleeperCancelledPaymentCallDto> list = getCancelledCallsForPayment(plantId, vendorCode);
        for (SleeperCancelledPaymentCallDto item : list) {
            boolean isChargeable = (item.getTotalPayableAmount() != null && item.getTotalPayableAmount() > 0);
            if (isChargeable) {
                String status = item.getPaymentStatus();
                if (status == null || (!"Approved by RITES Finance".equalsIgnoreCase(status) 
                        && !"APPROVED".equalsIgnoreCase(status)
                        && !"PAID".equalsIgnoreCase(status)
                        && !"COMPLETED".equalsIgnoreCase(status)
                        && !"PAYMENT COMPLETED".equalsIgnoreCase(status))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public SleeperCallCancellationDetail getCancellationDetails(String callNo) {
        if (sleeperCallCancellationDetailRepository != null && callNo != null) {
            return sleeperCallCancellationDetailRepository.findByCallNumber(callNo.trim()).orElse(null);
        }
        return null;
    }

    // ─── IBS Payment Verification Proxy ──────────────────────────────────────────

    private static final String IBS_GET_BILL_DETAILS_URL =
            "https://ritesinsp.com/IBS2MobileAPI/Sarthi/get-bill-details";

    private String getIbsBearerToken() {
        String envToken = System.getenv("IBS_BEARER_TOKEN");
        if (envToken != null && !envToken.isBlank()) return envToken;
        return "Basic cmltZXMtc2FydGhpOnNhclRISUBAc3BlcmkyNg==";
    }

    @Override
    public java.util.Map<String, Object> verifyIbsPayment(String caseNo, String callDate, int ibsCallSno) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getIbsBearerToken());

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("caseNo", caseNo);
            requestBody.put("callRecvDt", callDate);
            requestBody.put("callSno", ibsCallSno);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            org.springframework.http.ResponseEntity<java.util.Map> response =
                    restTemplate.postForEntity(IBS_GET_BILL_DETAILS_URL, entity, java.util.Map.class);

            if (response.getBody() != null) {
                return (java.util.Map<String, Object>) response.getBody();
            }
        } catch (Exception e) {
            log.error("Error calling IBS get-bill-details API for Sleeper caseNo={}, callDate={}, ibsCallSno={}: {}",
                    caseNo, callDate, ibsCallSno, e.getMessage());
            java.util.Map<String, Object> errorResp = new java.util.HashMap<>();
            errorResp.put("resultFlag", 0);
            errorResp.put("message", "Failed to reach IBS API: " + e.getMessage());
            errorResp.put("bill_details", java.util.Collections.emptyList());
            errorResp.put("payment_details", java.util.Collections.emptyList());
            errorResp.put("bill_details_error", e.getMessage());
            errorResp.put("payment_details_error", null);
            return errorResp;
        }
        java.util.Map<String, Object> empty = new java.util.HashMap<>();
        empty.put("resultFlag", 0);
        empty.put("message", "No response from IBS API");
        empty.put("bill_details", java.util.Collections.emptyList());
        empty.put("payment_details", java.util.Collections.emptyList());
        return empty;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void markPaymentApprovedByIbs(String callNo) {
        if (callNo == null || callNo.isBlank()) return;
        String cleanCallNo = callNo.trim();

        if (sleeperVendorFinancialLiabilityRepository != null) {
            Optional<SleeperVendorFinancialLiability> liabOpt =
                    sleeperVendorFinancialLiabilityRepository.findByCallNumber(cleanCallNo);

            SleeperVendorFinancialLiability liability;
            if (liabOpt.isPresent()) {
                liability = liabOpt.get();
            } else {
                liability = new SleeperVendorFinancialLiability();
                liability.setCallNumber(cleanCallNo);
                if (sleeperCallCancellationDetailRepository != null) {
                    sleeperCallCancellationDetailRepository.findByCallNumber(cleanCallNo)
                            .ifPresent(cd -> {
                                liability.setVendorCode(cd.getVendorCode());
                                if (cd.getFinalCancellationCharges() != null) {
                                    BigDecimal totalWithGst = cd.getFinalCancellationCharges()
                                            .multiply(BigDecimal.valueOf(1.18))
                                            .setScale(2, java.math.RoundingMode.HALF_UP);
                                    liability.setAmount(totalWithGst);
                                }
                            });
                }
            }
            liability.setPaymentStatus("Approved by RITES Finance");
            liability.setLiabilityType("CANCELLATION_CHARGES");
            sleeperVendorFinancialLiabilityRepository.saveAndFlush(liability);
            log.info("Payment marked as 'Approved by RITES Finance' for call {} via IBS verification in sleeper_vendor_financial_liability.", cleanCallNo);
        }
    }
}
