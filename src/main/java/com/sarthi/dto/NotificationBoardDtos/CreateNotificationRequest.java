package com.sarthi.dto.NotificationBoardDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateNotificationRequest {

    @NotBlank(message = "Title is mandatory")
    @Size(max = 500)
    private String title;

    @NotBlank(message = "Content is mandatory")
    private String content;

    @NotNull(message = "Effective From Date is mandatory")
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTill;

    private Boolean popupNotification;

    private String issuingAuthority;

    private String status;

    @NotEmpty(message = "At least one role must be selected")
    private List<Long> roleIds;
}