package com.sarthi.SRailPad.entity;

import com.sarthi.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Entity representing Railpad Vendor Financial Liability incurred due to chargeable cancellations.
 */
@Entity
@Table(name = "rail_vendor_financial_liability", indexes = {
    @Index(name = "idx_rail_liability_call_no", columnList = "call_number"),
    @Index(name = "idx_rail_liability_vendor_code", columnList = "vendor_code")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RailVendorFinancialLiability extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_number", nullable = false, length = 100)
    private String callNumber;

    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    @Column(name = "liability_type", length = 50)
    private String liabilityType; // CANCELLATION_CHARGES

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus; // PENDING / PAID
}
