package com.sarthi.dto;

import lombok.Data;

import java.util.List;

@Data
public class NearestPlantResponseDto {
    private Double ieLatitude;
    private Double ieLongitude;
    private NearestPlantDto nearestPlant;
    private List<NearestPlantDto> allPlants;
}
