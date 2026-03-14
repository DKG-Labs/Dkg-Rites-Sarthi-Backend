package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompanyUnitResponseDto {

    private String companyName;
    private List<String> unitNames;
}
