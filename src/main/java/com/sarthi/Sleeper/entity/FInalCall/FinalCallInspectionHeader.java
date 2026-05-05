package com.sarthi.Sleeper.entity.FInalCall;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "final_call_inspection_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalCallInspectionHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RLY + PO
    @Column(name = "rly_po_no")
    private String rlyPoNo;

    @Column(name = "call_no")
    private String callNo;

    @Column(name = "po_date")
    private LocalDate poDate;

    @Column(name = "vendor_name")
    private String vendorName;

    // PO Details
    @Column(name = "po_qty")
    private Integer poQty;

    @Column(name = "ma_no")
    private String maNo;

    @Column(name = "ma_date")
    private LocalDate maDate;

    // Inspection Summary
    @Column(name = "qty_offered_now")
    private Integer qtyOfferedNow;

    @Column(name = "accepted_qty")
    private Integer acceptedQty;

    @Column(name = "rejected_qty")
    private Integer rejectedQty;

    // ET Details
    @Column(name = "et_sleepers")
    private Integer etSleepers;

    @Column(name = "call_date")
    private LocalDate callDate;

    @Column(name = "no_of_batches")
    private Integer noOfBatches;

    private String shift;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "vendor_code")
    private String vendorCode;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
