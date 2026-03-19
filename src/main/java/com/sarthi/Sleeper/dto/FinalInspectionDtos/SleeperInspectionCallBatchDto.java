package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.util.List;

@Data
public class SleeperInspectionCallBatchDto {
    private String batchNo;
    private List<String> goodSleepers;
    private List<String> badSleepers;
}
