package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IcWorkflowTransitionDto;
import com.sarthi.dto.InspectionDataDto;
import com.sarthi.dto.WorkflowDto;
import com.sarthi.dto.WorkflowDtos.TransitionActionReqDto;
import com.sarthi.dto.WorkflowDtos.TransitionDto;
import com.sarthi.dto.WorkflowDtos.WorkflowTransitionDto;
import com.sarthi.entity.*;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.repository.*;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository;
import com.sarthi.service.WorkflowService;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Service
public class WorkflowServiceImpl implements WorkflowService {
    @Autowired
    private WorkflowMasterRepository workflowMasterRepository;
    @Autowired
    private UserMasterRepository userMasterRepository;
    @Autowired
    private TransitionMasterRepository transitionMasterRepository;
    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;
    @Autowired
    private RoleMasterRepository roleMasterRepository;
    @Autowired
    private PincodeClusterRepository pincodeClusterRepository;
    @Autowired
    private ClusterRioUserRepository clusterRioUserRepository;
    @Autowired
    private RegionClusterRepository regionClusterRepository;
    @Autowired
    private ClusterPrimaryIeRepository clusterPrimaryIeRepository;
    @Autowired
    private ClusterSecondaryIeRepository clusterSecondaryIeRepository;
    @Autowired
    private TransitionConditionMasterRepository transitionConditionMasterRepository;
    @Autowired
    private ClusterCmUserRepository clusterCmUserRepository;
    @Autowired
    private ProcessIeMasterRepository processIeMasterRepository;
    @Autowired
    private ProcessIeMappingRepository processIeMappingRepository;
    @Autowired
    private RegionSbuHeadRepository regionSbuHeadRepository;
    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private IeFieldsMappingRepository ieFieldsMappingRepository;
    @Autowired
    private PincodePoIMappingRepository pincodePoIMappingRepository;
    @Autowired
    private IePincodePoiMappingRepository iePincodePoiMappingRepository;
    @Autowired
    private ieControllingManagerRepository ieControllingManagerRepository;

    @Autowired
    private  ProcessIeUsersRepository processIeUsersRepository;
    @Autowired
    private IePoiMappingRepository iePoiMappingRepository;
    @Autowired
    private InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;

    @Autowired
    private FinalIeMappingRepository finalIeMappingRepository;

    @Autowired
    private ProcessIeQtyRepository processIeQtyRepository;

    @Autowired
    private ProcessInspectionDetailsRepository processInspectionDetailsRepository;

    @Autowired
    private PoHeaderRepository poHeaderRepository;
    @Autowired
    private VendorMasterRepository vendorMasterRepository;
    @Autowired
    private RmInspectionDetailsRepository rmInspectionDetailsRepository;
    @Autowired
    private RmHeatQuantityRepository rmHeatQuantityRepository;
    @Autowired
    private InventoryEntryRepository inventoryEntryRepository;

    @Autowired
    private PoiProcessIeMappingRepository poiProcessIeMappingRepository;
    private static final Logger log =
            LoggerFactory.getLogger(WorkflowServiceImpl.class);



    public void validateUser(Integer userId) {
        UserMaster userMaster = userMasterRepository.findById(userId).orElseThrow(() -> new InvalidInputException(new ErrorDetails(AppConstant.USER_NOT_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION, "User not found.")));
    }
    @Override
    @Transactional
    public WorkflowTransitionDto initiateWorkflow(String requestId, Integer createdBy, String workflowName, String pincode) {


        // Validate vendor
      //  userService.validateUser(createdBy);
        validateUser(createdBy);

        // UPDATE INSPECTION CALL CREATED_BY FIELD
        // When workflow is initiated, update the inspection_call.created_by to the actual user ID
        try {
            InspectionCall inspectionCall = inspectionCallRepository.findByIcNumber(requestId).orElse(null);
            if (inspectionCall != null &&
                (inspectionCall.getCreatedBy() == null ||
                 inspectionCall.getCreatedBy().equalsIgnoreCase("system") ||
                 inspectionCall.getCreatedBy().equals("0"))) {

                inspectionCall.setCreatedBy(String.valueOf(createdBy));
                inspectionCall.setUpdatedBy(String.valueOf(createdBy));
                inspectionCallRepository.save(inspectionCall);
                System.out.println("✅ Updated inspection_call.created_by to: " + createdBy + " for IC: " + requestId);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not update inspection_call.created_by: " + e.getMessage());
            // Don't fail the workflow if this update fails
        }

        // Fetch workflow
         WorkflowMaster workflow = workflowMasterRepository.findByWorkflowName(workflowName);

        if (Objects.isNull(workflow)) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Workflow not found"
                    )
            );
        }

      //  TransitionMaster transition = null;

        // Fetch initial transition
        TransitionMaster transition = transitionMasterRepository
                .findByWorkflowIdAndTransitionOrder(workflow.getWorkflowId(), 1)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Initial transition not found"
                        )
                ));
        // Prevent duplicate workflow
        WorkflowTransition exists = workflowTransitionRepository
                .findByWorkflowIdAndRequestId(workflow.getWorkflowId(), requestId);
      if (Objects.nonNull(exists)) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Workflow already initiated for this request"
                    )
            );
        }
        // If IE is scheduling IC → start workflowId=2
        if (workflowName.equalsIgnoreCase("IE INSPECTION")) {

              //  WorkflowTransition last =
                //        workflowTransitionRepository.findTopByRequestIdOrderByWorkflowTransitionIdDesc(requestId);
            WorkflowTransition last =
                    workflowTransitionRepository
                            .findTopByRequestIdAndStatusOrderByWorkflowTransitionIdDesc(requestId, "CALL_REGISTERED");

            System.out.println("Last CALL_REGISTERED record: " + last);

            System.out.print(last);
         //    String inspectionType ="PROCESS";
          Optional<InspectionCall> i = inspectionCallRepository.findByIcNumber(requestId);

          InspectionCall ic =null;
          if(i.isPresent()){
              ic= i.get();
          }
               if(ic.getTypeOfCall().equalsIgnoreCase("PROCESS")){
                  //  && last.getNextRoleName().equalsIgnoreCase("IE")
                 //  validateProcessIeAction(last.getProcessIeUserId(),createdBy);
                 /*  validateProcessIeAction(

                           last.getProcessIeUserId().longValue(),
                           createdBy.longValue(), ic.getPlaceOfInspection()
                   );*/
                   validateProcessIeAction(
                           createdBy.longValue(), ic.getPlaceOfInspection()
                   );

               }else if (ic.getTypeOfCall().equalsIgnoreCase("FINAL")) {

                   validateFinalIeAction(
                           last.getWorkflowTransitionId(),
                           createdBy
                   );
               }else {
                   if (last.getAssignedToUser() == null ||
                           !last.getAssignedToUser().equals(createdBy)) {// Only assigned IE can act

                       throw new InvalidInputException(
                               new ErrorDetails(
                                       AppConstant.ACCESS_DENIED,
                                       AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                       AppConstant.ERROR_TYPE_VALIDATION,
                                       "You are not authorized to act on this inspection call."
                               )
                       );
                   }
               }

            // Fetch first transition of IE workflow
            TransitionMaster transitionMaster = transitionMasterRepository
                    .findByWorkflowIdAndTransitionOrder(workflow.getWorkflowId(), 1)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Initial IE transition not found"
                            )
                    ));

            System.out.print(transitionMaster);
            WorkflowTransition entry = new WorkflowTransition();
            entry.setWorkflowId(workflow.getWorkflowId());
            entry.setTransitionId(transition.getTransitionId());
            entry.setRequestId(requestId);
            entry.setStatus("IE_SCHEDULED");
            entry.setAction("IE_SCHEDULE_CALL");
            entry.setRemarks("IE has scheduled the call");
            entry.setCreatedBy(createdBy);
            entry.setCreatedDate(new Date());
            entry.setCurrentRole(String.valueOf(transitionMaster.getCurrentRoleId()));
            entry.setNextRole(String.valueOf(transitionMaster.getNextRoleId()));
            entry.setCurrentRoleName(roleNameById(transitionMaster.getCurrentRoleId()));
            entry.setNextRoleName(roleNameById(transitionMaster.getNextRoleId()));
            if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
                entry.setAssignedToUser(createdBy);
            }else{
                entry.setAssignedToUser(null);
            }
            entry.setJobStatus("ASSIGNED");
            entry.setProcessIeUserId(last.getProcessIeUserId());
            entry.setWorkflowSequence(last.getWorkflowSequence()+1);
            workflowTransitionRepository.save(entry);

            if("Final".equalsIgnoreCase(ic.getTypeOfCall())) {


                //  Fetch IE mappings using POI code
                List<IePincodePoiMapping> ieMappings =
                        iePincodePoiMappingRepository.findByPoiCode(ic.getPlaceOfInspection());

                for (IePincodePoiMapping mapping : ieMappings) {

                    String employeeCode = mapping.getEmployeeCode();

                    // Fetch userId using employeeCode
                    UserMaster userOpt =
                            userMasterRepository.findByEmployeeCode(employeeCode);

                    Integer userId = userOpt.getUserId();

                    //  Save into FINAL_IE_MAPPING
                    FinalIeMapping finalMapping = new FinalIeMapping();
                    finalMapping.setWorkflowTransitionId(
                            entry.getWorkflowTransitionId()
                    );
                    finalMapping.setIeUserId(userId);

                    finalIeMappingRepository.save(finalMapping);

                }
            }

            return mapWorkflowTransition(entry);
        }

        Integer assignedRioUserId =null;
        String rio = null;


          if (workflowName.equalsIgnoreCase("INSPECTION CALL")) {

              // Step 1: Get cluster by pincode
           /*   PincodeCluster cluster = pincodeClusterRepository.findByPincode(pincode)
                      .orElseThrow(() -> new BusinessException(
                              new ErrorDetails(
                                      AppConstant.ERROR_CODE_RESOURCE,
                                      AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                      AppConstant.ERROR_TYPE_VALIDATION,
                                      "No cluster found for pincode")));

              // Step 2: Get region for that cluster
              RegionCluster region = regionClusterRepository.findByClusterName(cluster.getClusterName())
                      .orElseThrow(() -> new BusinessException(
                              new ErrorDetails(
                                      AppConstant.ERROR_CODE_RESOURCE,
                                      AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                      AppConstant.ERROR_TYPE_VALIDATION,
                               "No region found for cluster")
                      ));

              // Find RIO user for that region
              ClusterRioUser rioUser = clusterRioUserRepository.findByClusterName(cluster.getClusterName())
                      .orElseThrow(() -> new BusinessException(
                              new ErrorDetails(
                                      AppConstant.ERROR_CODE_RESOURCE,
                                      AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                      AppConstant.ERROR_TYPE_VALIDATION,
                         "No RIO user found for region")
                      ));*/

              InspectionCall ic = inspectionCallRepository.findByIcNumber(requestId)
                      .orElseThrow(() -> new BusinessException(
                              new ErrorDetails(
                                      AppConstant.ERROR_CODE_INVALID,
                                      AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                      AppConstant.ERROR_TYPE_VALIDATION,
                                      "Place of Inspection (POI) is not assigned"
                              )
                      ));

              //  PincodePoIMapping poi = pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection());


              PincodePoIMapping poi =
                      pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection())
                              .orElseThrow(() -> new BusinessException(
                                      new ErrorDetails(
                                              AppConstant.ERROR_CODE_RESOURCE,
                                              AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                              AppConstant.ERROR_TYPE_VALIDATION,
                                              "Invalid POI code"
                                      )
                              ));
              String stage = null;
              if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
                  stage ="R";
              }else if(ic.getTypeOfCall().equalsIgnoreCase("Process")){
                  stage="P";
              }else{
                  stage="F";
              }
              String product ="ERC";
              String pinCode = poi.getPinCode();

              IEFieldsMapping mapping =
                      ieFieldsMappingRepository
                              .findByPinCodeProductAndStageMatch(pinCode, product, stage)
                              .orElseThrow(() -> new BusinessException(
                                      new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                              AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                              AppConstant.ERROR_TYPE_VALIDATION,
                                              "No IE mapping for given pin/product/stage")));

              rio =mapping.getRio();

            //  assignedRioUserId = rioUser.getRioUserId();
          }

        // Create transition entry
        WorkflowTransition entry = new WorkflowTransition();
        entry.setWorkflowId(workflow.getWorkflowId());
        entry.setTransitionId(transition.getTransitionId());
        entry.setRequestId(requestId);
        entry.setStatus(AppConstant.CREATED_TYPE);
        entry.setJobStatus(AppConstant.CREATED_TYPE);
        //entry.setCurrentRole(String.valueOf(transition.getCurrentRoleId()));
        entry.setCurrentRoleName(roleNameById(transition.getCurrentRoleId()));
        entry.setNextRole(String.valueOf(transition.getNextRoleId()));
        entry.setCurrentRole(String.valueOf(transition.getCurrentRoleId()));
        entry.setNextRoleName(roleNameById(transition.getNextRoleId()));
      //  entry.setNextRole(String.valueOf(transition.getNextRoleId()));
        entry.setCreatedBy(createdBy);
        entry.setCreatedDate(new Date());
        entry.setWorkflowSequence(1);
        //entry.setTransitionOrder(1);

        if(workflow.getWorkflowName().equalsIgnoreCase("INSPECTION CALL")){
            entry.setAssignedToUser(assignedRioUserId);
            entry.setRio(rio);
        }

        workflowTransitionRepository.save(entry);

        return mapWorkflowTransition(entry);
    }


    private void validateFinalIeAction(
            Integer workflowTransitionId,
            Integer actionBy
    ) {
        boolean allowed = finalIeMappingRepository
                .existsByWorkflowTransitionIdAndIeUserId(
                        workflowTransitionId,
                        actionBy
                );

        if (!allowed) {
            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "You are not authorized to act on this FINAL inspection."
                    )
            );
        }
    }
/*
    private void validateProcessIeAction(Long processIeUserId, Long actionBy,String poicode) {

//        List<ProcessIeUsers> mappings =
//                processIeUsersRepository.findAllByProcessUserId(Math.toIntExact(processIeUserId));

        List<Long> mappings =
                processIeUsersRepository.findIeUsersByProcessIeAndPoi(Math.toIntExact(processIeUserId), poicode);

        System.out.println(mappings);
        if (mappings.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No IE users mapped under Process IE user: " + processIeUserId
                    )
            );
        }

//        List<Long> allowedUsers = mappings.stream()
//                .map(ProcessIeUsers::getIeUserId)
//                .collect(Collectors.toList());
//
//        allowedUsers.add(processIeUserId);

        if (!mappings.contains(actionBy)) {
            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "You are not authorized to perform this action. " +
                                    "User " + actionBy + " is not under Process IE user " + processIeUserId
                    )
            );
        }
    }*/

    private void validateProcessIeAction(Long actionBy, String poiCode) {

        //  Get all userIds mapped to POI
        List<Long> userIds =
                poiProcessIeMappingRepository.findUserIdsByPoiCode(poiCode);

        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No Process IE mapped for POI: " + poiCode
                    )
            );
        }

        //  Validate user
        if (!userIds.contains(actionBy)) {
            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "You are not authorized for POI " + poiCode +
                                    ". User " + actionBy + " is not mapped."
                    )
            );
        }
    }

