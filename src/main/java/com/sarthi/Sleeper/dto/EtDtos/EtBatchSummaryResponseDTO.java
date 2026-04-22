package com.sarthi.Sleeper.dto.EtDtos;

import lombok.Data;

@Data
public class EtBatchSummaryResponseDTO {

    private String batchNumber;
    private String location;

    private String dateOfCasting;
    private Long totalSleepers;
    private Long etSleepers;

    private Double etPercentage;
}
