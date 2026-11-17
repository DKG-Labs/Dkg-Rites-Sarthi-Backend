package com.sarthi.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    // OTP step: transaction id returned by /change-password/request-otp, and the OTP the user received
    private String transactionId;
    private String otp;
}
