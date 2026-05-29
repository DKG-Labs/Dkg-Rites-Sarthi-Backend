package com.sarthi.entity.processmaterial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sarthi.entity.rawmaterial.InspectionCall;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "process_inspection_details")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProcessInspectionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ---- RELATION TO INSPECTION CALL ----
    // Changed from @OneToOne to @ManyToOne to allow multiple lots per IC
    @ManyToOne
    @JoinColumn(name = "ic_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private InspectionCall inspectionCall;

    // ---- REFERENCE TO PARENT RM IC ----
    private Long rmIcId;

    @Column(columnDefinition = "TEXT")
    private String rmIcNumber;

    // ---- LOT INFORMATION ----
    private String lotNumber;

    // ---- HEAT INFORMATION ----
    private String heatNumber;
    private String manufacturer;
    private String manufacturerHeat;

    // ---- QUANTITY INFORMATION ----
    private Integer offeredQty;
    private Integer totalAcceptedQtyRm;
    private Integer declaredLotSize;

    // ---- PRODUCTION INFORMATION ----
    private LocalDate tentativeStartDate;

    // ---- APPROVAL/REJECTION TRACKING ----
    private Integer qtyAccepted;
    private Integer qtyRejected;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // ---- PLACE OF INSPECTION (AUTO-FETCHED FROM RM IC) ----
    private Integer companyId;
    private String companyName;
    private Integer unitId;
    private String unitName;

    @Column(columnDefinition = "TEXT")
    private String unitAddress;

    // ---- TIMESTAMPS ----
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
