package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SleeperInspectionCallSubmitDto {
    private String callNo;
    private String poNo;
    private String srNo;
    private String sleeperType;
    private Integer totalOffered;
    private Integer totalRejected;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate desiredInspectionDate;
    private Long createdBy;

    private String vendorCode;
    private String plantId;
    private List<SleeperInspectionCallBatchDto> batchesSelected;
}
