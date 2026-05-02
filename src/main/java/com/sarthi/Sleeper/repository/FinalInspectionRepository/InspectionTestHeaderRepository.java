package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.FinalInspectionProjection;
import com.sarthi.Sleeper.entity.FinalInspection.InspectionTestHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionTestHeaderRepository extends JpaRepository<InspectionTestHeader, Long> {
    @Query("""
    SELECT h
    FROM InspectionTestHeader h
    WHERE h.batchId = :batchId
    AND h.module.id = :moduleId
    """)
    InspectionTestHeader findByBatchIdAndModuleId(Long batchId, Long moduleId);

    InspectionTestHeader findTopByBatchIdAndModuleIdOrderByIdDesc(Long batchId, Long moduleId);

    @Query("""
            SELECT h.batchId
            FROM InspectionTestHeader h
            WHERE h.status='Completed'
            GROUP BY h.batchId
            HAVING COUNT(DISTINCT h.module.id)=3
           """)
    List<Long> findCompletedBatchIds();

    @Query("""
            SELECT h.batchId
            FROM InspectionTestHeader h
            WHERE h.status='Completed'
            AND h.batchId IN (
                SELECT d.id
                FROM ProductionDeclaration d
                JOIN d.chambers c
                JOIN c.benchGroups b
                WHERE d.createdBy = :userId AND b.sleeperType = :sleeperType
            )
            GROUP BY h.batchId
            HAVING COUNT(DISTINCT h.module.id)=3
           """)
    List<Long> findCompletedBatchIdsBySleeperTypeAndUserId(String sleeperType, Long userId);

    @Query(value = """
    SELECT 
        MAX(ith.test_date) AS testDate,
        COUNT(itr.id) AS rejectedCount
    FROM inspection_test_header ith
    JOIN inspection_test_result itr 
         ON ith.id = itr.test_header_id
    WHERE ith.batch_id = :batchId
      AND itr.result = 'REJECTED'
      AND itr.active = 1
""", nativeQuery = true)
    FinalInspectionProjection getFinalInspectionData(Long batchId);
}
