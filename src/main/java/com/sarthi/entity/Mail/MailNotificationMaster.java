package com.sarthi.entity.Mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_notification_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailNotificationMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(nullable = false,length = 30)
    private String module;

    @Column(nullable = false,length = 50)
    private String eventType;

    @Column(nullable = false,length = 100)
    private String referenceNo;

    @Column(nullable = false,length = 200)
    private String recipientName;

    @Column(nullable = false,length = 250)
    private String recipientEmail;

    @Column(nullable = false,length = 300)
    private String subject;

    @Column(nullable = false,length = 100)
    private String templateName;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    @Column(length = 20)
    private String status;      //PENDING,SENT,FAILED,RETRY

    private Integer retryCount;

    @Lob
    private String lastError;

    private Integer createdBy;

    private LocalDateTime createdDate;

    private LocalDateTime sentDate;

    private LocalDateTime updatedDate;

}