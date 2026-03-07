package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class StressBenchRequestDto {

    private String entryType; // RANGE or SINGLE


    private Integer benchNo;

    // for RANGE
    private Integer benchFrom;
    private Integer benchTo;

    private String sleeperCategory;
    private Integer mouldsPerBench;

}
