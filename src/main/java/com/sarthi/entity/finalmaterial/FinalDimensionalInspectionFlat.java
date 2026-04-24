package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Entity for Final Dimensional Inspection - FLAT STRUCTURE
 * 
 * Used for storing dimensional inspection data with flat fields:
 * - 1st Sampling: Go Gauge Fail, No-Go Fail, Flat Bearing Fail
 * - 2nd Sampling: Go Gauge Fail, No-Go Fail, Flat Bearing Fail
 * - Total Rejected
 * 
 * This is the OLD structure used by Final Visual Inspection page.
 * For parent-child structure, use FinalDimensionalInspection entity.
 */
@Entity
@Table(name = "final_dimensional_inspection_flat", indexes = {
    @Index(name = "idx_final_dim_flat_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_dim_flat_lot_no", columnList = "lot_no"),
    @Index(name = "idx_final_dim_flat_heat_no", columnList = "heat_no")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalDimensionalInspectionFlat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "lot_no", nullable = false, length = 100)
    private String lotNo;

    @Column(name = "heat_no", nullable = false, length = 100)
    private String heatNo;

    // 1st Sampling
    @Column(name = "first_sample_go_gauge_fail")
    private Integer firstSampleGoGaugeFail;

    @Column(name = "first_sample_no_go_fail")
    private Integer firstSampleNoGoFail;

    @Column(name = "first_sample_flat_bearing_fail")
    private Integer firstSampleFlatBearingFail;

    // Detailed 1st Sampling
    @Column(name = "first_sample_main_box_go")
    private Integer firstSampleMainBoxGo;

    @Column(name = "first_sample_main_box_no_go")
    private Integer firstSampleMainBoxNoGo;

    @Column(name = "first_sample_falling_go")
    private Integer firstSampleFallingGo;

    @Column(name = "first_sample_falling_no_go")
    private Integer firstSampleFallingNoGo;

    @Column(name = "first_sample_flat_bearing_go")
    private Integer firstSampleFlatBearingGo;

    @Column(name = "first_sample_flat_bearing_no_go")
    private Integer firstSampleFlatBearingNoGo;

    // 2nd Sampling
    @Column(name = "second_sample_go_gauge_fail")
    private Integer secondSampleGoGaugeFail;

    @Column(name = "second_sample_no_go_fail")
    private Integer secondSampleNoGoFail;

    @Column(name = "second_sample_flat_bearing_fail")
    private Integer secondSampleFlatBearingFail;

    // Detailed 2nd Sampling
    @Column(name = "second_sample_main_box_go")
    private Integer secondSampleMainBoxGo;

    @Column(name = "second_sample_main_box_no_go")
    private Integer secondSampleMainBoxNoGo;

    @Column(name = "second_sample_falling_go")
    private Integer secondSampleFallingGo;

    @Column(name = "second_sample_falling_no_go")
    private Integer secondSampleFallingNoGo;

    @Column(name = "second_sample_flat_bearing_go")
    private Integer secondSampleFlatBearingGo;

    @Column(name = "second_sample_flat_bearing_no_go")
    private Integer secondSampleFlatBearingNoGo;

    // Total and Status
    @Column(name = "total_rejected")
    private Integer totalRejected;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Audit Fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

