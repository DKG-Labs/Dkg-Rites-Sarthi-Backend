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

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        return authentication.getName();
    }

    @GetMapping
    public ResponseEntity<APIResponse> getUserProfile(@RequestParam(required = false) String empCode) {
        String identifier = (empCode != null && !empCode.trim().isEmpty()) ? empCode : getAuthenticatedUsername();
        UserProfileResponse profile = userProfileService.getUserProfile(identifier);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(profile), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<APIResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        String username = getAuthenticatedUsername();
        UserProfileResponse updatedProfile = userProfileService.updateProfile(username, request);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updatedProfile), HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<APIResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        String username = getAuthenticatedUsername();
        userProfileService.changePassword(username, request);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Password updated successfully."), HttpStatus.OK);
    }

    @PutMapping("/security-settings")
    public ResponseEntity<APIResponse> updateSecuritySettings(@RequestBody SecuritySettingsRequest request) {
        String username = getAuthenticatedUsername();
        userProfileService.updateSecuritySettings(username, request);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Security settings updated successfully."), HttpStatus.OK);
    }
}
