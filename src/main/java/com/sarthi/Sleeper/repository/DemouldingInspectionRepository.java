package com.sarthi.Sleeper.repository;



import com.sarthi.Sleeper.dto.SleeperDashboardDtos.DemouldingProjection;
import com.sarthi.Sleeper.entity.DemouldingInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemouldingInspectionRepository extends JpaRepository<DemouldingInspection, Long> {

    List<DemouldingInspection> findByBatchNoOrderByIdDesc(String batchNo);

    @Query("""
    SELECT d FROM DemouldingInspection d
    WHERE d.plantId = :plantId
    AND d.vendorCode = :vendorCode
    AND d.shift = :shift
    AND d.createdBy = :createdBy
    AND d.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<DemouldingInspection> findTodayData(
            String plantId,
            String vendorCode,
            String shift,
            String createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
    SELECT d FROM DemouldingInspection d
    WHERE (:plantId IS NULL OR :plantId = '' OR d.plantId = :plantId OR d.plantId LIKE CONCAT('%', :plantId, '%'))
    AND (:vendorCode IS NULL OR :vendorCode = '' OR d.vendorCode = :vendorCode OR d.vendorCode LIKE CONCAT('%', :vendorCode, '%'))
    AND (:shift IS NULL OR :shift = '' OR d.shift = :shift)
    AND (:createdBy IS NULL OR :createdBy = '' OR d.createdBy = :createdBy OR :createdBy IS NOT NULL)
    AND (d.createdDate BETWEEN :startOfDay AND :endOfDay OR d.updatedDate BETWEEN :startOfDay AND :endOfDay)
""")
    List<DemouldingInspection> findByDate(
            @Param("plantId") String plantId,
            @Param("vendorCode") String vendorCode,
            @Param("shift") String shift,
            @Param("createdBy") String createdBy,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
SELECT COUNT(d) > 0 
FROM DemouldingInspection d 
WHERE d.batchNo = :batchNo
""")
    boolean existsDemoulding(String batchNo);

    @Query(value = """
SELECT COUNT(ps.id)
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection di ON di.id = d.inspection_id
JOIN production_declaration pd ON pd.batch_number COLLATE utf8mb4_unicode_ci = di.batch_no COLLATE utf8mb4_unicode_ci
LEFT JOIN production_stress_chamber c ON c.declaration_id = pd.id
LEFT JOIN production_bench_group bg ON bg.chamber_id = c.id
LEFT JOIN production_longline_gang g ON g.declaration_id = pd.id
JOIN production_sleeper ps ON (ps.bench_group_id = bg.id OR ps.gang_id = g.id) AND ps.sleeper_no COLLATE utf8mb4_unicode_ci = d.sleeper_no COLLATE utf8mb4_unicode_ci
WHERE di.batch_no = :batchNo
  AND ( (d.visual_reason IS NOT NULL AND d.visual_reason <> '') OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '') )
""", nativeQuery = true)
    Long countDemouldingRejected(@Param("batchNo") String batchNo);

    @Query(value = """
SELECT di.batch_no, COUNT(ps.id)
FROM demoulding_defective_sleepers d
JOIN demoulding_inspection di ON di.id = d.inspection_id
JOIN production_declaration pd ON pd.batch_number COLLATE utf8mb4_unicode_ci = di.batch_no COLLATE utf8mb4_unicode_ci
LEFT JOIN production_stress_chamber c ON c.declaration_id = pd.id
LEFT JOIN production_bench_group bg ON bg.chamber_id = c.id
LEFT JOIN production_longline_gang g ON g.declaration_id = pd.id
JOIN production_sleeper ps ON (ps.bench_group_id = bg.id OR ps.gang_id = g.id) AND ps.sleeper_no COLLATE utf8mb4_unicode_ci = d.sleeper_no COLLATE utf8mb4_unicode_ci
WHERE di.batch_no IN :batchNos
  AND ( (d.visual_reason IS NOT NULL AND d.visual_reason <> '') OR (d.dim_reason IS NOT NULL AND d.dim_reason <> '') )
GROUP BY di.batch_no
""", nativeQuery = true)
    List<Object[]> countDemouldingRejectedByBatchNos(@Param("batchNos") List<String> batchNos);

   /* @Query(value = """
    SELECT di.inspection_date AS inspectionDate,
           COUNT(dds.id) AS rejectedCount
    FROM demoulding_inspection di
    LEFT JOIN demoulding_defective_sleepers dds 
           ON di.id = dds.inspection_id
    WHERE di.batch_no = :batchNo
    GROUP BY di.inspection_date
    LIMIT 1
""", nativeQuery = true)
    DemouldingProjection getDemouldingData(String batchNo);*/
   @Query(value = """
    SELECT di.inspection_date AS inspectionDate,
           COUNT(ps.id) AS rejectedCount
    FROM demoulding_inspection di
    JOIN demoulding_defective_sleepers dds ON di.id = dds.inspection_id
    JOIN production_declaration pd ON pd.batch_number COLLATE utf8mb4_unicode_ci = di.batch_no COLLATE utf8mb4_unicode_ci
    LEFT JOIN production_stress_chamber c ON c.declaration_id = pd.id
    LEFT JOIN production_bench_group bg ON bg.chamber_id = c.id
    LEFT JOIN production_longline_gang g ON g.declaration_id = pd.id
    JOIN production_sleeper ps ON (ps.bench_group_id = bg.id OR ps.gang_id = g.id) AND ps.sleeper_no COLLATE utf8mb4_unicode_ci = dds.sleeper_no COLLATE utf8mb4_unicode_ci
    WHERE di.batch_no = :batchNo
      AND (
            TRIM(COALESCE(dds.visual_reason, '')) <> ''
            OR
            TRIM(COALESCE(dds.dim_reason, '')) <> ''
          )
    GROUP BY di.inspection_date
    ORDER BY di.inspection_date DESC
    LIMIT 1
""", nativeQuery = true)
    DemouldingProjection getDemouldingData(String batchNo);


    @Query(value = """

        SELECT
            spm.company_name                       AS companyName,

            vp.plant_name                          AS plantName,

            di.plant_id                            AS plantId,

            ifm.rio                                AS rio,

            um.username                            AS ieName,

            'Process'                 AS stageOfInspection,

            di.shift                               AS shift,

            COUNT(
                DISTINCT CONCAT(
                    di.inspection_date,
                    '-',
                    di.shift
                )
            )                                      AS shiftsWorked,

           COUNT(
                   CASE
                       WHEN (
                           TRIM(COALESCE(dds.visual_reason, '')) <> ''
                           OR
                           TRIM(COALESCE(dds.dim_reason, '')) <> ''
                       )
                       THEN dds.id
                   END
               ) AS rejectedSleepers

        FROM demoulding_inspection di

        LEFT JOIN demoulding_defective_sleepers dds
               ON dds.inspection_id = di.id

        LEFT JOIN vendor_plant vp
               ON vp.plant_id COLLATE utf8mb4_unicode_ci =
                  di.plant_id COLLATE utf8mb4_unicode_ci

        LEFT JOIN sleeper_pincode_poi_mapping spm
               ON spm.vendor_code COLLATE utf8mb4_unicode_ci =
                  vp.vendor_id COLLATE utf8mb4_unicode_ci

        LEFT JOIN ie_fields_mapping ifm
               ON ifm.plant_pincode COLLATE utf8mb4_unicode_ci =
                  spm.pin_code COLLATE utf8mb4_unicode_ci

        LEFT JOIN user_master um
               ON um.userid = di.created_by

        WHERE di.inspection_date BETWEEN :fromDate AND :toDate

        GROUP BY
            spm.company_name,
            vp.plant_name,
            di.plant_id,
            ifm.rio,
            um.username,
            di.shift

        """, nativeQuery = true)
    List<Object[]> getProcessInspectionReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
