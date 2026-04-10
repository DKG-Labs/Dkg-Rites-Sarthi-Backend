package com.sarthi.Sleeper.dto.SteamCubeTestingDtos;

import lombok.Data;

import java.util.List;

@Data
public class SteamCubeTestingRequestDto {

    private String location;
    private String dateOfCasting; // dd/MM/yyyy
    private String batchNo;
    private String lbcTime;
    private String concreteGrade;
    private Double avgStrength;   // from UI
    private String result;


    private String vendorCode;
    private String plantId;
    private String shift;


    private Integer createdBy;

    private List<SteamCubeTestingDetailsDto> cubeDetails;
}
