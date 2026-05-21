package com.sarthi.entity.processmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for Forging Section data in 8-Hour Grid.
 * Stores hourly forging production data and temperature readings.
 */
@Entity
@Table(name = "process_forging_data", indexes = {
    @Index(name = "idx_proc_forg_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_proc_forg_po_no", columnList = "po_no")
})
@Data
public class ProcessForgingData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "po_no", nullable = false, length = 50)
    private String poNo;

    @Column(name = "line_no", nullable = false, length = 20)
    private String lineNo;

    // Shift and hour info
    @Column(name = "shift", length = 50)
    private String shift;

    @Column(name = "hour_index")
    private Integer hourIndex;

    @Column(name = "hour_label", length = 30)
    private String hourLabel;

    @Column(name = "no_production")
    private Boolean noProduction = false;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    // Forging Temperature - 2 readings
    @Column(name = "forging_temp_1", precision = 10, scale = 2)
    private BigDecimal forgingTemp1;

    @Column(name = "forging_temp_2", precision = 10, scale = 2)
    private BigDecimal forgingTemp2;



//    @Column(name = "accepted_qty")
//    private Integer acceptedQty;
//
//    @Column(name = "rejected_qty")
//    private Integer rejectedQty;

    // Separate rejection fields for each measurement
//    @Column(name = "forging_temperature_rejected")
//    private Integer forgingTemperatureRejected;
//
//    @Column(name = "forging_stabilisation_rejected")
//    private Integer forgingStabilisationRejected;
    @Column(name = "forging_temp_rejected")
    private Integer forgingTempRejected;



    @Column(name = "forging_stabilisation_rejection_rejected")
    private Integer forgingStabilisationRejectionRejected;
    // Separate rejection fields for each measurement


    // Forging Stabilisation Rejection - 2 readings + rejected count
    @Column(name = "forging_stabilisation_rejection_1", length = 30)
    private String forgingStabilisationRejection1;

    @Column(name = "forging_stabilisation_rejection_2", length = 30)
    private String forgingStabilisationRejection2;



    // Improper Forging - 2 readings + rejected count
    @Column(name = "improper_forging_1", length = 30)
    private String improperForging1;

    @Column(name = "improper_forging_2", length = 30)
    private String improperForging2;



    @Column(name = "improper_forging_rejected")
    private Integer improperForgingRejected;

    // Forging Defect (Marks / Notches) - 2 readings + rejected count
    @Column(name = "forging_defect_1", length = 30)
    private String forgingDefect1;

    @Column(name = "forging_defect_2", length = 30)
    private String forgingDefect2;

    @Column(name = "forging_defect_rejected")
    private Integer forgingDefectRejected;

    // Embossing Defect - 2 readings + rejected count
    @Column(name = "embossing_defect_1", length = 30)
    private String embossingDefect1;

    @Column(name = "embossing_defect_2", length = 30)
    private String embossingDefect2;

    @Column(name = "embossing_defect_rejected")
    private Integer embossingDefectRejected;

    @Column(name = "remarks", length = 500)
    private String remarks;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

