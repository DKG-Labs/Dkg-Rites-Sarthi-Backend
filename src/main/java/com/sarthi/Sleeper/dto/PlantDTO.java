package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class PlantDTO {
    private String plantName;
    private String plantId;

    public PlantDTO(String plantName, String plantId) {
        this.plantName = plantName;
        this.plantId = plantId;
    }
}