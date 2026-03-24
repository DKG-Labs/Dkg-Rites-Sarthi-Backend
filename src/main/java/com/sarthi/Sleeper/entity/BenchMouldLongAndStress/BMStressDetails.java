package com.sarthi.Sleeper.entity.BenchMouldLongAndStress;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "bm_stress_details")
@Data
public class BMStressDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bm_master_id")
    private BMMaster bmMaster;

    // Turnout only (NULL for normal)
    private String sleeperCode;
    private String sleeperDrawingNo;

    private String declarationMode; // SINGLE / RANGE

    // Bench fields
    private Integer benchFrom;
    private Integer benchTo;
    private Integer benchNumber;

    private Integer noOfMoulds;

    // Audit
    private Integer createdBy;
    private Date createdDate;
    private Integer updatedBy;
    private Date updatedDate;
}