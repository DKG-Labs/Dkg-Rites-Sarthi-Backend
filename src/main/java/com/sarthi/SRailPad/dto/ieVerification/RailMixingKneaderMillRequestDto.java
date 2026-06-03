package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailMixingKneaderMillRequestDto {
    private String plantId;
    private String vendorCode;
    private String shift;
    private LocalDate castingDate;
    private String railPadType;
    private String batchNo;
    private Double mixingTime;
    private Double mixingTemp;
    private String waterCirculation;
    private String dustCollector;
    private String status;
    private String timestamp;
    private Long createdBy;
    private Long updatedBy;
}
