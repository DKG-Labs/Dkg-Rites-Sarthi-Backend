package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.VendorPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorPlantRepository extends JpaRepository<VendorPlant, Long> {
    List<VendorPlant> findByVendorCode(String vendorCode);


    @Query("""
    SELECT DISTINCT m.plantId 
    FROM SleeperPoiIeMapping m
    WHERE m.ieUserId = :userId
""")
    List<String> findPlantIdsByUserId(@Param("userId") Integer userId);

    List<VendorPlant> findByVendorId(Long vendorCode);
}
