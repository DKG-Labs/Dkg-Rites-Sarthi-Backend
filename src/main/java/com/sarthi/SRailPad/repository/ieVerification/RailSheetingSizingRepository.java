package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailSheetingSizing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailSheetingSizingRepository extends JpaRepository<RailSheetingSizing, Long> {

    List<RailSheetingSizing> findByPlantIdAndVendorCodeOrderByCreatedDateDesc(String plantId, String vendorCode);

    Optional<RailSheetingSizing> findByBatchNoAndPlantIdAndVendorCode(String batchNo, String plantId, String vendorCode);
}
