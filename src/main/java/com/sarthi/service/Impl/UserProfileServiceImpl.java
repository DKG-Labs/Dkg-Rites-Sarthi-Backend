package com.sarthi.service.Impl;

import com.sarthi.dto.ChangePasswordRequest;
import com.sarthi.dto.SecuritySettingsRequest;
import com.sarthi.dto.UpdateProfileRequest;
import com.sarthi.dto.UserProfileResponse;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.UserProfileAuditLog;
import com.sarthi.entity.UserRoleMaster;
import com.sarthi.entity.RoleMaster;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.UserProfileAuditRepository;
import com.sarthi.repository.UserRoleMasterRepository;
import com.sarthi.repository.RoleMasterRepository;
import com.sarthi.service.UserProfileService;
import com.sarthi.constant.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMasterRepository userMasterRepository;
    private final UserProfileAuditRepository userProfileAuditRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMasterRepository userRoleMasterRepository;
    private final RoleMasterRepository roleMasterRepository;

    @Autowired
    public UserProfileServiceImpl(UserMasterRepository userMasterRepository,
                                  UserProfileAuditRepository userProfileAuditRepository,
                                  PasswordEncoder passwordEncoder,
                                  UserRoleMasterRepository userRoleMasterRepository,
                                  RoleMasterRepository roleMasterRepository) {
        this.userMasterRepository = userMasterRepository;
        this.userProfileAuditRepository = userProfileAuditRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleMasterRepository = userRoleMasterRepository;
        this.roleMasterRepository = roleMasterRepository;
    }

    private UserMaster getUserByUsernameOrEmpCode(String identifier) {
        return userMasterRepository.findFirstByUserName(identifier)
                .orElseGet(() -> userMasterRepository.findFirstByEmployeeCode(identifier)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        404, 404, "ERROR", "User profile not found for " + identifier))));
    }

    @Override
    public UserProfileResponse getUserProfile(String username) {
        UserMaster user = getUserByUsernameOrEmpCode(username);

        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getUserId());
        response.setLoginId(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmployeeNumber(user.getEmployeeCode());
        response.setDesignation(user.getDesignation());
        response.setDepartment(user.getDiscipline() != null ? user.getDiscipline() : user.getProductType());
        response.setOrganization("RITES"); // Or map from entity if available
        response.setRegion(user.getRio());
        response.setOfficeLocation(user.getZonalRly());
        response.setRegisteredMobileNumber(user.getMobileNumber());
        response.setAlternateMobileNumber(user.getAlternateMobileNumber());
        response.setEmailAddress(user.getEmail());
        response.setProfilePhotoPath(user.getProfilePhotoPath());
        // Fetch assigned roles from UserRoleMaster
        List<UserRoleMaster> userRoles = userRoleMasterRepository.findByUserId(user.getUserId());
        String assignedRoles = userRoles.stream()
                .map(ur -> roleMasterRepository.findByRoleId(ur.getRoleId())
                        .map(RoleMaster::getRoleName)
                        .orElse(""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(", "));
        
        response.setAssignedRoles(assignedRoles.isEmpty() ? user.getRoleName() : assignedRoles);
        response.setActiveRole(user.getRoleName()); // In future, determine active role from context
        response.setUserStatus("Active"); // Or map from entity status if available
        response.setAccountCreationDate(user.getCreatedDate() != null ? user.getCreatedDate().toString() : "");
        response.setLastLoginDate(user.getLastLoginDate() != null ? user.getLastLoginDate().toString() : "");
        response.setNotificationPreferences(user.getNotificationPreferences());
        response.setLoginSecurityEnabled(user.getLoginSecurityEnabled());
        
        response.setDateOfBirth(user.getDateOfBirth());
        response.setEmployeeCode(user.getEmployeeCode());
        response.setRitesEmployeeCode(user.getRitesEmployeeCode() != null ? String.valueOf(user.getRitesEmployeeCode()) : "");
        response.setEmploymentType(user.getEmploymentType());
        response.setProductType(user.getProductType());
        response.setZonalRailway(user.getZonalRly());
        
        return response;
    }

    @Override
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        UserMaster user = getUserByUsernameOrEmpCode(username);

        java.util.List<String> modifiedFieldsList = new java.util.ArrayList<>();
        java.util.Map<String, String> oldValuesMap = new java.util.HashMap<>();
        java.util.Map<String, String> newValuesMap = new java.util.HashMap<>();

        if (request.getMobileNumber() != null && !request.getMobileNumber().trim().isEmpty()) {
            if (!java.util.Objects.equals(user.getMobileNumber(), request.getMobileNumber())) {
                modifiedFieldsList.add("Mobile");
                oldValuesMap.put("Mobile", String.valueOf(user.getMobileNumber()));
                newValuesMap.put("Mobile", String.valueOf(request.getMobileNumber()));
                user.setMobileNumber(request.getMobileNumber());
            }
        }
        
        if (!java.util.Objects.equals(user.getAlternateMobileNumber(), request.getAlternateMobileNumber())) {
            modifiedFieldsList.add("AltMobile");
            oldValuesMap.put("AltMobile", String.valueOf(user.getAlternateMobileNumber()));
            newValuesMap.put("AltMobile", String.valueOf(request.getAlternateMobileNumber()));
            user.setAlternateMobileNumber(request.getAlternateMobileNumber());
        }
        
        if (request.getEmailAddress() != null && !request.getEmailAddress().trim().isEmpty()) {
            if (!java.util.Objects.equals(user.getEmail(), request.getEmailAddress())) {
                modifiedFieldsList.add("Email");
                oldValuesMap.put("Email", String.valueOf(user.getEmail()));
                newValuesMap.put("Email", String.valueOf(request.getEmailAddress()));
                user.setEmail(request.getEmailAddress());
            }
        }
        
        if (request.getDesignation() != null) {
            if (!java.util.Objects.equals(user.getDesignation(), request.getDesignation())) {
                modifiedFieldsList.add("Designation");
                oldValuesMap.put("Designation", String.valueOf(user.getDesignation()));
                newValuesMap.put("Designation", String.valueOf(request.getDesignation()));
                user.setDesignation(request.getDesignation());
            }
        }
        
        if (!java.util.Objects.equals(user.getNotificationPreferences(), request.getNotificationPreferences())) {
            modifiedFieldsList.add("NotificationPref");
            oldValuesMap.put("NotificationPref", String.valueOf(user.getNotificationPreferences()));
            newValuesMap.put("NotificationPref", String.valueOf(request.getNotificationPreferences()));
            user.setNotificationPreferences(request.getNotificationPreferences());
        }
        
        if (request.getProfilePhotoPath() != null) {
            if (!java.util.Objects.equals(user.getProfilePhotoPath(), request.getProfilePhotoPath())) {
                modifiedFieldsList.add("ProfilePhoto");
                oldValuesMap.put("ProfilePhoto", user.getProfilePhotoPath() != null ? "[OLD IMAGE DATA]" : "[NONE]");
                newValuesMap.put("ProfilePhoto", "[NEW IMAGE DATA]");
                user.setProfilePhotoPath(request.getProfilePhotoPath());
            }
        }

        if (!modifiedFieldsList.isEmpty()) {
            user.setUpdatedBy(username);
            user.setUpdatedDate(LocalDateTime.now());
            userMasterRepository.save(user);

            String modifiedFieldsStr = String.join(", ", modifiedFieldsList);
            logAudit(user.getUserId(), "UPDATE_PROFILE", modifiedFieldsStr, oldValuesMap.toString(), newValuesMap.toString());
        }

        return getUserProfile(username);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        UserMaster user = getUserByUsernameOrEmpCode(username);

        // System currently uses plain text passwords for login, so we must match that behavior.
        // Fallback to passwordEncoder in case the system migrates to encoded passwords in the future.
        boolean isCurrentPasswordCorrect = request.getCurrentPassword().equals(user.getPassword()) || 
                                           passwordEncoder.matches(request.getCurrentPassword(), user.getPassword());

        if (!isCurrentPasswordCorrect) {
            throw new BusinessException(new ErrorDetails(400, 400, "ERROR", "Current password is incorrect."));
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(new ErrorDetails(400, 400, "ERROR", "New password and confirm password do not match."));
        }

        if (request.getNewPassword().equals(user.getPassword()) || passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(new ErrorDetails(400, 400, "ERROR", "New password cannot match the current password."));
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!request.getNewPassword().matches(passwordRegex)) {
            throw new BusinessException(new ErrorDetails(400, 400, "ERROR", "Password must be at least 8 characters, contain one uppercase, one lowercase, one number, and one special character."));
        }

        // Save as plain text to remain compatible with UserServiceImpl.login()
        user.setPassword(request.getNewPassword());
        user.setUpdatedBy(username);
        user.setUpdatedDate(LocalDateTime.now());

        userMasterRepository.save(user);

        logAudit(user.getUserId(), "CHANGE_PASSWORD", "Password", "*****", "*****");
    }

    @Override
    public void updateSecuritySettings(String username, SecuritySettingsRequest request) {
        UserMaster user = getUserByUsernameOrEmpCode(username);

        String oldValue = String.valueOf(user.getLoginSecurityEnabled());
        user.setLoginSecurityEnabled(request.getLoginSecurityEnabled());
        user.setUpdatedBy(username);
        user.setUpdatedDate(LocalDateTime.now());

        userMasterRepository.save(user);

        logAudit(user.getUserId(), "UPDATE_SECURITY_SETTINGS", "Login Security Enabled", oldValue, String.valueOf(request.getLoginSecurityEnabled()));
    }

    private void logAudit(Integer userId, String action, String modifiedFields, String oldValues, String newValues) {
        UserProfileAuditLog auditLog = new UserProfileAuditLog();
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setModifiedFields(modifiedFields);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setTimestamp(LocalDateTime.now());
        
        String ipAddress = "unknown";
        try {
            org.springframework.web.context.request.RequestAttributes attribs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attribs instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest req = ((org.springframework.web.context.request.ServletRequestAttributes) attribs).getRequest();
                ipAddress = req.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = req.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            // keep "unknown"
        }
        
        auditLog.setIpAddress(ipAddress); 
        
        userProfileAuditRepository.save(auditLog);
    }
}
