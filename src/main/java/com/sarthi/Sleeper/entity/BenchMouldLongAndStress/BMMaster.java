package com.sarthi.Sleeper.entity.BenchMouldLongAndStress;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "bm_master")
@Data
public class BMMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String plantType;   // STRESS / LONG_LINE

    private String category;
    private String subCategory;
    private String drawingNo;


    private Integer createdBy;
    private Date createdDate;
    private Integer updatedBy;
    private Date updatedDate;
}