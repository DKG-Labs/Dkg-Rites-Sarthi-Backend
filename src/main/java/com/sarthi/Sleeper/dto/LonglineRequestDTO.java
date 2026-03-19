package com.sarthi.Sleeper.dto;


import lombok.Data;

import java.util.List;

@Data
public class LonglineRequestDTO {

    private Integer gangFrom;
    private Integer gangTo;

    // SINGLE
    private Integer gangNo;

    // Common
    private Integer count;
    private Integer mouldsPerGang;

    private String category;
    private String entryMode; // RANGE / SINGLE

    private int createdBy;
    private int updatedBy;

    private List<String> sleepers;
}
