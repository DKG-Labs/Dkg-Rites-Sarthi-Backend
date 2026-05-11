package com.sarthi.Sleeper.dto.mapping;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SleeperPoiIeMappingResDto {

    private Long id;

    private String poiCode;

    private String plantId;

    private Integer ieUserId;

    private String ieType;

    private LocalDateTime createdDate;
}