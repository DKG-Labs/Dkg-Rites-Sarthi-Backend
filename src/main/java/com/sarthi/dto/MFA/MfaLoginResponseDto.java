package com.sarthi.dto.MFA;

import lombok.Data;

@Data
public class MfaLoginResponseDto {

    private boolean mfaRequired;

    private String transactionId;

    private String message;

    public MfaLoginResponseDto(
            boolean mfaRequired,
            String transactionId,
            String message) {

        this.mfaRequired = mfaRequired;
        this.transactionId = transactionId;
        this.message = message;
    }


}
