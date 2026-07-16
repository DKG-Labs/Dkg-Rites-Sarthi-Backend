package com.sarthi.dto.NotificationBoardDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTill;

    private Boolean popupNotification;

    private String issuingAuthority;

    @NotEmpty
    private List<Long> roleIds;
}