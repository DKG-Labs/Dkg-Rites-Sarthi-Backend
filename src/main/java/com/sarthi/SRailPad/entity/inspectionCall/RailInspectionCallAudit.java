package com.sarthi.SRailPad.entity.inspectionCall;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rail_inspection_call_audit")
public class RailInspectionCallAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_no", nullable = false, length = 50)
    private String callNo;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
