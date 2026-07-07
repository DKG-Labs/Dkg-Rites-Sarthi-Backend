package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_PROFILE_AUDIT_LOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUDIT_ID")
    private Long auditId;

    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    @Column(name = "ACTION", nullable = false, length = 100)
    private String action; // e.g., "UPDATE_PROFILE", "CHANGE_PASSWORD", "UPDATE_SECURITY_SETTINGS", "LOGOUT"

    @Column(name = "MODIFIED_FIELDS", length = 500)
    private String modifiedFields;

    @Column(name = "OLD_VALUES", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "NEW_VALUES", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "TIMESTAMP", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

    @Column(name = "REMARKS", length = 500)
    private String remarks;
}
