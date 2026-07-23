package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailVisualInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailVisualInspectionRepository extends JpaRepository<RailVisualInspection, Long> {
    List<RailVisualInspection> findByPlantIdAndVendorCodeOrderByTimestampDesc(String plantId, String vendorCode);
}
