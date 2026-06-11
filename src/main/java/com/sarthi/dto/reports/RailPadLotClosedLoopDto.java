package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadLotClosedLoopDto {
    private String rlyPoSrNo;
    private Integer lotSize;
    private LocalDate dateOfInspection;
    private Integer acceptedQty;
    private Integer rejectedQty;
    private String overallStatus;
    private List<BatchDto> batches;
    private List<StageDto> stages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchDto {
        private String batchNo;
        private LocalDate productionDate;
        private Integer quantity;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StageDto {
        private String stageName;
        private LocalDate date;
        private Integer quantity;
        private String remarks;
    }
}
