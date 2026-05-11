package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailProductRecipeRepository extends JpaRepository<ProductRecipe, Long> {
    List<ProductRecipe> findAllByVendorCode(String vendorCode);
    List<ProductRecipe> findAllByPlantId(String plantId);
}
