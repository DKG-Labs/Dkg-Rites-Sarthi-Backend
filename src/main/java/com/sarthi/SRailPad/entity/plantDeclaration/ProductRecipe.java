package com.sarthi.SRailPad.entity.plantDeclaration;

import com.sarthi.SRailPad.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "RailProductRecipe")
@Table(name = "rail_product_recipe")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductRecipe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "recipe_identification")
    private String recipeIdentification;

    @Column(name = "pad_type")
    private String padType;

    @Column(name = "total_percentage")
    private Double totalPercentage;

    @Column(name = "virgin_total_percentage")
    private Double virginTotalPercentage;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }
}
