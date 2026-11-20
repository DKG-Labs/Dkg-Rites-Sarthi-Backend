package com.sarthi.SRailPad.entity.ieVerification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Copy of an IE production batch row taken just before it is deleted (unblock / re-save / delete). */
@Entity
@Table(name = "rail_ie_production_info_history")
@Getter
@Setter
public class RailIEProductionInfoHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "unblock_history_id")
    private Long unblockHistoryId;

    @Column(name = "info_id", nullable = false)
    private Long infoId;

    @Column(name = "verification_id")
    private Long verificationId;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "production_unit")
    private String productionUnit;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "shift")
    private String shift;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "drawing_no")
    private String drawingNo;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "initial_wt")
    private Double initialWt;

    @Column(name = "final_wt")
    private Double finalWt;

    @Column(name = "quantity_produced")
    private Integer quantityProduced;

    @Column(name = "used_accepted_qty", nullable = false)
    private Integer usedAcceptedQty = 0;

    @Column(name = "used_rejected_qty", nullable = false)
    private Integer usedRejectedQty = 0;

    @Column(name = "used_in_calls")
    private String usedInCalls;

    @Column(name = "deleted_by")
    private Integer deletedBy;

    @Column(name = "deleted_by_name")
    private String deletedByName;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
}
