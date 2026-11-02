package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.Data;

@Data
public class QualitySleeperReportDto {

    // GROUPING

    private String railwayZone;

    private String plantId;

    // Plant (CSP) name to show instead of the number after the slash of the plant id
    private String cspName;

    private String sleeperType;

    // IC based figures
    private Long icIssuedQty;
    private Long icIssuedCount;
    private String lastIcDate;


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
