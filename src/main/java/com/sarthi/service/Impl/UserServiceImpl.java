package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.*;
import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.entity.*;
import com.sarthi.entity.ProcessIeUsers;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.*;
import com.sarthi.service.JwtService;
import com.sarthi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMasterRepository userMasterRepository;
    @Autowired
    private UserRoleMasterRepository userRoleMasterRepository;
    @Autowired
    private RoleMasterRepository roleMasterRepository;
    @Autowired
    private ClusterRioUserRepository clusterRioUserRepository;
    @Autowired
    private RegionClusterRepository regionClusterRepository;
    @Autowired
    private ClusterPrimaryIeRepository clusterPrimaryIeRepository;
    @Autowired
    private ClusterSecondaryIeRepository clusterSecondaryIeRepository;
    @Autowired
    private ClusterCmUserRepository clusterCmUserRepository;
    @Autowired
    private ProcessIeMasterRepository processIeMasterRepository;
    @Autowired
    private ProcessIeMappingRepository processIeMappingRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RegionSbuHeadRepository regionSbuHeadRepository;
    @Autowired
    private IeProfileRepository ieProfileRepository;
    @Autowired
    private IePincodePoiMappingRepository iePincodePoiMappingRepository;
    @Autowired
    private ieControllingManagerRepository ieControllingManagerRepository;
    @Autowired
    private RioUserRepository rioUserRepository;
    @Autowired
    private  ProcessIeUsersRepository processIeUsersRepository;
    @Autowired
    private IePoiMappingRepository iePoiMappingRepository;

/*
    @Override
    public UserDto createUser(userRequestDto userDto) {
        UserMaster userMaster = new UserMaster();
        userMaster.setUserName(userDto.getUserName());
        userMaster.setMobileNumber(userDto.getMobileNumber());
        userMaster.setPassword(userDto.getPassword());
        userMaster.setEmail(userDto.getEmail());
        userMaster.setCreatedBy(userDto.getCreatedBy());
        userMaster.setEmployeeId(userDto.getEmployeeId());

        // Save role names as comma-separated string in user_master
        String rolesAsString = String.join(",", userDto.getRoleNames());
        userMaster.setRoleName(rolesAsString);

        // Save user
        userMasterRepository.save(userMaster);

        // Save each role in user_role_master
        for (String roleName : userDto.getRoleNames()) {
            RoleMaster role = roleMasterRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Role with name '" + roleName + "' not found.")
                    ));

            UserRoleMaster userRole = new UserRoleMaster();
            userRole.setUserId(userMaster.getUserId());
            userRole.setRoleId(role.getRoleId());
            userRole.setReadPermission(true);
            userRole.setWritePermission(true);
            userRole.setCreatedBy(userDto.getCreatedBy());
            userRole.setCreatedDate(new Date());
            userRoleMasterRepository.save(userRole);

            if (roleName.equalsIgnoreCase("RIO Help Desk")) {

                // Save cluster → RIO user mapping
                ClusterRioUser map = new ClusterRioUser();
                map.setClusterName(userDto.getClusterName());
                map.setRioUserId(userMaster.getUserId());
                map.setRegionName(userDto.getRegionName());
                clusterRioUserRepository.save(map);
            }

        }

        return mapToResponseDTO(userMaster);
    }
*/

