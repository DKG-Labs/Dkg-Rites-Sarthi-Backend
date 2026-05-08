package com.sarthi.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserDto {
    private Integer userId;
    private String password;
    private String userName;
    private String mobileNumber;
    private String roleName;
    private String createdBy;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdDate;
    private String fullName;
    private String employeeCode;
    private String designation;
    private String discipline;
    private String employmentType;
    private String dateOfBirth;
    private String rio;
}
