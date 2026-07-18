package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class IEProductionVerificationRequestDto {
    private LocalDate castingDate;
    private String shift;
    private String productionUnit;
    private Long requestId;
    private Integer totalPiecesProduced;
    private Integer totalPiecesRejected;
    private Integer totalAcceptedPieces;
    private Long createdBy;

    private List<ProductionInfoRequestDto> productionInfos;
    private List<ProductionRejectionRequestDto> rejections;

    @Data
    public static class ProductionInfoRequestDto {
        private String productType;
        private String drawingNo;
        private String batchNo;
        private Double initialWt;
        private Double finalWt;
        private Integer quantityProduced;
    }

    @Data
    public static class ProductionRejectionRequestDto {
        private String productType;
        private String drawingNo;
        private String batchNo;
        private Integer rejectedQty;
        private String reason;
    }
}
