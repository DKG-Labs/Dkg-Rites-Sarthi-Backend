package com.sarthi.Sleeper.dto;

import lombok.Data;
import java.util.List;

@Data
public class SleeperPoiIeMappingDto {

    private String poiCode;
    private String ieType;
    private List<Integer> ieUserIds;

}