/*
    private void validateProcessIeAction(Integer processIeUserId, Integer actionBy) {


        List<ProcessIeMapping> mappings =
                processIeMappingRepository.findByProcessIeUserId(processIeUserId);

        if (mappings.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No IE users mapped under Process IE user: " + processIeUserId)
            );
        }

        List<Integer> allowedIeUsers = mappings.stream()
                .map(ProcessIeMapping::getIeUserId)
                .collect(Collectors.toList());
        allowedIeUsers.add(processIeUserId);
        System.out.print("users"+ allowedIeUsers);

        //  If actionBy is NOT part of allowed list Reject
        if (!allowedIeUsers.contains(actionBy)) {
            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "You are not authorized to perform this action. " +
                                    "User " + actionBy + " is not under Process IE user " + processIeUserId
                    )
            );
        }
    }*/

    private static final List<String> PROCESS_SWIFTS =
            List.of("A", "B", "C", "G");


    @Override
    @Transactional
    public WorkflowTransitionDto  performTransitionAction(TransitionActionReqDto req) {

       // String userId = CommonUtils.getUserIdFromAuthHeader(authorizationHeader);

        // Validate user
       validateUser(req.getActionBy());

       System.out.print(req);
        // Fetch current workflow transition
       WorkflowTransition current = workflowTransitionRepository
                .findById(req.getWorkflowTransitionId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Workflow transition not found"
                        )
                ));

            TransitionMaster transition = transitionMasterRepository
                    .findById(current.getTransitionId())
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "transition not found"
                            )
                    ));




        // IE WORKFLOW (ID = 2)
        if (current.getWorkflowId() == 2) {



         //   TransitionMaster nextTransition = transitionMasterRepository.findByTransitionName(req.getAction());
          //  System.out.print(nextTransition);
            WorkflowTransition last =null;
          if(req.getAction().equalsIgnoreCase("INITIATE_INSPECTION")){
          last = workflowTransitionRepository
                              .findTopByRequestIdAndStatusOrderByWorkflowTransitionIdDesc(req.getRequestId(), "IE_SCHEDULED");
          }else{
            last = workflowTransitionRepository
                      .findTopByRequestIdOrderByWorkflowTransitionIdDesc(current.getRequestId());

          }
            //  Stop if inspection already completed
            if (last != null &&
                    ("INSPECTION_COMPLETE_CONFIRM".equalsIgnoreCase(last.getStatus())
                            || "INSPECTION_COMPLETE_CONFIRM".equalsIgnoreCase(last.getAction()))) {
                ProcessIeQty qty = new ProcessIeQty();
                qty.setRequestId(req.getRequestId());
                qty.setSwiftCode(req.getShiftCode() != null ? req.getShiftCode() : "A");
                qty.setIeUserId(req.getActionBy());
                qty.setInspectedQty(req.getInspectedQty());
                qty.setOfferedQty(req.getOfferedQty());
                qty.setManufactureQty(req.getManufacturedQty());
                qty.setHeatNo(req.getHeatNo());
                qty.setRejectedQty(req.getRejectedQty());

                //qty.setOfferedQty(req.getOfferedQty());
                qty.setLotNumber(req.getLotNo());
                qty.setCompleted(false);

                System.out.println("Inspection already completed. No new transition.");

                return mapWorkflowTransition(last); // Exit early
            }


//            String inspectionType ="PROCESS";
            Optional<InspectionCall> insp = inspectionCallRepository.findByIcNumber(req.getRequestId());

            InspectionCall call =null;
          if(insp.isPresent()){
              call=insp.get();
          }

         //   String inspectionType ="Raw Material";
            if(call.getTypeOfCall().equalsIgnoreCase("PROCESS")){
              //  validateProcessIeAction(last.getProcessIeUserId(),req.getActionBy());
              /*  validateProcessIeAction(
                        last.getProcessIeUserId().longValue(),
                        req.getActionBy().longValue(), call.getPlaceOfInspection()
                );*/
                validateProcessIeAction(
                        req.getActionBy().longValue(), call.getPlaceOfInspection()
                );

            }else if (call.getTypeOfCall().equalsIgnoreCase("FINAL")) {

                validateFinalIeAction(
                        last.getWorkflowTransitionId(),
                        req.getActionBy()
                );
            }else if (last.getAssignedToUser() == null ||
                    !last.getAssignedToUser().equals(req.getActionBy())) {

                throw new InvalidInputException(
                        new ErrorDetails(
                                AppConstant.ACCESS_DENIED,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "You are not authorized to act on this inspection call."
                        )
                );
            }
            if (req.getAction().equalsIgnoreCase("CANCEL_CALL")) {
                return cancelCallByIE(current, req);
            }

            if (req.getAction().startsWith("IE_REQUEST_RESCHEDULE")
                    || req.getAction().startsWith("CM_APPROVE_RESCHEDULE")
                    || req.getAction().startsWith("CM_REJECT_RESCHEDULE")
                    || req.getAction().startsWith("CM_FORWARD_TO_SBU_HEAD")
                    || req.getAction().startsWith("SBU_HEAD_APPROVE_RESCHEDULE")
                    || req.getAction().startsWith("SBU_HEAD_REJECT_RESCHEDULE")) {

                TransitionMaster nextTransition = resolveConditionalTransition(current, req);

                WorkflowTransition next = createNextTransition(
                        current,
                        nextTransition,
                        req.getAction(),
                        req.getRemarks(),
                        req
                );

                // IE ASSIGNMENT LOGIC
                InspectionCall ic = inspectionCallRepository
                        .findByIcNumber(req.getRequestId())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Inspection Call not found"
                                )
                        ));

                String inspectionType = ic.getTypeOfCall();

                if ("PROCESS".equalsIgnoreCase(inspectionType)) {

                   // Integer processIeUserId = getProcessIeUserFromPoi(ic.getPlaceOfInspection(), last.getProcessIeUserId());

                  //  next.setAssignedToUser(processIeUserId);
                  //  next.setProcessIeUserId(processIeUserId);
                }
                else if ("FINAL".equalsIgnoreCase(inspectionType)) {

                    List<IePincodePoiMapping> ieMappings =
                            iePincodePoiMappingRepository
                                    .findByPoiCode(ic.getPlaceOfInspection());

                    for (IePincodePoiMapping mapping : ieMappings) {

                        UserMaster user =
                                userMasterRepository
                                        .findByEmployeeCode(mapping.getEmployeeCode());

                        FinalIeMapping finalMapping = new FinalIeMapping();
                        finalMapping.setWorkflowTransitionId(
                                next.getWorkflowTransitionId()
                        );
                        finalMapping.setIeUserId(user.getUserId());

                        finalIeMappingRepository.save(finalMapping);
                    }
                }
                else {

                    PincodePoIMapping poi =
                            pincodePoIMappingRepository
                                    .findByPoiCode(ic.getPlaceOfInspection())
                                    .orElseThrow(() -> new BusinessException(
                                            new ErrorDetails(
                                                    AppConstant.ERROR_CODE_RESOURCE,
                                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                    AppConstant.ERROR_TYPE_VALIDATION,
                                                    "Invalid POI code"
                                            )
                                    ));

                    String stage;
                    if (inspectionType.equalsIgnoreCase("Raw Material")) {
                        stage = "R";
                    } else if (inspectionType.equalsIgnoreCase("Process")) {
                        stage = "P";
                    } else {
                        stage = "F";
                    }

                    next.setAssignedToUser(
                            assignIE(
                                    poi.getPinCode(),
                                    "ERC",
                                    stage,
                                    ic.getPlaceOfInspection()
                            )
                    );
                }

                assignRescheduleUser(next, current, req);

                workflowTransitionRepository.save(next);
                return mapWorkflowTransition(next);
            }

            if(req.getAction().equalsIgnoreCase("CONFIRM_CANCEL_AFTER_PAYMENT")){
                return confirmCancelAfterPayment(current,req);
            }

            if ("ENTRY_INSPECTION_RESULTS".equalsIgnoreCase(req.getAction())) {

                InspectionCall ic =
                        inspectionCallRepository
                                .findByIcNumber(req.getRequestId())
                                .orElseThrow(() -> new BusinessException(
                                        new ErrorDetails(
                                                AppConstant.ERROR_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_VALIDATION,
                                                "Invalid IC code"
                                        )
                                ));

                if(ic.getTypeOfCall().equalsIgnoreCase("Process")) {
                    String swift = req.getShiftCode(); // Use shiftCode from request first

                    if (swift == null || swift.isBlank()) {
                        swift = current.getSwiftCode(); // Fallback to current transition swift
                    }

                    if (swift == null || swift.isBlank()) {
                       swift="G";
                    }

                    // Total allowed qty (from process_inspection_details)
                    int totalOfferedQty =
                            processInspectionDetailsRepository
                                    .findOfferedQtyByIcId(ic.getId());

                    if (totalOfferedQty <= 0) {
                        throw new BusinessException(
                                new ErrorDetails(
                                        AppConstant.INVALID_WORKFLOW_TRANSITION,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Offered quantity not available. Cannot enter inspection results."
                                )
                        );
                    }

                    // Already inspected qty (history sum)
//                int alreadyInspectedQty =
//                        processIeQtyRepository
//                                .sumInspectedQtyByRequestId(req.getRequestId());

                    //  Lot number from request
                    String lotNo = req.getLotNo();

// Already inspected qty for SAME request + SAME lot
                    int alreadyInspectedLotQty =
                            processIeQtyRepository
                                    .sumInspectedQtyByRequestIdAndLotNumber(
                                            req.getRequestId(),
                                            lotNo
                                    );


                    int lotOfferedQty =
                            processIeQtyRepository
                                    .findOfferedQtyByRequestIdAndLotNumber(
                                            req.getRequestId(),
                                            lotNo
                                    );

                    if (lotOfferedQty == 0) {
                        lotOfferedQty = req.getOfferedQty();
                    }

                    int newQty = req.getInspectedQty();

                    //  CORE VALIDATION
//                if (alreadyInspectedQty + newQty > totalOfferedQty) {
//                    throw new BusinessException(
//                            new ErrorDetails(
//                                    AppConstant.INVALID_WORKFLOW_TRANSITION,
//                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
//                                    AppConstant.ERROR_TYPE_VALIDATION,
//                                    "Entered quantity exceeds total offered quantity. " +
//                                            "Remaining qty: " + (totalOfferedQty - alreadyInspectedQty)
//                            )
//                    );
//                }
                    // LOT-WISE VALIDATION
                    if (alreadyInspectedLotQty + newQty > lotOfferedQty) {
                        throw new BusinessException(
                                new ErrorDetails(
                                        AppConstant.INVALID_WORKFLOW_TRANSITION,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Entered quantity exceeds offered quantity for Lot "
                                                + lotNo +
                                                ". Remaining qty: "
                                                + (lotOfferedQty - alreadyInspectedLotQty)
                                )
                        );
                    }


                    ProcessIeQty qty = new ProcessIeQty();
                    qty.setRequestId(req.getRequestId());
                    qty.setSwiftCode(swift);
                    qty.setIeUserId(req.getActionBy());
                    qty.setInspectedQty(newQty);
                    qty.setOfferedQty(lotOfferedQty);
                    qty.setManufactureQty(req.getManufacturedQty());
                    qty.setHeatNo(req.getHeatNo());
                    qty.setRejectedQty(req.getRejectedQty());
                    qty.setDateOfInspection(req.getDateOfInspection());

                    //qty.setOfferedQty(req.getOfferedQty());
                    qty.setLotNumber(req.getLotNo());
                    qty.setCompleted(false);

                    processIeQtyRepository.save(qty);


                    if ("PROCESS".equalsIgnoreCase(ic.getTypeOfCall())) {

//                        int totalOfferedQty =
//                                processInspectionDetailsRepository
//                                        .findOfferedQtyByIcId(ic.getId());

                        int inspectedQty =
                                processIeQtyRepository
                                        .sumInspectedQtyByRequestId(req.getRequestId());

                       // if (inspectedQty < totalOfferedQty) {

                            WorkflowTransition pause = createNextTransition(
                                    current,
                                    resolveConditionalTransition(current, req),
                                    "PAUSE_INSPECTION_RESUME_NEXT_DAY",
                                    "Partial inspection done. Remaining qty pending.",
                                    req
                            );

                            pause.setStatus("PAUSE_INSPECTION_RESUME_NEXT_DAY");
                            pause.setAction("PAUSE_INSPECTION_RESUME_NEXT_DAY");
                            pause.setJobStatus("PAUSED");
                            pause.setAssignedToUser(current.getProcessIeUserId());

                            workflowTransitionRepository.save(pause);
                            return mapWorkflowTransition(pause);
                       // }
                    }

                }
            }



            if(req.getActionBy().equals("INITIATE_INSPECTION")){
                // AUTO EXPIRE ANY PENDING QTY EDIT REQUEST
                WorkflowTransition pendingQty = workflowTransitionRepository
                        .findTopByRequestIdAndStatus(current.getRequestId(), "QTY_EDIT_REQUESTED");

                if (pendingQty != null) {
                    System.out.println("Qty edit request expired automatically because inspection started.");
                    // Optional: update status in DB
                    pendingQty.setStatus("QTY_EDIT_EXPIRED");
                    workflowTransitionRepository.save(pendingQty);
                }
            }
            // If action is NOT VERIFY_PO_DETAILS then check whether last action was VERIFY_PO_DETAILS
            if (req.getAction().equalsIgnoreCase("ENTER_SHIFT_DETAILS_AND_START_INSPECTION")) {

                if (last == null ||
                        (
                                !last.getAction().equalsIgnoreCase("VERIFY_PO_DETAILS") &&
                                        !last.getAction().equalsIgnoreCase("PAUSE_INSPECTION_RESUME_NEXT_DAY")
                        )
                ) {

                    throw new BusinessException(
                            new ErrorDetails(
                                    AppConstant.INVALID_WORKFLOW_TRANSITION,
                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Please verify PO details before proceeding."
                            )
                    );
                }


                // Auto-Set PoStatus OK
               req.setPoStatus("OK");
            }

            TransitionMaster nextTransition = resolveConditionalTransition(current, req);
            if (nextTransition == null) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "No transition could be resolved for action: " + req.getAction()
                ));
            }
            WorkflowTransition next = createNextTransition(
                    current,
                    nextTransition,
                    req.getAction() == null ? "DONE" : req.getAction(),
                    req.getRemarks(),
                    req
            );



            // IE ASSIGNMENT LOGIC
            InspectionCall im = inspectionCallRepository
                    .findByIcNumber(req.getRequestId())
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Inspection Call not found"
                            )
                    ));



            if (im != null && "PROCESS".equalsIgnoreCase(im.getTypeOfCall())) {

                // First swift
                if (current.getSwiftCode() == null) {

                    next.setSwiftCode("A");
                    next.setPrimarySwift(true);

                } else {

                    next.setSwiftCode(current.getSwiftCode());
                    next.setPrimarySwift(false);
                }
            }
            String inspectionType = im.getTypeOfCall();

            if ("PROCESS".equalsIgnoreCase(inspectionType)) {

               // Integer processIeUserId = getProcessIeUserFromPoi(im.getPlaceOfInspection(), last.getProcessIeUserId());

              //  next.setAssignedToUser(processIeUserId);
              //  next.setProcessIeUserId(processIeUserId);
            }
            else if ("FINAL".equalsIgnoreCase(inspectionType)) {

                workflowTransitionRepository.save(next);
                List<IePincodePoiMapping> ieMappings =
                        iePincodePoiMappingRepository
                                .findByPoiCode(im.getPlaceOfInspection());

                for (IePincodePoiMapping mapping : ieMappings) {

                    UserMaster user =
                            userMasterRepository
                                    .findByEmployeeCode(mapping.getEmployeeCode());

                    FinalIeMapping finalMapping = new FinalIeMapping();
                    finalMapping.setWorkflowTransitionId(
                            next.getWorkflowTransitionId()
                    );
                    finalMapping.setIeUserId(user.getUserId());

                    finalIeMappingRepository.save(finalMapping);
                }
            }
            else {

                PincodePoIMapping poi =
                        pincodePoIMappingRepository
                                .findByPoiCode(im.getPlaceOfInspection())
                                .orElseThrow(() -> new BusinessException(
                                        new ErrorDetails(
                                                AppConstant.ERROR_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_VALIDATION,
                                                "Invalid POI code"
                                        )
                                ));

                String stage;
                if (inspectionType.equalsIgnoreCase("Raw Material")) {
                    stage = "R";
                } else if (inspectionType.equalsIgnoreCase("Process")) {
                    stage = "P";
                } else {
                    stage = "F";
                }

                next.setAssignedToUser(
                        assignIE(
                                poi.getPinCode(),
                                "ERC",
                                stage,
                                im.getPlaceOfInspection()
                        )
                );
            }

            if (req.getAction().equalsIgnoreCase("VERIFY_MATERIAL_AVAILABILITY")
                    && "NO".equalsIgnoreCase(req.getMaterialAvailable())) {

                String paymentType = "PAYABLE";
                boolean paymentDone = true;

                if (paymentType.equalsIgnoreCase("NON_PAYABLE")) {
                    next.setStatus("CANCELLED");
                    next.setRemarks("Cancelled - Material Not Available");
                }
                else if (!paymentDone) {
                    next.setStatus("BLOCKED");
                    next.setRemarks("Blocked - Payment Pending + No Material");
                }
                else {
                    next.setStatus("CANCELLED");
                    next.setRemarks("Cancelled - Payment Done + No Material");
                }
            }
            if (req.getAction().equalsIgnoreCase("ENTRY_INSPECTION_RESULTS")
                    && "NOT OK".equalsIgnoreCase(req.getMaterialAvailable())) {

                if(req.getMaterialName().equalsIgnoreCase("Raw material") || req.getMaterialName().equalsIgnoreCase("Final")){
                    String paymentType = "PAYABLE";
                    boolean paymentDone = true;

                    if (paymentType.equalsIgnoreCase("PAYABLE") && paymentDone) {
                        next.setStatus(AppConstant.REJECT_TYPE);
                        next.setRemarks("Rejected - Material Not Available");
                    }
                    else if (!paymentDone) {
                        next.setStatus("BLOCKED");
                        next.setRemarks("Blocked - Payment Pending + No Material");
                    }
                    else {
                        next.setStatus(AppConstant.REJECT_TYPE);
                        next.setRemarks("Rejected - Payment Done + No Material");
                    }
                }

            }




            // If next role is CM Assign CM user
            //CHANGE HERE
            if (req.getAction().equalsIgnoreCase("REQUEST_CORRECTION_TO_CM") && roleNameById(nextTransition.getNextRoleId()).equalsIgnoreCase("Control Manager")) {

                Integer ieUserId = current.getAssignedToUser() != null
                        ? current.getAssignedToUser()
                        : req.getActionBy();

             //   Integer cmUserId = getCmUserFromIeUser(ieUserId);
                Optional<UserMaster> um = userMasterRepository.findByUserId(ieUserId);

                Integer cmUserId =null;
                if(um.isPresent()){
                    UserMaster u = um.get();
                    cmUserId =  getCmUserFromIeEmployeeCode(u.getEmployeeCode());
                }

//                if (cmUserId == null) {
//                    throw new BusinessException(
//                            new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
//                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
//                                    AppConstant.ERROR_TYPE_VALIDATION,
//                                    "No CM user found for IE cluster")
//                    );
//                }


                next.setAssignedToUser(cmUserId);
            }
            if (roleNameById(nextTransition.getCurrentRoleId()).equalsIgnoreCase("Control Manager")) {
                next.setAssignedToUser(current.getCreatedBy());
            }



            workflowTransitionRepository.save(next);

           // workflowTransitionRepository.save(next);

            /*SAVE WHEN INSPECTION IS COMPLETED */
            if ("INSPECTION_COMPLETE_CONFIRM".equalsIgnoreCase(next.getStatus())) {

                InspectionCall ic = inspectionCallRepository
                        .findByIcNumber(next.getRequestId())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Inspection Call not found"
                                )
                        ));

                if (ic != null && "PROCESS".equalsIgnoreCase(ic.getTypeOfCall())) {

                    ProcessIeQty qty = new ProcessIeQty();
                    qty.setRequestId(req.getRequestId());
                    qty.setSwiftCode(req.getShiftCode() != null ? req.getShiftCode() : "A");
                    qty.setIeUserId(req.getActionBy());
                    qty.setInspectedQty(req.getInspectedQty());
                    qty.setOfferedQty(req.getOfferedQty());
                    qty.setManufactureQty(req.getManufacturedQty());
                    qty.setHeatNo(req.getHeatNo());
                    qty.setRejectedQty(req.getRejectedQty());

                    //qty.setOfferedQty(req.getOfferedQty());
                    qty.setLotNumber(req.getLotNo());
                    qty.setCompleted(false);

                    processIeQtyRepository.save(qty);

                  /*  Long icId = ic.getId();

//                    Optional<ProcessInspectionDetails> list = processInspectionDetailsRepository.findByIcIdCals(icId);

                    int totalOfferedQty =
                            processInspectionDetailsRepository.findOfferedQtyByIcId(ic.getId());

//                    int totalOfferedQty = list.stream()
//                            .mapToInt(ProcessInspectionDetails::getOfferedQty)
//                            .sum();

                    int inspectedQty =
                            processIeQtyRepository
                                    .sumInspectedQtyByRequestId(next.getRequestId());

                    if (inspectedQty < totalOfferedQty) {

                        next.setStatus("PAUSE_INSPECTION_RESUME_NEXT_DAY");
                        next.setRemarks("Partial inspection done. Remaining qty pending.");

                        next.setAction("PAUSE_INSPECTION_RESUME_NEXT_DAY");
                        next.setJobStatus("PAUSED");
                        workflowTransitionRepository.save(next);
                       /* WorkflowTransition nextSwift =
                                createNextTransition(
                                        next,
                                        nextTransition,
                                        "ENTER_SHIFT_DETAILS",
                                        "Continue inspection in next shift",
                                        req
                                );

                        nextSwift.setSwiftCode(null);
                        nextSwift.setPrimarySwift(false);
                        nextSwift.setAssignedToUser(current.getProcessIeUserId());

                        workflowTransitionRepository.save(nextSwift);*/
                       // return mapWorkflowTransition(next);
                  //  }
                   // if (inspectedQty == totalOfferedQty) {

                        next.setStatus("INSPECTION_COMPLETE_CONFIRM");
                        next.setAction("INSPECTION_COMPLETE_CONFIRM");
                        next.setRemarks("Process inspection completed.");

                        workflowTransitionRepository.save(next);
                     //   return mapWorkflowTransition(next);
                    //}
                }

                UserMaster user = userMasterRepository
                        .findByUserId(next.getModifiedBy())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "User not found"
                                )
                        ));

                PincodePoIMapping poi =
                        pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection())
                                .orElseThrow(() -> new BusinessException(
                                        new ErrorDetails(
                                                AppConstant.ERROR_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_VALIDATION,
                                                "Invalid POI code"
                                        )
                                ));
                String stage = null;
                if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
                    stage ="R";
                }else if(ic.getTypeOfCall().equalsIgnoreCase("Process")){
                    stage="P";
                }else{
                    stage="F";
                }
                String product ="ERC";
                String pinCode = poi.getPinCode();

                IEFieldsMapping mapping =
                        ieFieldsMappingRepository
                                .findByPinCodeProductAndStageMatch(pinCode, product, stage)
                                .orElseThrow(() -> new BusinessException(
                                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                AppConstant.ERROR_TYPE_VALIDATION,
                                                "No IE mapping for given pin/product/stage")));

                String  rio =mapping.getRio();
                InspectionCompleteDetails details = new InspectionCompleteDetails();

                //  Call No
                details.setCallNo(ic.getIcNumber());

                //  PO No
                details.setPoNo(ic.getPoNo());

                details.setCertificateNo(generateCertificateNo(rio, ic.getIcNumber(),user.getShortName()));

                details.setCreatedOn(LocalDateTime.now());

               inspectionCompleteDetailsRepository.save(details);
            }



            return mapWorkflowTransition(next);

        }






        if (current.getAssignedToUser() != null &&
                current.getNextRoleName().equalsIgnoreCase("RIO Help Desk")) {

            if (!current.getAssignedToUser().equals(req.getActionBy())) {

                throw new InvalidInputException(
                        new ErrorDetails(
                                AppConstant.ACCESS_DENIED,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "You are not authorized to act on this inspection call."
                        )
                );
            }
        }



        String action = req.getAction().toUpperCase();

        switch (action) {

            case "VERIFY":
                return verifyCall(current, req);

            case "RETURN_TO_VENDOR":
                return returnToVendor(current, req);

            case "FIX_ROUTING":
                return fixRouting(current, req);
            case "CREATE_CALL":
                return vendorResubmit(current, req);

            case "CM_RETURN_TO_IE":
                return cmReturnToIe(current, req);
            case "PAYMENT_VERIFICATION":
                return handleFinancePaymentVerification(current, req);

            case "PARKED_PAYMENT_NOT_RECEIVED":
                return handlePaymentNotReceived(current, req);
            case "CALL_REGISTERED":
                return handlePaymentReceived(current, req);
            case "PAYMENT_CONFIRMED_CANCEL":   // when po verification stage try cancle but blocked due payment then use this trastion to cancle
                return confirmCancelAfterPayment(current, req);
            case "REQUEST_QTY_EDIT":
                return requestQtyEdit(current, req);   //if vendor want to request for qty chnage of IC

            case "CM_QTY_DECISION":
                return cmQtyDecision(current, req);  // cm decision after vendor request for qty change of IC




            default:
                throw new InvalidInputException(new ErrorDetails(AppConstant.INVALID_WORKFLOW_TRANSITION, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION, "Invalid action:" +action));

        }
    }

    private String generateCertificateNo(String rioName,
                                         String callNo,
                                         String userShortName) {


        String rioFirstLetter = (rioName != null && !rioName.isEmpty())
                ? rioName.substring(0, 1).toUpperCase()
                : "X";

        return rioFirstLetter
                + "/" + callNo
                + "/" + userShortName.toUpperCase();
    }


    private WorkflowTransitionDto confirmCancelAfterPayment(WorkflowTransition current, TransitionActionReqDto req) {

        TransitionMaster t =
                transitionMasterRepository.findByTransitionName("CONFIRM_CANCEL_AFTER_PAYMENT");

        WorkflowTransition next = createNextTransition(
                current,
                t,
                "CANCELLED",
                "Payment received - Call fully cancelled",
                req
        );

        next.setJobStatus("CANCELLED");

        workflowTransitionRepository.save(next);
        return mapWorkflowTransition(next);
    }



    private WorkflowTransitionDto cancelCallByIE(WorkflowTransition current, TransitionActionReqDto req) {
    System.out.print(current);
        if (!roleNameById(Integer.valueOf(current.getNextRole()))
                .equalsIgnoreCase("IE") && !roleNameById(Integer.valueOf(current.getNextRole()))
                .equalsIgnoreCase("Process IE")) {
            throw new InvalidInputException(
                    new ErrorDetails(AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Only IE can cancel the call")
            );
        }

       /* String paymentType = req.getPaymentType();
        boolean paymentCompleted =
                req.getPaymentCompleted() != null &&
                        req.getPaymentCompleted().equalsIgnoreCase("true");
*/
        String paymentType = "PAYABLE";
        Boolean paymentCompleted = false;

        String status;
        String jobStatus;

        if ("NON_PAYABLE".equalsIgnoreCase(paymentType)) {
            status = "CANCELLED";
            jobStatus = "CANCELLED";
        }
        else if ("PAYABLE".equalsIgnoreCase(paymentType) && !paymentCompleted) {
            status = "BLOCKED";
            jobStatus = "BLOCKED";
        }
        else { // PAYABLE + Completed
            status = "CANCELLED";
            jobStatus = "CANCELLED";
        }


        TransitionMaster transition =
                transitionMasterRepository.findByTransitionNameAndWorkflowId(
                        "CANCEL_CALL",
                        current.getWorkflowId()
                );

        WorkflowTransition next = createNextTransition(
                current,
                transition,
                status,
                req.getRemarks() == null ? "Call cancelled by IE" : req.getRemarks(),
                req
        );

        next.setJobStatus(jobStatus);

        workflowTransitionRepository.save(next);
        return mapWorkflowTransition(next);
    }

private WorkflowTransitionDto verifyCall(WorkflowTransition current, TransitionActionReqDto req) {

    TransitionMaster verifyTransition =
            transitionMasterRepository.findByTransitionName("VERIFY_CALL");

    WorkflowTransition verified = createNextTransition(
            current, verifyTransition, "VERIFIED", "Inspection Call Verified", req
    );
    workflowTransitionRepository.save(verified);


    Optional<InspectionCall> i = inspectionCallRepository.findByIcNumber(req.getRequestId());
    InspectionCall insp = null;
    if(i.isPresent()){
        insp = i.get();
    }


    String inspectionType =insp.getTypeOfCall();
   // String  inspectionType ="PROCESS";
   // String inspectionType ="PROCESS";
    // Get actual inspection type
  //  String inspectionType = req.getInspectionType(); // RAW_MATERIAL / FINAL / PROCESS

    // RAW MATERIAL → Send to Finance
  /*  if ("Raw Material".equalsIgnoreCase(inspectionType)) {

        TransitionMaster paymentVerifyTransition =
                transitionMasterRepository.findByTransitionName("PAYMENT_VERIFICATION");

        WorkflowTransition financeStep = createNextTransition(
                verified,
                paymentVerifyTransition,
                "PAYMENT_VERIFICATION",
                "Awaiting Finance Verification",
                req
        );

      //  financeStep.setAssignedToUser(getFinanceUserForRio(current.getCreatedBy()));
        financeStep.setAssignedToUser(1);
        workflowTransitionRepository.save(financeStep);

        return mapWorkflowTransition(financeStep);
    }*/

    // FINAL / PROCESS → direct CALL_REGISTERED
    TransitionMaster callRegTransition =
            transitionMasterRepository.findByTransitionName("CALL_REGISTERED");

    WorkflowTransition callReg = createNextTransition(
            verified,
            callRegTransition,
            "CALL_REGISTERED",
            "Call Registered",
            req
    );

    if ("PROCESS".equalsIgnoreCase(inspectionType)) {

        // First time PROCESS IE assignment → use the Process IE user ID
      //  Integer processIeUserId = getProcessIeUserFromPoi(insp.getPlaceOfInspection(), 0);

      //  callReg.setAssignedToUser(processIeUserId);
      //  callReg.setProcessIeUserId(processIeUserId);

    }

    else {

        InspectionCall ic = inspectionCallRepository.findByIcNumber(req.getRequestId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Place of Inspection (POI) is not assigned"
                        )
                ));

      //  PincodePoIMapping poi = pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection());


        PincodePoIMapping poi =
                pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Invalid POI code"
                                )
                        ));
        String stage = null;
        if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
            stage ="R";
        }else if(ic.getTypeOfCall().equalsIgnoreCase("Process")){
            stage="P";
        }else{
            stage="F";
        }


        // RAW / FINAL → normal IE assignment
       // callReg.setAssignedToUser(assignIE(req.getPincode()));

        callReg.setAssignedToUser(assignIE(poi.getPinCode(),"ERC",stage,ic.getPlaceOfInspection() ));
    }


   // callReg.setAssignedToUser(assignIE(req.getPincode()));
    workflowTransitionRepository.save(callReg);

     if("Final".equalsIgnoreCase(inspectionType)) {


        //  Fetch IE mappings using POI code
        List<IePincodePoiMapping> ieMappings =
                iePincodePoiMappingRepository.findByPoiCode(insp.getPlaceOfInspection());

        for (IePincodePoiMapping mapping : ieMappings) {



            String employeeCode = mapping.getEmployeeCode();

            // Fetch userId using employeeCode
            UserMaster userOpt =
                    userMasterRepository.findByEmployeeCode(employeeCode);

            Integer userId = userOpt.getUserId();

            //  Save into FINAL_IE_MAPPING
            FinalIeMapping finalMapping = new FinalIeMapping();
            finalMapping.setWorkflowTransitionId(
                    callReg.getWorkflowTransitionId()
            );
            finalMapping.setIeUserId(userId);

            finalIeMappingRepository.save(finalMapping);

        }
    }

    return mapWorkflowTransition(callReg);
}




    private WorkflowTransitionDto returnToVendor(WorkflowTransition current, TransitionActionReqDto req) {

        TransitionMaster rejectTransition = transitionMasterRepository
                .findByTransitionName("RETURN_TO_VENDOR");

        WorkflowTransition next = createNextTransition(current, rejectTransition, "RETURNED",
                req.getRemarks(), req);
        WorkflowTransition vendorCreated =
                workflowTransitionRepository.findByStatusRequestIdAndCurrentRoleName("Created", req.getRequestId(), "Vendor");
        next.setAssignedToUser(vendorCreated.getCreatedBy());

        return mapWorkflowTransition(next);
    }

    private WorkflowTransitionDto fixRouting(WorkflowTransition current, TransitionActionReqDto req) {

        TransitionMaster fixRoutingTransition = transitionMasterRepository
                .findByTransitionName("FIX_ROUTING");

        WorkflowTransition last =
                workflowTransitionRepository.findTopByRequestIdOrderByWorkflowTransitionIdDesc(req.getRequestId());


        WorkflowTransition next = createNextTransition(current, fixRoutingTransition, "ROUTED_CORRECTION",
                "Routing corrected & forwarded to correct RIO", req);
        next.setAssignedToUser(req.getAssignUserId());
        next.setRio(req.getRioRouteChange());
        next.setWorkflowSequence(last.getWorkflowSequence()+1);
        return mapWorkflowTransition(next);
    }

    private WorkflowTransitionDto vendorResubmit(WorkflowTransition current, TransitionActionReqDto req) {

        WorkflowTransition last = workflowTransitionRepository
                .findTopByRequestIdOrderByWorkflowTransitionIdDesc(current.getRequestId());

        if (last.getAssignedToUser() == null ||
                !last.getAssignedToUser().equals(req.getActionBy())) {// Only assigned IE can act

            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.ACCESS_DENIED,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "You are not authorized to act on this inspection call."
                    )
            );
        }
        TransitionMaster transition = transitionMasterRepository
                .findByTransitionName(req.getAction());

        if (transition == null) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "transition not defined"));
        }

        WorkflowTransition next = new WorkflowTransition();
        next.setWorkflowId(current.getWorkflowId());
        next.setTransitionId(transition.getTransitionId());
        next.setRequestId(current.getRequestId());
        next.setStatus(AppConstant.Vendor_ReSubmitted);
        next.setAction(AppConstant.APPROVE_TYPE);
        next.setRemarks(req.getRemarks() == null ? "Resubmitted after correction" : req.getRemarks());
        //next.setCreatedBy(req.getActionBy());
        next.setCreatedBy(current.getCreatedBy());
        next.setModifiedBy(req.getActionBy());
        next.setCreatedDate(new Date());
        //  next.setCurrentRole(String.valueOf(transition.getCurrentRoleId()));
        //  next.setNextRole(String.valueOf(transition.getNextRoleId()));
        next.setCurrentRoleName(roleNameById(transition.getCurrentRoleId()));
        next.setNextRole(String.valueOf(transition.getNextRoleId()));
        next.setCurrentRole(String.valueOf(transition.getCurrentRoleId()));
        next.setNextRoleName(roleNameById(transition.getNextRoleId()));

        next.setJobStatus("IN_PROGRESS");
       /* PincodeCluster cluster = pincodeClusterRepository.findByPincode(req.getPincode())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "No cluster found for pincode")));

        // Step 2: Get region for that cluster
        RegionCluster region = regionClusterRepository.findByClusterName(cluster.getClusterName())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "No region found for cluster")
                ));

        // Step 3: Find RIO user for that region
        ClusterRioUser rioUser = clusterRioUserRepository.findByClusterName(cluster.getClusterName())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "No RIO user found for region")
                ));

        Integer assignedRioUserId = rioUser.getRioUserId();

        next.setAssignedToUser(assignedRioUserId);*/
        InspectionCall ic = inspectionCallRepository.findByIcNumber(req.getRequestId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Place of Inspection (POI) is not assigned"
                        )
                ));

        //  PincodePoIMapping poi = pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection());


        PincodePoIMapping poi =
                pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Invalid POI code"
                                )
                        ));
        String stage = null;
        if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
            stage ="R";
        }else if(ic.getTypeOfCall().equalsIgnoreCase("Process")){
            stage="P";
        }else{
            stage="F";
        }
        String product ="ERC";
        String pinCode = poi.getPinCode();

        IEFieldsMapping mapping =
                ieFieldsMappingRepository
                        .findByPinCodeProductAndStageMatch(pinCode, product, stage)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "No IE mapping for given pin/product/stage")));

         String  rio =mapping.getRio();

         next.setRio(rio);

        next.setWorkflowSequence(last.getWorkflowSequence()+1);

        workflowTransitionRepository.save(next);

        return mapWorkflowTransition(next);
    }

    private TransitionMaster resolveConditionalTransition(
            WorkflowTransition current,
            TransitionActionReqDto req
    ) {

        TransitionMaster previous =
                transitionMasterRepository.findById(current.getTransitionId())
                        .orElseThrow(() ->
                                new BusinessException(new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Previous transition not found"
                                ))
                        );


        System.out.println("DEBUG prevTransitionId=" + previous.getTransitionId()
                + ", name=" + previous.getTransitionName()
                + ", currRole=" + previous.getCurrentRoleId()
                + ", nextRole=" + previous.getNextRoleId()
                + ", uiAction=" + req.getAction());

        if (Objects.equals(previous.getCurrentRoleId(), previous.getNextRoleId())) {


            // We only use CURRENT_ACTION = UI action, for the SAME role.

            List<TransitionMaster> actionTransitions =
                    transitionMasterRepository.findByWorkflowId(current.getWorkflowId())
                            .stream()
                            // same actor role
                            .filter(t -> Objects.equals(t.getCurrentRoleId(), previous.getNextRoleId()))
                            // CURRENT_ACTION must match the action user performed
                            .filter(t -> t.getCurrentAction() != null &&
                                    t.getCurrentAction().equalsIgnoreCase(req.getAction()))
                            .toList();

            if (actionTransitions.isEmpty()) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.INVALID_WORKFLOW_TRANSITION,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "No transition found for CURRENT_ACTION: " + req.getAction()
                ));
            }

            // Normally only one row will match → use first
            return actionTransitions.get(0);
        }

        Integer currentRoleId = previous.getNextRoleId();

        List<TransitionMaster> transitions =
                transitionMasterRepository
                        .findByWorkflowId(current.getWorkflowId())
                        .stream()
                        .filter(t -> Objects.equals(t.getCurrentRoleId(), currentRoleId))
                        .toList();

        for (TransitionMaster t : transitions) {

            if (t.getConditionId() != null) {
                TransitionConditionMaster cond =
                        transitionConditionMasterRepository.findById(t.getConditionId()).orElse(null);

                if (cond != null && validateCondition(cond, req)) {
                    return t;
                }
            }
        }

        // No match found
        throw new BusinessException(new ErrorDetails(
                AppConstant.INVALID_WORKFLOW_TRANSITION,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                "No matching transition found for action " + req.getAction()
        ));
    }




    private boolean validateCondition(TransitionConditionMaster cond, TransitionActionReqDto req) {

        switch (cond.getConditionKey()) {

            case "PO_STATUS":
                return req.getPoStatus() != null &&
                        req.getPoStatus().equalsIgnoreCase(cond.getConditionValue());

            case "CM_DECISION":
                return req.getCmDecision() != null &&
                        req.getCmDecision().equalsIgnoreCase(cond.getConditionValue());
            case "SBU_HEAD_DECISION":
                return req.getSbuHeadDecision() != null &&
                        req.getSbuHeadDecision().equalsIgnoreCase(cond.getConditionValue());

            case "PAYMENT_TYPE":
                return req.getPaymentType() != null &&
                        req.getPaymentType().equalsIgnoreCase(cond.getConditionValue());

            case "MATERIAL_AVAILABLE":
                return req.getMaterialAvailable() != null &&
                        req.getMaterialAvailable().equalsIgnoreCase(cond.getConditionValue());

            case "RESULT_STATUS":
                return req.getResultStatus() != null &&
                        req.getResultStatus().equalsIgnoreCase(cond.getConditionValue());

            case "CM_FINAL_APPROVAL":
                return req.getCmFinalApproval() != null &&
                        req.getCmFinalApproval().equalsIgnoreCase(cond.getConditionValue());
        }
        return false;
    }


    private WorkflowTransition createNextTransition(
            WorkflowTransition current,
            TransitionMaster transition,
            String status,
            String remarks,
            TransitionActionReqDto req
    ) {
        WorkflowTransition last =
                workflowTransitionRepository.findTopByRequestIdOrderByWorkflowTransitionIdDesc(req.getRequestId());
System.out.print(last);
        WorkflowTransition next = new WorkflowTransition();
        next.setWorkflowId(current.getWorkflowId());
        next.setTransitionId(transition.getTransitionId());
       // next.setTransitionOrder(transition.getTransitionOrder());
        next.setRequestId(current.getRequestId());
        next.setStatus(status);
        next.setAction(req.getAction());
        next.setRemarks(remarks);
        next.setCreatedDate(new Date());
     //   next.setCreatedBy(req.getActionBy());
        next.setCreatedBy(current.getCreatedBy());
        next.setModifiedBy(req.getActionBy());
      //  String inspectionType = "PROCESS";
     //   String inspectionType ="Raw Material";

        InspectionCall ic = inspectionCallRepository
                .findByIcNumber(req.getRequestId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Inspection Call not found"
                        )
                ));

        String inspectionType = ic.getTypeOfCall();
        //PROCESS IE ROLE OVERRIDE
        if (inspectionType != null
                && inspectionType.equalsIgnoreCase("PROCESS")) {

            Integer processIeRoleId = 7; //  ROLE ID FOR PROCESS IE

            // If current role is IE (3), override to Process IE
            if (transition.getCurrentRoleId()!= null && transition.getCurrentRoleId() == 3) {
                next.setCurrentRole(String.valueOf(processIeRoleId));
                next.setCurrentRoleName(roleNameById(processIeRoleId));
            }else{
                next.setCurrentRoleName(roleNameById(transition.getCurrentRoleId()));
                next.setCurrentRole(String.valueOf(transition.getCurrentRoleId()));
            }
            // If next role is IE (3), override to Process IE
            if (transition.getNextRoleId() != null && transition.getNextRoleId() == 3) {
                next.setNextRole(String.valueOf(processIeRoleId));
                next.setNextRoleName(roleNameById(processIeRoleId));
            }else{
                next.setNextRole(String.valueOf(transition.getNextRoleId()));
                next.setNextRoleName(roleNameById(transition.getNextRoleId()));
            }
        }else{
            next.setCurrentRoleName(roleNameById(transition.getCurrentRoleId()));
            next.setNextRole(String.valueOf(transition.getNextRoleId()));
            next.setCurrentRole(String.valueOf(transition.getCurrentRoleId()));
            next.setNextRoleName(roleNameById(transition.getNextRoleId()));
        }

        if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
            next.setAssignedToUser(current.getAssignedToUser());
        }else if(ic.getTypeOfCall().equalsIgnoreCase("Process")){
            next.setProcessIeUserId(current.getProcessIeUserId());
        }else{
            next.setAssignedToUser(null);
        }


