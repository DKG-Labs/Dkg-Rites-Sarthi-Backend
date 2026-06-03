package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RailHydraulicPressResponseDto {
    private Long id;
    private String plantId;
    private String vendorCode;
    private String shift;
    private LocalDate castingDate;
    private String railPadType;
    private String batchNo;
    private String timeOfCheck;
    private Double curingTime;
    private Double curingTemp;
    private Double curingPressure;
    private String status;
    private String timestamp;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
