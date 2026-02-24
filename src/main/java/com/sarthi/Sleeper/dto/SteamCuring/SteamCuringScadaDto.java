package com.sarthi.Sleeper.dto.SteamCuring;


import lombok.Data;

import java.sql.Time;
import java.time.LocalTime;

@Data
public class SteamCuringScadaDto {

    private String date;
    private LocalTime time;
    private String batchNo;

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

    private Double totalSet;
    private Double totalActual;
}