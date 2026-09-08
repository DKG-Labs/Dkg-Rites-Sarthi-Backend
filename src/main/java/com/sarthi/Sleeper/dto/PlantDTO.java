package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class PlantDTO {
    private String plantName;
    private String plantId;
    private String rio;

    public PlantDTO() {}

    public PlantDTO(String plantName, String plantId) {
        this.plantName = plantName;
        this.plantId = plantId;
    }

    public PlantDTO(String plantName, String plantId, String rio) {
        this.plantName = plantName;
        this.plantId = plantId;
        this.rio = rio;
    }
}