package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cement_fineness_test")
@Data
public class CementFinenessTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_of_testing")
    private String typeOfTesting;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "consignment_no")
    private String consignmentNo;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "sample_weight_w1")
    private Double sampleWeightW1;

    @Column(name = "residue_weight_w2")
    private Double residueWeightW2;

    @Column(name = "residue1")
    private Double residue1;

    @Column(name = "residue2")
    private Double residue2;

    @Column(name = "residue3")
    private Double residue3;

    @Column(name = "percentage_fineness")
    private Double percentageFineness;

    @Column(name = "result")
    private String result;

    @Column(name = "shift")
    private String shift;

    @Column(name = "line_no")
    private String lineNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
