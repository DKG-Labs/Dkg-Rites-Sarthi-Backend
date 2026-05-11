package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.PlantSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantSetupRepository extends JpaRepository<PlantSetup, Long> {
    List<PlantSetup> findAllByVendorCode(String vendorCode);
    List<PlantSetup> findAllByPlantId(String plantId);
}
