package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CompanyUnitResponseDto {

    private String companyName;
    private String vendorCode;
    private List<String> companyNames;
    private List<String> unitNames;
    private Map<String, String> unitVendorMap;
    private Map<String, List<String>> companyUnitMap;
}
