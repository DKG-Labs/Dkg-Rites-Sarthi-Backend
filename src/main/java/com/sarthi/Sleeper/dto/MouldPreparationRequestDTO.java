package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class MouldPreparationRequestDTO {

    private String lineShedNo;

    private String preparationDate;

    private String preparationTime;

    private String batchNo;

    private String benchNo;

    private Boolean mouldCleaned;

    private Boolean oilApplied;

    private String remarks;

    private int createdBy;
    private int updateBy;
}
