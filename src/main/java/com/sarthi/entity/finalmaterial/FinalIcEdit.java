package com.sarthi.entity.finalmaterial;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FINAL_IC_EDIT")
@Data
public class FinalIcEdit {

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
