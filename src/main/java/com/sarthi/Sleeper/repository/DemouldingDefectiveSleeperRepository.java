package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.DemouldingDefectiveSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DemouldingDefectiveSleeperRepository extends JpaRepository<DemouldingDefectiveSleeper, Long> {
    @Query(value = """
SELECT d.sleeper_no
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection i 
  ON i.id = d.inspection_id
WHERE i.batch_no = :batchNo
""", nativeQuery = true)
    Set<String> findRejectedSleeperNos(@Param("batchNo") String batchNo);
}
