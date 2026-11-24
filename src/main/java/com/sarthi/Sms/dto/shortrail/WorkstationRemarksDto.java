package com.sarthi.Sms.dto.shortrail;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkstationRemarksDto {
     private String shiftDtls;
    private String shiftRemarks;
    private LocalDateTime startTime;
}
