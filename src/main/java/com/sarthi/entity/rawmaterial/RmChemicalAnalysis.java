package com.sarthi.entity.rawmaterial;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rm_chemical_analysis")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RmChemicalAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ---- RELATION ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rm_detail_id", nullable = false)
    @JsonIgnore  // Prevent circular reference during JSON serialization
    private RmInspectionDetails rmInspectionDetails;

    private String heatNumber;

    private BigDecimal carbon;
    private BigDecimal manganese;
    private BigDecimal silicon;
    private BigDecimal sulphur;
    private BigDecimal phosphorus;
    private BigDecimal chromium;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

