package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailRheometerTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailRheometerTestRepository extends JpaRepository<RailRheometerTest, Long> {

    List<RailRheometerTest> findByPlantIdAndVendorCodeOrderByCreatedDateDesc(String plantId, String vendorCode);

    Optional<RailRheometerTest> findByBatchNoAndPlantIdAndVendorCode(String batchNo, String plantId, String vendorCode);
}
