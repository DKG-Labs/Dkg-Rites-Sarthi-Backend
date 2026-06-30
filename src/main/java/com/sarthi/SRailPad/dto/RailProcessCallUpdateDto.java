package com.sarthi.SRailPad.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailProcessCallUpdateDto {
    private String drawingNo;
    private Integer qtyDesiredForFinal;
    private LocalDate productionInitiationDate;
    private Long userId; // The ID of the user performing the edit
}
