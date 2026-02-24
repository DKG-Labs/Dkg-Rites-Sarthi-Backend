package com.sarthi.Sleeper.entity.Compaction;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "compaction_manual")
@Data
public class CompactionManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bench_no")
    private String benchNo;

    @Column(name = "min_rpm")
    private Double minRpm;

    @Column(name = "max_rpm")
    private Double maxRpm;

    @Column(name = "source")
    private String source; // MANUAL


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compaction_id")
    private Compaction compaction;
}