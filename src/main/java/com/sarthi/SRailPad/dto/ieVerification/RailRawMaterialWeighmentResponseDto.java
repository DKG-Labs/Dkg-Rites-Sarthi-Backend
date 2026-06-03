package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RailRawMaterialWeighmentResponseDto {
    private Long id;
    private String plantId;
    private String vendorCode;
    private String shift;
    private LocalDate castingDate;
    private String railPadType;
    private String batchNo;
    private Double totalWeight;
    private String acceptedMaterials;
    private String contractSpecification;
    private Double rubberPercentage;
    private String status;
    private String timestamp;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;

    private List<MaterialItemDto> materials;

    @Data
    public static class MaterialItemDto {
        private Long id;
        private String name;
        private Double weight;
    }
}
