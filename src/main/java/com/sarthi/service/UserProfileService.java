package com.sarthi.service;

import com.sarthi.dto.ChangePasswordRequest;
import com.sarthi.dto.SecuritySettingsRequest;
import com.sarthi.dto.UpdateProfileRequest;
import com.sarthi.dto.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getUserProfile(String username);
    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);
    void changePassword(String username, ChangePasswordRequest request);
    // Validates the password change, then sends an OTP to the user's registered mobile.
    com.sarthi.service.Impl.OtpService.OtpResult requestChangePasswordOtp(String username, ChangePasswordRequest request);
    void updateSecuritySettings(String username, SecuritySettingsRequest request);
}
