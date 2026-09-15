package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SleeperInspectionCallListDto {
    private Long id;
    private String callNo;
    private String poNo;
    private String srNo;
    private String callDate;
    private LocalDate desiredInspectionDate;
    private String sleeperType;
    private Integer qtyOffered;
    private Integer batches;
    private String status;
    private String plantId;
    private String uom;
}
