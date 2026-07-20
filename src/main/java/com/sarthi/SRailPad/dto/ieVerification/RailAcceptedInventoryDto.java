package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RailAcceptedInventoryDto {
    private LocalDate castingDate;
    private List<BatchAcceptedDto> batches;

    @Data
    public static class BatchAcceptedDto {
        private Long infoId;
        private String batchNo;
        private String productType;
        private String drawingNo;
        private Integer acceptedQty;
    }
}
