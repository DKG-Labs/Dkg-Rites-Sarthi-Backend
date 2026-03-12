package com.sarthi.repository;

import com.sarthi.entity.VendorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorMasterRepository extends JpaRepository<VendorMaster, Long> {

    Optional<VendorMaster> findByVendorCode(String vendorCode);

    List<VendorMaster> findByVendorCodeIn(List<String> vendorCodes);

}
