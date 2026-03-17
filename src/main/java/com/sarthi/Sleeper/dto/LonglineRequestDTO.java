package com.sarthi.Sleeper.dto;


import lombok.Data;

@Data
public class LonglineRequestDTO {

    private Integer lineFrom;
    private Integer lineTo;
    private Integer noOfLines;
    private Integer mouldsPerLine;

    private String sleeperCategory;
    private String entryType;

    private int createdBy;
    private int updatedBy;
}
