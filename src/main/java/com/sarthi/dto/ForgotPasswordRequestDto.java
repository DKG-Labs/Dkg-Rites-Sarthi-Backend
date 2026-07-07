package com.sarthi.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequestDto {
    private String identifier; // can be username or employee code
    private String newPassword;
}
