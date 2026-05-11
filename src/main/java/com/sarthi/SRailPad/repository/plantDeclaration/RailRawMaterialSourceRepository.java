package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.RawMaterialSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailRawMaterialSourceRepository extends JpaRepository<RawMaterialSource, Long> {
    List<RawMaterialSource> findAllByVendorCode(String vendorCode);
    List<RawMaterialSource> findAllByPlantId(String plantId);
}
