package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IEProductionVerificationResponseDto {
    private Long id;
    private LocalDate castingDate;
    private String shift;
    private String productionUnit;
    private Long requestId;
    private Integer totalPiecesProduced;
    private Integer totalPiecesRejected;
    private Integer totalAcceptedPieces;
    private Long createdBy;
    private LocalDateTime createdDate;

    private List<ProductionInfoResponseDto> productionInfos;
    private List<ProductionRejectionResponseDto> rejections;

    @Data
    public static class ProductionInfoResponseDto {
        private Long id;
        private String productType;
        private String batchNo;
        private Double initialWt;
        private Double finalWt;
        private Integer quantityProduced;
    }

    @Data
    public static class ProductionRejectionResponseDto {
        private Long id;
        private String productType;
        private String batchNo;
        private Integer rejectedQty;
        private String reason;
    }
}
