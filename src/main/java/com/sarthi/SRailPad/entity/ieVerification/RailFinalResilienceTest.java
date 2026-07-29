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
@Table(name = "rail_final_resilience_test")
public class RailFinalResilienceTest extends BaseEntity {

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
    @Column(name = "s1_impact1") private String s1Impact1;
    @Column(name = "s1_impact2") private String s1Impact2;
    @Column(name = "s1_impact3") private String s1Impact3;
    @Column(name = "s1_impact4") private String s1Impact4;
    @Column(name = "s1_impact5") private String s1Impact5;
    @Column(name = "s1_impact6") private String s1Impact6;

    // Sample 2
    @Column(name = "s2_impact1") private String s2Impact1;
    @Column(name = "s2_impact2") private String s2Impact2;
    @Column(name = "s2_impact3") private String s2Impact3;
    @Column(name = "s2_impact4") private String s2Impact4;
    @Column(name = "s2_impact5") private String s2Impact5;
    @Column(name = "s2_impact6") private String s2Impact6;

    // Sample 3
    @Column(name = "s3_impact1") private String s3Impact1;
    @Column(name = "s3_impact2") private String s3Impact2;
    @Column(name = "s3_impact3") private String s3Impact3;
    @Column(name = "s3_impact4") private String s3Impact4;
    @Column(name = "s3_impact5") private String s3Impact5;
    @Column(name = "s3_impact6") private String s3Impact6;

    @Column(name = "resilience_status")
    private String resilienceStatus;

    @Column(name = "not_ok_count")
    private Integer notOkCount;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
