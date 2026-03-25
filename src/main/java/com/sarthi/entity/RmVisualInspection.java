package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing visual inspection defects for Raw Material.
 * Stores all defect selections in a single row per heat.
 */
@Entity
@Table(name = "rm_visual_inspection", indexes = {
    @Index(name = "idx_rm_visual_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_rm_visual_heat_no", columnList = "heat_no")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_rm_visual_call_heat", columnNames = {"inspection_call_no", "heat_no"})
})
@Data
public class RmVisualInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "heat_no", nullable = false, length = 50)
    private String heatNo;

    @Column(name = "heat_index")
    private Integer heatIndex;

    // Defect Boolean Columns
    @Column(name = "no_defect")
    private Boolean noDefect = false;

    @Column(name = "distortion")
    private Boolean distortion = false;

    @Column(name = "twist")
    private Boolean twist = false;

    @Column(name = "kink")
    private Boolean kink = false;

    @Column(name = "not_straight")
    private Boolean notStraight = false;

    @Column(name = "fold")
    private Boolean fold = false;

    @Column(name = "lap")
    private Boolean lap = false;

    @Column(name = "crack")
    private Boolean crack = false;

    @Column(name = "pit")
    private Boolean pit = false;

    @Column(name = "groove")
    private Boolean groove = false;

    @Column(name = "excessive_scaling")
    private Boolean excessiveScaling = false;

    @Column(name = "internal_defect")
    private Boolean internalDefect = false;

    @Column(name = "other")
    private Boolean other = false;

    // Defect Length Columns
    @Column(name = "distortion_length", precision = 10, scale = 2)
    private BigDecimal distortionLength;

    @Column(name = "twist_length", precision = 10, scale = 2)
    private BigDecimal twistLength;

    @Column(name = "kink_length", precision = 10, scale = 2)
    private BigDecimal kinkLength;

    @Column(name = "not_straight_length", precision = 10, scale = 2)
    private BigDecimal notStraightLength;

    @Column(name = "fold_length", precision = 10, scale = 2)
    private BigDecimal foldLength;

    @Column(name = "lap_length", precision = 10, scale = 2)
    private BigDecimal lapLength;

    @Column(name = "crack_length", precision = 10, scale = 2)
    private BigDecimal crackLength;

    @Column(name = "pit_length", precision = 10, scale = 2)
    private BigDecimal pitLength;

    @Column(name = "groove_length", precision = 10, scale = 2)
    private BigDecimal grooveLength;

    @Column(name = "excessive_scaling_length", precision = 10, scale = 2)
    private BigDecimal excessiveScalingLength;

    @Column(name = "internal_defect_length", precision = 10, scale = 2)
    private BigDecimal internalDefectLength;

    @Column(name = "other_length", precision = 10, scale = 2)
    private BigDecimal otherLength;

    @Column(name = "other_remarks", length = 500)
    private String otherRemarks;

    // Derived calculated fields stored explicitly
    @Column(name = "defect_count")
    private Integer defectCount;

    @Column(name = "weight_rejected", precision = 10, scale = 6)
    private BigDecimal weightRejected;

    @Column(name = "passed_at")
    private LocalDateTime passedAt;

    // Audit Fields
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

