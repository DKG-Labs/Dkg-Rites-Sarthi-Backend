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
@Table(name = "rail_final_ash_content")
public class RailFinalAshContent extends BaseEntity {

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

    // Compound A samples (3 actual + 6 marginal)
    @Column(name = "s1_a_crucible") private String s1ACrucible;
    @Column(name = "s1_a_sample") private String s1ASample;
    @Column(name = "s1_a_ash") private String s1AAsh;

    @Column(name = "s2_a_crucible") private String s2ACrucible;
    @Column(name = "s2_a_sample") private String s2ASample;
    @Column(name = "s2_a_ash") private String s2AAsh;

    @Column(name = "s3_a_crucible") private String s3ACrucible;
    @Column(name = "s3_a_sample") private String s3ASample;
    @Column(name = "s3_a_ash") private String s3AAsh;

    @Column(name = "m1_a_crucible") private String m1ACrucible;
    @Column(name = "m1_a_sample") private String m1ASample;
    @Column(name = "m1_a_ash") private String m1AAsh;

    @Column(name = "m2_a_crucible") private String m2ACrucible;
    @Column(name = "m2_a_sample") private String m2ASample;
    @Column(name = "m2_a_ash") private String m2AAsh;

    @Column(name = "m3_a_crucible") private String m3ACrucible;
    @Column(name = "m3_a_sample") private String m3ASample;
    @Column(name = "m3_a_ash") private String m3AAsh;

    @Column(name = "m4_a_crucible") private String m4ACrucible;
    @Column(name = "m4_a_sample") private String m4ASample;
    @Column(name = "m4_a_ash") private String m4AAsh;

    @Column(name = "m5_a_crucible") private String m5ACrucible;
    @Column(name = "m5_a_sample") private String m5ASample;
    @Column(name = "m5_a_ash") private String m5AAsh;

    @Column(name = "m6_a_crucible") private String m6ACrucible;
    @Column(name = "m6_a_sample") private String m6ASample;
    @Column(name = "m6_a_ash") private String m6AAsh;

    // Compound B samples (3 actual + 6 marginal)
    @Column(name = "s1_b_crucible") private String s1BCrucible;
    @Column(name = "s1_b_sample") private String s1BSample;
    @Column(name = "s1_b_ash") private String s1BAsh;

    @Column(name = "s2_b_crucible") private String s2BCrucible;
    @Column(name = "s2_b_sample") private String s2BSample;
    @Column(name = "s2_b_ash") private String s2BAsh;

    @Column(name = "s3_b_crucible") private String s3BCrucible;
    @Column(name = "s3_b_sample") private String s3BSample;
    @Column(name = "s3_b_ash") private String s3BAsh;

    @Column(name = "m1_b_crucible") private String m1BCrucible;
    @Column(name = "m1_b_sample") private String m1BSample;
    @Column(name = "m1_b_ash") private String m1BAsh;

    @Column(name = "m2_b_crucible") private String m2BCrucible;
    @Column(name = "m2_b_sample") private String m2BSample;
    @Column(name = "m2_b_ash") private String m2BAsh;

    @Column(name = "m3_b_crucible") private String m3BCrucible;
    @Column(name = "m3_b_sample") private String m3BSample;
    @Column(name = "m3_b_ash") private String m3BAsh;

    @Column(name = "m4_b_crucible") private String m4BCrucible;
    @Column(name = "m4_b_sample") private String m4BSample;
    @Column(name = "m4_b_ash") private String m4BAsh;

    @Column(name = "m5_b_crucible") private String m5BCrucible;
    @Column(name = "m5_b_sample") private String m5BSample;
    @Column(name = "m5_b_ash") private String m5BAsh;

    @Column(name = "m6_b_crucible") private String m6BCrucible;
    @Column(name = "m6_b_sample") private String m6BSample;
    @Column(name = "m6_b_ash") private String m6BAsh;

    @Column(name = "ash_status")
    private String ashStatus;

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
