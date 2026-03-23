package com.sarthi.dto;

import lombok.Data;

@Data
public class IePinPoiDto {


    private String product;
    private String pinCode;
    private String poiCode;
    private String ieType; // PRIMARY / SECONDARY

    private String employeeCode;
}
