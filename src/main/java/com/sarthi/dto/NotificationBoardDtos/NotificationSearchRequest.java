package com.sarthi.dto.NotificationBoardDtos;

import com.sarthi.entity.NotificationsBoard.NotificationStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificationSearchRequest {

    private String title;

    private NotificationStatus status;

    private Long roleId;

    private LocalDate fromDate;

    private LocalDate toDate;
}
