package com.sarthi.entity.processmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for Static Periodic Check in Process Material Inspection.
 * Stores equipment verification checks (Shearing Press, Forging Press, etc.)
 */
@Entity
@Table(name = "process_static_periodic_check", indexes = {
    @Index(name = "idx_proc_static_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_proc_static_po_no", columnList = "po_no")
})
@Data
public class ProcessStaticPeriodicCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "po_no", nullable = false, length = 50)
    private String poNo;

    @Column(name = "line_no", nullable = false, length = 20)
    private String lineNo;

    @Column(name = "shift", length = 10)
    private String shift;

    @Column(name = "lot_no", length = 50)
    private String lotNo;

    @Column(name = "date_of_inspection")
    private java.time.LocalDate dateOfInspection;

    // Equipment verification checks
    @Column(name = "shearing_press_check")
    private Boolean shearingPressCheck = false;

    @Column(name = "forging_press_check")
    private Boolean forgingPressCheck = false;

    @Column(name = "reheating_furnace_check")
    private Boolean reheatingFurnaceCheck = false;

    @Column(name = "quenching_time_check")
    private Boolean quenchingTimeCheck = false;

    @Column(name = "forging_die_check")
    private Boolean forgingDieCheck = false;

    @Column(name = "oil_tank_counter_value")
    private Integer oilTankCounterValue;

    @Column(name = "all_checks_passed")
    private Boolean allChecksPassed = false;

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

