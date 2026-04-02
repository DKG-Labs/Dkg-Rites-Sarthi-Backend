package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.PlantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantProfileRepository extends JpaRepository<PlantProfile, Long> {
/*
    @Query("SELECT DISTINCT p.numberOfSheds FROM PlantProfile p WHERE p.createdBy = :vendorCode AND p.numberOfSheds IS NOT NULL")
    List<Integer> findDistinctNumberOfShedsByVendorCode(String vendorCode);
    */

    @Query("""
SELECT DISTINCT p.numberOfSheds
FROM PlantProfile p
WHERE p.createdBy = :vendorId
AND p.plantId = :plantId
""")
    List<Integer> findDistinctShedsByVendorIdAndPlantId(
            @Param("vendorId") Long vendorId,
            @Param("plantId") String plantId);


    List<PlantProfile> findAllByVendorCode(String vendorCode);

    List<String> findLinesByVendorCodeAndPlantId(String vendorCode, String plantId);

    @Query("""
SELECT DISTINCT p.id,p.plantType, p.numberOfSheds
FROM PlantProfile p
WHERE p.createdBy = :vendorId
AND p.plantId = :plantId
""")
    List<Object[]> findPlantTypeAndSheds(Long vendorId, String plantId);
}
