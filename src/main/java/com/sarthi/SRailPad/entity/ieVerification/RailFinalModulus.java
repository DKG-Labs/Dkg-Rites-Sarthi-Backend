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
@Table(name = "rail_final_modulus")
public class RailFinalModulus extends BaseEntity {

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

    // Actual: Before Ageing samples (1 to 3)
    @Column(name = "sample_before1") private String sampleBefore1;
    @Column(name = "sample_before2") private String sampleBefore2;
    @Column(name = "sample_before3") private String sampleBefore3;

    // Actual: After Ageing samples (1 to 3)
    @Column(name = "sample_after1") private String sampleAfter1;
    @Column(name = "sample_after2") private String sampleAfter2;
    @Column(name = "sample_after3") private String sampleAfter3;

    // Marginal: Before Ageing samples (1 to 6)
    @Column(name = "marginal_before1") private String marginalBefore1;
    @Column(name = "marginal_before2") private String marginalBefore2;
    @Column(name = "marginal_before3") private String marginalBefore3;
    @Column(name = "marginal_before4") private String marginalBefore4;
    @Column(name = "marginal_before5") private String marginalBefore5;
    @Column(name = "marginal_before6") private String marginalBefore6;

    // Marginal: After Ageing samples (1 to 6)
    @Column(name = "marginal_after1") private String marginalAfter1;
    @Column(name = "marginal_after2") private String marginalAfter2;
    @Column(name = "marginal_after3") private String marginalAfter3;
    @Column(name = "marginal_after4") private String marginalAfter4;
    @Column(name = "marginal_after5") private String marginalAfter5;
    @Column(name = "marginal_after6") private String marginalAfter6;

    @Column(name = "modulus_status")
    private String modulusStatus;

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