//        if(inspectionType.equalsIgnoreCase("PROCESS")){
//            next.setProcessIeUserId(current.getProcessIeUserId());
//        }
        next.setJobStatus(determineJobStatus(req.getAction()));
        next.setWorkflowSequence(last.getWorkflowSequence()+1);
     //   workflowTransitionRepository.save(next);

        return next;
    }
/*
    private Integer assignIE(String pincode) {

        //  Get Cluster
        PincodeCluster cluster = pincodeClusterRepository.findByPincode(pincode)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Cluster not found for pincode")));

        String clusterName = cluster.getClusterName();

        // Always assign primary IE if available (no workload check)
        Integer primaryIe = getPrimaryIeForCluster(clusterName);
        if (primaryIe != null) {
            return primaryIe;
        }

        //  If no primary IE → choose best secondary IE
        Integer secondaryIe = getSecondaryIeForCluster(clusterName);
        if (secondaryIe != null) {
            return secondaryIe;
        }

        throw new BusinessException(
                new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "No IE available for assignment")
        );
    }*/
private Integer assignIE(
        String pinCode,
        String product,
        String stage,
        String poiCode) {

    // 1. Validate Pin + Product + Stage → RIO
//    IEFieldsMapping mapping =
//            ieFieldsMappingRepository
//                    .findByPinCodeAndProductAndStage(pinCode, product, stage)
//                    .orElseThrow(() -> new BusinessException(
//                            new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
//                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
//                                    AppConstant.ERROR_TYPE_VALIDATION,
//                                    "No IE mapping for given pin/product/stage")));

    System.out.print(pinCode+" "+product +" "+ stage +" "+poiCode);

    IEFieldsMapping mapping =
            ieFieldsMappingRepository
                    .findByPinCodeProductAndStageMatch(pinCode, product, stage)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "No IE mapping for given pin/product/stage")));


    String rio = mapping.getRio();
     // Validate POI
    boolean poiValid = pincodePoIMappingRepository
            .existsByPinCodeAndPoiCode(pinCode, poiCode);

    if (!poiValid) {
        throw new BusinessException(
                new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "Invalid POI for pin code")
        );
    }

    // 3. Try PRIMARY IE
