package com.sarthi.Sleeper.dto.mapping;

import lombok.Data;

import java.util.List;
@Data
public class CompanyEmployeeMappingReqDto {

    private String poiCode;

    private String plantId;

    private String ieType;

    private List<String> employeeCodes;
}
