package com.sarthi.SRailPad.dto.inspectionCall;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProcessInspectionSaveDto {
    private String callNo;
    private Integer callQty;
    private Integer totalManufacturedQty;
    private Integer totalRejectedQty;
    private Integer totalAcceptedQty;
    private String reasonForRejection;
    private String lotRangeFrom;
    private String lotRangeTo;
    private String remarks;
    private LocalDate inspectionStartDate;
    private LocalDate inspectionEndDate;
    private Boolean isFinish; // Determines if workflow should transition
    private String shift;
    private LocalDate inspectionDate;
    private Long createdBy;
    private Long updatedBy;
    private List<ProcessBatchSaveDto> batches;

    @Data
    public static class ProcessBatchSaveDto {
        private Long declarationBatchId;
        private String batchNo;
        private String drawingNo;
        private LocalDate productionDate;
        private Integer qtyManufactured;
        private Integer qtyRejected;
        private Integer qtyAccepted;
    }
}
