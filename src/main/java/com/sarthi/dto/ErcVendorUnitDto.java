package com.sarthi.dto;

import lombok.Data;

@Data
public class ErcVendorUnitDto {
    private Long id;
    private String unitName;
    private String pinCode;
    private String cin;
    private String address;
    private String district;
    private String state;
    private String contactPerson;
    private String contactPersonNumber;
    private String poiCode;
    private String rio;
    private String status = "Active";
}
