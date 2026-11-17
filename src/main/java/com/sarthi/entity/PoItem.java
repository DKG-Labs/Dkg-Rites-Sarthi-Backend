package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "po_item")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- RELATION ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_header_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PoHeader poHeader;

 // ---- BASIC ----
 private String rly;
 @Column(name = "case_no", columnDefinition = "TEXT")
 private String caseNo;
 private String itemSrNo;
 private String plNo;

 @Column(length = 4000)
 private String itemDesc;

 // ---- CONSIGNEE ----
 private String consigneeCd;
 private String immsConsigneeCd;
 private String immsConsigneeName;

 @Column(length = 300)
 private String consigneeDetail;

 // ---- QTY & UOM ----
 // Both fields map the same decimal(10,2) "qty" column.
 // qtyExact is the one written to the DB, so decimals from CRIS (e.g. 10.18 MT) are stored as received.
 // qty is a read-only whole-number view kept for existing code; it is rounded half-up like before.
 @Column(name = "qty", insertable = false, updatable = false)
 private Integer qty;
 @Column(name = "qty")
 private BigDecimal qtyExact;

 // Whole-number quantity (existing callers): stored exactly as given
 public void setQty(Integer qty) {
  this.qty = qty;
  this.qtyExact = qty == null ? null : BigDecimal.valueOf(qty);
 }

 // Decimal quantity: stored as received; qty view rounded half-up
 public void setQtyExact(BigDecimal qtyExact) {
  this.qtyExact = qtyExact;
  this.qty = qtyExact == null ? null : qtyExact.setScale(0, java.math.RoundingMode.HALF_UP).intValue();
 }
 private Integer qtyCancelled;

 private String uomCd;
 private String uom;

 // ---- FINANCIALS ----
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

 // ---- DATES ----
 private LocalDateTime deliveryDate;
 private LocalDateTime extendedDeliveryDate;
 private LocalDateTime crisTimestamp;

 // ---- MISC ----
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

