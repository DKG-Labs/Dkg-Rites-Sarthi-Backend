package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.FinalInspectionProjection;
import com.sarthi.Sleeper.entity.FinalInspection.InspectionTestHeader;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
                 LEFT JOIN d.chambers c
                 LEFT JOIN c.benchGroups b
                 LEFT JOIN d.gangs g
                 WHERE d.createdBy = :userId AND (b.sleeperType = :sleeperType OR g.sleeperType = :sleeperType)
             )
             GROUP BY h.batchId
             HAVING COUNT(DISTINCT h.module.id)=3
            """)
    List<Long> findCompletedBatchIdsBySleeperTypeAndUserId(String sleeperType, Long userId);

    @Query("""
             SELECT DISTINCT COALESCE(b.sleeperType, g.sleeperType)
             FROM ProductionDeclaration d
             LEFT JOIN d.chambers c
             LEFT JOIN c.benchGroups b
             LEFT JOIN d.gangs g
             WHERE d.createdBy = :userId
             AND (b.sleeperType IS NOT NULL OR g.sleeperType IS NOT NULL)
             AND (b.sleeperType <> '' OR g.sleeperType <> '')
             AND d.id IN (
                 SELECT h.batchId
                 FROM InspectionTestHeader h
                 WHERE h.status = 'Completed'
                 GROUP BY h.batchId
                 HAVING COUNT(DISTINCT h.module.id) = 3
             )
            """)
    List<String> findDistinctSleeperTypesByUserId(@Param("userId") Long userId);

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

    @Query("""
                SELECT b
                FROM SleeperInspectionCallBatch b
                JOIN b.inspectionCall c
                WHERE c.callNo = :callNo
            """)
    List<SleeperInspectionCallBatch> getBatchesByCallNo(String callNo);
    /*
     * 
     * @Query(value = """
     * 
     * SELECT
     * defects.defect_name,
     * COUNT(*) AS defect_count
     * 
     * FROM (
     * 
     * 
     * 
     * SELECT
     * dds.visual_reason AS defect_name
     * 
     * FROM demoulding_defective_sleepers dds
     * 
     * INNER JOIN demoulding_inspection di
     * ON di.id = dds.inspection_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.batch_number COLLATE utf8mb4_unicode_ci = di.batch_no COLLATE
     * utf8mb4_unicode_ci
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * AND pd.plant_id = :plantId
     * 
     * 
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header ith
     * 
     * WHERE ith.batch_id = pd.id
     * AND ith.module_id IN (1,2,3)
     * 
     * GROUP BY ith.batch_id
     * 
     * HAVING COUNT(DISTINCT ith.module_id) = 3
     * )
     * 
     * AND dds.visual_reason IS NOT NULL
     * AND dds.visual_reason <> ''
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * dds.dim_reason AS defect_name
     * 
     * FROM demoulding_defective_sleepers dds
     * 
     * INNER JOIN demoulding_inspection di
     * ON di.id = dds.inspection_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.batch_number COLLATE utf8mb4_unicode_ci = di.batch_no COLLATE
     * utf8mb4_unicode_ci
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * AND pd.plant_id = :plantId
     * 
     * 
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header ith
     * 
     * WHERE ith.batch_id = pd.id
     * AND ith.module_id IN (1,2,3)
     * 
     * GROUP BY ith.batch_id
     * 
     * HAVING COUNT(DISTINCT ith.module_id) = 3
     * )
     * 
     * AND dds.dim_reason IS NOT NULL
     * AND dds.dim_reason <> ''
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * itr.rejection_reason AS defect_name
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * AND pd.plant_id = :plantId
     * 
     * AND itr.module_id = 1
     * AND itr.result = 'REJECTED'
     * 
     * 
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header i2
     * 
     * WHERE i2.batch_id = pd.id
     * AND i2.module_id IN (1,2,3)
     * 
     * GROUP BY i2.batch_id
     * 
     * HAVING COUNT(DISTINCT i2.module_id) = 3
     * )
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * itr.rejection_reason AS defect_name
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * AND pd.plant_id = :plantId
     * 
     * AND itr.module_id = 2
     * AND itr.result = 'REJECTED'
     * 
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header i2
     * 
     * WHERE i2.batch_id = pd.id
     * AND i2.module_id IN (1,2,3)
     * 
     * GROUP BY i2.batch_id
     * 
     * HAVING COUNT(DISTINCT i2.module_id) = 3
     * )
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * itr.rejection_reason AS defect_name
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * AND pd.plant_id = :plantId
     * 
     * AND itr.module_id = 3
     * AND itr.result = 'REJECTED'
     * 
     * 
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header i2
     * 
     * WHERE i2.batch_id = pd.id
     * AND i2.module_id IN (1,2,3)
     * 
     * GROUP BY i2.batch_id
     * 
     * HAVING COUNT(DISTINCT i2.module_id) = 3
     * )
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * ) defects
     * 
     * GROUP BY defects.defect_name
     * 
     * ORDER BY defect_count DESC
     * 
     * """, nativeQuery = true)
     * List<Object[]> getProcessDefectDistribution(
     * 
     * @Param("plantId") String plantId,
     * 
     * @Param("fromDate") LocalDate fromDate,
     * 
     * @Param("toDate") LocalDate toDate
     * );
     */

    @Query(value = """

            SELECT
                defects.defect_name,
                COUNT(*) AS defect_count

            FROM (

                /* =====================================================
                   DEMOULDING VISUAL DEFECTS
                   ===================================================== */

                SELECT
                    dds.visual_reason AS defect_name

                FROM demoulding_defective_sleepers dds

                INNER JOIN demoulding_inspection di
                    ON di.id = dds.inspection_id

                INNER JOIN production_declaration pd
                    ON pd.batch_number COLLATE utf8mb4_unicode_ci =
                       di.batch_no COLLATE utf8mb4_unicode_ci

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND pd.plant_id = :plantId
                  AND dds.visual_reason IS NOT NULL
                  AND dds.visual_reason <> ''

                UNION ALL

                /* =====================================================
                   DEMOULDING DIMENSION DEFECTS
                   ===================================================== */

                SELECT
                    dds.dim_reason AS defect_name

                FROM demoulding_defective_sleepers dds

                INNER JOIN demoulding_inspection di
                    ON di.id = dds.inspection_id

                INNER JOIN production_declaration pd
                    ON pd.batch_number COLLATE utf8mb4_unicode_ci =
                       di.batch_no COLLATE utf8mb4_unicode_ci

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND pd.plant_id = :plantId
                  AND dds.dim_reason IS NOT NULL
                  AND dds.dim_reason <> ''

                UNION ALL

                /* =====================================================
                   FINAL VISUAL DEFECTS
                   module_id = 1
                   ===================================================== */

                SELECT
                    itr.rejection_reason AS defect_name

                FROM inspection_test_result itr

                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id

                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND pd.plant_id = :plantId
                  AND itr.module_id = 1
                  AND itr.result = 'REJECTED'

                  AND EXISTS (

                        SELECT 1

                        FROM inspection_test_header i2

                        WHERE i2.batch_id = pd.id
                          AND i2.module_id IN (1,2,3)

                        GROUP BY i2.batch_id

                        HAVING COUNT(DISTINCT i2.module_id) = 3
                  )

                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

                UNION ALL

                /* =====================================================
                   FINAL CRITICAL DEFECTS
                   module_id = 2
                   ===================================================== */

                SELECT
                    itr.rejection_reason AS defect_name

                FROM inspection_test_result itr

                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id

                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND pd.plant_id = :plantId
                  AND itr.module_id = 2
                  AND itr.result = 'REJECTED'

                  AND EXISTS (

                        SELECT 1

                        FROM inspection_test_header i2

                        WHERE i2.batch_id = pd.id
                          AND i2.module_id IN (1,2,3)

                        GROUP BY i2.batch_id

                        HAVING COUNT(DISTINCT i2.module_id) = 3
                  )

                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

                UNION ALL

                /* =====================================================
                   FINAL NON CRITICAL DEFECTS
                   module_id = 3
                   ===================================================== */

                SELECT
                    itr.rejection_reason AS defect_name

                FROM inspection_test_result itr

                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id

                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND pd.plant_id = :plantId
                  AND itr.module_id = 3
                  AND itr.result = 'REJECTED'

                  AND EXISTS (

                        SELECT 1

                        FROM inspection_test_header i2

                        WHERE i2.batch_id = pd.id
                          AND i2.module_id IN (1,2,3)

                        GROUP BY i2.batch_id

                        HAVING COUNT(DISTINCT i2.module_id) = 3
                  )

                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

            ) defects

            GROUP BY defects.defect_name

            ORDER BY defect_count DESC

            """, nativeQuery = true)
    List<Object[]> getProcessDefectDistribution(
            @Param("plantId") String plantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /*
     * @Query(value = """
     * 
     * SELECT
     * defects.category,
     * defects.defect_reason,
     * COUNT(*) AS defect_count
     * 
     * FROM (
     * 
     * 
     * 
     * SELECT
     * 'Visual (Demoulding)' AS category,
     * dds.visual_reason AS defect_reason
     * 
     * FROM demoulding_defective_sleepers dds
     * 
     * INNER JOIN demoulding_inspection di
     * ON di.id = dds.inspection_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.batch_number COLLATE utf8mb4_unicode_ci = di.batch_no COLLATE
     * utf8mb4_unicode_ci
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND dds.visual_reason IS NOT NULL
     * AND dds.visual_reason <> ''
     * 
     * 
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header ith
     * 
     * WHERE ith.batch_id = pd.id
     * AND ith.module_id IN (1,2,3)
     * 
     * GROUP BY ith.batch_id
     * 
     * HAVING COUNT(DISTINCT ith.module_id) = 3
     * )
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * 'Dimension (Demoulding)' AS category,
     * dds.dim_reason AS defect_reason
     * 
     * FROM demoulding_defective_sleepers dds
     * 
     * INNER JOIN demoulding_inspection di
     * ON di.id = dds.inspection_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.batch_number COLLATE utf8mb4_unicode_ci = di.batch_no COLLATE
     * utf8mb4_unicode_ci
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND dds.dim_reason IS NOT NULL
     * AND dds.dim_reason <> ''
     * 
     * 
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header ith
     * 
     * WHERE ith.batch_id = pd.id
     * AND ith.module_id IN (1,2,3)
     * 
     * GROUP BY ith.batch_id
     * 
     * HAVING COUNT(DISTINCT ith.module_id) = 3
     * )
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * 'Final (Visual)' AS category,
     * itr.rejection_reason AS defect_reason
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND itr.module_id = 1
     * AND itr.result COLLATE utf8mb4_unicode_ci = 'REJECTED'
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * 'Final (Critical)' AS category,
     * itr.rejection_reason AS defect_reason
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND itr.module_id = 2
     * AND itr.result COLLATE utf8mb4_unicode_ci = 'REJECTED'
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * UNION ALL
     * 
     * 
     * 
     * SELECT
     * 'Final (Non-Critical)' AS category,
     * itr.rejection_reason AS defect_reason
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND itr.module_id = 3
     * AND itr.result COLLATE utf8mb4_unicode_ci = 'REJECTED'
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * ) defects
     * 
     * GROUP BY
     * defects.category,
     * defects.defect_reason
     * 
     * ORDER BY defect_count DESC
     * 
     * """, nativeQuery = true)
     * List<Object[]> getDefectReasonDistribution(
     * 
     * @Param("fromDate") LocalDate fromDate,
     * 
     * @Param("toDate") LocalDate toDate
     * );
     */
    @Query(value = """
            SELECT
                defects.category,
                defects.defect_reason,
                COUNT(*) AS defect_count
            FROM (

                SELECT
                    'Visual (Demoulding)' AS category,
                    dds.visual_reason AS defect_reason
                FROM demoulding_defective_sleepers dds
                INNER JOIN demoulding_inspection di
                    ON di.id = dds.inspection_id
                INNER JOIN production_declaration pd
                    ON pd.batch_number COLLATE utf8mb4_unicode_ci
                     = di.batch_no COLLATE utf8mb4_unicode_ci
                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND dds.visual_reason IS NOT NULL
                  AND dds.visual_reason <> ''
                  AND EXISTS (
                        SELECT 1
                        FROM inspection_test_header ith
                        WHERE ith.batch_id = pd.id
                          AND ith.module_id IN (1,2,3)
                        GROUP BY ith.batch_id
                        HAVING COUNT(DISTINCT ith.module_id) = 3
                  )

                UNION ALL

                SELECT
                    'Dimension (Demoulding)' AS category,
                    dds.dim_reason AS defect_reason
                FROM demoulding_defective_sleepers dds
                INNER JOIN demoulding_inspection di
                    ON di.id = dds.inspection_id
                INNER JOIN production_declaration pd
                    ON pd.batch_number COLLATE utf8mb4_unicode_ci
                     = di.batch_no COLLATE utf8mb4_unicode_ci
                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND dds.dim_reason IS NOT NULL
                  AND dds.dim_reason <> ''
                  AND EXISTS (
                        SELECT 1
                        FROM inspection_test_header ith
                        WHERE ith.batch_id = pd.id
                          AND ith.module_id IN (1,2,3)
                        GROUP BY ith.batch_id
                        HAVING COUNT(DISTINCT ith.module_id) = 3
                  )

                UNION ALL

                SELECT
                    'Final (Visual)' AS category,
                    itr.rejection_reason AS defect_reason
                FROM inspection_test_result itr
                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id
                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id
                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND itr.module_id = 1
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

                UNION ALL

                SELECT
                    'Final (Critical)' AS category,
                    itr.rejection_reason AS defect_reason
                FROM inspection_test_result itr
                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id
                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id
                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND itr.module_id = 2
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

                UNION ALL

                SELECT
                    'Final (Non-Critical)' AS category,
                    itr.rejection_reason AS defect_reason
                FROM inspection_test_result itr
                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id
                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id
                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                  AND itr.module_id = 3
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

            ) defects

            GROUP BY defects.category, defects.defect_reason
            ORDER BY defect_count DESC
            """, nativeQuery = true)
    List<Object[]> getDefectReasonDistribution(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);



    @Query(value = """
