package com.sarthi.repository;

import com.sarthi.entity.VendorCalibrationHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorCalibrationHeaderRepository extends JpaRepository<VendorCalibrationHeader, Long> {

    List<VendorCalibrationHeader> findByVendorCode(String vendorCode);

    List<VendorCalibrationHeader> findByCreatedBy(String createdBy);

    Optional<VendorCalibrationHeader> findByVendorCodeAndCategory(String vendorCode, String category);

    boolean existsByVendorCodeAndCategory(String vendorCode, String category);
}
