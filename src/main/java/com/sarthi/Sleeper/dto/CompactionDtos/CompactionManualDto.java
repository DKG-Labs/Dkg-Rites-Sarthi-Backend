package com.sarthi.Sleeper.dto.CompactionDtos;


import lombok.Data;

@Data
public class CompactionManualDto {

    private Long id;

    private String benchNo;
    private Double minRpm;
    private Double maxRpm;
}