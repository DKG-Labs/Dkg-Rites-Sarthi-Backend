package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.PlantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantProfileRepository extends JpaRepository<PlantProfile, Long> {

    @Query("SELECT DISTINCT p.numberOfSheds FROM PlantProfile p WHERE p.createdBy = :vendorCode AND p.numberOfSheds IS NOT NULL")
    List<Integer> findDistinctNumberOfShedsByVendorCode(String vendorCode);

    List<PlantProfile> findAllByVendorCode(String vendorCode);

    List<String> findLinesByVendorCodeAndPlantId(String vendorCode, String plantId);
}
