package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * CorrectionSlip entity
 * Stores each row of a Correction to Inspection Certificate.
 * One call_no can have multiple correction rows.
 */
@Entity
@Table(name = "correction_slip", indexes = {
    @Index(name = "idx_correction_slip_call_no", columnList = "call_no")
})
@Data
public class CorrectionSlip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "call_no", nullable = false, length = 100)
    private String callNo;

    @Column(name = "column_name", nullable = false, length = 255)
    private String columnName;

    @Column(name = "read_as", nullable = false, columnDefinition = "TEXT")
    private String readAs;

    @Column(name = "instead_of", nullable = false, columnDefinition = "TEXT")
    private String insteadOf;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
