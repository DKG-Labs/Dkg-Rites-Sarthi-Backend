package com.sarthi.entity.CricsPos;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "po_ma_header")
@Data
public class PoMaHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_key", unique = true)
    private String maKey;

    private String rly;
    private LocalDate maKeyDate;

    private String poKey;
    private String poNo;

    private String maNo;
    private LocalDate maDate;
    private String maType;

    private String vcode;
    private String subject;
    private String remarks;

    private String maSignOff;
    private String finStatus;
    private String status;

    private String purDiv;
    private String purSec;

    private BigDecimal oldPoValue;
    private BigDecimal newPoValue;

    private String poMaSrno;
    private String publishFlag;

    private LocalDate sent4vet;
    private LocalDate vetDate;
    private String vetBy;

    private String sourceSystem = "CRIS";

    private String refNo;
    private LocalDate refDate;

    private String requestId;

    private String authSeq;
    private String authSeqFin;

  //  private String curUser;
  //  private String curUserInd;

    @Column(name = "curuser")
    private String curUser;

    @Column(name = "curuser_ind")
    private String curUserInd;

    private String signId;
    private String reqId;

    private String recInd;
    private String flag;

    private String reqFlag;

    @OneToMany(mappedBy = "maHeader", cascade = CascadeType.ALL)
    private List<PoMaDetail> details;
}
