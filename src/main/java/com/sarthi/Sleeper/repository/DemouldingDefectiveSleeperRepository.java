package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.DemouldingDefectiveSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
AND (
    :plantId IS NULL OR :plantId = '' 
    OR i.plant_id = :plantId 
    OR REPLACE(i.plant_id, ':', '') = REPLACE(:plantId, ':', '')
    OR i.plant_id LIKE CONCAT('%', REPLACE(:plantId, ':', ''), '%')
    OR :plantId LIKE CONCAT('%', REPLACE(i.plant_id, ':', ''), '%')
)
AND i.id = (
    SELECT MAX(sub.id) 
    FROM demoulding_inspection sub 
    WHERE sub.batch_no = :batchNo
      AND (
          :plantId IS NULL OR :plantId = '' 
          OR sub.plant_id = :plantId 
          OR REPLACE(sub.plant_id, ':', '') = REPLACE(:plantId, ':', '')
          OR sub.plant_id LIKE CONCAT('%', REPLACE(:plantId, ':', ''), '%')
          OR :plantId LIKE CONCAT('%', REPLACE(sub.plant_id, ':', ''), '%')
      )
)
AND (
    (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
    OR
    (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
)
""", nativeQuery = true)
    Set<String> findRejectedSleeperNos(@Param("batchNo") String batchNo, @Param("plantId") String plantId);

    default Set<String> findRejectedSleeperNos(String batchNo) {
        return findRejectedSleeperNos(batchNo, null);
    }

    @Query(value = """
SELECT DISTINCT d.sleeper_no
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection i 
  ON i.id = d.inspection_id
WHERE REPLACE(UPPER(i.batch_no), ' ', '') = REPLACE(UPPER(:batchNo), ' ', '')
AND (
    :plantId IS NULL OR :plantId = '' 
    OR i.plant_id = :plantId 
    OR REPLACE(i.plant_id, ':', '') = REPLACE(:plantId, ':', '')
    OR i.plant_id LIKE CONCAT('%', REPLACE(:plantId, ':', ''), '%')
    OR :plantId LIKE CONCAT('%', REPLACE(i.plant_id, ':', ''), '%')
)
AND d.sleeper_no IS NOT NULL
AND TRIM(d.sleeper_no) <> ''
AND (
    (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
    OR
    (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
)
""", nativeQuery = true)
    Set<String> findAllRejectedSleeperNosByBatchNo(@Param("batchNo") String batchNo, @Param("plantId") String plantId);

    default Set<String> findAllRejectedSleeperNosByBatchNo(String batchNo) {
        return findAllRejectedSleeperNosByBatchNo(batchNo, null);
    }

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

    @Query(value = """
SELECT COUNT(d.id)
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection di ON di.id = d.inspection_id
WHERE (di.plant_id IN (:plantIds) OR di.vendor_code IN (:plantIds) 
    OR REPLACE(COALESCE(di.plant_id, ''), ':', '') IN (:plantIds) 
    OR REPLACE(COALESCE(di.vendor_code, ''), ':', '') IN (:plantIds))
  AND (:startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR DATE(COALESCE(di.inspection_date, di.created_date)) BETWEEN :startDate AND :endDate)
  AND (
    (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
    OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
  )
""", nativeQuery = true)
    Long countByWithReasonsAndPlantIdsAndDate(
            @Param("plantIds") java.util.Collection<String> plantIds,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query(value = """
SELECT COUNT(d.id)
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection di ON di.id = d.inspection_id
WHERE (:startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR DATE(COALESCE(di.inspection_date, di.created_date)) BETWEEN :startDate AND :endDate)
  AND (
    (d.visual_reason IS NOT NULL AND d.visual_reason <> '')
    OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '')
  )
""", nativeQuery = true)
    Long countByWithReasonsAndDate(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query(value = "SELECT COUNT(id) FROM demoulding_defective_sleepers", nativeQuery = true)
    Long countBy();

    @Query(value = """
        SELECT 
            DATE_FORMAT(IFNULL(di.inspection_date, di.created_date), '%b-%y') AS Month_Year,
            YEAR(IFNULL(di.inspection_date, di.created_date)) AS Y,
            MONTH(IFNULL(di.inspection_date, di.created_date)) AS M,
            COUNT(d.id) AS Total_Rejected
        FROM demoulding_defective_sleepers d
        JOIN demoulding_inspection di ON di.id = d.inspection_id
        WHERE IFNULL(di.inspection_date, di.created_date) BETWEEN :startDate AND :endDate
          AND ((d.visual_reason IS NOT NULL AND d.visual_reason <> '') OR (d.dim_reason IS NOT NULL AND d.dim_reason <> ''))
        GROUP BY Y, M, Month_Year
        ORDER BY Y ASC, M ASC
    """, nativeQuery = true)
    List<Object[]> findMonthlyDemouldingRejections(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
        SELECT COUNT(DISTINCT dds.id)
        FROM ie_batch_summary ibs
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON CONVERT(TRIM(ibs.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(latest.request_id) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
        LEFT JOIN sleeper_inspection_call sic ON CONVERT(TRIM(ibs.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(sic.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON (CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.po_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN vendor_plant vp ON (CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(sic.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        INNER JOIN demoulding_inspection di ON (
            di.batch_no = ibs.batch_no
            AND di.casting_date = ibs.date_casted
            AND (CONVERT(di.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ibs.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
                 OR CONVERT(REPLACE(di.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(ibs.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        )
        INNER JOIN demoulding_defective_sleepers dds ON di.id = dds.inspection_id
        WHERE swt.workflow_id = 2
          AND (UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.action, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.job_status, '')) = 'IC_GENERATION')
          AND ((dds.visual_reason IS NOT NULL AND dds.visual_reason <> '') OR (dds.dim_reason IS NOT NULL AND dds.dim_reason <> ''))
          AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR :vendorPlantCode = 'all' OR
               CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(REPLACE(COALESCE(sic.plant_id, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(:vendorPlantCode, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(ibs.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
               CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
               REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.vendor_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%') OR
               REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(vp.company_name, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%'))
          AND (:zonalRailway IS NULL OR :zonalRailway = '' OR :zonalRailway = 'all' OR
               UPPER(TRIM(CONVERT(COALESCE(ph.rly_short_name, ph.rly_cd, vp.zonal_railway, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci)))
          AND (:startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR
               DATE(COALESCE(swt.created_date, di.inspection_date)) BETWEEN :startDate AND :endDate)
    """, nativeQuery = true)
    Long countDemouldingDefectsForIssuedIcsFiltered(
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query(value = """
        SELECT COUNT(DISTINCT dds.id)
        FROM ie_batch_summary ibs
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON CONVERT(TRIM(ibs.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(latest.request_id) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
        INNER JOIN demoulding_inspection di ON (
            di.batch_no = ibs.batch_no
            AND di.casting_date = ibs.date_casted
            AND (CONVERT(di.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ibs.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
                 OR CONVERT(REPLACE(di.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(ibs.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        )
        INNER JOIN demoulding_defective_sleepers dds ON di.id = dds.inspection_id
        WHERE swt.workflow_id = 2
          AND (UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.action, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.job_status, '')) = 'IC_GENERATION')
          AND ((dds.visual_reason IS NOT NULL AND dds.visual_reason <> '') OR (dds.dim_reason IS NOT NULL AND dds.dim_reason <> ''))
    """, nativeQuery = true)
    Long countAllDemouldingDefectsForIssuedIcs();
}
