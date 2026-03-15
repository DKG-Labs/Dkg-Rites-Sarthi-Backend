package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BatchInspectionDetailDto {

    private Long batchId;
    private String batchNumber;
    private LocalDate castingDate;
    private String sleeperType;
    private Long totalSleepers;

    private List<SleeperDto> sleepers;

}
