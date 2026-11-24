package com.sarthi.Sms.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SurfaceInspectionResDto {
    private int id;
    private String annotatedImage;
    private String railId;
    private String shift;
    private Integer distance;
    private int cameraId;
    private Integer labelIndex;
    private String labelName;
    private BigDecimal confidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer aiStatus;
    private Integer adminStatus;
    private String cameraName;
}