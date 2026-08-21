package com.sarthi.entity.CricsPos;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "amendment_po_item")
@Getter
@Setter
@ToString(exclude = "amendedPoHeader")
@EqualsAndHashCode(exclude = "amendedPoHeader")
public class AmendedPoItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "amended_po_header_id")
        private AmendedPoHeader amendedPoHeader;

        // BASIC

        private String rly;

        private String poKey;

        private String caseNo;

        private String itemSrNo;

        private String plNo;

        @Column(length = 4000)
        private String itemDesc;

        // CONSIGNEE

        private String consigneeCd;

        private String immsConsigneeCd;

        private String immsConsigneeName;

        @Column(length = 300)
        private String consigneeDetail;

        // QUANTITY

        private Integer qty;

        private Integer qtyCancelled;

        private String uomCd;

        private String uom;

        // FINANCIAL

        private BigDecimal rate;

        private BigDecimal basicValue;

        private BigDecimal salesTaxPercent;

        private BigDecimal salesTax;

        private String discountType;

        private BigDecimal discountPercent;

        private BigDecimal discount;

        private BigDecimal value;

        private String otChargeType;

        private BigDecimal otChargePercent;

        private BigDecimal otherCharges;

        // DATES

        private LocalDateTime deliveryDate;

        private LocalDateTime extendedDeliveryDate;

        private LocalDateTime crisTimestamp;

        // OTHER

        private String allocation;

        private String userId;

        private String sourceSystem;

        private String consigneeRly;

        private String consigneeRlyShortName;

        private String pRly;

        private String billPayOff;

        private String billPayOffDesc;

        private String billPassOff;

}






