package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

@Data
public class BenchDetailsResponseDto {

    private String sleeperType;
    private List<String> sleeperNos;
}
