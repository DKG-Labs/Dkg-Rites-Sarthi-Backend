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

    @Query("""
            SELECT h
            FROM InspectionTestHeader h
            WHERE h.batchId = :batchId
            AND h.module.id = :moduleId
            ORDER BY h.id DESC
            """)
    List<InspectionTestHeader> findHeadersByBatchIdAndModuleIdOrderByIdDesc(@Param("batchId") Long batchId, @Param("moduleId") Long moduleId);

    default InspectionTestHeader findTopByBatchIdAndModuleIdOrderByIdDesc(Long batchId, Long moduleId) {
        List<InspectionTestHeader> list = findHeadersByBatchIdAndModuleIdOrderByIdDesc(batchId, moduleId);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    @Query("""
            SELECT h.batchId, MAX(h.testDate)
            FROM InspectionTestHeader h
            LEFT JOIN h.module m
            WHERE h.batchId IN :batchIds
            AND (m.id = :moduleId OR :moduleId IS NULL)
            GROUP BY h.batchId
            """)
    List<Object[]> findLatestTestDatesByBatchIdsAndModuleId(
            @Param("batchIds") List<Long> batchIds,
            @Param("moduleId") Long moduleId
    );

    @Query("""
            SELECT h.batchId, MAX(h.testDate),
                   CASE WHEN SUM(CASE WHEN LOWER(h.status) = 'completed' THEN 1 ELSE 0 END) > 0 THEN 'Completed' ELSE MAX(h.status) END
            FROM InspectionTestHeader h
            LEFT JOIN h.module m
            WHERE h.batchId IN :batchIds
            AND (m.id = :moduleId OR :moduleId IS NULL)
            GROUP BY h.batchId
            """)
    List<Object[]> findLatestTestDatesAndStatusesByBatchIdsAndModuleId(
            @Param("batchIds") List<Long> batchIds,
            @Param("moduleId") Long moduleId
    );

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
             WHERE UPPER(h.status) = 'COMPLETED'
             AND (
                 h.batchId IN (
                     SELECT c.declaration.id
                     FROM ProductionBenchGroup b
                     JOIN b.chamber c
                     WHERE (c.declaration.createdBy = :userId OR :userId = 0)
                       AND (:sleeperType IS NULL OR :sleeperType = '' OR UPPER(TRIM(b.sleeperType)) = UPPER(TRIM(:sleeperType)))
                 )
                 OR h.batchId IN (
                     SELECT g.declaration.id
                     FROM ProductionLongLineGang g
                     WHERE (g.declaration.createdBy = :userId OR :userId = 0)
                       AND (:sleeperType IS NULL OR :sleeperType = '' OR UPPER(TRIM(g.sleeperType)) = UPPER(TRIM(:sleeperType)))
                 )
             )
             GROUP BY h.batchId
             HAVING COUNT(DISTINCT h.module.id) = 3
            """)
    List<Long> findCompletedBatchIdsBySleeperTypeAndUserId(@Param("sleeperType") String sleeperType, @Param("userId") Long userId);

    @Query("""
             SELECT h.batchId
             FROM InspectionTestHeader h
             WHERE UPPER(h.status) = 'COMPLETED'
             AND (
                 h.batchId IN (
                     SELECT c.declaration.id
                     FROM ProductionBenchGroup b
                     JOIN b.chamber c
                     WHERE (c.declaration.createdBy = :userId OR :userId = 0 
                            OR c.declaration.vendorCode = :vendorCode 
                            OR c.declaration.vendorCode = :vendorCodeClean
                            OR c.declaration.vendorCode = CONCAT(':', :vendorCodeClean)
                            OR REPLACE(c.declaration.vendorCode, ':', '') = :vendorCodeClean)
                       AND (:sleeperType IS NULL OR :sleeperType = '' 
                            OR UPPER(TRIM(b.sleeperType)) = UPPER(TRIM(:sleeperType))
                            OR UPPER(TRIM(REPLACE(b.sleeperType, ' ', ''))) = UPPER(TRIM(REPLACE(:sleeperType, ' ', ''))))
                 )
                 OR h.batchId IN (
                     SELECT g.declaration.id
                     FROM ProductionLongLineGang g
                     WHERE (g.declaration.createdBy = :userId OR :userId = 0 
                            OR g.declaration.vendorCode = :vendorCode 
                            OR g.declaration.vendorCode = :vendorCodeClean
                            OR g.declaration.vendorCode = CONCAT(':', :vendorCodeClean)
                            OR REPLACE(g.declaration.vendorCode, ':', '') = :vendorCodeClean)
                       AND (:sleeperType IS NULL OR :sleeperType = '' 
                            OR UPPER(TRIM(g.sleeperType)) = UPPER(TRIM(:sleeperType))
                            OR UPPER(TRIM(REPLACE(g.sleeperType, ' ', ''))) = UPPER(TRIM(REPLACE(:sleeperType, ' ', ''))))
                 )
             )
             GROUP BY h.batchId
             HAVING COUNT(DISTINCT h.module.id) = 3
            """)
    List<Long> findCompletedBatchIdsBySleeperTypeAndVendor(
            @Param("sleeperType") String sleeperType,
            @Param("userId") Long userId,
            @Param("vendorCode") String vendorCode,
            @Param("vendorCodeClean") String vendorCodeClean);

    @Query("""
             SELECT DISTINCT COALESCE(b.sleeperType, g.sleeperType)
             FROM ProductionDeclaration d
             LEFT JOIN d.chambers c
             LEFT JOIN c.benchGroups b
             LEFT JOIN d.gangs g
             WHERE (d.createdBy = :userId OR :userId = 0)
             AND (b.sleeperType IS NOT NULL OR g.sleeperType IS NOT NULL)
             AND (b.sleeperType <> '' OR g.sleeperType <> '')
             AND d.id IN (
                 SELECT h.batchId
                 FROM InspectionTestHeader h
                 WHERE UPPER(h.status) = 'COMPLETED'
                 GROUP BY h.batchId
                 HAVING COUNT(DISTINCT h.module.id) = 3
             )
            """)
    List<String> findDistinctSleeperTypesByUserId(@Param("userId") Long userId);

    @Query("""
             SELECT DISTINCT COALESCE(b.sleeperType, g.sleeperType)
             FROM ProductionDeclaration d
             LEFT JOIN d.chambers c
             LEFT JOIN c.benchGroups b
             LEFT JOIN d.gangs g
             WHERE (d.createdBy = :userId OR :userId = 0 
                    OR d.vendorCode = :vendorCode 
                    OR d.vendorCode = :vendorCodeClean
                    OR d.vendorCode = CONCAT(':', :vendorCodeClean)
                    OR REPLACE(d.vendorCode, ':', '') = :vendorCodeClean)
             AND (b.sleeperType IS NOT NULL OR g.sleeperType IS NOT NULL)
             AND (b.sleeperType <> '' OR g.sleeperType <> '')
             AND d.id IN (
                 SELECT h.batchId
                 FROM InspectionTestHeader h
                 WHERE UPPER(h.status) = 'COMPLETED'
                 GROUP BY h.batchId
                 HAVING COUNT(DISTINCT h.module.id) = 3
             )
            """)
    List<String> findDistinctSleeperTypesByVendor(
            @Param("userId") Long userId,
            @Param("vendorCode") String vendorCode,
            @Param("vendorCodeClean") String vendorCodeClean);

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

    /** Same as above, but for the production declarations of the closed ICs; repeat rejections are left out. */
    @Query(value = """
            SELECT defects.category, defects.defect_reason, COUNT(*) AS defect_count
            FROM (
                SELECT CASE WHEN dm.reason_type = 'VISUAL' THEN 'Visual (Demoulding)' ELSE 'Dimension (Demoulding)' END AS category,
                       dm.defect_reason
                FROM (
                    SELECT pd.id AS batch_id, dds.visual_reason AS defect_reason, 'VISUAL' AS reason_type
                    FROM demoulding_defective_sleepers dds
                    INNER JOIN demoulding_inspection di ON di.id = dds.inspection_id
                    INNER JOIN production_declaration pd
                       ON TRIM(pd.batch_number) COLLATE utf8mb4_unicode_ci = TRIM(di.batch_no) COLLATE utf8mb4_unicode_ci
                    WHERE pd.id IN (:pdIds)
                      AND dds.visual_reason IS NOT NULL AND dds.visual_reason <> ''
                    UNION ALL
                    SELECT pd.id AS batch_id, dds.dim_reason AS defect_reason, 'DIMENSION' AS reason_type
                    FROM demoulding_defective_sleepers dds
                    INNER JOIN demoulding_inspection di ON di.id = dds.inspection_id
                    INNER JOIN production_declaration pd
                       ON TRIM(pd.batch_number) COLLATE utf8mb4_unicode_ci = TRIM(di.batch_no) COLLATE utf8mb4_unicode_ci
                    WHERE pd.id IN (:pdIds)
                      AND dds.dim_reason IS NOT NULL AND dds.dim_reason <> ''
                ) dm

                UNION ALL

                SELECT CASE WHEN itr.module_id = 1 THEN 'Final (Visual)'
                            WHEN itr.module_id = 2 THEN 'Final (Critical)'
                            WHEN itr.module_id = 3 THEN 'Final (Non-Critical)' END AS category,
                       itr.rejection_reason AS defect_reason
                FROM inspection_test_result itr
                INNER JOIN inspection_test_header ith ON ith.id = itr.test_header_id
                WHERE ith.batch_id IN (:pdIds)
                  AND itr.module_id IN (1,2,3)
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL AND itr.rejection_reason <> ''
                  AND LOWER(itr.rejection_reason) NOT LIKE 'previously rejected%'
            ) defects
            GROUP BY defects.category, defects.defect_reason
            ORDER BY defect_count DESC
            """, nativeQuery = true)
    List<Object[]> getDefectReasonDistributionsByPdIds(
            @Param("pdIds") java.util.Collection<Long> pdIds);

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
                    AND di.casting_date = pd.casting_date

                WHERE pd.id IN (:pdIds)

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
                    AND di.casting_date = pd.casting_date

                WHERE pd.id IN (:pdIds)

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

                WHERE pd.id IN (:pdIds)

                  AND itr.module_id = 1
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''
                  AND LOWER(itr.rejection_reason) NOT LIKE 'previously rejected%'

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

                WHERE pd.id IN (:pdIds)

                  AND itr.module_id = 2
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''
                  AND LOWER(itr.rejection_reason) NOT LIKE 'previously rejected%'

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

                WHERE pd.id IN (:pdIds)

                  AND itr.module_id = 3
                  AND itr.result = 'REJECTED'
                  AND itr.rejection_reason IS NOT NULL
                  AND itr.rejection_reason <> ''
                  AND LOWER(itr.rejection_reason) NOT LIKE 'previously rejected%'

            ) defects

            GROUP BY defects.defect_reason

            ORDER BY defect_count DESC

            """, nativeQuery = true)
    List<Object[]> getParetoAnalysis(
            @Param("pdIds") java.util.Collection<Long> pdIds);

    /**
     * Batches (batch number, casting date, plant) of the calls whose latest status is SEND_CALL_TO_IBS and whose
     * IC date is inside the range. The caller maps them to production declarations.
     */
    @Query(value = """
            SELECT DISTINCT ibs.batch_no, ibs.date_casted, ibs.plant_id
            FROM ie_batch_summary ibs
            INNER JOIN (
                SELECT request_id, MAX(workflow_transition_id) AS max_id
                FROM sleeper_workflow_transaction
                WHERE workflow_id = 2
                GROUP BY request_id
            ) latest ON CONVERT(TRIM(ibs.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(latest.request_id) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
            LEFT JOIN (SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS call_no, MIN(certificate_no) AS cert_no, MIN(created_on) AS created_on
                       FROM sleeper_inspection_complete_details
                       GROUP BY CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci) sicd
                   ON sicd.call_no = CONVERT(TRIM(ibs.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (SELECT CONVERT(TRIM(ic_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic_no, MAX(created_at) AS ic_dt
                       FROM sleeper_final_ic_edit
                       GROUP BY CONVERT(TRIM(ic_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci) sfied
                   ON sfied.ic_no = CONVERT(TRIM(COALESCE(sicd.cert_no, ibs.call_no)) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            WHERE swt.workflow_id = 2
              AND (UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.action, '')) = 'SEND_CALL_TO_IBS')
              AND DATE(COALESCE(sfied.ic_dt, sicd.created_on, swt.created_date)) BETWEEN :fromDate AND :toDate
            """, nativeQuery = true)
    List<Object[]> getClosedIcBatches(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ===== Quality of PSC Sleepers Report (closed ICs, by IC date) =====

    /**
     * One row per call and batch of the closed ICs (latest status SEND_CALL_TO_IBS, IC date inside the range).
     * offered_now / passed are this batch's own figures; total_offered / total_accepted hold the whole CALL's totals
     * repeated on every batch row, so summing them over batches multiplies them by the number of batches.
     */
    @Query(value = """
            SELECT ibs.call_no, ibs.batch_no, ibs.date_casted, ibs.plant_id,
                   ibs.offered_now, ibs.passed,
                   DATE(COALESCE(sfied.ic_dt, sicd.created_on, swt.created_date)) AS ic_day
            FROM ie_batch_summary ibs
            INNER JOIN (
                SELECT request_id, MAX(workflow_transition_id) AS max_id
                FROM sleeper_workflow_transaction
                WHERE workflow_id = 2
                GROUP BY request_id
            ) latest ON CONVERT(TRIM(ibs.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(latest.request_id) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
            LEFT JOIN (SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS call_no, MIN(certificate_no) AS cert_no, MIN(created_on) AS created_on
                       FROM sleeper_inspection_complete_details
                       GROUP BY CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci) sicd
                   ON sicd.call_no = CONVERT(TRIM(ibs.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (SELECT CONVERT(TRIM(ic_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic_no, MAX(created_at) AS ic_dt
                       FROM sleeper_final_ic_edit
                       GROUP BY CONVERT(TRIM(ic_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci) sfied
                   ON sfied.ic_no = CONVERT(TRIM(COALESCE(sicd.cert_no, ibs.call_no)) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            WHERE swt.workflow_id = 2
              AND (UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.action, '')) = 'SEND_CALL_TO_IBS')
              AND DATE(COALESCE(sfied.ic_dt, sicd.created_on, swt.created_date)) BETWEEN :fromDate AND :toDate
            """, nativeQuery = true)
    List<Object[]> getClosedIcBatchRows(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /** Sleeper category (Mainline / Turnout / ...) of each production declaration, from its bench groups or gangs. */
    @Query(value = """
            SELECT t.pd_id, MIN(t.cat) AS cat
            FROM (
                SELECT ch.declaration_id AS pd_id, bg.sleeper_category AS cat
                FROM production_bench_group bg
                JOIN production_stress_chamber ch ON ch.id = bg.chamber_id
                WHERE ch.declaration_id IN (:pdIds)
                UNION ALL
                SELECT g.declaration_id AS pd_id, g.sleeper_category AS cat
                FROM production_longline_gang g
                WHERE g.declaration_id IN (:pdIds)
            ) t
            GROUP BY t.pd_id
            """, nativeQuery = true)
    List<Object[]> getSleeperCategoryByPdIds(@Param("pdIds") java.util.Collection<Long> pdIds);

    /** Final-inspection rejections per batch by defect group: dimension, end damage, honeycomb/surface/crack, dowel, other. */
    @Query(value = """
            SELECT t.batch_id AS pd_id,
                   COUNT(DISTINCT CASE WHEN t.grp = 1 THEN t.sleeper_id END) AS dimension_toe_gauge,
                   COUNT(DISTINCT CASE WHEN t.grp = 2 THEN t.sleeper_id END) AS end_damage,
                   COUNT(DISTINCT CASE WHEN t.grp = 3 THEN t.sleeper_id END) AS honeycomb_surface_crack,
                   COUNT(DISTINCT CASE WHEN t.grp = 4 THEN t.sleeper_id END) AS missing_dowel,
                   COUNT(DISTINCT CASE WHEN t.grp = 5 THEN t.sleeper_id END) AS other_defects
            FROM (
                SELECT ith.batch_id, itr.sleeper_id,
                       CASE
                         WHEN LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%dowel%' THEN 4
                         WHEN LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%end damage%' THEN 2
                         WHEN LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%honey%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%surface%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%crack%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%void%' THEN 3
                         WHEN LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%dimension%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%rail seat%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%gauge%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%toe%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%length%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%width%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%height%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%depth%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%slope%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%twist%'
                           OR LOWER(COALESCE(itr.rejection_reason, '')) LIKE '%camber%' THEN 1
                         ELSE 5
                       END AS grp
                FROM inspection_test_header ith
                JOIN inspection_test_result itr ON itr.test_header_id = ith.id
                WHERE ith.batch_id IN (:pdIds)
                  AND itr.active = 1
                  AND itr.result = 'REJECTED'
                  AND LOWER(COALESCE(itr.rejection_reason, '')) NOT LIKE 'previously rejected%'
            ) t
            GROUP BY t.batch_id
            """, nativeQuery = true)
    List<Object[]> getFinalDefectCountsByPdIds(@Param("pdIds") java.util.Collection<Long> pdIds);

    /** Accepted quantity of the IC itself per call: numbers (acc_nos) and, for turnouts, sets (acc_sets). */
    @Query(value = """
            SELECT TRIM(sfr.call_number) AS call_no,
                   COALESCE(SUM(sfr.total_accepted), 0) AS acc_nos,
                   COALESCE(SUM(sfr.accepted_sets_quantity), 0) AS acc_sets
            FROM sleeper_final_result sfr
            WHERE TRIM(sfr.call_number) IN (:callNos)
            GROUP BY TRIM(sfr.call_number)
            """, nativeQuery = true)
    List<Object[]> getFinalAcceptedByCalls(@Param("callNos") java.util.Collection<String> callNos);

    /** Sleepers rejected in process inspection (demoulding) per batch. */
    @Query(value = """
            SELECT di.batch_no, di.casting_date, di.plant_id, COUNT(dds.id) AS rejected
            FROM demoulding_inspection di
            JOIN demoulding_defective_sleepers dds ON dds.inspection_id = di.id
            WHERE TRIM(di.batch_no) IN (:batchNos)
              AND (TRIM(COALESCE(dds.visual_reason, '')) <> '' OR TRIM(COALESCE(dds.dim_reason, '')) <> '')
            GROUP BY di.batch_no, di.casting_date, di.plant_id
            """, nativeQuery = true)
    List<Object[]> getDemouldingRejectionsByBatch(@Param("batchNos") java.util.Collection<String> batchNos);

    /** Sleepers produced per plant and sleeper category: verified (workflow Completed) declarations cast inside the range. */
    @Query(value = """
            SELECT p.plant_id, p.cat, SUM(p.n) AS produced
            FROM (
                SELECT pd.id AS pd_id, pd.plant_id AS plant_id,
                       COALESCE(pd.total_casted_sleepers, 0) AS n,
                       COALESCE(NULLIF(TRIM(COALESCE(MIN(bg.sleeper_category), MIN(g.sleeper_category))), ''), 'Mainline') AS cat
                FROM production_declaration pd
                JOIN (SELECT CONVERT(request_id USING utf8mb4) COLLATE utf8mb4_unicode_ci AS rid, MAX(workflow_transition_id) AS max_id
                      FROM sleeper_workflow_transaction
                      WHERE module_id = 11
                      GROUP BY CONVERT(request_id USING utf8mb4) COLLATE utf8mb4_unicode_ci) lw
                     ON lw.rid = CAST(pd.id AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci
                JOIN sleeper_workflow_transaction w ON w.workflow_transition_id = lw.max_id AND LOWER(w.status) = 'completed'
                LEFT JOIN production_stress_chamber ch ON ch.declaration_id = pd.id
                LEFT JOIN production_bench_group bg ON bg.chamber_id = ch.id
                LEFT JOIN production_longline_gang g ON g.declaration_id = pd.id
                WHERE pd.casting_date BETWEEN :fromDate AND :toDate
                GROUP BY pd.id, pd.plant_id, pd.total_casted_sleepers
            ) p
            GROUP BY p.plant_id, p.cat
            """, nativeQuery = true)
    List<Object[]> getProducedByPlantCategory(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /** Railway zone and names of every plant. */
    @Query(value = """
            SELECT vp.plant_id, vp.zonal_railway, vp.plant_name, vp.company_name
            FROM vendor_plant vp
            """, nativeQuery = true)
    List<Object[]> getPlantZones();

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
