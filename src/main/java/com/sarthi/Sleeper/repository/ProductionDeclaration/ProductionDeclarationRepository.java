package com.sarthi.Sleeper.repository.ProductionDeclaration;

import com.sarthi.Sleeper.dto.BatchWithIdProjection;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto;
import com.sarthi.Sleeper.dto.ProductionDeclarationProjection;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.BatchProjection;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.Level2Projection;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.ProductionProjection;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.QualitySleeperReportProjection;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionDeclarationRepository extends JpaRepository<ProductionDeclaration, Long> {

    @Query("SELECT COALESCE(SUM(p.totalCastedSleepers), 0L) FROM ProductionDeclaration p")
    Long getTotalProductionCount();    /*
     * @Query("""
     * SELECT new
     * com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
     * d.id,
     * d.batchNumber,
     * b.sleeperType,
     * d.totalCastedSleepers,
     * COUNT(s.id),
     * 0.0,
     * 'Pending',
     * null
     * )
     * FROM ProductionDeclaration d
     * JOIN d.chambers c
     * JOIN c.benchGroups b
     * JOIN b.sleepers s
     * GROUP BY d.id,d.batchNumber,b.sleeperType,d.totalCastedSleepers
     * """)
     * List<BatchTestingListResponseDto> getAllBatchTesting();
     */
  /*  @Query("""
            SELECT new com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
            d.id,
            d.batchNumber,
            b.sleeperType,
            d.totalCastedSleepers,
            COUNT(s.id),
            0.0,
            'Pending',
            null,
            d.plantId
            )
            FROM ProductionDeclaration d
            JOIN d.chambers c
            JOIN c.benchGroups b
            JOIN b.sleepers s
            JOIN SleeperWorkflowTransaction w
                 ON w.requestId = CAST(d.id as string)
            WHERE w.status = 'Completed'
            GROUP BY d.id,d.batchNumber,b.sleeperType,d.totalCastedSleepers, d.plantId
            """)
    List<BatchTestingListResponseDto> getAllBatchTesting();*/
   /* @Query("""
SELECT new com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
d.id,
d.batchNumber,
b.sleeperType,
d.totalCastedSleepers,
COUNT(s.id),
0.0,
'Pending',
null,
d.plantId,
d.castingDate
)
FROM ProductionDeclaration d
JOIN d.chambers c
JOIN c.benchGroups b
JOIN b.sleepers s
JOIN SleeperWorkflowTransaction w
     ON CAST(w.requestId as long) = d.id
WHERE w.status = 'Completed'
GROUP BY d.id,d.batchNumber,b.sleeperType,d.totalCastedSleepers, d.plantId, d.castingDate
""")
    List<BatchTestingListResponseDto> getAllBatchTesting();
*/
    @Query("""
SELECT new com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
d.id,
d.batchNumber,
b.sleeperType,
b.sleeperCategory,
d.totalCastedSleepers,
CAST(d.totalCastedSleepers as long),
0.0,
'Pending',
null,
d.plantId,
d.castingDate
)
FROM ProductionDeclaration d
JOIN d.chambers c
JOIN c.benchGroups b
WHERE d.plantId = :plantId
GROUP BY d.id,d.batchNumber,b.sleeperType,b.sleeperCategory,d.totalCastedSleepers,d.plantId,d.castingDate
""")
    List<BatchTestingListResponseDto> getAllBatchTesting(String plantId);
  /*  @Query("""
SELECT new com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
d.id,
d.batchNumber,
g.sleeperType,
d.totalCastedSleepers,
COUNT(s.id),
0.0,
'Pending',
null,
d.plantId,
d.castingDate
)
FROM ProductionDeclaration d
JOIN d.gangs g
JOIN g.sleepers s
JOIN SleeperWorkflowTransaction w
     ON CAST(w.requestId as long) = d.id
WHERE w.status = 'Completed'
GROUP BY d.id,d.batchNumber,g.sleeperType,d.totalCastedSleepers,d.plantId,d.castingDate
""")
    List<BatchTestingListResponseDto> getLongLineBatchTesting();
*/
  @Query("""
SELECT new com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
d.id,
d.batchNumber,
g.sleeperType,
g.sleeperCategory,
d.totalCastedSleepers,
CAST(d.totalCastedSleepers as long),
0.0,
'Pending',
null,
d.plantId,
d.castingDate
)
FROM ProductionDeclaration d
JOIN d.gangs g
WHERE d.plantId = :plantId
GROUP BY d.id,d.batchNumber,g.sleeperType,g.sleeperCategory,d.totalCastedSleepers,d.plantId,d.castingDate
""")
  List<BatchTestingListResponseDto> getLongLineBatchTesting(String plantId);
    @Query("""
            SELECT d
            FROM ProductionDeclaration d
            WHERE d.id = :batchId
            """)
    ProductionDeclaration findBatchById(Long batchId);

    List<ProductionDeclaration> findByCreatedBy(Long createdBy);

    List<ProductionDeclaration> findByIdIn(List<Long> ids);

    @Query("SELECT DISTINCT p.batchNumber FROM ProductionDeclaration p " +
            "WHERE p.createdBy = :createdBy AND p.castingDate = :castingDate")
    List<String> findBatchNumbers(@Param("createdBy") Long createdBy,
                                  @Param("castingDate") LocalDate castingDate);

//    @Query("SELECT DISTINCT b.benchNo FROM ProductionBenchGroup b " +
//            "WHERE b.chamber.declaration.batchNumber = :batchNo")
//    List<String> findBenchNumbers(String batchNo);

    @Query(value = """
        SELECT DISTINCT b.bench_no
        FROM production_bench_group b
        JOIN production_stress_chamber c
            ON b.chamber_id = c.id
        JOIN production_declaration d
            ON c.declaration_id = d.id
        JOIN sleeper_workflow_transaction w 
            ON w.request_id = d.id
        WHERE d.batch_number = :batchNo
        AND d.production_unit = :productionUnit
        AND d.casting_date = :castingDate
        AND w.module_id = 11
        AND LOWER(w.status) = 'completed'
        AND w.workflow_transition_id = (
            SELECT MAX(w2.workflow_transition_id)
            FROM sleeper_workflow_transaction w2
            WHERE w2.request_id = d.id
              AND w2.module_id = 11
        )
        """, nativeQuery = true)
    List<String> findBenchNumbers(String batchNo, String productionUnit, @Param("castingDate") java.time.LocalDate castingDate);

    @Query(value = """
            SELECT DISTINCT p.batch_number
            FROM production_declaration p
            JOIN sleeper_workflow_transaction w 
              ON w.request_id = p.id
            WHERE p.created_by = :vendorId
              AND p.casting_date = :castingDate
              AND p.plant_id = :plantId
              AND p.production_unit = :productionUnit
              AND w.module_id = 11
              AND LOWER(w.status) = 'completed'
              AND w.workflow_transition_id = (
                  SELECT MAX(w2.workflow_transition_id)
                  FROM sleeper_workflow_transaction w2
                  WHERE w2.request_id = p.id
                    AND w2.module_id = 11
              )
            """, nativeQuery = true)
    List<String> findValidBatchNumbers(
            Long vendorId,
            LocalDate castingDate,
            String plantId,
            String productionUnit
    );


    @Query(value = """
            SELECT p.*
            FROM production_declaration p
            JOIN sleeper_workflow_transaction w 
              ON w.request_id = p.id
            WHERE p.batch_number = :batchNo
              AND w.module_id = 11
              AND LOWER(w.status) = 'completed'
              AND w.workflow_transition_id = (
                  SELECT MAX(w2.workflow_transition_id)
                  FROM sleeper_workflow_transaction w2
                  WHERE w2.request_id = p.id
                    AND w2.module_id = 11
              )
            LIMIT 1
            """, nativeQuery = true)
    ProductionDeclaration findByBatchNumber(@Param("batchNo") String batchNo);

    @Query(value = """
            SELECT p.*
            FROM production_declaration p
            JOIN sleeper_workflow_transaction w 
              ON w.request_id = p.id
            WHERE p.batch_number = :batchNo
              AND w.module_id = 11
              AND LOWER(w.status) = 'completed'
              AND w.workflow_transition_id = (
                  SELECT MAX(w2.workflow_transition_id)
                  FROM sleeper_workflow_transaction w2
                  WHERE w2.request_id = p.id
                    AND w2.module_id = 11
              )
            """, nativeQuery = true)
    List<ProductionDeclaration> findAllByBatchNumber(@Param("batchNo") String batchNo);



    @Query(value = """
            SELECT DISTINCT b.bench_no AS value, NULL AS gang_from, NULL AS gang_to
            FROM production_bench_group b
            JOIN production_stress_chamber c ON b.chamber_id = c.id
            JOIN production_declaration d ON c.declaration_id = d.id
            WHERE d.batch_number = :batchNo

            UNION

            SELECT DISTINCT NULL AS value, g.gang_from, g.gang_to
            FROM production_longline_gang g
            JOIN production_declaration d ON g.declaration_id = d.id
            WHERE d.batch_number = :batchNo
            """, nativeQuery = true)
    List<Object[]> findBenchAndGangRaw(String batchNo);

  /*  @Query("""
SELECT DISTINCT g.sleeperType 
FROM ProductionLongLineGang g
WHERE g.declaration.batchNumber = :batchNo
AND :benchNo BETWEEN g.gangFrom AND g.gangTo
""")
    List<String> findSleeperTypes(String batchNo, Integer benchNo);  */

    @Query("""
            SELECT DISTINCT g.sleeperType 
            FROM ProductionLongLineGang g
            WHERE g.declaration.batchNumber = :batchNo
            AND (
                               (g.gangFrom IS NOT NULL AND g.gangTo IS NOT NULL AND :benchNo BETWEEN g.gangFrom AND g.gangTo)
                               OR (g.gangNo IS NOT NULL AND g.gangNo = :benchNo)
            )
            """)
    List<String> findSleeperTypes(String batchNo, Integer benchNo);

    /*  @Query("""
   SELECT DISTINCT g.sleeperType
   FROM ProductionLongLineGang g
   WHERE g.declaration.batchNumber = :batchNo
   AND (
           (g.mode = 'RANGE' AND g.gangFrom IS NOT NULL AND g.gangTo IS NOT NULL
                AND :benchNo BETWEEN g.gangFrom AND g.gangTo)
        OR (g.mode = 'SINGLE' AND g.gangNo IS NOT NULL AND g.gangNo = :benchNo)
   )
   """)
      List<String> findSleeperTypes(String batchNo, Integer benchNo);
   */
 /*   @Query(value = """
SELECT DISTINCT g.gang_from, g.gang_to
FROM production_longline_gang g
JOIN production_declaration d 
    ON g.declaration_id = d.id
WHERE d.batch_number = :batchNo
""", nativeQuery = true)
    List<Object[]> findGangRanges(String batchNo);  */
   /* @Query(value = """
            SELECT DISTINCT g.mode, g.gang_from, g.gang_to, g.gang_no
            FROM production_longline_gang g
            JOIN production_declaration d 
                ON g.declaration_id = d.id
            WHERE d.batch_number = :batchNo
            """, nativeQuery = true)
    List<Object[]> findGangRanges(String batchNo); */
    @Query(value = """
        SELECT DISTINCT g.mode, g.gang_from, g.gang_to, g.gang_no
        FROM production_longline_gang g
        JOIN production_declaration d
            ON g.declaration_id = d.id
        JOIN sleeper_workflow_transaction w 
            ON w.request_id = d.id
        WHERE d.batch_number = :batchNo
        AND d.production_unit = :productionUnit
        AND d.casting_date = :castingDate
        AND w.module_id = 11
        AND LOWER(w.status) = 'completed'
        AND w.workflow_transition_id = (
            SELECT MAX(w2.workflow_transition_id)
            FROM sleeper_workflow_transaction w2
            WHERE w2.request_id = d.id
              AND w2.module_id = 11
        )
        """, nativeQuery = true)
    List<Object[]> findGangRanges(String batchNo, String productionUnit, @Param("castingDate") java.time.LocalDate castingDate);

    @Query(value = """
            SELECT DISTINCT 
                p.batch_number AS batchNumber,
                p.id AS id
            FROM production_declaration p
            JOIN sleeper_workflow_transaction w 
              ON w.request_id = p.id
            WHERE p.created_by = :vendorId
              AND p.casting_date = :castingDate
              AND p.plant_id = :plantId
              AND p.production_unit = :productionUnit
              AND w.module_id = 11
              AND LOWER(w.status) = 'completed'
              AND w.workflow_transition_id = (
                  SELECT MAX(w2.workflow_transition_id)
                  FROM sleeper_workflow_transaction w2
                  WHERE w2.request_id = p.id
                    AND w2.module_id = 11
              )
            """, nativeQuery = true)
    List<BatchWithIdProjection> findBatchWithId(
            Long vendorId,
            LocalDate castingDate,
            String plantId,
            String productionUnit
    );

    @Query(value = """
            SELECT p.*
            FROM production_declaration p
            JOIN sleeper_workflow_transaction w 
              ON w.request_id = p.id
            WHERE p.batch_number = :batchNo
              AND p.production_unit = :productionUnit
              AND w.module_id = 11
              AND LOWER(w.status) = 'completed'
              AND w.workflow_transition_id = (
                  SELECT MAX(w2.workflow_transition_id)
                  FROM sleeper_workflow_transaction w2
                  WHERE w2.request_id = p.id
                    AND w2.module_id = 11
              )
            LIMIT 1
            """, nativeQuery = true)
    ProductionDeclaration findByBatchNumberAndProductionUnit(@Param("batchNo") String batchNo, @Param("productionUnit") String productionUnit);

    @Query(value = """
            SELECT p.*
            FROM production_declaration p
            JOIN sleeper_workflow_transaction w 
              ON w.request_id = p.id
            WHERE p.batch_number = :batchNo
              AND p.production_unit = :productionUnit
              AND w.module_id = 11
              AND LOWER(w.status) = 'completed'
              AND w.workflow_transition_id = (
                  SELECT MAX(w2.workflow_transition_id)
                  FROM sleeper_workflow_transaction w2
                  WHERE w2.request_id = p.id
                    AND w2.module_id = 11
              )
            """, nativeQuery = true)
    List<ProductionDeclaration> findAllByBatchNumberAndProductionUnit(@Param("batchNo") String batchNo, @Param("productionUnit") String productionUnit);


    @Query(value = """
                SELECT pd.* FROM production_declaration pd
                WHERE pd.batch_number COLLATE utf8mb4_unicode_ci NOT IN (
                    SELECT mor.batch_number COLLATE utf8mb4_unicode_ci FROM moment_of_resistance mor
                )
            """, nativeQuery = true)
    List<ProductionDeclaration> findAllExcludingMR();

    @Query("""
            SELECT d.batchNumber
            FROM ProductionDeclaration d
            WHERE d.id = :batchId
            """)
    String getBatchNoById(Long batchId);

    @Query(value = """
                        SELECT
                                                          vp.plant_name,
                                                          vp.company_name,
                                                      
                                                          COALESCE(SUM(pd.total_casted_sleepers), 0) AS production,
                                                      
                                                          COALESCE(COUNT(DISTINCT d.id), 0) AS process_rejection,
                                                      
                                                          COALESCE(COUNT(DISTINCT r.id), 0) AS final_rejection,
                                                      
                                                          (
                                                              COALESCE(SUM(pd.total_casted_sleepers), 0)
                                                              - COALESCE(COUNT(DISTINCT d.id), 0)
                                                              - COALESCE(COUNT(DISTINCT r.id), 0)
                                                          ) AS acceptance,
                                                      
                                                          CASE
                                                              WHEN SUM(pd.total_casted_sleepers) = 0 THEN 0
                                                              ELSE (
                                                                  (COALESCE(COUNT(DISTINCT d.id), 0)
                                                                  + COALESCE(COUNT(DISTINCT r.id), 0)) * 100.0
                                                                  / SUM(pd.total_casted_sleepers)
                                                              )
                                                          END AS rejection_percentage
                                                      
                                                      FROM vendor_plant vp
                                                      
                                                      LEFT JOIN production_declaration pd
                                                          ON pd.plant_id = vp.plant_id
                                                          AND DATE(pd.casting_date) BETWEEN :startDate AND :endDate
                                                      
                                                      LEFT JOIN demoulding_defective_sleepers d
                                                          ON d.plant_id = vp.plant_id
                                                      
                                                      LEFT JOIN inspection_test_result r
                                                          ON r.plant_id = vp.plant_id
                                                          AND r.result = 'REJECTED'
                                                          AND r.active = true
                                                      
                                                      GROUP BY vp.plant_name, vp.company_name
                                                      ORDER BY vp.plant_name;
            """, nativeQuery = true)
    List<Object[]> getMonthlyAnalysis(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query(value = """
            SELECT vp.plant_id, vp.plant_name, vp.company_name,
                   COALESCE(SUM(pd.total_casted_sleepers), 0)
            FROM vendor_plant vp
            LEFT JOIN production_declaration pd 
                ON pd.plant_id COLLATE utf8mb4_unicode_ci = vp.plant_id
                AND pd.created_date BETWEEN :startDate AND :endDate
            GROUP BY vp.plant_id, vp.plant_name, vp.company_name
            """, nativeQuery = true)
    List<Object[]> getProduction(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
/*
    @Query(value = """
            SELECT 
                vp.plant_id,

                CONCAT(sp.company_name, ' - ', vp.plant_id) AS plant_name,

                im.rio AS inspected_by

            FROM sleeper_pincode_poi_mapping sp

            JOIN vendor_plant vp
                ON vp.vendor_id COLLATE utf8mb4_unicode_ci 
                   = sp.vendor_code COLLATE utf8mb4_unicode_ci

            LEFT JOIN ie_fields_mapping im
                ON im.pin_code COLLATE utf8mb4_unicode_ci 
                   = sp.pin_code COLLATE utf8mb4_unicode_ci

            GROUP BY vp.plant_id, sp.company_name, im.rio
            """, nativeQuery = true)
    List<Object[]> getPlantMasterData();
*/
@Query(value = """
        SELECT 
            vp.plant_id,

            CONCAT(vp.company_name, ' - ', vp.plant_id) AS plant_name,

            vp.rio AS inspected_by

        FROM vendor_plant vp

        GROUP BY vp.plant_id, vp.company_name, vp.rio
        """, nativeQuery = true)
List<Object[]> getPlantMasterData();

    @Query(value = """
                SELECT pd.total_casted_sleepers AS totalCastedSleepers,
                       pd.casting_date AS castingDate,
                       pd.total_sleeper_types AS totalSleeperTypes
                FROM production_declaration pd
                WHERE pd.id = :id AND pd.batch_number = :batchNo
            """, nativeQuery = true)
    ProductionProjection getProductionData(Long id, String batchNo);

    @Query(value = """
                SELECT id,
                       batch_number AS batchNumber
                FROM production_declaration
                WHERE plant_id = :plantId
            """, nativeQuery = true)
    List<BatchProjection> getBatches(String plantId);

    @Query(value = """

            -- DEMOULDING
            SELECT 
                CAST(CONCAT(di.inspection_date, ' (', di.shift, ')') AS CHAR),
                NULL,
                COUNT(dds.id),
                NULL,NULL,NULL,NULL,NULL
            FROM demoulding_inspection di
            LEFT JOIN demoulding_defective_sleepers dds 
                   ON di.id = dds.inspection_id
            WHERE di.batch_no = :batchNo
            GROUP BY di.inspection_date, di.shift

            UNION ALL

            SELECT 
                CAST(CONCAT(scd.date_of_testing, ' (', sct.shift, ')') AS CHAR),
                ROUND(AVG(scd.strength), 2),
                NULL,NULL,NULL,NULL,NULL,NULL
            FROM steam_cube_testing sct
            JOIN steam_cube_testing_details scd 
                 ON sct.id = scd.steam_cube_testing_id
            WHERE sct.batch_no = :batchNo
            GROUP BY scd.date_of_testing, sct.shift

            UNION ALL

            -- WATER CUBE
            SELECT 
                CAST(CONCAT(DATE(wc.created_date), ' (', wc.shift, ')') AS CHAR),
                NULL,NULL,NULL,NULL,NULL,
                ROUND(AVG(wcd.strength_nmm2),2),
                NULL
            FROM water_cube_strength_test wc
            JOIN water_cube_strength_detail wcd 
                 ON wc.id = wcd.strength_test_id
            WHERE wc.batch_number = :batchNo
            GROUP BY wc.created_date, wc.shift

            UNION ALL

            -- FINAL INSPECTION
            SELECT 
                CAST(CONCAT(ith.test_date, ' (', ith.shift, ')') AS CHAR),
                NULL,
                NULL,
                COUNT(CASE WHEN itr.module_id = 1 AND itr.result = 'REJECTED' THEN 1 END),
                COUNT(CASE WHEN itr.module_id = 2 AND itr.result = 'REJECTED' THEN 1 END),
                COUNT(CASE WHEN itr.module_id = 3 AND itr.result = 'REJECTED' THEN 1 END),
                NULL,
                NULL
            FROM inspection_test_header ith
            JOIN inspection_test_result itr 
                 ON ith.id = itr.test_header_id 
                 AND itr.active = 1
            WHERE ith.batch_id = :batchId
            GROUP BY ith.test_date, ith.shift

            UNION ALL

            --  MR
            SELECT 
                CAST(CONCAT(DATE(mrt.created_date), ' (', mrt.shift, ')') AS CHAR),
                NULL,NULL,NULL,NULL,NULL,NULL,
                ROUND(AVG(mrd.ct),2)
            FROM moment_of_resistance_test mrt
            JOIN moment_of_resistance_detail mrd 
                 ON mrt.id = mrd.mr_test_id
            WHERE mrt.batch_number = :batchNo
            GROUP BY mrt.created_date, mrt.shift

            ORDER BY 1

            """, nativeQuery = true)
    List<Object[]> getBatchCheckingReport(String batchNo, Long batchId);

    @Query("""
                SELECT p 
                FROM ProductionDeclaration p
                WHERE p.batchNumber = :batchNo
            """)
    ProductionDeclaration getProductionByBatch(String batchNo);

    @Query(value = """

SELECT 
    ph.po_no AS poNo,
    pi.item_sr_no AS srNo,
    pi.consignee_detail AS consignee,

    pi.qty AS qty,
    pi.uom AS uom,

    pi.delivery_date AS deliveryDate,
    pi.extended_delivery_date AS extendedDeliveryDate,

    -- 🔹 TOTAL ACCEPTED
    (
        SELECT COUNT(gs.batch_id)
        FROM sleeper_inspection_call sic
        JOIN sleeper_inspection_call_batch b 
             ON sic.id = b.inspection_call_id
        JOIN sleeper_ic_good_sleepers gs 
             ON gs.batch_id = b.id
        WHERE sic.po_no = ph.po_no
          AND sic.sr_no = pi.item_sr_no
    ) AS totalAccepted,

    -- 🔹 PROCESS REJECTION
    (
        SELECT COUNT(dds.id)
        FROM demoulding_inspection di
        JOIN demoulding_defective_sleepers dds 
             ON di.id = dds.inspection_id
        WHERE di.batch_no IN (
            SELECT b.batch_no
            FROM sleeper_inspection_call_batch b
            JOIN sleeper_inspection_call sic 
                 ON sic.id = b.inspection_call_id
            WHERE sic.po_no = ph.po_no
              AND sic.sr_no = pi.item_sr_no
        )
    ) AS processRejected,

  
    (
        SELECT COUNT(itr.id)
        FROM inspection_test_header ith
        JOIN inspection_test_result itr 
             ON ith.id = itr.test_header_id
        WHERE itr.result = 'REJECTED'
          AND itr.active = 1
          AND itr.module_id IN (1,2,3)
          AND ith.batch_id IN (
              SELECT pd.id
              FROM production_declaration pd
              WHERE pd.batch_number IN (
                  SELECT b.batch_no
                  FROM sleeper_inspection_call_batch b
                  JOIN sleeper_inspection_call sic 
                       ON sic.id = b.inspection_call_id
                  WHERE sic.po_no = ph.po_no
                    AND sic.sr_no = pi.item_sr_no
              )
          )
    ) AS finalRejected

FROM po_header ph
JOIN po_item pi 
     ON ph.id = pi.po_header_id


WHERE ph.po_no = :poNo COLLATE utf8mb4_unicode_ci

""", nativeQuery = true)
    List<Level2Projection> getLevel2Data(String poNo);

  /*  @Query(value = """

        SELECT

            DATE_FORMAT(pd.casting_date, '%b %Y') AS month,

            SUM(pd.total_casted_sleepers) AS inspectedNos,

            (
                COALESCE(SUM(dem.reject_count),0)
                +
                COALESCE(SUM(test.reject_count),0)
            ) AS rejectedNos

        FROM production_declaration pd


        LEFT JOIN (

            SELECT
                di.batch_no,
                COUNT(dds.id) AS reject_count

            FROM demoulding_inspection di

            LEFT JOIN demoulding_defective_sleepers dds
                ON dds.inspection_id = di.id

            GROUP BY di.batch_no

        ) dem
            ON dem.batch_no COLLATE utf8mb4_unicode_ci = pd.batch_number COLLATE utf8mb4_unicode_ci



        LEFT JOIN (

            SELECT
                ith.batch_id,
                COUNT(itr.id) AS reject_count

            FROM inspection_test_result itr

            INNER JOIN inspection_test_header ith
                ON ith.id = itr.test_header_id

            WHERE itr.result = 'REJECTED'

            GROUP BY ith.batch_id

        ) test
            ON test.batch_id = pd.id

        WHERE pd.casting_date BETWEEN :fromDate AND :toDate
          AND pd.plant_id = :plantId



          AND EXISTS (

                SELECT 1

                FROM inspection_test_header ith

                WHERE ith.batch_id = pd.id
                  AND ith.module_id IN (1,2,3)

                GROUP BY ith.batch_id

                HAVING COUNT(DISTINCT ith.module_id) = 3
          )



          AND EXISTS (

                SELECT 1

                FROM steam_cube_sample_declaration s

                WHERE s.batch_no COLLATE utf8mb4_unicode_ci = pd.batch_number COLLATE utf8mb4_unicode_ci
          )



          AND EXISTS (

                SELECT 1

                FROM water_cube_strength_test w

                WHERE w.batch_number COLLATE utf8mb4_unicode_ci = pd.batch_number COLLATE utf8mb4_unicode_ci
          )

        GROUP BY
            DATE_FORMAT(pd.casting_date, '%Y-%m'),
            DATE_FORMAT(pd.casting_date, '%b %Y')

        ORDER BY
            DATE_FORMAT(pd.casting_date, '%Y-%m')

        """, nativeQuery = true)
    List<Object[]> getMonthlyPerformance(
            @Param("plantId") String plantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );  */

    @Query(value = """

    SELECT

        DATE_FORMAT(pd.casting_date, '%b %Y') AS month,

        SUM(pd.total_casted_sleepers) AS inspectedNos,

        (
            COALESCE(SUM(dem.reject_count),0)
            +
            COALESCE(SUM(test.reject_count),0)
        ) AS rejectedNos

    FROM production_declaration pd

    /* =========================================
       DEMOULDING REJECTION
       uses batch_number
       ========================================= */

    LEFT JOIN (

        SELECT
            di.batch_no,
            COUNT(dds.id) AS reject_count

        FROM demoulding_inspection di

        LEFT JOIN demoulding_defective_sleepers dds
            ON dds.inspection_id = di.id

        GROUP BY di.batch_no

    ) dem
        ON dem.batch_no COLLATE utf8mb4_unicode_ci =
           pd.batch_number COLLATE utf8mb4_unicode_ci

    /* =========================================
       INSPECTION REJECTION
       uses pd.id
       ========================================= */

    LEFT JOIN (

        SELECT
            ith.batch_id,
            COUNT(itr.id) AS reject_count

        FROM inspection_test_result itr

        INNER JOIN inspection_test_header ith
            ON ith.id = itr.test_header_id

        WHERE itr.result = 'REJECTED'

        GROUP BY ith.batch_id

    ) test
        ON test.batch_id = pd.id

    WHERE pd.casting_date BETWEEN :fromDate AND :toDate
      AND pd.plant_id = :plantId

    GROUP BY
        DATE_FORMAT(pd.casting_date, '%Y-%m'),
        DATE_FORMAT(pd.casting_date, '%b %Y')

    ORDER BY
        DATE_FORMAT(pd.casting_date, '%Y-%m')

    """, nativeQuery = true)
    List<Object[]> getMonthlyPerformance(
            @Param("plantId") String plantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


    @Query(value = """
SELECT
    finalData.castingDate,
    finalData.shift,
    finalData.lineOrShedNo,
    SUM(finalData.noOfBatches) AS noOfBatches,
    SUM(finalData.noOfSleepers) AS noOfSleepers,
    GROUP_CONCAT(
        DISTINCT finalData.sleeperTypesAndCounts
        SEPARATOR ', '
    ) AS sleeperTypesAndCounts,
    SUM(finalData.processRejectedSleepers) AS processRejectedSleepers,
    SUM(finalData.finalRejectedSleepers) AS finalRejectedSleepers,
    SUM(finalData.etRejectedSleepers) AS etRejectedSleepers
FROM (
    SELECT
        pd.id,
        pd.casting_date AS castingDate,
        pd.shift,
        pd.production_unit AS lineOrShedNo,
        1 AS noOfBatches,
        pd.total_casted_sleepers AS noOfSleepers,

        (
            SELECT GROUP_CONCAT(
                CONCAT(
                    temp.sleeper_type,
                    ' (',
                    temp.sleeper_count,
                    ')'
                )
                SEPARATOR ', '
            )
            FROM (
                SELECT
                    pbg.sleeper_type,
                    COUNT(ps.id) AS sleeper_count
                FROM production_stress_chamber psc
                INNER JOIN production_bench_group pbg
                    ON pbg.chamber_id = psc.id
                INNER JOIN production_sleeper ps
                    ON ps.bench_group_id = pbg.id
                WHERE psc.declaration_id = pd.id
                GROUP BY pbg.sleeper_type

                UNION ALL

                SELECT
                    plg.sleeper_type,
                    COUNT(ps.id) AS sleeper_count
                FROM production_longline_gang plg
                INNER JOIN production_sleeper ps
                    ON (
                        (
                            plg.mode = 'SINGLE'
                            AND ps.gang_id = plg.gang_no
                        )
                        OR
                        (
                            plg.mode = 'RANGE'
                            AND ps.gang_id BETWEEN plg.gang_from
                                               AND plg.gang_to
                        )
                    )
                WHERE plg.declaration_id = pd.id
                GROUP BY plg.sleeper_type
            ) temp
        ) AS sleeperTypesAndCounts,

        (
            SELECT COUNT(DISTINCT dds.id)
            FROM demoulding_inspection di
            INNER JOIN demoulding_defective_sleepers dds
                ON dds.inspection_id = di.id
            WHERE
                di.casting_date = pd.casting_date

                AND di.line_shed_no COLLATE utf8mb4_unicode_ci =
                    pd.production_unit COLLATE utf8mb4_unicode_ci

                AND di.batch_no COLLATE utf8mb4_unicode_ci =
                    pd.batch_number COLLATE utf8mb4_unicode_ci

                AND (
                    (
                        dds.visual_reason IS NOT NULL
                        AND dds.visual_reason <> ''
                    )
                    OR
                    (
                        dds.dim_reason IS NOT NULL
                        AND dds.dim_reason <> ''
                    )
                )
        ) AS processRejectedSleepers,

        (
            SELECT COUNT(DISTINCT itr.id)
            FROM inspection_test_header ith
            INNER JOIN inspection_test_result itr
                ON itr.test_header_id = ith.id
            WHERE
                ith.batch_id = pd.id
                AND itr.result = 'REJECTED'
                AND itr.active = 1
        ) AS finalRejectedSleepers,

        (
            SELECT COUNT(DISTINCT esd.id)
            FROM et_epoxy_treated_sleeper ets
            INNER JOIN et_sleeper_details esd
                ON esd.et_id = ets.id
            WHERE
                ets.batch_number COLLATE utf8mb4_unicode_ci =
                pd.batch_number COLLATE utf8mb4_unicode_ci
        ) AS etRejectedSleepers

    FROM production_declaration pd

    WHERE
        pd.casting_date BETWEEN :startDate AND :endDate
        AND pd.plant_id = :plantId

) finalData

GROUP BY
    finalData.castingDate,
    finalData.shift,
    finalData.lineOrShedNo

ORDER BY
    finalData.castingDate DESC
""", nativeQuery = true)
    List<Object[]> getShiftWiseProductionReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("plantId") String plantId
    );



  @Query(value = """

SELECT

    vp.cse AS cse,

    pd.plant_id AS plantId,

    'Mainline' AS sleeperType,

    COALESCE(SUM(pd.total_casted_sleepers),0)
        AS noOfSleepersProducedDuringMonth

FROM production_declaration pd

LEFT JOIN vendor_plant vp
    ON vp.plant_id = pd.plant_id

WHERE
    pd.casting_date BETWEEN :startDate AND :endDate

GROUP BY
    vp.cse,
    pd.plant_id

ORDER BY
    vp.cse

""", nativeQuery = true)
  List<Object[]> getPSCSleeperQualityReport(
          LocalDate startDate,
          LocalDate endDate
  );

/*
  @Query(value = """

SELECT

    x.railwayZone AS railwayZone,

    x.plantId AS plantId,

    NULL AS sleeperType,



    SUM(x.totalProducedSleepers)
        AS totalProducedSleepers,



    SUM(x.noOfSleeperInspectedInProcess)
        AS noOfSleeperInspectedInProcess,



    SUM(x.noOfSleeperRejectedInProcess)
        AS noOfSleeperRejectedInProcess



FROM (

    SELECT

        pd.id AS productionId,

        vp.zonal_railway AS railwayZone,

        pd.plant_id AS plantId,

        pd.batch_number AS batchNumber,



        -- produced sleepers per batch

        pd.total_casted_sleepers
            AS totalProducedSleepers,



        -- inspected sleepers

        COUNT(dds.id)
            AS noOfSleeperInspectedInProcess,



        -- rejected sleepers

        SUM(

            CASE

                WHEN (

                        dds.visual_reason IS NOT NULL
                        AND TRIM(dds.visual_reason) <> ''

                     )

                  OR (

                        dds.dim_reason IS NOT NULL
                        AND TRIM(dds.dim_reason) <> ''

                     )

                THEN 1

                ELSE 0

            END

        ) AS noOfSleeperRejectedInProcess



    FROM production_declaration pd



    LEFT JOIN vendor_plant vp

        ON vp.plant_id COLLATE utf8mb4_unicode_ci
         = pd.plant_id COLLATE utf8mb4_unicode_ci



    LEFT JOIN demoulding_inspection di

        ON di.batch_no COLLATE utf8mb4_unicode_ci
         = pd.batch_number COLLATE utf8mb4_unicode_ci



    LEFT JOIN demoulding_defective_sleepers dds

        ON dds.inspection_id = di.id



    WHERE DATE(pd.created_date)
    BETWEEN :startDate AND :endDate



    GROUP BY

        pd.id,
        vp.zonal_railway,
        pd.plant_id,
        pd.batch_number,
        pd.total_casted_sleepers

) x



GROUP BY

    x.railwayZone,
    x.plantId



ORDER BY

    x.railwayZone,
    x.plantId

""", nativeQuery = true)
  List<QualitySleeperReportProjection>
  getQualitySleeperReport(
          LocalDate startDate,
          LocalDate endDate);*/


  @Query(value = """

SELECT

    x.railwayZone AS railwayZone,

    x.plantId AS plantId,

    NULL AS sleeperType,


    -- =========================================
    -- TOTAL PRODUCED
    -- =========================================

    COALESCE(SUM(x.totalProducedSleepers),0)
        AS totalProducedSleepers,


    -- =========================================
    -- DEMOULDING INSPECTED
    -- =========================================

    COALESCE(SUM(x.noOfSleeperInspectedInProcess),0)
        AS noOfSleeperInspectedInProcess,


    -- =========================================
    -- DEMOULDING REJECTED
    -- =========================================

    COALESCE(SUM(x.noOfSleeperRejectedInProcess),0)
        AS noOfSleeperRejectedInProcess,


    -- =========================================
    -- DEFECT CATEGORY COUNTS
    -- =========================================

    COALESCE(SUM(x.forDimensionToeGauge),0)
        AS forDimensionToeGauge,

    COALESCE(SUM(x.forEndDamage),0)
        AS forEndDamage,

    COALESCE(SUM(x.honeyCombingSurfaceDefectCrack),0)
        AS honeyCombingSurfaceDefectCrack,

    COALESCE(SUM(x.missingDowel),0)
        AS missingDowel,

    COALESCE(SUM(x.otherDefectsInsertSinkTilt),0)
        AS otherDefectsInsertSinkTilt,


    -- =========================================
    -- TOTAL REJECTED DEFECTS
    -- =========================================

    COALESCE(SUM(x.totalRejectedDefects),0)
        AS totalRejectedDefects,


    -- =========================================
    -- REJECTION %
    -- =========================================

    ROUND(

        (
            COALESCE(SUM(x.totalRejectedDefects),0) * 100.0
        )

        /

        NULLIF(
            COALESCE(SUM(x.totalProducedSleepers),0),
            0
        ),

    2)

    AS rejectionPercentage


FROM (

    SELECT

        pd.id AS productionId,

        vp.zonal_railway AS railwayZone,

        pd.plant_id AS plantId,

        pd.batch_number AS batchNumber,


        -- =====================================
        -- PRODUCED SLEEPERS
        -- =====================================

        pd.total_casted_sleepers
            AS totalProducedSleepers,


        -- =====================================
        -- DEMOULDING INSPECTED
        -- =====================================

        COUNT(dds.id)
            AS noOfSleeperInspectedInProcess,


        -- =====================================
        -- DEMOULDING REJECTED
        -- =====================================

        SUM(

            CASE

                WHEN (

                        dds.visual_reason IS NOT NULL
                        AND TRIM(dds.visual_reason) <> ''

                     )

                  OR (

                        dds.dim_reason IS NOT NULL
                        AND TRIM(dds.dim_reason) <> ''

                     )

                THEN 1

                ELSE 0

            END

        ) AS noOfSleeperRejectedInProcess,


        -- =====================================
        -- FOR DIMENSION / TOE GAUGE
        -- =====================================

        COALESCE(

            (

                SELECT COUNT(DISTINCT itr.sleeper_id)

                FROM inspection_test_header ith

                JOIN inspection_test_result itr
                    ON itr.test_header_id = ith.id

                JOIN inspection_parameter_result ipr
                    ON ipr.test_result_id = itr.id

                JOIN inspection_reason_master irm
                    ON irm.id = ipr.reason_master_id

                WHERE ith.batch_id = pd.id

                  AND itr.active = 1

                  AND itr.result = 'REJECTED'

                  AND ipr.parameter_result = 'REJECTED'

                  AND irm.id IN (

                        51,52,

                        101,102,

                        161,162,

                        163,164,

                        211,212,

                        213,214,

                        215,216,

                        221,222,

                        223,224,

                        225,226,

                        231,232,

                        201,202

                  )

            ),

        0)

        AS forDimensionToeGauge,


        -- =====================================
        -- END DAMAGE
        -- =====================================

        COALESCE(

            (

                SELECT COUNT(DISTINCT itr.sleeper_id)

                FROM inspection_test_header ith

                JOIN inspection_test_result itr
                    ON itr.test_header_id = ith.id

                JOIN inspection_parameter_result ipr
                    ON ipr.test_result_id = itr.id

                JOIN inspection_reason_master irm
                    ON irm.id = ipr.reason_master_id

                WHERE ith.batch_id = pd.id

                  AND itr.active = 1

                  AND itr.result = 'REJECTED'

                  AND ipr.parameter_result = 'REJECTED'

                  AND irm.id = 7

            ),

        0)

        AS forEndDamage,


        -- =====================================
        -- HONEYCOMB / SURFACE / CRACK
        -- =====================================

        COALESCE(

            (

                SELECT COUNT(DISTINCT itr.sleeper_id)

                FROM inspection_test_header ith

                JOIN inspection_test_result itr
                    ON itr.test_header_id = ith.id

                JOIN inspection_parameter_result ipr
                    ON ipr.test_result_id = itr.id

                JOIN inspection_reason_master irm
                    ON irm.id = ipr.reason_master_id

                WHERE ith.batch_id = pd.id

                  AND itr.active = 1

                  AND itr.result = 'REJECTED'

                  AND ipr.parameter_result = 'REJECTED'

                  AND irm.id IN (

                        6,
                        8,
                        9,
                        10,
                        11,
                        12

                  )

            ),

        0)

        AS honeyCombingSurfaceDefectCrack,


        -- =====================================
        -- MISSING DOWEL
        -- =====================================

        COALESCE(

            (

                SELECT COUNT(DISTINCT itr.sleeper_id)

                FROM inspection_test_header ith

                JOIN inspection_test_result itr
                    ON itr.test_header_id = ith.id

                JOIN inspection_parameter_result ipr
                    ON ipr.test_result_id = itr.id

                JOIN inspection_reason_master irm
                    ON irm.id = ipr.reason_master_id

                WHERE ith.batch_id = pd.id

                  AND itr.active = 1

                  AND itr.result = 'REJECTED'

                  AND ipr.parameter_result = 'REJECTED'

                  AND irm.id = 16

            ),

        0)

        AS missingDowel,


        -- =====================================
        -- OTHER DEFECTS
        -- =====================================

        COALESCE(

            (

                SELECT COUNT(DISTINCT itr.sleeper_id)

                FROM inspection_test_header ith

                JOIN inspection_test_result itr
                    ON itr.test_header_id = ith.id

                JOIN inspection_parameter_result ipr
                    ON ipr.test_result_id = itr.id

                JOIN inspection_reason_master irm
                    ON irm.id = ipr.reason_master_id

                WHERE ith.batch_id = pd.id

                  AND itr.active = 1

                  AND itr.result = 'REJECTED'

                  AND ipr.parameter_result = 'REJECTED'

                  AND irm.id IN (

                        13,
                        14,
                        15,
                        17,
                        18,
                        19

                  )

            ),

        0)

        AS otherDefectsInsertSinkTilt,


        -- =====================================
        -- TOTAL REJECTED DEFECTS
        -- =====================================

        (

            COALESCE(

                (

                    SELECT COUNT(DISTINCT itr.sleeper_id)

                    FROM inspection_test_header ith

                    JOIN inspection_test_result itr
                        ON itr.test_header_id = ith.id

                    JOIN inspection_parameter_result ipr
                        ON ipr.test_result_id = itr.id

                    JOIN inspection_reason_master irm
                        ON irm.id = ipr.reason_master_id

                    WHERE ith.batch_id = pd.id

                      AND itr.active = 1

                      AND itr.result = 'REJECTED'

                      AND ipr.parameter_result = 'REJECTED'

                      AND irm.id IN (

                            51,52,

                            101,102,

                            161,162,

                            163,164,

                            211,212,

                            213,214,

                            215,216,

                            221,222,

                            223,224,

                            225,226,

                            231,232,

                            201,202

                      )

                ),

            0)

            +

            COALESCE(

                (

                    SELECT COUNT(DISTINCT itr.sleeper_id)

                    FROM inspection_test_header ith

                    JOIN inspection_test_result itr
                        ON itr.test_header_id = ith.id

                    JOIN inspection_parameter_result ipr
                        ON ipr.test_result_id = itr.id

                    JOIN inspection_reason_master irm
                        ON irm.id = ipr.reason_master_id

                    WHERE ith.batch_id = pd.id

                      AND itr.active = 1

                      AND itr.result = 'REJECTED'

                      AND ipr.parameter_result = 'REJECTED'

                      AND irm.id = 7

                ),

            0)

            +

            COALESCE(

                (

                    SELECT COUNT(DISTINCT itr.sleeper_id)

                    FROM inspection_test_header ith

                    JOIN inspection_test_result itr
                        ON itr.test_header_id = ith.id

                    JOIN inspection_parameter_result ipr
                        ON ipr.test_result_id = itr.id

                    JOIN inspection_reason_master irm
                        ON irm.id = ipr.reason_master_id

                    WHERE ith.batch_id = pd.id

                      AND itr.active = 1

                      AND itr.result = 'REJECTED'

                      AND ipr.parameter_result = 'REJECTED'

                      AND irm.id IN (

                            6,
                            8,
                            9,
                            10,
                            11,
                            12

                      )

                ),

            0)

            +

            COALESCE(

                (

                    SELECT COUNT(DISTINCT itr.sleeper_id)

                    FROM inspection_test_header ith

                    JOIN inspection_test_result itr
                        ON itr.test_header_id = ith.id

                    JOIN inspection_parameter_result ipr
                        ON ipr.test_result_id = itr.id

                    JOIN inspection_reason_master irm
                        ON irm.id = ipr.reason_master_id

                    WHERE ith.batch_id = pd.id

                      AND itr.active = 1

                      AND itr.result = 'REJECTED'

                      AND ipr.parameter_result = 'REJECTED'

                      AND irm.id = 16

                ),

            0)

            +

            COALESCE(

                (

                    SELECT COUNT(DISTINCT itr.sleeper_id)

                    FROM inspection_test_header ith

                    JOIN inspection_test_result itr
                        ON itr.test_header_id = ith.id

                    JOIN inspection_parameter_result ipr
                        ON ipr.test_result_id = itr.id

                    JOIN inspection_reason_master irm
                        ON irm.id = ipr.reason_master_id

                    WHERE ith.batch_id = pd.id

                      AND itr.active = 1

                      AND itr.result = 'REJECTED'

                      AND ipr.parameter_result = 'REJECTED'

                      AND irm.id IN (

                            13,
                            14,
                            15,
                            17,
                            18,
                            19

                      )

                ),

            0)

        )

        AS totalRejectedDefects


    FROM production_declaration pd


    LEFT JOIN vendor_plant vp

        ON vp.plant_id COLLATE utf8mb4_unicode_ci
         = pd.plant_id COLLATE utf8mb4_unicode_ci


    LEFT JOIN demoulding_inspection di

        ON di.batch_no COLLATE utf8mb4_unicode_ci
         = pd.batch_number COLLATE utf8mb4_unicode_ci


    LEFT JOIN demoulding_defective_sleepers dds

        ON dds.inspection_id = di.id


    WHERE DATE(pd.created_date)
    BETWEEN :startDate AND :endDate


    GROUP BY

        pd.id,
        vp.zonal_railway,
        pd.plant_id,
        pd.batch_number,
        pd.total_casted_sleepers

) x


GROUP BY

    x.railwayZone,
    x.plantId


ORDER BY

    x.railwayZone,
    x.plantId

""", nativeQuery = true)
  List<QualitySleeperReportProjection>
  getQualitySleeperReport(
          LocalDate startDate,
          LocalDate endDate);


    @Query("""
    SELECT
        p.productionUnit AS productionUnit,
        p.batchNumber AS batchNumber,
        p.castingDate AS castingDate,
        p.totalCastedSleepers AS totalCastedSleepers
    FROM ProductionDeclaration p
    WHERE p.id = :requestId
""")
    Optional<ProductionDeclarationProjection> findProductionDetailsByRequestId(Long requestId);

    @Query(value = """
    SELECT
        vp.plant_id,
        COUNT(DISTINCT ph.id) AS noOfPos,
        COALESCE(SUM(pi.qty),0) AS poQty,
        GROUP_CONCAT(DISTINCT pi.uom) AS uom
    FROM vendor_plant vp
    LEFT JOIN po_header ph
           ON ph.vendor_code COLLATE utf8mb4_unicode_ci = vp.vendor_code COLLATE utf8mb4_unicode_ci
    LEFT JOIN po_item pi
           ON pi.po_header_id = ph.id
    GROUP BY vp.plant_id
    """, nativeQuery = true)
    List<Object[]> getPoDetailsByPlant();

    @Query(value = """
SELECT
    vp.zonal_railway,

    ph.po_no,

    ph.po_date,

    COALESCE(SUM(pi.qty),0) AS poQty,

    CONCAT(vp.company_name,' - ',vp.plant_id) AS plantName,

    vp.rio AS inspectedBy,

    COALESCE(prod.production,0) AS production,

    COALESCE(proc.processRejection,0) AS processRejection,

    COALESCE(fin.finalRejection,0) AS finalRejection,

    COUNT(DISTINCT ph.id) AS noOfPos,

    GROUP_CONCAT(DISTINCT pi.uom) AS uom

FROM vendor_plant vp

LEFT JOIN po_header ph
       ON ph.vendor_code COLLATE utf8mb4_unicode_ci =
          vp.vendor_code COLLATE utf8mb4_unicode_ci

LEFT JOIN po_item pi
       ON pi.po_header_id = ph.id

LEFT JOIN
(
    SELECT
        pd.plant_id,
        SUM(pd.total_casted_sleepers) AS production
    FROM production_declaration pd
    WHERE pd.created_date BETWEEN :startDate AND :endDate
    GROUP BY pd.plant_id
) prod
ON prod.plant_id COLLATE utf8mb4_unicode_ci =
   vp.plant_id COLLATE utf8mb4_unicode_ci

LEFT JOIN
(
    SELECT
        di.plant_id,
        COUNT(ds.id) AS processRejection
    FROM demoulding_inspection di
    JOIN demoulding_defective_sleepers ds
         ON ds.inspection_id = di.id
    WHERE di.created_date BETWEEN :startDate AND :endDate
      AND (
            TRIM(COALESCE(ds.visual_reason,'')) <> ''
         OR TRIM(COALESCE(ds.dim_reason,'')) <> ''
      )
    GROUP BY di.plant_id
) proc
ON proc.plant_id COLLATE utf8mb4_unicode_ci =
   vp.plant_id COLLATE utf8mb4_unicode_ci

LEFT JOIN
(
    SELECT
        pd.plant_id,
        COUNT(DISTINCT CONCAT(h.batch_id,'-',r.sleeper_id)) AS finalRejection
    FROM production_declaration pd
    JOIN inspection_test_header h
         ON h.batch_id = pd.id
    JOIN inspection_test_result r
         ON r.test_header_id = h.id
    WHERE h.created_date BETWEEN :startDate AND :endDate
      AND r.result = 'REJECTED'
      AND r.active = TRUE
    GROUP BY pd.plant_id
) fin
ON fin.plant_id COLLATE utf8mb4_unicode_ci =
   vp.plant_id COLLATE utf8mb4_unicode_ci

WHERE vp.plant_id COLLATE utf8mb4_unicode_ci =
      CAST(:plantId AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci

GROUP BY
    vp.zonal_railway,
    ph.po_no,
    ph.po_date,
    vp.company_name,
    vp.plant_id,
    vp.rio,
    prod.production,
    proc.processRejection,
    fin.finalRejection

ORDER BY ph.po_date DESC
""", nativeQuery = true)
    List<Object[]> getPoWiseAnalysis(
            @Param("plantId") String plantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
