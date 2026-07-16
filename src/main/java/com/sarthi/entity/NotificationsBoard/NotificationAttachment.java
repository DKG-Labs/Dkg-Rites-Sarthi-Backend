package com.sarthi.entity.NotificationsBoard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private NotificationMaster notification;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "blob_url", length = 1000)
    private String blobUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
