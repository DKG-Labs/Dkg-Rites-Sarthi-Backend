package com.sarthi.Sleeper.entity.Compaction;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "compaction_scada")
@Data
public class CompactionScada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "bench_no")
    private String benchNo;

    @Column(name = "v1_v4_rpm")
    private Double v1V4Rpm;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "source")
    private String source; // SCADA


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compaction_id")
    private Compaction compaction;
}