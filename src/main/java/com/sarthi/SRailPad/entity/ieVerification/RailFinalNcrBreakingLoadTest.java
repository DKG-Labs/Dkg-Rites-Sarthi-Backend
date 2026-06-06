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
@Table(name = "rail_final_ncr_breaking_load_test")
public class RailFinalNcrBreakingLoadTest extends BaseEntity {

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

    // 5 actual samples
    @Column(name = "sample1") private String sample1;
    @Column(name = "sample2") private String sample2;
    @Column(name = "sample3") private String sample3;
    @Column(name = "sample4") private String sample4;
    @Column(name = "sample5") private String sample5;

    // 10 marginal samples
    @Column(name = "marginal1") private String marginal1;
    @Column(name = "marginal2") private String marginal2;
    @Column(name = "marginal3") private String marginal3;
    @Column(name = "marginal4") private String marginal4;
    @Column(name = "marginal5") private String marginal5;
    @Column(name = "marginal6") private String marginal6;
    @Column(name = "marginal7") private String marginal7;
    @Column(name = "marginal8") private String marginal8;
    @Column(name = "marginal9") private String marginal9;
    @Column(name = "marginal10") private String marginal10;

    @Column(name = "ncr_breaking_status")
    private String ncrBreakingStatus;

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
