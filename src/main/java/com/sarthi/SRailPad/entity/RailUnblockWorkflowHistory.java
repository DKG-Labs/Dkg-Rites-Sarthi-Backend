package com.sarthi.SRailPad.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Rail_unblock_workflow_hitory")
@Data
public class RailUnblockWorkflowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "workflow_id")
    private Long workflowId;

    @Column(name = "plant_id")
    private String plantId;

    @Column(name = "vendor_code")
    private String vendorCode;

    @Column(name = "shift")
    private String shift;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "previous_action")
    private String previousAction;

    @Column(name = "previous_remarks", columnDefinition = "TEXT")
    private String previousRemarks;

    @Column(name = "unblocked_by")
    private Long unblockedBy;

    @Column(name = "unblocked_by_name")
    private String unblockedByName;

    @Column(name = "unblocked_by_role")
    private String unblockedByRole;

    @Column(name = "unblock_remarks", columnDefinition = "TEXT")
    private String unblockRemarks;

    @Column(name = "unblocked_on")
    private LocalDateTime unblockedOn;

    @PrePersist
    protected void onCreate() {
        if (this.unblockedOn == null) {
            this.unblockedOn = LocalDateTime.now();
        }
    }
}
