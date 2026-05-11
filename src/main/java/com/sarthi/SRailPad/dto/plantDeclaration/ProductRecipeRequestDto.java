package com.sarthi.SRailPad.dto.plantDeclaration;

import lombok.Data;
import java.util.List;

@Data
public class ProductRecipeRequestDto {
    private String vendorName;
    private String vendorCode;
    private String plantId;
    private String shift;
    private String recipeIdentification;
    private String padType;
    private Double totalPercentage;
    private Double virginTotalPercentage;
    private Long createdBy;
    private Long updatedBy;
    private List<IngredientDto> ingredients;

    @Data
    public static class IngredientDto {
        private String rawMaterial;
        private Double percentage;
    }
}
