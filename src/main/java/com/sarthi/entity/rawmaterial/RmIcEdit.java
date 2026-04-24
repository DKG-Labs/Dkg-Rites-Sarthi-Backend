package com.sarthi.entity.rawmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for storing manual modifications to Raw Material Inspection Calls.
 * Only specific fields are persisted as per RITES requirements.
 */
@Entity
@Table(name = "rm_ic_edit", indexes = {
    @Index(name = "idx_rm_ic_edit_ic_no", columnList = "ic_number", unique = true)
})
@Data
public class RmIcEdit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ic_number", nullable = false, length = 50)
    private String icNumber;

    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "book_no")
    private String bookNo;

    @Column(name = "set_no")
    private String setNo;

    @Column(name = "offered_installment_no")
    private String offeredInstallmentNo;

    @Column(name = "passed_installment_no")
    private String passedInstallmentNo;

    @Column(name = "drawing_no")
    private String drawingNo;

    // Audit Fields
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
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
