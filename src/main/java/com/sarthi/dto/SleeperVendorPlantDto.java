package com.sarthi.dto;

import lombok.Data;

@Data
public class SleeperVendorPlantDto {
    private Long id;
    private String plantName;
    private String plantId; // e.g. :41647/waidiyaram or location
    private String pinCode;
    private String zonalRailway;
    private String rio;
    private String contactPerson;
    private String contactPersonNumber;
    private String status = "Active";
}
