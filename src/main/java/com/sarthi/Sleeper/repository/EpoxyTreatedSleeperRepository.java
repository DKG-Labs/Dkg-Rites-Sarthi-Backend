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

    // Fix: COLLATE must only be applied on column side, not on ? params.
    // Use CONVERT(:param USING utf8mb4) COLLATE utf8mb4_unicode_ci for parameter values.
    @Query(value = """
SELECT
    et.batch_number,
    et.location,
    pd.casting_date,
    pd.total_casted_sleepers,
    COUNT(esd.id),
    COALESCE(et.plant_id, pd.plant_id),
    COALESCE(et.vendor_code, pd.vendor_code),
    et.created_by
FROM et_epoxy_treated_sleeper et
JOIN production_declaration pd
    ON pd.batch_number COLLATE utf8mb4_unicode_ci = et.batch_number COLLATE utf8mb4_unicode_ci
    AND pd.production_unit COLLATE utf8mb4_unicode_ci = et.location COLLATE utf8mb4_unicode_ci
JOIN et_sleeper_details esd
    ON esd.et_id = et.id
WHERE (:plantId IS NULL OR :plantId = ''
    OR et.plant_id COLLATE utf8mb4_unicode_ci = CONVERT(:plantId USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR pd.plant_id COLLATE utf8mb4_unicode_ci = CONVERT(:plantId USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR et.plant_id COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:plantId USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci
    OR pd.plant_id COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:plantId USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci)
  AND (:vendorCode IS NULL OR :vendorCode = ''
    OR et.vendor_code COLLATE utf8mb4_unicode_ci = CONVERT(:vendorCode USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR pd.vendor_code COLLATE utf8mb4_unicode_ci = CONVERT(:vendorCode USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR et.vendor_code COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorCode USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci
    OR pd.vendor_code COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorCode USING utf8mb4), '%') COLLATE utf8mb4_unicode_ci)
  AND (:createdBy IS NULL OR et.created_by = :createdBy)
GROUP BY et.batch_number, et.location, pd.casting_date, pd.total_casted_sleepers, et.plant_id, pd.plant_id, et.vendor_code, pd.vendor_code, et.created_by
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
