package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
public class MouldPreparationResponseDTO {

    private Long id;

    private String lineShedNo;

    private String preparationDate;

    private LocalTime preparationTime;

    private String batchNo;

    private String benchNo;

    private Boolean mouldCleaned;

    private Boolean oilApplied;

    private String remarks;

    private int createdBy;

    private int updatedBy;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String status;
}
