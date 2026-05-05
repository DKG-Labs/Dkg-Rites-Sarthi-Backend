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

    private String vendorCode;
    private String plantId;
    private String shift;
    private String location;

    private List<WireTensioningScadaDto> scadaRecords;
    private List<WireTensioningManualDto> manualRecords;
}