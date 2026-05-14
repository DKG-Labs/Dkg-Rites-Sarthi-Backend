package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductionDeclarationRequestDto {
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private LocalDate productionDate;
    private String productionLine;
    private String poNo;
    private Long createdBy;
    private Long updatedBy;
    
    private List<ProductDto> products;

    @Data
    public static class ProductDto {
        private String productType;
        private String measurementMode;
        private List<BatchDto> batches;
    }

    @Data
    public static class BatchDto {
        private String batchNo;
        private String compABatch;
        private String compBBatch;
        private Double initialWt;
        private Double finalWt;
        private Integer quantity;
    }
}
