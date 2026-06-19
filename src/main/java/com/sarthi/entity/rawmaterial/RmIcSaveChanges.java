package com.sarthi.entity.rawmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for storing intermediate draft modifications to Raw Material Inspection Calls.
 * Persisted when the user clicks "Save Changes".
 */
@Entity
@Table(name = "rm_ic_save_changes", indexes = {
    @Index(name = "idx_rm_ic_save_changes_ic_no", columnList = "ic_number", unique = true)
})
@Data
public class RmIcSaveChanges {

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

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "contractor_po")
    private String contractorPo;

    @Column(name = "consignee_railway")
    private String consigneeRailway;

    @Column(name = "consignee_manufacturer")
    private String consigneeManufacturer;

    @Column(name = "purchasing_authority")
    private String purchasingAuthority;

    @Column(name = "description")
    private String description;

    @Column(name = "spec_no")
    private String specNo;

    @Column(name = "qap_no")
    private String qapNo;

    @Column(name = "chp_clause")
    private String chpClause;

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
