package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.dto.SleeperWorkflowTransactionDto;
import com.sarthi.Sleeper.entity.SleeperPincodePoIMapping;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.entity.SleeperTransitionMaster;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.*;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.constant.AppConstant;
import com.sarthi.entity.*;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void validateUser(Integer userId) {
        UserMaster userMaster = userMasterRepository.findById(userId).orElseThrow(() -> new InvalidInputException(new ErrorDetails(AppConstant.USER_NOT_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION, "User not found.")));
    }
     /*   @Override
        public SleeperWorkflowTransactionDto initiateWorkflow(
                String requestId,
                Long moduleId,
                Long workflowId,
                Long createdBy) {

            validateUser(Math.toIntExact(createdBy));
            validateWorkflowAndModule(workflowId, moduleId);

            SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();


            SleeperPincodePoIMapping mapping =
                    sleeperPincodePoIMappingRepository.findByVendorCode(String.valueOf(createdBy));

            tx.setRequestId(requestId);
            tx.setModuleId(moduleId);
            tx.setWorkflowId(workflowId);

            tx.setCurrentRole("Vendor");
            tx.setNextRole("IE");
            tx.setAction(AppConstant.CREATED_TYPE);
            tx.setStatus(AppConstant.CREATED_TYPE);

            tx.setPoiCode(mapping.getPoiCode());
            tx.setCreatedBy(createdBy);
            tx.setCreatedDate(LocalDateTime.now());

            SleeperWorkflowTransaction saved = repository.save(tx);

            return mapToResponse(saved);
        } */
     @Override
     public SleeperWorkflowTransactionDto initiateWorkflow(
             String requestId,
             Long moduleId,
             Long workflowId,
             Long createdBy) {

         validateUser(Math.toIntExact(createdBy));
         if (workflowId == 1) {
             validateWorkflowAndModule(workflowId, moduleId);
         }
         SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

         SleeperPincodePoIMapping mapping =
                 sleeperPincodePoIMappingRepository.findByVendorCode(String.valueOf(createdBy));

         tx.setRequestId(requestId);
         tx.setModuleId(moduleId);
         tx.setWorkflowId(workflowId);

         // workflowId = 2 use TRANSITION_MASTER
         if (workflowId == 2) {

             SleeperTransitionMaster transition =
                     sleeperTransitionMasterRepository
                             .findFirstByWorkflowIdAndCurrentActionOrderByTransitionOrderAsc(
                                     workflowId.intValue(), AppConstant.CREATED_TYPE)
                             .orElseThrow(() -> new RuntimeException("Transition not configured"));

             tx.setCurrentRole(getRoleName(transition.getCurrentRoleId()));
             tx.setNextRole(getRoleName(transition.getNextRoleId()));

             tx.setAction(transition.getCurrentAction());
             tx.setStatus(AppConstant.CREATED_TYPE);
             if(transition.getNextRoleId().equals(2)) {
                 SleeperPincodePoIMapping poi =
                         sleeperPincodePoIMappingRepository.findByPoiCode(mapping.getPoiCode())
                                 .orElseThrow(() -> new BusinessException(
                                         new ErrorDetails(
                                                 AppConstant.ERROR_CODE_RESOURCE,
                                                 AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                 AppConstant.ERROR_TYPE_VALIDATION,
                                                 "Invalid POI code"
                                         )
                                 ));
                 String stage = "F";
                 String product = "Sleeper";
                 String pinCode = poi.getPinCode();

                 IEFieldsMapping map =
                         ieFieldsMappingRepository
                                 .findByPinCodeProductAndStageMatch(pinCode, product, stage)
                                 .orElseThrow(() -> new BusinessException(
                                         new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                                 AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                                 AppConstant.ERROR_TYPE_VALIDATION,
                                                 "No IE mapping for given pin/product/stage")));

                 String rio = map.getRio();

                 tx.setRio(rio);
             }

         }
         else {
             // workflowId = 1
             tx.setCurrentRole("Vendor");
             tx.setNextRole("IE");

             tx.setAction(AppConstant.CREATED_TYPE);
             tx.setStatus(AppConstant.CREATED_TYPE);
         }

         tx.setPoiCode(mapping.getPoiCode());
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

        dto.setCurrentRole(tx.getCurrentRole());
        dto.setNextRole(tx.getNextRole());
        dto.setShift(tx.getShift());

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
            mappings = poiIeMappingRepository
                    .findByPoiCodeAndIeType(tx.getPoiCode(), "Main IE");
        } else {
            if("Vendor".equalsIgnoreCase(tx.getNextRole())){
              vendorId = sleeperPincodePoIMappingRepository
                       .findVendorCodeByPoiCode(tx.getPoiCode())
                       .orElseThrow(() -> new RuntimeException("Vendor not found for POI " ));
           }else{
               // Existing logic
               mappings = poiIeMappingRepository
                       .findByPoiCode(tx.getPoiCode());
           }
        }
        if (mappings != null) {
            userIds = mappings.stream()
                    .map(SleeperPoiIeMapping::getIeUserId)
                    .toList();
        }
        if(vendorId != null){
            dto.setAssignedToUser(Long.valueOf(vendorId));
        }

        dto.setAccessibleUserIds(userIds);

        return dto;
    }
