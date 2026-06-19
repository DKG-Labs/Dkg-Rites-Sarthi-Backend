package com.sarthi.entity.finalmaterial;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing intermediate draft modifications to Final Product Inspection Calls.
 * Persisted when the user clicks "Save Changes".
 */
@Entity
@Table(name = "FINAL_IC_SAVE_CHANGES")
@Data
public class FinalIcSaveChanges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "IC_NUMBER", unique = true)
    private String icNumber;

    @Column(name = "CERTIFICATE_ID")
    private Long certificateId;

    @Column(name = "BOOK_NO")
    private String bookNo;

    @Column(name = "SET_NO")
    private String setNo;

    @Column(name = "OFFERED_INSTALLMENT_NO")
    private String offeredInstallmentNo;

    @Column(name = "PASSED_INSTALLMENT_NO")
    private String passedInstallmentNo;

    @Column(name = "CONSIGNEE")
    private String consignee;

    @Column(name = "CUMM_QTY_OFFERED_PREV")
    private String cummQtyOfferedPrev;

    @Column(name = "QTY_PREV_PASSED")
    private String qtyPrevPassed;

    @Column(name = "QTY_STILL_DUE")
    private String qtyStillDue;

    @Column(name = "MA_NUMBER_AND_DATE")
    private String maNumberAndDate;

    @Column(name = "PURCHASING_AUTHORITY")
    private String purchasingAuthority;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TR_REC_DATE")
    private String trRecDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
