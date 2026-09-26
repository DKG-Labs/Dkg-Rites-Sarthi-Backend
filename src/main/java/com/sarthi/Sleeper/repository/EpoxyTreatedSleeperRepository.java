package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.EtProjection;
import com.sarthi.Sleeper.entity.EpoxyTreatedSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpoxyTreatedSleeperRepository extends JpaRepository<EpoxyTreatedSleeper, Long> {

    // Single optimized query with zero N+1 queries: aggregates production declaration totals and matches on casting_date and plant_id
    @Query(value = """
SELECT
    et.batch_number,
    et.location,
    COALESCE(et.date_of_casting, pd_summary.casting_date),
    pd_summary.total_sleepers,
    COUNT(DISTINCT esd.id),
    COALESCE(et.plant_id, pd_summary.plant_id),
    COALESCE(et.vendor_code, pd_summary.vendor_code),
    et.created_by
FROM et_epoxy_treated_sleeper et
JOIN (
    SELECT
        pd.batch_number,
        pd.production_unit,
        pd.casting_date,
        pd.plant_id,
        pd.vendor_code,
        SUM(pd.total_casted_sleepers) AS total_sleepers
    FROM production_declaration pd
    GROUP BY pd.batch_number, pd.production_unit, pd.casting_date, pd.plant_id, pd.vendor_code
) pd_summary
    ON pd_summary.batch_number COLLATE utf8mb4_unicode_ci = et.batch_number COLLATE utf8mb4_unicode_ci
    AND pd_summary.production_unit COLLATE utf8mb4_unicode_ci = et.location COLLATE utf8mb4_unicode_ci
    AND (et.date_of_casting IS NULL OR pd_summary.casting_date = et.date_of_casting)
    AND (et.plant_id IS NULL OR pd_summary.plant_id IS NULL OR REPLACE(pd_summary.plant_id, ':', '') COLLATE utf8mb4_unicode_ci = REPLACE(et.plant_id, ':', '') COLLATE utf8mb4_unicode_ci)
JOIN et_sleeper_details esd
    ON esd.et_id = et.id
WHERE (:plantId IS NULL OR :plantId = ''
    OR et.plant_id COLLATE utf8mb4_unicode_ci = CONVERT(:plantId USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR pd_summary.plant_id COLLATE utf8mb4_unicode_ci = CONVERT(:plantId USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR et.plant_id COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:plantId USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci
    OR pd_summary.plant_id COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:plantId USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci)
  AND (:vendorCode IS NULL OR :vendorCode = ''
    OR et.vendor_code COLLATE utf8mb4_unicode_ci = CONVERT(:vendorCode USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR pd_summary.vendor_code COLLATE utf8mb4_unicode_ci = CONVERT(:vendorCode USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR et.vendor_code COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorCode USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci
    OR pd_summary.vendor_code COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorCode USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci)
  AND (:createdBy IS NULL OR et.created_by = :createdBy)
GROUP BY et.batch_number, et.location, COALESCE(et.date_of_casting, pd_summary.casting_date), pd_summary.total_sleepers, et.plant_id, pd_summary.plant_id, et.vendor_code, pd_summary.vendor_code, et.created_by
""", nativeQuery = true)
    List<Object[]> getBatchWiseEtSummary(
        @Param("plantId") String plantId,
        @Param("vendorCode") String vendorCode,
        @Param("createdBy") Long createdBy
    );

    @Query("""
SELECT et FROM EpoxyTreatedSleeper et
WHERE (:plantId IS NULL OR :plantId = '' OR et.plantId = :plantId OR et.plantId LIKE CONCAT('%', :plantId, '%'))
  AND (:vendorCode IS NULL OR :vendorCode = '' OR et.vendorCode = :vendorCode OR et.vendorCode LIKE CONCAT('%', :vendorCode, '%'))
  AND (:createdBy IS NULL OR et.createdBy = :createdBy)
ORDER BY et.id DESC
""")
    List<EpoxyTreatedSleeper> findAllByPlantAndVendor(
        @Param("plantId") String plantId,
        @Param("vendorCode") String vendorCode,
        @Param("createdBy") Long createdBy
    );

    @Query(value = """
    SELECT ets.created_date AS createdDate,
           COUNT(esd.id) AS sleeperCount
    FROM et_epoxy_treated_sleeper ets
    LEFT JOIN et_sleeper_details esd ON ets.id = esd.et_id
    WHERE ets.batch_number = :batchNo
      AND ets.id = (SELECT MAX(id) FROM et_epoxy_treated_sleeper WHERE batch_number = :batchNo)
    GROUP BY ets.created_date
""", nativeQuery = true)
    EtProjection getETData(@Param("batchNo") String batchNo);
}
