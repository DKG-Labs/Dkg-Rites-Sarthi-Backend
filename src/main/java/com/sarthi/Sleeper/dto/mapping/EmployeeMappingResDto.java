package com.sarthi.Sleeper.dto.mapping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeMappingResDto {

    private Integer userId;

    private String employeeCode;

    private String ieType;

    private String plantId;

    private String companyName;
}