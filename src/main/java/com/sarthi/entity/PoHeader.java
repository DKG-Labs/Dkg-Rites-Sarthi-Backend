package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "po_header")
@Data
public class PoHeader {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


        @Column(name = "po_key", nullable = false)
        private String poKey;

        private String poNo;
        private String l5PoNo;

        private String rlyCd;
        private String rlyShortName;

        private String purchaserCode;

        @Column(length = 500)
        private String purchaserDetail;

        private String stockNonStock;
        private String rlyNonRly;
        private String poOrLetter;

        private String vendorCode;

        @Column(length = 500)
        private String vendorDetails;

        @Column(length = 300)
        private String firmDetails;

        private String inspectingAgency;
        private String poStatus;

        @Column(length = 500)
        private String pdfPath;

        private LocalDateTime poDate;
        private LocalDateTime receivedDate;
        private LocalDateTime crisTimestamp;

        private String userId;

        private String sourceSystem;

        private String regionCode;
        private String remarks;
        private String billPayOff;
        private String billPayOffName;
        private String poiCd;
        private String itemCat;
        private String itemCatDescr;


        // ---- RELATION ----
        @OneToMany(mappedBy = "poHeader",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
        private List<PoItem> items = new ArrayList<>();


}

