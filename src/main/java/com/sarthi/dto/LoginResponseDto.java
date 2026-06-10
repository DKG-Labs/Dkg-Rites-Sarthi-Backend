package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponseDto {

    private Integer userId;
    private String userName;
    private String vendorName;
    private List<String> roleName;
    private String token;

    private String rio;

    private String shortName;  // IE short name for IC number generation

    private String employeeCode;

}
