package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RailProductionProductRepository extends JpaRepository<RailProductionProduct, Long> {
}
