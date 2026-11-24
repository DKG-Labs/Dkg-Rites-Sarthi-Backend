package com.sarthi.Sms.entity.bsp;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="fwt_test_record")
@Data
public class FwtTestRecord {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String testName;
    private String lotNo;
    private String sampleNo;
    private String strandNo;
    private String test;
    private String sampleTestDtm;

    @Column(name = "deflection_mm")
    private String deflectionMm;

    private String remarks;
    private String retestStep;
    private String heatNo;
    private String mill;
    private String sms;
    private String railGrade;
    private String section;
    private String heatJudgmentDt;
    private String heatStatus;
    private String heatJudgmentDtm;

    private LocalDate createdAt;
}
