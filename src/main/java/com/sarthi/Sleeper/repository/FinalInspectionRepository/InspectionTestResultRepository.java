package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.InspectionTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
  @Query("""
SELECT COUNT(DISTINCT r.sleeperId)
FROM InspectionTestResult r
JOIN r.testHeader h
WHERE h.batchId = :batchId
AND h.module.id = :moduleId
AND r.active = true              -- only active records
AND r.result <> 'PENDING'        -- exclude pending
""")
  Long countTestedSleepers(Long batchId, Long moduleId);

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

    List<InspectionTestResult> findByTestHeader_BatchIdAndResultAndActiveTrue(Long batchId, String rejected);

    List<InspectionTestResult> findByTestHeader_BatchIdAndActiveTrue(Long batchId);

   // List<InspectionTestResult> findByBatchIdAndModuleIdAndSleeperIdAndActiveTrue(Long batchId, Long moduleId, Long sleeperId);

    List<InspectionTestResult> findByTestHeader_BatchIdAndModuleIdAndSleeperIdAndActiveTrue(
            Long batchId,
            Long moduleId,
            Long sleeperId
    );
}
