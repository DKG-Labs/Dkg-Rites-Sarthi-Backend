package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "PROCESS_INSPECTION_DISCREPANCY")
@Data
@EntityListeners(AuditingEntityListener.class)
public class ProcessInspectionDiscrepancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DISCREPANCY_NO", unique = true, nullable = false)
    private String discrepancyNo;

    @Column(name = "DATE_OF_RAISING")
    private LocalDate dateOfRaising;

    @Column(name = "PRODUCT_TYPE", nullable = false)
    private String productType;

    @Column(name = "VENDOR_CODE", nullable = false)
    private String vendorCode;

    @Column(name = "PLANT_ID")
    private Integer plantId;

    @Column(name = "PO_NUMBER")
    private String poNumber;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "SUB_CATEGORY")
    private String subCategory;

    @Column(name = "URGENCY")
    private String urgency;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "IE_DOCUMENT_PATH")
    private String ieDocumentPath;

    @Column(name = "DATE_OF_RECTIFICATION")
    private LocalDate dateOfRectification;

    @Column(name = "CORRECTIVE_ACTION", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "VENDOR_DOCUMENT_PATH")
    private String vendorDocumentPath;

    @Column(name = "STATUS")
    private String status;

    @CreatedBy
    @Column(name = "CREATED_BY", updatable = false)
    private Integer createdBy;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", updatable = false)
    private Date createdAt;

    @LastModifiedBy
    @Column(name = "UPDATED_BY")
    private Integer updatedBy;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT")
    private Date updatedAt;
}
