package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.DemouldingDefectiveSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DemouldingDefectiveSleeperRepository extends JpaRepository<DemouldingDefectiveSleeper, Long> {
    @Query("""
    SELECT d.sleeperNo
    FROM DemouldingDefectiveSleeper d
    WHERE d.inspection.batchNo = :batchNo
""")
    Set<String> findRejectedSleeperNos(@Param("batchNo") String batchNo);
}