@Override
public UserDto createUser(userRequestDto userDto) {

    //Save user
    UserMaster userMaster = new UserMaster();
    userMaster.setUserName(userDto.getUserName());
    userMaster.setMobileNumber(userDto.getMobileNumber());
    userMaster.setPassword(userDto.getPassword());
    userMaster.setEmail(userDto.getEmail());
    userMaster.setCreatedBy(userDto.getCreatedBy());
    userMaster.setEmployeeId(userDto.getEmployeeId());

    userMaster.setEmployeeCode(userDto.getEmployeeCode());
    userMaster.setRitesEmployeeCode(userDto.getRitesEmployeeCode());
    userMaster.setEmploymentType(userDto.getEmploymentType());
    userMaster.setFullName(userDto.getFullName());
    userMaster.setShortName(userDto.getShortName());
    userMaster.setDesignation(userDto.getDesignation());
    userMaster.setDiscipline(userDto.getDiscipline());

    String rolesAsString = String.join(",", userDto.getRoleNames());
    userMaster.setRoleName(rolesAsString);

    userMasterRepository.save(userMaster);

    // Role-based logic
    for (String roleName : userDto.getRoleNames()) {

        RoleMaster role = roleMasterRepository.findByRoleName(roleName)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Role not found: " + roleName)));

        UserRoleMaster userRole = new UserRoleMaster();
        userRole.setUserId(userMaster.getUserId());
        userRole.setRoleId(role.getRoleId());
        userRole.setReadPermission(true);
        userRole.setWritePermission(true);
        userRole.setCreatedBy(userDto.getCreatedBy());
        userRole.setCreatedDate(new Date());


        // Validate cluster-region mapping (only for role types)
      /*  if (roleName.equalsIgnoreCase("RIO Help Desk")
               // || roleName.equalsIgnoreCase("IE")
              //  || roleName.equalsIgnoreCase("IE Secondary")
          ) {

            RegionCluster rc = regionClusterRepository
                    .findByClusterName(userDto.getClusterName())
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Cluster not found: " + userDto.getClusterName())
                    ));

            if (!rc.getRegionName().equalsIgnoreCase(userDto.getRegionName())) {
                throw new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Region mismatch for cluster: " + userDto.getClusterName())
                );
            }
        }*/

        // Save RIO mapping
        if (roleName.equalsIgnoreCase("RIO Help Desk")) {

//            ClusterRioUser map = new ClusterRioUser();
//            map.setClusterName(userDto.getClusterName());
//            map.setRioUserId(userMaster.getUserId());
//            clusterRioUserRepository.save(map);

            RioUser rio = new RioUser();
            rio.setRio(userDto.getRio());
            rio.setEmployeeCode(userDto.getEmployeeCode());

            rioUserRepository.save(rio);
        }


        // Save primary IE
//        if (roleName.equalsIgnoreCase("IE")) {
//
//            boolean exists = clusterPrimaryIeRepository
//                    .findByClusterName(userDto.getClusterName())
//                    .isPresent();
//
//            if (exists) {
//                throw new BusinessException(
//                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
//                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
//                                AppConstant.ERROR_TYPE_VALIDATION,
//                                "Primary IE already exists for this cluster: "
//                                        + userDto.getClusterName()
//                        ));
//            }
//            ClusterPrimaryIe p = new ClusterPrimaryIe();
//            p.setClusterName(userDto.getClusterName());
//            p.setIeUserId(userMaster.getUserId());
//            clusterPrimaryIeRepository.save(p);
//        }

        // Save secondary IE
//        if (roleName.equalsIgnoreCase("IE Secondary")) {
//
//            ClusterSecondaryIe s = new ClusterSecondaryIe();
//            s.setClusterName(userDto.getClusterName());
//            s.setIeUserId(userMaster.getUserId());
//            s.setPriorityOrder(userDto.getPriority());
//            clusterSecondaryIeRepository.save(s);
//        }

        // saveing the Cm can have multple cluster (now we mapping only one throught the user user creation we can do multiple throught another api
        if (roleName.equalsIgnoreCase("Control Manager")) {

            ClusterCmUser cmUser = new ClusterCmUser();
            cmUser.setClusterName(userDto.getClusterName());
            cmUser.setCmUserId(userMaster.getUserId());
            clusterCmUserRepository.save(cmUser);
        }

        // Save SBU Head mapping (1 region = 1 SBU Head)
        if (roleName.equalsIgnoreCase("SBU Head")) {

            // Check if SBU Head already exists for region
            Optional<RegionSbuHead> existing =
                    regionSbuHeadRepository.findByRegionName(userDto.getRegionName());

            if (existing.isPresent()) {
                throw new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "SBU Head already exists for region: " + userDto.getRegionName())
                );
            }

            // Save SBU Head
            RegionSbuHead sbu = new RegionSbuHead();
            sbu.setRegionName(userDto.getRegionName());
            sbu.setSbuHeadUserId(userMaster.getUserId());
            regionSbuHeadRepository.save(sbu);
        }
