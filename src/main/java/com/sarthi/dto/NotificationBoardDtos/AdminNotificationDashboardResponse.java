package com.sarthi.dto.NotificationBoardDtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminNotificationDashboardResponse {

    private Long totalNotifications;

    private Long draftNotifications;

    private Long publishedNotifications;

    private Long archivedNotifications;

    private Long expiredNotifications;
}