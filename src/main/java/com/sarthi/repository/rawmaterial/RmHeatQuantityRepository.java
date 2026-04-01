package com.sarthi.repository.rawmaterial;

import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RmHeatQuantity entity.
 * Provides CRUD operations and custom queries for heat-wise quantity data.
 * Matches actual database schema: rm_heat_quantities table.
 */
@Repository
public interface RmHeatQuantityRepository extends JpaRepository<RmHeatQuantity, Integer> {

    /* ==================== Find by RM Detail ID ==================== */

    /**
     * Find all heat quantities for a given RM inspection detail
     */
    @Query("SELECT hq FROM RmHeatQuantity hq WHERE hq.rmInspectionDetails.id = :rmDetailId")
    List<RmHeatQuantity> findByRmDetailId(@Param("rmDetailId") Integer rmDetailId);
    
    List<RmHeatQuantity> findByRmInspectionDetailsIdIn(List<Long> rmDetailIds);

    /* ==================== Find by Heat Number ==================== */

    Optional<RmHeatQuantity> findByHeatNumber(String heatNumber);

    List<RmHeatQuantity> findByHeatNumberContainingIgnoreCase(String heatNumber);

    /* ==================== Find by TC Number ==================== */

    List<RmHeatQuantity> findByTcNumber(String tcNumber);

    /* ==================== Find with Chemical Analyses ==================== */

    /**
     * Find heat quantity by ID
     * Note: Chemical analyses are linked via heat number, not direct relationship
     */
    @Query("SELECT hq FROM RmHeatQuantity hq WHERE hq.id = :id")
    Optional<RmHeatQuantity> findByIdWithChemicalAnalyses(@Param("id") Integer id);

    /**
     * Find all heat quantities for RM detail
     * Note: Chemical analyses are linked via heat number, not direct relationship
     */
    @Query("SELECT hq FROM RmHeatQuantity hq WHERE hq.rmInspectionDetails.id = :rmDetailId")
    List<RmHeatQuantity> findByRmDetailIdWithChemicalAnalyses(@Param("rmDetailId") Integer rmDetailId);
}

