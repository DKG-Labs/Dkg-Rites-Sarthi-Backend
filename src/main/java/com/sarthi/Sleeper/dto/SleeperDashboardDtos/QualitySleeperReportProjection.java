package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

public interface QualitySleeperReportProjection {

    String getRailwayZone();

    String getPlantId();

    String getSleeperType();

    Long getTotalProducedSleepers();
    Long getNoOfSleeperInspectedInProcess();

    Long getNoOfSleeperRejectedInProcess();

    Long getForDimensionToeGauge();

    Long getForEndDamage();

    Long getHoneyCombingSurfaceDefectCrack();

    Long getMissingDowel();

    Long getOtherDefectsInsertSinkTilt();

    Long getTotalRejectedDefects();

    Double getRejectionPercentage();

}