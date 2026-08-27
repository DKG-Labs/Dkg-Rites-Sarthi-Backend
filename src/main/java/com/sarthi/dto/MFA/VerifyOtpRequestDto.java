package com.sarthi.dto.MFA;

import lombok.Data;

@Data
public class VerifyOtpRequestDto {

    private String transactionId;
    private String otp;


}