package com.sarthi.Sleeper.dto.CompactionDtos;


import lombok.Data;
import java.util.List;

@Data
public class CompactionResponseDto {

    private Long id;

    private String batchNo;
    private String sleeperType;
    private String entryDate;

    private List<CompactionScadaDto> scadaRecords;
    private List<CompactionManualDto> manualRecords;
}
