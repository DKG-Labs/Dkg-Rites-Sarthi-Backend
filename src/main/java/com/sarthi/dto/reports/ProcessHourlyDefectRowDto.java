package com.sarthi.dto.reports;

import lombok.Data;

@Data
public class ProcessHourlyDefectRowDto {

    private Integer hourIndex;
    private String hourLabel;
    private Boolean noProduction = false;
    private String lotNo;

    // Shearing defects
    private Integer lengthCutBarRejected = 0;
    private Integer improperDiaRejected = 0;
    private Integer sharpEdgesRejected = 0;
    private Integer crackedEdgesRejected = 0;

    // Turning defects
    private Integer parallelLengthRejected = 0;
    private Integer fullTurningLengthRejected = 0;
    private Integer turningDiaRejected = 0;

    // MPI defect
    private Integer mpiRejected = 0;

    // Forging defects
    private Integer forgingTempRejected = 0;
    private Integer forgingStabilisationRejectionRejected = 0;
    private Integer improperForgingRejected = 0;
    private Integer forgingDefectRejected = 0;
    private Integer forgingEmbossingRejected = 0;

    // Quenching defects
    private Integer quenchingTemperatureRejected = 0;
    private Integer quenchingDurationRejected = 0;
    private Integer quenchingHardnessRejected = 0;
    private Integer quenchingBoxGaugeRejected = 0;
    private Integer quenchingFlatBearingAreaRejected = 0;
    private Integer quenchingFallingGaugeRejected = 0;

    // Tempering Base defects
    private Integer temperingTemperatureRejected = 0;
    private Integer temperingDurationRejected = 0;

    // Final Check defects
    private Integer surfaceDefectRejected = 0;
    private Integer embossingDefectRejected = 0;
    private Integer markingRejected = 0;
    private Integer temperingHardnessRejected = 0;
    private Integer finalBoxGaugeRejected = 0;
    private Integer finalFlatBearingAreaRejected = 0;
    private Integer finalFallingGaugeRejected = 0;

    // Testing & Finishing defects
    private Integer toeLoadRejected = 0;
    private Integer weightRejected = 0;
    private Integer paintIdentificationRejected = 0;
    private Integer ercCoatingRejected = 0;
}
