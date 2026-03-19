package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "longline_master")
@Data
public class LonglineMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer gangFrom;
    private Integer gangTo;

    // SINGLE field (clarity)
    private Integer gangNo;

    // Common
    private Integer count;
    private Integer mouldsPerGang;

    private String category;
    private String entryMode; // RANGE / SINGLE

    private int createdBy;
    private int updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "longlineMaster", cascade = CascadeType.ALL)
    private List<SleeperDetails> sleepers;
}