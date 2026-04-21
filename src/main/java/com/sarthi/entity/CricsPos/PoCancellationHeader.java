package com.sarthi.entity.CricsPos;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "po_cancellation_header")
@Data
public class PoCancellationHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rly;
    private String cakey;
    private LocalDate cakeyDate;

    private String pokey;
    private String poNo;

    private String caNo;
    private LocalDate caDate;
    private String caType;

    private String vcode;

    private String refNo;
    private LocalDate refDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private String caSignOff;

    private String requestId;
    private String authSeq;
    private String authSeqFin;

    private String curuser;
    private String curuserInd;
    private String signId;

    private String reqId;
    private String finStatus;
    private String recInd;
    private String flag;

    private String status;

    private String purDiv;
    private String purSec;

    private BigDecimal oldPoValue;
    private BigDecimal newPoValue;
    private BigDecimal recoveryAmt;

    private String recadvNo;

    private String poMaSrno;
    private String caReason;

    private String reinstNo;
    private LocalDate reinstDate;

    @Column(columnDefinition = "TEXT")
    private String reinstRemarks;

    private String publishFlag;
    private String sent4vet;
    private LocalDate vetDate;
    private String vetBy;




    @OneToMany(
            mappedBy = "header",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PoCancellationDetail> details = new ArrayList<>();
}

