package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SleeperInspectionCallDetailDto {
    private Long id;
    private String callNo;
    private String poNo;
    private String srNo;
    private String sleeperType;
    private Integer totalOffered;
    private Integer totalRejected;
    private LocalDate desiredInspectionDate;
    private String status;
    private Long createdBy;
    private String plantId;
    private LocalDateTime createdAt;
    private String uom;
    private List<SleeperInspectionCallBatchDto> batchesSelected;
}
