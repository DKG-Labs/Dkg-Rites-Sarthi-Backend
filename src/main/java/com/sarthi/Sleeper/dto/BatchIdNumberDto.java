package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class BatchIdNumberDto {

    private Long id;
    private String batchNumber;


    public BatchIdNumberDto(Long id, String batchNumber) {
        this.id = id;
        this.batchNumber = batchNumber;
    }
}
