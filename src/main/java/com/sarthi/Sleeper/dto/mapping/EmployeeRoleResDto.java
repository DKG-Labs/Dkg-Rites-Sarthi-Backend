package com.sarthi.Sleeper.dto.mapping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeRoleResDto {

    private Integer userId;

    private String employeeCode;
    
    private String fullName;
    
    private String userName;
}
