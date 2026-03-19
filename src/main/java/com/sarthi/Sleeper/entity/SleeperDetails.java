package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sleeper_details")
@Data
public class SleeperDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sleeperName;

    @ManyToOne
    @JoinColumn(name = "longline_id")
    private LonglineMaster longlineMaster;
}
