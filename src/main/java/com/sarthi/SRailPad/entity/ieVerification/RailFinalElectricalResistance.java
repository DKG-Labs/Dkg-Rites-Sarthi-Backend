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
@Table(name = "rail_final_electrical_resistance")
public class RailFinalElectricalResistance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "lot_no", nullable = false)
    private String lotNo;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "shift")
    private String shift;

    @Column(name = "railpad_type")
    private String railpadType;

    @Column(name = "offered_qty")
    private Integer offeredQty;

    @Column(name = "date_of_shift")
    private LocalDate dateOfShift;

    // Before Immersion Forward (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_before_forward") private String s1BeforeForward;
    @Column(name = "s2_before_forward") private String s2BeforeForward;
    @Column(name = "s3_before_forward") private String s3BeforeForward;
    @Column(name = "m1_before_forward") private String m1BeforeForward;
    @Column(name = "m2_before_forward") private String m2BeforeForward;
    @Column(name = "m3_before_forward") private String m3BeforeForward;
    @Column(name = "m4_before_forward") private String m4BeforeForward;
    @Column(name = "m5_before_forward") private String m5BeforeForward;
    @Column(name = "m6_before_forward") private String m6BeforeForward;

    // Before Immersion Reverse (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_before_reverse") private String s1BeforeReverse;
    @Column(name = "s2_before_reverse") private String s2BeforeReverse;
    @Column(name = "s3_before_reverse") private String s3BeforeReverse;
    @Column(name = "m1_before_reverse") private String m1BeforeReverse;
    @Column(name = "m2_before_reverse") private String m2BeforeReverse;
    @Column(name = "m3_before_reverse") private String m3BeforeReverse;
    @Column(name = "m4_before_reverse") private String m4BeforeReverse;
    @Column(name = "m5_before_reverse") private String m5BeforeReverse;
    @Column(name = "m6_before_reverse") private String m6BeforeReverse;

    // After Immersion Forward (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_after_forward") private String s1AfterForward;
    @Column(name = "s2_after_forward") private String s2AfterForward;
    @Column(name = "s3_after_forward") private String s3AfterForward;
    @Column(name = "m1_after_forward") private String m1AfterForward;
    @Column(name = "m2_after_forward") private String m2AfterForward;
    @Column(name = "m3_after_forward") private String m3AfterForward;
    @Column(name = "m4_after_forward") private String m4AfterForward;
    @Column(name = "m5_after_forward") private String m5AfterForward;
    @Column(name = "m6_after_forward") private String m6AfterForward;

    // After Immersion Reverse (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_after_reverse") private String s1AfterReverse;
    @Column(name = "s2_after_reverse") private String s2AfterReverse;
    @Column(name = "s3_after_reverse") private String s3AfterReverse;
    @Column(name = "m1_after_reverse") private String m1AfterReverse;
    @Column(name = "m2_after_reverse") private String m2AfterReverse;
    @Column(name = "m3_after_reverse") private String m3AfterReverse;
    @Column(name = "m4_after_reverse") private String m4AfterReverse;
    @Column(name = "m5_after_reverse") private String m5AfterReverse;
    @Column(name = "m6_after_reverse") private String m6AfterReverse;

    @Column(name = "electrical_status")
    private String electricalStatus;

    @Column(name = "not_ok_count")
    private Integer notOkCount;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

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
