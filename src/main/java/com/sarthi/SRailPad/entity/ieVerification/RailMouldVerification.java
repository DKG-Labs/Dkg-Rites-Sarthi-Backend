package com.sarthi.SRailPad.entity.ieVerification;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rail_mould_verification")
@Data
public class RailMouldVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_id", length = 100, nullable = false)
    private String plantId;

    @Column(name = "vendor_code", length = 100, nullable = false)
    private String vendorCode;

    @Column(name = "shift", length = 50)
    private String shift;

    @Column(name = "casting_date", length = 50)
    private String castingDate;

    @Column(name = "mould_number", length = 100, nullable = false)
    private String mouldNumber;

    @Column(name = "time_of_check", length = 50)
    private String timeOfCheck;

    @Column(name = "dimensional_accuracy", length = 50)
    private String dimensionalAccuracy;

    @Column(name = "dimensional_remarks", columnDefinition = "TEXT")
    private String dimensionalRemarks;

    @Column(name = "freedom_from_defects", length = 50)
    private String freedomFromDefects;

    @Column(name = "defects_remarks", columnDefinition = "TEXT")
    private String defectsRemarks;

    @Column(name = "visual_remarks", columnDefinition = "TEXT")
    private String visualRemarks;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
