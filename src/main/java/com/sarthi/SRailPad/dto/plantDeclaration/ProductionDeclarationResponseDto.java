package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductionDeclarationResponseDto {
    private Long id;
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private LocalDate productionDate;
    private String productionLine;
    private String status;
    
    private List<ProductResponseDto> products;

    @Data
    public static class ProductResponseDto {
        private Long id;
        private String productType;
        private String measurementMode;
        private List<BatchResponseDto> batches;
    }

    @Data
    public static class BatchResponseDto {
        private Long id;
        private String batchNo;
        private String compABatch;
        private String compBBatch;
        private Double initialWt;
        private Double finalWt;
        private Integer quantity;
    }
}
