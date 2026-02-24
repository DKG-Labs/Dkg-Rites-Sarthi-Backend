package com.sarthi.Sleeper.dto.BatchWeighmentDtos;


import lombok.Data;

@Data
public class ScadaWeighmentDto {

    private Long id;

    private String batchNo;
    private String date;
    private String time;

    private Double ca1Set;
    private Double ca1Actual;

    private Double ca2Set;
    private Double ca2Actual;

    private Double faSet;
    private Double faActual;

    private Double cementSet;
    private Double cementActual;

    private Double waterSet;
    private Double waterActual;

    private Double admixtureSet;
    private Double admixtureActual;

    private Double total;
}
