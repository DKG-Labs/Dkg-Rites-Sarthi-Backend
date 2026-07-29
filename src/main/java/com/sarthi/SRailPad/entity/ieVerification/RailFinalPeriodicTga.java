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
@Table(name = "rail_final_periodic_tga")
public class RailFinalPeriodicTga extends BaseEntity {

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

    @Column(name = "date_of_last_test")
    private LocalDate dateOfLastTest;

    @Column(name = "qty_produced_since_last_test")
    private Integer qtyProducedSinceLastTest;

    @Column(name = "testing_threshold")
    private Integer testingThreshold;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;

    // Sample 1
    @Column(name = "s1_lot_no") private String s1LotNo;
    @Column(name = "s1_sample_no") private String s1SampleNo;
    @Column(name = "s1_sample_wt") private String s1SampleWt;
    @Column(name = "s1_temp_range") private String s1TempRange;
    @Column(name = "s1_polymer_content") private String s1PolymerContent;

    // Sample 2
    @Column(name = "s2_lot_no") private String s2LotNo;
    @Column(name = "s2_sample_no") private String s2SampleNo;
    @Column(name = "s2_sample_wt") private String s2SampleWt;
    @Column(name = "s2_temp_range") private String s2TempRange;
    @Column(name = "s2_polymer_content") private String s2PolymerContent;

    // Sample 3
    @Column(name = "s3_lot_no") private String s3LotNo;
    @Column(name = "s3_sample_no") private String s3SampleNo;
    @Column(name = "s3_sample_wt") private String s3SampleWt;
    @Column(name = "s3_temp_range") private String s3TempRange;
    @Column(name = "s3_polymer_content") private String s3PolymerContent;

    // Sample 4
    @Column(name = "s4_lot_no") private String s4LotNo;
    @Column(name = "s4_sample_no") private String s4SampleNo;
    @Column(name = "s4_sample_wt") private String s4SampleWt;
    @Column(name = "s4_temp_range") private String s4TempRange;
    @Column(name = "s4_polymer_content") private String s4PolymerContent;

    // Sample 5
    @Column(name = "s5_lot_no") private String s5LotNo;
    @Column(name = "s5_sample_no") private String s5SampleNo;
    @Column(name = "s5_sample_wt") private String s5SampleWt;
    @Column(name = "s5_temp_range") private String s5TempRange;
    @Column(name = "s5_polymer_content") private String s5PolymerContent;

    @Column(name = "tga_status")
    private String tgaStatus;

    @Column(name = "not_ok_count")
    private Integer notOkCount;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
