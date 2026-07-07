package com.sarthi.entity.CricsPos;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "po_ma_header")
@Data
public class PoMaHeader {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String rly;
        private String maKey;
        private LocalDate maKeyDate;

        private String poKey;
        private String poNo;

        private String maNo;
        private LocalDate maDate;

        private String maType;

        private String vendorCode;

        @Column(length = 3000)
        private String subject;

        private String refNo;

        private LocalDate refDate;

        @Column(length = 3000)
        private String remarks;

        private String maSignOff;

        private String requestId;

        private String authSeq;

        private String authSeqFin;

        private String curUser;

        private String curUserInd;

        private String signId;

        private String reqId;

        private String finStatus;

        private String recInd;

        private String flag;

        private String status;

        private String purDiv;

        private String purSec;

        private String oldPoValue;

        private String newPoValue;

        private String poMaSrNo;

        private String publishFlag;

        private String sent4Vet;

        private LocalDate vetDate;

        private String vetBy;

        private String reqFlag;

        @OneToMany(
                mappedBy = "maPoHeader",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
        private List<PoMaDetail> items = new ArrayList<>();

}
