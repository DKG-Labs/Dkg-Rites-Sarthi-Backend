package com.sarthi.SRailPad.dto.inspectionCall;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProcessAvailableBatchDto {
    private LocalDate productionDate;
    private Integer totalBatchesCount;
    private List<ProcessBatchDetailDto> batches;

    @Data
    public static class ProcessBatchDetailDto {
        private Long declarationBatchId;
        private String batchNo;
        private Integer qtyManufactured;
        private LocalDate productionDate;
        private String drawingNo;
        private Integer verificationRejectedQty;
        private String verificationRejectedReason;
        private List<RejectionDetailDto> rejections;
    }

    @Data
    public static class RejectionDetailDto {
        private String drawingNo;
        private String reason;
        private Integer rejectedQty;
    }
}
