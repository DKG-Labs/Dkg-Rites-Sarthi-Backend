package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sleeper_final_ic_edit", indexes = {
    @Index(name = "idx_sleeper_final_ic_edit_ic_no", columnList = "ic_number", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleeperFinalIcEdit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ic_number", unique = true, nullable = false)
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

    @Column(name = "consignee", columnDefinition = "TEXT")
    private String consignee;

    @Column(name = "cumm_qty_offered_prev")
    private String cummQtyOfferedPrev;

    @Column(name = "qty_prev_passed")
    private String qtyPrevPassed;

    @Column(name = "qty_still_due")
    private String qtyStillDue;

    @Column(name = "ma_number_and_date", columnDefinition = "TEXT")
    private String maNumberAndDate;

    @Column(name = "purchasing_authority", columnDefinition = "TEXT")
    private String purchasingAuthority;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "manufacturer", columnDefinition = "TEXT")
    private String manufacturer;

    @Column(name = "tr_rec_date")
    private String trRecDate;

    @Column(name = "no_of_visits")
    private String noOfVisits;

    @Column(name = "dates_of_inspection", columnDefinition = "TEXT")
    private String datesOfInspection;

    @Column(name = "sealing_pattern", columnDefinition = "TEXT")
    private String sealingPattern;

    @Column(name = "reasons_for_rejection", columnDefinition = "TEXT")
    private String reasonsForRejection;

    @Column(name = "facsimile_text", columnDefinition = "TEXT")
    private String facsimileText;

    @Column(name = "inspecting_engineer", columnDefinition = "TEXT")
    private String inspectingEngineer;

    @Column(name = "status")
    private String status;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
