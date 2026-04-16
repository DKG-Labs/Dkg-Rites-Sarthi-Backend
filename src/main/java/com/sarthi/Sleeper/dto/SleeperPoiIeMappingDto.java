package com.sarthi.Sleeper.dto;

import lombok.Data;
import java.util.List;

@Data
public class SleeperPoiIeMappingDto {

    private String poiCode;
    private String ieType;
    private String plantId;
    private List<Integer> ieUserIds;

}