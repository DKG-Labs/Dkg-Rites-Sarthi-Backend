package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.PlantProjection;
import com.sarthi.Sleeper.entity.VendorPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
    @Query(value = """
    SELECT plant_name AS plantName,
           plant_id AS plantId
    FROM vendor_plant
    WHERE vendor_id = :vendorCode
""", nativeQuery = true)
    List<PlantProjection> getPlants(String vendorCode);

    @Query("""
    SELECT DISTINCT v.plantId
    FROM VendorPlant v
    WHERE v.vendorId = :vendorCode
    ORDER BY v.plantId
    """)
    List<String> findPlantIdsByVendorCode(String vendorCode);
}
