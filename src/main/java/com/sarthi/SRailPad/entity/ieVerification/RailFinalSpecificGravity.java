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
@Table(name = "rail_final_specific_gravity")
public class RailFinalSpecificGravity extends BaseEntity {

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

    // Compound A - Air Weight (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_a_air") private String s1AAir;
    @Column(name = "s2_a_air") private String s2AAir;
    @Column(name = "s3_a_air") private String s3AAir;
    @Column(name = "m1_a_air") private String m1AAir;
    @Column(name = "m2_a_air") private String m2AAir;
    @Column(name = "m3_a_air") private String m3AAir;
    @Column(name = "m4_a_air") private String m4AAir;
    @Column(name = "m5_a_air") private String m5AAir;
    @Column(name = "m6_a_air") private String m6AAir;

    // Compound A - Water Weight (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_a_water") private String s1AWater;
    @Column(name = "s2_a_water") private String s2AWater;
    @Column(name = "s3_a_water") private String s3AWater;
    @Column(name = "m1_a_water") private String m1AWater;
    @Column(name = "m2_a_water") private String m2AWater;
    @Column(name = "m3_a_water") private String m3AWater;
    @Column(name = "m4_a_water") private String m4AWater;
    @Column(name = "m5_a_water") private String m5AWater;
    @Column(name = "m6_a_water") private String m6AWater;

    // Compound B - Air Weight (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_b_air") private String s1BAir;
    @Column(name = "s2_b_air") private String s2BAir;
    @Column(name = "s3_b_air") private String s3BAir;
    @Column(name = "m1_b_air") private String m1BAir;
    @Column(name = "m2_b_air") private String m2BAir;
    @Column(name = "m3_b_air") private String m3BAir;
    @Column(name = "m4_b_air") private String m4BAir;
    @Column(name = "m5_b_air") private String m5BAir;
    @Column(name = "m6_b_air") private String m6BAir;

    // Compound B - Water Weight (Actual s1..3, Marginal m1..6)
    @Column(name = "s1_b_water") private String s1BWater;
    @Column(name = "s2_b_water") private String s2BWater;
    @Column(name = "s3_b_water") private String s3BWater;
    @Column(name = "m1_b_water") private String m1BWater;
    @Column(name = "m2_b_water") private String m2BWater;
    @Column(name = "m3_b_water") private String m3BWater;
    @Column(name = "m4_b_water") private String m4BWater;
    @Column(name = "m5_b_water") private String m5BWater;
    @Column(name = "m6_b_water") private String m6BWater;

    @Column(name = "sg_status")
    private String sgStatus;

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
