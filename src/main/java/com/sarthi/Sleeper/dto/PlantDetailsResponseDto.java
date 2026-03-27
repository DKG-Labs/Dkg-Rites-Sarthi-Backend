package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlantDetailsResponseDto {

    private String plantType;
    private Integer numberOfSheds;
    private List<String> units;
}
