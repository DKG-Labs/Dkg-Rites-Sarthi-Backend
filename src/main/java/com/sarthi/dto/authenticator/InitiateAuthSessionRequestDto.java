package com.sarthi.dto.authenticator;

import lombok.Data;

@Data
public class InitiateAuthSessionRequestDto {
    private String userId;
    private String userName;
    private String loginTransactionId;
}
