package com.sarthi.Sleeper.dto.mapping;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class CompanyEmployeeMappingResDto {

    private List<String> successEmployees;

    private List<String> failedEmployees;
}
