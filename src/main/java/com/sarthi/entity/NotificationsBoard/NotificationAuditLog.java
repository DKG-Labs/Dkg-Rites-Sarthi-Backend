package com.sarthi.entity.NotificationsBoard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "action")
    private String action;

    @Column(name = "action_by")
    private String actionBy;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}