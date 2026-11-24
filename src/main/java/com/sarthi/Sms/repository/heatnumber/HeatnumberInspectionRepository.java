package com.sarthi.Sms.repository.heatnumber;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.heatnumber.HeatnumberInspectionEntity;
@Repository
public interface HeatnumberInspectionRepository extends JpaRepository<HeatnumberInspectionEntity, Integer> {
    
    /**
     * Find heat number by rail ID
     * @param railId The rail ID to search for
     * @return Optional containing the HeatnumberInspectionEntity if found
     */
    Optional<HeatnumberInspectionEntity> findByRailId(String railId);
    
    /**
     * Get only the heat number by rail ID
     * @param railId The rail ID to search for
     * @return The heat number as String, or null if not found
     */
    @Query("SELECT h.heatnumber FROM HeatnumberInspectionEntity h WHERE h.railId = :railId")
    String findHeatnumberByRailId(@Param("railId") String railId);
    
    /**
     * Check if rail ID exists in the table
     * @param railId The rail ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByRailId(String railId);
}
