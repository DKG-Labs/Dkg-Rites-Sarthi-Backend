package com.sarthi.dto;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Integer userId;
    private String loginId; // maps to userName
    private String fullName;
    private String employeeNumber;
    private String designation;
    private String department; // maps to discipline or productType based on existing
    private String organization;
    private String region;
    private String officeLocation;
    private String registeredMobileNumber;
    private String alternateMobileNumber;
    private String emailAddress;
    private String profilePhotoPath;
    private String assignedRoles; // maps to roleName
    private String activeRole; // Could be passed dynamically
    private String userStatus;
    private String accountCreationDate;
    private String lastLoginDate;
    private String notificationPreferences;
    private Boolean loginSecurityEnabled;
    private String dateOfBirth;
    private String employeeCode;
    private String ritesEmployeeCode;
    private String employmentType;
    private String productType;
    private String zonalRailway;
}