//    Optional<Integer> primaryIe =
//            iePincodePoiMappingRepository
//                    .findPrimaryIe(pinCode, product, poiCode);
//
//    if (primaryIe.isPresent()) {
//        return primaryIe.get();
//    }
//
//    // 4. Try SECONDARY IE
//    Optional<Integer> secondaryIe =
//            iePincodePoiMappingRepository
//                    .findSecondaryIe(pinCode, product, poiCode, rio);
//
//    if (secondaryIe.isPresent()) {
//        return secondaryIe.get();
//    }
    System.out.println(pinCode);
    System.out.println(product);
    System.out.println(poiCode);

    Optional<String> primaryIe =  iePincodePoiMappingRepository.findPrimaryIe(pinCode, product, poiCode);
    if (primaryIe.isPresent()) {
        UserMaster um = userMasterRepository.findByEmployeeCode(primaryIe.get());
       return um.getUserId();
    }
    Optional<String> secondaryIe = iePincodePoiMappingRepository.findSecondaryIe(pinCode, product, poiCode);

    if (secondaryIe.isPresent()) {
        UserMaster um = userMasterRepository.findByEmployeeCode(secondaryIe.get());
        return um.getUserId();
    }

    // 5. No IE
    throw new BusinessException(
            new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION,
                    "No IE available for assignment")
    );
}


    private int getIeWorkload(Integer ieUserId) {
        return workflowTransitionRepository.countActiveCallsForIE(ieUserId);
    }

    private Integer getPrimaryIeForCluster(String clusterName) {

        ClusterPrimaryIe primary = clusterPrimaryIeRepository
                .findByClusterName(clusterName)
                .orElse(null);

        return primary != null ? primary.getIeUserId() : null;
    }

    private Integer getSecondaryIeForCluster(String clusterName) {

        List<ClusterSecondaryIe> list =
                clusterSecondaryIeRepository.findByClusterNameOrderByPriorityOrderAsc(clusterName);

        if (list.isEmpty()) return null;

        Integer bestIe = null;
        int minWorkload = Integer.MAX_VALUE;

        for (ClusterSecondaryIe ie : list) {
            int workload = getIeWorkload(ie.getIeUserId());
            if (workload < minWorkload) {
                minWorkload = workload;
                bestIe = ie.getIeUserId();
            }
        }

        return bestIe;
    }




    private WorkflowTransitionDto cmReturnToIe(WorkflowTransition current, TransitionActionReqDto req) {

       /* if (req.getNextIeUserId() == null) {
            throw new InvalidInputException(
                    new ErrorDetails(
                            AppConstant.INVALID_WORKFLOW_TRANSITION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "IE user ID required when CM returns IC"
                    )
            );
        }*/

        WorkflowTransition last =
                        workflowTransitionRepository.findTopByRequestIdOrderByWorkflowTransitionIdDesc(req.getRequestId());

                TransitionMaster transition = transitionMasterRepository
                .findByTransitionName("CM_RETURN_TO_IE");

        WorkflowTransition next = new WorkflowTransition();
        next.setWorkflowId(current.getWorkflowId());
        next.setTransitionId(transition.getTransitionId());
        next.setRequestId(current.getRequestId());
        next.setStatus("RETURNED_BY_CM");
        next.setAction("CM_RETURN_TO_IE");
        next.setRemarks(req.getRemarks());
        //next.setCreatedBy(req.getActionBy());
        next.setCreatedBy(current.getCreatedBy());
        next.setModifiedBy(req.getActionBy());
        next.setCreatedDate(new Date());
      //  next.setCurrentRoleName("CM");
      //  next.setNextRoleName("IE");
      //  next.setCurrentRole("4"); // CM
      //  next.setNextRole("3");    // IE
        next.setCurrentRoleName(roleNameById(transition.getCurrentRoleId()));
        next.setNextRole(String.valueOf(transition.getNextRoleId()));
        next.setCurrentRole(String.valueOf(transition.getCurrentRoleId()));
        next.setNextRoleName(roleNameById(transition.getNextRoleId()));
        next.setAssignedToUser(req.getAssignUserId()); // <-- assigns to secondary selected  by cm

        next.setJobStatus("ASSIGNED");
        next.setWorkflowSequence(last.getWorkflowSequence());

        workflowTransitionRepository.save(next);

        return mapWorkflowTransition(next);
    }


    private String roleNameById(Integer roleId) {
        if (Objects.nonNull(roleId)) {
            return roleMasterRepository.findById(roleId).orElse(new RoleMaster()).getRoleName();
        } else {
            return null;
        }
    }

