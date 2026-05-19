package com.sarthi.dto.ibsDtos;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String loginId;
    private String password;
}
