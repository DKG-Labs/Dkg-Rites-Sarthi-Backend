package com.sarthi.entity.CricsPos;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "po_ma_detail")
@Getter
@Setter
@ToString(exclude = "maPoHeader")
@EqualsAndHashCode(exclude = "maPoHeader")
public class PoMaDetail {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "ma_po_header_id")
        @com.fasterxml.jackson.annotation.JsonIgnore
        private PoMaHeader maPoHeader;

        private String rly;

        private String maKey;

        private String slNo;

        private String maFld;

        private String maFldDescr;

        @Column(length = 4000)
        private String oldValue;

        @Column(length = 4000)
        private String newValue;

        private String newValueInd;

        private String newValueFlag;

        private String plNo;

        private String poSr;

        private String expSr;

        private String expCode;

        private String condSlNo;

        private String condNo;

        private String condCode;

        private String status;

        private String maSrNo;

        private String origDp;

        private String paymentYear;

        @Column(length = 5000)
        private String newPoSrData;

        private String refPoNo;

        private String consigneeRly;

}
