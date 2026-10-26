package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "udm_rejection", uniqueConstraints = {
        @UniqueConstraint(name = "uq_rejection_number", columnNames = "rejection_number")
})
public class UdmRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number", length = 50)
    private String poNumber;

    @Column(name = "po_date")
    private LocalDateTime poDate;

    @Column(name = "pl_number", length = 50)
    private String plNumber;

    @Column(name = "po_serial_number", length = 20)
    private String poSerialNumber;

    @Column(name = "rejection_number", length = 100, nullable = false)
    private String rejectionNumber;

    @Column(name = "rejection_date")
    private LocalDateTime rejectionDate;

    @Column(name = "type_of_rejection", length = 100)
    private String typeOfRejection;

    @Column(name = "rejected_qty", precision = 15, scale = 3)
    private BigDecimal rejectedQty;

    @Column(name = "uom", length = 30)
    private String uom;

    @Column(name = "reason_for_rejection", columnDefinition = "TEXT")
    private String reasonForRejection;

    @Column(name = "rejection_value", precision = 18, scale = 2)
    private BigDecimal rejectionValue;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "ic_number", length = 100)
    private String icNumber;

    @Column(name = "ic_date")
    private LocalDateTime icDate;

    @Column(name = "inspection_agency", length = 100)
    private String inspectionAgency;

    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(name = "consignee", length = 500)
    private String consignee;

    @Column(name = "material_received_date")
    private LocalDateTime materialReceivedDate;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "fetched_date", nullable = false)
    private LocalDate fetchedDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
