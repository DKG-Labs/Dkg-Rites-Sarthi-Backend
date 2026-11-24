package com.sarthi.Sms.entity.bsp;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "macro_test_record")
@Data
public class MacroTestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String testName;
    private String lotNo;
    private String sampleNo;
    private String strandNo;
    private String test;
    private String sampleTestDtm;
    private String visualExam;
    private String sulphurPrint;
    private String limitPrint;
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
