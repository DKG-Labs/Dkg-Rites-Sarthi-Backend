package com.sarthi.udmcomplaint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Current state of a UDM consignee rejection inside SARTHI (one row per udm_rejection). */
@Entity
@Table(name = "udm_complaint")
@Data
public class UdmComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "udm_rejection_id", nullable = false, unique = true)
    private Long udmRejectionId;

    @Column(name = "rejection_number", nullable = false, length = 100)
    private String rejectionNumber;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "pending_with_role_id")
    private Integer pendingWithRoleId;

    @Column(name = "pending_with_role_name", length = 100)
    private String pendingWithRoleName;

    @Column(name = "assigned_rio", length = 50)
    private String assignedRio;

    @Column(name = "assigned_cm_user_id")
    private Integer assignedCmUserId;

    @Column(name = "assigned_cm_name", length = 150)
    private String assignedCmName;

    @Column(name = "last_remarks", length = 500)
    private String lastRemarks;

    @Column(name = "closed_by")
    private Integer closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Optimistic lock: two users acting on the same complaint cannot both succeed. */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (version == null) version = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
