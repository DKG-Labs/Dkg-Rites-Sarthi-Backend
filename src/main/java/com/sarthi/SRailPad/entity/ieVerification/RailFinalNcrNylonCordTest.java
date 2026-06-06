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
@Table(name = "rail_final_ncr_nylon_cord_test")
public class RailFinalNcrNylonCordTest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false)
    private String callNo;

    @Column(name = "lot_no", nullable = false)
    private String lotNo;

    @Column(name = "railpad_type")
    private String railpadType;

    @Column(name = "offered_qty")
    private Integer offeredQty;

    @Column(name = "date_of_shift")
    private LocalDate dateOfShift;

    // Sample 1
    @Column(name = "s1_denier") private String s1Denier;
    @Column(name = "s1_epi") private String s1Epi;
    @Column(name = "s1_thickness") private String s1Thickness;
    @Column(name = "s1_load_at_break") private String s1LoadAtBreak;
    @Column(name = "s1_elongation") private String s1Elongation;
    @Column(name = "s1_twists") private String s1Twists;

    // Sample 2
    @Column(name = "s2_denier") private String s2Denier;
    @Column(name = "s2_epi") private String s2Epi;
    @Column(name = "s2_thickness") private String s2Thickness;
    @Column(name = "s2_load_at_break") private String s2LoadAtBreak;
    @Column(name = "s2_elongation") private String s2Elongation;
    @Column(name = "s2_twists") private String s2Twists;

    // Sample 3
    @Column(name = "s3_denier") private String s3Denier;
    @Column(name = "s3_epi") private String s3Epi;
    @Column(name = "s3_thickness") private String s3Thickness;
    @Column(name = "s3_load_at_break") private String s3LoadAtBreak;
    @Column(name = "s3_elongation") private String s3Elongation;
    @Column(name = "s3_twists") private String s3Twists;

    // Marginal 1
    @Column(name = "m1_denier") private String m1Denier;
    @Column(name = "m1_epi") private String m1Epi;
    @Column(name = "m1_thickness") private String m1Thickness;
    @Column(name = "m1_load_at_break") private String m1LoadAtBreak;
    @Column(name = "m1_elongation") private String m1Elongation;
    @Column(name = "m1_twists") private String m1Twists;

    // Marginal 2
    @Column(name = "m2_denier") private String m2Denier;
    @Column(name = "m2_epi") private String m2Epi;
    @Column(name = "m2_thickness") private String m2Thickness;
    @Column(name = "m2_load_at_break") private String m2LoadAtBreak;
    @Column(name = "m2_elongation") private String m2Elongation;
    @Column(name = "m2_twists") private String m2Twists;

    // Marginal 3
    @Column(name = "m3_denier") private String m3Denier;
    @Column(name = "m3_epi") private String m3Epi;
    @Column(name = "m3_thickness") private String m3Thickness;
    @Column(name = "m3_load_at_break") private String m3LoadAtBreak;
    @Column(name = "m3_elongation") private String m3Elongation;
    @Column(name = "m3_twists") private String m3Twists;

    // Marginal 4
    @Column(name = "m4_denier") private String m4Denier;
    @Column(name = "m4_epi") private String m4Epi;
    @Column(name = "m4_thickness") private String m4Thickness;
    @Column(name = "m4_load_at_break") private String m4LoadAtBreak;
    @Column(name = "m4_elongation") private String m4Elongation;
    @Column(name = "m4_twists") private String m4Twists;

    // Marginal 5
    @Column(name = "m5_denier") private String m5Denier;
    @Column(name = "m5_epi") private String m5Epi;
    @Column(name = "m5_thickness") private String m5Thickness;
    @Column(name = "m5_load_at_break") private String m5LoadAtBreak;
    @Column(name = "m5_elongation") private String m5Elongation;
    @Column(name = "m5_twists") private String m5Twists;

    // Marginal 6
    @Column(name = "m6_denier") private String m6Denier;
    @Column(name = "m6_epi") private String m6Epi;
    @Column(name = "m6_thickness") private String m6Thickness;
    @Column(name = "m6_load_at_break") private String m6LoadAtBreak;
    @Column(name = "m6_elongation") private String m6Elongation;
    @Column(name = "m6_twists") private String m6Twists;

    @Column(name = "ncr_cord_status")
    private String ncrCordStatus;

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
