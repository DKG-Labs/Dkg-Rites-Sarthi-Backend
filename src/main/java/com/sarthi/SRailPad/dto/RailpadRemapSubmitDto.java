package com.sarthi.SRailPad.dto;

import lombok.Data;

@Data
public class RailpadRemapSubmitDto {
    private String callNo;
    private String plantId;
    private Integer oldUserId;
    private Integer newUserId;
}
