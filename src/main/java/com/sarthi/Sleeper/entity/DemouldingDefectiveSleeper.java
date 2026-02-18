package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "demoulding_defective_sleepers")
@Data
public class DemouldingDefectiveSleeper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String benchGangNo;

    private String sequenceNo;

    private String sleeperNo;

    private String visualReason;

    private String dimReason;

    private LocalDateTime createdDate;


    /* Many Defects → One Inspection */

    @ManyToOne
    @JoinColumn(name = "inspection_id")
    private DemouldingInspection inspection;
}
