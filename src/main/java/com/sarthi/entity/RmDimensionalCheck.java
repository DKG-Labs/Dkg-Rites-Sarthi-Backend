package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing dimensional check samples for Raw Material.
 * Stores all 20 diameter samples per heat in a single row.
 */
@Entity
@Table(name = "rm_dimensional_check", indexes = {
    @Index(name = "idx_rm_dim_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_rm_dim_heat_no", columnList = "heat_no")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_rm_dim_call_heat", columnNames = {"inspection_call_no", "heat_no"})
})
@Data
public class RmDimensionalCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "heat_no", nullable = false, length = 50)
    private String heatNo;

    @Column(name = "heat_index")
    private Integer heatIndex;

    // 20 Sample Diameter Columns
    @Column(name = "sample_1_diameter", precision = 8, scale = 4)
    private BigDecimal sample1Diameter;

    @Column(name = "sample_2_diameter", precision = 8, scale = 4)
    private BigDecimal sample2Diameter;

    @Column(name = "sample_3_diameter", precision = 8, scale = 4)
    private BigDecimal sample3Diameter;

    @Column(name = "sample_4_diameter", precision = 8, scale = 4)
    private BigDecimal sample4Diameter;

    @Column(name = "sample_5_diameter", precision = 8, scale = 4)
    private BigDecimal sample5Diameter;

    @Column(name = "sample_6_diameter", precision = 8, scale = 4)
    private BigDecimal sample6Diameter;

    @Column(name = "sample_7_diameter", precision = 8, scale = 4)
    private BigDecimal sample7Diameter;

    @Column(name = "sample_8_diameter", precision = 8, scale = 4)
    private BigDecimal sample8Diameter;

    @Column(name = "sample_9_diameter", precision = 8, scale = 4)
    private BigDecimal sample9Diameter;

    @Column(name = "sample_10_diameter", precision = 8, scale = 4)
    private BigDecimal sample10Diameter;

    @Column(name = "sample_11_diameter", precision = 8, scale = 4)
    private BigDecimal sample11Diameter;

    @Column(name = "sample_12_diameter", precision = 8, scale = 4)
    private BigDecimal sample12Diameter;

    @Column(name = "sample_13_diameter", precision = 8, scale = 4)
    private BigDecimal sample13Diameter;

    @Column(name = "sample_14_diameter", precision = 8, scale = 4)
    private BigDecimal sample14Diameter;

    @Column(name = "sample_15_diameter", precision = 8, scale = 4)
    private BigDecimal sample15Diameter;

    @Column(name = "sample_16_diameter", precision = 8, scale = 4)
    private BigDecimal sample16Diameter;

    @Column(name = "sample_17_diameter", precision = 8, scale = 4)
    private BigDecimal sample17Diameter;

    @Column(name = "sample_18_diameter", precision = 8, scale = 4)
    private BigDecimal sample18Diameter;

    @Column(name = "sample_19_diameter", precision = 8, scale = 4)
    private BigDecimal sample19Diameter;

    @Column(name = "sample_20_diameter", precision = 8, scale = 4)
    private BigDecimal sample20Diameter;

    // Audit Fields
    @Column(name = "defect_count")
    private Integer defectCount;

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

