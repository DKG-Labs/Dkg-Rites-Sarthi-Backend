package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.*;
import com.sarthi.dto.WorkflowDtos.ProductCmDto;
import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.entity.*;
import com.sarthi.entity.PoiProcessIeMapping;
import com.sarthi.entity.ProcessIeUsers;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.*;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.JwtService;
import com.sarthi.service.UserService;
import com.sarthi.entity.UserProfileAuditLog;
import com.sarthi.repository.UserProfileAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            userMaster.setPassword(userDto.getPassword());
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

        UserDto userDto = new UserDto();
        userDto.setUserId(userMaster.getUserId());
        userDto.setUserName(userMaster.getUsername());
        userDto.setPassword(userMaster.getPassword());
        userDto.setMobileNumber(userMaster.getMobileNumber());
        userDto.setRoleName(userMaster.getRoleName());
        userDto.setCreatedDate(userMaster.getCreatedDate());
        userDto.setCreatedBy(userMaster.getCreatedBy());

        // Add additional fields
        userDto.setFullName(userMaster.getFullName());
        userDto.setEmployeeCode(userMaster.getEmployeeCode());
        userDto.setDesignation(userMaster.getDesignation());
        userDto.setDiscipline(userMaster.getDiscipline());
        userDto.setEmploymentType(userMaster.getEmploymentType());
        userDto.setDateOfBirth(userMaster.getDateOfBirth());
        userDto.setRio(userMaster.getRio());
        userDto.setEmail(userMaster.getEmail());
        userDto.setAlternateMobileNumber(userMaster.getAlternateMobileNumber());
        userDto.setNotificationPreferences(userMaster.getNotificationPreferences());
        userDto.setShortName(userMaster.getShortName());
        userDto.setProductType(userMaster.getProductType());
        userDto.setProfilePhotoPath(userMaster.getProfilePhotoPath());

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

        if (!loginRequestDto.getPassword().equals(user.getPassword())) {
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
        if (roleNames.contains("Vendor") || roleNames.contains("Sleeper Vendor")) {
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
        if (roleNames.contains("Vendor") || roleNames.contains("Sleeper Vendor")) {
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
            userMaster.setPassword(userDto.getPassword());
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
        List<UserDto> users = userMasterRepository.findUsersByRoleNameViaJoin(roleName).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        if (users == null || users.isEmpty()) {
            List<UserMaster> containingUsers = userMasterRepository.findByRoleNameContaining(roleName);
            if (containingUsers != null && !containingUsers.isEmpty()) {
                users = containingUsers.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
            }
        }

        return users != null ? users : new ArrayList<>();
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

        if ("RPF".equalsIgnoreCase(prefix) || "RPP".equalsIgnoreCase(prefix)) {
            com.sarthi.SRailPad.entity.RailWorkflowTransaction tx = railWorkflowTransactionRepository.findFirstByRequestIdOrderByWorkflowTransitionIdDesc(callNo);
            if (tx != null) {
                poiCode = tx.getPoiCode();
                plantId = tx.getPlantId();
            }
        } else {
            poiCode = inspectionCallRepository.findPoiByCallNo(callNo);
        }

        List<String> result = new ArrayList<>();

        if ("EP".equalsIgnoreCase(prefix)) {
            // EP calls: Strictly use poi_process_ie_mapping table via place_of_inspection
            if (poiCode != null) {
                result = pincodePoIMappingRepository.findProcessIeEmpCodeWithName(poiCode);
            }
        } else if ("RPF".equalsIgnoreCase(prefix) || "RPP".equalsIgnoreCase(prefix)) {
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
        return userMasterRepository.findAll().stream()
                .map(this::mapToResponseDTO)
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
            userRoleMasterRepository.deleteByUserId(userId);
            if (user.getEmployeeCode() != null) {
                try {
                    ieProfileRepository.deleteByEmployeeCode(user.getEmployeeCode());
                } catch (Exception e) {
                    // Ignore if no profile found or error deleting profile
                }
            }
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

        user.setPassword(requestDto.getNewPassword());
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
}

