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
SELECT DISTINCT d.sleeper_no
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection i 
  ON i.id = d.inspection_id
WHERE REPLACE(UPPER(i.batch_no), ' ', '') = REPLACE(UPPER(:batchNo), ' ', '')
AND d.sleeper_no IS NOT NULL
AND TRIM(d.sleeper_no) <> ''
""", nativeQuery = true)
    Set<String> findAllRejectedSleeperNosByBatchNo(@Param("batchNo") String batchNo);

    @Query(value = """
SELECT COUNT(d.id)
FROM demoulding_defective_sleepers d
WHERE (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
   OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
""", nativeQuery = true)
    Long countByWithReasons();

    @Query(value = """
SELECT COUNT(d.id)
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection di ON di.id = d.inspection_id
WHERE di.plant_id = :plantId
  AND (
    (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
    OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
  )
""", nativeQuery = true)
    Long countByWithReasonsAndPlantId(@Param("plantId") String plantId);

    @Query(value = """
SELECT COUNT(d.id)
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection di ON di.id = d.inspection_id
WHERE di.plant_id IN :plantIds
  AND (
    (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
    OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
  )
""", nativeQuery = true)
    Long countByWithReasonsAndPlantIds(@Param("plantIds") java.util.Collection<String> plantIds);

    @Query(value = "SELECT COUNT(id) FROM demoulding_defective_sleepers", nativeQuery = true)
    Long countBy();
}
