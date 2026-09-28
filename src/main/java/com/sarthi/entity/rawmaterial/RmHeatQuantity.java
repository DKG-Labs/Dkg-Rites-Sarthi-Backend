package com.sarthi.entity.rawmaterial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rm_heat_quantities")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RmHeatQuantity {

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
    private String manufacturer;

    private BigDecimal offeredQty;

    private String tcNumber;
    private LocalDate tcDate;
    private BigDecimal tcQuantity;

    private BigDecimal qtyLeft;
    private BigDecimal qtyAccepted;
    private BigDecimal qtyRejected;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Color code manually entered by inspector
    @Column(name = "color_code", length = 50)
    private String colorCode;

    // Audit fields - matching database schema
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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


