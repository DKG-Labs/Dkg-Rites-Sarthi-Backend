package com.sarthi.Sleeper.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mf_test_details")
@Data
public class MfTestDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate testingDate;

    private Double strength;

    private Double finalStrength;

    private String result;

    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;

    private String shift;
    private String vendorCode;
    private String plantId;




    @ManyToOne
    @JoinColumn(name = "modulus_of_failure_id")
    private ModulusOfFailure modulusOfFailure;
}