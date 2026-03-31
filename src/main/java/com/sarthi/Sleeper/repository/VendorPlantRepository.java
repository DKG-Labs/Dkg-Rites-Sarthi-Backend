package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.VendorPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorPlantRepository extends JpaRepository<VendorPlant, Long> {
    List<VendorPlant> findByVendorCode(String vendorCode);

    List<VendorPlant> findByVendorId(Long vendorCode);
}
