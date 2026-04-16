package com.sarthi.Sleeper.dto.CompactionDtos;


import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class CompactionResponseDto {

    private Long id;

    private String batchNo;
    private String sleeperType;
    private String entryDate;

    private String vendorCode;
    private String plantId;
    private String shift;

    private String location;
    private LocalTime time;

    private List<CompactionScadaDto> scadaRecords;
    private List<CompactionManualDto> manualRecords;
}
