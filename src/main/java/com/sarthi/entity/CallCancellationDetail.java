package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Entity representing SRS Call Cancellation Details.
 * Stores call cancellation fields, calculation results, and document references.
 */
@Entity
@Table(name = "call_cancellation_details", indexes = {
    @Index(name = "idx_cancellation_call_no", columnList = "call_number"),
    @Index(name = "idx_cancellation_vendor_code", columnList = "vendor_code")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class CallCancellationDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_number", nullable = false, length = 100)
    private String callNumber;

    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    @Column(name = "cancellation_basis", nullable = false, length = 50)
    private String cancellationBasis; // CHARGEABLE / NON_CHARGEABLE

    @Column(name = "visit_status", length = 50)
    private String visitStatus;       // BEFORE_VISIT / AFTER_VISIT

    @Column(name = "reasons", columnDefinition = "TEXT")
    private String reasons;            // Semicolon-separated reasons

    @Column(name = "cancellation_description", columnDefinition = "TEXT")
    private String cancellationDescription;

    @Column(name = "material_value", precision = 15, scale = 2)
    private BigDecimal materialValue;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "calculated_charges", precision = 15, scale = 2)
    private BigDecimal calculatedCharges;

    @Column(name = "maximum_cap", precision = 15, scale = 2)
    private BigDecimal maximumCap;

    @Column(name = "final_cancellation_charges", precision = 15, scale = 2)
    private BigDecimal finalCancellationCharges;

    @Column(name = "document_name", length = 255)
    private String documentName;

    @Column(name = "action_by")
    private Long actionBy;
}
