package com.sarthi.entity.Mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="notification_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    private Long notificationId;

    private Integer attemptNo;

    private String status;

    @Lob
    private String errorMessage;

    private LocalDateTime attemptedAt;

}