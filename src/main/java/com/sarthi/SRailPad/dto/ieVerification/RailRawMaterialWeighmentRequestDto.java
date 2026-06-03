package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RailRawMaterialWeighmentRequestDto {
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
    private Long updatedBy;

    private List<MaterialItemDto> materials;

    @Data
    public static class MaterialItemDto {
        private String name;
        private Double weight;
    }
}
