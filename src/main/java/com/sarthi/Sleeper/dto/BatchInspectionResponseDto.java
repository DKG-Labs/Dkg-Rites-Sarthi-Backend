package com.sarthi.Sleeper.dto;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperDto;
import lombok.Data;

import java.util.List;
@Data
public class BatchInspectionResponseDto {

    private Long batchId;
    private Long totalSleepers;
    private Long goodCount;
    private Long badCount;

    private List<SleeperDto> goodSleepers;
    private List<BadSleeperDto> badSleepers;
}
