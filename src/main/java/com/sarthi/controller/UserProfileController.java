package com.sarthi.controller;

import com.sarthi.dto.ChangePasswordRequest;
import com.sarthi.dto.SecuritySettingsRequest;
import com.sarthi.dto.UpdateProfileRequest;
import com.sarthi.dto.UserProfileResponse;
import com.sarthi.service.UserProfileService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private String getAuthenticatedIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.sarthi.entity.UserMaster userMaster) {
            if (userMaster.getEmployeeCode() != null && !userMaster.getEmployeeCode().trim().isEmpty()) {
                return userMaster.getEmployeeCode().trim();
            }
            if (userMaster.getUserId() != null) {
                return String.valueOf(userMaster.getUserId());
            }
        }
        return authentication.getName();
    }

    @GetMapping
    public ResponseEntity<APIResponse> getUserProfile(@RequestParam(required = false) String empCode) {
        String identifier = (empCode != null && !empCode.trim().isEmpty()) ? empCode.trim() : getAuthenticatedIdentifier();
        UserProfileResponse profile = userProfileService.getUserProfile(identifier);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(profile), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<APIResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        String identifier = getAuthenticatedIdentifier();
        UserProfileResponse updatedProfile = userProfileService.updateProfile(identifier, request);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updatedProfile), HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<APIResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        String identifier = getAuthenticatedIdentifier();
        userProfileService.changePassword(identifier, request);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Password updated successfully."), HttpStatus.OK);
    }

    @PutMapping("/security-settings")
    public ResponseEntity<APIResponse> updateSecuritySettings(@RequestBody SecuritySettingsRequest request) {
        String identifier = getAuthenticatedIdentifier();
        userProfileService.updateSecuritySettings(identifier, request);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Security settings updated successfully."), HttpStatus.OK);
    }
}
