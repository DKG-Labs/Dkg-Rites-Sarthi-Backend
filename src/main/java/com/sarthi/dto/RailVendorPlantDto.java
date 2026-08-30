package com.sarthi.dto;

import lombok.Data;

@Data
public class RailVendorPlantDto {
    private Long id;
    private String plantName;
    private String plantId;
    private String pinCode;
    private String zonalRailway;
    private String rio;
    private String contactPerson;
    private String contactPersonNumber;
    private String status = "Active";
}
