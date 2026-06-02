package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PSCSleeperQualityReportDto {
    private String cse;

    private String plantId;

    private String sleeperType;

    private Long noOfSleepersProducedDuringMonth;

}
