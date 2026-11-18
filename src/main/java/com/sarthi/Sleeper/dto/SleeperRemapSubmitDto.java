package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class SleeperRemapSubmitDto {
    private String callNo;
    private String plantId;
    private Integer oldUserId;
    private Integer newUserId;
    // true only when remapping from Pending Verification: also moves the plant's IE mapping
    private Boolean updatePlantMapping;
}
