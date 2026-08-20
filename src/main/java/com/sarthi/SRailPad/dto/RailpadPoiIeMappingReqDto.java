package com.sarthi.SRailPad.dto;

import lombok.Data;

@Data
public class RailpadPoiIeMappingReqDto {
    private Long id;
    private String poiCode;
    private String plantId;
    private Integer ieUserId;
    private String ieType;
}
