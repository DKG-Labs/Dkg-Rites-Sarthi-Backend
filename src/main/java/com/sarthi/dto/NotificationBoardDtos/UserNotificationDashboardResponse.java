package com.sarthi.dto.NotificationBoardDtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotificationDashboardResponse {

    private Long totalNotifications;

    private Long unreadNotifications;

    private Long popupNotifications;
}