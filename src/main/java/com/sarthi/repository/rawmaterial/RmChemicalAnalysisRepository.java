package com.sarthi.repository.rawmaterial;

import com.sarthi.entity.rawmaterial.RmChemicalAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RmChemicalAnalysis entity.
 * Provides CRUD operations and custom queries for chemical analysis data.
 */
@Repository
public interface RmChemicalAnalysisRepository extends JpaRepository<RmChemicalAnalysis, Long> {

    /* ==================== Find by Heat Number ==================== */

    Optional<RmChemicalAnalysis> findByHeatNumber(String heatNumber);

    List<RmChemicalAnalysis> findByHeatNumberOrderByCreatedAtDesc(String heatNumber);

    /* ==================== Find by RM Detail ID ==================== */

    List<RmChemicalAnalysis> findByRmInspectionDetailsIdOrderByCreatedAtDesc(Integer rmDetailId);

    List<RmChemicalAnalysis> findByRmInspectionDetailsId(Integer rmDetailId);

    /* ==================== Find by Inspection Call Number ==================== */

    /**
     * Find all chemical analyses for a given inspection call number
     * Joins through rm_inspection_details -> inspection_call
     */
    @Query("SELECT ca FROM RmChemicalAnalysis ca " +
           "JOIN ca.rmInspectionDetails rd " +
           "JOIN rd.inspectionCall ic " +
           "WHERE ic.icNumber = :callNo " +
           "ORDER BY ca.heatNumber, ca.createdAt DESC")
    List<RmChemicalAnalysis> findByInspectionCallNo(@Param("callNo") String callNo);

}

