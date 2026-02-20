package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing inventory_entries table.
 * Stores inventory entries for vendors with material, supplier, and PO details.
 */
@Entity
@Table(name = "inventory_entries", indexes = {
        @Index(name = "idx_inventory_vendor_code", columnList = "vendor_code"),
        @Index(name = "idx_inventory_heat_number", columnList = "heat_number"),
        @Index(name = "idx_inventory_sub_po_number", columnList = "sub_po_number"),
        @Index(name = "idx_inventory_status", columnList = "status")
})
@Data
public class InventoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Vendor Separation */
    @Column(name = "vendor_code", nullable = false, length = 50)
    private String vendorCode;

    @Column(name = "vendor_name", length = 150)
    private String vendorName;

    /* Company (optional for now) */
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", length = 150)
    private String companyName;

    /* Supplier & Unit */
    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;

    @Column(name = "unit_name", nullable = false, length = 150)
    private String unitName;

    @Column(name = "supplier_address", length = 255)
    private String supplierAddress;

    /* Material */
    @Column(name = "raw_material", nullable = false, length = 150)
    private String rawMaterial;

    @Column(name = "grade_specification", nullable = false, length = 100)
    private String gradeSpecification;

    @Column(name = "length_of_bars", precision = 10, scale = 2)
    private BigDecimal lengthOfBars;

    /* TC / Batch */
    @Column(name = "heat_number", nullable = false, length = 100)
    private String heatNumber;

    @Column(name = "tc_number", nullable = false, length = 100)
    private String tcNumber;

    @Column(name = "tc_date", nullable = false)
    private LocalDate tcDate;

    @Column(name = "tc_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal tcQuantity;

    /* Inspection Call Tracking */
    @Column(name = "offered_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal offeredQuantity = BigDecimal.ZERO;

    @Column(name = "qty_left_for_inspection", nullable = false, precision = 12, scale = 3)
    private BigDecimal qtyLeftForInspection = BigDecimal.ZERO;

    /* PO & Invoice */
    @Column(name = "sub_po_number", nullable = false, length = 100)
    private String subPoNumber;

    @Column(name = "sub_po_date")
    private LocalDate subPoDate;

    @Column(name = "sub_po_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal subPoQty;

    @Column(name = "invoice_number", nullable = false, length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    /* Pricing */
    @Column(name = "unit_of_measurement", nullable = false, length = 50)
    private String unitOfMeasurement;

    @Column(name = "rate_of_material", precision = 12, scale = 2)
    private BigDecimal rateOfMaterial;

    @Column(name = "rate_of_gst", precision = 5, scale = 2)
    private BigDecimal rateOfGst;

    @Column(name = "base_value_po", precision = 14, scale = 2)
    private BigDecimal baseValuePo;

    @Column(name = "total_po", precision = 14, scale = 2)
    private BigDecimal totalPo;

    /* Status */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryStatus status = InventoryStatus.FRESH_PO;

    /* Audit Fields - matching database schema */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();

        // Initialize qty_left_for_inspection with tc_quantity if not already set
        if (this.qtyLeftForInspection == null || this.qtyLeftForInspection.compareTo(BigDecimal.ZERO) == 0) {
            this.qtyLeftForInspection = this.tcQuantity != null ? this.tcQuantity : BigDecimal.ZERO;
        }

        // Initialize offered_quantity to 0 if not already set
        if (this.offeredQuantity == null) {
            this.offeredQuantity = BigDecimal.ZERO;
        }
    }

    @Column(name = "number_of_bundles")
    private Integer numberOfBundles;

    @Column(name = "tc_file_path", length = 255)
    private String tcFilePath;

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Enum for inventory entry status
     */
    public enum InventoryStatus {
        FRESH_PO,
        UNDER_INSPECTION,
        ACCEPTED,
        REJECTED,
        EXHAUSTED
    }
}
