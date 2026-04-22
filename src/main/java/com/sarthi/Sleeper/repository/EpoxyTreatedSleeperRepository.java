package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.EpoxyTreatedSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    COUNT(DISTINCT esd.sleeperId)
FROM EpoxyTreatedSleeper et
JOIN ProductionDeclaration pd 
    ON pd.batchNumber = et.batchNumber 
    AND pd.productionUnit = et.location   
JOIN EtSleeperDetails esd 
    ON esd.et.id = et.id
GROUP BY et.batchNumber, et.location, pd.castingDate, pd.totalCastedSleepers
""")
    List<Object[]> getBatchWiseEtSummary();
}
