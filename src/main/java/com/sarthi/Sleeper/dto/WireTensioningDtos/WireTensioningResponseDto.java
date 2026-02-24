package com.sarthi.Sleeper.dto.WireTensioningDtos;


import lombok.Data;
import java.util.List;

@Data
public class WireTensioningResponseDto {

    private Long id;

    private String batchNo;
    private String sleeperType;
    private Integer wiresPerSleeper;
    private Double targetLoadKn;

    private List<WireTensioningScadaDto> scadaRecords;
    private List<WireTensioningManualDto> manualRecords;
}