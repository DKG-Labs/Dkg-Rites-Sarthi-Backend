package com.sarthi.Sleeper.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LonglineResponseDTO {

    private Long id;

    private Integer lineFrom;
    private Integer lineTo;
    private Integer noOfLines;
    private Integer mouldsPerLine;

    private String sleeperCategory;
    private String entryType;

    private String status;

    private int createdBy;
    private int updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}