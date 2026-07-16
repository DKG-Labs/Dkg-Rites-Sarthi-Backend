package com.sarthi.dto.NotificationBoardDtos;

import com.sarthi.entity.NotificationsBoard.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDetailResponse {

    private Long id;

    private String notificationNumber;

    private String title;

    private String content;

    private NotificationStatus status;

    private Boolean popupNotification;

    private String issuingAuthority;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTill;

    private LocalDateTime createdDate;

    private List<RoleDto> roles;

    private List<AttachmentDto> attachments;
}