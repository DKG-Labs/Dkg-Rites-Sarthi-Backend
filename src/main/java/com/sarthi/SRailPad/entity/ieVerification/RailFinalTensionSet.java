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
@Table(name = "rail_final_tension_set")
public class RailFinalTensionSet extends BaseEntity {

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

    // Initial Length (A) actual samples (1 to 3)
    @Column(name = "sample_initial1") private String sampleInitial1;
    @Column(name = "sample_initial2") private String sampleInitial2;
    @Column(name = "sample_initial3") private String sampleInitial3;

    // Initial Length (A) marginal samples (1 to 6)
    @Column(name = "marginal_initial1") private String marginalInitial1;
    @Column(name = "marginal_initial2") private String marginalInitial2;
    @Column(name = "marginal_initial3") private String marginalInitial3;
    @Column(name = "marginal_initial4") private String marginalInitial4;
    @Column(name = "marginal_initial5") private String marginalInitial5;
    @Column(name = "marginal_initial6") private String marginalInitial6;

    // Final Length (B) actual samples (1 to 3)
    @Column(name = "sample_final1") private String sampleFinal1;
    @Column(name = "sample_final2") private String sampleFinal2;
    @Column(name = "sample_final3") private String sampleFinal3;

    // Final Length (B) marginal samples (1 to 6)
    @Column(name = "marginal_final1") private String marginalFinal1;
    @Column(name = "marginal_final2") private String marginalFinal2;
    @Column(name = "marginal_final3") private String marginalFinal3;
    @Column(name = "marginal_final4") private String marginalFinal4;
    @Column(name = "marginal_final5") private String marginalFinal5;
    @Column(name = "marginal_final6") private String marginalFinal6;

    @Column(name = "tension_status")
    private String tensionStatus;

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
