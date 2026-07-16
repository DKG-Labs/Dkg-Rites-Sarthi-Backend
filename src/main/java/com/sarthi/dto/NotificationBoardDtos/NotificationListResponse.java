package com.sarthi.dto.NotificationBoardDtos;

import com.sarthi.entity.NotificationsBoard.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationListResponse {

    private Long id;

    private String notificationNumber;

    private String title;

    private NotificationStatus status;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTill;

    private Boolean popupNotification;

    private String issuingAuthority;

    private LocalDateTime createdDate;
}