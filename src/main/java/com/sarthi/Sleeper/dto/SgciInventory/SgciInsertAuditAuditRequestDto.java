package com.sarthi.Sleeper.dto.SgciInventory;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SgciInsertAuditAuditRequestDto {
    private LocalDate testDate;
    private String consignmentNo;
    private String lotNo;
    private String supplier;
    private String type;
    private String ritesIc;
    
    private Integer checked;
    private Integer accepted;
    private Integer rejected;
    private Double rejectionPct;

    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;
    private Integer createdBy;

    private List<SgciInsertReadingDto> readings;
}
