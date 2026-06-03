package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailHydraulicPressRequestDto {
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
    private Long updatedBy;
}
