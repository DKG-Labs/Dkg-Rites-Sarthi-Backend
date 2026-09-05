package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InspectionTestResultRepository extends JpaRepository<InspectionTestResult, Long> {
   /* @Query("""
        SELECT COUNT(DISTINCT r.sleeperId)
        FROM InspectionTestResult r
        JOIN r.testHeader h
        WHERE h.batchId = :batchId
    """)
    Long countTestedSleepers(@Param("batchId") Long batchId);*/

  @Query("""
          SELECT r
          FROM InspectionTestResult r
          JOIN r.testHeader h
          WHERE h.batchId = :batchId
          """)
  List<InspectionTestResult> findByBatchId(Long batchId);

  /*  @Query("""
SELECT COUNT(DISTINCT r.sleeperId)
FROM InspectionTestResult r
JOIN r.testHeader h
WHERE h.batchId = :batchId
AND h.module.id = :moduleId
""")
    Long countTestedSleepers(Long batchId, Long moduleId);
*/
/*  @Query("""
          SELECT COUNT(DISTINCT r.sleeperId)
          FROM InspectionTestResult r
          JOIN r.testHeader h
          WHERE h.batchId = :batchId
          AND h.module.id = :moduleId
          AND r.active = true            
          AND r.result <> 'PENDING'      
          """)
  Long countTestedSleepers(Long batchId, Long moduleId);*/

    @Query("""
       SELECT COUNT(DISTINCT r.sleeperId)

       FROM InspectionTestResult r
       JOIN r.testHeader h

       WHERE h.batchId = :batchId
       AND h.module.id = :moduleId
       AND h.sleeperType = :sleeperType
       AND r.active = true
       AND r.result <> 'PENDING'
       """)
    Long countTestedSleepers(
            Long batchId,
            Long moduleId,
            String sleeperType
    );

    @Query("""
       SELECT h.batchId, COUNT(DISTINCT r.sleeperId)
       FROM InspectionTestResult r
       JOIN r.testHeader h
       WHERE h.batchId IN :batchIds
       AND h.module.id = :moduleId
       AND r.active = true
       AND r.result <> 'PENDING'
       GROUP BY h.batchId
       """)
    List<Object[]> countTestedSleepersByBatchIds(
            @Param("batchIds") List<Long> batchIds,
            @Param("moduleId") Long moduleId
    );

  @Query("""
          SELECT r
          FROM InspectionTestResult r
          JOIN r.testHeader h
          WHERE h.batchId = :batchId
          AND h.module.id = 3
          AND h.status='Completed'
          """)
  List<InspectionTestResult> findFinalModuleResults(Long batchId);

  @Query("""
          SELECT r
          FROM InspectionTestResult r
          JOIN r.testHeader h
          WHERE h.batchId = :batchId
          AND LOWER(h.status) = 'completed'
          """)
  List<InspectionTestResult> findAllResultsByBatchId(@Param("batchId") Long batchId);


  @Query("""
          SELECT COUNT(r) > 0
          FROM InspectionTestResult r
          WHERE r.testHeader.batchId = :batchId
          AND r.testHeader.module.id = :moduleId
          AND r.sleeperId = :sleeperId
          """)
  boolean existsByBatchIdAndModuleIdAndSleeperId(
          Long batchId, Long moduleId, Long sleeperId);

  List<InspectionTestResult> findByTestHeader_BatchIdAndModuleId(Long batchId, Long moduleId);

  List<InspectionTestResult> findByTestHeader_BatchIdAndModuleIdAndActiveTrue(Long batchId, Long moduleId);
 List<InspectionTestResult> findByTestHeader_BatchIdAndModuleIdInAndActiveTrue(
         Long batchId, List<Long> moduleIds);
  List<InspectionTestResult> findByTestHeader_BatchIdAndResultAndActiveTrue(Long batchId, String rejected);

  List<InspectionTestResult> findByTestHeader_BatchIdAndActiveTrue(Long batchId);

  // List<InspectionTestResult> findByBatchIdAndModuleIdAndSleeperIdAndActiveTrue(Long batchId, Long moduleId, Long sleeperId);

  List<InspectionTestResult> findByTestHeader_BatchIdAndModuleIdAndSleeperIdAndActiveTrue(
          Long batchId,
          Long moduleId,
          Long sleeperId
  );

  @Query(value = "SELECT COUNT(id) FROM inspection_test_result WHERE result = 'REJECTED' AND active = 1", nativeQuery = true)
  Long getTotalRejectedCount();

  @Query(value = """
SELECT COUNT(r.id)
FROM inspection_test_result r
JOIN inspection_test_header h ON h.id = r.test_header_id
JOIN production_declaration pd ON pd.id = h.batch_id
WHERE r.result = 'REJECTED'
  AND r.active = 1
  AND pd.plant_id = :plantId
""", nativeQuery = true)
  Long getTotalRejectedCountByPlantId(@Param("plantId") String plantId);

  @Query(value = """
SELECT COUNT(r.id)
FROM inspection_test_result r
JOIN inspection_test_header h ON h.id = r.test_header_id
JOIN production_declaration pd ON pd.id = h.batch_id
WHERE r.result = 'REJECTED'
  AND r.active = 1
  AND pd.plant_id IN :plantIds
""", nativeQuery = true)
  Long getTotalRejectedCountByPlantIds(@Param("plantIds") java.util.Collection<String> plantIds);
/*
  @Query(value = """
SELECT vp.plant_id,
       COALESCE(COUNT(d.id), 0)
FROM vendor_plant vp
LEFT JOIN demoulding_inspection di 
    ON di.plant_id COLLATE utf8mb4_unicode_ci = vp.plant_id
    AND di.created_date BETWEEN :startDate AND :endDate
LEFT JOIN demoulding_defective_sleepers d 
    ON d.inspection_id = di.id
GROUP BY vp.plant_id
""", nativeQuery = true)
  List<Object[]> getProcessRejection(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate);
*/

    @Query(value = """
SELECT 
    vp.plant_id,
    COUNT(d.id)
FROM vendor_plant vp
LEFT JOIN demoulding_inspection di 
    ON di.plant_id COLLATE utf8mb4_unicode_ci = vp.plant_id
LEFT JOIN demoulding_defective_sleepers d 
    ON d.inspection_id = di.id
WHERE di.created_date BETWEEN :startDate AND :endDate
AND (
    TRIM(COALESCE(d.visual_reason, '')) <> ''
    OR
    TRIM(COALESCE(d.dim_reason, '')) <> ''
)
GROUP BY vp.plant_id
""", nativeQuery = true)
    List<Object[]> getProcessRejection(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


  @Query(value = """
SELECT 
    pd.plant_id,
    COUNT(DISTINCT CONCAT(h.batch_id, '-', r.sleeper_id)) AS final_rejection
FROM production_declaration pd

JOIN inspection_test_header h 
    ON h.batch_id = pd.id
    AND h.created_date BETWEEN :startDate AND :endDate

JOIN inspection_test_result r 
    ON r.test_header_id = h.id
    AND r.result = 'REJECTED'
    AND r.active = true

GROUP BY pd.plant_id
""", nativeQuery = true)
  List<Object[]> getFinalRejection(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate);
}