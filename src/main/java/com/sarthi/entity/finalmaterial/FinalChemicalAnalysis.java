package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Entity for Final Inspection - Chemical Analysis
 * Stores chemical composition analysis data for final inspection
 */
@Entity
@Table(name = "final_chemical_analysis", indexes = {
    @Index(name = "idx_final_chem_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_chem_lot_no", columnList = "lot_no"),
    @Index(name = "idx_final_chem_heat_no", columnList = "heat_no")
})
@Data
public class FinalChemicalAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

    @Column(name = "heat_no", length = 100)
    private String heatNo;

    @Column(name = "sample_no")
    private Integer sampleNo;

    @Column(name = "carbon_percent", precision = 5, scale = 3)
    private BigDecimal carbonPercent;

    @Column(name = "silicon_percent", precision = 5, scale = 3)
    private BigDecimal siliconPercent;

    @Column(name = "manganese_percent", precision = 5, scale = 3)
    private BigDecimal manganesePercent;

    @Column(name = "sulphur_percent", precision = 5, scale = 3)
    private BigDecimal sulphurPercent;

    @Column(name = "phosphorus_percent", precision = 5, scale = 3)
    private BigDecimal phosphorusPercent;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Audit Fields
    @Column(name = "created_by", length = 100)
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

