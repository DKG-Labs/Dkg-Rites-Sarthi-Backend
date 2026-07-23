package com.sarthi.SRailPad.entity.ieVerification;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rail_sheeting_sizing")
@Data
public class RailSheetingSizing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_id", length = 100, nullable = false)
    private String plantId;

    @Column(name = "vendor_code", length = 100, nullable = false)
    private String vendorCode;

    @Column(name = "batch_no", length = 100, nullable = false)
    private String batchNo;

    @Column(name = "sheeting", length = 50, nullable = false)
    private String sheeting;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

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
