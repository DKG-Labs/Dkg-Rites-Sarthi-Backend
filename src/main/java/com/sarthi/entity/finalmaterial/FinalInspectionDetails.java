package com.sarthi.entity.finalmaterial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sarthi.entity.rawmaterial.InspectionCall;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for Final Inspection Details
 * Stores summary information for Final inspection calls
 */
@Entity
@Table(name = "final_inspection_details")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class FinalInspectionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ---- RELATION TO INSPECTION CALL ----
    @OneToOne
    @JoinColumn(name = "ic_id", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    private InspectionCall inspectionCall;

    // ---- REFERENCE TO PARENT RM ICs (comma-separated if multiple) ----
    private Long rmIcId; // Primary RM IC id (first one)

    @Column(columnDefinition = "TEXT")
    private String rmIcNumber; // Comma-separated RM IC numbers

    // ---- REFERENCE TO PARENT PROCESS ICs (comma-separated if multiple) ----
    private Long processIcId; // Primary Process IC id (first one)

    @Column(columnDefinition = "TEXT")
    private String processIcNumber; // Comma-separated Process IC numbers

    // ---- PLACE OF INSPECTION (SAME AS RM IC & PROCESS IC) ----
    private Integer companyId;
    private String companyName;
    private Integer unitId;
    private String unitName;

    @Column(columnDefinition = "TEXT")
    private String unitAddress;

    // ---- SUMMARY INFORMATION ----
    private Integer totalLots;
    private Integer totalOfferedQty;
    private Integer totalAcceptedQty;
    private Integer totalRejectedQty;

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
