package com.sarthi.Sleeper.dto.BatchWeighmentDtos;


import lombok.Data;

@Data
public class ManualWeighmentDto {

    private Long id;

    private String batchNo;
    private String date;
    private String time;

    private Double ca1Actual;
    private Double ca2Actual;
    private Double faActual;
    private Double cementActual;
    private Double waterActual;
    private Double admixtureActual;
}
