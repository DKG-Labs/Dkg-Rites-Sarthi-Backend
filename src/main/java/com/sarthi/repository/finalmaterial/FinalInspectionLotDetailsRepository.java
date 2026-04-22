package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Final Inspection Lot Details
 */
@Repository
public interface FinalInspectionLotDetailsRepository extends JpaRepository<FinalInspectionLotDetails, Long> {

    /**
     * Find all lot details by Final Detail ID
     */
    List<FinalInspectionLotDetails> findByFinalDetailId(Long finalDetailId);

    List<FinalInspectionLotDetails> findByFinalDetailIdIn(List<Long> finalDetailIds);

    /**
     * Find lot details by lot number
     */
    List<FinalInspectionLotDetails> findByLotNumber(String lotNumber);

    /**
     * Find lot details by heat number
     */
    List<FinalInspectionLotDetails> findByHeatNumber(String heatNumber);

    /**
     * Find lot details by final detail id and lot number
     */
    Optional<FinalInspectionLotDetails> findByFinalDetailIdAndLotNumber(Long finalDetailId, String lotNumber);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(f.offeredQty) FROM FinalInspectionLotDetails f WHERE f.heatNumber = :heatNumber AND f.lotNumber = :lotNumber")
    Integer sumOfferedQtyByHeatNumberAndLotNumber(@org.springframework.data.repository.query.Param("heatNumber") String heatNumber, @org.springframework.data.repository.query.Param("lotNumber") String lotNumber);
}

