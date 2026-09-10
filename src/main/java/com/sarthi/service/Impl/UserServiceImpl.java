package com.sarthi.service.Impl;

import com.sarthi.config.SecurityConfig;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.*;
import com.sarthi.dto.MFA.MfaLoginResponseDto;
import com.sarthi.dto.MFA.VerifyOtpRequestDto;
import com.sarthi.dto.WorkflowDtos.ProductCmDto;
import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.entity.*;
import com.sarthi.entity.PoiProcessIeMapping;
import com.sarthi.entity.ProcessIeUsers;
import com.sarthi.entity.mfa.LoginOtp;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.*;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.repository.mfa.LoginOtpRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.JwtService;
import com.sarthi.service.UserService;
import com.sarthi.entity.UserProfileAuditLog;
import com.sarthi.repository.UserProfileAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

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
    private ProcessIeUsersRepository processIeUsersRepository;
    @Autowired
    private IePoiMappingRepository iePoiMappingRepository;
    @Autowired
    private PincodePoIMappingRepository pincodePoIMappingRepository;
    @Autowired
    private InspectionCallRepository inspectionCallRepository;
    @Autowired
    private UserProductCmMappingRepository userProductCmMappingRepository;

    @Autowired
    private PoiProcessIeMappingRepository poiProcessIeMappingRepository;

    @Autowired
    private VendorMasterRepository vendorMasterRepository;

    @Autowired
    private EmployeeCodeSequenceRepository employeeCodeSequenceRepository;

    @Autowired
    private RailWorkflowTransactionRepository railWorkflowTransactionRepository;

    @Autowired
    private com.sarthi.SRailPad.repository.RailPoiIeMappingRepository railPoiIeMappingRepository;
    @Autowired
    private OtpService otpService;
    @Autowired
    private LoginOtpRepository loginOtpRepository;
    @Autowired
    private IeFieldsMappingRepository ieFieldsMappingRepository;
    @Autowired
    private com.sarthi.Sleeper.repository.SleeperPincodePoIMappingRepository sleeperPincodePoIMappingRepository;
    @Autowired
    private com.sarthi.Sleeper.repository.VendorPlantRepository vendorPlantRepository;
    @Autowired
    private com.sarthi.SRailPad.repository.RailPadPincodePoIMappingRepository railPadPincodePoIMappingRepository;
    @Autowired
    private com.sarthi.SRailPad.repository.RailVendorPlantsRepository railVendorPlantsRepository;
    @Autowired
    private com.sarthi.repository.WorkflowTransitionRepository workflowTransitionRepository;
    @Autowired
    private com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository railInspectionCallRepository;



    @Transactional
    @Override
    public UserDto createUser(userRequestDto userDto) {

        // Save user
        UserMaster userMaster;
        if (userDto.getUserId() != null) {
            userMaster = userMasterRepository.findById(userDto.getUserId()).orElse(new UserMaster());
        } else {
            userMaster = new UserMaster();
            userMaster.setCreatedDate(LocalDateTime.now());
        }

        userMaster.setUserName(userDto.getUserName());
        userMaster.setMobileNumber(userDto.getMobileNumber());
        userMaster.setAlternateMobileNumber(userDto.getAlternateMobileNumber());
        userMaster.setNotificationPreferences(userDto.getNotificationPreferences());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            userMaster.setPassword(com.sarthi.util.PasswordEncryptionUtil.encrypt(userDto.getPassword()));
        }
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
        userMaster.setZonalRly(userDto.getZonalRly());
        if ("Inactive".equalsIgnoreCase(userDto.getStatus())){
            userMaster.setStatus(AppConstant.USER_STATUS_INACTIVE);
        }else{
            userMaster.setStatus(AppConstant.USER_STATUS);
        }

        userMaster.setDateOfBirth(userDto.getDateOfBirth());
        userMaster.setRio(userDto.getRio());
        if (userDto.getProfilePhotoPath() != null) {
            userMaster.setProfilePhotoPath(userDto.getProfilePhotoPath());
        }

        String rolesAsString = String.join(",", userDto.getRoleNames());
        userMaster.setRoleName(rolesAsString);

        userMasterRepository.save(userMaster);
 
        // If update, clear old roles to prevent duplicates
        if (userDto.getUserId() != null) {
            userRoleMasterRepository.deleteByUserId(userMaster.getUserId());
        }
 
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



            // Save RIO mapping
            if (roleName.equalsIgnoreCase("RIO Help Desk")) {

                // ClusterRioUser map = new ClusterRioUser();
                // map.setClusterName(userDto.getClusterName());
                // map.setRioUserId(userMaster.getUserId());
                // clusterRioUserRepository.save(map);

                RioUser rio = new RioUser();
                rio.setRio(userDto.getRio());
                rio.setEmployeeCode(userDto.getEmployeeCode());

                rioUserRepository.save(rio);
            }

            if (userDto.getProductCmMappings() != null) {

                for (ProductCmDto dto : userDto.getProductCmMappings()) {

                    UserProductCmMapping mapping =
                            new UserProductCmMapping();

                    mapping.setUserEmployeeCode(
                            userMaster.getEmployeeCode());

                    mapping.setProductType(dto.getProductType());

                    mapping.setCmEmployeeCode(
                            dto.getCmEmployeeCode());

                    mapping.setCreatedBy(
                            Long.valueOf(userDto.getCreatedBy()));

                    mapping.setCreatedDate(new Date());

                    userProductCmMappingRepository.save(mapping);
                }
            }

            // Save SBU Head mapping (1 region = 1 SBU Head)
            if (roleName.equalsIgnoreCase("SBU Head")) {

                // Check if SBU Head already exists for region
                Optional<RegionSbuHead> existing = regionSbuHeadRepository.findByRegionName(userDto.getRegionName());

                if (existing.isPresent()) {
                    throw new BusinessException(
                            new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "SBU Head already exists for region: " + userDto.getRegionName()));
                }

                // Save SBU Head
                RegionSbuHead sbu = new RegionSbuHead();
                sbu.setRegionName(userDto.getRegionName());
                sbu.setSbuHeadUserId(userMaster.getUserId());
                regionSbuHeadRepository.save(sbu);
            }
            if (roleName.equalsIgnoreCase("ZONAL RAILWAY")) {

                String roleCode = "ZR";
                String zoneCode = userDto.getZonalRly(); // CR

                EmployeeCodeSequence seq =
                        employeeCodeSequenceRepository
                                .findByRoleCodeAndZoneCode(roleCode, zoneCode)
                                .orElse(null);

                int nextNumber = 1;

                if (seq == null) {

                    seq = new EmployeeCodeSequence();
                    seq.setRoleCode(roleCode);
                    seq.setZoneCode(zoneCode);
                    seq.setLastNumber(1);

                } else {

                    nextNumber = seq.getLastNumber() + 1;
                    seq.setLastNumber(nextNumber);
                }

                employeeCodeSequenceRepository.save(seq);

                String employeeCode =
                        roleCode +
                                zoneCode +
                                String.format("%02d", nextNumber);

                userMaster.setEmployeeCode(employeeCode);
            }

//
//            if (roleName.equalsIgnoreCase("Process IE")) {
//
//                if (userDto.getIePoiMappings() != null && !userDto.getIePoiMappings().isEmpty()) {
//
//                    for (IePoiMappingDto ieDto : userDto.getIePoiMappings()) {
//
//                        // Save Process IE → IE mapping
//                        ProcessIeUsers map = new ProcessIeUsers();
//                        map.setProcessUserId(userMaster.getUserId().longValue());
//                        map.setIeUserId(ieDto.getIeUserId());
//                        map.setCreatedBy(Long.valueOf(userDto.getCreatedBy()));
//                        map.setCreatedDate(new Date());
//
//                        processIeUsersRepository.save(map);
//
//                        // Save IE → multiple POIs (NEW TABLE)
//                        if (ieDto.getPoiCodes() != null && !ieDto.getPoiCodes().isEmpty()) {
//
//                            for (String poi : ieDto.getPoiCodes()) {
//
//                                IePoiMapping poiMap = new IePoiMapping();
//                                poiMap.setIeUserId(ieDto.getIeUserId());
//                                poiMap.setPoiCode(poi);
//                                poiMap.setCreatedBy(Long.valueOf(userDto.getCreatedBy()));
//                                poiMap.setCreatedDate(new Date());
//
//                                iePoiMappingRepository.save(poiMap);
//                            }
//                        }
//                    }
//                }
//            }

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
        return mapToResponseDTOWithContext(userMaster, null, null, null, null, null);
    }

    private UserDto mapToResponseDTOWithContext(
            UserMaster userMaster,
            Map<String, String> vendorCodeToName,
            Map<String, String> rioUserMap,
            Map<String, List<PincodePoIMapping>> vendorCodeToPpm,
            Map<String, String> ieFieldsRioMap,
            Map<Integer, List<String>> userRolesMap) {

        UserDto userDto = new UserDto();
        userDto.setUserId(userMaster.getUserId());
        userDto.setUserName(userMaster.getUsername());
        userDto.setPassword(com.sarthi.util.PasswordEncryptionUtil.decrypt(userMaster.getPassword()));
        userDto.setMobileNumber(userMaster.getMobileNumber());
        userDto.setCreatedDate(userMaster.getCreatedDate());
        userDto.setCreatedBy(userMaster.getCreatedBy());

        // Resolve all roles from user_role_master
        List<String> rolesFromMaster = (userRolesMap != null && userMaster.getUserId() != null) 
                ? userRolesMap.get(userMaster.getUserId()) 
                : null;
        if (rolesFromMaster == null && userMaster.getUserId() != null) {
            try {
                rolesFromMaster = userMasterRepository.findRoleNamesByUserId(userMaster.getUserId());
            } catch (Exception ignored) {}
        }

        if (rolesFromMaster != null && !rolesFromMaster.isEmpty()) {
            userDto.setRoleNames(rolesFromMaster);
            userDto.setRoleName(String.join(", ", rolesFromMaster));
        } else if (userMaster.getRoleName() != null && !userMaster.getRoleName().trim().isEmpty()) {
            userDto.setRoleName(userMaster.getRoleName());
            userDto.setRoleNames(Arrays.stream(userMaster.getRoleName().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        } else {
            userDto.setRoleNames(Collections.emptyList());
            userDto.setRoleName("");
        }

        // 1. Resolve employeeCode
        String allRoles = userDto.getRoleName();
        boolean isVendor = allRoles != null && 
                (allRoles.contains("Vendor") || allRoles.equalsIgnoreCase("Vendor"));

        String empCode = userMaster.getEmployeeCode();
        if (empCode == null || empCode.trim().isEmpty()) {
            if (userMaster.getUsername() != null && !userMaster.getUsername().trim().isEmpty()) {
                empCode = userMaster.getUsername().trim();
            } else if (userMaster.getEmail() != null && userMaster.getEmail().contains("@")) {
                empCode = userMaster.getEmail().substring(0, userMaster.getEmail().indexOf("@")).trim();
            }
        }
        if (isVendor && empCode != null && !empCode.trim().isEmpty()) {
            if (!empCode.startsWith(":")) {
                empCode = ":" + empCode.trim();
            }
        }
        userDto.setEmployeeCode(empCode);

        // 2. Resolve fullName
        String fullName = userMaster.getFullName();

        if ((fullName == null || fullName.trim().isEmpty()) && empCode != null && !empCode.trim().isEmpty()) {
            if (isVendor) {
                if (vendorCodeToName != null) {
                    fullName = vendorCodeToName.get(empCode);
                    if (fullName == null) {
                        fullName = vendorCodeToName.get(":" + empCode);
                    }
                } else {
                    String vCodeCol = ":" + empCode;
                    Optional<VendorMaster> vmOpt = vendorMasterRepository.findByVendorCode(vCodeCol);
                    if (vmOpt.isEmpty()) {
                        vmOpt = vendorMasterRepository.findByVendorCode(empCode);
                    }
                    if (vmOpt.isPresent() && vmOpt.get().getVendorName() != null && !vmOpt.get().getVendorName().trim().isEmpty()) {
                        fullName = vmOpt.get().getVendorName().trim();
                    }
                }

                if ((fullName == null || fullName.trim().isEmpty())) {
                    List<PincodePoIMapping> mappings = null;
                    if (vendorCodeToPpm != null) {
                        mappings = vendorCodeToPpm.get(empCode);
                        if (mappings == null) mappings = vendorCodeToPpm.get(":" + empCode);
                    } else {
                        mappings = pincodePoIMappingRepository.findByVendorCode(":" + empCode);
                        if (mappings.isEmpty()) {
                            mappings = pincodePoIMappingRepository.findByVendorCode(empCode);
                        }
                    }
                    if (mappings != null && !mappings.isEmpty() && mappings.get(0).getCompanyName() != null) {
                        fullName = mappings.get(0).getCompanyName().trim();
                    }
                }
            }
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = userMaster.getUsername() != null ? userMaster.getUsername().replaceAll("^:", "") : empCode;
        }
        userDto.setFullName(fullName);

        // 3. Resolve RIO (Region)
        String rio = userMaster.getRio();
        if ((rio == null || rio.trim().isEmpty())) {
            if (empCode != null && empCode.startsWith("ZR")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("^ZR([A-Z]+)\\d+$").matcher(empCode);
                if (m.find()) {
                    rio = m.group(1);
                }
            } else if (isVendor && empCode != null && !empCode.trim().isEmpty()) {
                List<PincodePoIMapping> mappings = null;
                if (vendorCodeToPpm != null) {
                    mappings = vendorCodeToPpm.get(empCode);
                    if (mappings == null) mappings = vendorCodeToPpm.get(":" + empCode);
                } else {
                    mappings = pincodePoIMappingRepository.findByVendorCode(":" + empCode);
                    if (mappings.isEmpty()) {
                        mappings = pincodePoIMappingRepository.findByVendorCode(empCode);
                    }
                }
                if (mappings != null && !mappings.isEmpty()) {
                    Set<String> rios = new LinkedHashSet<>();
                    for (PincodePoIMapping ppm : mappings) {
                        if (ppm.getPinCode() != null && !ppm.getPinCode().trim().isEmpty()) {
                            String r = null;
                            if (ieFieldsRioMap != null) {
                                r = ieFieldsRioMap.get(ppm.getPinCode().trim());
                            } else {
                                Optional<IEFieldsMapping> ieOpt = ieFieldsMappingRepository.findFirstByPinCodeAndProduct(ppm.getPinCode().trim(), "ERC");
                                if (ieOpt.isPresent()) r = ieOpt.get().getRio();
                            }
                            if (r != null && !r.trim().isEmpty()) {
                                rios.add(r.trim());
                            } else if (ppm.getState() != null && !ppm.getState().trim().isEmpty()) {
                                rios.add(deriveRioFromState(ppm.getState().trim()));
                            }
                        }
                    }
                    if (!rios.isEmpty()) {
                        rio = String.join(", ", rios);
                    }
                }
            } else if (empCode != null && !empCode.trim().isEmpty()) {
                if (rioUserMap != null) {
                    rio = rioUserMap.get(empCode);
                } else {
                    Optional<RioUser> rioUserOpt = rioUserRepository.findFirstByEmployeeCode(empCode);
                    if (rioUserOpt.isPresent() && rioUserOpt.get().getRio() != null) {
                        rio = rioUserOpt.get().getRio();
                    }
                }
            }
        }
        userDto.setRio(rio);

        // Add additional fields
        userDto.setDesignation(userMaster.getDesignation());
        userDto.setDiscipline(userMaster.getDiscipline());
        userDto.setEmploymentType(userMaster.getEmploymentType());
        userDto.setDateOfBirth(userMaster.getDateOfBirth());
        userDto.setEmail(userMaster.getEmail());
        userDto.setAlternateMobileNumber(userMaster.getAlternateMobileNumber());
        userDto.setNotificationPreferences(userMaster.getNotificationPreferences());
        userDto.setShortName(userMaster.getShortName() != null ? userMaster.getShortName() : empCode);
        userDto.setProductType(userMaster.getProductType());
        userDto.setProfilePhotoPath(userMaster.getProfilePhotoPath());
        userDto.setStatus(userMaster.getStatus() != null ? userMaster.getStatus() : AppConstant.USER_STATUS);

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
                                "Invalid login credentials.")));

        if (!com.sarthi.util.PasswordEncryptionUtil.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid login credentials."));
        }
        List<UserRoleMaster> userRoles = userRoleMasterRepository.findByUserId(user.getUserId());

        List<String> roleNames = userRoles.stream()
                .map(userRole -> roleMasterRepository.findByRoleId(userRole.getRoleId())
                        .map(RoleMaster::getRoleName)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        // ================= RIO =================
        String rio = rioUserRepository
                .findFirstByEmployeeCode(user.getEmployeeCode())
                .map(RioUser::getRio)
                .orElse(null);

        String token = jwtService.generateToken(user);

        String vendorName = null;
        if (roleNames.contains("Vendor") || roleNames.contains("Sleeper Vendor") || roleNames.contains("Rail Vendor")) {
            Optional<VendorMaster> vendorOpt = vendorMasterRepository.findByVendorCode(user.getUsername());
            if (vendorOpt.isEmpty() && user.getUsername().startsWith(":")) {
                vendorOpt = vendorMasterRepository.findByVendorCode(user.getUsername().substring(1));
            }
            if (vendorOpt.isPresent()) {
                vendorName = vendorOpt.get().getVendorName();
            }
        }

        user.setLastLoginDate(LocalDateTime.now());
        userMasterRepository.save(user);

        return new LoginResponseDto(
                user.getUserId(),
                user.getUsername(),
                vendorName,
                roleNames,
                token,
                rio,
                user.getShortName(), // Include shortName for IC number generation
                user.getEmployeeCode()
        );
    }
