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
public class SleeperFinalResultRequestDto {

    private String callNumber;
    private String poNo;
    private String srNo;
    private String shift;
    private String dateOfInspection; // DMY or ISO string
    private String sleeperType;
    private BigDecimal totalOfferedQuantity;
    private BigDecimal totalAccepted;
    private BigDecimal totalRejected;
    private String plantId;
    private String createdBy;
    private String updatedBy;

    private List<SleeperBatchResultDto> batches;
}
