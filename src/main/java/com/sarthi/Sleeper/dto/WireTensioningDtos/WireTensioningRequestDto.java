package com.sarthi.Sleeper.dto.WireTensioningDtos;


import lombok.Data;
import java.util.List;

@Data
public class WireTensioningRequestDto {

    private String batchNo;
    private String sleeperType;
    private Integer wiresPerSleeper;
    private Double targetLoadKn;

    private Integer createdBy;
    private Integer updatedBy;

    private String vendorCode;
    private String plantId;
    private String shift;
    private String location;


    private List<WireTensioningScadaDto> scadaRecords;
    private List<WireTensioningManualDto> manualRecords;
}