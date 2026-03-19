package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.util.List;

@Data
public class SleeperInspectionCallSubmitDto {
    private String poNo;
    private String srNo;
    private String sleeperType;
    private Integer totalOffered;
    private Integer totalRejected;
    private Long createdBy;
    private List<SleeperInspectionCallBatchDto> batchesSelected;
}
