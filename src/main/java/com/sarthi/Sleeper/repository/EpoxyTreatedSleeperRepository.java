package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.EtProjection;
import com.sarthi.Sleeper.entity.EpoxyTreatedSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpoxyTreatedSleeperRepository extends JpaRepository<EpoxyTreatedSleeper, Long> {

    @Query("""
SELECT 
    et.batchNumber,
    et.location,
    pd.castingDate,
    pd.totalCastedSleepers,
    COUNT(esd.id),
    COALESCE(et.plantId, pd.plantId),
    COALESCE(et.vendorCode, pd.vendorCode),
    et.createdBy
FROM EpoxyTreatedSleeper et
JOIN ProductionDeclaration pd 
    ON pd.batchNumber = et.batchNumber 
    AND pd.productionUnit = et.location   
JOIN EtSleeperDetails esd 
    ON esd.et.id = et.id
WHERE (:plantId IS NULL OR :plantId = '' OR et.plantId = :plantId OR pd.plantId = :plantId OR et.plantId LIKE CONCAT('%', :plantId, '%') OR pd.plantId LIKE CONCAT('%', :plantId, '%'))
  AND (:vendorCode IS NULL OR :vendorCode = '' OR et.vendorCode = :vendorCode OR pd.vendorCode = :vendorCode OR et.vendorCode LIKE CONCAT('%', :vendorCode, '%') OR pd.vendorCode LIKE CONCAT('%', :vendorCode, '%'))
  AND (:createdBy IS NULL OR et.createdBy = :createdBy)
GROUP BY et.batchNumber, et.location, pd.castingDate, pd.totalCastedSleepers, et.plantId, pd.plantId, et.vendorCode, pd.vendorCode, et.createdBy
""")
    List<Object[]> getBatchWiseEtSummary(
        @Param("plantId") String plantId, 
        @Param("vendorCode") String vendorCode,
        @Param("createdBy") Long createdBy
    );

    @Query("""
SELECT et FROM EpoxyTreatedSleeper et
WHERE (:plantId IS NULL OR :plantId = '' OR et.plantId = :plantId OR et.plantId LIKE CONCAT('%', :plantId, '%'))
  AND (:vendorCode IS NULL OR :vendorCode = '' OR et.vendorCode = :vendorCode OR et.vendorCode LIKE CONCAT('%', :vendorCode, '%'))
  AND (:createdBy IS NULL OR et.createdBy = :createdBy)
ORDER BY et.id DESC
""")
    List<EpoxyTreatedSleeper> findAllByPlantAndVendor(
        @Param("plantId") String plantId, 
        @Param("vendorCode") String vendorCode,
        @Param("createdBy") Long createdBy
    );

    @Query(value = """
    SELECT ets.created_date AS createdDate,
           COUNT(esd.id) AS sleeperCount
    FROM et_epoxy_treated_sleeper ets
    LEFT JOIN et_sleeper_details esd ON ets.id = esd.et_id
    WHERE ets.batch_number = :batchNo
      AND ets.id = (SELECT MAX(id) FROM et_epoxy_treated_sleeper WHERE batch_number = :batchNo)
    GROUP BY ets.created_date
""", nativeQuery = true)
    EtProjection getETData(@Param("batchNo") String batchNo);
}
