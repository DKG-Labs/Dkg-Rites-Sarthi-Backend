package com.sarthi.Sms.entity.bsp;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "chem_test_record")
@Data
public class ChemTestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ord;
    private String analysis;
    private String cResult;
    private String mnResult;
    private String pResult;
    private String sResult;
    private String siResult;
    private String alResult;
    private String hResult;
    private String cuResult;
    private String crResult;
    private String vResult;
    private String niResult;
    private String moResult;
    private String nResult;
    private String oResult;
    private String sbResult;
    private String snResult;
    private String tiResult;
    private String nbResult;
    private String ctResult;
    private String cmnResult;
    private String retestStep;
    private String remarks;
    private String type;
    private String lotNo;
    private String strandNo;
    private String heatNo;
    private String mill;
    private String sms;

    private LocalDate createdAt;
}
