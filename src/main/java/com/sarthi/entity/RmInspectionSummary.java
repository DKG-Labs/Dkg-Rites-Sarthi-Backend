package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing Raw Material Inspection Summary.
 * Stores pre-inspection data and inspector details when inspection is finished.
 */
@Entity
@Table(name = "rm_inspection_summary", indexes = {
    @Index(name = "idx_rm_summary_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_rm_summary_po_no", columnList = "po_no")
})
@Data
public class RmInspectionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, unique = true, length = 50)
    private String inspectionCallNo;

    // Pre-Inspection Summary
    @Column(name = "total_heats_offered")
    private Integer totalHeatsOffered;

    @Column(name = "total_qty_offered_mt", precision = 12, scale = 4)
    private BigDecimal totalQtyOfferedMt;

    @Column(name = "number_of_bundles")
    private Integer numberOfBundles;

    @Column(name = "number_of_erc")
    private Integer numberOfErc;

    @Column(name = "product_model", length = 20)
    private String productModel;

    @Column(name = "divisor", precision = 10, scale = 4)
    private BigDecimal divisor;

    @Column(name = "po_no", length = 50)
    private String poNo;

    @Column(name = "po_date")
    private LocalDate poDate;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "place_of_inspection", length = 200)
    private String placeOfInspection;

    @Column(name = "source_of_raw_material", length = 100)
    private String sourceOfRawMaterial;

    // Inspector Details
    @Column(name = "finished_by", length = 100)
    private String finishedBy;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "shift_of_inspection", length = 20)
    private String shiftOfInspection;

    // Audit Fields
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

