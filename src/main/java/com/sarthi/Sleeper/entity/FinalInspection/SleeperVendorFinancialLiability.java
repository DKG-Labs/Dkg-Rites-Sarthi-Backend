package com.sarthi.Sleeper.entity.FinalInspection;

import com.sarthi.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Entity representing Sleeper Vendor Financial Liabilities for Call Cancellation charges.
 * Tracks payment status (PENDING / PAID / Approved by RITES Finance) to restrict/unblock vendor call creation.
 */
@Entity
@Table(name = "sleeper_vendor_financial_liability", indexes = {
    @Index(name = "idx_sleeper_liability_vendor_code", columnList = "vendor_code"),
    @Index(name = "idx_sleeper_liability_call_no", columnList = "call_number"),
    @Index(name = "idx_sleeper_liability_status", columnList = "payment_status")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class SleeperVendorFinancialLiability extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_number", nullable = false, length = 100)
    private String callNumber;

    @Column(name = "vendor_code", nullable = false, length = 100)
    private String vendorCode;

    @Column(name = "liability_type", length = 50)
    private String liabilityType = "CANCELLATION_CHARGES";

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus = "PENDING"; // PENDING / Payment Pending for Approval / Approved by RITES Finance
}