/*
        // Save Process IE + multiple IE mappings
        if (roleName.equalsIgnoreCase("Process IE")) {

            //Save Process IE master entry
            ProcessIeMaster p = new ProcessIeMaster();
            p.setProcessIeUserId(userMaster.getUserId());
            p.setClusterName(userDto.getClusterName());
            p.setCreatedBy(userDto.getCreatedBy());
            processIeMasterRepository.save(p);

            //Validate and Save IE Mapping List
            if (userDto.getIeUserIds() == null || userDto.getIeUserIds().isEmpty()) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "At least one IE must be mapped to Process IE"
                ));
            }

            for (Integer ieUserId : userDto.getIeUserIds()) {
                ProcessIeMapping mapping = new ProcessIeMapping();
                mapping.setProcessIeUserId(userMaster.getUserId());
                mapping.setIeUserId(ieUserId);
                mapping.setCreatedBy(userDto.getCreatedBy());
                processIeMappingRepository.save(mapping);
            }

    }*/
        if (roleName.equalsIgnoreCase("Process IE")) {

            if (userDto.getIePoiMappings() == null || userDto.getIePoiMappings().isEmpty()) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "IE and POI mapping is required for Process IE"
                ));
            }


            for (IePoiMappingDto ieDto : userDto.getIePoiMappings()) {

                // Save Process IE → IE mapping
                ProcessIeUsers map = new ProcessIeUsers();
                map.setProcessUserId(userMaster.getUserId().longValue());
                map.setIeUserId(ieDto.getIeUserId());
                map.setCreatedBy(Long.valueOf(userDto.getCreatedBy()));
                map.setCreatedDate(new Date());

                processIeUsersRepository.save(map);

                //  Save IE → multiple POIs (NEW TABLE)
                if (ieDto.getPoiCodes() != null && !ieDto.getPoiCodes().isEmpty()) {

                    for (String poi : ieDto.getPoiCodes()) {

                        IePoiMapping poiMap = new IePoiMapping();
                        poiMap.setIeUserId(ieDto.getIeUserId());
                        poiMap.setPoiCode(poi);
                        poiMap.setCreatedBy(Long.valueOf(userDto.getCreatedBy()));
                        poiMap.setCreatedDate(new Date());

                        iePoiMappingRepository.save(poiMap);
                    }
                }
            }

        }

        boolean isIeRole = userDto.getRoleNames().stream()
                .anyMatch(r -> r.equalsIgnoreCase("IE")
                        || r.equalsIgnoreCase("IE Secondary"));

        if (isIeRole) {

            // ---------- IE PROFILE ----------
            IeProfile ieProfile = new IeProfile();
            ieProfile.setEmployeeCode(userMaster.getEmployeeCode());
            ieProfile.setRio(userDto.getRio());
            ieProfile.setCurrentCityOfPosting(userDto.getCurrentCityOfPosting());
            ieProfile.setMetalStampNo(userDto.getMetalStampNo());
            ieProfileRepository.save(ieProfile);

            // ---------- IE PIN + POI ----------
            if (userDto.getIePinPoiList() != null) {
                for (IePinPoiDto dto : userDto.getIePinPoiList()) {

                    IePincodePoiMapping m = new IePincodePoiMapping();
                    m.setEmployeeCode(userMaster.getEmployeeCode());
                    m.setProduct(dto.getProduct());
                    m.setPinCode(dto.getPinCode());
                    m.setPoiCode(dto.getPoiCode());
                    m.setIeType(dto.getIeType()); // PRIMARY / SECONDARY

                    iePincodePoiMappingRepository.save(m);
                }
            }

            // ---------- IE → CONTROLLING MANAGER ----------
            if (userDto.getControllingManagerUserId() != null) {
                IeControllingManager cm = new IeControllingManager();
                cm.setIeEmployeeCode(userMaster.getEmployeeCode());
                cm.setCmUserId(userDto.getControllingManagerUserId());
                ieControllingManagerRepository.save(cm);
            }
        }


        userRoleMasterRepository.save(userRole);

    }

    return mapToResponseDTO(userMaster);
}


    private UserDto mapToResponseDTO(UserMaster userMaster) {

        UserDto userDto = new UserDto();
        userDto.setUserId(userMaster.getUserId());
        userDto.setUserName(userMaster.getUsername());
        userDto.setPassword(userMaster.getPassword());
        userDto.setMobileNumber(userMaster.getMobileNumber());
        userDto.setRoleName(userMaster.getRoleName());
        userDto.setCreatedDate(userMaster.getCreatedDate());
        userDto.setCreatedBy(userMaster.getCreatedBy());
        return userDto;
    }


    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        UserMaster user = userMasterRepository.findByUserId(loginRequestDto.getUserId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_INVALID,
                                AppConstant.ERROR_TYPE_INVALID,
                                "Invalid login credentials.")
                ));

        if (!loginRequestDto.getPassword().equals(user.getPassword())) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid login credentials.")
            );
        }

        String rio = rioUserRepository
                .findByEmployeeCode(user.getEmployeeCode())
                .map(RioUser::getRio)
                .orElse(null);

        String token = jwtService.generateToken(user);

        return new LoginResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getRoleName(),
                token,
                rio,
                user.getShortName()  // Include shortName for IC number generation

        );
    }

    @Override
    public LoginResponseDto loginBasedOnType(LoginRequestBasedTypeDto loginDto) {

        UserMaster user;

        String loginType = loginDto.getLoginType();
        String loginId   = loginDto.getLoginId();

        // ================= IE LOGIN =================
        if ("IE".equalsIgnoreCase(loginType)) {

            user = userMasterRepository
                    .findByEmployeeCode(loginId);
        }

        // ================= VENDOR LOGIN =================
        else if ("VENDOR".equalsIgnoreCase(loginType)) {

            user = userMasterRepository
                    .findByUserName(loginId).
                    orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_INVALID,
                                    AppConstant.ERROR_TYPE_CODE_INVALID,
                                    AppConstant.ERROR_TYPE_INVALID,
                                    "Invalid Vendor credentials."
                            )
                    ));
        }

        // ================= INVALID TYPE =================
        else {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid login type."
                    )
            );
        }

        // ================= PASSWORD CHECK =================
        if (!loginDto.getPassword().equals(user.getPassword())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid login credentials."
                    )
            );
        }

        // ================= RIO =================
        String rio = rioUserRepository
                .findByEmployeeCode(user.getEmployeeCode())
                .map(RioUser::getRio)
                .orElse(null);

        // ================= TOKEN =================
        String token = jwtService.generateToken(user);

        // ================= RESPONSE =================
        return new LoginResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getRoleName(),
                token,
                rio,
                user.getShortName()
        );
    }




    public UserDetails loadUserByUsername(Integer userId) throws UsernameNotFoundException {
        return userMasterRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + userId));
    }



    @Transactional
    @Override
    public UserDto createUserAndRole(userRequestDto userDto) {

        UserMaster userMaster = new UserMaster();

        userMaster.setUserName(userDto.getUserName());
        userMaster.setMobileNumber(userDto.getMobileNumber());
        userMaster.setPassword(userDto.getPassword());
        userMaster.setEmail(userDto.getEmail());
        userMaster.setCreatedBy(userDto.getCreatedBy());
        userMaster.setEmployeeId(userDto.getEmployeeId());

        userMaster.setEmployeeCode(userDto.getEmployeeCode());
        userMaster.setRitesEmployeeCode(userDto.getRitesEmployeeCode());
        userMaster.setEmploymentType(userDto.getEmploymentType());
        userMaster.setFullName(userDto.getFullName());
        userMaster.setShortName(userDto.getShortName());
        userMaster.setDesignation(userDto.getDesignation());
        userMaster.setDiscipline(userDto.getDiscipline());

        String rolesAsString = String.join(",", userDto.getRoleNames());
        userMaster.setRoleName(rolesAsString);

        userMasterRepository.save(userMaster);

        // Role-based logic
        for (String roleName : userDto.getRoleNames()) {

            RoleMaster role = roleMasterRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Role not found: " + roleName)));

            UserRoleMaster userRole = new UserRoleMaster();
            userRole.setUserId(userMaster.getUserId());
            userRole.setRoleId(role.getRoleId());
            userRole.setReadPermission(true);
            userRole.setWritePermission(true);
            userRole.setCreatedBy(userDto.getCreatedBy());
            userRole.setCreatedDate(new Date());

            userRoleMasterRepository.save(userRole);
        }

        return mapToResponseDTO(userMaster);
    }


    @Transactional
    public Object setupIe(Long userId, IeSetupRequestDto dto) {

        UserMaster user = userMasterRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // IE Profile
        IeProfile profile = new IeProfile();
        profile.setEmployeeCode(user.getEmployeeCode());
        profile.setRio(dto.getRio());
        profile.setCurrentCityOfPosting(dto.getCurrentCityOfPosting());
        profile.setMetalStampNo(dto.getMetalStampNo());

        ieProfileRepository.save(profile);

        // PIN + POI
        if (dto.getIePinPoiList() != null) {

            for (IePinPoiDto p : dto.getIePinPoiList()) {

                IePincodePoiMapping map = new IePincodePoiMapping();

                map.setEmployeeCode(user.getEmployeeCode());
                map.setProduct(p.getProduct());
                map.setPinCode(p.getPinCode());
                map.setPoiCode(p.getPoiCode());
                map.setIeType(p.getIeType());

                iePincodePoiMappingRepository.save(map);
            }
        }

        // Controlling Manager
        if (dto.getControllingManagerUserId() != null) {

            IeControllingManager cm = new IeControllingManager();

            cm.setIeEmployeeCode(user.getEmployeeCode());
            cm.setCmUserId(dto.getControllingManagerUserId());

            ieControllingManagerRepository.save(cm);
        }
        return null;
    }


    @Transactional
    @Override
    public Object mapProcessIe(Long userId,
                               ProcessIeMappingRequestDto dto,
                               String createdBy) {

//        if (dto.getIePoiMappings() == null || dto.getIePoiMappings().isEmpty()) {
//            throw new BusinessException("Mapping required");
//        }

        for (IePoiMappingDto ieDto : dto.getIePoiMappings()) {

            // Process IE → IE
            ProcessIeUsers map = new ProcessIeUsers();

            map.setProcessUserId(userId);
            map.setIeUserId(ieDto.getIeUserId());
            map.setCreatedBy(Long.valueOf(createdBy));
            map.setCreatedDate(new Date());

            processIeUsersRepository.save(map);

            // IE → POI
            for (String poi : ieDto.getPoiCodes()) {

                IePoiMapping poiMap = new IePoiMapping();

                poiMap.setIeUserId(ieDto.getIeUserId());
                poiMap.setPoiCode(poi);
                poiMap.setCreatedBy(Long.valueOf(createdBy));
                poiMap.setCreatedDate(new Date());

                iePoiMappingRepository.save(poiMap);
            }
        }
        return null;
    }




}
