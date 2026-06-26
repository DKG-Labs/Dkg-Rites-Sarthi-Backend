package com.sarthi.dto;

import lombok.Data;

@Data
public class ImageCaptureDto {
    private String base64Data;
    private Double latitude;
    private Double longitude;
}
