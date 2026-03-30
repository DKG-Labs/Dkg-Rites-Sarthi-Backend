package com.sarthi.Sleeper.dto.PlantProfile;

import lombok.Data;

@Data
public class PlantProfileRequestDto {

    private String plantNameLocation;
    private String vendorCode;
    private String plantType;
    private Integer numberOfSheds;
    private String plantId;

    private Integer createdBy;
    private Integer updatedBy;
}