package com.sarthi.SRailPad.entity.inspectionCall;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Stores the "Save Changes" draft data for Process Inspection Certificate.
 * Populated when the IE clicks Save Changes.
 */
@Entity
@Table(name = "railpad_process_ic_save_changes", indexes = {
    @Index(name = "idx_rpad_pic_draft_ic_no", columnList = "ic_number", unique = true)
})
@Data
public class RailpadProcessIcSaveChanges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ic_number", nullable = false, length = 100)
    private String icNumber;

    @Column(name = "book_no")
    private String bookNo;

    @Column(name = "set_no")
    private String setNo;

    @Column(name = "installment_no")
    private String installmentNo;

    @Column(name = "contractor", columnDefinition = "TEXT")
    private String contractor;

    @Column(name = "contract_ref", columnDefinition = "TEXT")
    private String contractRef;

    @Column(name = "bill_paying_officer", columnDefinition = "TEXT")
    private String billPayingOfficer;

    @Column(name = "consignee", columnDefinition = "TEXT")
    private String consignee;

    @Column(name = "purchasing_authority", columnDefinition = "TEXT")
    private String purchasingAuthority;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "drg_no")
    private String drgNo;

    @Column(name = "spec_no")
    private String specNo;

    @Column(name = "qap_no", columnDefinition = "TEXT")
    private String qapNo;

    @Column(name = "type_of_inspection", columnDefinition = "TEXT")
    private String typeOfInspection;

    @Column(name = "chp_cl_no", columnDefinition = "TEXT")
    private String chpClNo;

    @Column(name = "lot_no")
    private String lotNo;

    @Column(name = "qty_now_offered")
    private String qtyNowOffered;

    @Column(name = "qty_now_passed")
    private String qtyNowPassed;

    @Column(name = "qty_now_rejected")
    private String qtyNowRejected;

    @Column(name = "quantity_now_passed_text", columnDefinition = "TEXT")
    private String quantityNowPassedText;

    @Column(name = "reasons_for_rejection", columnDefinition = "TEXT")
    private String reasonsForRejection;

    @Column(name = "date_of_call")
    private String dateOfCall;

    @Column(name = "no_of_visits")
    private String noOfVisits;

    @Column(name = "dates_of_inspection", columnDefinition = "TEXT")
    private String datesOfInspection;

    @Column(name = "sealing_pattern", columnDefinition = "TEXT")
    private String sealingPattern;

    @Column(name = "inspecting_engineer")
    private String inspectingEngineer;

    // Audit
    @Column(name = "created_by", length = 100)
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
