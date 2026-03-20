package com.sarthi.Sleeper.dto.HtsWire;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HtsWireDailyTestRequestDto {
    private Long requestId;
    private LocalDate testDate;
    private String consignmentNo;
    private String coilNo;
    private String inventoryId;
    private Double nominalWeight;
    private Double layLength;
    private Double strandDiameter;

    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;
    private Integer createdBy;
}
