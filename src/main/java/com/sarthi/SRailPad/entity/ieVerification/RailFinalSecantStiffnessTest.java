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
@Table(name = "rail_final_secant_stiffness_test")
public class RailFinalSecantStiffnessTest extends BaseEntity {

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

    // Sample 1 (Loads 20 & 90 with A, B, C, D deflections)
    @Column(name = "s1_s20_a") private String s1S20A;
    @Column(name = "s1_s20_b") private String s1S20B;
    @Column(name = "s1_s20_c") private String s1S20C;
    @Column(name = "s1_s20_d") private String s1S20D;
    @Column(name = "s1_s90_a") private String s1S90A;
    @Column(name = "s1_s90_b") private String s1S90B;
    @Column(name = "s1_s90_c") private String s1S90C;
    @Column(name = "s1_s90_d") private String s1S90D;

    // Sample 2
    @Column(name = "s2_s20_a") private String s2S20A;
    @Column(name = "s2_s20_b") private String s2S20B;
    @Column(name = "s2_s20_c") private String s2S20C;
    @Column(name = "s2_s20_d") private String s2S20D;
    @Column(name = "s2_s90_a") private String s2S90A;
    @Column(name = "s2_s90_b") private String s2S90B;
    @Column(name = "s2_s90_c") private String s2S90C;
    @Column(name = "s2_s90_d") private String s2S90D;

    // Marginal 1
    @Column(name = "m1_s20_a") private String m1S20A;
    @Column(name = "m1_s20_b") private String m1S20B;
    @Column(name = "m1_s20_c") private String m1S20C;
    @Column(name = "m1_s20_d") private String m1S20D;
    @Column(name = "m1_s90_a") private String m1S90A;
    @Column(name = "m1_s90_b") private String m1S90B;
    @Column(name = "m1_s90_c") private String m1S90C;
    @Column(name = "m1_s90_d") private String m1S90D;

    // Marginal 2
    @Column(name = "m2_s20_a") private String m2S20A;
    @Column(name = "m2_s20_b") private String m2S20B;
    @Column(name = "m2_s20_c") private String m2S20C;
    @Column(name = "m2_s20_d") private String m2S20D;
    @Column(name = "m2_s90_a") private String m2S90A;
    @Column(name = "m2_s90_b") private String m2S90B;
    @Column(name = "m2_s90_c") private String m2S90C;
    @Column(name = "m2_s90_d") private String m2S90D;

    // Marginal 3
    @Column(name = "m3_s20_a") private String m3S20A;
    @Column(name = "m3_s20_b") private String m3S20B;
    @Column(name = "m3_s20_c") private String m3S20C;
    @Column(name = "m3_s20_d") private String m3S20D;
    @Column(name = "m3_s90_a") private String m3S90A;
    @Column(name = "m3_s90_b") private String m3S90B;
    @Column(name = "m3_s90_c") private String m3S90C;
    @Column(name = "m3_s90_d") private String m3S90D;

    // Marginal 4
    @Column(name = "m4_s20_a") private String m4S20A;
    @Column(name = "m4_s20_b") private String m4S20B;
    @Column(name = "m4_s20_c") private String m4S20C;
    @Column(name = "m4_s20_d") private String m4S20D;
    @Column(name = "m4_s90_a") private String m4S90A;
    @Column(name = "m4_s90_b") private String m4S90B;
    @Column(name = "m4_s90_c") private String m4S90C;
    @Column(name = "m4_s90_d") private String m4S90D;

    @Column(name = "secant_status")
    private String secantStatus;

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
