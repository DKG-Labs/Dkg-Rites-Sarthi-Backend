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
@Table(name = "rail_final_hardness_test")
public class RailFinalHardnessTest extends BaseEntity {

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

    // Compound A samples (1 to 10)
    @Column(name = "sample_a1")
    private String sampleA1;

    @Column(name = "sample_a2")
    private String sampleA2;

    @Column(name = "sample_a3")
    private String sampleA3;

    @Column(name = "sample_a4")
    private String sampleA4;

    @Column(name = "sample_a5")
    private String sampleA5;

    // Compound A marginal samples (1 to 10)
    @Column(name = "marginal_a1") private String marginalA1;
    @Column(name = "marginal_a2") private String marginalA2;
    @Column(name = "marginal_a3") private String marginalA3;
    @Column(name = "marginal_a4") private String marginalA4;
    @Column(name = "marginal_a5") private String marginalA5;
    @Column(name = "marginal_a6") private String marginalA6;
    @Column(name = "marginal_a7") private String marginalA7;
    @Column(name = "marginal_a8") private String marginalA8;
    @Column(name = "marginal_a9") private String marginalA9;
    @Column(name = "marginal_a10") private String marginalA10;

    // Compound B samples (1 to 5)
    @Column(name = "sample_b1")
    private String sampleB1;

    @Column(name = "sample_b2")
    private String sampleB2;

    @Column(name = "sample_b3")
    private String sampleB3;

    @Column(name = "sample_b4")
    private String sampleB4;

    @Column(name = "sample_b5")
    private String sampleB5;

    // Compound B marginal samples (1 to 10)
    @Column(name = "marginal_b1") private String marginalB1;
    @Column(name = "marginal_b2") private String marginalB2;
    @Column(name = "marginal_b3") private String marginalB3;
    @Column(name = "marginal_b4") private String marginalB4;
    @Column(name = "marginal_b5") private String marginalB5;
    @Column(name = "marginal_b6") private String marginalB6;
    @Column(name = "marginal_b7") private String marginalB7;
    @Column(name = "marginal_b8") private String marginalB8;
    @Column(name = "marginal_b9") private String marginalB9;
    @Column(name = "marginal_b10") private String marginalB10;

    @Column(name = "hardness_status")
    private String hardnessStatus;

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
