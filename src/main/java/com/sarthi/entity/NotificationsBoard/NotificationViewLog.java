package com.sarthi.entity.NotificationsBoard;

import com.sarthi.entity.UserMaster;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_view_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private NotificationMaster notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserMaster user;

    private LocalDateTime viewedAt;

    private Boolean popupClosed = false;

    private LocalDateTime popupClosedAt;

    private String createdBy;

    private LocalDateTime createdDate;

    private String updatedBy;

    private LocalDateTime updatedDate;
}