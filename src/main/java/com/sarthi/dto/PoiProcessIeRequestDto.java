package com.sarthi.dto;

import lombok.Data;

import java.util.List;
@Data
public class PoiProcessIeRequestDto {

    private String poiCode;
    private List<String> employeeCodes;
    private Integer createdBy;
}
