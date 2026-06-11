package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.Data;

@Data
public class QualitySleeperReportDto {

    // GROUPING

    private String railwayZone;

    private String plantId;

    private String sleeperType;


    // PRODUCTION

    private Long totalProducedSleepers;

    private Long noOfSleeperInspectedInProcess;
    private Long noOfSleeperRejectedInProcess;


    private Long forDimensionToeGauge;

    private Long forEndDamage;

    private Long honeyCombingSurfaceDefectCrack;

    private Long missingDowel;

    private Long otherDefectsInsertSinkTilt;

    private Long totalRejectedDefects;

    private Double rejectionPercentage;

}
