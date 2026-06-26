package com.sarthi.SRailPad.entity.inspectionCall;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "rail_inspection_call")
public class RailInspectionCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", unique = true)
    private String callNo;

    @Column(name = "po_no")
    private String poNo;

    /** PO item serial number, e.g. "001". Maps to po_item.item_sr_no */
    @Column(name = "po_sr")
    private String poSr;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "rail_pad_type")
    private String railPadType;

    @Column(name = "total_qty")
    private Integer totalQty;

    @Column(name = "no_of_lots")
    private Integer noOfLots;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "call_type")
    private String callType = "FINAL"; // Default to FINAL for backwards compatibility

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private String vendorName;

    @Transient
    private String scrCode;

    @Transient
    private String rlyPoSrNo;

    // Database fields for Drawing and Process IC reference
    @Column(name = "drawing_no")
    private String drawingNo;

    @Column(name = "process_ic_no")
    private String processIcNo;

    @Transient
    private String uom;

    @Transient
    private Integer qtyOnOrder;

    @Transient
    private Integer qtyAcceptedTillNow;

    @Transient
    private Integer qtyDesiredForFinal;

    @Transient
    private Integer qtyDue;

    @Transient
    private LocalDate productionInitiationDate;

    @OneToMany(mappedBy = "inspectionCall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RailInspectionLot> lots;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
