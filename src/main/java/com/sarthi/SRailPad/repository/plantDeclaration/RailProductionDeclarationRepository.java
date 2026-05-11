package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RailProductionDeclarationRepository extends JpaRepository<RailProductionDeclaration, Long> {
    List<RailProductionDeclaration> findByPlantIdOrderByProductionDateDesc(String plantId);
}
