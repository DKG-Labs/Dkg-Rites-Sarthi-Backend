package com.sarthi.Sleeper.dto.SleeperDashboardDtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefectReasonDistributionDto {

    private String category;

    private String defectReason;

    private Long defectCount;

    private Double percentage;
}