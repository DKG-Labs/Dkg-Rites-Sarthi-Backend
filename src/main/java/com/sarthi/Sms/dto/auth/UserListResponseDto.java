package com.sarthi.Sms.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponseDto {
    private String userId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String userType;   // SARTHI role name(s)
    private Boolean isActive;
    private LocalDateTime createDate;
}
