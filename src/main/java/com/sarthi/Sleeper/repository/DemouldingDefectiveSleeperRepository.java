package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.DemouldingDefectiveSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DemouldingDefectiveSleeperRepository extends JpaRepository<DemouldingDefectiveSleeper, Long> {
   /* @Query(value = """
SELECT d.sleeper_no
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection i 
  ON i.id = d.inspection_id
WHERE i.batch_no = :batchNo
""", nativeQuery = true)
    Set<String> findRejectedSleeperNos(@Param("batchNo") String batchNo);  */

    @Query(value = """
SELECT d.sleeper_no
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection i 
  ON i.id = d.inspection_id
WHERE i.batch_no = :batchNo
AND i.id = (
    SELECT MAX(id) FROM demoulding_inspection WHERE batch_no = :batchNo
)
AND (
    (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
    OR
    (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
)
""", nativeQuery = true)
    Set<String> findRejectedSleeperNos(@Param("batchNo") String batchNo);

    @Query(value = """
SELECT COUNT(d.id)
FROM demoulding_defective_sleepers d
WHERE (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
   OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
""", nativeQuery = true)
    Long countByWithReasons();

    @Query(value = "SELECT COUNT(id) FROM demoulding_defective_sleepers", nativeQuery = true)
    Long countBy();
}
