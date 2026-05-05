package com.sarthi.entity.processmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for Testing & Finishing Section data in 8-Hour Grid.
 * Stores hourly toe load, weight, paint identification and ERC coating test
 * data.
 */
@Entity
@Table(name = "process_testing_finishing_data", indexes = {
        @Index(name = "idx_proc_test_call_no", columnList = "inspection_call_no"),
        @Index(name = "idx_proc_test_po_no", columnList = "po_no")
})
@Data
public class ProcessTestingFinishingData {

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
    @Column(name = "shift", length = 5)
    private String shift;

    @Column(name = "hour_index")
    private Integer hourIndex;

    @Column(name = "hour_label", length = 30)
    private String hourLabel;

    @Column(name = "no_production")
    private Boolean noProduction = false;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    // Toe Load - 2 readings
    @Column(name = "toe_load_1", precision = 10, scale = 2)
    private BigDecimal toeLoad1;

    @Column(name = "toe_load_2", precision = 10, scale = 2)
    private BigDecimal toeLoad2;

    // Weight - 2 readings
    @Column(name = "weight_1", precision = 10, scale = 2)
    private BigDecimal weight1;

    @Column(name = "weight_2", precision = 10, scale = 2)
    private BigDecimal weight2;

    // Paint Identification - 2 readings
    @Column(name = "paint_identification_1", length = 50)
    private String paintIdentification1;

    @Column(name = "paint_identification_2", length = 50)
    private String paintIdentification2;

    // ERC Coating - 2 readings
    @Column(name = "erc_coating_1", length = 50)
    private String ercCoating1;

    @Column(name = "erc_coating_2", length = 50)
    private String ercCoating2;

    @Column(name = "accepted_qty")
    private Integer acceptedQty;

    @Column(name = "rejected_qty")
    private Integer rejectedQty;

    // Separate rejection fields for each measurement
    @Column(name = "toe_load_rejected")
    private Integer toeLoadRejected;

    @Column(name = "weight_rejected")
    private Integer weightRejected;

    @Column(name = "paint_identification_rejected")
    private Integer paintIdentificationRejected;

    @Column(name = "erc_coating_rejected")
    private Integer ercCoatingRejected;

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
