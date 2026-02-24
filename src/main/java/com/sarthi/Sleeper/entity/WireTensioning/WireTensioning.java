package com.sarthi.Sleeper.entity.WireTensioning;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wire_tensioning")
@Data
public class WireTensioning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "sleeper_type")
    private String sleeperType;

    @Column(name = "wires_per_sleeper")
    private Integer wiresPerSleeper;

    @Column(name = "target_load_kn")
    private Double targetLoadKn;


    // ===== Audit =====

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;



    @OneToMany(mappedBy = "wireTensioning",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<WireTensioningScada> scadaRecords = new ArrayList<>();


    @OneToMany(mappedBy = "wireTensioning",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<WireTensioningManual> manualRecords = new ArrayList<>();
}