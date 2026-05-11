package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.ApprovedAshSG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailApprovedAshSGRepository extends JpaRepository<ApprovedAshSG, Long> {
    List<ApprovedAshSG> findAllByVendorCode(String vendorCode);
    List<ApprovedAshSG> findAllByPlantId(String plantId);
}
