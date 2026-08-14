package com.sarthi.repository;

import com.sarthi.entity.VendorFinancialLiability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorFinancialLiabilityRepository extends JpaRepository<VendorFinancialLiability, Long> {
    List<VendorFinancialLiability> findByVendorCodeAndPaymentStatus(String vendorCode, String paymentStatus);
    Optional<VendorFinancialLiability> findByCallNumber(String callNumber);
    boolean existsByVendorCodeAndPaymentStatus(String vendorCode, String paymentStatus);
}
