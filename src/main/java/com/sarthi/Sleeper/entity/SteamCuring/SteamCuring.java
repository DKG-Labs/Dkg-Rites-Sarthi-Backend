package com.sarthi.Sleeper.entity.SteamCuring;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "steam_curing")
@Data
public class SteamCuring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "chamber")
    private String chamber;

    @Column(name = "grade")
    private String grade;

    @Column(name = "entry_date")
    private LocalDate entryDate;


    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;


    @OneToMany(mappedBy = "steamCuring",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<SteamCuringScada> scadaRecords = new ArrayList<>();


    @OneToMany(mappedBy = "steamCuring",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<SteamCuringManual> manualRecords = new ArrayList<>();
}