//     private WorkflowTransitionDto mapWorkflowTransition(WorkflowTransition wt) {

//         Optional<InspectionCall> ic = inspectionCallRepository.findByIcNumber(wt.getRequestId());

//         InspectionCall i =null;
//         if(ic.isPresent()){
//             i = ic.get();
//         }
//         WorkflowTransitionDto dto = new WorkflowTransitionDto();
//         if(wt.getProcessIeUserId()!= null) {
//             int processIe = wt.getProcessIeUserId();
//             String poi = i.getPlaceOfInspection();

//             List<Integer> ieUsers = null;

//             ieUsers = getIeUsersByProcessIeAndPoi(processIe, poi);


//             ieUsers.add(processIe);
//             dto.setProcessIes(ieUsers);
//         }

//         if (i != null && "Final".equalsIgnoreCase(i.getTypeOfCall())) {

//             List<FinalIeMapping> mappings =
//                     finalIeMappingRepository
//                             .findByWorkflowTransitionId(wt.getWorkflowTransitionId());

//             List<Integer> finalIes = mappings.stream()
//                     .map(FinalIeMapping::getIeUserId)
//                     .collect(Collectors.toList());

//             dto.setFinalIes(finalIes);
//         }

//         dto.setWorkflowTransitionId(wt.getWorkflowTransitionId());
//         dto.setWorkflowId(wt.getWorkflowId());
//         dto.setTransitionId(wt.getTransitionId());
//         dto.setRequestId(wt.getRequestId());
//         dto.setStatus(wt.getStatus());
//         dto.setAction(wt.getAction());
//         //dto.setAction(wt.getStatus());
//         dto.setRemarks(wt.getRemarks());
//         dto.setCreatedBy(wt.getCreatedBy());
//         dto.setCreatedDate(wt.getCreatedDate());
//         dto.setCurrentRole(wt.getCurrentRole());
//         dto.setNextRole(wt.getNextRole());
//         dto.setAssignedToUser(wt.getAssignedToUser());
//         dto.setWorkflowSequence(wt.getWorkflowSequence());
//         dto.setModifiedBy(wt.getModifiedBy());
//         dto.setRio(wt.getRio());


//         if(ic.isPresent()){
//             dto.setPoNo(i.getPoNo());
//             dto.setVendorName(i.getVendorId());
//             dto.setProductType(i.getTypeOfCall());
//             dto.setDesiredInspectionDate(String.valueOf(i.getDesiredInspectionDate()));

//                 // Compute inspection date range from workflow transitions for this request
//                 try {
//                         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
//                         List<WorkflowTransition> transitions = workflowTransitionRepository.findByRequestId(wt.getRequestId());

//                         java.util.Set<String> startStatuses = new java.util.HashSet<>(java.util.Arrays.asList("INITIATE_INSPECTION", "IE_SCHEDULED", "INSPECTION_IN_PROGRESS"));
//                         java.util.Set<String> endStatuses = new java.util.HashSet<>(java.util.Arrays.asList("INSPECTION_COMPLETE_CONFIRM", "INSPECTION_COMPLETE"));

//                         java.util.Date start = transitions.stream()
//                                 .filter(t -> t.getStatus() != null && startStatuses.contains(t.getStatus().toUpperCase()))
//                                 .map(WorkflowTransition::getCreatedDate)
//                                 .min(java.util.Date::compareTo)
//                                 .orElse(null);

//                         java.util.Date end = transitions.stream()
//                                 .filter(t -> t.getStatus() != null && endStatuses.contains(t.getStatus().toUpperCase()))
//                                 .map(WorkflowTransition::getCreatedDate)
//                                 .max(java.util.Date::compareTo)
//                                 .orElse(null);

//                         if (start != null && end != null) {
//                                 dto.setInspectionDate(sdf.format(start) + " - " + sdf.format(end));
//                         } else if (start != null) {
//                                 dto.setInspectionDate(sdf.format(start));
//                         }
//                 } catch (Exception ex) {
//                         // ignore
//                 }
//         }
//       //  dto.setTransitionOrder(wt.getTransitionOrder());
//         return dto;
//     }

//     /**
//      * Optimized version of mapWorkflowTransition that uses pre-fetched data
//      * to avoid N+1 query problem
//      */
//     private WorkflowTransitionDto mapWorkflowTransitionOptimized(
//             WorkflowTransition wt,
//             Map<String, InspectionCall> inspectionCallMap,
//             Map<Integer, List<Integer>> finalIeMappings) {

//         InspectionCall i = inspectionCallMap.get(wt.getRequestId());

//         WorkflowTransitionDto dto = new WorkflowTransitionDto();

//         // Handle Process IE mappings (skip for now to avoid additional queries)
//         if (wt.getProcessIeUserId() != null && i != null) {
//             int processIe = wt.getProcessIeUserId();
//             String poi = i.getPlaceOfInspection();

//             try {
//                 List<Integer> ieUsers = getIeUsersByProcessIeAndPoi(processIe, poi);
//                 ieUsers.add(processIe);
//                 dto.setProcessIes(ieUsers);
//             } catch (Exception e) {
//                 // Silently handle errors to avoid breaking the entire list
//                 dto.setProcessIes(Collections.singletonList(processIe));
//             }
//         }

//         // Handle Final IE mappings using pre-fetched data
//         if (i != null && "Final".equalsIgnoreCase(i.getTypeOfCall())) {
//             List<Integer> finalIes = finalIeMappings.getOrDefault(
//                     wt.getWorkflowTransitionId(),
//                     Collections.emptyList()
//             );
//             dto.setFinalIes(finalIes);
//         }

//         // Map basic fields
//         dto.setWorkflowTransitionId(wt.getWorkflowTransitionId());
//         dto.setWorkflowId(wt.getWorkflowId());
//         dto.setTransitionId(wt.getTransitionId());
//         dto.setRequestId(wt.getRequestId());
//         dto.setStatus(wt.getStatus());
//         dto.setAction(wt.getAction());
//         dto.setRemarks(wt.getRemarks());
//         dto.setCreatedBy(wt.getCreatedBy());
//         dto.setCreatedDate(wt.getCreatedDate());
//         dto.setCurrentRole(wt.getCurrentRole());
//         dto.setNextRole(wt.getNextRole());
//         dto.setAssignedToUser(wt.getAssignedToUser());
//         dto.setWorkflowSequence(wt.getWorkflowSequence());
//         dto.setModifiedBy(wt.getModifiedBy());
//         dto.setRio(wt.getRio());

//         // Map inspection call data using pre-fetched data
//         if (i != null) {
//             dto.setPoNo(i.getPoNo());
//             dto.setVendorName(i.getVendorId());
//             dto.setProductType(i.getTypeOfCall());
//             dto.setDesiredInspectionDate(String.valueOf(i.getDesiredInspectionDate()));
//                         // Compute inspection date range from workflow transitions for this request
//                         try {
//                                 java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
//                                 List<WorkflowTransition> transitions = workflowTransitionRepository.findByRequestId(wt.getRequestId());

//                                 java.util.Set<String> startStatuses = new java.util.HashSet<>(java.util.Arrays.asList("INITIATE_INSPECTION", "IE_SCHEDULED", "INSPECTION_IN_PROGRESS"));
//                                 java.util.Set<String> endStatuses = new java.util.HashSet<>(java.util.Arrays.asList("INSPECTION_COMPLETE_CONFIRM", "INSPECTION_COMPLETE"));

//                                 java.util.Date start = transitions.stream()
//                                                 .filter(t -> t.getStatus() != null && startStatuses.contains(t.getStatus().toUpperCase()))
//                                                 .map(WorkflowTransition::getCreatedDate)
//                                                 .min(java.util.Date::compareTo)
//                                                 .orElse(null);

//                                 java.util.Date end = transitions.stream()
//                                                 .filter(t -> t.getStatus() != null && endStatuses.contains(t.getStatus().toUpperCase()))
//                                                 .map(WorkflowTransition::getCreatedDate)
//                                                 .max(java.util.Date::compareTo)
//                                                 .orElse(null);

//                                 if (start != null && end != null) {
//                                         dto.setInspectionDate(sdf.format(start) + " - " + sdf.format(end));
//                                 } else if (start != null) {
//                                         dto.setInspectionDate(sdf.format(start));
//                                 }
//                         } catch (Exception ex) {
//                                 // ignore
//                         }
//         }

