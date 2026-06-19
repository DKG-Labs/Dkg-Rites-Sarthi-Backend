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
    WHERE d.plantId = :plantId
    AND d.vendorCode = :vendorCode
    AND d.shift = :shift
    AND d.createdBy = :createdBy
    AND d.createdDate BETWEEN :startOfDay AND :endOfDay
 
""")
    List<DemouldingInspection> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            String createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
SELECT COUNT(d) > 0 
FROM DemouldingInspection d 
WHERE d.batchNo = :batchNo
""")
    boolean existsDemoulding(String batchNo);

    @Query("""
SELECT COUNT(d.id)
FROM DemouldingDefectiveSleeper d
WHERE d.inspection.batchNo = :batchNo
""")
    Long countDemouldingRejected(String batchNo);


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
           COUNT(dds.id) AS rejectedCount
    FROM demoulding_inspection di
    JOIN demoulding_defective_sleepers dds
         ON di.id = dds.inspection_id
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
