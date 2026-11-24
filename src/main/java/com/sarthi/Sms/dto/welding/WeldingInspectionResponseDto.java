package com.sarthi.Sms.dto.welding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeldingInspectionResponseDto {
    private Integer inspectionMasterId;
    private LocalDate date;
    private String shift;
    private String panelId;
    private String railId1;
    private String railId2;
    private String panelDecision; // New field
}