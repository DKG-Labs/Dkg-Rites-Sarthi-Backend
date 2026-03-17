package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "longline_master")
@Data
public class LonglineMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer lineFrom;
    private Integer lineTo;
    private Integer noOfLines;
    private Integer mouldsPerLine;

    private String sleeperCategory;
    private String entryType; // RANGE / SINGLE


    private int createdBy;
    private int updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}