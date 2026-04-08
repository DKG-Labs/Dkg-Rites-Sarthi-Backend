package com.sarthi.Sleeper.dto.SteamCuring;


import lombok.Data;
import java.util.List;

@Data
public class SteamCuringResponseDto {

    private Long id;

    private String batchNo;
    private String chamber;
    private String grade;
    private String entryDate;

    private String location;

    private List<SteamCuringScadaDto> scadaRecords;
    private List<SteamCuringManualDto> manualRecords;
}
