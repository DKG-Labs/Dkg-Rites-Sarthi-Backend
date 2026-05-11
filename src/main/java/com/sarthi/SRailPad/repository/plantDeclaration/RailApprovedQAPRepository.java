package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.ApprovedQAP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailApprovedQAPRepository extends JpaRepository<ApprovedQAP, Long> {
    List<ApprovedQAP> findAllByVendorCode(String vendorCode);
    List<ApprovedQAP> findAllByPlantId(String plantId);
}