//         return dto;
//     }

private WorkflowTransitionDto mapWorkflowTransition(WorkflowTransition wt) {

        Optional<InspectionCall> ic = inspectionCallRepository.findByIcNumber(wt.getRequestId());

        InspectionCall i =null;
        if(ic.isPresent()){
            i = ic.get();
        }
        WorkflowTransitionDto dto = new WorkflowTransitionDto();
      //  if(wt.getProcessIeUserId()!= null) {
    if(wt.getRequestId() != null && wt.getRequestId().startsWith("EP")){
            Integer processIe = wt.getProcessIeUserId();
            String poi = i.getPlaceOfInspection();

            List<Integer> ieUsers = null;

            ieUsers = getIeUsersByProcessIeAndPlaceOfInsp(processIe, poi);



           // ieUsers.add(processIe);
            dto.setProcessIes(ieUsers);
        }

        if (i != null && "Final".equalsIgnoreCase(i.getTypeOfCall())) {

            List<FinalIeMapping> mappings =
                    finalIeMappingRepository
                            .findByWorkflowTransitionId(wt.getWorkflowTransitionId());

            List<Integer> finalIes = mappings.stream()
                    .map(FinalIeMapping::getIeUserId)
                    .collect(Collectors.toList());

            dto.setFinalIes(finalIes);
        }

        dto.setWorkflowTransitionId(wt.getWorkflowTransitionId());
        dto.setWorkflowId(wt.getWorkflowId());
        dto.setTransitionId(wt.getTransitionId());
        dto.setRequestId(wt.getRequestId());
        dto.setStatus(wt.getStatus());
        dto.setAction(wt.getAction());
        //dto.setAction(wt.getStatus());
        dto.setRemarks(wt.getRemarks());
        dto.setCreatedBy(wt.getCreatedBy());
        dto.setCreatedDate(wt.getCreatedDate());
        dto.setCurrentRole(wt.getCurrentRole());
        dto.setNextRole(wt.getNextRole());
        dto.setAssignedToUser(wt.getAssignedToUser());
        dto.setWorkflowSequence(wt.getWorkflowSequence());
        dto.setModifiedBy(wt.getModifiedBy());
        dto.setRio(wt.getRio());


        if(ic.isPresent()){
            dto.setPoNo(i.getPoNo());
            dto.setVendorName(i.getVendorId());
            dto.setProductType(i.getTypeOfCall());
            dto.setDesiredInspectionDate(String.valueOf(i.getDesiredInspectionDate()));
        }
      //  dto.setTransitionOrder(wt.getTransitionOrder());
        return dto;
    }

  /*  @Cacheable(
            value = "ieUsersByProcessPoi",
            key = "#processIeUserId + '_' + #poiCode"
    )
    private List<Integer> getIeUsersByProcessIeAndPlaceOfInsp(
            Integer processIeUserId,
            String poiCode
    ) {

        List<Long> ieUserIds =
                processIeUsersRepository
                        .findIeUsersByProcessIeAndPoi(
                                processIeUserId, poiCode);


        if (ieUserIds.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No IE found for POI " + poiCode +
                                    " under Process IE " + processIeUserId
                    )
            );
        }

        return new ArrayList<>(
                ieUserIds.stream()
                        .map(Long::intValue)
                        .toList()
        );

    }
*/
//  @Cacheable(
//          value = "ieUsersByProcessPoi",
//          key = "#processIeUserId + '_' + #poiCode"
//  )
  private List<Integer> getIeUsersByProcessIeAndPlaceOfInsp(
          Integer processIeUserId,
          String poiCode
  ) {


      List<Long> userIds =
              poiProcessIeMappingRepository.findUserIdsByPoiCode(poiCode);

      if (userIds == null || userIds.isEmpty()) {
          throw new BusinessException(
                  new ErrorDetails(
                          AppConstant.ERROR_CODE_RESOURCE,
                          AppConstant.ERROR_TYPE_CODE_RESOURCE,
                          AppConstant.ERROR_TYPE_VALIDATION,
                          "No Process IE found for POI " + poiCode
                  )
          );
      }


      return new ArrayList<>(
              userIds.stream()
                      .map(Long::intValue)
                      .toList()
      );
  }
/*
    private List<Integer> getIeUsersByProcessIeAndPoi(Integer processIeUserId, String poiCode) {

        //  Get all IE mappings under Process IE
        List<ProcessIeUsers> ieMappings =
                processIeUsersRepository.findAllByProcessUserId(processIeUserId);

        if (ieMappings.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No IE mapped to Process IE: " + processIeUserId
                    )
            );
        }

        //  Filter IEs by POI
        List<Integer> ieUserIds = new ArrayList<>();

        for (ProcessIeUsers map : ieMappings) {
            Integer ieUserId = Math.toIntExact(map.getIeUserId());

            boolean poiExists =
                    iePoiMappingRepository.existsByIeUserIdAndPoiCode(ieUserId, poiCode);

            if (poiExists) {
                ieUserIds.add(ieUserId);
            }
        }

        if (ieUserIds.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No IE found for POI " + poiCode + " under Process IE " + processIeUserId
                    )
            );
        }

        return ieUserIds;
    }
*/
private List<Integer> getIeUsersByProcessIeAndPoi(
        Integer processIeUserId,
        String poiCode
) {


    List<Long> userIds =
            poiProcessIeMappingRepository.findUserIdsByPoiCode(poiCode);

    if (userIds == null || userIds.isEmpty()) {
        throw new BusinessException(
                new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "No Process IE found for POI: " + poiCode
                )
        );
    }


    if (processIeUserId != null && processIeUserId != 0) {
        userIds = userIds.stream()
                .filter(id -> id.equals(Long.valueOf(processIeUserId)))
                .toList();

        if (userIds.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Process IE " + processIeUserId + " not mapped to POI " + poiCode
                    )
            );
        }
    }


    return userIds.stream()
            .map(Long::intValue)
            .toList();
}

/*
    private Integer getCmUserFromIeUser(Integer ieUserId) {

        // Step 1: Find cluster of IE (check primary)
        ClusterPrimaryIe primaryIe = clusterPrimaryIeRepository
                .findByIeUserId(ieUserId)
                .orElse(null);

        String clusterName = null;

        if (primaryIe != null) {
            clusterName = primaryIe.getClusterName();
        } else {
            // Check secondary IE list
            ClusterSecondaryIe secondaryIe = clusterSecondaryIeRepository
                    .findByIeUserId(ieUserId)
                    .orElse(null);

            if (secondaryIe != null) {
                clusterName = secondaryIe.getClusterName();
            }
        }

        if (clusterName == null) return null;

        // Find CM for that cluster
        ClusterCmUser cmUser = clusterCmUserRepository
                .findByClusterName(clusterName)
                .orElse(null);

        return cmUser != null ? cmUser.getCmUserId() : null;
    }*/

    private Integer getCmUserFromIeEmployeeCode(String ieEmployeeCode) {

        return ieControllingManagerRepository
                .findByIeEmployeeCode(ieEmployeeCode)
                .map(IeControllingManager::getCmUserId)
                .orElse(null);
    }


    private String determineJobStatus(String action) {

        if (action == null) return "UNKNOWN";

        switch (action.toUpperCase()) {

            case "VERIFY":
            case "VERIFY_PO_DETAILS":
                return "VERIFIED";

            case "CM_RETURN_TO_IE":
                return "IN_PROGRESS";

            case "ENTER_SHIFT_DETAILS_AND_START_INSPECTION":
            case "VERIFY_MATERIAL_AVAILABILITY":
            case "ENTRY_INSPECTION_RESULTS":
                return "IN_PROGRESS";

            case "REQUEST_CORRECTION_TO_CM":
                return "APPROVED";

            case "PAUSE_INSPECTION_RESUME_NEXT_DAY":
                return "PAUSED";
            case "PARTIAL_INSPECTION_COMPLETED":
                return "PAUSED";
            case "BLOCK_DUE_TO_PAYMENT":
                return "BLOCKED";

            case "COMPLETE_INSPECTION":
            case "INSPECTION_COMPLETE_CONFIRM":
            case "SUBMIT_INSPECTION_RESULTS":
                return "COMPLETED";
            case "CM_REJECT_CORRECTION":
                return  "REJECTED";

            case "VENDOR_CANCEL":
            case "CANCEL_DUE_TO_NO_MATERIAL":
                return "CANCELLED";

            default:
                return "IN_PROGRESS";
        }
    }
/*
    private TransitionMaster handlePaymentCheck(TransitionActionReqDto req, List<TransitionMaster> transitions) {

        //here we have to fetch data from payement master ic is payable or non payable
        String paymentType = "PAYABLE";     // PAYABLE / NON_PAYABLE
        Boolean paymentCompleted = false; // TRUE / FALSE

        TransitionMaster cancelled = transitions.stream()
                .filter(t -> t.getTransitionName().equalsIgnoreCase("CALL_CANCELLED_NON_PAYABLE"))
                .findFirst().orElse(null);

        TransitionMaster blocked = transitions.stream()
                .filter(t -> t.getTransitionName().equalsIgnoreCase("CALL_BLOCKED_PENDING_PAYMENT"))
                .findFirst().orElse(null);

        if (paymentType == null) return null;

        // NON PAYABLE → cancel
        if (paymentType.equalsIgnoreCase("NON_PAYABLE")) {
            return cancelled;
        }

        // PAYABLE CASE
        if (paymentType.equalsIgnoreCase("PAYABLE")) {

            if (paymentCompleted != null && !paymentCompleted) {
                return blocked;   // Payable but payment not done → BLOCK
            }

            // Payable & Payment done → Cancel
            return cancelled;
        }

        return null;
    }*/

    private TransitionMaster handlePaymentCheck(TransitionActionReqDto req, List<TransitionMaster> transitions) {

        /*   // 1. Fetch IC payment details using requestId
        ICPaymentMaster payment = icPaymentMasterRepository
                .findByRequestId(req.getRequestId())
                .orElse(null);

        if (payment == null) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Payment details not found for requestId: " + req.getRequestId())
            );
        }*/
        //here we have to fetch data from payement master ic is payable or non payable
        String paymentType = "NON_PAYABLE";     // PAYABLE / NON_PAYABLE
        Boolean paymentCompleted = false; // TRUE / FALSE

        System.out.print(paymentCompleted);
        System.out.print(paymentType);
       // String paymentType = payment.getPaymentType(); // PAYABLE / NON_PAYABLE
      //  Boolean paymentCompleted = payment.getPaymentCompleted(); // true / false

        // 2. Find the transitions
        TransitionMaster cancelled = transitions.stream()
                .filter(t -> t.getTransitionName().equalsIgnoreCase("CALL_CANCELLED_NON_PAYABLE"))
                .findFirst().orElse(null);

        System.out.print("cancelled" +cancelled);
        TransitionMaster blocked = transitions.stream()
                .filter(t -> t.getTransitionName().equalsIgnoreCase("CALL_BLOCKED_PENDING_PAYMENT"))
                .findFirst().orElse(null);
        System.out.print("blocked" +blocked);
        // 3. Apply logic

        // NON PAYABLE → Cancel directly
        if ("NON_PAYABLE".equalsIgnoreCase(paymentType)) {
            return cancelled;
        }

        // PAYABLE CASE
        if ("PAYABLE".equalsIgnoreCase(paymentType)) {

            // If payment NOT completed → BLOCK
            if (paymentCompleted != null && !paymentCompleted) {
                return blocked;
            }

            // If payment completed → CANCEL (CM rejected even after payment)
            return cancelled;
        }

        return null;
    }

    private WorkflowTransitionDto handleFinancePaymentVerification(
            WorkflowTransition current, TransitionActionReqDto req) {

        boolean paymentReceived =
                req.getPaymentReceivedStatus() != null &&
                        req.getPaymentReceivedStatus().equalsIgnoreCase("YES");

        if (!paymentReceived) {
            // Payment not received → PARKED
            TransitionMaster parked =
                    transitionMasterRepository.findByTransitionName("PARKED_PAYMENT_NOT_RECEIVED");

            WorkflowTransition next = createNextTransition(
                    current, parked, "PARKED", "Payment Not Received", req
            );

            next.setAssignedToUser(current.getAssignedToUser()); // Finance keeps it
            workflowTransitionRepository.save(next);

            return mapWorkflowTransition(next);
        }

        // PAYMENT RECEIVED → CALL REGISTERED
        TransitionMaster callReg =
                transitionMasterRepository.findByTransitionName("CALL_REGISTERED");

        WorkflowTransition registered = createNextTransition(
                current, callReg, "CALL_REGISTERED", "Payment Verified - Call Registered", req
        );

      //  registered.setAssignedToUser(assignIE(req.getPincode()));
       InspectionCall ic = inspectionCallRepository.findByIcNumber(req.getRequestId())
               .orElseThrow(() -> new BusinessException(
                       new ErrorDetails(
                               AppConstant.ERROR_CODE_INVALID,
                               AppConstant.ERROR_TYPE_CODE_VALIDATION,
                               AppConstant.ERROR_TYPE_VALIDATION,
                               "Place of Inspection (POI) is not assigned"
                       )
               ));

        System.out.println("Inspection call"+ ic.getPlaceOfInspection());
       // PincodePoIMapping poi = pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection());


        PincodePoIMapping poi =
                pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Invalid POI code"
                                )
                        ));

        System.out.println("poi"+ poi);
        String stage = null;
        if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
            stage ="R";
        }else if(ic.getTypeOfCall().equalsIgnoreCase("Process")){
            stage="P";
        }else{
            stage="F";
        }
        // RAW / FINAL → normal IE assignment
        // callReg.setAssignedToUser(assignIE(req.getPincode()));

        registered.setAssignedToUser(assignIE(poi.getPinCode(),"ERC",stage,ic.getPlaceOfInspection() ));

        registered.setJobStatus("REGISTERED");

        workflowTransitionRepository.save(registered);
        return mapWorkflowTransition(registered);
    }

    private WorkflowTransitionDto handlePaymentReceived(
            WorkflowTransition current, TransitionActionReqDto req) {

        TransitionMaster callReg =
                transitionMasterRepository.findByTransitionName("CALL_REGISTERED");

        WorkflowTransition next = createNextTransition(
                current, callReg, "CALL_REGISTERED",
                "Payment Received - Call Registered", req
        );

      //  next.setAssignedToUser(assignIE(req.getPincode()));
        InspectionCall ic = inspectionCallRepository.findByIcNumber(req.getRequestId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Place of Inspection (POI) is not assigned"
                        )
                ));

       // PincodePoIMapping poi = pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection());

        PincodePoIMapping poi =
                pincodePoIMappingRepository.findByPoiCode(ic.getPlaceOfInspection())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Invalid POI code"
                                )
                        ));
        String stage = null;
        if(ic.getTypeOfCall().equalsIgnoreCase("Raw Material")){
            stage ="R";
        }else if(ic.getTypeOfCall().equalsIgnoreCase("Process")){
            stage="P";
        }else{
            stage="F";
        }

        // RAW / FINAL → normal IE assignment
        // callReg.setAssignedToUser(assignIE(req.getPincode()));

      next.setAssignedToUser(assignIE(poi.getPinCode(),"ERC",stage,ic.getPlaceOfInspection() ));

        next.setJobStatus("REGISTERED");

        workflowTransitionRepository.save(next);
        return mapWorkflowTransition(next);
    }
    private WorkflowTransitionDto handlePaymentNotReceived(
            WorkflowTransition current, TransitionActionReqDto req) {

        TransitionMaster parked =
                transitionMasterRepository.findByTransitionName("PARKED_PAYMENT_NOT_RECEIVED");

        WorkflowTransition next = createNextTransition(
                current, parked, "PARKED", "Payment Not Received", req
        );

        next.setAssignedToUser(current.getAssignedToUser()); // Finance retains
        workflowTransitionRepository.save(next);

        return mapWorkflowTransition(next);
    }
