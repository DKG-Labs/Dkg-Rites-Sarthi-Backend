package com.sarthi.dto;

import lombok.Data;
import java.util.List;

@Data
public class RemapDetailsDto {

    private String poiCode;
    private String companyName;
    private String unitName;
    private String unitAddress;

    private Integer currentMappedUserId;
    private String currentMappedEmployeeCode;
    private String currentMappedEmployeeName;
    private String currentMappedEmployeeRole;

    private List<AvailableEmployee> availableEmployees;

    @Data
    public static class AvailableEmployee {
        private Integer userId;
        private String employeeCode;
        private String employeeName;
        private String role;
        
        public AvailableEmployee(Integer userId, String employeeCode, String employeeName, String role) {
            this.userId = userId;
            this.employeeCode = employeeCode;
            this.employeeName = employeeName;
            this.role = role;
        }
    }
}
