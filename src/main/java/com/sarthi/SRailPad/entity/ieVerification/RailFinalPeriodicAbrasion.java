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
@Table(name = "rail_final_periodic_abrasion")
public class RailFinalPeriodicAbrasion extends BaseEntity {

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
    @Column(name = "s1_initial_mass") private String s1InitialMass;
    @Column(name = "s1_final_mass") private String s1FinalMass;
    @Column(name = "s1_loss_of_mass") private String s1LossOfMass;
    @Column(name = "s1_relative_loss") private String s1RelativeLoss;

    // Sample 2
    @Column(name = "s2_lot_no") private String s2LotNo;
    @Column(name = "s2_sample_no") private String s2SampleNo;
    @Column(name = "s2_initial_mass") private String s2InitialMass;
    @Column(name = "s2_final_mass") private String s2FinalMass;
    @Column(name = "s2_loss_of_mass") private String s2LossOfMass;
    @Column(name = "s2_relative_loss") private String s2RelativeLoss;

    // Sample 3
    @Column(name = "s3_lot_no") private String s3LotNo;
    @Column(name = "s3_sample_no") private String s3SampleNo;
    @Column(name = "s3_initial_mass") private String s3InitialMass;
    @Column(name = "s3_final_mass") private String s3FinalMass;
    @Column(name = "s3_loss_of_mass") private String s3LossOfMass;
    @Column(name = "s3_relative_loss") private String s3RelativeLoss;

    // Sample 4
    @Column(name = "s4_lot_no") private String s4LotNo;
    @Column(name = "s4_sample_no") private String s4SampleNo;
    @Column(name = "s4_initial_mass") private String s4InitialMass;
    @Column(name = "s4_final_mass") private String s4FinalMass;
    @Column(name = "s4_loss_of_mass") private String s4LossOfMass;
    @Column(name = "s4_relative_loss") private String s4RelativeLoss;

    // Sample 5
    @Column(name = "s5_lot_no") private String s5LotNo;
    @Column(name = "s5_sample_no") private String s5SampleNo;
    @Column(name = "s5_initial_mass") private String s5InitialMass;
    @Column(name = "s5_final_mass") private String s5FinalMass;
    @Column(name = "s5_loss_of_mass") private String s5LossOfMass;
    @Column(name = "s5_relative_loss") private String s5RelativeLoss;

    @Column(name = "abrasion_status")
    private String abrasionStatus;

    @Column(name = "not_ok_count")
    private Integer notOkCount;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
