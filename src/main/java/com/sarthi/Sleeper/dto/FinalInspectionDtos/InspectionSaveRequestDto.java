package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

import java.util.List;

@Data
public class InspectionSaveRequestDto {

    private Long batchId;
    private Long moduleId;
    private String shift;
    private Long createdBy;

    private String sleeperType;
    private List<SleeperInspectionDto> sleepers;

}
