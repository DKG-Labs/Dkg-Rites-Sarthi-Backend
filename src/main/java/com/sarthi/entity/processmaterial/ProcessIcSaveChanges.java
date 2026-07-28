package com.sarthi.entity.processmaterial;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing intermediate draft modifications to Process Inspection Calls.
 * Persisted when the user clicks "Save Changes".
 */
@Entity
@Table(name = "PROCESS_IC_SAVE_CHANGES")
@Data
public class ProcessIcSaveChanges {

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

    @Column(name = "CONSIGNEE", columnDefinition = "TEXT")
    private String consignee;

    @Column(name = "CONTRACT_REF", columnDefinition = "TEXT")
    private String contractRef;

    @Column(name = "MA_NUMBER_AND_DATE", columnDefinition = "TEXT")
    private String maNumberAndDate;

    @Column(name = "BILL_PAYING_OFFICER", columnDefinition = "TEXT")
    private String billPayingOfficer;

    @Column(name = "PURCHASING_AUTHORITY", columnDefinition = "TEXT")
    private String purchasingAuthority;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "QAP_NO", columnDefinition = "TEXT")
    private String qapNo;

    @Column(name = "CHP_CLAUSE", columnDefinition = "TEXT")
    private String chpClause;

    @Column(name = "INSPECTION_DATE")
    private String inspectionDate;

    @Column(name = "MAN_DAYS")
    private String manDays;

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
