package com.sarthi.udmcomplaint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CmOptionDto {
    private Integer userId;
    private String name;
    private String employeeCode;
}
