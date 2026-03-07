package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class StressBenchResponseDto {

    private Long id;

    private String entryType;

    private Integer benchNo;
    private Integer benchFrom;
    private Integer benchTo;

    private Integer noOfBenches;

    private String sleeperCategory;
    private Integer mouldsPerBench;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;
}
