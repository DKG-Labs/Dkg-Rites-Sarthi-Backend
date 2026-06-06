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
@Table(name = "rail_final_ncr_adhesion_test")
public class RailFinalNcrAdhesionTest extends BaseEntity {

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

    // Samples (peel, hpull)
    @Column(name = "s1_peel") private String s1Peel;
    @Column(name = "s1_hpull") private String s1Hpull;

    @Column(name = "s2_peel") private String s2Peel;
    @Column(name = "s2_hpull") private String s2Hpull;

    @Column(name = "m1_peel") private String m1Peel;
    @Column(name = "m1_hpull") private String m1Hpull;

    @Column(name = "m2_peel") private String m2Peel;
    @Column(name = "m2_hpull") private String m2Hpull;

    @Column(name = "m3_peel") private String m3Peel;
    @Column(name = "m3_hpull") private String m3Hpull;

    @Column(name = "m4_peel") private String m4Peel;
    @Column(name = "m4_hpull") private String m4Hpull;

    @Column(name = "ncr_adhesion_status")
    private String ncrAdhesionStatus;

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
