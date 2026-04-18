package com.sarthi.Sleeper.dto.SteamCubeTestingDtos;

import lombok.Data;

@Data
public class SteamCubeTestingDetailsDto {
    private Long id;

    private String cubeNo;
    private String dateOfTesting;
    private String time;

    private Double ageHours;
    private Double weightKgs;
    private Double loadKn;
    private Double strength;
}