/*
    @Override
    public LoginResponseDto loginBasedOnType(LoginRequestBasedTypeDto loginDto) {

        UserMaster user;

        String loginType = loginDto.getLoginType();
        String loginId = loginDto.getLoginId();

        // ================= IE LOGIN =================
        if ("IE".equalsIgnoreCase(loginType)) {
            user = userMasterRepository.findFirstByEmployeeCode(loginId).orElse(null);
            if (user == null) {
                throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_INVALID, AppConstant.ERROR_TYPE_CODE_INVALID, AppConstant.ERROR_TYPE_INVALID, "Invalid login credentials."));
            }
        }

        // ================= VENDOR LOGIN =================
        else if ("VENDOR".equalsIgnoreCase(loginType)) {

            user = userMasterRepository
                    .findFirstByUserName(loginId).orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_INVALID,
                                    AppConstant.ERROR_TYPE_CODE_INVALID,
                                    AppConstant.ERROR_TYPE_INVALID,
                                    "Invalid Vendor credentials.")));
        }

        // ================= INVALID TYPE =================
        else {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid login type."));
        }

        // ================= STATUS CHECK =================
        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Your account is inactive. Please contact the administrator."));
        }

        // ================= PASSWORD CHECK =================
        if (!loginDto.getPassword().equals(user.getPassword())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid login credentials."));
        }
        List<UserRoleMaster> userRoles = userRoleMasterRepository.findByUserId(user.getUserId());

        List<String> roleNames = userRoles.stream()
                .map(userRole -> roleMasterRepository.findByRoleId(userRole.getRoleId())
                        .map(RoleMaster::getRoleName)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
        // ================= RIO =================
        String rio = rioUserRepository
                .findFirstByEmployeeCode(user.getEmployeeCode())
                .map(RioUser::getRio)
                .orElse(null);

        // ================= TOKEN =================
        String token = jwtService.generateToken(user);

        String vendorName = null;
        if (roleNames.contains("Vendor") || roleNames.contains("Sleeper Vendor") || roleNames.contains("Rail Vendor")) {
            Optional<VendorMaster> vendorOpt = vendorMasterRepository.findByVendorCode(user.getUsername());
            if (vendorOpt.isEmpty() && user.getUsername().startsWith(":")) {
                vendorOpt = vendorMasterRepository.findByVendorCode(user.getUsername().substring(1));
            }
            if (vendorOpt.isPresent()) {
                vendorName = vendorOpt.get().getVendorName();
            }
        }

        user.setLastLoginDate(LocalDateTime.now());
        userMasterRepository.save(user);

        return new LoginResponseDto(
                user.getUserId(),
                user.getUsername(),
                vendorName,
                roleNames,
                token,
                rio,
                user.getShortName(),
                user.getEmployeeCode());
    }
*/

    @Override
    public Object loginBasedOnType(LoginRequestBasedTypeDto loginDto) {

        UserMaster user;

        String loginType = loginDto.getLoginType();
        String loginId = loginDto.getLoginId();

        // ================= IE LOGIN =================
        if ("IE".equalsIgnoreCase(loginType)) {

            user = userMasterRepository
                    .findFirstByEmployeeCode(loginId)
                    .orElse(null);

            if (user == null) {
                throw new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_INVALID,
                                AppConstant.ERROR_TYPE_INVALID,
                                "Invalid login credentials."
                        )
                );
            }
        }

        // ================= VENDOR LOGIN =================
        else if ("VENDOR".equalsIgnoreCase(loginType)) {

            user = userMasterRepository
                    .findFirstByUserName(loginId)
                    .orElseThrow(() ->
                            new BusinessException(
                                    new ErrorDetails(
                                            AppConstant.ERROR_CODE_INVALID,
                                            AppConstant.ERROR_TYPE_CODE_INVALID,
                                            AppConstant.ERROR_TYPE_INVALID,
                                            "Invalid Vendor credentials."
                                    )
                            )
                    );
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

        // ================= STATUS CHECK =================
        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Your account is inactive. Please contact the administrator."
                    )
            );
        }

        // ================= PASSWORD CHECK =================
        if (!com.sarthi.util.PasswordEncryptionUtil.matches(loginDto.getPassword(), user.getPassword())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid login credentials."
                    )
            );
        }


        // ============================================================
        // MFA START
        // ============================================================
        // Password is correct.
        //
        // DO NOT generate JWT here.
        // DO NOT execute your existing role/token code here.
        //
        // Generate OTP and send it to registered mobile number.
        // ============================================================

        String transactionId = otpService.generateAndSendOtp(user);

        String mobile = user.getMobileNumber();
        boolean hasMobile = mobile != null && !mobile.trim().isEmpty();

        String noticeMessage;
        if (hasMobile && mobile.trim().length() >= 4) {
            String cleanMobile = mobile.trim();
            noticeMessage = "OTP sent to your registered mobile number ending with •••• " + cleanMobile.substring(cleanMobile.length() - 4) + ".";
        } else {
            noticeMessage = "Mobile number not found. Please enter default OTP •••• 123456.";
        }

        return new MfaLoginResponseDto(
                true,
                transactionId,
                noticeMessage
        );
    }

    @Override
    public LoginResponseDto verifyOtp(VerifyOtpRequestDto request) {

        // ============================================================
        // STEP 1: Find OTP
        // ============================================================

        Long otpId;

        try {
            otpId = Long.parseLong(request.getTransactionId());

        } catch (NumberFormatException e) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid OTP transaction."
                    )
            );
        }


        // ============================================================
        // STEP 2: Get OTP record
        // ============================================================

        LoginOtp loginOtp =
                loginOtpRepository.findById(otpId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        new ErrorDetails(
                                                AppConstant.ERROR_CODE_INVALID,
                                                AppConstant.ERROR_TYPE_CODE_INVALID,
                                                AppConstant.ERROR_TYPE_INVALID,
                                                "Invalid or expired OTP."
                                        )
                                )
                        );


        // ============================================================
        // STEP 3: Check OTP already used
        // ============================================================

        if (Boolean.TRUE.equals(loginOtp.getUsed())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "OTP has already been used."
                    )
            );
        }


        // ============================================================
        // STEP 4: Check OTP expiry
        // ============================================================

        if (LocalDateTime.now().isAfter(loginOtp.getExpiresAt())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "OTP has expired. Please request a new OTP."
                    )
            );
        }


        // ============================================================
        // STEP 5: Maximum attempts
        // ============================================================

        if (loginOtp.getAttemptCount() >= 5) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Maximum OTP attempts exceeded."
                    )
            );
        }


        // ============================================================
        // STEP 6: Get USER
        // ============================================================

        UserMaster user =
                userMasterRepository
                        .findByUserId(Math.toIntExact(loginOtp.getUserId()))
                        .orElseThrow(() ->
                                new BusinessException(
                                        new ErrorDetails(
                                                AppConstant.ERROR_CODE_INVALID,
                                                AppConstant.ERROR_TYPE_CODE_INVALID,
                                                AppConstant.ERROR_TYPE_INVALID,
                                                "User not found."
                                        )
                                )
                        );


        // ============================================================
        // STEP 7: Verify OTP
        // Users WITH saved mobile number -> MUST enter real OTP
        // Users WITHOUT saved mobile number -> ALLOW 123456 fallback
        // ============================================================

        String userMobile = user.getMobileNumber();
        boolean hasSavedMobile = userMobile != null && !userMobile.trim().isEmpty();

        boolean otpCorrect;
        if (hasSavedMobile) {
            // Strict verification against generated SMS OTP
            otpCorrect = request.getOtp() != null && request.getOtp().trim().equals(loginOtp.getOtp());
        } else {
            // Fallback for users with no mobile number saved
            otpCorrect = "123456".equals(request.getOtp() != null ? request.getOtp().trim() : "")
                    || (request.getOtp() != null && request.getOtp().trim().equals(loginOtp.getOtp()));
        }


        // ============================================================
        // WRONG OTP
        // ============================================================

        if (!otpCorrect) {

            loginOtp.setAttemptCount(
                    loginOtp.getAttemptCount() + 1
            );

            loginOtpRepository.save(loginOtp);

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid OTP."
                    )
            );
        }


        // ============================================================
        // OTP CORRECT
        // ============================================================

        loginOtp.setUsed(true);

        loginOtpRepository.save(loginOtp);


        // ============================================================
        // FROM HERE:
        // YOUR EXISTING LOGIN CODE
        // ============================================================

        List<UserRoleMaster> userRoles =
                userRoleMasterRepository
                        .findByUserId(user.getUserId());


        List<String> roleNames =
                userRoles.stream()
                        .map(userRole ->
                                roleMasterRepository
                                        .findByRoleId(
                                                userRole.getRoleId()
                                        )
                                        .map(RoleMaster::getRoleName)
                                        .orElse(null)
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        if (roleNames.isEmpty() && user.getRoleName() != null && !user.getRoleName().trim().isEmpty()) {
            roleNames = Arrays.stream(user.getRoleName().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }


        // ================= RIO =================

        String rio =
                rioUserRepository
                        .findFirstByEmployeeCode(
                                user.getEmployeeCode()
                        )
                        .map(RioUser::getRio)
                        .orElse(null);


        // ================= TOKEN =================

        // IMPORTANT:
        // JWT is generated ONLY AFTER OTP verification.

        String token =
                jwtService.generateToken(user);


        // ================= VENDOR =================

        String vendorName = null;

        if (roleNames.contains("Vendor")
                || roleNames.contains("Sleeper Vendor")
                || roleNames.contains("Rail Vendor")
                || (user.getRoleName() != null && user.getRoleName().toUpperCase().contains("VENDOR"))) {

            Optional<VendorMaster> vendorOpt =
                    vendorMasterRepository
                            .findByVendorCode(
                                    user.getUsername()
                            );

            if (vendorOpt.isEmpty()
                    && user.getUsername().startsWith(":")) {

                vendorOpt =
                        vendorMasterRepository
                                .findByVendorCode(
                                        user.getUsername()
                                                .substring(1)
                                );
            }

            if (vendorOpt.isPresent()) {

                vendorName =
                        vendorOpt.get().getVendorName();
            }
        }


        // ================= LAST LOGIN =================

        user.setLastLoginDate(
                LocalDateTime.now()
        );

        userMasterRepository.save(user);


        // ================= EXISTING RESPONSE =================

        return new LoginResponseDto(
                user.getUserId(),
                user.getUsername(),
                vendorName,
                roleNames,
                token,
                rio,
                user.getShortName(),
                user.getEmployeeCode()
        );
    }


    public UserDetails loadUserByUsername(Integer userId) throws UsernameNotFoundException {
        return userMasterRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + userId));
    }

    @Transactional
    @Override
    public UserDto createUserAndRole(userRequestDto userDto) {

        UserMaster userMaster;
        if (userDto.getUserId() != null) {
            userMaster = userMasterRepository.findById(userDto.getUserId()).orElse(new UserMaster());
        } else {
            userMaster = new UserMaster();
        }

        userMaster.setUserName(userDto.getUserName());
        userMaster.setMobileNumber(userDto.getMobileNumber());
        userMaster.setAlternateMobileNumber(userDto.getAlternateMobileNumber());
        userMaster.setNotificationPreferences(userDto.getNotificationPreferences());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            userMaster.setPassword(com.sarthi.util.PasswordEncryptionUtil.encrypt(userDto.getPassword()));
        }
        userMaster.setEmail(userDto.getEmail());
        userMaster.setCreatedBy(userDto.getCreatedBy());
        userMaster.setEmployeeId(userDto.getEmployeeId());
        userMaster.setProductType(userDto.getProductType());

        userMaster.setEmployeeCode(userDto.getEmployeeCode());
        userMaster.setRitesEmployeeCode(userDto.getRitesEmployeeCode());
        userMaster.setEmploymentType(userDto.getEmploymentType());
        userMaster.setFullName(userDto.getFullName());
        userMaster.setShortName(userDto.getShortName());
        userMaster.setDesignation(userDto.getDesignation());
        userMaster.setDiscipline(userDto.getDiscipline());

        userMaster.setDateOfBirth(userDto.getDateOfBirth());
        userMaster.setRio(userDto.getRio());

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

        // if (dto.getIePoiMappings() == null || dto.getIePoiMappings().isEmpty()) {
        // throw new BusinessException("Mapping required");
        // }

        for (IePoiMappingDto ieDto : dto.getIePoiMappings()) {

            // Process IE → IE
            ProcessIeUsers map = new ProcessIeUsers();

            map.setProcessUserId(userId);
            map.setIeUserId(ieDto.getIeUserId());
            map.setCreatedBy(safeParseLong(createdBy));
            map.setCreatedDate(new Date());

            processIeUsersRepository.save(map);

            // IE → POI
            for (String poi : ieDto.getPoiCodes()) {

                IePoiMapping poiMap = new IePoiMapping();

                poiMap.setIeUserId(ieDto.getIeUserId());
                poiMap.setPoiCode(poi);
                poiMap.setCreatedBy(safeParseLong(createdBy));
                poiMap.setCreatedDate(new Date());

                iePoiMappingRepository.save(poiMap);
            }
        }
        return null;
    }

    @Override
    public List<String> getAllRoleNames() {
        return roleMasterRepository.findAll().stream()
                .map(RoleMaster::getRoleName)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getUsersByRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return getAllUsers();
        }

        List<UserMaster> matchedUsers = userMasterRepository.findUsersByRoleNameViaJoin(roleName);
        if (matchedUsers == null || matchedUsers.isEmpty()) {
            matchedUsers = userMasterRepository.findByRoleNameContaining(roleName);
        }
        if (matchedUsers == null || matchedUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Bulk preload User Roles for matched users in one single query
        Map<Integer, List<String>> userRolesMap = new HashMap<>();
        try {
            List<Integer> userIds = matchedUsers.stream()
                    .map(UserMaster::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            if (!userIds.isEmpty()) {
                List<Object[]> roleRows = userRoleMasterRepository.findUserRolesByUserIds(userIds);
                for (Object[] row : roleRows) {
                    if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                        Integer uId = ((Number) row[0]).intValue();
                        String rName = row[1].toString().trim();
                        userRolesMap.computeIfAbsent(uId, k -> new ArrayList<>()).add(rName);
                    }
                }
            }
        } catch (Exception ignored) {}

        // 2. Bulk preload RioUser mapping
        Map<String, String> rioUserMap = new HashMap<>();
        try {
            for (RioUser ru : rioUserRepository.findAll()) {
                if (ru.getEmployeeCode() != null && ru.getRio() != null) {
                    rioUserMap.put(ru.getEmployeeCode().trim(), ru.getRio().trim());
                }
            }
        } catch (Exception ignored) {}

        // 3. Map with context (zero per-row database queries)
        return matchedUsers.stream()
                .map(u -> mapToResponseDTOWithContext(u, null, rioUserMap, null, null, userRolesMap))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCompanies() {
        return pincodePoIMappingRepository.findAllDistinctCompanyNames();
    }

    @Override
    public List<String> getUnitsByCompany(String companyName) {
        return pincodePoIMappingRepository.findUnitNamesByCompanyName(companyName);
    }

    @Override
    public PincodePoIMapping getMappingByCompanyAndUnit(String companyName, String unitName) {
        return pincodePoIMappingRepository.findByCompanyNameAndUnitName(companyName, unitName)
                .orElse(null);
    }

    private Long safeParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 1L; // Default to system/admin ID if not a number
        }
    }



    @Override
    public List<CompanyUnitIeResponseDto> getAllCompanyMappedIe() {

        List<Object[]> rows = pincodePoIMappingRepository.findAllCompanyUnitIe();

        List<CompanyUnitIeResponseDto> list = new ArrayList<>();

        for (Object[] r : rows) {

            CompanyUnitIeResponseDto dto = new CompanyUnitIeResponseDto();

            dto.setCompanyName((String) r[0]);
            dto.setUnitName((String) r[1]);
            dto.setEmployeeCode((String) r[2]);
            dto.setRio((String) r[3]);

            list.add(dto);
        }

        return list;
    }

    @Override
    public List<CompanyUnitProcessIeDto> getCompanyUnitProcessIe() {

        List<Object[]> rows = pincodePoIMappingRepository.findCompanyUnitEmployees();

        List<CompanyUnitProcessIeDto> list = new ArrayList<>();

        for (Object[] r : rows) {

            CompanyUnitProcessIeDto dto = new CompanyUnitProcessIeDto();

            dto.setCompanyName((String) r[0]);
            dto.setUnitName((String) r[1]);
            dto.setEmployeeCode((String) r[2]);

            list.add(dto);
        }

        return list;
    }


    @Override
    public List<String> getEmployeeCodesByCallNo(String callNo) {

        if (callNo == null || callNo.trim().isEmpty()) {
            return new ArrayList<>();
        }

        callNo = callNo.trim();
        String prefix = callNo.split("-")[0];

        String poiCode = null;
        String plantId = null;

        if ("RPF".equalsIgnoreCase(prefix) || "RPP".equalsIgnoreCase(prefix) || prefix.toUpperCase().startsWith("RP")) {
            com.sarthi.SRailPad.entity.RailWorkflowTransaction tx = railWorkflowTransactionRepository.findFirstByRequestIdOrderByWorkflowTransitionIdDesc(callNo);
            if (tx != null) {
                poiCode = tx.getPoiCode();
                plantId = tx.getPlantId();
                if (tx.getAssignedToUser() != null) {
                    Optional<UserMaster> optUser = userMasterRepository.findById(Math.toIntExact(tx.getAssignedToUser()));
                    if (optUser.isPresent()) {
                        List<String> list = new ArrayList<>();
                        list.add(optUser.get().getEmployeeCode() + " - " + (optUser.get().getFullName() != null && !optUser.get().getFullName().isBlank() ? optUser.get().getFullName() : optUser.get().getUsername()));
                        return list;
                    }
                }
            }
            if (plantId == null || plantId.trim().isEmpty()) {
                if (railInspectionCallRepository != null) {
                    Optional<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> optCall = railInspectionCallRepository.findByCallNo(callNo);
                    if (optCall.isPresent()) {
                        plantId = optCall.get().getPlantId();
                        if (poiCode == null || poiCode.trim().isEmpty()) {
                            poiCode = optCall.get().getVendorCode();
                        }
                    }
                }
            }
        } else {
            poiCode = inspectionCallRepository.findPoiByCallNo(callNo);
        }

        // For ER / EF calls: if already verified and assigned to a specific IE, return that assigned IE
        if ("ER".equalsIgnoreCase(prefix) || "EF".equalsIgnoreCase(prefix)) {
            WorkflowTransition tx = workflowTransitionRepository.findTopByRequestIdOrderByWorkflowTransitionIdDesc(callNo);
            if (tx != null && tx.getAssignedToUser() != null) {
                Optional<UserMaster> optUser = userMasterRepository.findById(tx.getAssignedToUser());
                if (optUser.isPresent()) {
                    List<String> list = new ArrayList<>();
                    list.add(optUser.get().getFullName() + " (" + optUser.get().getEmployeeCode() + ")");
                    return list;
                }
            }
        }

        List<String> result = new ArrayList<>();

        if ("EP".equalsIgnoreCase(prefix)) {
            // EP calls: Strictly use poi_process_ie_mapping table via place_of_inspection
            if (poiCode != null) {
                result = pincodePoIMappingRepository.findProcessIeEmpCodeWithName(poiCode);
            }
        } else if ("RPF".equalsIgnoreCase(prefix) || "RPP".equalsIgnoreCase(prefix) || prefix.toUpperCase().startsWith("RP")) {
            result = railPoiIeMappingRepository.findIeEmpCodeWithNameAndPlantId(poiCode, plantId);
        } else if ("ER".equalsIgnoreCase(prefix) || "EF".equalsIgnoreCase(prefix)) {
            // ER / EF calls: Strictly use ie_pincode_poi_mapping table via place_of_inspection
            if (poiCode != null) {
                result = iePincodePoiMappingRepository.findIeEmpCodeWithName(poiCode);
            }
        }

        return result != null ? result : new ArrayList<>();
    }




    public List<IePincodePoiMapping> getEmployeesByPoi(String poiCode) {
        return iePincodePoiMappingRepository.findByPoiCode(poiCode);
    }


    @Transactional
    public String updateCompanyIeMapping(String poiCode, List<IePinPoiDto> newList) {

        // 1. Fetch existing mappings
        List<IePincodePoiMapping> existingList =
                iePincodePoiMappingRepository.findByPoiCode(poiCode);

        Map<String, IePincodePoiMapping> existingMap = existingList.stream()
                .collect(Collectors.toMap(IePincodePoiMapping::getEmployeeCode, e -> e));

        Set<String> newEmpCodes = newList.stream()
                .map(IePinPoiDto::getEmployeeCode)
                .collect(Collectors.toSet());

        // =========================
        // 2. DELETE removed employees
        // =========================
        List<IePincodePoiMapping> toDelete = existingList.stream()
                .filter(e -> !newEmpCodes.contains(e.getEmployeeCode()))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            iePincodePoiMappingRepository.deleteAll(toDelete);
        }


        List<IePincodePoiMapping> toSave = new ArrayList<>();

        // Lookup pincode from pincode_poi_mapping table for this POI Code
        String foundPinCode = pincodePoIMappingRepository.findPinCodeByPoiCode(poiCode);

        for (IePinPoiDto dto : newList) {

            IePincodePoiMapping entity = existingMap.get(dto.getEmployeeCode());

            if (entity == null) {
                entity = new IePincodePoiMapping();
                entity.setEmployeeCode(dto.getEmployeeCode());
                entity.setPoiCode(poiCode);
            }

            // Set product to ERC if not explicitly provided or DEFAULT
            String product = dto.getProduct();
            if (product == null || product.trim().isEmpty() || "DEFAULT".equalsIgnoreCase(product)) {
                product = "ERC";
            }

            // Set pinCode from pincode_poi_mapping lookup if missing or default
            String pinCode = dto.getPinCode();
            if (pinCode == null || pinCode.trim().isEmpty() || "000000".equals(pinCode)) {
                pinCode = foundPinCode;
            }

            entity.setProduct(product);
            entity.setPinCode(pinCode != null ? pinCode : "000000");
            entity.setIeType(dto.getIeType() != null ? dto.getIeType() : "PRIMARY");

            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            iePincodePoiMappingRepository.saveAll(toSave);
        }

        return "Company mapping synced successfully";
    }


    public Map<String, Object> getProcessAndIeUsers(String poiCode) {

        List<Object[]> rows = processIeUsersRepository.getProcessAndIeWithEmp(poiCode);

        Map<Long, String> ieUsers = new HashMap<>();
        Map<Long, String> processUsers = new HashMap<>();

        for (Object[] row : rows) {

            Long processId = ((Number) row[0]).longValue();
            Long ieUserId = ((Number) row[1]).longValue();
            String ieEmp = (String) row[2];
            String processEmp = (String) row[3];

            ieUsers.put(ieUserId, ieEmp);
            processUsers.put(processId, processEmp);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processIe", processUsers);
        result.put("ieUsers", ieUsers);

        return result;
    }

    @Transactional
    public String updatePoiIeUsers(String poiCode,
                                           List<Long> inputIds,
                                           String createdBy) {

        Long createdById = Long.parseLong(createdBy);



        Set<Long> allIeUsers = new HashSet<>();

        // 1A: treat all as processIds first
        List<ProcessIeUsers> processMappings =
                processIeUsersRepository.findByProcessUserIdIn(inputIds);

        Set<Long> processIdsFound = processMappings.stream()
                .map(ProcessIeUsers::getProcessUserId)
                .collect(Collectors.toSet());

        // IE users from process
        for (ProcessIeUsers m : processMappings) {
            allIeUsers.add(m.getIeUserId());
        }

        // 1B: remaining IDs → treat as IE users
        for (Long id : inputIds) {
            if (!processIdsFound.contains(id)) {
                allIeUsers.add(id);
            }
        }


        List<IePoiMapping> existing =
                iePoiMappingRepository.findByPoiCode(poiCode);

        Set<Long> existingIds = existing.stream()
                .map(IePoiMapping::getIeUserId)
                .collect(Collectors.toSet());


        List<IePoiMapping> toDelete = existing.stream()
                .filter(e -> !allIeUsers.contains(e.getIeUserId()))
                .toList();

        if (!toDelete.isEmpty()) {
            iePoiMappingRepository.deleteAll(toDelete);
        }


        List<IePoiMapping> toSave = new ArrayList<>();

        for (Long ieId : allIeUsers) {
            if (!existingIds.contains(ieId)) {

                IePoiMapping m = new IePoiMapping();
                m.setIeUserId(ieId);
                m.setPoiCode(poiCode);
                m.setCreatedBy(createdById);
                m.setCreatedDate(new Date());

                toSave.add(m);
            }
        }

        if (!toSave.isEmpty()) {
            iePoiMappingRepository.saveAll(toSave);
        }

        return "POI updated correctly";
    }


    public String getPlaceOfInspection(String icNumber) {

        if (icNumber == null || icNumber.trim().isEmpty()) {
            throw new RuntimeException("IC Number is empty");
        }

        icNumber = icNumber.trim();
        String prefix = icNumber.split("-")[0];

        if ("RPF".equalsIgnoreCase(prefix)) {
            return railWorkflowTransactionRepository.findLatestPoiByRequestId(icNumber);
        }

        String poi = inspectionCallRepository
                .findPlaceOfInspectionByIcNumber(icNumber);

        if (poi == null) {
            throw new RuntimeException("No data found for given IC Number");
        }

        return poi;
    }


    @Transactional
    public String mapProcessIe(PoiProcessIeRequestDto dto) {


        // 2. Insert new mappings
        List<PoiProcessIeMapping> list = dto.getEmployeeCodes().stream().map(emp -> {
            PoiProcessIeMapping m = new PoiProcessIeMapping();
            m.setEmployeeCode(emp);
            m.setPoiCode(dto.getPoiCode());
            m.setCreatedBy(Long.valueOf(dto.getCreatedBy()));
           /// m.setCreatedDate(new Date());
            return m;
        }).toList();

        poiProcessIeMappingRepository.saveAll(list);

        return "Process IE mapping saved successfully";
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserMaster> allUsers = userMasterRepository.findAll();
        if (allUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Bulk preload vendor names
        Map<String, String> vendorCodeToName = new HashMap<>();
        try {
            for (VendorMaster vm : vendorMasterRepository.findAll()) {
                if (vm.getVendorCode() != null && vm.getVendorName() != null) {
                    vendorCodeToName.put(vm.getVendorCode().trim(), vm.getVendorName().trim());
                    vendorCodeToName.put(vm.getVendorCode().trim().replaceAll("^:", ""), vm.getVendorName().trim());
                }
            }
        } catch (Exception ignored) {}

        // 2. Bulk preload RIO User mappings
        Map<String, String> rioUserMap = new HashMap<>();
        try {
            for (RioUser ru : rioUserRepository.findAll()) {
                if (ru.getEmployeeCode() != null && ru.getRio() != null) {
                    rioUserMap.put(ru.getEmployeeCode().trim(), ru.getRio().trim());
                }
            }
        } catch (Exception ignored) {}

        // 3. Bulk preload PincodePoIMapping
        Map<String, List<PincodePoIMapping>> vendorCodeToPpm = new HashMap<>();
        try {
            for (PincodePoIMapping ppm : pincodePoIMappingRepository.findAll()) {
                if (ppm.getVendorCode() != null) {
                    String vc = ppm.getVendorCode().trim();
                    vendorCodeToPpm.computeIfAbsent(vc, k -> new ArrayList<>()).add(ppm);
                    vendorCodeToPpm.computeIfAbsent(vc.replaceAll("^:", ""), k -> new ArrayList<>()).add(ppm);
                }
            }
        } catch (Exception ignored) {}

        // 4. Bulk preload IEFieldsMapping
        Map<String, String> ieFieldsRioMap = new HashMap<>();
        try {
            for (IEFieldsMapping ief : ieFieldsMappingRepository.findAll()) {
                if ("ERC".equalsIgnoreCase(ief.getProduct()) && ief.getPinCode() != null && ief.getRio() != null) {
                    ieFieldsRioMap.put(ief.getPinCode().trim(), ief.getRio().trim());
                }
            }
        } catch (Exception ignored) {}

        // 5. Bulk preload User Roles from USER_ROLE_MASTER
        Map<Integer, List<String>> userRolesMap = new HashMap<>();
        try {
            List<Object[]> roleRows = userRoleMasterRepository.findAllUserRolesWithRoleNames();
            for (Object[] row : roleRows) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    Integer uId = ((Number) row[0]).intValue();
                    String rName = row[1].toString().trim();
                    userRolesMap.computeIfAbsent(uId, k -> new ArrayList<>()).add(rName);
                }
            }
        } catch (Exception ignored) {}

        return allUsers.stream()
                .map(u -> mapToResponseDTOWithContext(u, vendorCodeToName, rioUserMap, vendorCodeToPpm, ieFieldsRioMap, userRolesMap))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(userRequestDto userDto) {
        return createUser(userDto); // Reuse existing logic
    }

    @Transactional
    @Override
    public void deleteUser(Integer userId) {
        UserMaster user = userMasterRepository.findById(userId).orElse(null);
        if (user != null) {
            String empCode = user.getEmployeeCode();
            String rawCode = empCode != null ? empCode.replaceAll("^:", "").trim() : null;
            String colonCode = rawCode != null && !rawCode.isEmpty() ? ":" + rawCode : null;

            // 1. Delete from USER_ROLE_MASTER
            userRoleMasterRepository.deleteByUserId(userId);

            // 2. Delete from IE_PROFILE if it's an IE
            if (empCode != null) {
                try {
                    ieProfileRepository.deleteByEmployeeCode(empCode);
                } catch (Exception ignored) {}
            }

            // 3. Delete from VENDOR_MASTER
            try {
                if (colonCode != null) {
                    vendorMasterRepository.findByVendorCode(colonCode).ifPresent(vendorMasterRepository::delete);
                }
                if (rawCode != null) {
                    vendorMasterRepository.findByVendorCode(rawCode).ifPresent(vendorMasterRepository::delete);
                }
            } catch (Exception ignored) {}

            // 4. Delete ERC units from PINCODE_POI_MAPPING
            try {
                if (colonCode != null) {
                    List<PincodePoIMapping> units = pincodePoIMappingRepository.findByVendorCode(colonCode);
                    if (units != null && !units.isEmpty()) {
                        pincodePoIMappingRepository.deleteAll(units);
                    }
                }
                if (rawCode != null) {
                    List<PincodePoIMapping> units = pincodePoIMappingRepository.findByVendorCode(rawCode);
                    if (units != null && !units.isEmpty()) {
                        pincodePoIMappingRepository.deleteAll(units);
                    }
                }
            } catch (Exception ignored) {}

            // 5. Delete Sleeper registered unit from SLEEPER_PINCODE_POI_MAPPING
            try {
                List<com.sarthi.Sleeper.entity.SleeperPincodePoIMapping> spList = new ArrayList<>();
                com.sarthi.Sleeper.entity.SleeperPincodePoIMapping sp1 = sleeperPincodePoIMappingRepository.findByVendorCode(userId.toString());
                if (sp1 != null) spList.add(sp1);
                if (colonCode != null) {
                    com.sarthi.Sleeper.entity.SleeperPincodePoIMapping sp2 = sleeperPincodePoIMappingRepository.findByVendorCode(colonCode);
                    if (sp2 != null && !spList.contains(sp2)) spList.add(sp2);
                }
                if (rawCode != null) {
                    com.sarthi.Sleeper.entity.SleeperPincodePoIMapping sp3 = sleeperPincodePoIMappingRepository.findByVendorCode(rawCode);
                    if (sp3 != null && !spList.contains(sp3)) spList.add(sp3);
                }
                if (!spList.isEmpty()) {
                    sleeperPincodePoIMappingRepository.deleteAll(spList);
                }
            } catch (Exception ignored) {}

            // 6. Delete Sleeper plants from VENDOR_PLANT
            try {
                List<com.sarthi.Sleeper.entity.VendorPlant> plants = new ArrayList<>();
                List<com.sarthi.Sleeper.entity.VendorPlant> p1 = vendorPlantRepository.findByVendorId(userId.longValue());
                if (p1 != null) plants.addAll(p1);
                if (colonCode != null) {
                    List<com.sarthi.Sleeper.entity.VendorPlant> p2 = vendorPlantRepository.findByVendorCode(colonCode);
                    if (p2 != null) {
                        for (com.sarthi.Sleeper.entity.VendorPlant vp : p2) {
                            if (!plants.contains(vp)) plants.add(vp);
                        }
                    }
                }
                if (rawCode != null) {
                    List<com.sarthi.Sleeper.entity.VendorPlant> p3 = vendorPlantRepository.findByVendorCode(rawCode);
                    if (p3 != null) {
                        for (com.sarthi.Sleeper.entity.VendorPlant vp : p3) {
                            if (!plants.contains(vp)) plants.add(vp);
                        }
                    }
                }
                if (!plants.isEmpty()) {
                    vendorPlantRepository.deleteAll(plants);
                }
            } catch (Exception ignored) {}

            // 7. Delete Railpad single unit from RAILPAD_PINCODE_POI_MAPPING
            try {
                List<com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping> rList = new ArrayList<>();
                railPadPincodePoIMappingRepository.findByVendorCode(userId.toString()).ifPresent(rList::add);
                if (colonCode != null) {
                    railPadPincodePoIMappingRepository.findByVendorCode(colonCode).ifPresent(r -> { if (!rList.contains(r)) rList.add(r); });
                }
                if (rawCode != null) {
                    railPadPincodePoIMappingRepository.findByVendorCode(rawCode).ifPresent(r -> { if (!rList.contains(r)) rList.add(r); });
                }
                if (!rList.isEmpty()) {
                    railPadPincodePoIMappingRepository.deleteAll(rList);
                }
            } catch (Exception ignored) {}

            // 8. Delete Railpad plants from RAIL_VENDOR_PLANT
            try {
                List<com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants> rPlants = new ArrayList<>();
                List<com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants> rp1 = railVendorPlantsRepository.findByVendorId(userId.longValue());
                if (rp1 != null) rPlants.addAll(rp1);
                if (colonCode != null) {
                    List<com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants> rp2 = railVendorPlantsRepository.findByVendorCode(colonCode);
                    if (rp2 != null) {
                        for (com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants vp : rp2) {
                            if (!rPlants.contains(vp)) rPlants.add(vp);
                        }
                    }
                }
                if (rawCode != null) {
                    List<com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants> rp3 = railVendorPlantsRepository.findByVendorCode(rawCode);
                    if (rp3 != null) {
                        for (com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants vp : rp3) {
                            if (!rPlants.contains(vp)) rPlants.add(vp);
                        }
                    }
                }
                if (!rPlants.isEmpty()) {
                    railVendorPlantsRepository.deleteAll(rPlants);
                }
            } catch (Exception ignored) {}

            // 9. Delete from USER_MASTER (ie_fields_mapping is preserved)
            userMasterRepository.deleteById(userId);
        }
    }

    @Autowired
    private UserProfileAuditRepository userProfileAuditRepository;

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequestDto requestDto) {
        String identifier = requestDto.getIdentifier();
        UserMaster user = userMasterRepository.findFirstByUserName(identifier)
                .orElseGet(() -> userMasterRepository.findFirstByEmployeeCode(identifier)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        404, 404, "ERROR", "User not found for " + identifier))));

        user.setPassword(com.sarthi.util.PasswordEncryptionUtil.encrypt(requestDto.getNewPassword()));
        userMasterRepository.save(user);

        UserProfileAuditLog auditLog = new UserProfileAuditLog();
        auditLog.setUserId(user.getUserId());
        auditLog.setAction("FORGOT_PASSWORD");
        auditLog.setModifiedFields("Password");
        auditLog.setOldValues("[REDACTED]");
        auditLog.setNewValues("[REDACTED]");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setIpAddress("0.0.0.0"); // placeholder or could be pulled from context
        
        userProfileAuditRepository.save(auditLog);
    }

    @Transactional
    @Override
    public String updateUserRegion(Integer userId, String newRegion) {
        UserMaster user = userMasterRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "User not found")));
        user.setRio(newRegion);
        userMasterRepository.save(user);
        return "Region updated successfully";
    }

    @Transactional
    @Override
    public String updateUserRole(Integer userId, List<String> newRoles) {
        UserMaster user = userMasterRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "User not found")));
        
        String rolesAsString = String.join(",", newRoles);
        user.setRoleName(rolesAsString);
        userMasterRepository.save(user);
        
        // Update user_role_master
        userRoleMasterRepository.deleteByUserId(userId);
        for (String roleName : newRoles) {
            RoleMaster role = roleMasterRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Role not found: " + roleName)));
            
            UserRoleMaster userRole = new UserRoleMaster();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getRoleId());
            userRole.setReadPermission(true);
            userRole.setWritePermission(true);
            userRole.setCreatedBy("Admin");
            userRole.setCreatedDate(new Date());
            userRoleMasterRepository.save(userRole);
        }
        
        return "Role updated successfully";
    }

    @Transactional
    @Override
    public String updateUnitContact(String poiCode, String contactPerson, String contactPersonNumber) {
        PincodePoIMapping mapping = pincodePoIMappingRepository.findByPoiCode(poiCode)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Unit not found for POI Code: " + poiCode)));

        mapping.setContactPerson(contactPerson);
        mapping.setContactPersonNumber(contactPersonNumber);
        pincodePoIMappingRepository.save(mapping);

        return "Contact details updated successfully for unit: " + mapping.getUnitName();
    }

    @Transactional
    @Override
    public Object createOrUpdateErcVendor(com.sarthi.dto.ErcVendorCreationDto dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Company Name is required"));
        }
        if (dto.getVendorCode() == null || dto.getVendorCode().trim().isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Vendor Code is required"));
        }

        String cleanVendorCode = dto.getVendorCode().trim();
        String cleanCompanyName = dto.getCompanyName().trim();

        final String vendorCodeFormatted = cleanVendorCode.startsWith(":") ? cleanVendorCode : ":" + cleanVendorCode;

        // 1. Save or Update USER_MASTER
        UserMaster userMaster;
        if (dto.getUserId() != null) {
            userMaster = userMasterRepository.findById(dto.getUserId()).orElse(new UserMaster());
        } else {
            userMaster = userMasterRepository.findFirstByEmployeeCode(vendorCodeFormatted)
                    .orElseGet(() -> userMasterRepository.findFirstByEmployeeCode(cleanVendorCode)
                    .orElseGet(() -> {
                        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                            return userMasterRepository.findFirstByEmail(dto.getEmail().trim()).orElse(new UserMaster());
                        }
                        return new UserMaster();
                    }));
            if (userMaster.getUserId() == null) {
                userMaster.setCreatedDate(LocalDateTime.now());
            }
        }

        userMaster.setUserName(cleanCompanyName);
        userMaster.setFullName(cleanCompanyName);
        userMaster.setShortName(vendorCodeFormatted);
        userMaster.setEmployeeCode(vendorCodeFormatted);
        userMaster.setEmail(dto.getEmail());
        userMaster.setMobileNumber(null); // Mobile number is stored per-unit in pincode_poi_mapping (contact_person_number)
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            userMaster.setPassword(com.sarthi.util.PasswordEncryptionUtil.encrypt(dto.getPassword()));
        }
        userMaster.setRoleName("Vendor");
        userMaster.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Admin");
        userMaster.setStatus("Inactive".equalsIgnoreCase(dto.getStatus()) ? AppConstant.USER_STATUS_INACTIVE : AppConstant.USER_STATUS);

        // 2. Assign USER_ROLE_MASTER (roleId = 1 for Vendor) without deleting other existing roles
        RoleMaster vendorRole = roleMasterRepository.findByRoleName("Vendor")
                .orElseGet(() -> {
                    RoleMaster rm = new RoleMaster();
                    rm.setRoleId(1);
                    rm.setRoleName("Vendor");
                    return rm;
                });

        Integer vendorRoleId = vendorRole.getRoleId() != null ? vendorRole.getRoleId() : 1;
        boolean hasVendorRole = userRoleMasterRepository.existsByUserIdAndRoleId(userMaster.getUserId(), vendorRoleId);
        if (!hasVendorRole) {
            UserRoleMaster userRole = new UserRoleMaster();
            userRole.setUserId(userMaster.getUserId());
            userRole.setRoleId(vendorRoleId);
            userRole.setReadPermission(true);
            userRole.setWritePermission(true);
            userRole.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Admin");
            userRole.setCreatedDate(new Date());
            userRoleMasterRepository.save(userRole);
        }

        // Sync user_master.role_name with all assigned roles
        List<String> currentRoles = userMasterRepository.findRoleNamesByUserId(userMaster.getUserId());
        if (currentRoles != null && !currentRoles.isEmpty()) {
            userMaster.setRoleName(String.join(", ", currentRoles));
            userMasterRepository.save(userMaster);
        }

        // 3. Save or Update VENDOR_MASTER
        VendorMaster vendorMaster = vendorMasterRepository.findByVendorCode(vendorCodeFormatted)
                .orElseGet(() -> vendorMasterRepository.findByVendorCode(cleanVendorCode)
                        .orElse(new VendorMaster()));
        vendorMaster.setVendorCode(vendorCodeFormatted);
        vendorMaster.setVendorName(cleanCompanyName);
        if (vendorMaster.getId() == null) {
            vendorMaster.setCreatedDate(LocalDateTime.now());
        }
        vendorMasterRepository.save(vendorMaster);

        // 4. Save units into PINCODE_POI_MAPPING
        if (dto.getUnits() != null && !dto.getUnits().isEmpty()) {
            for (com.sarthi.dto.ErcVendorUnitDto unitDto : dto.getUnits()) {
                if (unitDto.getUnitName() == null || unitDto.getUnitName().trim().isEmpty()) {
                    continue;
                }

                PincodePoIMapping mapping = null;
                if (unitDto.getId() != null) {
                    mapping = pincodePoIMappingRepository.findById(unitDto.getId()).orElse(null);
                }
                if (mapping == null && unitDto.getPoiCode() != null && !unitDto.getPoiCode().trim().isEmpty()) {
                    mapping = pincodePoIMappingRepository.findByPoiCode(unitDto.getPoiCode().trim()).orElse(null);
                }
                if (mapping == null) {
                    mapping = pincodePoIMappingRepository.findByCompanyNameAndUnitName(cleanCompanyName, unitDto.getUnitName().trim())
                            .orElse(new PincodePoIMapping());
                }

                mapping.setCompanyName(cleanCompanyName);
                mapping.setUnitName(unitDto.getUnitName().trim());
                mapping.setPinCode(unitDto.getPinCode() != null ? unitDto.getPinCode().trim() : "");
                mapping.setCin(unitDto.getCin() != null ? unitDto.getCin().trim() : "");
                mapping.setAddress(unitDto.getAddress() != null ? unitDto.getAddress().trim() : "");
                mapping.setDistrict(unitDto.getDistrict() != null ? unitDto.getDistrict().trim() : "");
                mapping.setState(unitDto.getState() != null ? unitDto.getState().trim() : "");
                mapping.setContactPerson(unitDto.getContactPerson() != null ? unitDto.getContactPerson().trim() : null);
                
                String cleanPhone = unitDto.getContactPersonNumber() != null 
                        ? unitDto.getContactPersonNumber().replaceAll("\\D", "") 
                        : null;
                if (cleanPhone != null && !cleanPhone.isEmpty() && cleanPhone.length() != 10) {
                    throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Contact Person Number for " + unitDto.getUnitName() + " must be exactly 10 digits"));
                }
                mapping.setContactPersonNumber(cleanPhone);
                mapping.setVendorCode(vendorCodeFormatted);
                mapping.setStatus(unitDto.getStatus() != null ? unitDto.getStatus() : "Active");

                if (mapping.getPoiCode() == null || mapping.getPoiCode().trim().isEmpty()) {
                    if (unitDto.getPoiCode() != null && !unitDto.getPoiCode().trim().isEmpty()) {
                        mapping.setPoiCode(unitDto.getPoiCode().trim());
                    } else {
                        mapping.setPoiCode(generateNextPoiCode());
                    }
                }

                pincodePoIMappingRepository.save(mapping);

                // 5. Ensure entry in IE_FIELDS_MAPPING for product ERC
                if (mapping.getPinCode() != null && !mapping.getPinCode().trim().isEmpty()) {
                    String pin = mapping.getPinCode().trim();
                    boolean exists = ieFieldsMappingRepository.existsByPinCodeAndProduct(pin, "ERC");
                    if (!exists) {
                        IEFieldsMapping ieMap = new IEFieldsMapping();
                        ieMap.setPinCode(pin);
                        ieMap.setProduct("ERC");
                        ieMap.setStage("R,P,F");
                        ieMap.setPlantPincode(pin);
                        ieMap.setRio(unitDto.getRio() != null && !unitDto.getRio().trim().isEmpty()
                                ? unitDto.getRio().trim()
                                : deriveRioFromState(mapping.getState()));
                        ieFieldsMappingRepository.save(ieMap);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userMaster.getUserId());
        result.put("userName", userMaster.getUsername());
        result.put("employeeCode", userMaster.getEmployeeCode());
        result.put("message", "ERC Vendor registered successfully!");
        return result;
    }

    @Override
    public Object getErcVendorDetails(Integer userId) {
        UserMaster user = userMasterRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "User not found")));

        com.sarthi.dto.ErcVendorCreationDto dto = new com.sarthi.dto.ErcVendorCreationDto();
        dto.setUserId(user.getUserId());
        
        // 1. Resolve Vendor Code
        String empCode = user.getEmployeeCode();
        if (empCode == null || empCode.trim().isEmpty()) {
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                empCode = user.getUsername().trim().replaceAll("^:", "");
            } else if (user.getEmail() != null && user.getEmail().contains("@")) {
                String prefix = user.getEmail().substring(0, user.getEmail().indexOf("@")).trim();
                empCode = prefix.replaceAll("^:", "");
            }
        }
        dto.setVendorCode(empCode);

        // 2. Resolve Company Name
        String companyName = user.getFullName();
        if ((companyName == null || companyName.trim().isEmpty()) && empCode != null && !empCode.trim().isEmpty()) {
            String vCodeCol = ":" + empCode;
            Optional<VendorMaster> vmOpt = vendorMasterRepository.findByVendorCode(vCodeCol);
            if (vmOpt.isEmpty()) {
                vmOpt = vendorMasterRepository.findByVendorCode(empCode);
            }
            if (vmOpt.isPresent() && vmOpt.get().getVendorName() != null && !vmOpt.get().getVendorName().trim().isEmpty()) {
                companyName = vmOpt.get().getVendorName().trim();
            }
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            companyName = user.getUsername() != null ? user.getUsername().replaceAll("^:", "") : empCode;
        }
        dto.setCompanyName(companyName);

        dto.setEmail(user.getEmail());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setPassword(com.sarthi.util.PasswordEncryptionUtil.decrypt(user.getPassword()));
        dto.setStatus(user.getStatus() != null ? user.getStatus() : "Active");

        // 3. Find units
        List<PincodePoIMapping> units = new ArrayList<>();
        if (empCode != null && !empCode.trim().isEmpty()) {
            String vCodeCol = ":" + empCode;
            units = pincodePoIMappingRepository.findByVendorCode(vCodeCol);
            if (units == null || units.isEmpty()) {
                units = pincodePoIMappingRepository.findByVendorCode(empCode);
            }
        }
        if ((units == null || units.isEmpty()) && companyName != null && !companyName.trim().isEmpty()) {
            units = pincodePoIMappingRepository.findByCompanyName(companyName);
        }

        List<com.sarthi.dto.ErcVendorUnitDto> unitDtos = new ArrayList<>();
        if (units != null) {
            for (PincodePoIMapping u : units) {
                com.sarthi.dto.ErcVendorUnitDto uDto = new com.sarthi.dto.ErcVendorUnitDto();
                uDto.setId(u.getId());
                uDto.setUnitName(u.getUnitName());
                uDto.setPinCode(u.getPinCode());
                uDto.setCin(u.getCin());
                uDto.setAddress(u.getAddress());
                uDto.setDistrict(u.getDistrict());
                uDto.setState(u.getState());
                uDto.setContactPerson(u.getContactPerson());
                uDto.setContactPersonNumber(u.getContactPersonNumber());
                uDto.setPoiCode(u.getPoiCode());
                uDto.setStatus(u.getStatus() != null ? u.getStatus() : "Active");

                // Resolve RIO
                if (u.getPinCode() != null && !u.getPinCode().trim().isEmpty()) {
                    Optional<IEFieldsMapping> ieOpt = ieFieldsMappingRepository.findFirstByPinCodeAndProduct(u.getPinCode().trim(), "ERC");
                    if (ieOpt.isPresent() && ieOpt.get().getRio() != null && !ieOpt.get().getRio().trim().isEmpty()) {
                        uDto.setRio(ieOpt.get().getRio().trim());
                    } else {
                        uDto.setRio(deriveRioFromState(u.getState()));
                    }
                } else {
                    uDto.setRio(deriveRioFromState(u.getState()));
                }

                unitDtos.add(uDto);
            }
        }
        dto.setUnits(unitDtos);
        return dto;
    }

    private synchronized String generateNextPoiCode() {
        try {
            String maxPoi = pincodePoIMappingRepository.findMaxNumericPoiCode();
            if (maxPoi != null && maxPoi.toUpperCase().startsWith("POI")) {
                int num = Integer.parseInt(maxPoi.substring(3).trim());
                return "POI" + (num + 1);
            }
        } catch (Exception ignored) {
        }
        return "POI" + (pincodePoIMappingRepository.count() + 1);
    }

    private String deriveRioFromState(String state) {
        if (state == null) return "WRIO";
        String s = state.toUpperCase().trim();
        if (s.contains("WEST BENGAL") || s.contains("BENGAL") || s.contains("BIHAR") || s.contains("JHARKHAND") || s.contains("ODISHA") || s.contains("ORISSA") || s.contains("ASSAM")) {
            return "ERIO";
        } else if (s.contains("HARYANA") || s.contains("PUNJAB") || s.contains("DELHI") || s.contains("RAJASTHAN") || s.contains("UTTAR PRADESH") || s.contains("UP") || s.contains("UTTARAKHAND") || s.contains("HIMACHAL") || s.contains("JAMMU")) {
            return "NRIO";
        } else if (s.contains("TAMIL") || s.contains("KERALA") || s.contains("KARNATAKA") || s.contains("ANDHRA") || s.contains("TELANGANA")) {
            return "SRIO";
        } else if (s.contains("CENTRAL") || s.contains("MADHYA")) {
            return "CRIO";
        }
        return "WRIO";
    }

    @Transactional
    @Override
    public Object createOrUpdateSleeperVendor(com.sarthi.dto.SleeperVendorCreationDto dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Company Name is required"));
        }
        if (dto.getVendorCode() == null || dto.getVendorCode().trim().isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Vendor Code is required"));
        }

        String cleanVendorCode = dto.getVendorCode().trim();
        String cleanCompanyName = dto.getCompanyName().trim();
        final String vendorCodeFormatted = cleanVendorCode.startsWith(":") ? cleanVendorCode : ":" + cleanVendorCode;

        // 1. Save or Update USER_MASTER
        UserMaster userMaster;
        if (dto.getUserId() != null) {
            userMaster = userMasterRepository.findById(dto.getUserId()).orElse(new UserMaster());
        } else {
            userMaster = userMasterRepository.findFirstByEmployeeCode(vendorCodeFormatted)
                    .orElseGet(() -> userMasterRepository.findFirstByEmployeeCode(cleanVendorCode)
                    .orElseGet(() -> {
                        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                            return userMasterRepository.findFirstByEmail(dto.getEmail().trim()).orElse(new UserMaster());
                        }
                        return new UserMaster();
                    }));
            if (userMaster.getUserId() == null) {
                userMaster.setCreatedDate(LocalDateTime.now());
            }
        }

        userMaster.setUserName(cleanCompanyName);
        userMaster.setFullName(cleanCompanyName);
        userMaster.setShortName(vendorCodeFormatted);
        userMaster.setEmployeeCode(vendorCodeFormatted);
        userMaster.setEmail(dto.getEmail());
        userMaster.setMobileNumber(null); // Mobile numbers are stored per-plant in vendor_plant
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            userMaster.setPassword(dto.getPassword());
        }
        userMaster.setRoleName("Sleeper Vendor");
        userMaster.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Admin");
        userMaster.setStatus("Inactive".equalsIgnoreCase(dto.getStatus()) ? AppConstant.USER_STATUS_INACTIVE : AppConstant.USER_STATUS);

        // 2. Assign USER_ROLE_MASTER (roleId = 12 for Sleeper Vendor) without deleting other existing roles
        RoleMaster sleeperVendorRole = roleMasterRepository.findByRoleName("Sleeper Vendor")
                .orElseGet(() -> {
                    RoleMaster rm = new RoleMaster();
                    rm.setRoleId(12);
                    rm.setRoleName("Sleeper Vendor");
                    return rm;
                });

        Integer sleeperRoleId = sleeperVendorRole.getRoleId() != null ? sleeperVendorRole.getRoleId() : 12;
        boolean hasSleeperRole = userRoleMasterRepository.existsByUserIdAndRoleId(userMaster.getUserId(), sleeperRoleId);
        if (!hasSleeperRole) {
            UserRoleMaster userRole = new UserRoleMaster();
            userRole.setUserId(userMaster.getUserId());
            userRole.setRoleId(sleeperRoleId);
            userRole.setReadPermission(true);
            userRole.setWritePermission(true);
            userRole.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Admin");
            userRole.setCreatedDate(new Date());
            userRoleMasterRepository.save(userRole);
        }

        // Sync user_master.role_name with all assigned roles
        List<String> currentRoles = userMasterRepository.findRoleNamesByUserId(userMaster.getUserId());
        if (currentRoles != null && !currentRoles.isEmpty()) {
            userMaster.setRoleName(String.join(", ", currentRoles));
            userMasterRepository.save(userMaster);
        }

        // 3. Save or Update VENDOR_MASTER
        VendorMaster vendorMaster = vendorMasterRepository.findByVendorCode(vendorCodeFormatted)
                .orElseGet(() -> vendorMasterRepository.findByVendorCode(cleanVendorCode)
                        .orElse(new VendorMaster()));
        vendorMaster.setVendorCode(vendorCodeFormatted);
        vendorMaster.setVendorName(cleanCompanyName);
        if (vendorMaster.getId() == null) {
            vendorMaster.setCreatedDate(LocalDateTime.now());
        }
        vendorMasterRepository.save(vendorMaster);

        // 4. Save Single Unit into SLEEPER_PINCODE_POI_MAPPING
        com.sarthi.Sleeper.entity.SleeperPincodePoIMapping spm = null;
        if (dto.getUnitId() != null) {
            spm = sleeperPincodePoIMappingRepository.findById(dto.getUnitId()).orElse(null);
        }
        if (spm == null && userMaster.getUserId() != null) {
            spm = sleeperPincodePoIMappingRepository.findByVendorCode(userMaster.getUserId().toString());
        }
        if (spm == null) {
            spm = sleeperPincodePoIMappingRepository.findByCompanyNameAndUnitName(cleanCompanyName, dto.getUnitName() != null ? dto.getUnitName().trim() : cleanCompanyName)
                    .orElse(new com.sarthi.Sleeper.entity.SleeperPincodePoIMapping());
        }

        String unitName = (dto.getUnitName() != null && !dto.getUnitName().trim().isEmpty())
                ? dto.getUnitName().trim()
                : (cleanCompanyName + " - Head Office / Unit");
        String unitPin = dto.getUnitPinCode() != null ? dto.getUnitPinCode().trim() : "";
        String poiCode = (dto.getPoiCode() != null && !dto.getPoiCode().trim().isEmpty())
                ? dto.getPoiCode().trim()
                : (spm.getPoiCode() != null ? spm.getPoiCode() : generateNextSleeperPoiCode());

        spm.setCompanyName(cleanCompanyName);
        spm.setUnitName(unitName);
        spm.setPinCode(unitPin);
        spm.setCin(dto.getCin() != null ? dto.getCin().trim() : "");
        spm.setAddress(dto.getUnitAddress() != null ? dto.getUnitAddress().trim() : "");
        spm.setDistrict(dto.getUnitDistrict() != null ? dto.getUnitDistrict().trim() : "");
        spm.setState(dto.getUnitState() != null ? dto.getUnitState().trim() : "");
        spm.setPoiCode(poiCode);
        spm.setVendorCode(userMaster.getUserId() != null ? userMaster.getUserId().toString() : vendorCodeFormatted);
        spm.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        sleeperPincodePoIMappingRepository.save(spm);

        // 5. Save Multiple Plants into VENDOR_PLANT
        if (dto.getPlants() != null && !dto.getPlants().isEmpty()) {
            Set<String> seenPlantIds = new HashSet<>();
            Set<String> seenPlantNames = new HashSet<>();

            for (com.sarthi.dto.SleeperVendorPlantDto plantDto : dto.getPlants()) {
                if (plantDto.getPlantName() == null || plantDto.getPlantName().trim().isEmpty()) {
                    continue;
                }

                String plantName = plantDto.getPlantName().trim();
                String plantNameLower = plantName.toLowerCase();
                if (seenPlantNames.contains(plantNameLower)) {
                    throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Duplicate Plant Name '" + plantName + "' found. Each plant must have a unique name."));
                }
                seenPlantNames.add(plantNameLower);

                String plantPincode = plantDto.getPinCode() != null ? plantDto.getPinCode().trim() : "";

                if (plantDto.getPlantId() != null && !plantDto.getPlantId().trim().isEmpty()) {
                    String pid = plantDto.getPlantId().trim().toLowerCase();
                    if (seenPlantIds.contains(pid)) {
                        throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Duplicate Plant ID '" + plantDto.getPlantId() + "' found in plant '" + plantName + "'. Each plant must have a unique Plant ID."));
                    }
                    seenPlantIds.add(pid);
                }
                
                String plantId = (plantDto.getPlantId() != null && !plantDto.getPlantId().trim().isEmpty())
                        ? plantDto.getPlantId().trim()
                        : plantName;

                String cleanPhone = plantDto.getContactPersonNumber() != null 
                        ? plantDto.getContactPersonNumber().replaceAll("\\D", "") 
                        : null;
                if (cleanPhone != null && !cleanPhone.isEmpty() && cleanPhone.length() != 10) {
                    throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Contact Person Number for plant " + plantName + " must be exactly 10 digits"));
                }

                String derivedRio = plantDto.getRio() != null && !plantDto.getRio().trim().isEmpty()
                        ? plantDto.getRio().trim()
                        : deriveRioFromState(dto.getUnitState());

                // Save/Update VENDOR_PLANT
                com.sarthi.Sleeper.entity.VendorPlant vp = null;
                if (plantDto.getId() != null) {
                    vp = vendorPlantRepository.findById(plantDto.getId()).orElse(null);
                }
                if (vp == null && plantDto.getPlantId() != null && !plantDto.getPlantId().trim().isEmpty()) {
                    vp = vendorPlantRepository.findByPlantId(plantDto.getPlantId().trim()).orElse(null);
                }
                if (vp == null) {
                    vp = vendorPlantRepository.findByCompanyNameAndPlantName(cleanCompanyName, plantName)
                            .orElse(new com.sarthi.Sleeper.entity.VendorPlant());
                }

                vp.setVendorCode(vendorCodeFormatted);
                vp.setCompanyName(cleanCompanyName);
                vp.setPlantName(plantName);
                vp.setPlantId(plantId);
                vp.setPlantPincode(plantPincode);
                vp.setRio(derivedRio);
                vp.setZonalRailway(plantDto.getZonalRailway());
                vp.setContactPerson(plantDto.getContactPerson() != null ? plantDto.getContactPerson().trim() : null);
                vp.setContactPersonNumber(cleanPhone);
                if (userMaster.getUserId() != null) {
                    vp.setVendorId(userMaster.getUserId().longValue());
                }
                vendorPlantRepository.save(vp);

                // Ensure entry in IE_FIELDS_MAPPING for product Sleeper
                if (!plantPincode.isEmpty()) {
                    boolean exists = ieFieldsMappingRepository.existsByPinCodeAndProduct(plantPincode, "Sleeper");
                    if (!exists) {
                        IEFieldsMapping ieMap = new IEFieldsMapping();
                        ieMap.setPinCode(plantPincode);
                        ieMap.setProduct("Sleeper");
                        ieMap.setStage("Sleeper");
                        ieMap.setPlantPincode(plantPincode);
                        ieMap.setRio(derivedRio);
                        ieFieldsMappingRepository.save(ieMap);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userMaster.getUserId());
        result.put("userName", userMaster.getUsername());
        result.put("employeeCode", userMaster.getEmployeeCode());
        result.put("message", "Sleeper Vendor registered successfully!");
        return result;
    }

    private synchronized String generateNextSleeperPoiCode() {
        try {
            String maxPoi = sleeperPincodePoIMappingRepository.findMaxNumericPoiCode();
            if (maxPoi != null && maxPoi.toUpperCase().startsWith("POI")) {
                int num = Integer.parseInt(maxPoi.substring(3).trim());
                return String.format("POI%02d", num + 1);
            }
        } catch (Exception ignored) {
        }
        return String.format("POI%02d", sleeperPincodePoIMappingRepository.count() + 1);
    }

    @Override
    public Object getSleeperVendorDetails(Integer userId) {
        UserMaster user = userMasterRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "User not found")));

        com.sarthi.dto.SleeperVendorCreationDto dto = new com.sarthi.dto.SleeperVendorCreationDto();
        dto.setUserId(user.getUserId());

        // 1. Resolve Vendor Code
        String empCode = user.getEmployeeCode();
        if (empCode == null || empCode.trim().isEmpty()) {
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                empCode = user.getUsername().trim();
            } else if (user.getEmail() != null && user.getEmail().contains("@")) {
                empCode = user.getEmail().substring(0, user.getEmail().indexOf("@")).trim();
            }
        }
        if (empCode != null && !empCode.startsWith(":")) {
            empCode = ":" + empCode;
        }
        dto.setVendorCode(empCode);

        // 2. Resolve Company Name
        String compName = user.getFullName();
        if (compName == null || compName.trim().isEmpty() || compName.startsWith(":")) {
            String vCodeCol = empCode != null && empCode.startsWith(":") ? empCode : (empCode != null ? ":" + empCode : "");
            String rawCode = empCode != null ? empCode.replaceAll("^:", "") : "";
            Optional<VendorMaster> vmOpt = vendorMasterRepository.findByVendorCode(vCodeCol);
            if (vmOpt.isEmpty() && !rawCode.isEmpty()) {
                vmOpt = vendorMasterRepository.findByVendorCode(rawCode);
            }
            if (vmOpt.isPresent() && vmOpt.get().getVendorName() != null && !vmOpt.get().getVendorName().trim().isEmpty() && !vmOpt.get().getVendorName().startsWith(":")) {
                compName = vmOpt.get().getVendorName().trim();
            }
        }
        if (compName == null || compName.trim().isEmpty() || compName.startsWith(":")) {
            com.sarthi.Sleeper.entity.SleeperPincodePoIMapping spmLookup = sleeperPincodePoIMappingRepository.findByVendorCode(userId != null ? userId.toString() : "");
            if (spmLookup == null && empCode != null) {
                spmLookup = sleeperPincodePoIMappingRepository.findByVendorCode(empCode);
            }
            if (spmLookup != null && spmLookup.getCompanyName() != null && !spmLookup.getCompanyName().trim().isEmpty() && !spmLookup.getCompanyName().startsWith(":")) {
                compName = spmLookup.getCompanyName().trim();
            }
        }
        if (compName == null || compName.trim().isEmpty()) {
            compName = user.getUsername() != null ? user.getUsername().replaceAll("^:", "") : empCode;
        }
        dto.setCompanyName(compName);
        dto.setEmail(user.getEmail());
        dto.setPassword(com.sarthi.util.PasswordEncryptionUtil.decrypt(user.getPassword()));
        dto.setStatus(user.getStatus());

        // 3. Fetch Single Unit info from SLEEPER_PINCODE_POI_MAPPING
        com.sarthi.Sleeper.entity.SleeperPincodePoIMapping spm = null;
        if (userId != null) {
            spm = sleeperPincodePoIMappingRepository.findByVendorCode(userId.toString());
        }
        if (spm == null && empCode != null) {
            String rawCode = empCode.replaceAll("^:", "");
            spm = sleeperPincodePoIMappingRepository.findByVendorCode(empCode);
            if (spm == null) {
                spm = sleeperPincodePoIMappingRepository.findByVendorCode(rawCode);
            }
        }
        if (spm != null) {
            dto.setUnitId(spm.getId());
            dto.setUnitName(spm.getUnitName());
            dto.setUnitPinCode(spm.getPinCode());
            dto.setCin(spm.getCin());
            dto.setUnitAddress(spm.getAddress());
            dto.setUnitDistrict(spm.getDistrict());
            dto.setUnitState(spm.getState());
            dto.setPoiCode(spm.getPoiCode());
        }

        // 4. Fetch Multiple Plants from VENDOR_PLANT
        List<com.sarthi.dto.SleeperVendorPlantDto> plantList = new ArrayList<>();
        List<com.sarthi.Sleeper.entity.VendorPlant> vpList = new ArrayList<>();
        if (userId != null) {
            vpList = vendorPlantRepository.findByVendorId(userId.longValue());
        }
        if ((vpList == null || vpList.isEmpty()) && empCode != null) {
            String rawCode = empCode.replaceAll("^:", "");
            vpList = vendorPlantRepository.findByVendorCode(empCode);
            if (vpList == null || vpList.isEmpty()) {
                vpList = vendorPlantRepository.findByVendorCode(rawCode);
            }
            if (vpList == null || vpList.isEmpty()) {
                vpList = vendorPlantRepository.findByVendorCode(":" + rawCode);
            }
        }

        if (vpList != null && !vpList.isEmpty()) {
            for (com.sarthi.Sleeper.entity.VendorPlant vp : vpList) {
                com.sarthi.dto.SleeperVendorPlantDto pDto = new com.sarthi.dto.SleeperVendorPlantDto();
                pDto.setId(vp.getId());
                pDto.setPlantName(vp.getPlantName());
                pDto.setPlantId(vp.getPlantId());
                pDto.setPinCode(vp.getPlantPincode());
                pDto.setRio(vp.getRio());
                pDto.setZonalRailway(vp.getZonalRailway());
                pDto.setContactPerson(vp.getContactPerson());
                pDto.setContactPersonNumber(vp.getContactPersonNumber());
                pDto.setStatus("Active");
                plantList.add(pDto);
            }
        }

        dto.setPlants(plantList);
        return dto;
    }

    @Transactional
    @Override
    public Object createOrUpdateRailpadVendor(com.sarthi.dto.RailpadVendorCreationDto dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Company Name is required"));
        }
        if (dto.getVendorCode() == null || dto.getVendorCode().trim().isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Vendor Code is required"));
        }

        String cleanVendorCode = dto.getVendorCode().trim();
        String cleanCompanyName = dto.getCompanyName().trim();
        final String vendorCodeFormatted = cleanVendorCode.startsWith(":") ? cleanVendorCode : ":" + cleanVendorCode;

        // 1. Save or Update USER_MASTER
        UserMaster userMaster;
        if (dto.getUserId() != null) {
            userMaster = userMasterRepository.findById(dto.getUserId()).orElse(new UserMaster());
        } else {
            userMaster = userMasterRepository.findFirstByEmployeeCode(vendorCodeFormatted)
                    .orElseGet(() -> userMasterRepository.findFirstByEmployeeCode(cleanVendorCode)
                    .orElseGet(() -> {
                        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                            return userMasterRepository.findFirstByEmail(dto.getEmail().trim()).orElse(new UserMaster());
                        }
                        return new UserMaster();
                    }));
            if (userMaster.getUserId() == null) {
                userMaster.setCreatedDate(LocalDateTime.now());
            }
        }

        userMaster.setUserName(cleanCompanyName);
        userMaster.setFullName(cleanCompanyName);
        userMaster.setShortName(vendorCodeFormatted);
        userMaster.setEmployeeCode(vendorCodeFormatted);
        userMaster.setEmail(dto.getEmail());
        userMaster.setMobileNumber(null); // Mobile numbers are stored per-plant in rail_vendor_plant
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            userMaster.setPassword(com.sarthi.util.PasswordEncryptionUtil.encrypt(dto.getPassword()));
        }
        userMaster.setRoleName("Rail Vendor");
        userMaster.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Admin");
        userMaster.setStatus("Inactive".equalsIgnoreCase(dto.getStatus()) ? AppConstant.USER_STATUS_INACTIVE : AppConstant.USER_STATUS);

        // 2. Assign USER_ROLE_MASTER (roleId = 17 for Rail Vendor) without deleting other existing roles
        RoleMaster railVendorRole = roleMasterRepository.findByRoleName("Rail Vendor")
                .orElseGet(() -> roleMasterRepository.findByRoleName("Railpad Vendor")
                .orElseGet(() -> {
                    RoleMaster rm = new RoleMaster();
                    rm.setRoleId(17);
                    rm.setRoleName("Rail Vendor");
                    return rm;
                }));

        Integer railRoleId = railVendorRole.getRoleId() != null ? railVendorRole.getRoleId() : 17;
        boolean hasRailRole = userRoleMasterRepository.existsByUserIdAndRoleId(userMaster.getUserId(), railRoleId);
        if (!hasRailRole) {
            UserRoleMaster userRole = new UserRoleMaster();
            userRole.setUserId(userMaster.getUserId());
            userRole.setRoleId(railRoleId);
            userRole.setReadPermission(true);
            userRole.setWritePermission(true);
            userRole.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Admin");
            userRole.setCreatedDate(new Date());
            userRoleMasterRepository.save(userRole);
        }

        // Sync user_master.role_name with all assigned roles
        List<String> currentRoles = userMasterRepository.findRoleNamesByUserId(userMaster.getUserId());
        if (currentRoles != null && !currentRoles.isEmpty()) {
            userMaster.setRoleName(String.join(", ", currentRoles));
            userMasterRepository.save(userMaster);
        }

        // 3. Save or Update VENDOR_MASTER
        VendorMaster vendorMaster = vendorMasterRepository.findByVendorCode(vendorCodeFormatted)
                .orElseGet(() -> vendorMasterRepository.findByVendorCode(cleanVendorCode)
                        .orElse(new VendorMaster()));
        vendorMaster.setVendorCode(vendorCodeFormatted);
        vendorMaster.setVendorName(cleanCompanyName);
        if (vendorMaster.getId() == null) {
            vendorMaster.setCreatedDate(LocalDateTime.now());
        }
        vendorMasterRepository.save(vendorMaster);

        // 4. Save Single Unit into RAILPAD_PINCODE_POI_MAPPING
        com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping rpm = null;
        if (dto.getUnitId() != null) {
            rpm = railPadPincodePoIMappingRepository.findById(dto.getUnitId()).orElse(null);
        }
        if (rpm == null && userMaster.getUserId() != null) {
            rpm = railPadPincodePoIMappingRepository.findByVendorCode(userMaster.getUserId().toString()).orElse(null);
        }
        if (rpm == null) {
            rpm = railPadPincodePoIMappingRepository.findByCompanyNameAndUnitName(cleanCompanyName, dto.getUnitName() != null ? dto.getUnitName().trim() : cleanCompanyName)
                    .orElse(new com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping());
        }

        String unitName = (dto.getUnitName() != null && !dto.getUnitName().trim().isEmpty())
                ? dto.getUnitName().trim()
                : (cleanCompanyName + " - Head Office / Unit");
        String unitPin = dto.getUnitPinCode() != null ? dto.getUnitPinCode().trim() : "";
        String poiCode = (dto.getPoiCode() != null && !dto.getPoiCode().trim().isEmpty())
                ? dto.getPoiCode().trim()
                : (rpm.getPoiCode() != null ? rpm.getPoiCode() : generateNextRailpadPoiCode());

        rpm.setCompanyName(cleanCompanyName);
        rpm.setUnitName(unitName);
        rpm.setPinCode(unitPin);
        rpm.setCin(dto.getCin() != null ? dto.getCin().trim() : "");
        rpm.setAddress(dto.getUnitAddress() != null ? dto.getUnitAddress().trim() : "");
        rpm.setDistrict(dto.getUnitDistrict() != null ? dto.getUnitDistrict().trim() : "");
        rpm.setState(dto.getUnitState() != null ? dto.getUnitState().trim() : "");
        rpm.setPoiCode(poiCode);
        rpm.setVendorCode(userMaster.getUserId() != null ? userMaster.getUserId().toString() : vendorCodeFormatted);
        rpm.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        if (rpm.getId() == null) {
            rpm.setCreatedDate(LocalDateTime.now());
        }
        rpm.setUpdatedDate(LocalDateTime.now());
        railPadPincodePoIMappingRepository.save(rpm);

        // 5. Save Multiple Plants into RAIL_VENDOR_PLANT
        if (dto.getPlants() != null && !dto.getPlants().isEmpty()) {
            Set<String> seenPlantIds = new HashSet<>();
            Set<String> seenPlantNames = new HashSet<>();

            for (com.sarthi.dto.RailVendorPlantDto plantDto : dto.getPlants()) {
                if (plantDto.getPlantName() == null || plantDto.getPlantName().trim().isEmpty()) {
                    continue;
                }

                String plantName = plantDto.getPlantName().trim();
                String plantNameLower = plantName.toLowerCase();
                if (seenPlantNames.contains(plantNameLower)) {
                    throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Duplicate Plant Name '" + plantName + "' found. Each plant must have a unique name."));
                }
                seenPlantNames.add(plantNameLower);

                String plantPincode = plantDto.getPinCode() != null ? plantDto.getPinCode().trim() : "";

                if (plantDto.getPlantId() != null && !plantDto.getPlantId().trim().isEmpty()) {
                    String pid = plantDto.getPlantId().trim().toLowerCase();
                    if (seenPlantIds.contains(pid)) {
                        throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Duplicate Plant ID '" + plantDto.getPlantId() + "' found in plant '" + plantName + "'. Each plant must have a unique Plant ID."));
                    }
                    seenPlantIds.add(pid);
                }

                String plantId = (plantDto.getPlantId() != null && !plantDto.getPlantId().trim().isEmpty())
                        ? plantDto.getPlantId().trim()
                        : plantName;

                String cleanPhone = plantDto.getContactPersonNumber() != null 
                        ? plantDto.getContactPersonNumber().replaceAll("\\D", "") 
                        : null;
                if (cleanPhone != null && !cleanPhone.isEmpty() && cleanPhone.length() != 10) {
                    throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Contact Person Number for plant " + plantName + " must be exactly 10 digits"));
                }

                String derivedRio = plantDto.getRio() != null && !plantDto.getRio().trim().isEmpty()
                        ? plantDto.getRio().trim()
                        : deriveRioFromState(dto.getUnitState());

                // Save/Update RAIL_VENDOR_PLANT
                com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants vp = null;
                if (plantDto.getId() != null) {
                    vp = railVendorPlantsRepository.findById(plantDto.getId()).orElse(null);
                }
                if (vp == null && plantDto.getPlantId() != null && !plantDto.getPlantId().trim().isEmpty()) {
                    vp = railVendorPlantsRepository.findByPlantId(plantDto.getPlantId().trim()).orElse(null);
                }
                if (vp == null) {
                    vp = railVendorPlantsRepository.findByCompanyNameAndPlantName(cleanCompanyName, plantName)
                            .orElse(new com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants());
                }

                vp.setVendorCode(vendorCodeFormatted);
                vp.setCompanyName(cleanCompanyName);
                vp.setPlantName(plantName);
                vp.setPlantId(plantId);
                vp.setPlantPincode(plantPincode);
                vp.setRio(derivedRio);
                vp.setZonalRailway(plantDto.getZonalRailway());
                vp.setContactPerson(plantDto.getContactPerson() != null ? plantDto.getContactPerson().trim() : null);
                vp.setContactPersonNumber(cleanPhone);
                vp.setStatus(plantDto.getStatus() != null ? plantDto.getStatus() : "Active");
                if (userMaster.getUserId() != null) {
                    vp.setVendorId(userMaster.getUserId().longValue());
                }
                if (vp.getId() == null) {
                    vp.setCreatedDate(LocalDateTime.now());
                }
                vp.setUpdatedDate(LocalDateTime.now());
                railVendorPlantsRepository.save(vp);

                // Ensure entry in IE_FIELDS_MAPPING for product Rail Pad
                if (!plantPincode.isEmpty()) {
                    boolean exists = ieFieldsMappingRepository.existsByPinCodeAndProduct(plantPincode, "Rail Pad");
                    if (!exists) {
                        IEFieldsMapping ieMap = new IEFieldsMapping();
                        ieMap.setPinCode(plantPincode);
                        ieMap.setProduct("Rail Pad");
                        ieMap.setStage("Rail Pad");
                        ieMap.setPlantPincode(plantPincode);
                        ieMap.setRio(derivedRio);
                        ieFieldsMappingRepository.save(ieMap);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userMaster.getUserId());
        result.put("userName", userMaster.getUsername());
        result.put("employeeCode", userMaster.getEmployeeCode());
        result.put("message", "Railpad Vendor registered successfully!");
        return result;
    }

    private synchronized String generateNextRailpadPoiCode() {
        try {
            String maxPoi = railPadPincodePoIMappingRepository.findMaxNumericPoiCode();
            if (maxPoi != null && maxPoi.toUpperCase().startsWith("POI")) {
                int num = Integer.parseInt(maxPoi.substring(3).trim());
                return String.format("POI%02d", num + 1);
            }
        } catch (Exception ignored) {
        }
        return String.format("POI%02d", railPadPincodePoIMappingRepository.count() + 1);
    }

    @Override
    public Object getRailpadVendorDetails(Integer userId) {
        UserMaster user = userMasterRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "User not found")));

        com.sarthi.dto.RailpadVendorCreationDto dto = new com.sarthi.dto.RailpadVendorCreationDto();
        dto.setUserId(user.getUserId());

        // 1. Resolve Vendor Code
        String empCode = user.getEmployeeCode();
        if (empCode == null || empCode.trim().isEmpty()) {
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                empCode = user.getUsername().trim();
            } else if (user.getEmail() != null && user.getEmail().contains("@")) {
                empCode = user.getEmail().substring(0, user.getEmail().indexOf("@")).trim();
            }
        }
        if (empCode != null && !empCode.startsWith(":")) {
            empCode = ":" + empCode;
        }
        dto.setVendorCode(empCode);

        // 2. Resolve Company Name
        String compName = user.getFullName();
        if (compName == null || compName.trim().isEmpty() || compName.startsWith(":")) {
            String vCodeCol = empCode != null && empCode.startsWith(":") ? empCode : (empCode != null ? ":" + empCode : "");
            String rawCode = empCode != null ? empCode.replaceAll("^:", "") : "";
            Optional<VendorMaster> vmOpt = vendorMasterRepository.findByVendorCode(vCodeCol);
            if (vmOpt.isEmpty() && !rawCode.isEmpty()) {
                vmOpt = vendorMasterRepository.findByVendorCode(rawCode);
            }
            if (vmOpt.isPresent() && vmOpt.get().getVendorName() != null && !vmOpt.get().getVendorName().trim().isEmpty() && !vmOpt.get().getVendorName().startsWith(":")) {
                compName = vmOpt.get().getVendorName().trim();
            }
        }
        if (compName == null || compName.trim().isEmpty() || compName.startsWith(":")) {
            Optional<com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping> rpmLookup = railPadPincodePoIMappingRepository.findByVendorCode(userId != null ? userId.toString() : "");
            if (rpmLookup.isEmpty() && empCode != null) {
                rpmLookup = railPadPincodePoIMappingRepository.findByVendorCode(empCode);
            }
            if (rpmLookup.isPresent() && rpmLookup.get().getCompanyName() != null && !rpmLookup.get().getCompanyName().trim().isEmpty() && !rpmLookup.get().getCompanyName().startsWith(":")) {
                compName = rpmLookup.get().getCompanyName().trim();
            }
        }
        if (compName == null || compName.trim().isEmpty()) {
            compName = user.getUsername() != null ? user.getUsername().replaceAll("^:", "") : empCode;
        }
        dto.setCompanyName(compName);
        dto.setEmail(user.getEmail());
        dto.setPassword(com.sarthi.util.PasswordEncryptionUtil.decrypt(user.getPassword()));
        dto.setStatus(user.getStatus());

        // 3. Fetch Single Unit info from RAILPAD_PINCODE_POI_MAPPING
        com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping rpm = null;
        if (userId != null) {
            rpm = railPadPincodePoIMappingRepository.findByVendorCode(userId.toString()).orElse(null);
        }
        if (rpm == null && empCode != null) {
            String rawCode = empCode.replaceAll("^:", "");
            rpm = railPadPincodePoIMappingRepository.findByVendorCode(empCode).orElse(null);
            if (rpm == null) {
                rpm = railPadPincodePoIMappingRepository.findByVendorCode(rawCode).orElse(null);
            }
        }
        if (rpm != null) {
            dto.setUnitId(rpm.getId());
            dto.setUnitName(rpm.getUnitName());
            dto.setUnitPinCode(rpm.getPinCode());
            dto.setCin(rpm.getCin());
            dto.setUnitAddress(rpm.getAddress());
            dto.setUnitDistrict(rpm.getDistrict());
            dto.setUnitState(rpm.getState());
            dto.setPoiCode(rpm.getPoiCode());
        }

        // 4. Fetch Multiple Plants from RAIL_VENDOR_PLANT
        List<com.sarthi.dto.RailVendorPlantDto> plantList = new ArrayList<>();
        List<com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants> vpList = new ArrayList<>();
        if (userId != null) {
            vpList = railVendorPlantsRepository.findByVendorId(userId.longValue());
        }
        if ((vpList == null || vpList.isEmpty()) && empCode != null) {
            String rawCode = empCode.replaceAll("^:", "");
            vpList = railVendorPlantsRepository.findByVendorCode(empCode);
            if (vpList == null || vpList.isEmpty()) {
                vpList = railVendorPlantsRepository.findByVendorCode(rawCode);
            }
            if (vpList == null || vpList.isEmpty()) {
                vpList = railVendorPlantsRepository.findByVendorCode(":" + rawCode);
            }
        }

        if (vpList != null && !vpList.isEmpty()) {
            for (com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants vp : vpList) {
                com.sarthi.dto.RailVendorPlantDto pDto = new com.sarthi.dto.RailVendorPlantDto();
                pDto.setId(vp.getId());
                pDto.setPlantName(vp.getPlantName());
                pDto.setPlantId(vp.getPlantId());
                pDto.setPinCode(vp.getPlantPincode());
                pDto.setRio(vp.getRio());
                pDto.setZonalRailway(vp.getZonalRailway());
                pDto.setContactPerson(vp.getContactPerson());
                pDto.setContactPersonNumber(vp.getContactPersonNumber());
                pDto.setStatus(vp.getStatus() != null ? vp.getStatus() : "Active");
                plantList.add(pDto);
            }
        }

        dto.setPlants(plantList);
        return dto;
    }

    @Transactional
    @Override
    public Map<String, Object> migrateAllPlainTextPasswords() {
        List<UserMaster> allUsers = userMasterRepository.findAll();
        int totalUsers = allUsers.size();
        int migratedCount = 0;
        int alreadyEncryptedCount = 0;

        for (UserMaster u : allUsers) {
            String pwd = u.getPassword();
            if (pwd != null && !pwd.trim().isEmpty()) {
                if (!com.sarthi.util.PasswordEncryptionUtil.isEncrypted(pwd)) {
                    u.setPassword(com.sarthi.util.PasswordEncryptionUtil.encrypt(pwd));
                    userMasterRepository.save(u);
                    migratedCount++;
                } else {
                    alreadyEncryptedCount++;
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("totalUsers", totalUsers);
        res.put("migratedCount", migratedCount);
        res.put("alreadyEncryptedCount", alreadyEncryptedCount);
        res.put("message", "Password migration completed successfully.");
        return res;
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void autoMigratePasswordsOnStartup() {
        try {
            migrateAllPlainTextPasswords();
        } catch (Exception e) {
            System.err.println("Notice: Password auto-migration on startup encountered an issue: " + e.getMessage());
        }
    }
}

