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
@Table(name = "rail_final_periodic_durability")
public class RailFinalPeriodicDurability extends BaseEntity {

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
    @Column(name = "s1_initial_thickness") private String s1InitialThickness;
    @Column(name = "s1_final_thickness") private String s1FinalThickness;
    @Column(name = "s1_reduction_thickness") private String s1ReductionThickness;
    @Column(name = "s1_initial_load_comp") private String s1InitialLoadComp;
    @Column(name = "s1_final_load_comp") private String s1FinalLoadComp;
    @Column(name = "s1_change_ld") private String s1ChangeLd;

    // Sample 2
    @Column(name = "s2_lot_no") private String s2LotNo;
    @Column(name = "s2_initial_thickness") private String s2InitialThickness;
    @Column(name = "s2_final_thickness") private String s2FinalThickness;
    @Column(name = "s2_reduction_thickness") private String s2ReductionThickness;
    @Column(name = "s2_initial_load_comp") private String s2InitialLoadComp;
    @Column(name = "s2_final_load_comp") private String s2FinalLoadComp;
    @Column(name = "s2_change_ld") private String s2ChangeLd;

    // Sample 3
    @Column(name = "s3_lot_no") private String s3LotNo;
    @Column(name = "s3_initial_thickness") private String s3InitialThickness;
    @Column(name = "s3_final_thickness") private String s3FinalThickness;
    @Column(name = "s3_reduction_thickness") private String s3ReductionThickness;
    @Column(name = "s3_initial_load_comp") private String s3InitialLoadComp;
    @Column(name = "s3_final_load_comp") private String s3FinalLoadComp;
    @Column(name = "s3_change_ld") private String s3ChangeLd;

    // Sample 4
    @Column(name = "s4_lot_no") private String s4LotNo;
    @Column(name = "s4_initial_thickness") private String s4InitialThickness;
    @Column(name = "s4_final_thickness") private String s4FinalThickness;
    @Column(name = "s4_reduction_thickness") private String s4ReductionThickness;
    @Column(name = "s4_initial_load_comp") private String s4InitialLoadComp;
    @Column(name = "s4_final_load_comp") private String s4FinalLoadComp;
    @Column(name = "s4_change_ld") private String s4ChangeLd;

    // Sample 5
    @Column(name = "s5_lot_no") private String s5LotNo;
    @Column(name = "s5_initial_thickness") private String s5InitialThickness;
    @Column(name = "s5_final_thickness") private String s5FinalThickness;
    @Column(name = "s5_reduction_thickness") private String s5ReductionThickness;
    @Column(name = "s5_initial_load_comp") private String s5InitialLoadComp;
    @Column(name = "s5_final_load_comp") private String s5FinalLoadComp;
    @Column(name = "s5_change_ld") private String s5ChangeLd;

    @Column(name = "durability_status")
    private String durabilityStatus;

    @Column(name = "not_ok_count")
    private Integer notOkCount;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
