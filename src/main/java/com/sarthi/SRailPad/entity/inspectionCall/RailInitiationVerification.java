package com.sarthi.SRailPad.entity.inspectionCall;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores the IE officer's verified Section A & B data for a Railpad inspection call.
 * Also holds the shift details entered when clicking "OPEN & VERIFY FORM".
 *
 * Table: rail_initiation_verification
 */
@Data
@Entity
@Table(name = "rail_initiation_verification")
public class RailInitiationVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", unique = true, nullable = false)
    private String callNo;

    // ---- Section A: PO Information ----
    @Column(name = "rly_po_no")
    private String rlyPoNo;

    @Column(name = "po_no")
    private String poNo;

    @Column(name = "po_date")
    private String poDate;

    @Column(name = "po_qty")
    private Integer poQty;

    @Column(name = "po_sr_qty")
    private Integer poSrQty;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "ma_no")
    private String maNo;

    @Column(name = "ma_date")
    private String maDate;

    @Column(name = "purchasing_authority", length = 500)
    private String purchasingAuthority;

    @Column(name = "bill_paying_officer")
    private String billPayingOfficer;

    @Column(name = "section_a_status")
    private String sectionAStatus = "approved";

    // ---- Section B: Inspection Call Details ----
    @Column(name = "rly_po_no_serial", length = 150)
    private String rlyPoNoSerial;

    @Column(name = "item_desc", columnDefinition = "TEXT")
    private String itemDesc;

    @Column(name = "erc_type")
    private String ercType;

    @Column(name = "unit")
    private String unit;

    @Column(name = "consignee")
    private String consignee;

    @Column(name = "orig_dp")
    private String origDp;

    @Column(name = "ext_dp")
    private String extDp;

    @Column(name = "call_qty")
    private String callQty;

    @Column(name = "qty_unit")
    private String qtyUnit;

    @Column(name = "place_of_inspection")
    private String placeOfInspection;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "section_b_status")
    private String sectionBStatus = "approved";

    // ---- Shift Details (from ShiftDutyForm modal) ----
    @Column(name = "shift")
    private String shift;

    @Column(name = "company")
    private String company;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "production_unit")
    private String productionUnit;

    // ---- Audit ----
    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        if (verifiedAt == null) {
            verifiedAt = LocalDateTime.now();
        }
    }
}
