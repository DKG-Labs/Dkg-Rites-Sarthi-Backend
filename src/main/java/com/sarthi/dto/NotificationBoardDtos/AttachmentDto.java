package com.sarthi.dto.NotificationBoardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {

    private Long id;

    private String fileName;

    private String blobUrl;

    private Long fileSize;
}