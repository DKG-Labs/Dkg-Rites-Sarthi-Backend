package com.sarthi.Sleeper.dto.PlantProfile;

import lombok.Data;

@Data
public class PlantProfileRequestDto {

    private String plantNameLocation;
    private String vendorCode;
    private String plantType;
    private Integer numberOfSheds;

    private Integer createdBy;
    private Integer updatedBy;
}