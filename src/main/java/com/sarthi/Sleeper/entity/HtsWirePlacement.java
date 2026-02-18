package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hts_wire_placement")
@Data
public class HtsWirePlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_shed_no")
    private String lineShedNo;

    @Column(name = "placement_date")
    private LocalDate placementDate;

    @Column(name = "placement_time")
    private String placementTime;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "bench_no")
    private String benchNo;

    @Column(name = "sleeper_type")
    private String sleeperType;

    @Column(name = "no_of_wires_used")
    private Integer noOfWiresUsed;

    @Column(name = "hts_wire_dia_mm")
    private BigDecimal htsWireDiaMm;

    @Column(name = "lay_length_mm")
    private BigDecimal layLengthMm;

    @Column(name = "arrangement_ok")
    private Boolean arrangementOk;

    @Column(name = "overall_status")
    private String overallStatus;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "status")
    private String status;

}