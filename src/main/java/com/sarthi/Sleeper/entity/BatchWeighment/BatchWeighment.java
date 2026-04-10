package com.sarthi.Sleeper.entity.BatchWeighment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "batch_weighment")
@Data
public class BatchWeighment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;

    private  String batchNumber;

    private String moistureAnalysis;

    private String lineNo;

    private LocalDate entryDate;

    private String sandType;

    private String moistureSensorStatus; // WORKING / NOT_AVAILABLE / NOT_WORKING

    private String verifiedBy;

    @Column(length = 1000)
    private String remarks;

    private String entryMode; // SCADA / MANUAL / MIXED

    private LocalDateTime createdDate;

    private int createdBy;

    private LocalDateTime updatedDate;

    private int updatedBy;

    private String vendorCode;
    private String plantId;
    private String shift;
    // ================= Relations =================

    @OneToMany(mappedBy = "batchWeighment", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<BatchDetails> batchDetailsList = new ArrayList<>();;

    @OneToMany(mappedBy = "batchWeighment", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<ScadaWeighment> scadaRecords= new ArrayList<>();;

    @OneToMany(mappedBy = "batchWeighment", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<ManualWeighment> manualRecords = new ArrayList<>();;
}