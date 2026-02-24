package com.sarthi.Sleeper.dto.SteamCuring;


import lombok.Data;
import java.util.List;

@Data
public class SteamCuringRequestDto {

    private String batchNo;
    private String chamber;
    private String grade;
    private String entryDate;

    private Integer createdBy;
    private Integer updatedBy;

    private List<SteamCuringScadaDto> scadaRecords;
    private List<SteamCuringManualDto> manualRecords;
}