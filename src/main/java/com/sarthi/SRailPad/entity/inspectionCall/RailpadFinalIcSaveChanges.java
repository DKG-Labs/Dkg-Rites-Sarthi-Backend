package com.sarthi.SRailPad.entity.inspectionCall;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "railpad_final_ic_save_changes", indexes = {
    @Index(name = "idx_rpad_fic_draft_ic_no", columnList = "ic_number", unique = true)
})
@Data
public class RailpadFinalIcSaveChanges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ic_number", nullable = false, length = 50)
    private String icNumber;

    @Column(name = "book_no")
    private String bookNo;

    @Column(name = "set_no")
    private String setNo;

    @Column(name = "offered_inst_no")
    private String offeredInstNo;

    @Column(name = "passed_inst_no")
    private String passedInstNo;

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

    @Column(name = "qty_offered_previously")
    private String qtyOfferedPreviously;

    @Column(name = "qty_passed_previously")
    private String qtyPassedPreviously;

    @Column(name = "qty_now_rejected")
    private String qtyNowRejected;

    @Column(name = "qty_still_due")
    private String qtyStillDue;

    @Column(name = "quantity_now_passed_text", columnDefinition = "TEXT")
    private String quantityNowPassedText;

    @Column(name = "no_of_items_checked")
    private String noOfItemsChecked;

    @Column(name = "dates_of_inspection", columnDefinition = "TEXT")
    private String datesOfInspection;

    @Column(name = "tr_rec_date")
    private String trRecDate;

    @Column(name = "sealing_pattern", columnDefinition = "TEXT")
    private String sealingPattern;

    @Column(name = "facsimile_text", columnDefinition = "TEXT")
    private String facsimileText;

    @Column(name = "reasons_for_rejection", columnDefinition = "TEXT")
    private String reasonsForRejection;

    @Column(name = "inspecting_engineer")
    private String inspectingEngineer;

    @Column(name = "certificate_date")
    private String certificateDate;

    @Column(name = "contractor", columnDefinition = "TEXT")
    private String contractor;

    @Column(name = "place_of_inspection", columnDefinition = "TEXT")
    private String placeOfInspection;

    @Column(name = "no_of_visits")
    private String noOfVisits;

    @Column(name = "date_of_call")
    private String dateOfCall;

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
