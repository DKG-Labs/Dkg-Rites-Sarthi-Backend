package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SleeperBatchResultDto {

    private String batchNo;
    private BigDecimal batchOfferedQuantity;
    private BigDecimal batchPassedQuantity;
    private BigDecimal batchRejectedQuantity;
    private List<RejectedDto> rejectedSleepers;
    private List<SleeperDto> epoxyTreatedSleepers;
}