/*
     @Override
    public List<WorkflowTransitionDto> allPendingWorkflowTransition(String roleName) {

        List<WorkflowTransition> pending;

        if ("IE".equalsIgnoreCase(roleName)) {

            pending = workflowTransitionRepository
                    .findPendingByRoles(List.of("IE", "Process IE"));

        } else {
            pending = workflowTransitionRepository
                    .findPendingByRole(roleName);
        }
        System.out.println(pending);

        return pending.stream()
                .sorted(Comparator.comparing(WorkflowTransition::getRequestId)
                        .thenComparing(WorkflowTransition::getCreatedDate))
                .map(this::mapWorkflowTransition)
                .collect(Collectors.toList());
    }*/
@Override
public List<WorkflowTransitionDto> allPendingWorkflowTransition(String roleName) {

    long t1 = System.currentTimeMillis();
    List<WorkflowTransition> pending =
            "IE".equalsIgnoreCase(roleName)
                    ? workflowTransitionRepository.findPendingByRoles(
                    List.of("IE", "Process IE"))
                    : workflowTransitionRepository.findPendingByRole(roleName);

    log.info("Workflow query time = {} ms",
            System.currentTimeMillis() - t1);

    long t2 = System.currentTimeMillis();

    List<String> requestIds = pending.stream()
            .map(WorkflowTransition::getRequestId)
            .distinct()
            .toList();

    List<Integer> wtIds = pending.stream()
            .map(WorkflowTransition::getWorkflowTransitionId)
            .toList();

    Map<String, InspectionDataDto> inspectionMap =
            inspectionCallRepository.findLiteByIcNumberIn(requestIds)
                    .stream()
                    .collect(Collectors.toMap(
                            InspectionDataDto::icNumber,
                            Function.identity(),
                            (a, b) -> a
                    ));

    // Bulk fetch PoHeader and VendorMaster for performance and formatting
    List<String> poNos = inspectionMap.values().stream()
            .map(InspectionDataDto::poNo)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    Map<String, PoHeader> poMap = poHeaderRepository.findByPoNoIn(poNos).stream()
            .collect(Collectors.toMap(PoHeader::getPoNo, Function.identity(), (a, b) -> a));

    List<String> vendorCodes = inspectionMap.values().stream()
            .map(InspectionDataDto::vendorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    Map<String, VendorMaster> vendorMap = vendorMasterRepository.findByVendorCodeIn(vendorCodes).stream()
            .collect(Collectors.toMap(VendorMaster::getVendorCode, Function.identity(), (a, b) -> a));

//    Map<String, InspectionCall> inspectionMap = inspectionCallRepository.findByIcNumberIn(requestIds) .stream()
//            .collect(Collectors.toMap( InspectionCall::getIcNumber, Function.identity() ));



    Map<Integer, List<Integer>> finalIeMap =
            finalIeMappingRepository.findByWorkflowTransitionIdIn(wtIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            FinalIeMapping::getWorkflowTransitionId,
                            Collectors.mapping(
                                    FinalIeMapping::getIeUserId,
                                    Collectors.toList()
                            )
                    ));

    // Bulk fetch Process IE users
    Set<Long> processIeUserIds = pending.stream()
            .map(WorkflowTransition::getProcessIeUserId)
            .filter(Objects::nonNull)
            .map(Integer::longValue)
            .collect(Collectors.toSet());

    Map<String, List<Integer>> processIeIeMap = new HashMap<>();
    if (!processIeUserIds.isEmpty()) {
        List<Object[]> rows = processIeUsersRepository.findIeUsersByProcessIeBulk(processIeUserIds);
        for (Object[] row : rows) {
            Long pId = (Long) row[0];
            String poi = (String) row[1];
            Long ieId = (Long) row[2];
            String key = pId + "_" + poi;
            processIeIeMap.computeIfAbsent(key, k -> new ArrayList<>()).add(ieId.intValue());
        }
    }

    log.info("Workflow bulk processing time = {} ms",
            System.currentTimeMillis() - t2);

    return pending.stream()
            .map(wt -> mapWorkflow(wt, inspectionMap, finalIeMap, poMap, vendorMap, processIeIeMap))
            .collect(Collectors.toList());
}

    private WorkflowTransitionDto mapWorkflow(
            WorkflowTransition wt,
            Map<String, InspectionDataDto> inspectionMap,
            Map<Integer, List<Integer>> finalIeMap,
            Map<String, PoHeader> poMap,
            Map<String, VendorMaster> vendorMap,
            Map<String, List<Integer>> processIeIeMap
    ) {
        long t3 = System.currentTimeMillis();


        InspectionDataDto i = inspectionMap.get(wt.getRequestId());
        WorkflowTransitionDto dto = new WorkflowTransitionDto();

       // if (wt.getProcessIeUserId() != null && i != null) {
        if (wt.getRequestId() != null && wt.getRequestId().startsWith("EP")) {
            Integer processIe = wt.getProcessIeUserId();
            String poi = i.placeOfInspection();

         //   List<Integer> ieUsers = processIeIeMap.getOrDefault(processIe + "_" + poi, new ArrayList<>());
            List<Integer>   ieUsers = getIeUsersByProcessIeAndPlaceOfInsp(processIe, poi);

            // Replicate original exception logic if empty
            if (ieUsers.isEmpty()) {
                // We'll log a warning instead of failing the entire batch,
                // but let's see if we should strictly follow original.
                // Logically, if it was throwing exception before, it should probably continue to do so.
                // However, throwing BusinessException here would stop the map loop.
                // Let's just use the processIe itself as fallback or throw if required.
                log.warn("No IE found for POI {} under Process IE {}", poi, processIe);
            }

            List<Integer> finalIeUsers = new ArrayList<>(ieUsers);
            finalIeUsers.add(processIe);
            dto.setProcessIes(finalIeUsers);
        }

        if (i != null && "Final".equalsIgnoreCase(i.typeOfCall())) {
            dto.setFinalIes(
                    finalIeMap.getOrDefault(
                            wt.getWorkflowTransitionId(),
                            Collections.emptyList()
                    )
            );
        }

        dto.setWorkflowTransitionId(wt.getWorkflowTransitionId());
        dto.setWorkflowId(wt.getWorkflowId());
        dto.setTransitionId(wt.getTransitionId());
        dto.setRequestId(wt.getRequestId());
        dto.setStatus(wt.getStatus());
        dto.setAction(wt.getAction());
        dto.setRemarks(wt.getRemarks());
        dto.setCreatedBy(wt.getCreatedBy());
        dto.setCreatedDate(wt.getCreatedDate());
        dto.setCurrentRole(wt.getCurrentRole());
        dto.setNextRole(wt.getNextRole());
        dto.setAssignedToUser(wt.getAssignedToUser());
        dto.setWorkflowSequence(wt.getWorkflowSequence());
        dto.setModifiedBy(wt.getModifiedBy());
        dto.setRio(wt.getRio());

        if (i != null) {
            // PO No Formatting: Railway / PoNo / PoSrNo
            PoHeader ph = poMap.get(i.poNo());
            String poNo = i.poNo() != null ? i.poNo() : "";
            String srNo = i.poSerialNo() != null ? i.poSerialNo() : "";

            // Strip redundant PO prefix from serial number
            if (!poNo.isEmpty() && srNo.startsWith(poNo + "/")) {
                srNo = srNo.substring(poNo.length() + 1);
            }

            String formattedPo = (ph != null ? ph.getRlyShortName() : "") + " / " +
                                 poNo + " / " + srNo;
            dto.setPoNo(formattedPo);
            dto.setRawPoNo(i.poNo());

            // Vendor Name from Master
            VendorMaster vm = vendorMap.get(i.vendorId());
            dto.setVendorName(vm != null ? vm.getVendorName() : i.vendorId());

            dto.setProductType(i.typeOfCall());
            dto.setDesiredInspectionDate(
                    String.valueOf(i.desiredInspectionDate())
            );
        }

        return dto;
    }



    @Override
    public List<WorkflowTransitionDto> allPendingQtyEditTransitions(String roleName) {

        List<WorkflowTransition> pendingQty =
                workflowTransitionRepository.findPendingQtyEditByRole(roleName);

        return pendingQty.stream()
                .sorted(Comparator.comparing(WorkflowTransition::getRequestId)
                        .thenComparing(WorkflowTransition::getCreatedDate))
                .map(this::mapWorkflowTransition)
                .collect(Collectors.toList());
    }




//    private Integer getProcessIeUserFromCluster(String pincode) {
//
//        // 1. Find cluster
//        PincodeCluster cluster = pincodeClusterRepository.findByPincode(pincode)
//                .orElseThrow(() -> new BusinessException(
//                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
//                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
//                                AppConstant.ERROR_TYPE_VALIDATION,
//                                "Cluster not found for pincode: " + pincode)
//                ));
//
//        // 2. Find Process IE mapped to that cluster
//        ProcessIeMaster process = processIeMasterRepository
//                .findByClusterName(cluster.getClusterName())
//                .orElseThrow(() -> new BusinessException(
//                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
//                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
//                                AppConstant.ERROR_TYPE_VALIDATION,
//                                "No Process IE found for cluster: " + cluster.getClusterName())
//                ));
//
//        return process.getProcessIeUserId();
//    }
/*
    private Integer getProcessIeUserFromPoi(String poiCode) {

        //  Find all IEs for the POI
        List<IePoiMapping> poiMappings = iePoiMappingRepository
                .findAllByPoiCode(poiCode);

        if (poiMappings.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No IE found for POI: " + poiCode
                    )
            );
        }

        System.out.println(poiMappings);

        //  Pick Process IE from mapped IEs (first match)
        for (IePoiMapping poiMap : poiMappings) {

            Optional<ProcessIeUsers> processIeOpt =
                    processIeUsersRepository.findByIeUserId(poiMap.getIeUserId());


            if (processIeOpt.isPresent()) {
                return processIeOpt.get().getProcessUserId().intValue();
            }
        }

        //  No Process IE found
        throw new BusinessException(
                new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "No Process IE mapped for POI: " + poiCode
                )
        );
    }
*/
/*
private Integer getProcessIeUserFromPoi(String poiCode, Integer processIe) {

    // 1. Get latest IE mapped to POI
    IePoiMapping latestPoiIe =
            iePoiMappingRepository
                    .findTopByPoiCodeOrderByCreatedDateDesc(poiCode)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "No IE found for POI: " + poiCode
                            )
                    ));
    ProcessIeUsers latestProcessIe =null;
    if(processIe!=0){
        return processIe;
    }else {

        // 2. Get latest Process IE for that IE
       latestProcessIe =
                processIeUsersRepository
                        .findTopByIeUserIdOrderByCreatedDateDesc(
                                latestPoiIe.getIeUserId()
                        )
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "No Process IE mapped for IE: " + latestPoiIe.getIeUserId()
                                )
                        ));
    }

    // 3. Return Process User ID
    return latestProcessIe.getProcessUserId().intValue();
}
*/



    @Override
    public List<WorkflowTransitionDto> workflowTransitionHistory(String requestId) {

        List<WorkflowTransitionDto> workflowTransitionDtoList = new ArrayList<>();
        List<WorkflowTransition> workflowTransitionList = null;
        workflowTransitionList = workflowTransitionRepository.findByRequestId(requestId);
        if (Objects.nonNull(workflowTransitionList) && !workflowTransitionList.isEmpty()) {
            workflowTransitionDtoList = workflowTransitionList.stream().sorted(Comparator.comparing(WorkflowTransition::getWorkflowSequence).reversed()).map(e -> {
                return mapWorkflowTransition(e);
            }).collect(Collectors.toList());
        }

        return workflowTransitionDtoList;
    }


    private WorkflowTransitionDto requestQtyEdit(WorkflowTransition current, TransitionActionReqDto req) {


        // Vendor can request only before inspection initiation
        WorkflowTransition initiated = workflowTransitionRepository
                .findTopByRequestIdAndStatus(current.getRequestId(), "INITIATE_INSPECTION");

        if (initiated != null) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.INVALID_WORKFLOW_TRANSITION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Inspection already initiated. Quantity edit not allowed."));
        }

        // Must be after CALL_REGISTERED
        WorkflowTransition callReg = workflowTransitionRepository
                .findTopByRequestIdAndStatus(current.getRequestId(), "CALL_REGISTERED");

        if (callReg == null) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.INVALID_WORKFLOW_TRANSITION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Call must be registered before requesting qty edit."));
        }

        // Cutoff date logic
       WorkflowTransition schedule = workflowTransitionRepository
                .findTopByRequestIdAndStatus(current.getRequestId(), "IE_SCHEDULED");

       // Date inspectionDate = 08-12-2025;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

// Parse LocalDate
        LocalDate inspectionLocalDate = LocalDate.parse("10-12-2025", formatter);

