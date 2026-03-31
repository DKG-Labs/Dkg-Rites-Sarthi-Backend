package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MoistureAnalysisListDTO {

    private Long id;
    private String batchNo;
    private String entryDate;

    public MoistureAnalysisListDTO(Long id, String batchNo, LocalDate entryDate) {
        this.id = id;
        this.batchNo = batchNo;
        this.entryDate = entryDate.toString();
    }
}
