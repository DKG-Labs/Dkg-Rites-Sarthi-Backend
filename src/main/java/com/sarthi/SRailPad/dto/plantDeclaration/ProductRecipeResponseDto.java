package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductRecipeResponseDto {
    private Long id;
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private String recipeIdentification;
    private String padType;
    private Double totalPercentage;
    private Double virginTotalPercentage;
    private String status;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
    private List<IngredientDto> ingredients;

    @Data
    public static class IngredientDto {
        private Long id;
        private String rawMaterial;
        private Double percentage;
    }
}