// Convert LocalDate → Date
        Date inspectionDate = Date.from(inspectionLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (schedule != null) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(inspectionDate);
            cal.add(Calendar.DAY_OF_MONTH, -1);  // 1 day before inspection

            if (new Date().after(cal.getTime())) {
                throw new BusinessException(
                        new ErrorDetails(AppConstant.INVALID_WORKFLOW_TRANSITION,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Quantity edit request not allowed after schedule cutoff date.")
                );
            }
        }


        // Forward to CM
        TransitionMaster t = transitionMasterRepository
                .findByTransitionName("REQUEST_QTY_EDIT");

        WorkflowTransition next = createNextTransition(
                current,
                t,
                "QTY_EDIT_REQUESTED",
                req.getRemarks(),
                req
        );

        // Assign CM of this IE's cluster
       // Integer cmUserId = getCmUserFromIeUser(callReg.getAssignedToUser());
        Optional<UserMaster> um = userMasterRepository.findByUserId(callReg.getAssignedToUser());

        Integer cmUserId =null;
        if(um.isPresent()){
            UserMaster u = um.get();
           cmUserId =  getCmUserFromIeEmployeeCode(u.getEmployeeCode());
        }


        next.setAssignedToUser(cmUserId);

        workflowTransitionRepository.save(next);
        return mapWorkflowTransition(next);
    }


    private WorkflowTransitionDto cmQtyDecision(WorkflowTransition current, TransitionActionReqDto req) {

        TransitionMaster nextT = resolveConditionalTransition(current, req);

        if (nextT == null) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.INVALID_WORKFLOW_TRANSITION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No valid qty edit decision path."));
        }

        WorkflowTransition initiated = workflowTransitionRepository
                .findTopByRequestIdAndStatus(current.getRequestId(), "INITIATE_INSPECTION");

        if (initiated != null) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.INVALID_WORKFLOW_TRANSITION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,"Inspection initiated. CM cannot approve or reject now."));
        }


        WorkflowTransition next = createNextTransition(
                current,
                nextT,
                req.getCmDecision().equalsIgnoreCase("APPROVED") ?
                        "QTY_APPROVED" :
                        "QTY_REJECTED",
                req.getRemarks(),
                req
        );

        // Return back to vendor
        next.setAssignedToUser(current.getCreatedBy());

        workflowTransitionRepository.save(next);
        return mapWorkflowTransition(next);
    }


    @Override
    public List<WorkflowTransitionDto> allBlockedWorkflowTransitions() {

        List<WorkflowTransition> blocked =
                workflowTransitionRepository.findBlockedTransitions();

        return blocked.stream()
                .sorted(Comparator.comparing(WorkflowTransition::getRequestId)
                        .thenComparing(WorkflowTransition::getCreatedDate))
                .map(this::mapWorkflowTransition)
                .collect(Collectors.toList());
    }


    @Override
    public WorkflowDto workflowByWorkflowName(String workflowName) {
        WorkflowDto workflowDto = null;

        if (Objects.nonNull(workflowName)) {
            WorkflowMaster workflowMaster = workflowMasterRepository.findByWorkflowName(workflowName);
            if (Objects.nonNull(workflowMaster)) {
                workflowDto = new WorkflowDto();
                workflowDto.setWorkflowId(workflowMaster.getWorkflowId());
                workflowDto.setWorkflowName(workflowMaster.getWorkflowName());
                workflowDto.setCreatedBy(workflowMaster.getCreatedBy());
                workflowDto.setCreatedDate(workflowMaster.getCreatedDate());
            } else {
                throw new InvalidInputException(new ErrorDetails(AppConstant.WORKFLOW_NOT_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION, "Workflow not found."));
            }
        } else {
            throw new InvalidInputException(new ErrorDetails(AppConstant.USER_INVALID_INPUT, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION, "Invalid input."));
        }

        return workflowDto;
    }

    @Override
    public List<TransitionDto> transitionsByWorkflowId(Integer workflowId) {

        List<TransitionDto> transitionDtoList = new ArrayList<>();
        List<TransitionMaster> transitionMasterList = transitionMasterRepository.findByWorkflowId(workflowId);

        if (Objects.nonNull(transitionMasterList) && !transitionMasterList.isEmpty()) {
            transitionDtoList = transitionMasterList.stream().map(transitionMaster -> {
                TransitionDto transitionDto = new TransitionDto();
                transitionDto.setWorkflowId(transitionMaster.getWorkflowId());
                transitionDto.setTransitionId(transitionMaster.getTransitionId());
                transitionDto.setCreatedDate(transitionMaster.getCreatedDate());
                transitionDto.setTransitionOrder(transitionMaster.getTransitionOrder());
                transitionDto.setConditionId(transitionMaster.getConditionId());
                transitionDto.setCurrentRoleId(transitionMaster.getCurrentRoleId());
                transitionDto.setNextRoleId(transitionMaster.getNextRoleId());

                transitionDto.setTransitionName(transitionMaster.getTransitionName());
                transitionDto.setWorkflowName(workflowNameById(transitionMaster.getWorkflowId()));
                transitionDto.setCurrentRoleName(roleNameById(transitionMaster.getCurrentRoleId()));
                transitionDto.setNextRoleName(roleNameById(transitionMaster.getNextRoleId()));

                return transitionDto;
            }).collect(Collectors.toList());
        } else {
            throw new InvalidInputException(new ErrorDetails(AppConstant.WORKFLOW_NOT_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION, "Workflow not found."));
        }
        return transitionDtoList;
    }



    private String workflowNameById(Integer workflowId) {
        if (Objects.nonNull(workflowId)) {
            return workflowMasterRepository.findById(workflowId).orElse(new WorkflowMaster()).getWorkflowName();
        } else {
            return null;
        }
    }

    private void assignRescheduleUser(WorkflowTransition next, WorkflowTransition current, TransitionActionReqDto req) {

        String action = req.getAction();

        if (action.equalsIgnoreCase("IE_REQUEST_RESCHEDULE")) {
          //  next.setAssignedToUser(getCmUserFromIeUser(req.getActionBy()));
            Optional<UserMaster> um = userMasterRepository.findByUserId(req.getActionBy());

            Integer cmUserId;
            if(um.isPresent()){
                UserMaster u = um.get();
                cmUserId =  getCmUserFromIeEmployeeCode(u.getEmployeeCode());
            }
        }
        else if (action.equalsIgnoreCase("CM_FORWARD_TO_SBU_HEAD")) {
            next.setAssignedToUser(getSbuHeadUser(req.getPincode()));
        }
        else if (action.contains("APPROVE") || action.contains("REJECT")) {
            next.setAssignedToUser(current.getCreatedBy());
        }
    }
    private Integer getSbuHeadUser(String pincode) {

        // Get the latest workflow transaction (for clusterName)
      Optional<PincodeCluster> cluster = pincodeClusterRepository.findByPincode(pincode);
        if (cluster == null) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "No workflow history found for requestId: " + pincode)
            );
        }

        //Get cluster details
        RegionCluster regionCluster =
                regionClusterRepository.findByClusterName(cluster.get().getClusterName())
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Cluster not found: " + cluster.get().getClusterName())
                        ));

        String regionName = regionCluster.getRegionName();

        // Fetch SBU Head for that region
        RegionSbuHead sbu =
                regionSbuHeadRepository.findByRegionName(regionName)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "No SBU Head defined for region: " + regionName)
                        ));

        return sbu.getSbuHeadUserId();
    }


    @Override
    public List<IcWorkflowTransitionDto> getInspectionCompletedByCreatedUser(Integer createdBy) {
        List<WorkflowTransition> entities = workflowTransitionRepository
                .findCompletedByUserRule(Long.valueOf(createdBy));

        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> requestIds = entities.stream()
                .map(WorkflowTransition::getRequestId)
                .distinct()
                .toList();

        Map<String, InspectionDataDto> icMap = inspectionCallRepository.findLiteByIcNumberIn(requestIds).stream()
                .collect(Collectors.toMap(InspectionDataDto::icNumber, Function.identity(), (a, b) -> a));

        List<String> poNos = icMap.values().stream()
                .map(InspectionDataDto::poNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, PoHeader> poMap = poHeaderRepository.findByPoNoIn(poNos).stream()
                .collect(Collectors.toMap(PoHeader::getPoNo, Function.identity(), (a, b) -> a));

        List<String> vendorCodes = icMap.values().stream()
                .map(InspectionDataDto::vendorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, VendorMaster> vendorMap = vendorMasterRepository.findByVendorCodeIn(vendorCodes).stream()
                .collect(Collectors.toMap(VendorMaster::getVendorCode, Function.identity(), (a, b) -> a));

        List<String> poiCodes = icMap.values().stream()
                .map(InspectionDataDto::placeOfInspection)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, PincodePoIMapping> poiMap = pincodePoIMappingRepository.findByPoiCodeIn(poiCodes).stream()
                .collect(Collectors.toMap(PincodePoIMapping::getPoiCode, Function.identity(), (a, b) -> a));

        List<String> pinCodes = poiMap.values().stream()
                .map(PincodePoIMapping::getPinCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<IEFieldsMapping> allIeMappings = ieFieldsMappingRepository.findByPinCodeInAndProduct(pinCodes, "ERC");

        // Optimize: Group IE mappings by PinCode for faster lookup
        Map<String, List<IEFieldsMapping>> ieGroupedByPin = allIeMappings.stream()
                .collect(Collectors.groupingBy(IEFieldsMapping::getPinCode));

        return entities.stream().map(wt -> {
            IcWorkflowTransitionDto dto = new IcWorkflowTransitionDto();
            dto.setWorkflowTransitionId(wt.getWorkflowTransitionId());
            dto.setWorkflowId(wt.getWorkflowId());
            dto.setTransitionId(wt.getTransitionId());
            dto.setRequestId(wt.getRequestId());
            dto.setCurrentRole(wt.getCurrentRole());
            dto.setNextRole(wt.getNextRole());
            dto.setCurrentRoleName(wt.getCurrentRoleName());
            dto.setNextRoleName(wt.getNextRoleName());
            dto.setStatus(wt.getStatus());
            dto.setAction(wt.getAction());
            dto.setRemarks(wt.getRemarks());
            dto.setCreatedBy(wt.getCreatedBy());
            dto.setModifiedBy(wt.getModifiedBy());
            dto.setAssignedToUser(wt.getAssignedToUser());
            dto.setJobStatus(wt.getJobStatus());
            dto.setProcessIeUserId(wt.getProcessIeUserId());
            dto.setCreatedDate(wt.getCreatedDate());
            dto.setWorkflowSequence(wt.getWorkflowSequence());

            InspectionDataDto ic = icMap.get(wt.getRequestId());
            if (ic != null) {
                // PO No Formatting: Railway / PoNo / PoSrNo
                PoHeader ph = poMap.get(ic.poNo());
                String poNo = ic.poNo() != null ? ic.poNo() : "";
                String srNo = ic.poSerialNo() != null ? ic.poSerialNo() : "";

                // Strip redundant PO prefix from serial number (e.g., "123/001" -> "001")
                if (!poNo.isEmpty() && srNo.startsWith(poNo + "/")) {
                    srNo = srNo.substring(poNo.length() + 1);
                }

                String formattedPo = (ph != null ? ph.getRlyShortName() : "") + " / " +
                                     poNo + " / " + srNo;
                dto.setPoNo(formattedPo);

                // Vendor Name from Master
                VendorMaster vm = vendorMap.get(ic.vendorId());
                dto.setVendorName(vm != null ? vm.getVendorName() : ic.vendorId());

                dto.setProductType("ERC-" + ic.typeOfCall());
                dto.setStage(ic.typeOfCall());

                // IE Mapping (RIO)
                PincodePoIMapping poi = poiMap.get(ic.placeOfInspection());
                if (poi != null) {
                    final String pin = poi.getPinCode();
                    final String stageCode = ic.typeOfCall().equalsIgnoreCase("Raw Material") ? "R" :
                                       ic.typeOfCall().equalsIgnoreCase("Process") ? "P" : "F";

                    List<IEFieldsMapping> pinMappings = ieGroupedByPin.get(pin);
                    if (pinMappings != null) {
                        Optional<IEFieldsMapping> mapping = pinMappings.stream()
                                .filter(m -> m.getStage().equals(stageCode) || m.getStage().contains(stageCode))
                                .findFirst();
                        mapping.ifPresent(m -> dto.setRio(m.getRio()));
                    }
                }
            }

            return dto;
        }).toList();
    }

    @Transactional
    public String withdrawCall(TransitionActionReqDto dto) {

        // =========================
        // 1. VALIDATION
        // =========================
        if (dto.getWorkflowTransitionId() == null) {
            throw new RuntimeException("WorkflowTransitionId is required");
        }

        // =========================
        // 2. FETCH WORKFLOW
        // =========================
        WorkflowTransition wf = workflowTransitionRepository
                .findById(dto.getWorkflowTransitionId())
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if ("WITHDRAW".equalsIgnoreCase(wf.getStatus())) {
            throw new RuntimeException("Already withdrawn");
        }

        String icNumber = wf.getRequestId();

        // =========================
        // 3. UPDATE WORKFLOW
        // =========================
        wf.setStatus("WITHDRAW");
        wf.setAction("WITHDRAW");
        wf.setJobStatus("WITHDRAW");
        wf.setRemarks(dto.getRemarks());
        wf.setModifiedBy(dto.getActionBy());
        wf.setNextRole(null);



        workflowTransitionRepository.save(wf);

        // =========================
        // 4. GET INSPECTION CALL
        // =========================
        Optional<InspectionCall> callOpt = inspectionCallRepository.findByIcNumber(icNumber);

        if (callOpt.isEmpty()) {
            return "Withdrawn (No inspection call)";
        }

        InspectionCall inspectionCall = callOpt.get();


        Optional<RmInspectionDetails> rmDetails =
                rmInspectionDetailsRepository.findByIcId(inspectionCall.getId());

        if (rmDetails == null || rmDetails.isEmpty()) {
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Inspection request not found with ID: " + inspectionCall.getId()
            ));
        }




        rmDetails.ifPresent(rm -> {

            List<RmHeatQuantity> heats =
                    rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(rm.getId()));

            if (heats.isEmpty()) return;

            for (RmHeatQuantity heat : heats) {

                BigDecimal qty = heat.getOfferedQty();

                if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) continue;

                if (heat.getHeatNumber() == null || heat.getTcNumber() == null) continue;

                restoreInventoryJpa(
                        heat.getHeatNumber(),
                        heat.getTcNumber(),
                        qty
                );
            }
        });

        return "Call withdrawn & inventory restored successfully";
    }

    private void restoreInventoryJpa(String heatNumber, String tcNumber, BigDecimal qty) {

        InventoryEntry inv = inventoryEntryRepository
                .findByHeatNumberAndTcNumber(heatNumber, tcNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Inventory not found for Heat: " + heatNumber + " TC: " + tcNumber
                ));


        BigDecimal offeredQty = inv.getOfferedQuantity();
        BigDecimal inspectionQty = inv.getQtyLeftForInspection();

        if (offeredQty == null) offeredQty = BigDecimal.ZERO;
        if (inspectionQty == null) inspectionQty = BigDecimal.ZERO;

        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }


        BigDecimal updatedOffered = offeredQty.subtract(qty);

        if (updatedOffered.compareTo(BigDecimal.ZERO) < 0) {
            updatedOffered = BigDecimal.ZERO; // safety
        }

        // add to qty_left_for_inspection
        BigDecimal updatedInspection = inspectionQty.add(qty);


        inv.setOfferedQuantity(updatedOffered);
        inv.setQtyLeftForInspection(updatedInspection);

        inventoryEntryRepository.save(inv);
    }


}