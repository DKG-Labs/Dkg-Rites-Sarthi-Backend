package com.sarthi.entity.NotificationsBoard;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notification_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMaster {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "notification_number",
                nullable = false,
                unique = true,
                length = 30)
        private String notificationNumber;

        @Column(name = "title",
                nullable = false,
                length = 500)
        private String title;

        @Lob
        @Column(name = "content",
                nullable = false)
        private String content;

        @Column(name = "effective_from",
                nullable = false)
        private LocalDateTime effectiveFrom;

        @Column(name = "effective_till")
        private LocalDateTime effectiveTill;

        @Column(name = "popup_notification")
        private Boolean popupNotification = false;

        @Enumerated(EnumType.STRING)
        @Column(name = "status",
                nullable = false)
        private NotificationStatus status;

        @Column(name = "issuing_authority")
        private String issuingAuthority;

        @Column(name = "is_deleted")
        private Boolean isDeleted = false;

        @OneToMany(mappedBy = "notification",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
        private List<NotificationRoleMapping> roleMappings =
                new ArrayList<>();

        @OneToMany(mappedBy = "notification",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
        private List<NotificationAttachment> attachments =
                new ArrayList<>();

        @Column(name = "created_by")
        private String createdBy;

        @Column(name = "created_date")
        private LocalDateTime createdDate;

        @Column(name = "updated_by")
        private String updatedBy;

        @Column(name = "updated_date")
        private LocalDateTime updatedDate;

}
