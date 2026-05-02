package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.EtProjection;
import com.sarthi.Sleeper.entity.EpoxyTreatedSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpoxyTreatedSleeperRepository extends JpaRepository<EpoxyTreatedSleeper, Long> {

   /* @Query("""
SELECT 
    et.batchNumber,
    et.location,
    pd.castingDate,
    pd.totalCastedSleepers,
    COUNT(DISTINCT esd.sleeperId)
FROM EpoxyTreatedSleeper et
JOIN ProductionDeclaration pd 
    ON pd.batchNumber = et.batchNumber 
    AND pd.productionUnit = et.location   
JOIN EtSleeperDetails esd 
    ON esd.et.id = et.id
GROUP BY et.batchNumber, et.location, pd.castingDate, pd.totalCastedSleepers
""")
    List<Object[]> getBatchWiseEtSummary();  */

    @Query("""
SELECT 
    et.batchNumber,
    et.location,
    pd.castingDate,
    pd.totalCastedSleepers,
    COUNT(esd.id)
FROM EpoxyTreatedSleeper et
JOIN ProductionDeclaration pd 
    ON pd.batchNumber = et.batchNumber 
    AND pd.productionUnit = et.location   
JOIN EtSleeperDetails esd 
    ON esd.et.id = et.id
GROUP BY et.batchNumber, et.location, pd.castingDate, pd.totalCastedSleepers
""")
    List<Object[]> getBatchWiseEtSummary();

    @Query(value = """
    SELECT ets.created_date AS createdDate,
           COUNT(esd.id) AS sleeperCount
    FROM et_epoxy_treated_sleeper ets
    LEFT JOIN et_sleeper_details esd ON ets.id = esd.et_id
    WHERE ets.batch_number = :batchNo
      AND ets.id = (SELECT MAX(id) FROM et_epoxy_treated_sleeper WHERE batch_number = :batchNo)
    GROUP BY ets.created_date
""", nativeQuery = true)
    EtProjection getETData(String batchNo);
}
