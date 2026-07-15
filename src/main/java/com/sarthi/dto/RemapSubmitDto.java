package com.sarthi.dto;

import lombok.Data;

@Data
public class RemapSubmitDto {
    private String callNo;
    private String poiCode;
    private String previousEmpCode;
    private String newEmpCode;
    private String stage; // "ER" or "EF" or "EP"
}
