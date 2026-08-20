package com.sarthi.Sleeper.dto.SteamCuring;


import lombok.Data;

@Data
public class SteamCuringManualDto {

    private String batchNo;
    private String chamber;

    private Double minTemp;
    private Double maxTemp;

    private String lbcTime;
    private String curingStage;
    private Double temperature;
}