/*
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

        // Validate next role
       // validateNextRole(req.getActionBy(), current.getNextRole());

        validateUserForPoi(current.getPoiCode(), req.getActionBy());
        String status = determineStatus(req.getAction());

        SleeperWorkflowTransaction tx = new SleeperWorkflowTransaction();

        tx.setRequestId(req.getRequestId());
        tx.setModuleId(req.getModuleId());
        tx.setWorkflowId(current.getWorkflowId());

        tx.setAction(req.getAction());
        tx.setStatus(status);
        tx.setRemarks(req.getRemarks());

        tx.setShift(current.getShift());

        tx.setPoiCode(current.getPoiCode());

        if(req.getAction().equals("REQUEST_BACK")) {
            tx.setCurrentRole("IE");
            tx.setNextRole("Vendor");
        }
        else if(req.getAction().equals("RESUBMIT")){
            tx.setCurrentRole("Vendor");
            tx.setNextRole("IE");
        }
        else{
            tx.setCurrentRole("IE");
        }
        tx.setAssignedToUser(req.getActionBy());

        tx.setCreatedBy(current.getCreatedBy());
        tx.setModifiedBy(req.getActionBy());
        tx.setCreatedDate(LocalDateTime.now());

        SleeperWorkflowTransaction saved = repository.save(tx);

        return mapToResponse(saved);
    }

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
                            "Workflow transition not found"
                    )
            ));

    if(current.getWorkflowId()==1 && current.getNextRole().equalsIgnoreCase("IE")){
        validateUserForPoi(current.getPoiCode(), req.getActionBy());
    }else if(current.getWorkflowId() == 2
            && current.getNextRole().equalsIgnoreCase("RIO Help Desk")) {

        // Get employee code from user_master
        String employeeCode = userMasterRepository
                .findEmployeeCodeByUserId(Math.toIntExact(req.getActionBy()));

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

        // Validate RIO mapping
        boolean exists = rioUserRepository
                .existsByRioAndEmployeeCode(current.getRio(), employeeCode);

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
    }else if(current.getWorkflowId()==2
            && current.getNextRole().equalsIgnoreCase("Main IE")){

        boolean exists = poiIeMappingRepository
                .existsByPoiCodeAndIeUserIdAndIeType(
                        current.getPoiCode(),
                        Math.toIntExact(req.getActionBy()),
                        "Main IE");

        if(!exists){
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "User is not mapped as Main IE for this POI"
                    )
            );
        }
    }
    String status =null;

    if(current.getWorkflowId()==1){
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

    // Workflow 2 → Use TRANSITION_MASTER
    if (current.getWorkflowId().equals(2L)) {

        SleeperTransitionMaster transition =
                sleeperTransitionMasterRepository
                        .findByWorkflowIdAndCurrentRoleIdAndCurrentAction(
                                current.getWorkflowId().intValue(),
                                getRoleId(current.getNextRole()),
                                req.getAction())
                        .orElseThrow(() -> new RuntimeException("Transition not configured"));


        tx.setCurrentRole(current.getNextRole());

        if (transition.getNextRoleId() != null) {
            tx.setNextRole(getRoleName(transition.getNextRoleId()));
        }

        if (transition.getNextRoleId() == null) {
            tx.setStatus(AppConstant.COMPLETED_TYPE);
        }else{
            tx.setStatus(AppConstant.PENDING_TYPE);
        }
        if (transition.getNextRoleId() != null && transition.getNextRoleId().equals(2)) {
            SleeperPincodePoIMapping poi =
                    sleeperPincodePoIMappingRepository.findByPoiCode(current.getPoiCode())
                            .orElseThrow(() -> new BusinessException(
                                    new ErrorDetails(
                                            AppConstant.ERROR_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_VALIDATION,
                                            "Invalid POI code"
                                    )
                            ));
            String stage = "F";
            String product = "Sleeper";
            String pinCode = poi.getPinCode();

            IEFieldsMapping map =
                    ieFieldsMappingRepository
                            .findByPinCodeProductAndStageMatch(pinCode, product, stage)
                            .orElseThrow(() -> new BusinessException(
                                    new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                            AppConstant.ERROR_TYPE_VALIDATION,
                                            "No IE mapping for given pin/product/stage")));

            String rio = map.getRio();

            tx.setRio(rio);
        }

    }
    else {
        // Existing workflow logic (workflowId = 1)

        if(req.getAction().equals("REQUEST_BACK")) {
            tx.setCurrentRole("IE");
            tx.setNextRole("Vendor");
        }
        else if(req.getAction().equals("RESUBMIT")){
            tx.setCurrentRole("Vendor");
            tx.setNextRole("IE");
        }
        else{
            tx.setCurrentRole("IE");
        }
    }

    tx.setAssignedToUser(req.getActionBy());

    tx.setCreatedBy(current.getCreatedBy());
    tx.setModifiedBy(req.getActionBy());
    tx.setCreatedDate(LocalDateTime.now());

    SleeperWorkflowTransaction saved = repository.save(tx);

    return mapToResponse(saved);
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

    private void validateNextRole(Long actionBy, String expectedRole) {

        String userRole =
                userMasterRepository.findRoleNameByUserId(Math.toIntExact(actionBy));

        if (userRole == null || !userRole.equalsIgnoreCase(expectedRole)) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "User is not allowed to perform this action. Expected role: " + expectedRole
                    )
            );
        }
    }

    private void validateUserForPoi(String poiCode, Long actionBy) {

        boolean exists = poiIeMappingRepository
                .existsByPoiCodeAndIeUserId(poiCode, Math.toIntExact(actionBy));

        if (!exists) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "User is not mapped to this POI"
                    )
            );
        }
    }


    @Override
    public List<SleeperWorkflowTransactionDto> allPendingWorkflowTransitions(String roleName) {


        List<SleeperWorkflowTransaction> list =
                repository.findLastPendingRequestsByRole(roleName);

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

/*
    @Override
    public List<SleeperWorkflowTransactionDto> getCompletedRequests() {

        List<SleeperWorkflowTransaction> list =
                repository.findLastCompletedRequests();

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }
*/

    @Override
    public List<SleeperWorkflowTransactionDto> workflowTransitionHistory(String requestId){
        List<SleeperWorkflowTransaction> list =
                repository.findByRequestIdOrderByCreatedDateAsc(requestId);

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SleeperWorkflowTransactionDto> allCompletedWorkflowTransitions() {

        List<SleeperWorkflowTransaction> list =
                repository.findCompletedRequests();

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }


}
