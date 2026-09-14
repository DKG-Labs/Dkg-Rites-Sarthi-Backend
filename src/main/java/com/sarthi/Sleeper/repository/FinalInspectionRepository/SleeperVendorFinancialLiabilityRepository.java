package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperVendorFinancialLiability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperVendorFinancialLiabilityRepository extends JpaRepository<SleeperVendorFinancialLiability, Long> {

    Optional<SleeperVendorFinancialLiability> findByCallNumber(String callNumber);

    List<SleeperVendorFinancialLiability> findByVendorCode(String vendorCode);

    List<SleeperVendorFinancialLiability> findByVendorCodeAndPaymentStatus(String vendorCode, String paymentStatus);
}
