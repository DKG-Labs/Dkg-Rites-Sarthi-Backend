package com.sarthi.SRailPad.entity.ieVerification;

import com.sarthi.SRailPad.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "rail_final_visual_dimensional_inspection")
public class RailFinalVisualDimensionalInspection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "lot_no", nullable = false)
    private String lotNo;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "railpad_type")
    private String railpadType;

    @Column(name = "offered_qty")
    private Integer offeredQty;

    @Column(name = "date_of_shift")
    private LocalDate dateOfShift;

    // Visual Inspection fields
    @Column(name = "visual_samples")
    private Integer visualSamples;

    @Column(name = "visual_not_ok")
    private Integer visualNotOk;

    @Column(name = "visual_reason")
    private String visualReason;

    @Column(name = "visual_result")
    private String visualResult;

    // Dimensional Inspection fields
    @Column(name = "dimensional_samples")
    private Integer dimensionalSamples;

    @Column(name = "dimensional_not_ok")
    private Integer dimensionalNotOk;

    @Column(name = "dimensional_reason")
    private String dimensionalReason;

    @Column(name = "dimensional_result")
    private String dimensionalResult;

    // Total Rejected
    @Column(name = "total_rejected")
    private Integer totalRejected;

    @PrePersist
    protected void onCreate() {
        setCreatedDate(java.time.LocalDateTime.now());
        setUpdatedDate(java.time.LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedDate(java.time.LocalDateTime.now());
    }
}
