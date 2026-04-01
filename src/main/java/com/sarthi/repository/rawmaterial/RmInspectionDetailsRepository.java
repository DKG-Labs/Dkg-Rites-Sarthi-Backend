package com.sarthi.repository.rawmaterial;

import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RmInspectionDetailsRepository
        extends JpaRepository<RmInspectionDetails, Long> {

    /* ==================== Find by Inspection Call ID ==================== */

    @Query("""
        SELECT rd
        FROM RmInspectionDetails rd
        WHERE rd.inspectionCall.id = :icId
    """)
    Optional<RmInspectionDetails> findByIcId(@Param("icId") Long icId);
    
    List<RmInspectionDetails> findByInspectionCallIdIn(List<Long> icIds);

    /* ==================== Find by TC Number ==================== */

    Optional<RmInspectionDetails> findByTcNumber(String tcNumber);

    /* ==================== Find by Manufacturer ==================== */

    List<RmInspectionDetails> findByManufacturerContainingIgnoreCase(String manufacturer);

    /* ==================== Find with Heat Quantities ==================== */

    @Query("""
        SELECT DISTINCT rd
        FROM RmInspectionDetails rd
        LEFT JOIN FETCH rd.heatQuantities
        WHERE rd.id = :id
    """)
    Optional<RmInspectionDetails> findByIdWithHeatQuantities(@Param("id") Long id);

    /* ==================== Find with Inspection Call ==================== */

    @Query("""
        SELECT rd
        FROM RmInspectionDetails rd
        JOIN FETCH rd.inspectionCall
        WHERE rd.id = :id
    """)
    Optional<RmInspectionDetails> findByIdWithInspectionCall(@Param("id") Long id);
}

