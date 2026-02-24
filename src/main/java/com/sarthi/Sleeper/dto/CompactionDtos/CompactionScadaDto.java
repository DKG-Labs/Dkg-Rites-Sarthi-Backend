package com.sarthi.Sleeper.dto.CompactionDtos;


import lombok.Data;

@Data
public class CompactionScadaDto {

    private Long id;

    private String time;
    private String benchNo;
    private Double v1V4Rpm;
    private Double duration;
}