package com.sarthi.Sleeper.dto.SteamCubeTestingDtos;

import lombok.Data;

import java.util.List;

@Data
public class SteamCubeTestingResponseDto {

    private Long id;

    private String location;
    private String dateOfCasting;
    private String batchNo;
    private String lbcTime;
    private String concreteGrade;


    private String vendorCode;
    private String plantId;
    private String shift;
    private Double avgStrength;
    private String result;

    private List<SteamCubeTestingDetailsDto> cubeDetails;
}