SELECT
    defects.category,
    defects.defect_reason,
    COUNT(*) AS defect_count
FROM (

    /* Demoulding */
    SELECT
        CASE
            WHEN reason_type = 'VISUAL'
                THEN 'Visual (Demoulding)'
            ELSE 'Dimension (Demoulding)'
        END AS category,
        defect_reason
    FROM (
        SELECT
            pd.id AS batch_id,
            dds.visual_reason AS defect_reason,
            'VISUAL' AS reason_type
        FROM demoulding_defective_sleepers dds
        INNER JOIN demoulding_inspection di
            ON di.id = dds.inspection_id
        INNER JOIN production_declaration pd
           ON pd.batch_number COLLATE utf8mb4_unicode_ci =
                          di.batch_no COLLATE utf8mb4_unicode_ci
        WHERE pd.casting_date BETWEEN :fromDate AND :toDate
          AND dds.visual_reason IS NOT NULL
          AND dds.visual_reason <> ''

        UNION ALL

        SELECT
            pd.id AS batch_id,
            dds.dim_reason AS defect_reason,
            'DIMENSION' AS reason_type
        FROM demoulding_defective_sleepers dds
        INNER JOIN demoulding_inspection di
            ON di.id = dds.inspection_id
        INNER JOIN production_declaration pd
           ON pd.batch_number COLLATE utf8mb4_unicode_ci =
                          di.batch_no COLLATE utf8mb4_unicode_ci
        WHERE pd.casting_date BETWEEN :fromDate AND :toDate
          AND dds.dim_reason IS NOT NULL
          AND dds.dim_reason <> ''
    ) dm
    INNER JOIN (
        SELECT batch_id
        FROM inspection_test_header
        WHERE module_id IN (1,2,3)
        GROUP BY batch_id
        HAVING COUNT(DISTINCT module_id) = 3
    ) vb
        ON vb.batch_id = dm.batch_id

    UNION ALL

    /* Final Inspection */
    SELECT
        CASE
            WHEN itr.module_id = 1 THEN 'Final (Visual)'
            WHEN itr.module_id = 2 THEN 'Final (Critical)'
            WHEN itr.module_id = 3 THEN 'Final (Non-Critical)'
        END AS category,
        itr.rejection_reason AS defect_reason
    FROM inspection_test_result itr
    INNER JOIN inspection_test_header ith
        ON ith.id = itr.test_header_id
    INNER JOIN production_declaration pd
        ON pd.id = ith.batch_id
       
    WHERE pd.casting_date BETWEEN :fromDate AND :toDate
      AND itr.module_id IN (1,2,3)
      AND itr.result = 'REJECTED'
      AND itr.rejection_reason IS NOT NULL
      AND itr.rejection_reason <> ''

) defects
GROUP BY defects.category, defects.defect_reason
ORDER BY defect_count DESC
""", nativeQuery = true)
    List<Object[]> getDefectReasonDistributions(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /*
     * @Query(value = """
     * 
     * SELECT
     * defect_category,
     * COUNT(*) AS defect_count
     * 
     * FROM (
     * 
     * /* =========================================
     * VISUAL DEMOULDING
     * =========================================
     */

    /*
     * SELECT
     * 'Visual (Dem)' AS defect_category
     * 
     * FROM demoulding_defective_sleepers dds
     * 
     * INNER JOIN demoulding_inspection di
     * ON di.id = dds.inspection_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.batch_number = di.batch_no
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND dds.visual_reason IS NOT NULL
     * AND dds.visual_reason <> ''
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header ith
     * 
     * WHERE ith.batch_id = pd.id
     * AND ith.module_id IN (1,2,3)
     * 
     * GROUP BY ith.batch_id
     * 
     * HAVING COUNT(DISTINCT ith.module_id) = 3
     * )
     * 
     * UNION ALL
     * 
     * /* =========================================
     * DIMENSION DEMOULDING
     * =========================================
     */

    /*
     * SELECT
     * 'Dimension (Dem)' AS defect_category
     * 
     * FROM demoulding_defective_sleepers dds
     * 
     * INNER JOIN demoulding_inspection di
     * ON di.id = dds.inspection_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.batch_number = di.batch_no
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND dds.dim_reason IS NOT NULL
     * AND dds.dim_reason <> ''
     * 
     * AND EXISTS (
     * 
     * SELECT 1
     * 
     * FROM inspection_test_header ith
     * 
     * WHERE ith.batch_id = pd.id
     * AND ith.module_id IN (1,2,3)
     * 
     * GROUP BY ith.batch_id
     * 
     * HAVING COUNT(DISTINCT ith.module_id) = 3
     * )
     * 
     * UNION ALL
     * 
     * /* =========================================
     * FINAL VISUAL
     * =========================================
     */

    /*
     * SELECT
     * 'Final (Visual)' AS defect_category
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND itr.module_id = 1
     * AND itr.result = 'REJECTED'
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * UNION ALL
     * 
     * /* =========================================
     * FINAL CRITICAL
     * =========================================
     */

    /*
     * SELECT
     * 'Final (Critical)' AS defect_category
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND itr.module_id = 2
     * AND itr.result = 'REJECTED'
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * UNION ALL
     * 
     * /* =========================================
     * FINAL NON CRITICAL
     * =========================================
     */

    /*
     * SELECT
     * 'Final (Non-Critical)' AS defect_category
     * 
     * FROM inspection_test_result itr
     * 
     * INNER JOIN inspection_test_header ith
     * ON ith.id = itr.test_header_id
     * 
     * INNER JOIN production_declaration pd
     * ON pd.id = ith.batch_id
     * 
     * WHERE pd.casting_date BETWEEN :fromDate AND :toDate
     * 
     * AND itr.module_id = 3
     * AND itr.result = 'REJECTED'
     * 
     * AND itr.rejection_reason IS NOT NULL
     * AND itr.rejection_reason <> ''
     * 
     * ) defects
     * 
     * GROUP BY defect_category
     * 
     * ORDER BY defect_count DESC
     * 
     * """, nativeQuery = true)
     * List<Object[]> getParetoAnalysis(
     * 
     * @Param("fromDate") LocalDate fromDate,
     * 
     * @Param("toDate") LocalDate toDate
     * );
     */

    @Query(value = """

            SELECT
                defects.defect_reason,
                COUNT(*) AS defect_count

            FROM (

                /* DEMOULDING VISUAL */

                SELECT
                    CONVERT(dds.visual_reason USING utf8mb4)
                    COLLATE utf8mb4_unicode_ci AS defect_reason

                FROM demoulding_defective_sleepers dds

                INNER JOIN demoulding_inspection di
                    ON di.id = dds.inspection_id

                INNER JOIN production_declaration pd
                    ON pd.batch_number COLLATE utf8mb4_unicode_ci
                     = di.batch_no COLLATE utf8mb4_unicode_ci

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate

                  AND dds.visual_reason IS NOT NULL
                  AND dds.visual_reason <> ''

                  AND EXISTS (
                        SELECT 1
                        FROM inspection_test_header ith
                        WHERE ith.batch_id = pd.id
                          AND ith.module_id IN (1,2,3)
                        GROUP BY ith.batch_id
                        HAVING COUNT(DISTINCT ith.module_id) = 3
                  )

                UNION ALL

                /* DEMOULDING DIMENSION */

                SELECT
                    CONVERT(dds.dim_reason USING utf8mb4)
                    COLLATE utf8mb4_unicode_ci AS defect_reason

                FROM demoulding_defective_sleepers dds

                INNER JOIN demoulding_inspection di
                    ON di.id = dds.inspection_id

                INNER JOIN production_declaration pd
                    ON pd.batch_number COLLATE utf8mb4_unicode_ci
                     = di.batch_no COLLATE utf8mb4_unicode_ci

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate

                  AND dds.dim_reason IS NOT NULL
                  AND dds.dim_reason <> ''

                  AND EXISTS (
                        SELECT 1
                        FROM inspection_test_header ith
                        WHERE ith.batch_id = pd.id
                          AND ith.module_id IN (1,2,3)
                        GROUP BY ith.batch_id
                        HAVING COUNT(DISTINCT ith.module_id) = 3
                  )

                UNION ALL

                /* FINAL VISUAL */

                SELECT
                    CONVERT(itr.rejection_reason USING utf8mb4)
                    COLLATE utf8mb4_unicode_ci AS defect_reason

                FROM inspection_test_result itr

                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id

                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate

                  AND itr.module_id = 1
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

                UNION ALL

                /* FINAL CRITICAL */

                SELECT
                    CONVERT(itr.rejection_reason USING utf8mb4)
                    COLLATE utf8mb4_unicode_ci AS defect_reason

                FROM inspection_test_result itr

                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id

                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate

                  AND itr.module_id = 2
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

                UNION ALL

                /* FINAL NON CRITICAL */

                SELECT
                    CONVERT(itr.rejection_reason USING utf8mb4)
                    COLLATE utf8mb4_unicode_ci AS defect_reason

                FROM inspection_test_result itr

                INNER JOIN inspection_test_header ith
                    ON ith.id = itr.test_header_id

                INNER JOIN production_declaration pd
                    ON pd.id = ith.batch_id

                WHERE pd.casting_date BETWEEN :fromDate AND :toDate

                  AND itr.module_id = 3
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''

            ) defects

            GROUP BY defects.defect_reason

            ORDER BY defect_count DESC

            """, nativeQuery = true)
    List<Object[]> getParetoAnalysis(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(value = """

            SELECT
                spm.company_name                           AS companyName,

                vp.plant_name                              AS plantName,

                pd.plant_id                                AS plantId,

                ifm.rio                                    AS rio,

                um.username                                AS ieName,

                'Final'               AS stageOfInspection,

                ith.shift                                  AS shift,

                COUNT(
                    DISTINCT CONCAT(
                        ith.test_date,
                        '-',
                        ith.shift
                    )
                )                                          AS shiftsWorked,

                COUNT(
                    CASE
                        WHEN itr.active = 1
                        THEN itr.id
                    END
                )                                          AS rejectedSleepers

            FROM inspection_test_header ith

            LEFT JOIN inspection_test_result itr
                   ON itr.test_header_id = ith.id

            LEFT JOIN production_declaration pd
                   ON pd.id = ith.batch_id

            LEFT JOIN vendor_plant vp
                   ON vp.plant_id COLLATE utf8mb4_unicode_ci =
                      pd.plant_id COLLATE utf8mb4_unicode_ci

            LEFT JOIN sleeper_pincode_poi_mapping spm
                   ON spm.vendor_code COLLATE utf8mb4_unicode_ci =
                      vp.vendor_id COLLATE utf8mb4_unicode_ci

            LEFT JOIN ie_fields_mapping ifm
                   ON ifm.plant_pincode COLLATE utf8mb4_unicode_ci =
                      spm.pin_code COLLATE utf8mb4_unicode_ci

            LEFT JOIN user_master um
                   ON um.userid = ith.created_by

            WHERE itr.result = 'REJECTED'
              AND ith.test_date BETWEEN :fromDate AND :toDate

            GROUP BY
                spm.company_name,
                vp.plant_name,
                pd.plant_id,
                ifm.rio,
                um.username,
                ith.shift

            """, nativeQuery = true)
    List<Object[]> getFinalInspectionReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
