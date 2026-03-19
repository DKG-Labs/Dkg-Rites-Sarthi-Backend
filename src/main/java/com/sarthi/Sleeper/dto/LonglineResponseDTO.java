package com.sarthi.Sleeper.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LonglineResponseDTO {

    private Long id;

    // RANGE
    private Integer gangFrom;
    private Integer gangTo;

    // SINGLE
    private Integer gangNo;

    // Common
    private Integer count;
    private Integer mouldsPerGang;

    private String category;
    private String entryMode;

    private String status;

    private int createdBy;
    private int updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}