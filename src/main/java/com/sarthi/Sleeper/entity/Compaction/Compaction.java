package com.sarthi.Sleeper.entity.Compaction;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compaction")
@Data
public class Compaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "sleeper_type")
    private String sleeperType;

    @Column(name = "entry_date")
    private LocalDate entryDate;


    // ===== Audit =====

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;


    // ===== Relations =====

    @OneToMany(mappedBy = "compaction",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CompactionScada> scadaRecords = new ArrayList<>();


    @OneToMany(mappedBy = "compaction",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CompactionManual> manualRecords = new ArrayList<>();
}